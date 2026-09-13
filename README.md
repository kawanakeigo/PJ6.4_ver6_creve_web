# CreVe / PLAYPIT — 設計修正版・事業別モジュール構成

2026-09-14。元の提出ZIPは変更せず、新しく再構成したソース一式です。

**本番利用を承認した完成品ではありません。** 2026-09-14にMac上で`Verify.command`を実行し、全Mavenモジュールのビルド、60件の単体テスト、専用PostgreSQLを使う9件の結合テストが成功しました。実施済みの検証と、管理画面の全操作・実端末・負荷・本番構成など未実施の検証を `docs/VERIFICATION.md` に分けています。環境変更後や導入前には、改めて `Verify.command` を実行してください。

## 1. CreVeとPLAYPITの置き場所

```text
creve-web/                         ← 作業フォルダ・親Mavenプロジェクト
├── pom.xml
├── platform/                      ← 共有基盤。認証、共通ヘッダー、共通データ
│   ├── pom.xml
│   └── src/main/
│       ├── java/world/creve/platform/
│       └── resources/
├── projects/
│   ├── creve/                     ← CreVeのJava・画面・CSS
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/world/creve/portal/
│   │       └── resources/{templates/creve,static/creve}/
│   ├── playpit/                   ← PLAYPIT・作品・感想・羽花・投稿管理
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/world/creve/playpit/
│   │       └── resources/{templates/playpit,static/playpit}/
│   ├── live/                      ← ライブのJava・画面・CSS
│   └── lp/                        ← LPのJava・画面・CSS
├── app/                           ← 起動・実行設定・管理者初期作成
├── config/public/                 ← 運営者が確定する公開文章
├── scripts/                       ← 起動、検証、導入、PJ追加
├── docs/                          ← 変更内容、差異、導入条件
└── verification/                  ← 実施済み検証の結果と再実行コード
```

単なるフォルダ分けではなく、**各PJにpom.xmlを持たせたMavenモジュール**です。CreVe・PLAYPIT・ライブ・LP間の直接importはありません。各PJが共有基盤に依存し、`app` が組み合わせて起動します。既存URL `/`、`/playpit/...`、`/live/...`、`/lp/...` は維持します。

**サーバーをPJごとに分けた構成ではありません。** 元設計の「一つのSpring Boot＋Thymeleafアプリ」を維持しながら、ソース・画面資材・依存関係を分離しています。別Gitリポジトリ化・別サーバー公開・DB分割は今回行っていません。

## 2. 最初に行うこと

元の `Documents/.../creve-web` に直接展開・上書きせず、Downloadsなど別の場所へ展開してください。**旧版に保存したい投稿があり、メモリ内H2で動いている場合は、アプリを停止する前にデータを書き出してください。** フォルダのバックアップだけではDBは保存されません。データを保全してから既存アプリを停止してください。

前提は **JDK 21以上、Docker EngineとDocker Compose v2、インターネット接続** です。導入・PJ追加用のPythonスクリプトにはPython 3が必要です。

展開した `creve-web` 内で実行します。

```bash
bash Start.command
```

このコマンドは、ランダムな秘密情報を含む `.env.local` を初回だけ作成し、Maven単体テスト・パッケージ作成、新しいPostgreSQL、Javaアプリを順に起動します。エラーが起きた時点で停止します。起動完了後にブラウザで `http://localhost:8080/` を開いてください。

管理画面は `http://localhost:8080/admin/login`。初期の管理者メールは `.env.local` の `CREVE_ADMIN_EMAIL`、ランダムなパスワードは `CREVE_ADMIN_PASSWORD` です。固定の共通パスワードは同梱していません。このファイルは公開・Gitコミット・チャット添付しないでください。既に管理者がいるDBでは自動初期化によってパスワードをリセットしません。

新しいDBの名前は `creve_modular`、ポートは `55432`、永続ボリュームは専用の `creve_postgres_v2` です。旧版のDBを自動接続・変更・削除しません。アプリ停止は Control+C、DB停止は次のコマンドです。`-v` は付けないでください。

```bash
docker compose --env-file .env.local stop db
```

### 起動時点で投稿を停止している理由

`.env.local` の `CREVE_POSTING_ENABLED=false` が初期値です。利用規約・プライバシーポリシー・問い合わせ先の確定本文が設計資料にないため、それらを仮文で完成扱いにしないための運用上の停止スイッチです。未設定でも閲覧・管理者による登録・編集はできます。

運営者が次のUTF-8プレーンテキストを用意し、公開内容を確認した後に投稿受付を有効にしてください。HTMLとして実行せず、文章として表示します。

```text
config/public/terms.txt
config/public/privacy.txt
config/public/contact.txt
```

検証環境で投稿を試す場合も、その環境の用途と文章を確認して `.env.local` の `CREVE_POSTING_ENABLED=true` に変更し、アプリを再起動してください。本番規約のひな型をこちらで確定したわけではありません。

### 作品・イベントが表示されない場合

旧版のダミーデータや本物か確認できないイベント情報は自動投入しません。管理画面で、クリエイター → 作品 → イベントの順に登録します。各データの状態を `PUBLISHED` にし、イベント編集画面で「参加クリエイター」「出展作品」の関係も登録してください。作品と人物を登録しただけでは、そのイベントへの所属は作成されません。

SNSは `ラベル|URL` を1行ずつ、複数メディアは `IMAGE|URL|説明`、`VIDEO|URL|説明`、`AUDIO|URL|説明` を1行ずつ入力します。作品の制作者は登録後には変更できません。画像・音声・動画の本体アップロードは今回の設計範囲で方式が確定していないため、URL指定方式です。

## 3. 実DBでの検証

```bash
bash Verify.command
```

単体テストに続き、**別名の検証専用DB `creve_modular_test`（ポート55433、一時領域）** を立ち上げ、実際のSpring Boot・Flyway・JPA・HTTP・CSRFを使用する結合試験を実行します。テストはローカルの専用DB名以外を拒否します。検証用DBは終了時に停止・廃棄します。通常の永続DBとは別のComposeプロジェクトです。

`target/surefire-reports` と `app/target/failsafe-reports` が実行結果です。Java起動確認だけではなく、入力条件、保存、再送、3回制限、10分重複、公開状態、匿名、並行再送、300件表示、DB制約を確認するテストを同梱しています。ただし、全業務・全ブラウザ・全管理操作を網羅する総合試験ではありません。

## 4. Macの元フォルダへ反映

展開側で検証後、アプリを停止してから次を実行してください。

```bash
bash Install.command
```

画面に対象パスを表示し、`APPLY` と入力した場合だけ反映します。処理は次のとおりです。

- 元フォルダ全体を `creve-web_backup_日時` として隣に保存する。
- 修正版を元のパスに配置し、Gitの `.git` を引き継ぐ。
- 旧版の `.env` やDB設定は引き継がない。旧ファイルはバックアップに残す。
- 展開側で検証に使った **修正版の `.env.local`** がある場合だけ、それを新フォルダへ引き継ぐ。これにより検証時の新DBパスワードを維持する。

別のパスの場合は確認だけを先に行えます。

```bash
python3 scripts/install.py "/対象のプロジェクト"
python3 scripts/install.py "/対象のプロジェクト" --apply
```

このチャットからMac上の原本へ書き込みは行っていません。`Install.command` を実行して初めて反映されます。IDEや別の編集ツールを閉じ、同時に書き込みが行われない状態で実行してください。

## 5. 新しいPJの追加

```bash
python3 scripts/add-project.py gallery --title "GALLERY"
./mvnw verify
```

`projects/gallery/` に独立したpom、Java、HTML、CSS、JavaScriptを作成します。親pomと`app/pom.xml`に組込みを追加します。CreVeやPLAYPITの内部コードは変更しません。`ProjectContribution` の実装を共有ナビゲーションとCreVe側が取得します。生成されるのはひな型であり、新PJの業務機能まで自動で実装するものではありません。

DBが必要なPJは、自分のモジュールにEntity・Repository・Flyway SQLを追加してください。マイグレーション番号は全モジュールで重複させないでください。既存PJを**削除**するための依存整理や、事業ごとの独立デプロイまでは自動化していません。

## 6. その他の実行方法

Dockerを使わず、別途用意した**新しい空のPostgreSQL**に接続する場合は、`.env.example` を参考に環境変数を設定して起動します。旧版DBのURLを流用しないでください。

```bash
./mvnw -B clean verify
# 必要なCREVE_DATASOURCE_*、CREVE_SESSION_HASH_SALT等を環境変数へ設定した後
java -jar app/target/app-1.0.0-SNAPSHOT.jar
```

この修正版のDBをバックアップする場合は `bash scripts/backup-db.sh`。旧版DBの移行については `docs/LEGACY_DATA.md` を参照してください。

## 7. 詳細資料

`docs/ARCHITECTURE.md`：事業別分離と元設計との対応。
`docs/DECISIONS.md`：設計本文にない補足判断と未確定事項。
`docs/REMEDIATION.md`：前回監査A01〜A35への対応。
`docs/VERIFICATION.md`：確認できたこと・できていないこと。
`docs/source/PLAYPIT_design_source.txt`：元設計本文の変更していないコピー。
