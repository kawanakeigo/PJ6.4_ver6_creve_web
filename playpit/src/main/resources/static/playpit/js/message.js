'use strict';

(function () {
  let form;
  let inFlight = false;
  let pendingMemory = null;
  let reportTargetButton = null;
  const storageKey = 'playpit.pending:' + window.location.pathname;

  function textLength(value) {
    return Array.from(value).length;
  }

  function updateCount() {
    const input = document.getElementById('messageText');
    const count = document.getElementById('messageCount');
    if (input && count) count.textContent = String(textLength(input.value));
  }

  function error(message) {
    const node = document.getElementById('messageError');
    if (node) node.textContent = message || '';
  }

  function setSending(sending) {
    const button = document.getElementById('submitMessageButton');
    if (!button) return;
    button.disabled = sending || form?.dataset.enabled !== 'true';
    button.textContent = sending ? '送信中...' : '感想を送る';
  }

  function numericId(id) {
    const raw = document.getElementById(id)?.value;
    if (!raw) return null;
    const value = Number(raw);
    if (!Number.isSafeInteger(value) || value < 1) throw new Error('投稿先の情報が正しくありません。');
    return value;
  }

  function payload() {
    return {
      eventId: numericId('eventId'),
      creatorId: numericId('creatorId'),
      artworkId: numericId('artworkId'),
      message: document.getElementById('messageText').value.trim(),
      displayName: document.getElementById('displayName').value.trim() || null,
      anonymous: document.getElementById('anonymousFlag').checked,
      agreement: document.getElementById('agreementFlag').checked
    };
  }

  function validate(data) {
    error('');
    if (!data.message || textLength(data.message) > 300) {
      error('感想は1〜300文字で入力してください。');
      return false;
    }
    if (data.displayName && textLength(data.displayName) > 30) {
      error('表示名は30文字以内で入力してください。');
      return false;
    }
    if (!data.agreement) {
      error('利用規約への同意が必要です。');
      return false;
    }
    return true;
  }

  function requestKey(data) {
    const serialized = JSON.stringify(data);
    let previous = pendingMemory;
    try {
      previous = JSON.parse(sessionStorage.getItem(storageKey) || 'null') || previous;
    } catch (_) {
      // Memory fallback is enough for this tab when storage is unavailable.
    }
    if (previous?.payload === serialized && previous.key) return previous.key;
    const key = crypto.randomUUID();
    pendingMemory = {payload: serialized, key};
    try {
      sessionStorage.setItem(storageKey, JSON.stringify(pendingMemory));
    } catch (_) {
      // Keep the same key in memory.
    }
    return key;
  }

  function clearPending() {
    pendingMemory = null;
    try {
      sessionStorage.removeItem(storageKey);
    } catch (_) {
      // The server receipt is already committed.
    }
  }

  function renderMessage(item) {
    const article = document.createElement('article');
    article.className = 'word-card';
    const body = document.createElement('p');
    body.className = 'preserve-lines';
    body.textContent = item.message;
    const meta = document.createElement('p');
    meta.textContent = `${item.publicName || '匿名'} · ${item.createdAt || ''}`;
    const report = document.createElement('button');
    report.type = 'button';
    report.className = 'text-button';
    report.dataset.reportMessage = item.messageId;
    report.textContent = '通報';
    article.append(body, meta, report);
    return article;
  }

  function openReport(button) {
    const dialog = document.getElementById('reportDialog');
    if (!dialog) return;
    reportTargetButton = button;
    document.getElementById('reportForm')?.reset();
    document.getElementById('reportError').textContent = '';
    dialog.showModal();
  }

  async function submitReport(event) {
    event.preventDefault();
    const button = reportTargetButton;
    const category = document.getElementById('reportCategory').value;
    const detail = document.getElementById('reportDetail').value.trim();
    const reportError = document.getElementById('reportError');
    const messageId = Number(button?.dataset.reportMessage);
    if (!button || !Number.isSafeInteger(messageId) || messageId < 1) {
      reportError.textContent = '通報先を確認できません。';
      return;
    }
    if (!category || !detail || textLength(detail) > 1000) {
      reportError.textContent = '理由を選び、詳細を1〜1000文字で入力してください。';
      return;
    }
    const submitButton = document.getElementById('submitReportButton');
    submitButton.disabled = true;
    try {
      const response = await window.CreveHttp.postJson('/api/reports', {
        messageId,
        category,
        detail
      });
      if (!response.ok) throw new Error(response.status === 429
        ? '通報回数の上限に達しました。時間をおいてください。'
        : '通報を受け付けられませんでした。');
      button.textContent = '通報済み';
      document.getElementById('reportDialog').close();
      document.getElementById('messageSuccess').textContent = '通報を受け付けました。';
      reportTargetButton = null;
    } catch (reason) {
      reportError.textContent = reason.message;
    } finally {
      submitButton.disabled = false;
    }
  }

  async function refreshMessages() {
    const data = payload();
    const params = new URLSearchParams({eventId: String(data.eventId), page: '0', size: '20'});
    if (data.creatorId) params.set('creatorId', String(data.creatorId));
    if (data.artworkId) params.set('artworkId', String(data.artworkId));
    const response = await fetch('/api/public/messages?' + params, {
      credentials: 'same-origin',
      headers: {Accept: 'application/json'}
    });
    if (!response.ok) throw new Error('感想一覧を更新できませんでした。');
    const result = await response.json();
    const list = document.querySelector('[data-words-list]');
    if (list) list.replaceChildren(...result.items.map(renderMessage));
    const empty = document.querySelector('[data-words-empty]');
    if (empty) empty.hidden = result.items.length > 0;
  }

  async function submit(event) {
    event.preventDefault();
    if (inFlight) return;
    if (form.dataset.enabled !== 'true') {
      error('現在は投稿を受け付けていません。');
      return;
    }
    const data = payload();
    if (!validate(data)) return;
    if (!data.eventId || (!data.creatorId && !data.artworkId)) {
      error('投稿先の情報が正しくありません。');
      return;
    }

    inFlight = true;
    setSending(true);
    document.getElementById('messageSuccess').textContent = '';
    const key = requestKey(data);
    try {
      const response = await window.CreveHttp.postJson('/api/messages', data, {'Idempotency-Key': key});
      const messages = {
        400: '入力内容を確認してください。',
        403: '現在は投稿を受け付けていません。',
        404: '投稿先が見つかりません。',
        409: '同じ受付番号の内容が一致しません。',
        429: '投稿回数または同一内容の制限に達しました。時間をおいてください。'
      };
      if (!response.ok) throw new Error(messages[response.status] || '投稿できませんでした。');
      const result = await response.json();
      if (!result.messageId) throw new Error('保存結果を確認できませんでした。');
      clearPending();
      document.getElementById('messageSuccess').textContent = result.completionMessage;
      form.reset();
      updateCount();
      try {
        await refreshMessages();
      } catch (_) {
        error('感想は保存されましたが、一覧を更新できませんでした。ページを再読み込みしてください。');
      }
    } catch (reason) {
      error(reason instanceof TypeError
        ? '通信結果を確認できません。入力を保持しています。同じ内容で再送してください。'
        : reason.message);
    } finally {
      inFlight = false;
      setSending(false);
    }
  }

  function initialize() {
    form = document.getElementById('messageForm');
    if (!form) return;
    form.addEventListener('submit', submit);
    document.addEventListener('click', function (event) {
      const button = event.target.closest('[data-report-message]');
      if (button) openReport(button);
    });
    document.getElementById('reportForm')?.addEventListener('submit', submitReport);
    document.getElementById('cancelReportButton')?.addEventListener('click', function () {
      document.getElementById('reportDialog')?.close();
      reportTargetButton = null;
    });
    document.getElementById('messageText')?.addEventListener('input', updateCount);
    updateCount();
    setSending(false);
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
  else initialize();
})();
