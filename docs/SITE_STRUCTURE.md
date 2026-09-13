# サイト構成と実装配置

公開URLの正規構成は次のとおり。URLに使う `{eventId}`、`{creatorId}`、`{artworkId}`、`{lpId}` は、DBの数値IDではなく公開用の一意なslugを表す。

```text
creve.world
├── /                              CreVe公式トップ
├── /about                         CreVeについて
├── /news                          お知らせ
├── /creators                      クリエイター一覧
│   └── /{creatorId}               プロフィール、作品、参加イベント、感想投稿、個人の羽花
├── /playpit                       PLAYPITトップ・コンセプト
│   ├── /events                    PLAYPITイベント一覧
│   ├── /archive                   過去のPLAYPIT・羽花
│   └── /{eventId}                 イベント詳細、参加クリエイター、展示作品
│       ├── /creators
│       │   └── /{creatorId}       イベント内プロフィール、作品、SNS、感想投稿
│       ├── /artworks
│       │   └── /{artworkId}       作品詳細、制作者、感想投稿、投稿済み感想
│       └── /uka                    イベント全体の羽花
├── /NLJ                           ライブイベント一覧
│   └── /{eventId}                 ライブイベント詳細
├── /lp/{lpId}                     集客・募集用LP
├── /vr                            VR構想
├── /contact
├── /privacy
└── /terms
```

正規URLは詳細構成に合わせて `/playpit`（小文字）と `/NLJ`（大文字）。資料内の旧表記 `/PLAYPIT/**` は `/playpit/**` へ、旧実装 `/live/**` は `/NLJ/**` へ308リダイレクトする。

ソースは事業境界に合わせ、CreVe公式画面を `projects/creve`、PLAYPIT・感想・羽花・クリエイター公開画面を `projects/playpit`、NLJを `projects/nlj`、LPを `projects/lp` に置く。認証とEvent・Creator・Artworkの共通データは `platform` が所有する。トップレベルの `/creators` がPLAYPITモジュール内にあるのは、同ページがPLAYPITイベント単位の感想投稿と羽花を含むためである。

複数のPLAYPITへ参加したクリエイターは、`/creators/{creatorId}?event={eventId}` で感想と羽花の対象イベントを切り替える。指定がなければ開始日時が新しい公開中の参加イベントを使用する。作品と参加履歴は全公開イベントを横断して表示する。
