export const INPUT_MIN_LENGTH = 5;
export const INPUT_MAX_LENGTH = 2000;

export function validateInput(value) {
  const text = String(value ?? '').trim();
  const count = Array.from(text).length;

  if (count < INPUT_MIN_LENGTH) {
    return {
      valid: false,
      text,
      count,
      message: '再写一点吧，至少需要 5 个字。'
    };
  }

  if (count > INPUT_MAX_LENGTH) {
    return {
      valid: false,
      text,
      count,
      message: '这段文字有些长，请控制在 2000 字以内。'
    };
  }

  return {valid: true, text, count, message: ''};
}

export function formatScore(value) {
  const score = Number(value);
  if (!Number.isFinite(score)) {
    return '匹配度待确认';
  }
  return `${Math.min(100, Math.max(0, Math.round(score)))}% 接近`;
}

export function modeLabel(mode) {
  if (mode === 'RAG') {
    return '经知识库匹配';
  }
  if (mode === 'TOOL_FALLBACK') {
    return '经概念库复核';
  }
  return '经真实概念库匹配';
}

export function sanitizeSourceUrl(value) {
  try {
    const url = new URL(String(value));
    return url.protocol === 'https:' || url.protocol === 'http:' ? url.href : null;
  } catch {
    return null;
  }
}

export function formatDate(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '时间未记录';
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}
