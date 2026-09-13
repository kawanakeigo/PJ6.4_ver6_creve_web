'use strict';
(function(){
 const allowed=new Set(['creator_view','form_view','submission_attempt','petal_click','sns_click','qr_visit']);
 function scope(){const source=document.querySelector('[data-uka-board]')||document.body;const data={};for(const name of ['eventId','creatorId','artworkId']){const value=Number(source.dataset[name]);data[name]=Number.isSafeInteger(value)&&value>0?value:null;}return data;}
 function trackPlaypitEvent(eventName){
  if(!allowed.has(eventName))return;const data=scope();if(!data.eventId||!window.CreveHttp)return;
  // This allowlisted object never includes message, displayName, form values, URL, IP or session id.
  window.CreveHttp.postJson('/api/analytics',{eventName,...data}).catch(()=>{});
 }
 function initializeAnalytics(){
  if(window.location.pathname.includes('/creators/'))trackPlaypitEvent('creator_view');
  if(new URLSearchParams(window.location.search).get('source')==='qr')trackPlaypitEvent('qr_visit');
  const form=document.getElementById('messageForm');if(form){if('IntersectionObserver' in window){const observer=new IntersectionObserver(entries=>{if(entries.some(e=>e.isIntersecting)){trackPlaypitEvent('form_view');observer.disconnect();}});observer.observe(form);}else trackPlaypitEvent('form_view');}
  document.querySelectorAll('[data-sns-link]').forEach(link=>link.addEventListener('click',()=>trackPlaypitEvent('sns_click')));
 }
 window.trackPlaypitEvent=trackPlaypitEvent;
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',initializeAnalytics);else initializeAnalytics();
})();
