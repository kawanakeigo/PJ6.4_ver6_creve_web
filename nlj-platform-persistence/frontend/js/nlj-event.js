'use strict';
const id = new URLSearchParams(location.search).get('id');
fetch(`/api/v1/public/events/${encodeURIComponent(id || '')}`).then(response => {
  if (!response.ok) throw new Error();
  return response.json();
}).then(detail => {
  const event = detail.event;
  document.title = `${event.title} | NEW LEGACY JAM`;
  document.getElementById('eventTitle').textContent = event.title;
  document.getElementById('eventSummary').textContent = event.summary;
  document.getElementById('cycleId').textContent = `#${event.activeCycleId}`;
  document.getElementById('voteState').textContent = event.acceptingVotes ? '受付予定' : '受付外';
  const list = document.getElementById('artistList');
  list.replaceChildren(...detail.artists.map(artist => {
    const article = document.createElement('article');
    const order = document.createElement('p');
    order.className = 'artist-order';
    order.textContent = `${String(artist.position).padStart(2,'0')} / SIDE ${artist.side}`;
    const name = document.createElement('h3');
    name.textContent = artist.name;
    const profile = document.createElement('p');
    profile.textContent = artist.profile;
    const link = document.createElement('a');
    link.className = 'action';
    link.href = `/vote.html?eventId=${event.eventId}&artistId=${artist.artistId}&cycleId=${event.activeCycleId}`;
    link.textContent = 'この出演者を選ぶ';
    article.append(order, name, profile, link);
    return article;
  }));
}).catch(() => document.getElementById('eventTitle').textContent = 'イベントを読み込めませんでした。');
