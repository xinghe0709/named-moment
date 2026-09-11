import test from 'node:test';
import assert from 'node:assert/strict';

import {ApiError, createEmotionApi} from '../../main/resources/static/js/api.mjs';

function successFetch(calls, data) {
  return async (url, options = {}) => {
    calls.push({url, options});
    return {
      ok: true,
      status: 200,
      json: async () => ({code: 0, message: 'success', data})
    };
  };
}

test('match posts trimmed user text to the matching endpoint', async () => {
  const calls = [];
  const api = createEmotionApi(successFetch(calls, {candidates: []}));

  const data = await api.match('想念从前的生活');

  assert.deepEqual(data, {candidates: []});
  assert.equal(calls[0].url, '/api/emotions/match');
  assert.equal(calls[0].options.method, 'POST');
  assert.equal(calls[0].options.headers['Content-Type'], 'application/json');
  assert.deepEqual(JSON.parse(calls[0].options.body), {text: '想念从前的生活'});
});

test('auth operations use same-origin cookie sessions', async () => {
  const calls = [];
  const api = createEmotionApi(successFetch(calls, {id: 4, username: 'reader'}));

  await api.register('reader', 'long-enough-password');
  await api.login('reader', 'long-enough-password');
  await api.me();
  await api.logout();

  assert.equal(calls[0].url, '/api/auth/register');
  assert.deepEqual(JSON.parse(calls[0].options.body), {
    username: 'reader', password: 'long-enough-password'
  });
  assert.equal(calls[0].options.credentials, 'same-origin');
  assert.equal(calls[1].url, '/api/auth/login');
  assert.equal(calls[2].url, '/api/auth/me');
  assert.equal(calls[3].url, '/api/auth/logout');
  assert.equal(calls[3].options.method, 'POST');
});

test('record operations use the existing REST contract', async () => {
  const calls = [];
  const payload = {inputText: '这一刻', conceptId: 7, matchScore: 91, explanation: '很接近'};
  const api = createEmotionApi(successFetch(calls, []));

  await api.saveRecord(payload);
  await api.listRecords();
  await api.deleteRecord(42);

  assert.equal(calls[0].url, '/api/emotions/records');
  assert.equal(calls[0].options.method, 'POST');
  assert.deepEqual(JSON.parse(calls[0].options.body), payload);
  assert.equal(calls[1].url, '/api/emotions/records');
  assert.equal(calls[1].options.method, 'GET');
  assert.equal(calls[2].url, '/api/emotions/records/42');
  assert.equal(calls[2].options.method, 'DELETE');
});

test('non-zero business responses become readable ApiError instances', async () => {
  const api = createEmotionApi(async () => ({
    ok: true,
    status: 200,
    json: async () => ({code: 1001, message: '输入不符合要求', data: null})
  }));

  await assert.rejects(() => api.match('测试输入内容'), error => {
    assert.equal(error instanceof ApiError, true);
    assert.equal(error.message, '输入不符合要求');
    assert.equal(error.code, 1001);
    return true;
  });
});

test('http and malformed responses become readable ApiError instances', async () => {
  const httpApi = createEmotionApi(async () => ({
    ok: false,
    status: 503,
    json: async () => ({message: '服务暂不可用'})
  }));
  const malformedApi = createEmotionApi(async () => ({
    ok: true,
    status: 200,
    json: async () => ({hello: 'world'})
  }));

  await assert.rejects(() => httpApi.listRecords(), /服务暂不可用/);
  await assert.rejects(() => malformedApi.listRecords(), /没有返回可识别的结果/);
});

test('failed responses expose the server request id for log lookup', async () => {
  const api = createEmotionApi(async () => ({
    ok: false,
    status: 500,
    headers: {get: name => name === 'X-Request-Id' ? 'request-debug-123' : null},
    json: async () => ({code: 50001, message: '情感指纹生成失败'})
  }));

  await assert.rejects(() => api.match('测试输入内容'), error => {
    assert.equal(error.requestId, 'request-debug-123');
    assert.match(error.message, /请求编号：request-debug-123/);
    return true;
  });
});
