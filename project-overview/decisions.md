# P0判断記録

更新日: 2026-09-20

## 採用した判断

1. CreVe、PLAYPIT、NLJは別Mavenモジュール、別Spring Bootプロセス、別DataSource、別Cookieで起動する。
2. ローカルの公開入口はCreVe `8080`、PLAYPIT `8081`、NLJ `8082`とする。PJ間の移動は設定可能な公開URLリンクだけを使う。
3. PostgreSQL 17を一つ起動し、`creve`、`playpit`、`nlj`のDBと接続ユーザーを分離する。
4. PLAYPITの羽花、花びら、LIGHT/FLOWERはP0対象外とし、データ、API、画面、JavaScriptから除外する。旧URLはイベント詳細へ308転送する。
5. CreVeの公開部屋は、編集テーブルの最新版ではなく、承認済みpublicationの固定snapshotだけから描画する。
6. NLJの投票処理はイベントロック、時刻確定、Cycle再確認、票、利用者EXP、Artist EXPを一つのトランザクション境界に置く。
7. NLJの既存V001-V015と14表の実DDLが未提供のため、推測migrationを作らない。実adapter未接続時は503とし、投票成功を偽装しない。
8. 開発用fixtureは`dev`だけで投入し、`prod`では無効にする。

## 本番判断へ格上げしていない値

- localhostのport、DB名、DBパスワード、デモアカウント
- NLJのEXP付与量、ランキング公開、Artist EXP公開
- CreVe studio機能の本番公開可否
- 既存QR、旧live、LP、本番ドメインの移行方法

これらは環境変数または後続の移行判断で確定する。
