# 検証結果 — 2026-09-14

## 結論

ソースの再作成、事業別分離と個別部品の検証に加え、2026-09-14にMac上でMaven全体ビルド、Spring Boot起動、実PostgreSQL結合試験を実施し、すべて成功した。ただし、**全要件適合・全管理操作・実端末・負荷・本番公開可能とは判定していない**。

|確認|結果|何を確認したか／確認していないか|
|---|---|---|
|Java構文|全99製品Java＋6テストJava＋2検証用JavaをJDK 21で構文解析しエラー0|構文解析であり、Spring依存込みの型解決・コンパイルではない|
|純Java部品|実製品ソース7ファイルをjavacでコンパイルし実行。20,039アサーション成功|投稿境界・制限・URL・5,000個の座標/種別/安定性の反復検査。20,039個の独立業務ケースという意味ではない。スタブなし|
|画面部品|Chromiumで35項目成功|実HTML断片・JS/CSSを使用。APIは明示的に模擬。Spring/Thymeleaf/実DBは起動していない|
|静的資材・依存|131チェック成功|POM XML、JS構文、資材リンク、断片ファイル、PJ間import禁止等。最終の管理画面移動後も構文・資材を再確認|
|導入・PJ追加|15チェック成功|shell/Python構文、仮フォルダでのdry-run/バックアップ差し替え、2つの新PJ追加、重複拒否。Mac原本は触らない|
|元ファイルの保持|177ファイルのSHA-256が提出ZIPと一致|原本を変更しない再構成|
|Maven全体ビルド・JUnit|成功|全7モジュールをJDK 25（Java 21 release指定）でコンパイル・パッケージ化。単体テスト60件、失敗0・エラー0・スキップ0|
|実PostgreSQL・Spring・HTTP・CSRF|成功|PostgreSQL 17.11、Flyway 5マイグレーション、JPA validate、Spring Boot実起動、HTTP/CSRF/投稿/制限/公開境界/DB制約の結合テスト9件が成功。`/creators`、`/NLJ`、旧URLの308転送を含む。管理者の実ログイン操作は未実施|
|起動後の公開画面|成功|Chromiumの1440×900と390×844で `/creators` と `/NLJ` の空状態、ナビゲーション、文字収まりを確認。`/live` と `/PLAYPIT/events` の正規URLへの転送も確認|
|Safari・実端末・負荷・本番構成|**未検証**|Chromiumの起動確認で代替できない|

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

## 2026-10-31：共通クリエイターページへの導線修正

`./mvnw -B -o test` が全7モジュールで成功。既存60件に追加5件を加えた単体テスト65件、失敗・エラー・スキップ0。`SharedCreatorTest` は同一プロフィールIDの2イベント表示、イベント別作品・感想サービス呼出し、参加検証、旧URLの308転送、参加なしの作品一覧、管理サービスの既存参加更新・別イベント追加とプロフィール非複製、slug変更拒否を検証する。`CreatorTemplateTest` は実Thymeleafで2イベントの紹介カードを生成し、共通プロフィールパスとイベントクエリを確認する。サービス境界のテストはMockitoを使用し、実DBの保存件数確認とは区別する。

`ApplicationPostgresIT` に、同一クリエイターの再参加時のDB件数・参加の重複防止、両イベントからの共通リンク、旧URL転送、作品・感想のイベント分離、非公開イベントの拒否を確認する結合ケースを追加。また管理者ログイン、既存ID確認画面、HTTPでの再参加登録・重複登録、共通プロフィール編集と両イベントへの反映、slug変更拒否、非公開クリエイターの閲覧拒否を確認するケースを追加。

当初はDocker daemon未稼働だったが、追加指示に従ってインストール済みDocker Desktopを通常起動し解消。既存の `scripts/verify.sh` は固定Composeプロジェクトの事前終了を含むためそのまま実行せず、新規の一意なコンテナ名、ランダムなlocalhostポート、tmpfs上の `creve_modular_test` を使用。既存のpostgres:17イメージを使用し、新規インストールや設定変更はしていない。テストDB名を検証する既存の安全条件を維持し、`./mvnw -B -o -Ppostgres-it verify` を実行した。

全7モジュールのビルド成功。単体65件と実PostgreSQL結合11件が成功し、失敗・エラー・スキップ0。実際のSpring Boot・Flyway・JPA・HTTP・CSRF・Thymeleafを使用した。今回作成した一時コンテナは終了時に削除し、テストアプリも終了。既存DBの内容は読み書きしていない。Docker起動時には既存の再起動ポリシーに従い一部既存コンテナも自動再開していたが、それらへ停止・変更操作はしていない。

結果は `app/target/failsafe-reports/world.creve.ApplicationPostgresIT.txt` と各モジュールの `target/surefire-reports` に保存。実ブラウザでの全体レイアウト・Safari実端末は今回未確認。HTTPによる管理操作・HTML生成の成功と、視覚的な確認は区別する。

## 2026-10-31：ナビ修復・イベント掲載の再検証

一時DBで `./mvnw -B -o -Ppostgres-it verify` を再実行。単体65件、結合13件がすべて成功（失敗・エラー・スキップ0）。途中保存時の結合12件中4失敗は解消。追加ケースではPLAYPITと共通プロフィールのナビ非表示、他PJナビと直接URL維持、日付のみの開始表示、終了時刻表示、説明URLのリンク化とHTMLエスケープを検証した。

既存ローカルDBにBeforeAfter展を管理サービスで1件登録し、再起動後の `/playpit`、`/playpit/events`、`/playpit/beforeafter-2026` の200とイベント内容を確認。`/creators`、`/admin/login`、`/NLJ`、`/about`、`/vr`も200。未認証の`/admin/creators/new`はログイン画面へ転送。実ユーザーでのログイン操作は行っていない。

SafariのPLAYPITトップで対象ナビ3項目の非表示、クリエイター紹介リンク維持、BeforeAfter展カード・会期・会場・紹介文の表示を確認。最新ビルドは実行中JARの上書きの影響を避けるため `/tmp/playpit-preview-verified.jar` に複製して起動し、127.0.0.1限定、Flywayと管理者初期作成は無効のまま。DBスキーマ変更・削除、外部公開はしていない。

## PLAYPIT専用情報ページの検証

`./mvnw -B -o -Ppostgres-it verify`：単体67件・結合14件、全成功（失敗・エラー・スキップ0）。専用お知らせへの他PJニュース・NLJ・非公開イベントの混入防止、専用問い合わせ本文の未設定・空・サイズ上限、共通ページの維持、ナビリンク先とheader/footer各1件を確認。

実サイトで `/playpit/news`、`/playpit/contact`、`/news`、`/contact`、`/admin/login`、既存BeforeAfter展が200。Safariで専用お知らせのBeforeAfter展表示・問い合わせの準備中表示を確認し、最後に管理者ログインタブを前面に戻した。認証情報入力・管理者作成変更・第三者への送信は実施していない。問い合わせ先確定後の実連絡先表示と、一般のお知らせ投稿機能は今回の完了範囲外。

## 指定管理者のローカル運用ツール

`scripts/manage-playpit-admin.sh` と `scripts/admin/AdminAccountTool.java` を追加。対象ID・ローカルDBを固定し、本人のConsole.readPassword入力とADD/CHANGE確認を経てからトランザクションで保存する。既存アカウントを維持し、パスワード変更は現在のパスワード照合を必須にする。

隔離した一時PostgreSQLで `AdminAccountToolTest` を実行し、19チェック成功。追加件数、BCrypt強度12、既存管理者保持、大小文字を含む重複拒否、短い/72バイト超パスワード拒否、現パスワード不一致時の非更新、変更成功、旧パスワード不一致、無効/未存在アカウントの拒否を確認。シェル構文とJavaコンパイル成功、シェル/Java双方の非対話入力拒否も確認した。テスト用パスワードは実行時ランダム生成で表示していない。

実アカウントの追加はユーザーの非表示入力・最終確認待ち。本人入力前に追加済みとは扱わない。

## 提供ロゴの確認

元PNGと配置PNGのSHA-256一致を確認。全体ビルド・単体67件・実DB結合15件が成功。PLAYPIT公開画面のロゴ2箇所、alt、トップリンク、画像のHTTP配信と元バイト一致、他PJ・管理画面のブランド維持を検証。SafariのPLAYPITトップで実際のヘッダーロゴの表示サイズ・比率をスクリーンショットで確認した。最終表示は管理者ログインタブへ戻した。

管理者ツールは準備・隔離DB19チェックまで完了。Terminalアプリを自動で入力待ちにする操作は、コンピューター操作ツールが `com.apple.Terminal` の操作を安全上の理由で拒否したため実行できなかった。別経路での自動起動は行わず、本人がREADMEのコマンドを実行して非表示入力・最終確認する手順とした。

### 2026-10-31 画像ファイルと人物別作品登録
- 隔離したPostgreSQL 17（新規一時コンテナ、tmpfs、localhost動的ポート）で `./mvnw -B -o -Ppostgres-it verify`：単体71件・結合18件成功。実DBは使用していない。
- JPEG/PNG実デコード、10MiB・4096px制限、SVG/破損画像拒否、生成ファイル名・パス/シンボリックリンク拒否、ストレージ再生成後の再取得を単体確認。
- HTTP multipart+CSRF、未ログイン拒否、下書き画像非公開、公開状態の変更反映、人物ID固定、複数作品、イベント参加未作成、画像未選択時保持、DBロールバック時ファイル除去、413エラーを結合確認。
- Safariプライベートウインドウ＋別の隔離アプリ/DBで実ファイル選択→プロフィール保存→人物から作品追加→偽PNG拒否と入力保持→正しいPNGへ再選択→作品保存→画像を保持して名称を再編集→人物の作品一覧で再表示を確認。検証ウインドウと一時アプリ/DBは終了済み。実ユーザーの通常Safariセッションと入力途中のフォームは保持した。
- スマートフォン実機のファイル選択は未検証。標準HTML file inputを使用。ユーザーが未保存フォームの破棄を承認したため、検証済みJARを実ローカル環境へ反映済み。PID 91932、127.0.0.1:8080限定、Flyway・管理者初期生成無効を確認。主要5ページ200。Safariはセッション失効により/admin/loginを表示し、本人再ログイン待ち。新版管理フォームの実環境ログイン後表示は未確認（隔離Safariでは確認済み）。

### 2026-10-31 ローカル管理者パスワード再設定手順
`reset-password` を本人のConsole非表示入力・対象ID付き最終確認で実行する機能を追加。実アカウントでの実行は行っていない。PostgreSQL17の新規tmpfs一時DBで `AdminAccountToolTest` 33項目成功（既存add/changeの19項目＋resetの14項目）。現在PW不要の再設定、新旧PW照合、BCrypt12、ID/有効状態維持、別admin維持、対象不在、大小文字違い重複、無効アカウント、確認中止/EOF、短いPW拒否を確認。テスト用DBは終了時に除去。shell構文とdiffチェックも成功。本人ターミナルでの最終入力・確定と実ログインは未実行。公開Webリセット画面は後日の要望として未実装。

### 作品識別子自動化検証
単体74件・隔離PostgreSQL結合19件成功。新規slug未指定・空文字・日本語指定を無視して自動生成、日本語同名の連続登録、画像添付、既存URL維持、作品名必須を確認。候補衝突時の再生成・上限・既存URL維持を単体確認。Safariプライベート＋別一時DBで識別子欄なしの日本語名/画像登録、再編集でURL不変、同じ元タイトルの別作品に別URLの発行を確認。検証アプリ/DBは終了。通常Safariの未保存作品フォーム（/admin/creators/1/artworks）は入力を保持。実ローカルサイトは再起動しておらず、反映はその入力の扱いについて本人確認待ち。

### 添付JPEGの実画像検証（2026-11-01 UTC）
Libraryのユーザー添付 IMG_6076.JPG をMac作業領域へ取得して実ピクセルを確認。1,017,106 bytes、1773×1773、baseline JPEG 3成分RGB、sRGBプロファイル、EXIF APP1なし。寸法/容量制限内で、向きのEXIF依存なし。原本は変更せずリポジトリにも格納していない。新規の隔離PostgreSQL＋localhost一時アプリで旧版と修正版を比較した。旧版＋slug=Kawayanは「識別子を確認してください。」を再現。旧版＋妥当なslugは同じJPEGで保存成功。修正版＋slug未指定も保存成功。両成功例で保存後の画像URLを取得し、返されたimage/jpegの全バイトが原本と一致することを確認。画像制限緩和や再圧縮は不要だった。自動slug修正で今回再現した障害を解消。JPEG保存の生成画像回帰テストを追加。実環境の未保存作品入力は保持し、再起動・実作品への紐付け・登録は未実施。比較用の両アプリと一時DBは終了済み。

### クリエイター新構成（公開作品最大3点 → 紹介 → 作品一覧）
- `bash /tmp/playpit-story-it.sh` により新規の一時PostgreSQL/tmpfs・専用 `creve_modular_test` で検証。単体76件、結合21件、失敗0。対象テスト `creatorOpeningAboutAndWorksRespectPublicationAndPaging` は0/1/2/3/4作品、公開制御、ページ移動で冒頭固定、本人アイコン保持、紹介文HTMLエスケープをHTTPで確認。
- `git diff --check` 成功。最新JAR SHA256: `a748b0d728bdca51ff9c5f06d5741055d14cd8fcd7a9d6925cb3180e7dcf6c06`。
- 提供参考IMG_8605.PNGを公式Library helperでMacへ取得し実画素確認。配置参考のみで画像の転載なし。
- ゴッホの独立静的デモ `http://localhost:8094/` と390px幅の `/mobile.html` をSafariで表示・目視。冒頭3作品→自画像/名前/紹介→4作品、原比率、本文/出典導線を確認。Met公式公開画像5点をローカル取得・目視、実DB登録なし。
- 通常8080は旧PID5168を保持。この改修JARへのアプリ切替は未実施。イベント/トップ/CSPの改修なし。
- 続報：Safariの旧編集タブが未入力のログイン画面のままで、新しい編集タブがないことを再確認し、最新ユーザー依頼に必要な通常反映として切替完了。新PID6543、127.0.0.1:8080限定、Flyway/bootstrap無効を維持。クリエイター詳細で `selectedWorks → about → works` のHTML順序を確認、トップ/一覧/イベント一覧/管理ログインもHTTP200。Safari実表示で既存作品と本人アイコンを確認。ゴッホ8094もHTTP200で維持。

### 展示テーマ・LINE・動画反映
- 単体76件、隔離PostgreSQL結合22件成功。新テストで公開HTMLの資料/フォームURL非表示、元description/ticketUrl保持、テーマエスケープ、トップのみのCSP例外、初期iframeなしを確認。
- 最終CSSの動画見出し折返しを調整して再パッケージ成功。最終JAR SHA256 `b6f6d97ddecf62b66bb731d402becae75c21e79fe469cf6403067be6c42bf418`。
- 未保存入力なしをSafariで確認し切替。PID7482、127.0.0.1:8080、migration/bootstrap無効維持。DB更新はBeforeAfter event_id=1を旧値一致条件で1件のみ、トランザクションで実施。元URLと旧summaryの保持をDB読取り確認。
- 通常サイトのトップ/イベント一覧/詳細/作家/管理ログインは200。HTMLのURL漏れなし、CSP対象限定確認。SafariでPCイベント詳細を目視。独立IABで実サイトを390pxにしてイベント一覧/詳細/トップを目視し横溢れなし。動画はクリック前iframe0、クリック後プレーヤー、手動再生で実映像まで確認。検証用viewportは解除。
- ゴッホデモ8094を保持しHTTP200。公開deploy/commit/pushなし。
- テーマ全文差替え：最新ユーザー指定286文字をBeforeAfter event_id=1のsummaryへ全文保存。外側の引用符のみ除外し、内部引用符・7改行・1空行区切りを保持。DB更新前後でsummary以外の全フィールド一致、HTTP200のテーマ段落と指定文字列の完全一致を確認。Safariアクセシビリティ表示でも全文確認。ユーザーがSafariを操作し始めたため追加スクロールは行わず現在表示を保持。コード変更・アプリ再起動なし、PID7482継続。

### 一般/募集LP分離（反映待ち）
- 単体76件・隔離PostgreSQL結合23件成功。一般LINE/料金なし、募集Artists/Works/料金なし、テーマ共通、元料金データ保持、非公開/別種イベント404、汎用LP回帰、非誘導・CSP対象限定を検証。
- `git diff --check` 成功。検証済みJAR SHA256 `7e17aa38189755d5f01b4fd490f68833d0f3843e7d6f80868e2aff62dbd02e36`。
- Safariには直前まで一般イベントと管理ログインタブが存在。管理タブ選択後の状態確認が30秒タイムアウトし、再取得もタイムアウト。未保存入力の現状を確定できないため切替・再起動は未実施。現行PID7482維持。新LPの実サイトHTTP/PC390pxの目視は切替後の残作業。
- 続報：未保存確認タイムアウトを説明した上でユーザーが再起動反映を承認。上記検証済みJARへ切替完了、新PID8757、127.0.0.1:8080限定、migration/bootstrap無効。
- 一般 `/playpit/beforeafter-2026` と募集 `/lp/playpit/beforeafter-2026` はHTTP200。両方のテーマ286文字・改行は指定原文と完全一致。一般LINEなし、両料金なし、募集Artists/Worksなし、TOPからLPへのリンクなし、CSP対象分離を実HTTPで再確認。
- Safari復旧し募集LPのPC実表示を確認。独立IABで両ページ390px全体を目視、横溢れなし。募集LPはクリック前iframe0、クリック後プレーヤー、手動再生で実映像を確認。viewport解除済み。Safariは募集LPを別タブに表示。ゴッホ8094はHTTP200、実DB変更なし。

## 2026-11-02 PLAYPIT メニュー・円形アイコン（反映前QA）
- `scripts/verify.sh`: 単体78件、隔離PostgreSQL結合24件成功。
- Safari実画面: PCの6メニューと円形一覧、390pxの一覧2列・詳細アイコン・ABOUT・EXHIBITION・NEWS・CONTACTを確認。メニューの開閉とEscape閉じを確認。
- 390pxでABOUT見出しの共通CSS不足を発見し、creator.cssの見出しルールをplaypit.cssへ移動。修正後目視、単体78件とpackage成功。
- キーボードフォーカス・タッチ拡大・reduced motionはCSS対応。実機タッチとOSのreduced-motion切替は未実施。
- 隔離QA `http://127.0.0.1:8096/creators`、390px静的スナップショット `http://127.0.0.1:8097/creators-mobile.html`。後者は表示確認用で、全リンク/APIが利用できるアプリではない。
- 8080既存サイトは未切替、実DB変更なし。順位のスペース集計範囲・問い合わせフォームURL・ABOUT創設理由は未確定。
- Instagramの通常UIでCONCEPT告知画像と本文、UKA/TEN本文を確認。画像ファイル取得・開催年確定・終了時刻不一致解消・既存データ照合は未完、過去イベント登録0件。
- 最終CSS版でも `scripts/verify.sh` 再実行成功：単体78件・PostgreSQL結合24件。Safariの隔離fixtureでタッチ対象44px以上、390px横溢れなし、円形、フォーカス、通常拡大、reduced-motion分岐の拡大停止・transition 0.01ms以下の8項目成功。OS設定は変更せずCSSOMで分岐を有効化した検証であり、実機タッチは未検証。
- 承認後、既存起動手順に沿って通常8080へ切替完了（PID66001）。127.0.0.1限定、Flyway/bootstrap無効、既存env/画像保存先維持。旧JAR `/tmp/playpit-preview-before-six-menu.jar` を保存。反映後に全主要ルートHTTP200、6メニュー、一般/LP導線分離、料金/LINE制約、BeforeAfterテーマHTML完全一致、既存公開creatorの名前・紹介・画像一致を確認。実DBへ書込なし。隔離QA8096は停止済み、静的390px確認8097は維持。

### 過去展示8件の限定追加（2026-11-03）
- 2024羽化/いのちのかたち/羽化vol2、2025壁/CONCEPT/羽化、2026年8月UKA/TEN・10月CONCEPT。原資料・ユーザー確認から開催日を採用。時刻未確認は日付のみ表示。6月uka終了日の矛盾資料は保留。
- 画像なしhero、未設定住所/アクセス省略、参加者部分掲載注記。人物/作品/参加関係追加0、アイコン未設定。一般ページに料金/LINE/募集LP導線なし。
- 単体79・結合24成功。隔離DBの8件挿入と二重投入の全体停止、8詳細/一覧HTTPを確認。Safari PCおよび390px iframeの実出力snapshotで目視。実機タッチ未検証。
- 限定トランザクション前に非公開ローカルbackup。既存BeforeAfter行と非eventsデータhash不変確認。localhost8080へPID81390で反映、Flyway/bootstrap無効。通常サイト8詳細と既存一般/LP/creator/テーマのHTTP回帰成功。
- 元資料・投入SQL・backup所在・復旧方針は作業領域exhibition-research/eight-event-execution-status.json。秘密・回答連絡先は記録しない。原画像と参加者紹介は未完。

### 6参加者・順位実装（2026-11-03）
公開SNSと資料照合の6名を共通作家/SNS/参加関係各6件として限定追加。作品/画像追加0。紹介文未設定は準備中、本人アイコン未設定はaria-label付きP仮表示。部分掲載注記維持。
順位V006は旧スキーマの隔離DBで限定DDL＋履歴整合を適用し、通常Flyway検証成功後に実DBへ同じ限定DDLを適用（アプリFlyway/bootstrapは無効のまま）。既存参加関係の旧列hashと影響外テーブルhash不変。6区分だけ更新。
最終単体83件・結合25件成功。JST年越し、未来年、0下限、未知区分、同日/同点/名前、複数参加、重複、非公開/参加取消、DB制約・管理入力保持を検証。PCライブ・390px隔離出力snapshotをSafari確認。HTTPで6プロフィール/イベント分離/既存テーマとKawayan/LP制約の回帰成功。PID82862、localhost8080で起動維持。

### TOP展示の3件詳細／以降簡潔表示（2026-11-03）
開催開始日時降順・同日時event_id降順、公開PLAYPIT全件をTOPへ取得。最新3件は既存詳細カード、4件目以降はアイコン/名称/開催年だけのリンク。画像未設定はダッシュの仮表示。他一覧/詳細・料金LINE/LP制約を維持。
単体87件・結合26件成功。0/1/3/6件、同日時安定順、認証済み隔離管理フォームの実HTML項目を検証。PC/390pxの実テンプレート出力をSafari確認、Option+Tabによる簡潔カードのフォーカス可視を確認。実サイト3詳細+6簡潔・全リンク200、既存順位/BeforeAfter原文/Kawayan/LP制約の回帰成功。PID84300、127.0.0.1:8080、Flyway/bootstrap無効。実DB書込0。
実Safari管理画面は/admin/loginへ遷移。本人ログイン後フォームの目視は未実施、隔離認証HTTPとコード確認とは区別。展示画像はmainImageUrlのみでファイル選択未実装、作家アイコン/作品画像はファイル選択対応。開催予定/開催中/終了済みは日時から自動判定、公開掲載はPUBLISHEDが条件（ARCHIVEDは過去一覧へ出す設定ではない）。

## 2026-11-03 一括参加登録・展示画像アップロード
- 最終 `playpit-history-it.sh`: 単体90件・PostgreSQL結合28件、失敗0。CSV日本語/BOM/改行/不正UTF-8/列/行/容量、XLSX数式・外部参照・XXE拒否を検証。認証/CSRF、プレビュー無書込、確認未チェック、取消、対象展示違い、二重実行/同一ファイル再プレビュー、同じ作家の別展示参加、イベント別スペース維持、ランキング参加回数を検証。
- 展示画像: JPEG/PNGの既存検証を再使用。非公開画像の公開取得拒否、管理閲覧、公開後配信、未選択編集のURL保持、不正SVGのDBロールバック、TOP/一覧/詳細表示を結合検証。
- Safari隔離8098: 合成2行CSVのファイル選択→プレビュー→チェック→キーボード実行→下書き2/参加2件を実UI確認。展示PNG選択→保存→現在画像再表示も確認。PCと390pxの合成HTMLスナップショットで表/ボタン/画像比率を目視。実機iOSタッチと実Google Sheetsアプリからの書出し一連操作は未実施。
- 実DBは事前バックアップ後V007テーブルだけ限定追加。既存データ全体（移行履歴と新テーブル以外）のdata-onlyチェックサム同一。実回答の一括取込なし、新バッチ0件。旧JAR互換維持。
- localhost8080 PID86618、Flyway/bootstrap無効で反映。全公開ルート、一般/募集LP分離、LINE/料金非表示、BeforeAfter原文、既存作家/画像、TOP3詳細+6簡潔表示、6作家の開催別紐付けと順位をスモーク検証。
