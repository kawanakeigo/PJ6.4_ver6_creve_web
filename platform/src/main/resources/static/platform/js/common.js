'use strict';
(function () {
 let previousOverflow = '';
 function openGlobalMenu() {
  const menu=document.getElementById('globalMenu'), button=document.getElementById('globalMenuButton');
  if(!menu||!button)return;
  if(!menu.classList.contains('is-open')) previousOverflow=document.body.style.overflow;
  menu.classList.add('is-open');button.setAttribute('aria-expanded','true');document.body.classList.add('menu-open');document.body.style.overflow='hidden';
 }
 function closeGlobalMenu() {
  const menu=document.getElementById('globalMenu'), button=document.getElementById('globalMenuButton');
  if(!menu||!button)return;
  menu.classList.remove('is-open');button.setAttribute('aria-expanded','false');document.body.classList.remove('menu-open');document.body.style.overflow=previousOverflow;
 }
 function toggleGlobalMenu(){const menu=document.getElementById('globalMenu');if(menu?.classList.contains('is-open'))closeGlobalMenu();else openGlobalMenu();}
 function csrfHeaders(){
  const token=document.querySelector('meta[name="_csrf"]')?.content;
  const name=document.querySelector('meta[name="_csrf_header"]')?.content;
  return token&&name?{[name]:token}:{};
 }
 async function postJson(url,body,extraHeaders={}) {
  return fetch(url,{method:'POST',credentials:'same-origin',headers:{'Content-Type':'application/json',...csrfHeaders(),...extraHeaders},body:JSON.stringify(body)});
 }
 function initialize(){
  document.getElementById('globalMenuButton')?.addEventListener('click',toggleGlobalMenu);
  document.addEventListener('keydown',e=>{if(e.key==='Escape')closeGlobalMenu();});
  window.matchMedia('(min-width:761px)').addEventListener('change',e=>{if(e.matches)closeGlobalMenu();});
  document.querySelectorAll('form[data-confirm]').forEach(form=>form.addEventListener('submit',e=>{if(!window.confirm(form.dataset.confirm))e.preventDefault();}));
 }
 window.CreveHttp=Object.freeze({csrfHeaders,postJson});
 window.CreveMenu=Object.freeze({openGlobalMenu,closeGlobalMenu,toggleGlobalMenu});
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',initialize);else initialize();
})();
