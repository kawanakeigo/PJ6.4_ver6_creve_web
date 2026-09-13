'use strict';
(function () {
 let form, inFlight=false, pendingMemory=null;
 const storageKey='playpit.pending:'+window.location.pathname;
 function updateMessageCount(){const text=document.getElementById('messageText'),count=document.getElementById('messageCount');if(text&&count)count.textContent=String(text.value.length);}
 function clearMessageError(){const node=document.getElementById('messageError');if(node)node.textContent='';}
 function showMessageError(message){const node=document.getElementById('messageError');if(node)node.textContent=message;}
 function disableSubmitButton(){const button=document.getElementById('submitMessageButton');if(button){button.disabled=true;button.textContent='投稿中…';}}
 function enableSubmitButton(){const button=document.getElementById('submitMessageButton');if(button){button.disabled=form?.dataset.enabled!=='true';button.textContent='言葉を花びらにする';}}
 function validateMessageForm(){
  if(!form)return false;clearMessageError();const text=document.getElementById('messageText').value.trim(),name=document.getElementById('displayName').value;
  if(!text){showMessageError('感想を入力してください。');return false;}
  if(text.length>300){showMessageError('感想は300文字以内で入力してください。');return false;}
  if(name.length>30){showMessageError('表示名は30文字以内で入力してください。');return false;}
  if(!document.getElementById('agreementFlag').checked){showMessageError('利用規約への同意が必要です。');return false;}
  return true;
 }
 function resetMessageForm(){form?.reset();updateMessageCount();}
 function numericId(id){const raw=document.getElementById(id)?.value;if(!raw)return null;const value=Number(raw);if(!Number.isSafeInteger(value)||value<1)throw new Error('投稿先の情報が正しくありません。');return value;}
 function payload(){return {eventId:numericId('eventId'),creatorId:numericId('creatorId'),artworkId:numericId('artworkId'),message:document.getElementById('messageText').value.trim(),displayName:document.getElementById('displayName').value.trim()||null,anonymous:document.getElementById('anonymousFlag').checked,agreement:document.getElementById('agreementFlag').checked};}
 function idempotencyKey(data){
  const serialized=JSON.stringify(data);let previous=null;try{previous=JSON.parse(sessionStorage.getItem(storageKey)||'null');}catch(error){}
  previous=previous||pendingMemory;
  if(previous?.payload===serialized&&previous.key)return previous.key;
  const key=typeof crypto.randomUUID==='function'?crypto.randomUUID():Array.from(crypto.getRandomValues(new Uint8Array(16)),b=>b.toString(16).padStart(2,'0')).join('');pendingMemory={payload:serialized,key};try{sessionStorage.setItem(storageKey,JSON.stringify(pendingMemory));}catch(error){}
  return key;
 }
 async function showPetalAnimation(text,petal){
  const board=document.querySelector('[data-uka-board]');if(!board)return;
  if(window.matchMedia('(prefers-reduced-motion: reduce)').matches){window.Uka?.addNewPetal(petal);return;}
  const origin=document.getElementById('messageText').getBoundingClientRect(),target=board.getBoundingClientRect();
  const element=document.createElement('div');element.className='flying-petal';element.textContent=text;document.body.appendChild(element);element.style.left=(origin.left+origin.width/2-55)+'px';element.style.top=(origin.top+origin.height/2-35)+'px';
  const x=target.left+target.width*(petal?.positionX??50)/100-(origin.left+origin.width/2);
  const y=target.top+target.height*(petal?.positionY??50)/100-(origin.top+origin.height/2);
  try{const animation=element.animate([{transform:'translate(0,0) scale(1)',opacity:1},{transform:`translate(${x}px,${y}px) rotate(-35deg) scale(.3)`,opacity:.4}],{duration:900,easing:'ease-in-out',fill:'forwards'});await animation.finished;}catch(error){}finally{element.remove();window.Uka?.addNewPetal(petal);}
 }
 async function submitMessage(event){
  event?.preventDefault();if(inFlight)return;
  if(form?.dataset.enabled!=='true'){showMessageError('現在は投稿を受け付けていません。');return;}
  if(!validateMessageForm())return;
  inFlight=true;disableSubmitButton();document.getElementById('messageSuccess').textContent='';let confirmed=false;
  try{
   const data=payload();if(data.eventId===null||(!data.creatorId&&!data.artworkId))throw new Error('投稿先の情報が正しくありません。');
   const key=idempotencyKey(data);window.trackPlaypitEvent?.('submission_attempt');
   const response=await window.CreveHttp.postJson('/api/messages',data,{'Idempotency-Key':key});
   if(response.status!==201){
    const errors={400:'入力内容を確認してください。',401:'ページを再読み込みしてください。',403:'投稿が許可されていません。ページを再読み込みしてご確認ください。',404:'投稿先が見つかりません。',429:'投稿が集中しているか、同じ感想がすでに送られています。時間をおいてからお試しください。',500:'サーバーでエラーが発生しました。時間をおいて再送してください。'};
    throw new Error(errors[response.status]||'投稿できませんでした。');
   }
   const result=await response.json();if(!result.messageId||!result.status)throw new Error('保存結果を確認できません。再送すると同じ受付番号で確認します。');
   confirmed=true;pendingMemory=null;try{sessionStorage.removeItem(storageKey);}catch(error){}
   document.getElementById('messageSuccess').textContent=result.completionMessage||'投稿を受け付けました。';
   if(result.status==='PUBLISHED'){
    try{const uka=await window.Uka.refresh();const petal=uka.petals.find(p=>p.messageId===result.messageId);await showPetalAnimation(data.message,petal);await window.Uka.refreshWords();}
    catch(error){showMessageError('投稿は保存されましたが、表示の更新に失敗しました。ページを再読み込みしてください。');}
   }
   resetMessageForm();
  }catch(error){showMessageError(error instanceof TypeError?'通信に失敗しました。入力内容は保持しています。同じ内容で再送してください。':error.message||'送信に失敗しました。');}
  finally{inFlight=false;enableSubmitButton();}
 }
 function initializeMessageForm(){form=document.getElementById('messageForm');if(!form)return;form.addEventListener('submit',submitMessage);document.getElementById('messageText')?.addEventListener('input',updateMessageCount);updateMessageCount();enableSubmitButton();}
 window.PlaypitMessage=Object.freeze({initializeMessageForm,updateMessageCount,validateMessageForm,submitMessage,disableSubmitButton,enableSubmitButton,showMessageError,clearMessageError,showPetalAnimation,resetMessageForm});
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',initializeMessageForm);else initializeMessageForm();
})();
