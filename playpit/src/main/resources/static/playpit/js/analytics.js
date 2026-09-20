'use strict';

(function () {
  function scope() {
    const result = {};
    for (const name of ['eventId', 'creatorId', 'artworkId']) {
      const value = Number(document.body.dataset[name]);
      result[name] = Number.isSafeInteger(value) && value > 0 ? value : null;
    }
    return result;
  }

  function track(kind) {
    const data = scope();
    if (!data.eventId || !window.CreveHttp || !crypto.randomUUID) return;
    window.CreveHttp.postJson('/api/metrics', {metricId: crypto.randomUUID(), kind, ...data}).catch(() => {});
  }

  function initialize() {
    if (document.body.dataset.artworkId) track('ARTWORK_VIEW');
    else if (document.body.dataset.creatorId) track('CREATOR_VIEW');
    else if (document.body.dataset.eventId) track('EVENT_VIEW');
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
  else initialize();
})();
