'use strict';
fetch('/api/v1/public/events').then(response => {
  if (!response.ok) throw new Error();
  return response.json();
}).then(page => {
  const list = document.getElementById('eventList');
  list.replaceChildren(...page.items.map(event => {
    const article = document.createElement('article');
    const date = document.createElement('time');
    date.textContent = new Date(event.startsAt).toLocaleDateString('ja-JP', {year:'numeric',month:'long',day:'numeric'});
    const title = document.createElement('h3');
    title.textContent = event.title;
    const summary = document.createElement('p');
    summary.textContent = event.summary;
    const link = document.createElement('a');
    link.href = `/event.html?id=${encodeURIComponent(event.eventId)}`;
    link.textContent = '出演者を見る →';
    article.append(date, title, summary, link);
    return article;
  }));
  if (!page.items.length) list.textContent = '公開中のイベントはありません。';
}).catch(() => document.getElementById('eventList').textContent = '開催情報を読み込めませんでした。');
