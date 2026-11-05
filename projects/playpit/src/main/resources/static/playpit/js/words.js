'use strict';
(function(){let modal,lastFocused,reportBusy=false;function number(v,f=0){return Number.isFinite(Number(v))?Number(v):f;}
function scopeParams(){const params=new URLSearchParams();for(const key of ['eventId','creatorId','artworkId'])if(document.body.dataset[key])params.set(key,document.body.dataset[key]);return params;}
 function openMessageModal(petal){
  if(!modal)return;
  lastFocused=document.activeElement;const p=petal.dataset||petal;modal.dataset.messageId=String(p.messageId||'');
  const fields={modalMessage:'message',modalDisplayName:'displayName',modalCreatedAt:'createdAt',modalArtworkTitle:'artworkTitle',modalCreatorName:'creatorName'};
  for(const [id,key] of Object.entries(fields)){const node=document.getElementById(id);if(node)node.textContent=p[key]||'—';}
  const result=document.getElementById('reportResult');if(result)result.textContent='';document.getElementById('reportForm')?.reset();
  modal.showModal();
 }
 function closeMessageModal(){if(modal?.open)modal.close();lastFocused?.focus();}
 async function refreshWords(){
  const target=document.querySelector('[data-words-list]');if(!target)return;
  const params=scopeParams();params.set('page',document.querySelector('[data-words-page]')?.dataset.wordsPage||'0');
  const response=await fetch('/api/messages?'+params,{credentials:'same-origin',headers:{Accept:'application/json'}});
  if(!response.ok)throw new Error('感想一覧の更新に失敗しました。');const data=await response.json();const fragment=document.createDocumentFragment();
  for(const item of data.items||[]){const article=document.createElement('article');article.className='word-card';for(const [key,cls] of [['message','preserve-lines'],['displayName',''],['createdAt',''],['artworkTitle',''],['creatorName','']]){const p=document.createElement('p');p.className=cls;p.textContent=item[key]||'';article.appendChild(p);}const button=document.createElement('button');button.type='button';button.dataset.wordDetail='';button.textContent='詳細・通報';for(const key of ['messageId','message','displayName','createdAt','artworkTitle','creatorName'])button.dataset[key]=String(item[key]||'');article.appendChild(button);fragment.appendChild(article);}target.replaceChildren(fragment);
  const empty=document.querySelector('[data-words-empty]');if(empty)empty.hidden=(data.items||[]).length>0;
  const navigation=document.querySelector('[data-words-pagination]');
  if(navigation){navigation.replaceChildren();const page=number(data.page),pages=Math.max(1,number(data.totalPages));
   const link=(label,index)=>{const a=document.createElement('a');a.textContent=label;const url=new URL(location.href);url.searchParams.set('page',String(index));a.href=url.pathname+url.search;return a;};
   if(page>0)navigation.appendChild(link('前の感想',page-1));const label=document.createElement('span');label.textContent=(page+1)+' / '+pages;navigation.appendChild(label);if(page+1<pages)navigation.appendChild(link('次の感想',page+1));}

 }
 async function report(event){
  event.preventDefault();if(reportBusy)return;const reason=document.getElementById('reportReason').value.trim();const result=document.getElementById('reportResult');
  if(!reason||reason.length>300){result.textContent='通報理由を1〜300文字で入力してください。';return;}
  reportBusy=true;try{const response=await window.CreveHttp.postJson('/api/messages/'+encodeURIComponent(modal.dataset.messageId)+'/reports',{reason});result.textContent=response.status===201?'運営へ報告しました。':'送信できませんでした。時間をおいて再度お試しください。';}catch(error){result.textContent='通信に失敗しました。再度お試しください。';}finally{reportBusy=false;}
 }

function initialize(){modal=document.getElementById('messageModal');document.querySelector('[data-words-list]')?.addEventListener('click',e=>{const button=e.target.closest('[data-word-detail]');if(button)openMessageModal(button);});document.getElementById('closeMessageModalButton')?.addEventListener('click',closeMessageModal);modal?.addEventListener('close',()=>lastFocused?.focus());document.getElementById('reportForm')?.addEventListener('submit',report);}
window.PlaypitWords=Object.freeze({refreshWords});if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',initialize);else initialize();})();
