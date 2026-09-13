from helpers import *
from pathlib import Path
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
import json,threading,http.server,urllib.parse,mimetypes,shutil
OUT=BASE/'verification';R=BASE/'projects/playpit/src/main/resources/templates/playpit/fragments'
def fragment(path):
 soup=BeautifulSoup(path.read_text(),'html.parser')
 for n in list(soup.find_all(attrs={'th:each':True})):n.decompose()
 for el in soup.find_all(True):
  for a in list(el.attrs):
   if a.startswith('th:') or a=='xmlns:th':del el[a]
 return soup
form=fragment(R/'message-form.html');form.select_one('#messageForm')['data-enabled']='true';form.select_one('.notice').decompose()
for id,value in [('eventId','1'),('creatorId','2'),('artworkId','3')]:form.select_one('#'+id)['value']=value
board=fragment(R/'uka.html');board.select_one('[data-uka-board]').attrs.update({'data-event-id':'1','data-creator-id':'2','data-artwork-id':'3','data-growth-stage':'0'})
words=fragment(R/'words.html');words.select_one('.words-section')['data-words-page']='0';words.select_one('.pagination').clear()
header=fragment(BASE/'platform/src/main/resources/templates/common/header.html')
body=str(header)+'<main id="mainContent"><h1>作品詳細・修正版の部品検証</h1>'+str(form)+str(board)+str(words)+'</main>'
html='''<!doctype html><html lang="ja"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><meta name="_csrf" content="probe-token"><meta name="_csrf_header" content="X-CSRF-TOKEN"><link rel="stylesheet" href="/platform/css/common.css"><link rel="stylesheet" href="/playpit/css/uka.css"><script defer src="/platform/js/common.js"></script><script defer src="/playpit/js/uka.js"></script><script defer src="/playpit/js/message.js"></script></head><body>'''+body+'</body></html>'
(OUT/'browser-fixture.html').write_text(html)
asset_roots=list(BASE.glob('**/src/main/resources/static'))
# Offline DOM tests: no navigation, external network, or browser policy changes.
# API is mocked using an explicit Playwright binding.
soup=BeautifulSoup(html,'html.parser')
for tag in list(soup.find_all('script')):tag.decompose()
for link in list(soup.find_all('link')):
    path=link['href'].lstrip('/');file=next(r/path for r in asset_roots if (r/path).is_file())
    style=soup.new_tag('style');style.string=file.read_text();link.replace_with(style)
offline_html=str(soup)
js_paths=[BASE/'platform/src/main/resources/static/platform/js/common.js',BASE/'projects/playpit/src/main/resources/static/playpit/js/uka.js',BASE/'projects/playpit/src/main/resources/static/playpit/js/message.js']
checks=[];posts=[];visible=[];mode='normal';counter=0;errors=[]
def check(name,passed,detail=''):
 checks.append({'test':name,'pass':bool(passed),'detail':detail})
 if not passed:print('FAIL',name,detail)
with sync_playwright() as pw:
 browser=pw.chromium.launch(executable_path=shutil.which('chromium'),headless=True,args=['--no-sandbox'])
 ctx=browser.new_context(viewport={'width':1100,'height':900},reduced_motion='reduce');page=ctx.new_page();page.on('pageerror',lambda e:errors.append(str(e)))
 def api_binding(source,input,options):
  global counter
  path=urllib.parse.urlparse(input).path
  if path=='/api/messages' and options.get('method')=='POST':
   data=json.loads(options['body']);posts.append({'body':data,'headers':{k.lower():v for k,v in options['headers'].items()}});counter+=1
   if mode=='network':return {'networkError':True}
   if mode=='429':return {'status':429,'body':{'error':'制限'}}
   if mode=='pending':return {'status':201,'body':{'messageId':counter,'status':'PENDING','petalType':1,'petalSeed':9,'createdAt':'2026-09-14T10:00:00','completionMessage':'運営の確認後に公開されます。'}}
   visible.append({'messageId':counter,'petalType':1,'positionX':30+counter,'positionY':55,'rotation':-35,'scale':.8,'message':data['message'],'displayName':'匿名' if data['anonymous'] else data['displayName'],'artworkTitle':'検証作品','creatorName':'検証作者','createdAt':'2026-09-14T10:00:00'})
   return {'status':201,'body':{'messageId':counter,'status':'PUBLISHED','petalType':1,'petalSeed':9,'createdAt':'2026-09-14T10:00:00','completionMessage':'あなたの言葉が、一枚の花びらになりました。'}}
  if path=='/api/uka':return {'status':200,'body':{'eventId':1,'eventTitle':'検証展示','messageCount':len(visible),'growthStage':1 if visible else 0,'petals':visible}}
  if path=='/api/messages':return {'status':200,'body':{'items':visible[:30],'page':0,'size':30,'totalElements':len(visible),'totalPages':1}}
  return {'status':201,'body':{}}
 ctx.expose_binding('mockApi',api_binding)
 def setup_page(p):
  p.set_content(offline_html)
  p.evaluate("""() => {window.fetch=async (input,options={})=>{const r=await window.mockApi(String(input),options);if(r.networkError)throw new TypeError('Network failed');return new Response(JSON.stringify(r.body),{status:r.status,headers:{'Content-Type':'application/json'}});};}""")
  for path in js_paths:p.add_script_tag(content=path.read_text())
 def load():
  global page
  page.close();page=ctx.new_page();page.on('pageerror',lambda e:errors.append(str(e)))
  setup_page(page);page.locator('#agreementFlag').check()
 def fill(text='感動しました'):
  page.locator('#messageText').fill(text);page.locator('#displayName').fill('秘密の表示名')
 def submit_wait():
  page.locator('#submitMessageButton').click();page.wait_for_function("document.querySelector('#submitMessageButton').textContent !== '投稿中…'")
 load()
 for text,expected in [('',False),('　 ',False),('あ'*300,True),('あ'*301,False)]:
  page.locator('#messageText').evaluate('(el,v)=>{el.value=v;}',text);check('Body validation length '+str(len(text)),page.evaluate('PlaypitMessage.validateMessageForm()')==expected)
 fill();page.locator('#displayName').evaluate('(el)=>el.value="名".repeat(31)');check('Display name 31 rejected',page.evaluate('PlaypitMessage.validateMessageForm()')==False)
 page.locator('#displayName').evaluate('(el)=>el.value="名".repeat(30)');check('Display name 30 accepted',page.evaluate('PlaypitMessage.validateMessageForm()')==True)
 page.locator('#agreementFlag').uncheck();check('Agreement required',page.evaluate('PlaypitMessage.validateMessageForm()')==False);page.locator('#agreementFlag').check()
 fill();start=len(posts);page.evaluate('''()=>{const f=document.querySelector('#messageForm');f.dispatchEvent(new Event('submit',{cancelable:true}));f.dispatchEvent(new Event('submit',{cancelable:true}));}''');page.wait_for_function("document.querySelector('#submitMessageButton').textContent !== '投稿中…'")
 check('Double submit => one fetch',len(posts)-start==1)
 check('API exact design fields',set(posts[-1]['body'])=={'eventId','creatorId','artworkId','message','displayName','anonymous','agreement'})
 check('Numeric resource IDs',all(type(posts[-1]['body'][k]) is int for k in ['eventId','creatorId','artworkId']))
 check('CSRF header present',posts[-1]['headers'].get('x-csrf-token')=='probe-token')
 check('Idempotency header present',bool(posts[-1]['headers'].get('idempotency-key')))
 check('Artwork page contains new petal',page.locator('.petal').count()==1)
 check('Visible count updated',page.locator('[data-message-count]').inner_text()=='1')
 check('Text alternative updated','感動しました' in page.locator('[data-words-list]').inner_text())
 check('Form reset after save',page.locator('#messageText').input_value()=='')
 page.locator('.petal').click();check('Modal anonymity',page.locator('#modalDisplayName').inner_text()=='匿名');check('Modal date/work/creator',all(page.locator('#'+x).inner_text() for x in ['modalCreatedAt','modalArtworkTitle','modalCreatorName']));page.locator('#closeMessageModalButton').click()
 # No user text is parsed as HTML.
 page.evaluate("Uka.renderPetals({messageCount:1,growthStage:1,petals:[{messageId:50,petalType:1,positionX:40,positionY:40,rotation:0,scale:1,message:'<img src=x onerror=window.XSS=1>',displayName:'匿名'}]})")
 page.locator('.petal').click();check('Modal text not executed as HTML',page.locator('#modalMessage img').count()==0 and page.evaluate('window.XSS===undefined'));page.locator('#closeMessageModalButton').click()
 mode='network';load();fill('再送テスト');submit_wait();key=posts[-1]['headers']['idempotency-key'];check('Network failure retains input',page.locator('#messageText').input_value()=='再送テスト');check('Network failure reenables button',not page.locator('#submitMessageButton').is_disabled())
 mode='normal';submit_wait();check('Network retry uses same receipt key',posts[-1]['headers']['idempotency-key']==key)
 mode='429';load();fill('投稿制限');submit_wait();check('429 shows understandable error','同じ感想' in page.locator('#messageError').inner_text());check('429 retains input',page.locator('#messageText').input_value()=='投稿制限')
 mode='pending';load();fill('審査対象');submit_wait();check('Pending not added to public petals',page.locator('.petal').count()==0);check('Pending completion copy','確認後' in page.locator('#messageSuccess').inner_text())
 mode='normal';load();page.evaluate('''()=>Uka.renderPetals({messageCount:501,growthStage:4,petals:Array.from({length:501},(_,i)=>({messageId:i+1,petalType:1,positionX:20+i%60,positionY:30,rotation:0,scale:1,displayName:'匿名',message:'表示試験'}))})''')
 check('Rendered maximum 300',page.locator('.petal').count()==300);check('Total remains 501',page.locator('[data-message-count]').inner_text()=='501');check('Growth stage connected',page.locator('[data-uka-board]').get_attribute('data-growth-stage')=='4')
 page.set_viewport_size({'width':390,'height':844});page.locator('#globalMenuButton').click();check('Mobile menu locks scroll',page.evaluate("document.body.style.overflow==='hidden'"));page.keyboard.press('Escape');check('Escape closes and restores scroll',page.evaluate("document.body.style.overflow!=='hidden'"));check('No mobile horizontal page overflow',page.evaluate('document.documentElement.scrollWidth<=window.innerWidth+1'))
 page.screenshot(path=str(OUT/'browser-mobile.png'),full_page=True)
 # Exercise the actual 900ms animation once, not only the reduced-motion path.
 ctx2=browser.new_context(viewport={'width':1100,'height':900},reduced_motion='no-preference');ctx2.expose_binding('mockApi',api_binding);p2=ctx2.new_page();setup_page(p2);p2.locator('#messageText').fill('アニメーション確認');p2.locator('#agreementFlag').check();p2.locator('#submitMessageButton').click();p2.wait_for_selector('.flying-petal');check('Flying text-to-petal animation executed',p2.locator('.flying-petal').count()==1);p2.wait_for_function("document.querySelector('#submitMessageButton').textContent !== '投稿中…'");check('Animation element cleaned up',p2.locator('.flying-petal').count()==0)
 p2.screenshot(path=str(OUT/'browser-desktop.png'),full_page=True)
 check('No JavaScript page errors',not errors,str(errors));browser.close()

(OUT/'browser-checks.json').write_text(json.dumps({'scope':'Actual generated HTML fragments and JS/CSS; mocked API responses; not Spring/Thymeleaf/DB integration','checks':checks},ensure_ascii=False,indent=2))
print('BROWSER',len(checks),'PASSED',sum(x['pass'] for x in checks))
