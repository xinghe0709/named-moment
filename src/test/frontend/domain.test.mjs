import test from 'node:test';
import assert from 'node:assert/strict';

import {
  formatDate,
  formatScore,
  modeLabel,
  sanitizeSourceUrl,
  validateInput
} from '../../main/resources/static/js/domain.mjs';

test('validateInput trims valid text and reports its length', () => {
  assert.deepEqual(validateInput('  我今天忽然很想念以前的生活  '), {
    valid: true,
    text: '我今天忽然很想念以前的生活',
    count: 13,
    message: ''
  });
});

test('validateInput rejects text shorter than five characters', () => {
  assert.deepEqual(validateInput('想家'), {
    valid: false,
    text: '想家',
    count: 2,
    message: '再写一点吧，至少需要 5 个字。'
  });
});

test('validateInput rejects text longer than two thousand characters', () => {
  const result = validateInput('念'.repeat(2001));

  assert.equal(result.valid, false);
  assert.equal(result.count, 2001);
  assert.equal(result.message, '这段文字有些长，请控制在 2000 字以内。');
});

test('formatScore clamps unexpected values into a readable percentage', () => {
  assert.equal(formatScore(94), '94% 接近');
  assert.equal(formatScore(130), '100% 接近');
  assert.equal(formatScore(-2), '0% 接近');
  assert.equal(formatScore(undefined), '匹配度待确认');
});

test('modeLabel keeps implementation details out of the interface', () => {
  assert.equal(modeLabel('RAG'), '经知识库匹配');
  assert.equal(modeLabel('TOOL_FALLBACK'), '经概念库复核');
  assert.equal(modeLabel('UNKNOWN'), '经真实概念库匹配');
});

test('sanitizeSourceUrl accepts only http sources', () => {
  assert.equal(sanitizeSourceUrl('https://example.org/word'), 'https://example.org/word');
  assert.equal(sanitizeSourceUrl('http://example.org/word'), 'http://example.org/word');
  assert.equal(sanitizeSourceUrl('javascript:alert(1)'), null);
  assert.equal(sanitizeSourceUrl('not a url'), null);
});

test('formatDate returns a stable Chinese date and tolerates invalid data', () => {
  assert.match(formatDate('2026-09-08T10:30:00+08:00'), /^2026年9月8日/);
  assert.equal(formatDate('invalid'), '时间未记录');
});
