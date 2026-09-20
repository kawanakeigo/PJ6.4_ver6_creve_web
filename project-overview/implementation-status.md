# 実装状況

更新日: 2026-09-20

## 確認した現物

- 作業開始commit: `9959347`
- Java: 25.0.1（各pomのreleaseはJava 21）
- Maven Wrapper: 3.9.16
- PostgreSQL: 17（`compose.yml`）
- 基本設計: v0.4
- 詳細設計: 本文 v0.2
- 操作とコードの動き説明書: v1.0。羽花停止前の説明は新詳細設計を優先した

## P0構成

| PJ | Boot | port | DB | 状態 |
|---|---:|---:|---|---|
| CreVe | 4.1.1 | 8080 | creve | 公式画面、公式原稿、事業リンク、承認snapshot公開、ログイン、studio feature gateを実装 |
| PLAYPIT | 3.5.16 | 8081 | playpit | 公開閲覧、参加・出展、感想、冪等受付、制限、管理、通報、計測を実装。羽花を削除 |
| NLJ | 3.5.16 | 8082 | nlj | 公開画面、LINE検証、投票TX契約を実装。実DDL adapterは現物待ち |

3PJは別プロセス、別DataSource、別Cookieです。他PJのJava・Entityをimportしません。

## 設計との差分・未完

- CreVeの全CA-01〜CA-75は未完。今回のローカル表示では公式公開、承認snapshot、認証入口、制作状態の境界を優先した。
- CreVeのメール確認、素材本体保存、全studio CRUD、Chaser全管理、保存・フォロー・成果は後続。
- PLAYPITのファイルアップロード本体とRange配信は後続。外部URLのサーバー取得は行わない。
- PLAYPIT通報の同一主体・同一対象10分冪等制御は実装済み。解決メモと完全な監査は後続。
- NLJ V001〜V015、14表の実列、EXP v2の実ルール、既存URL・payloadは未提供。推測migrationを作らず、投票を503で停止している。
- 本物のLINE、メール、素材検査、負荷、本番TLSは未検証。

未完項目を成功表示で代替しない。prodではデモデータを無効にする。

## 2026-09-20 ローカル検証

- `./mvnw -B clean test`: 36件成功（CreVe 3、PLAYPIT 30、NLJ 3）
- PostgreSQL 17へ3PJを別DB・別接続で起動
- 主要公開URLは200、旧PLAYPIT羽花URLはイベント詳細へ308
- PLAYPIT感想APIは受付キー欠落を400 `VALIDATION_ERROR`で返却
- NLJのLINE検証は接続2秒・読取5秒、投票時刻は固定可能な`Clock`を使用
