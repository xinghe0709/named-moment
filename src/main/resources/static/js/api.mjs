export class ApiError extends Error {
  constructor(message, code = null, status = null) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.status = status;
  }
}

export function createEmotionApi(fetchImpl = globalThis.fetch) {
  if (typeof fetchImpl !== 'function') {
    throw new TypeError('fetch implementation is required');
  }

  async function request(url, options = {}) {
    let response;
    try {
      response = await fetchImpl(url, options);
    } catch (error) {
      throw new ApiError('暂时无法连接服务，请确认应用仍在运行。');
    }

    let payload;
    try {
      payload = await response.json();
    } catch (error) {
      throw new ApiError('服务没有返回可识别的结果。', null, response.status);
    }

    if (!response.ok) {
      throw new ApiError(payload?.message || '服务暂时没有回应，请稍后再试。', payload?.code, response.status);
    }
    if (!payload || typeof payload.code !== 'number') {
      throw new ApiError('服务没有返回可识别的结果。', null, response.status);
    }
    if (payload.code !== 0) {
      throw new ApiError(payload.message || '这次请求没有完成。', payload.code, response.status);
    }
    return payload.data;
  }

  function jsonOptions(method, body) {
    return {
      method,
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(body)
    };
  }

  return {
    match(text) {
      return request('/api/emotions/match', jsonOptions('POST', {text}));
    },
    saveRecord(record) {
      return request('/api/emotions/records', jsonOptions('POST', record));
    },
    listRecords() {
      return request('/api/emotions/records', {method: 'GET'});
    },
    deleteRecord(id) {
      return request(`/api/emotions/records/${encodeURIComponent(id)}`, {method: 'DELETE'});
    }
  };
}
