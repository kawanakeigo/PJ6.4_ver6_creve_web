# PLAYPIT運用

- port: 8081
- DB: playpit
- Cookie: PLAYPIT_SESSION
- 感想: 60秒最大3件、同一本文10分、Idempotency-Key UUID
- イベント単位の `accepting_messages` と全体スイッチの両方がtrueの場合だけ受付
- devだけデモイベントと管理者を投入
- prodはデモ投入・管理者自動作成を無効化
