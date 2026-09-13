'use strict';
(function () {
 let board, modal, lastFocused, reportBusy=false;
 const MAX_PETALS=300;
 function number(value,fallback=0){const v=Number(value);return Number.isFinite(v)?v:fallback;}
 function createPetalElement(petal){
  const button=document.createElement('button');button.type='button';button.className='petal petal-type-'+Math.max(1,Math.min(5,number(petal.petalType,1)));
  button.dataset.messageId=String(petal.messageId);button.dataset.message=petal.message||'';button.dataset.displayName=petal.displayName||'匿名';button.dataset.createdAt=petal.createdAt||'';button.dataset.artworkTitle=petal.artworkTitle||'';button.dataset.creatorName=petal.creatorName||'';
  button.style.left=number(petal.positionX)+'%';button.style.top=number(petal.positionY)+'%';button.style.transform=`translate(-50%,-50%) rotate(${number(petal.rotation)}deg) scale(${number(petal.scale,1)})`;
  button.setAttribute('aria-label','感想を読む：'+button.dataset.displayName);return button;
 }
 function renderPetals(data){
  if(!board)return;
  const layer=board.querySelector('[data-uka-petals]');const fragment=document.createDocumentFragment();
  (data.petals||[]).slice(0,MAX_PETALS).forEach(p=>fragment.appendChild(createPetalElement(p)));layer.replaceChildren(fragment);
  board.dataset.growthStage=String(number(data.growthStage));board._ukaData=data;
  document.querySelectorAll('[data-message-count]').forEach(n=>{n.textContent=String(data.messageCount);});
  const label=document.querySelector('[data-growth-label]');const stages=['芽・羽の輪郭','花びらが少しずつ現れる','羽の一部が形成される','羽の形が明確になる','大きな羽へ'];
  if(label)label.textContent='成長段階：'+number(data.growthStage)+' / '+(stages[number(data.growthStage)]||'');
  const empty=board.querySelector('[data-uka-empty]');if(empty)empty.hidden=number(data.messageCount)>0;
  let note=document.querySelector('[data-representative-note]');
  if(number(data.messageCount)>300&&!note){note=document.createElement('p');note.dataset.representativeNote='';note.textContent='花びらは最大300件を代表表示しています。総投稿数は別に表示しています。';board.after(note);}
  if(note)note.hidden=number(data.messageCount)<=300;
  resizeUkaCanvas();
 }
 function openMessageModal(petal){
  if(!modal)return;
  lastFocused=document.activeElement;const p=petal.dataset||petal;modal.dataset.messageId=String(p.messageId||'');
  const fields={modalMessage:'message',modalDisplayName:'displayName',modalCreatedAt:'createdAt',modalArtworkTitle:'artworkTitle',modalCreatorName:'creatorName'};
  for(const [id,key] of Object.entries(fields)){const node=document.getElementById(id);if(node)node.textContent=p[key]||'—';}
  const result=document.getElementById('reportResult');if(result)result.textContent='';document.getElementById('reportForm')?.reset();
  modal.showModal();window.trackPlaypitEvent?.('petal_click');
 }
 function closeMessageModal(){if(modal?.open)modal.close();lastFocused?.focus();}
 function addNewPetal(petal){
  if(!board||!petal)return;const layer=board.querySelector('[data-uka-petals]');
  if([...layer.children].some(p=>p.dataset.messageId===String(petal.messageId)))return;
  if(layer.children.length<MAX_PETALS)layer.appendChild(createPetalElement(petal));
 }
 function resizeUkaCanvas(){if(board){const width=board.getBoundingClientRect().width;board.style.setProperty('--petal-width',Math.max(18,Math.min(30,width*.035))+'px');}}
 function scopeParams(){
  const params=new URLSearchParams();for(const [key,attr] of [['eventId','eventId'],['creatorId','creatorId'],['artworkId','artworkId']]){if(board?.dataset[attr])params.set(key,board.dataset[attr]);}return params;
 }
 async function refresh(){
  if(!board)throw new Error('羽花の表示領域がありません。');
  const response=await fetch('/api/uka?'+scopeParams(),{credentials:'same-origin',headers:{Accept:'application/json'}});
  if(!response.ok)throw new Error('羽花の再読み込みに失敗しました。');const data=await response.json();renderPetals(data);return data;
 }
 async function refreshWords(){
  const target=document.querySelector('[data-words-list]');if(!target||!board)return;
  const params=scopeParams();params.set('page',document.querySelector('[data-words-page]')?.dataset.wordsPage||'0');
  const response=await fetch('/api/messages?'+params,{credentials:'same-origin',headers:{Accept:'application/json'}});
  if(!response.ok)throw new Error('感想一覧の更新に失敗しました。');const data=await response.json();const fragment=document.createDocumentFragment();
  for(const item of data.items||[]){const article=document.createElement('article');article.className='word-card';for(const [key,cls] of [['message','preserve-lines'],['displayName',''],['createdAt',''],['artworkTitle',''],['creatorName','']]){const p=document.createElement('p');p.className=cls;p.textContent=item[key]||'';article.appendChild(p);}fragment.appendChild(article);}target.replaceChildren(fragment);
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
 function initializeUka(){
  board=document.querySelector('[data-uka-board]');modal=document.getElementById('messageModal');if(!board)return;
  board.addEventListener('click',e=>{const p=e.target.closest('.petal');if(p)openMessageModal(p);});
  document.getElementById('closeMessageModalButton')?.addEventListener('click',closeMessageModal);
  modal?.addEventListener('close',()=>lastFocused?.focus());document.getElementById('reportForm')?.addEventListener('submit',report);
  window.addEventListener('resize',resizeUkaCanvas);resizeUkaCanvas();
 }
 window.Uka=Object.freeze({initializeUka,renderPetals,createPetalElement,openMessageModal,closeMessageModal,addNewPetal,resizeUkaCanvas,refresh,refreshWords});
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',initializeUka);else initializeUka();
})();
