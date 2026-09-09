import {createEmotionApi} from './api.mjs';
import {
  formatDate,
  formatScore,
  modeLabel,
  sanitizeSourceUrl,
  validateInput
} from './domain.mjs';

const api = createEmotionApi();
const analysisSteps = [
  '正在读懂这段文字里的情感重心……',
  '正在世界语言的真实概念中寻找……',
  '正在核对来源、含义与相似之处……'
];
const views = {
  compose: document.querySelector('#composeView'),
  results: document.querySelector('#resultsView'),
  archive: document.querySelector('#archiveView')
};

const momentForm = document.querySelector('#momentForm');
const momentText = document.querySelector('#momentText');
const characterCount = document.querySelector('#characterCount');
const inputMessage = document.querySelector('#inputMessage');
const matchButton = document.querySelector('#matchButton');
const matchButtonLabel = document.querySelector('#matchButtonLabel');
const analysisStatus = document.querySelector('#analysisStatus');
const analysisStatusText = document.querySelector('#analysisStatusText');
const resultsWall = document.querySelector('#resultsWall');
const resultMessage = document.querySelector('#resultMessage');
const matchModeLabel = document.querySelector('#matchModeLabel');
const archiveStatus = document.querySelector('#archiveStatus');
const archiveWall = document.querySelector('#archiveWall');
const archiveCount = document.querySelector('#archiveCount');
const globalMessage = document.querySelector('#globalMessage');

let currentInput = '';
let analysisTimer = null;
let globalMessageTimer = null;
let knownArchiveCount = null;

function element(tagName, className, text) {
  const node = document.createElement(tagName);
  if (className) {
    node.className = className;
  }
  if (text !== undefined && text !== null) {
    node.textContent = String(text);
  }
  return node;
}

function button(className, text) {
  const node = element('button', className, text);
  node.type = 'button';
  return node;
}

function showView(name, focusSelector) {
  Object.entries(views).forEach(([viewName, view]) => {
    view.hidden = viewName !== name;
  });

  window.scrollTo({top: 0, behavior: 'smooth'});
  if (focusSelector) {
    requestAnimationFrame(() => document.querySelector(focusSelector)?.focus());
  }
}

function updateInputState() {
  const validation = validateInput(momentText.value);
  characterCount.textContent = `${validation.count} / 2000`;
  matchButton.disabled = !validation.valid;
  inputMessage.textContent = validation.count === 0 || validation.valid ? '' : validation.message;
}

function startAnalysis() {
  let step = 0;
  document.body.classList.add('is-analyzing');
  momentForm.setAttribute('aria-busy', 'true');
  analysisStatus.hidden = false;
  analysisStatusText.textContent = analysisSteps[step];
  matchButton.disabled = true;
  matchButtonLabel.textContent = '正在寻找……';
  analysisTimer = window.setInterval(() => {
    step = (step + 1) % analysisSteps.length;
    analysisStatusText.textContent = analysisSteps[step];
  }, 2200);
}

function stopAnalysis() {
  window.clearInterval(analysisTimer);
  analysisTimer = null;
  document.body.classList.remove('is-analyzing');
  momentForm.removeAttribute('aria-busy');
  analysisStatus.hidden = true;
  matchButtonLabel.textContent = '为此刻寻找名字';
  updateInputState();
}

function announce(message) {
  window.clearTimeout(globalMessageTimer);
  globalMessage.textContent = message;
  globalMessage.hidden = false;
  globalMessageTimer = window.setTimeout(() => {
    globalMessage.hidden = true;
  }, 4400);
}

function sourceLink(url) {
  const safeUrl = sanitizeSourceUrl(url);
  if (!safeUrl) {
    return element('span', 'source-link source-link--unavailable', '来源暂不可用');
  }
  const link = element('a', 'source-link', '查看可靠来源');
  link.href = safeUrl;
  link.target = '_blank';
  link.rel = 'noopener noreferrer';
  return link;
}

function renderFingerprint(fingerprint) {
  const pane = element('article', 'fingerprint-pane glass-pane');
  pane.append(
    element('h3', 'fingerprint-title', '你的情感指纹'),
    element('p', 'fingerprint-meaning', fingerprint?.meaning || '这个此刻有自己的重量'),
    element('p', '', fingerprint?.description || '我们从你的叙述中辨认出这份感受的方向。')
  );
  return pane;
}

function renderCandidate(candidate, index) {
  const positionClass = ['candidate-pane--primary', 'candidate-pane--secondary', 'candidate-pane--tertiary'][index];
  const toneClass = index === 0 ? 'candidate-pane--cobalt' : index === 2 ? 'candidate-pane--oxblood' : '';
  const pane = element('article', `candidate-pane ${positionClass} ${toneClass}`.trim());
  pane.dataset.conceptId = String(candidate.conceptId);

  const header = element('div', 'candidate-header');
  const meta = element('div');
  meta.append(
    element('p', 'candidate-kicker', `候选 0${index + 1}`),
    element('span', 'candidate-language', candidate.language || '语言信息待确认')
  );
  header.append(meta, element('span', 'candidate-score', formatScore(candidate.matchScore)));

  const status = element('p', 'candidate-status');
  status.setAttribute('aria-live', 'polite');
  const footer = element('div', 'candidate-footer');
  const chooseButton = button('choose-action', '这就是我的感觉');
  chooseButton.dataset.chooseConcept = String(candidate.conceptId);
  chooseButton.setAttribute('aria-label', `选择并收藏 ${candidate.name}`);
  chooseButton.addEventListener('click', () => saveCandidate(candidate, pane, chooseButton, status));
  footer.append(sourceLink(candidate.sourceUrl), chooseButton);

  pane.append(
    header,
    element('h3', 'candidate-name', candidate.name || '未命名概念'),
    element('p', 'candidate-meaning', candidate.meaning || '含义待确认'),
    element('p', 'candidate-description', candidate.description || ''),
    element('p', 'candidate-explanation', candidate.explanation || '它与这段文字表达的情绪很接近。'),
    status,
    footer
  );
  return pane;
}

function renderResults(data) {
  const candidates = Array.isArray(data?.candidates) ? data.candidates.slice(0, 3) : [];
  if (candidates.length !== 3) {
    throw new Error('这次没有找到完整的三个概念，请再试一次。');
  }

  resultsWall.replaceChildren(renderFingerprint(data.fingerprint));
  candidates.forEach((candidate, index) => {
    resultsWall.append(renderCandidate(candidate, index));
  });
  resultMessage.textContent = '';
  resultMessage.removeAttribute('role');
  matchModeLabel.textContent = modeLabel(data.matchMode);
  showView('results', '#resultsTitle');
}

async function saveCandidate(candidate, pane, chooseButton, status) {
  const allChooseButtons = [...document.querySelectorAll('[data-choose-concept]')];
  chooseButton.disabled = true;
  chooseButton.textContent = '正在收藏……';
  resultMessage.textContent = '';
  resultMessage.removeAttribute('role');
  status.textContent = '正在把这个名字收进你的档案馆……';
  status.removeAttribute('role');

  try {
    await api.saveRecord({
      inputText: currentInput,
      conceptId: candidate.conceptId,
      matchScore: candidate.matchScore,
      explanation: candidate.explanation
    });
    pane.classList.add('is-selected');
    chooseButton.textContent = '已收藏到私人档案馆';
    allChooseButtons.forEach(item => {
      item.disabled = true;
    });
    allChooseButtons.filter(item => item !== chooseButton).forEach(item => {
      item.textContent = '本次已选择其他概念';
    });
    knownArchiveCount = knownArchiveCount === null ? null : knownArchiveCount + 1;
    updateArchiveCount(knownArchiveCount);
    status.textContent = `已点亮“${candidate.name}”，并收藏到私人档案馆。`;
    resultMessage.textContent = `你为这个此刻选择了“${candidate.name}”。它已经被好好收进档案馆。`;
    announce('已收藏。以后回看时，这段文字和你选择的名字都会在。');
  } catch (error) {
    chooseButton.disabled = false;
    chooseButton.textContent = '这就是我的感觉';
    status.textContent = error.message || '暂时没能保存，请稍后再试。';
    status.setAttribute('role', 'alert');
  }
}

function updateArchiveCount(count) {
  if (!Number.isInteger(count) || count <= 0) {
    archiveCount.hidden = true;
    archiveCount.textContent = '';
    return;
  }
  archiveCount.textContent = count > 99 ? '99+' : String(count);
  archiveCount.hidden = false;
}

function archiveTone(index) {
  if (index % 4 === 1) {
    return 'archive-pane--cobalt';
  }
  if (index % 4 === 2) {
    return 'archive-pane--amber';
  }
  if (index % 4 === 3) {
    return 'archive-pane--oxblood';
  }
  return '';
}

function renderArchiveRecord(record, index) {
  const wideClass = index % 3 === 0 ? 'archive-pane--wide' : '';
  const pane = element('article', `archive-pane ${wideClass} ${archiveTone(index)}`.trim());
  pane.dataset.recordId = String(record.id);

  const heading = element('div', 'archive-heading');
  const headingText = element('div');
  headingText.append(
    element('h3', 'archive-name', record.name || '未命名概念'),
    element('span', 'archive-language', record.language || '语言信息待确认')
  );
  heading.append(headingText, element('time', 'archive-date', formatDate(record.createdAt)));

  const status = element('p', 'archive-record-status');
  status.setAttribute('aria-live', 'polite');
  const actions = element('div', 'archive-actions');
  const removeButton = button('delete-action', '移出档案馆');
  removeButton.setAttribute('aria-label', `从档案馆移除 ${record.name}`);
  removeButton.addEventListener('click', () => confirmDeleteRecord(record, pane, removeButton, status));
  actions.append(sourceLink(record.sourceUrl), removeButton);

  pane.append(
    heading,
    element('p', 'archive-meaning', record.meaning || ''),
    element('blockquote', 'archive-input', record.inputText || ''),
    element('p', 'archive-explanation', record.explanation || ''),
    status,
    actions
  );
  return pane;
}

function renderEmptyArchive() {
  const empty = element('div', 'empty-pane glass-pane');
  const writeButton = button('inline-action', '写下第一个此刻');
  writeButton.addEventListener('click', () => showCompose(true));
  empty.append(
    element('h3', '', '还没有收藏的此刻'),
    element('p', '', '当你选择一个真正贴近自己的词，它会被留在这里。'),
    writeButton
  );
  archiveWall.append(empty);
}

function renderArchive(records) {
  archiveWall.replaceChildren();
  knownArchiveCount = records.length;
  updateArchiveCount(knownArchiveCount);
  if (records.length === 0) {
    renderEmptyArchive();
  } else {
    records.forEach((record, index) => archiveWall.append(renderArchiveRecord(record, index)));
  }
  archiveStatus.hidden = true;
  archiveWall.hidden = false;
}

function renderArchiveError(message) {
  archiveWall.replaceChildren();
  const pane = element('div', 'error-pane glass-pane');
  const retryButton = button('inline-action', '重新打开档案馆');
  retryButton.addEventListener('click', openArchive);
  pane.setAttribute('role', 'alert');
  pane.append(
    element('h3', '', '档案馆暂时没有打开'),
    element('p', '', message),
    retryButton
  );
  archiveStatus.hidden = true;
  archiveWall.hidden = false;
  archiveWall.append(pane);
}

async function openArchive() {
  showView('archive', '#archiveTitle');
  archiveWall.hidden = true;
  archiveStatus.hidden = false;
  archiveStatus.textContent = '正在打开你的档案馆……';
  try {
    const records = await api.listRecords();
    renderArchive(Array.isArray(records) ? records : []);
  } catch (error) {
    renderArchiveError(error.message || '暂时无法读取记录，请稍后再试。');
  }
}

async function confirmDeleteRecord(record, pane, removeButton, status) {
  if (removeButton.dataset.confirming !== 'true') {
    removeButton.dataset.confirming = 'true';
    removeButton.classList.add('is-confirming');
    removeButton.textContent = '再点一次确认移出';
    status.textContent = '这不会删除概念，只会移出这条私人记录。';
    status.removeAttribute('role');
    window.setTimeout(() => {
      if (removeButton.isConnected && removeButton.dataset.confirming === 'true') {
        removeButton.dataset.confirming = 'false';
        removeButton.classList.remove('is-confirming');
        removeButton.textContent = '移出档案馆';
        status.textContent = '';
      }
    }, 4200);
    return;
  }

  removeButton.disabled = true;
  removeButton.textContent = '正在移出……';
  status.textContent = '正在从档案馆移出……';
  status.removeAttribute('role');
  try {
    await api.deleteRecord(record.id);
    pane.remove();
    knownArchiveCount = Math.max(0, (knownArchiveCount || 1) - 1);
    updateArchiveCount(knownArchiveCount);
    if (knownArchiveCount === 0) {
      renderEmptyArchive();
    }
    announce(`“${record.name}”已从档案馆移出。`);
  } catch (error) {
    removeButton.disabled = false;
    removeButton.dataset.confirming = 'false';
    removeButton.classList.remove('is-confirming');
    removeButton.textContent = '移出档案馆';
    status.textContent = error.message || '暂时没能删除这条记录，请再次尝试。';
    status.setAttribute('role', 'alert');
  }
}

function showCompose(clearInput = false) {
  if (clearInput) {
    momentText.value = '';
    currentInput = '';
    updateInputState();
  }
  showView('compose');
  requestAnimationFrame(() => momentText.focus());
}

momentText.addEventListener('input', updateInputState);
momentForm.addEventListener('submit', async event => {
  event.preventDefault();
  const validation = validateInput(momentText.value);
  if (!validation.valid) {
    inputMessage.textContent = validation.message;
    momentText.focus();
    return;
  }

  currentInput = validation.text;
  inputMessage.textContent = '';
  startAnalysis();
  try {
    const result = await api.match(currentInput);
    renderResults(result);
  } catch (error) {
    inputMessage.textContent = error.message || '这次没有找到名字，请稍后再试。';
    inputMessage.setAttribute('role', 'alert');
    momentText.focus();
  } finally {
    stopAnalysis();
  }
});

document.querySelector('#homeButton').addEventListener('click', () => showCompose(false));
document.querySelector('#archiveButton').addEventListener('click', openArchive);
document.querySelector('#writeAgainButton').addEventListener('click', () => showCompose(false));
document.querySelector('#newMomentButton').addEventListener('click', () => showCompose(true));

updateInputState();
