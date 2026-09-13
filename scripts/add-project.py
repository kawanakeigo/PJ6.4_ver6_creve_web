#!/usr/bin/env python3
"""Add an independent Maven product; no CreVe/PLAYPIT source changes."""
from pathlib import Path
import argparse, re, shutil, tempfile
root=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser(description=__doc__);p.add_argument('name');p.add_argument('--title',default=None);a=p.parse_args()
name=a.name
if not re.fullmatch(r'[a-z][a-z0-9]{1,29}',name): p.error('英小文字で始まる2〜30文字の英小文字・数字を指定してください。')
if name in {'admin','api','platform','common','error','login','static','assets','about','privacy','terms','contact','news','vr'}: p.error('予約された名前です。')
target=root/'projects'/name
if target.exists(): p.error('同名PJが既に存在するため変更しません。')
title=a.title or name
# UI text is escaped at generation; Java string has no user title interpolation.
import html
escaped=html.escape(title)
parent=(root/'pom.xml').read_text();app=(root/'app/pom.xml').read_text()
if '</modules>' not in parent or '</dependencies>' not in app: p.error('pom構成を確認できません。変更しません。')
new_parent=parent.replace('</modules>',f'<module>projects/{name}</module></modules>',1)
new_app=app.replace('</dependencies>',f'<dependency><groupId>world.creve</groupId><artifactId>{name}</artifactId><version>${{project.version}}</version></dependency></dependencies>',1)
pack='world.creve.'+name
files={
'pom.xml':f'<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><parent><groupId>world.creve</groupId><artifactId>creve-parent</artifactId><version>1.0.0-SNAPSHOT</version><relativePath>../../pom.xml</relativePath></parent><artifactId>{name}</artifactId><dependencies><dependency><groupId>world.creve</groupId><artifactId>platform</artifactId><version>${{project.version}}</version></dependency></dependencies></project>\n',
f'src/main/java/{pack.replace(".","/")}/ProjectController.java':f'package {pack};\nimport org.springframework.stereotype.Controller;import org.springframework.web.bind.annotation.GetMapping;\n@Controller("{name}ProjectController") public class ProjectController {{ @GetMapping("/{name}") public String index() {{return "{name}/index";}} }}\n',
f'src/main/java/{pack.replace(".","/")}/Contribution.java':f'package {pack};\nimport org.springframework.stereotype.Component;import java.util.List;import world.creve.platform.spi.ProjectContribution;import world.creve.platform.dto.EventResponse;\n@Component("{name}ProjectContribution") public class Contribution implements ProjectContribution {{ public String key(){{return "{name}";}} public String title(){{return "{name}";}} public String path(){{return "/{name}";}} public List<EventResponse> upcomingEvents(){{return List.of();}} }}\n',
f'src/main/resources/templates/{name}/index.html':f'<!doctype html><html lang="ja" xmlns:th="http://www.thymeleaf.org"><head><meta charset="UTF-8"><title>{escaped}</title><link rel="stylesheet" th:href="@{{/{name}/css/{name}.css}}"></head><body><h1>{escaped}</h1><p>このPJの仕様に従って作成してください。</p><a href="/">CreVe</a></body></html>\n',
f'src/main/resources/static/{name}/css/{name}.css':f'/* {name} only */\n',
f'src/main/resources/static/{name}/js/{name}.js':"'use strict';\n"}
staging=Path(tempfile.mkdtemp(prefix='.project-',dir=root/'projects'))
try:
 for relative,content in files.items():
  f=staging/relative;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(content,encoding='utf-8')
 staging.rename(target)
 try:
  (root/'pom.xml').write_text(new_parent);(root/'app/pom.xml').write_text(new_app)
 except Exception:
  (root/'pom.xml').write_text(parent);(root/'app/pom.xml').write_text(app);shutil.rmtree(target);raise
finally:
 if staging.exists():shutil.rmtree(staging)
print(f'projects/{name} を追加しました。root/appのpomのみ連携変更しました。./mvnw verify を実行してください。')
