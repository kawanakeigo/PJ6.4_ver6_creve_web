# PJ境界

```text
browser
  ├─ http://localhost:8080 -> creve -> DB creve
  ├─ http://localhost:8081 -> playpit -> DB playpit
  └─ http://localhost:8082 -> nlj-platform-persistence -> DB nlj
```

- PJ間の遷移はURLリンクだけを使う。
- 各アプリは自分のDataSourceだけを参照する。
- 公開URL、Cookie、管理認証、Flyway履歴を共有しない。
- ルートpomは集約ビルド用で、業務依存を作らない。
- CreVeは `jp.creve`、PLAYPITは `world.creve`、NLJは `com.newlegacyjam.platform` のみを走査する。

## 公開URL

- CreVe: `/`, `/about`, `/news`, `/discover`, `/r/{slug}`, `/contact`, `/privacy`, `/terms`, `/vr`
- PLAYPIT: `/playpit`, `/playpit/events`, `/playpit/{eventSlug}`, `/playpit/{eventSlug}/creators/{creatorId}`, `/playpit/{eventSlug}/artworks/{artworkId}`
- NLJ: `/index.html`, `/event.html`, `/vote.html`, `/me.html`, `/ranking.html`

旧PLAYPIT `/uka` は羽花を表示せず、イベント詳細へ308転送する。
