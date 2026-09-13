# 構成変更と設計の対応

## 今回、ユーザーの指示で変更したこと

従来の単一 `src/main/java/world/creve/{controller,service,...}` と一つのresourcesに置かれていた製品を、6つのMavenモジュールへ分離した。親pomはパッケージング`pom`であり、それ自体はWeb製品ではない。

依存方向は `app → 各PJ / platform`、`各PJ → platform` のみ。CreVeからPLAYPITのController・Serviceを直接呼ばない。Spring起動時に `ProjectContribution` 実装一覧を受け取り、プロジェクト名・URL・開催イベント情報を取得する。共通Event・Creator・Artworkは人物や作品がイベントをまたいで共通であるという元設計に従い、platformに置く。感想・羽花の処理はPLAYPIT側が所有する。

## 配置の対応

| 原設計の対象 | 新しい配置 | URLの扱い |
|---|---|---|
| CreveController・トップ・About・News | projects/creve。Javaはworld.creve.portal | `/`、`/about`、`/news`を維持 |
| PlaypitController・CreatorController・ArtworkController | projects/playpit | `/playpit/{eventSlug}/...`を維持 |
| MessageController・MessageService・MessageRepository・Message | projects/playpit | `POST /api/messages`の設計契約へ修正 |
| UkaController・UkaService・UkaResponse・PetalResponse | projects/playpit | `/playpit/{eventSlug}/uka` |
| AdminControllerの投稿管理部分 | projects/playpit | `/admin/messages` |
| 管理者ログイン・ダッシュボード | platformのAdminAuthController | `/admin/login`、`/admin/dashboard` |
| 共通カタログのEntity/Repository/Service | platform | 製品の画面から利用 |
| NljController・NljService・画面 | projects/nlj | `/NLJ`、`/NLJ/{eventSlug}`（旧`/live`は転送） |
| CreatorController・感想・羽花 | projects/playpit | `/creators`、`/creators/{creatorSlug}`、`/playpit/**` |
| LandingPageController・LPのデータ・画面 | projects/lp | `/lp/{lpSlug}` |
| 認証・共通例外・Clock・共通ヘッダー・フッター | platform | 全製品で共有 |
| CreveApplication・起動設定・管理者初期作成 | app | 起動を集約 |

クラスの単純名・主要メソッド名は元の詳細設計に合わせ、パッケージだけを所有モジュールへ移した。ただし元設計にない画面データの組立て・バリデーション・連携部品などは追加した。全クラスが旧完全修飾名と完全一致するという意味ではない。

Thymeleafはclasspathから画面を読むため、異なるモジュールに同じ `templates/index.html` を置く構造にはしなかった。`templates/creve/index.html`、`templates/playpit/index.html` のように製品名を付ける。ブラウザ上のURLを変更する必要はない。CSS・JS・画像も `/creve/`、`/playpit/`、`/nlj/`、`/lp/`、`/platform/` の名前空間に分ける。共通管理画面の一部は共通の `templates/admin/` 名前空間を使用するが、投稿管理の実ファイルはPLAYPITモジュールにある。公開URLと実装ファイルの対応は `SITE_STRUCTURE.md` に記載する。

## データの扱い

元設計にある主要4テーブルの列名・型・長さ・NULL条件をFlyway SQLとEntityへ記載した。IDはLong/BIGSERIAL、URL識別子は別のslug。JPAによる勝手なDDL更新はしない。Flywayで作成した後、Hibernateの`validate`でEntityとの対応を確認する構成。実際の検証完了は `Verify.command` の実行結果で判断する。

`V001`：共通カタログ・管理者・ログイン制限。
`V002`：PLAYPIT感想・一時的投稿制限・通報・アクセス解析。
`V003`：CreVeのお知らせ。
`V004`：LP。
`V005`：既存のイベント種別`LIVE`を`NLJ`へ移行。

## 新規PJ

新規PJには `ProjectContribution` を実装する。追加スクリプトはJavaBean名もPJ固有にし、複数の新PJを追加した場合にSpringのBean名が重複しないようにしている。既存PJの改変なしで追加できることと、個別のWebサーバーとして独立運用できることは別である。

## 依拠した一次資料

Apache Maven, Guide to Working with Multiple Modules:
https://maven.apache.org/guides/mini/guide-multiple-modules.html

元設計のSpring Boot+Thymeleaf構成は `source/PLAYPIT_design_source.txt`「基本設計書 使用技術変更」6〜9章、15章。今回の構成変更はユーザーの追加指示によるもので、元設計本文を書き換えていない。

画面の反復と断片の取込みは、th:eachを外側、th:replaceを内側に分けて評価順を明確にした。共通メニューはPLAYPIT→NLJ→追加PJの順。イベント一覧は各ページ内で開催区分に分類し、各区分内の日時降順を維持する。

Thymeleaf一次資料（Attribute Precedence）:
https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html
