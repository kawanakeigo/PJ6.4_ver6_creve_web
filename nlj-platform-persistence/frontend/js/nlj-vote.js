'use strict';
const query = new URLSearchParams(location.search);
const eventId = Number(query.get('eventId'));
const artistId = Number(query.get('artistId'));
const cycleId = Number(query.get('cycleId'));
const button = document.getElementById('voteButton');
const message = document.getElementById('voteMessage');
document.getElementById('backLink').href = `/event.html?id=${encodeURIComponent(eventId)}`;

fetch(`/api/v1/public/events/${encodeURIComponent(eventId)}`).then(response => response.json()).then(detail => {
  const artist = detail.artists.find(item => item.artistId === artistId);
  if (!artist || detail.event.activeCycleId !== cycleId) throw new Error();
  document.getElementById('artistName').textContent = artist.name;
  document.getElementById('eventName').textContent = detail.event.title;
  document.getElementById('cycleName').textContent = `#${cycleId}`;
  if (window.liff && typeof window.liff.getIDToken === 'function' && window.liff.getIDToken()) {
    button.disabled = false;
    message.textContent = '投票先とCycleを確認してください。';
  }
}).catch(() => message.textContent = '投票対象を確認できませんでした。選択画面へ戻ってください。');

button.addEventListener('click', async () => {
  if (button.disabled) return;
  button.disabled = true;
  button.textContent = '送信中...';
  const token = window.liff.getIDToken();
  try {
    const response = await fetch('/api/v1/votes', {method:'POST',headers:{'Content-Type':'application/json','X-Line-ID-Token':token},body:JSON.stringify({eventId,artistId,expectedCycleId:cycleId})});
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || '投票を受け付けられませんでした。');
    message.textContent = `投票を受け付けました。受付番号 ${result.voteId}`;
    button.textContent = '投票済み';
  } catch (error) {
    message.textContent = error.message;
    button.disabled = false;
    button.textContent = 'もう一度確認する';
  }
});
