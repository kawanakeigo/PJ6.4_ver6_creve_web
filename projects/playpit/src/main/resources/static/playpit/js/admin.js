'use strict';
(function(){function initializeAdmin(){const error=document.querySelector('.editor-form .error');if(error){error.tabIndex=-1;error.focus();}}if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',initializeAdmin);else initializeAdmin();})();

document.addEventListener('change',function(event){const input=event.target;if(!input.matches('[data-image-upload]'))return;const file=input.files[0];input.setCustomValidity(file&&file.size>10*1024*1024?'画像は1枚10MB以下にしてください。':'');if(!input.checkValidity())input.reportValidity();});
