from pathlib import Path
import subprocess,sys,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[2]
for p in root.rglob('pom.xml'):ET.parse(p)
for p in root.glob('**/src/main/resources/static/**/*.js'):subprocess.run(['node','--check',str(p)],check=True)
out=root/'verification/local-results/classes';out.mkdir(parents=True,exist_ok=True)
source=root/'verification/local-source'
files=[source/'JavaSyntaxCheck.java',source/'LocalPolicyProbe.java']
for name in ['PetalLayout','PostingPolicy','MessagePolicy','MessageStatus','InvalidMessageException','RateLimitExceededException','SafeUrls']:
 files+=list(root.glob('**/src/main/java/**/'+name+'.java'))
subprocess.run(['javac','--release','21','-encoding','UTF-8','-d',str(out)]+list(map(str,files)),check=True)
subprocess.run(['java','-cp',str(out),'LocalPolicyProbe'],check=True)
subprocess.run(['java','-cp',str(out),'JavaSyntaxCheck',str(root)],check=True)
