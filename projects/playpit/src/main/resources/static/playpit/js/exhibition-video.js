document.querySelector('[data-load-exhibition-video]')?.addEventListener('click', function () {
  const frame = document.createElement('iframe');
  frame.src = 'https://www.youtube-nocookie.com/embed/RuZj3S_MjQo?autoplay=0&playsinline=1';
  frame.title = 'PLAYPIT 展示会紹介';
  frame.allow = 'encrypted-media; picture-in-picture; fullscreen';
  frame.allowFullscreen = true;
  frame.referrerPolicy = 'strict-origin-when-cross-origin';
  this.closest('[data-exhibition-player]').replaceChildren(frame);
  frame.focus();
}, { once: true });
