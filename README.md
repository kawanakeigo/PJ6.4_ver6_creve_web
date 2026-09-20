# creve.world local P0

基本設計 v0.4、詳細設計 v0.2 に合わせて、CreVe、PLAYPIT、NLJを独立したSpring Bootアプリとして配置したローカルP0です。

```text
creve.world/
├── creve/                     # CreVe公式・作者の部屋（:8080 / DB creve）
├── playpit/                   # PLAYPIT（:8081 / DB playpit）
├── nlj-platform-persistence/  # NLJ Phase 1（:8082 / DB nlj）
├── project-overview/          # 構成・実装状況・未決事項
├── docker/postgres/init/      # ローカル3DB初期化
├── scripts/                   # 起動・停止・検証
├── compose.yml
└── pom.xml                    # 3PJの集約ビルドだけを行う
```

各PJは他PJのJavaクラス・Entity・DBを直接参照しません。入口はリンクで接続します。Cookie名もPJ別です。

## 起動

前提: JDK 21以上、Docker / Docker Compose v2。

```bash
bash Start.command
```

- CreVe: <http://localhost:8080/>
- PLAYPIT: <http://localhost:8081/playpit>
- NLJ: <http://localhost:8082/index.html>

停止は `bash Stop.command`、検証は `bash Verify.command` です。

## ローカル確認用

- CreVeログイン: `chaser@creve.local` / `creve-local-chaser`
- PLAYPIT管理: `admin@playpit.local` / `playpit-local-admin`

デモデータと固定ログインはdevだけで有効です。prodでは自動投入しません。

## NLJの境界

既存NLJのFlyway V001〜V015と実DDLは資料に含まれていません。設計書の指示に従い、推測migrationは作っていません。公開画面は確認できますが、投票はLINE設定と既存schema adapterの照合が終わるまで503で停止します。詳細は `project-overview/implementation-status.md` と `nlj-platform-persistence/docs/schema-mapping.md` を参照してください。
