# 既存schema mapping

状態: **現物待ち**

| port | 実表・列 | 型・制約 | 状態 |
|---|---|---|---|
| users | 未取得 | lineSub一意、有効状態が必要 | 未照合 |
| artists | 未取得 | artist ID、名称、紹介が必要 | 未照合 |
| events / event_artists | 未取得 | 所属・表示順・公開状態が必要 | 未照合 |
| voting_periods / voting_cycles | 未取得 | event、UTC開始終了、activeが必要 | 未照合 |
| votes | 未取得 | UNIQUE(user_id,voting_cycle_id)が必要 | 未照合 |
| exp_rules | 未取得 | rule ID/version、両unitsが必要 | 未照合 |
| user_exp_transactions | 未取得 | user＋付与根拠の一意性が必要 | 未照合 |
| artist_exp_transactions | 未取得 | artist＋付与根拠の一意性が必要 | 未照合 |
| ranking_snapshots | 未取得 | scope・集計時点・行が必要 | 未照合 |
| analytics_events | 未取得 | 機密を含まない操作記録が必要 | 未照合 |
| admin_users / audit_logs | 未取得 | 管理認可・操作履歴が必要 | 未照合 |

V001〜V015は不変とし、現物取得後に実列とのadapterと必要なV016以降だけを追加する。
