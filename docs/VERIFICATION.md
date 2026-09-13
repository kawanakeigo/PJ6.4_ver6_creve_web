# 検証結果 — 2026-09-14

## 結論

ソースの再作成、事業別分離と個別部品の検証に加え、2026-09-14にMac上でMaven全体ビルド、Spring Boot起動、実PostgreSQL結合試験を実施し、すべて成功した。ただし、**全要件適合・全管理操作・実端末・負荷・本番公開可能とは判定していない**。

|確認|結果|何を確認したか／確認していないか|
|---|---|---|
|Java構文|全98製品Java＋6テストJava＋2検証用JavaをJDK 21で構文解析しエラー0|構文解析であり、Spring依存込みの型解決・コンパイルではない|
|純Java部品|実製品ソース7ファイルをjavacでコンパイルし実行。20,039アサーション成功|投稿境界・制限・URL・5,000個の座標/種別/安定性の反復検査。20,039個の独立業務ケースという意味ではない。スタブなし|
|画面部品|Chromiumで35項目成功|実HTML断片・JS/CSSを使用。APIは明示的に模擬。Spring/Thymeleaf/実DBは起動していない|
|静的資材・依存|131チェック成功|POM XML、JS構文、資材リンク、断片ファイル、PJ間import禁止等。最終の管理画面移動後も構文・資材を再確認|
|導入・PJ追加|15チェック成功|shell/Python構文、仮フォルダでのdry-run/バックアップ差し替え、2つの新PJ追加、重複拒否。Mac原本は触らない|
|元ファイルの保持|177ファイルのSHA-256が提出ZIPと一致|原本を変更しない再構成|
|Maven全体ビルド・JUnit|成功|全7モジュールをJDK 25（Java 21 release指定）でコンパイル・パッケージ化。単体テスト60件、失敗0・エラー0・スキップ0|
|実PostgreSQL・Spring・HTTP・CSRF|成功|PostgreSQL 17.11、Flyway 4マイグレーション、JPA validate、Spring Boot実起動、HTTP/CSRF/投稿/制限/公開境界/DB制約の結合テスト9件が成功。管理者の実ログイン操作は未実施|
|Safari・実端末・負荷・本番構成|**未検証**|Chromiumの部品検証で代替できない|

## 実際のログ

梱包時のログは`verification/JavaSyntaxCheck.txt`、`LocalPolicyProbe.txt`、`browser-checks.json`、`static-checks.json`、`operation-checks.json`、`maven-attempt.txt`、`original-integrity.json`。Mac再検証のJUnit XML/TXTは各モジュールの`target/surefire-reports`、実DB結合試験は`app/target/failsafe-reports`に生成される。`target`はGit管理対象外である。

ブラウザのサンプル画像は `verification/browser-desktop.png` と `browser-mobile.png`。これは**検証用データを補った部品画面**であり、完成した実サイトの起動スクリーンショットではない。

## 再実行

JDK 21・Node.js・Python 3がある環境で、依存ライブラリなしの部品/構文検査：

```bash
python3 verification/tools/check_static.py
```

ブラウザ部品検査にはPlaywrightとBeautifulSoup4、実行可能なChromiumが必要：

```bash
python3 verification/tools/browser_verify.py
```

このスクリプトはオフラインのDOMへ実資材を読み込み、明示的なmockApiで応答を返す。外部サイトやユーザーDBには接続しない。ブラウザのインストール場所が`chromium`コマンドで見つからない場合は、Playwrightが管理するChromiumのインストールを利用するよう実行パスを設定する必要がある。

Maven依存取得＋本物のSpring/DB/HTTP検査（2026-09-14に成功）：

```bash
bash Verify.command
```

検証失敗を隠して先へ進むスクリプトにはしていない。エラーを確認して修正し、すべて成功した後も、管理者の登録/編集・メディア・QR・ログイン制限・公開運用の受入試験を実施する。

## 最終梱包前の再検査

最終編集後にJavaScript構文、テンプレート断片・静的資材の参照、事業間import禁止、同一要素のth:each/th:replace競合を再検査し、251チェックが成功しました。結果は `verification/final-static-checks.json`。また純Java部品・全Java構文を再実行し、結果を `verification/final-local-verification.txt` に保存しました。これらもSpring/Thymeleaf本体の起動試験を意味しません。
