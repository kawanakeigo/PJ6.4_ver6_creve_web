# PLAYPIT / CreVe 設計適合監査

**監査日：2026年9月14日（日本時間）｜対象：提出ZIPのスナップショット｜ソース修正：なし**

## 結論

**現時点で「設計どおりに完成している」とは判定しない。** Java・Spring Boot・Thymeleafの基礎、公開画面、感想保存経路、匿名表示、投稿管理の一部は存在する。しかし、投稿APIとDBの契約が設計から変更され、投稿制限値も異なり、作品投稿後の羽花体験やコンテンツ管理の更新機能が不足している。

本報告の35件は**不適合・未実装に加えて、環境依存の注意点・静的リスクを含む監査指摘の単位**であり、35個の独立した不具合を実動作で再現したという意味ではない。章・テストの件数から完成率を計算していない。

参照順： [主要指摘](#findings) → [初期対象19項目](#scope) → [内部契約](#contracts) → [DB全列比較](#database) → [全81章の確認表](#coverage) → [実施した検証](#tests)。

## 1. 対象・方法・証拠の範囲

| 項目 | 確認内容 |
| --- | --- |
| 提出物 | PLAYPIT_review_20260914_024701.zip |
| 基準資料 | この会話で確認したPLAYPIT_design_source.txt全文（1,979行）。要件17章・基本11章・技術15章・詳細38章。 |
| ファイル棚卸し | 177ファイル。製品Java55、テストJava1、HTML28、CSS8、JavaScript6、SVG63。 |
| 静的確認 | Java、テンプレート、JavaScript、CSS、SQL、設定、依存関係、テストを照合。ファイルの存在だけでなく呼出経路・項目・条件を確認。 |
| 変更の有無 | 提出ZIPから展開したoriginal配下はSHA-256比較で全177ファイル一致。ビルドは別コピー、プローブは別ディレクトリ。 |
| 全体ビルド | Maven Wrapperの取得先repo.maven.apache.orgを名前解決できず中止。アプリのコンパイルエラーを検出した結果ではない。 |
| 個別ブラウザ検証 | 元のHTML/JS/CSSを隔離Chromiumで検証。Thymeleaf・Spring・API・DBは起動せず、API応答とModel由来のdata属性を監査用に補った。 |
| 個別Java検証 | 元の9 Javaファイルのメソッド本体をjavacで検証。依存注釈・型だけを最小スタブで補った。Spring/JPA/Bean Validationのランタイム検証ではない。 |
| 未確認 | 実PostgreSQL、起動時JPA生成/検索、実HTTPステータス、認証/CSRFの実通信、負荷、実環境の設定、外部CDN・監視、Safari/各端末の実機表示、公開内容の承認、Git履歴。 |



判定の使い分け：**適合**は明記した範囲だけの一致、**不適合**は実装があるが設計と異なる、**未実装**は要求に対応する処理が見つからない、**確認不能／未検証**は必要な実行・環境・証跡がない、**文書差異**は設計書内の記載違いを勝手に統一していないことを表す。

ファイル参照の短縮表記：`java/` は `src/main/java/world/creve/`、`resources/` は `src/main/resources/`。行番号は提出ZIPから展開した原本の1始まり。設計書の行番号は抽出本文ファイルの1始まり。

<a id="findings"></a>
## 2. 主要指摘一覧

| ID | 重要度 | 指摘 | 判定 | 設計参照 |
| --- | --- | --- | --- | --- |
| [A01](#A01) | 高 | 標準DBが永続PostgreSQLではなくメモリ内H2 | 標準設定は不適合／実環境は確認不能 | 詳細3.1・31、技術10・15、要件15 |
| [A02](#A02) | 高 | 投稿APIの項目名・ID型・応答形式が設計と異なる | 不適合 | 詳細16・17・18・25 |
| [A03](#A03) | 高 | 主要テーブルの列名・型・長さ・NULL条件が設計と異なる | 不適合 | 詳細22・31、基本9 |
| [A04](#A04) | 高 | 外部キー・投稿対象・花びら種別のDB制約がない | 未実装 | 詳細22.3・31.3 |
| [A05](#A05) | 高 | 投稿制限が3回／10分／429ではなく5回／1分／400 | 不適合 | 詳細18・21・29 |
| [A06](#A06) | 高 | 画面の対象なしを404にする共通処理が揃っていない | 不適合／HTTP実測は未検証 | 詳細9・11・13・18・29 |
| [A07](#A07) | 中 | 送信中の再実行ガードがなく、DB二重登録防止も未実証 | 一部不適合／同時DB登録は確認不能 | 詳細15.4、要件11.4 |
| [A08](#A08) | 高 | 作品投稿後の花びら追加・移動演出がない | 未実装／一部再現済み | 要件4.4・15、基本8、詳細15・26・38 |
| [A09](#A09) | 中 | 作品投稿後の件数が増えず、作品詳細から羽花へのリンクもない | 不適合／件数は再現済み | 基本4.2、詳細13.3・15 |
| [A10](#A10) | 高 | 花びらの配置は楕円状計算で、完全重複を避ける処理がない | 不適合／衝突の反例を確認 | 要件5、詳細25.2 |
| [A11](#A11) | 高 | 300件制限が実際の羽花HTML経路で使われていない | 不適合 | 詳細23・34 |
| [A12](#A12) | 高 | 成長段階の計算は正しいが画面の成長制御に接続されていない | 数値判定は適合／画面連携は不適合 | 詳細23・24・25・26 |
| [A13](#A13) | 中 | 花びらから表示する情報が本文・表示名だけ | 不適合 | 要件5.4、詳細25.1 |
| [A14](#A14) | 中 | 羽花の文字一覧・人物別集計が揃っていない | 一部未実装 | 要件6・11.5、基本5 |
| [A15](#A15) | 高 | イベント・クリエイター・作品管理は閲覧一覧のみ | 管理の更新機能が未実装 | 詳細2、基本7・10 F-017〜F-019 |
| [A16](#A16) | 高 | 管理者ログイン失敗時の一時制限がない | 未実装 | 詳細27.3 |
| [A17](#A17) | 高 | 既定管理者の自動登録が環境を限定していない | 運用上の要確認事項 | 詳細27・33、技術11（補足リスク） |
| [A18](#A18) | 中 | 管理者ログイン成功先が設計と異なる | 不適合 | 詳細27.1 |
| [A19](#A19) | 中 | 投稿管理の投稿日検索・画面名・責務が異なる | 一部不適合 | 詳細28 |
| [A20](#A20) | 中 | 会場・作品別QRコードの生成・表示・保存項目がない | 未実装 | 要件14、基本4.1・9・10 F-007 |
| [A21](#A21) | 中 | アクセス解析の計測処理がない | 未実装／外部設定は確認不能 | 要件13 |
| [A22](#A22) | 高 | 同梱テストはコンテキスト起動1件だけ | 業務テストが未実装／実行は環境で阻止 | 詳細35、技術13 |
| [A23](#A23) | 中 | 公開一覧・詳細画面の表示項目が不足 | 一部不適合 | 詳細8・9・10・12・13 |
| [A24](#A24) | 中 | 複数作品メディアの保存・表示・切替が揃わない | 一部未実装 | 基本4.2・9、詳細13、技術4.3 |
| [A25](#A25) | 中 | CreVeトップの取得条件・上限・Modelが異なる | 不適合 | 詳細7 |
| [A26](#A26) | 中 | 開催区分の分類がなく、archiveも公開全件を取得 | 分類は不適合／archive対象範囲は文書差異 | 詳細8.2、基本1・5、要件15 |
| [A27](#A27) | 中 | モバイルメニューの背景スクロール停止・フッター項目が不足 | 一部不適合 | 詳細6 |
| [A28](#A28) | 中 | クラスは存在するがメソッド・DTO・責務の契約が異なる | 構造不適合 | 詳細4・7〜13・18・19・23〜25・28・32 |
| [A29](#A29) | 中 | HTML要素とJavaScriptの指定関数が一致しない | 構造不適合／一部機能は実装 | 詳細6.1・14・15・26 |
| [A30](#A30) | 中 | 指定された業務ログの出力処理がない | 未実装 | 詳細30 |
| [A31](#A31) | 中 | ライブ詳細とLPの目的に必要な項目・導線が不足 | 一部未実装 | 基本7 LIV-002/LP-001、詳細2、技術4 |
| [A32](#A32) | 中 | お問い合わせ・規約・プライバシーはプレースホルダー | 内容は未完成／確定本文は確認不能 | 要件8、基本1、詳細6・14 |
| [A33](#A33) | 中 | 公開感想GET APIで親コンテンツの公開確認がない | 追加の静的リスク／実HTTPは未検証 | 詳細9・11・13・33（公開境界の確認） |
| [A34](#A34) | 中 | SQLファイルの定義が実DBへ反映される経路が確認できない | 標準起動経路で不使用／実DBは確認不能 | 詳細31・34、技術10 |
| [A35](#A35) | 中 | 通報された投稿を扱う仕組みがない | 未実装 | 要件7.1・7.2 |



重要度は監査上の影響評価であり、設計の優先順位を変更したものではない。実装変更・新機能追加は一切実施していない。

<a id="A01"></a>
### A01　標準DBが永続PostgreSQLではなくメモリ内H2

**判定：標準設定は不適合／実環境は確認不能｜重要度：高｜設計：詳細3.1・31、技術10・15、要件15**

**設計の記載：** PostgreSQLを使用し、イベント終了後も感想と羽花を残す。

**実装の事実：** application.propertiesの既定URLはjdbc:h2:mem:creve、既定ドライバはorg.h2.Driver。DB_CLOSE_DELAY=-1は同じJVMが生きている間の保持であり、永続保存ではない。PostgreSQLドライバと環境変数による接続先の上書き手段は存在する。

**影響：** この既定設定のまま運用すると、JVM終了を越えて投稿を保存する条件を満たさない。H2のPostgreSQL互換モードはPostgreSQLそのものではない。

**ソース根拠：** `resources/application.properties:6–15`。

**判定の限界・留意：** Mac上の環境変数・外部DB・本番設定はZIPに含まれないため、実際の接続先や既存データの消失を確認したわけではない。技術根拠：H2 Features / In-Memory Databases。

<a id="A02"></a>
### A02　投稿APIの項目名・ID型・応答形式が設計と異なる

**判定：不適合｜重要度：高｜設計：詳細16・17・18・25**

**設計の記載：** eventId/creatorId/artworkIdはLong。本文message、規約同意agreement。応答はmessageId、status、petalType、petalSeed、createdAt、completionMessage。

**実装の事実：** IDはString、本文はbody、規約同意はtermsAgreed。anonymousは必須Booleanではなく初期値trueのprimitive boolean。応答はid/body等を返す別構造で、messageIdとcompletionMessageがない。JavaScriptも変更後の契約に合わせて送信している。

**影響：** 同梱画面とAPI同士の整合性があっても、設計どおりのAPI利用者・テスト・将来の共通APIとは契約が一致しない。単なる表示ラベルの違いではない。

**ソース根拠：** `java/dto/request/MessageRequest.java:8–28`、`java/dto/response/MessageResponse.java:7–21`、`resources/static/js/message.js:73–85`。

**個別検証：** B01–B02、Java契約検査。

**判定の限界・留意：** 実APIでのJSON変換・HTTP応答は未実行。OffsetDateTimeはJSONでは文字列になる可能性があるため、createdAtだけを通信型違反とは断定しない。

<a id="A03"></a>
### A03　主要テーブルの列名・型・長さ・NULL条件が設計と異なる

**判定：不適合｜重要度：高｜設計：詳細22・31、基本9**

**設計の記載：** 設計のevents/creators/artworks/messagesの列、型、必須条件、登録・更新日時を実装する。

**実装の事実：** events等の主キーはevent_id等のBIGSERIALではなくid VARCHAR(64)。title→name、genre→category、message→bodyなど変更。artworks.production_yearはSMALLINTではなくVARCHAR(40)。主要3テーブルのcreated_at/updated_at、messages.updated_atなどがない。その他の長さ・NULL条件の差も付録に全列単位で記載した。

**影響：** 設計のDDL・Entity契約で作成されたDBとは一致しない。更新日時や会場情報、料金・チケットなどを設計の列で保存できない。

**ソース根拠：** `java/entity/Event.java:14–63`、`java/entity/Creator.java:13–55`、`java/entity/Artwork.java:13–55`、`java/entity/Message.java:16–64`、`resources/db/schema.sql:1–87`。

<a id="A04"></a>
### A04　外部キー・投稿対象・花びら種別のDB制約がない

**判定：未実装｜重要度：高｜設計：詳細22.3・31.3**

**設計の記載：** messagesのevent_id/creator_id/artwork_idをFKにし、少なくとも一つの投稿対象、petal_typeの1〜5を制約として持つ。作品の制作者もFK。

**実装の事実：** 関連IDを単純なString列として保持。DDLにFOREIGN KEY/REFERENCES/CHECKがなく、Entity側にも関連マッピング・該当制約がない。event_creators/event_artworksの組合せ一意制約は存在する。

**影響：** Serviceの入力確認はあるが、DB自身の参照整合性や値域保証は設計どおりではない。

**ソース根拠：** `resources/db/schema.sql:37–87`、`java/entity/EventCreator.java:12–28`、`java/entity/EventArtwork.java:12–28`。

**判定の限界・留意：** 実際の外部PostgreSQLに手動追加された制約の有無は確認不能。

<a id="A05"></a>
### A05　投稿制限が3回／10分／429ではなく5回／1分／400

**判定：不適合｜重要度：高｜設計：詳細18・21・29**

**設計の記載：** 60秒で最大3回。同一本文の判定期間は10分。制限超過はHTTP 429。

**実装の事実：** MessageServiceは1分前を基準に回数と重複を調べ、5回以上でBadRequestException。PostingRateLimiterも最大5回。例外ハンドラはBadRequestExceptionを400へ変換する。

**影響：** 設計では拒否する4回目・5回目が制限部品で許容される。1分より前〜10分以内の重複はこの検索期間に含まれない。API利用者は429を受け取れない実装経路になっている。

**ソース根拠：** `java/service/MessageService.java:91–105`、`java/security/PostingRateLimiter.java:13–28`、`java/exception/GlobalExceptionHandler.java:19–21`。

**個別検証：** J-RATE-attempt-1〜6：4回目・5回目を許容。

**判定の限界・留意：** 429ではなく400となる点とDB重複検索期間はソース判定。実HTTP・DBの時間経過テストは未実行。

<a id="A06"></a>
### A06　画面の対象なしを404にする共通処理が揃っていない

**判定：不適合／HTTP実測は未検証｜重要度：高｜設計：詳細9・11・13・18・29**

**設計の記載：** 存在しないイベント・人物・作品、所属不正を適切な404画面またはAPIレスポンスにする。共通例外処理を整える。

**実装の事実：** GlobalExceptionHandlerの適用対象はMessageControllerに限定。NotFoundExceptionは@ResponseStatusなしのRuntimeException。ページControllerから投げられる同例外を404画面へ変換する処理が見つからない。指定8例外は2つの汎用例外に置換されている。

**影響：** 投稿APIのNotFoundは404変換される一方、一般画面については設計の404を保証できず、未処理例外になる構成。

**ソース根拠：** `java/exception/GlobalExceptionHandler.java:11–28`、`java/exception/NotFoundException.java:1–7`、`java/service/CreatorService.java:45–57`。

**判定の限界・留意：** HTTP 500になる可能性はソースに基づく推論であり、この環境で画面HTTP応答を実測した結果ではない。

<a id="A07"></a>
### A07　送信中の再実行ガードがなく、DB二重登録防止も未実証

**判定：一部不適合／同時DB登録は確認不能｜重要度：中｜設計：詳細15.4、要件11.4**

**設計の記載：** submitMessage()の最初に二重送信中でないことを確認し、通信失敗時も同じ内容が二重登録されないようにする。

**実装の事実：** ボタン無効化はあるが送信処理中フラグの検査がない。通信未完了中にsubmitイベントを2回発火させるとfetchが2回呼ばれた。DB側は重複存在確認とsaveが別手順。

**影響：** ボタンの連打抑制と、送信関数自体の多重実行防止は同じではない。DB同時実行時の一意な登録保証も確認できていない。

**ソース根拠：** `resources/static/js/message.js:49–96`、`java/service/MessageService.java:97–136`。

**個別検証：** B19：送信要求2回。

**判定の限界・留意：** 再現は監査スクリプトによるsubmitイベント二重発火。通常の物理ダブルクリックで必ず二重登録されるという意味ではない。実DBの競合テストは未実行。

<a id="A08"></a>
### A08　作品投稿後の花びら追加・移動演出がない

**判定：未実装／一部再現済み｜重要度：高｜設計：要件4.4・15、基本8、詳細15・26・38**

**設計の記載：** 言葉が花びらになり、羽花に移動し、追加される演出を表示する。作品詳細からの投稿にも接続する。

**実装の事実：** 作品詳細にdata-uka-boardがなく、appendPetal()はboardがないとreturnする。人物ページでは静的な花びら要素は追加されるが、移動演出やshowPetalAnimation()はない。

**影響：** PLAYPITの中心体験である「言葉が花びらとなって加わる」が作品ページ経由では成立しない。人物ページでも要素追加だけで演出条件を満たさない。

**ソース根拠：** `resources/templates/playpit/artwork-detail.html:24–57`、`resources/static/js/message.js:101–107`、`resources/static/js/message.js:118–139`、`resources/static/css/uka.css:113–125`。

**個別検証：** B03：花びらなし、B08：人物は要素追加、B09：移動アニメーションなし。

**判定の限界・留意：** API成功応答を模したブラウザ検証。実DBへの投稿成功を確認したわけではない。

<a id="A09"></a>
### A09　作品投稿後の件数が増えず、作品詳細から羽花へのリンクもない

**判定：不適合／件数は再現済み｜重要度：中｜設計：基本4.2、詳細13.3・15**

**設計の記載：** 作品の投稿数を表示し、羽花へ移動する導線を設ける。

**実装の事実：** 作品の件数spanにdata-message-countがなく、message.jsのカウント更新はboardが存在する処理内だけ。作品詳細テンプレートに羽花へのリンクがない。

**影響：** 投稿後の本文一覧は増えても件数は0のまま。作品からイベント全体の羽花へ進む設計の導線も欠ける。

**ソース根拠：** `resources/templates/playpit/artwork-detail.html:48–57`、`resources/static/js/message.js:118–138`。

**個別検証：** B04：0のまま、B06：本文一覧への追加は成功。

**判定の限界・留意：** 件数再現は初期0件・モック成功応答。

<a id="A10"></a>
### A10　花びらの配置は楕円状計算で、完全重複を避ける処理がない

**判定：不適合／衝突の反例を確認｜重要度：高｜設計：要件5、詳細25.2**

**設計の記載：** 羽全体の形を優先し、成長段階ごとの領域を使い、花びら同士が完全に重ならないように調整する。

**実装の事実：** x/yは中心50にcos/sin×半径を加える楕円状の式。半径は投稿順index依存で上限42。既配置花びらとの衝突判定・再配置処理がない。異なるseed/indexでも座標・角度・サイズが完全一致する入力を確認した。

**影響：** 「羽を構成する配置」と「完全重複の調整」の仕様を満たしていない。色や好みのデザインを採点した指摘ではない。

**ソース根拠：** `java/util/PetalLayoutUtil.java:43–68`。

**個別検証：** Java衝突検査：x/y/角度/サイズが同一。

**判定の限界・留意：** 反例は関数単体：seed=10000,index=100,stage=4 と seed=5560,index=220,stage=4。実データがこのseedを取ったことや実画面の全投稿分布を確認したわけではない。

<a id="A11"></a>
### A11　300件制限が実際の羽花HTML経路で使われていない

**判定：不適合｜重要度：高｜設計：詳細23・34**

**設計の記載：** 羽花の花びら表示は初期300件まで。総投稿数は別に表示する。

**実装の事実：** UkaServiceのAPI用処理にはPageRequest.of(0,300)がある。しかしUkaControllerはUkaServiceを呼ばず、MessageService.findPublicByEvent()で全件取得し、uka.htmlで全件反復する。

**影響：** 300件制限のコードの存在だけでは適合にならない。通常の羽花ページの描画経路には上限がない。

**ソース根拠：** `java/service/UkaService.java:28–47`、`java/controller/UkaController.java:27–35`、`java/service/MessageService.java:144–148`、`resources/templates/playpit/uka.html:13–20`。

**判定の限界・留意：** 301件以上を実DBに登録してブラウザ計測したわけではなく、呼び出し経路の静的判定。

<a id="A12"></a>
### A12　成長段階の計算は正しいが画面の成長制御に接続されていない

**判定：数値判定は適合／画面連携は不適合｜重要度：高｜設計：詳細23・24・25・26**

**設計の記載：** UkaServiceのcalculateGrowthStage(long)とgetUkaData(eventId)で成長・表示データを作り、画面へ渡す。

**実装の事実：** 0/1〜10/11〜30/31〜60/61以上の数値判定はPetalLayoutUtilにあり境界値は一致。一方UkaControllerのModelにgrowthStageやUkaResponseがなく、uka.jsはモーダル開閉だけ。花びらの座標は投稿時に保存されたものを描画する。

**影響：** 成長関数のテスト成功をもって、表示段階に応じて羽が形成されることまで確認できない。指定クラス・メソッドの責務も異なる。

**ソース根拠：** `java/util/PetalLayoutUtil.java:23–36`、`java/controller/UkaController.java:27–35`、`resources/static/js/uka.js:1–22`。

**個別検証：** J-GROW-0/1/10/11/30/31/60/61：全8件一致。

<a id="A13"></a>
### A13　花びらから表示する情報が本文・表示名だけ

**判定：不適合｜重要度：中｜設計：要件5.4、詳細25.1**

**設計の記載：** 感想本文、表示名、投稿日、対象作品名などを表示できる。PetalResponseには作品名・クリエイター名も定義される。

**実装の事実：** モーダルへ渡すのは本文と表示名だけ。createdAtはレスポンスに存在しても表示していない。PetalResponseにartworkTitle/creatorNameがない。

**影響：** どの作品へのいつの言葉かを、設計どおり花びらから確認できない。

**ソース根拠：** `java/dto/response/PetalResponse.java:6–19`、`resources/static/js/uka.js:4–14`、`resources/static/js/message.js:4–10`、`resources/templates/playpit/creator-detail.html:75–78`。

**個別検証：** B11–B12は成功、B13の投稿日表示は失敗。

<a id="A14"></a>
### A14　羽花の文字一覧・人物別集計が揃っていない

**判定：一部未実装｜重要度：中｜設計：要件6・11.5、基本5**

**設計の記載：** 視覚的な花びらだけでなく言葉を一覧で確認でき、イベント全体では参加者ごとの投稿数・個人ページ導線を扱う。

**実装の事実：** 作品詳細には本文一覧がある。人物詳細とイベント羽花は花びら中心で、本文一覧の代替表示がない。イベント羽花のasideは総数と一覧リンクだけで、人物別の投稿数を描画しない。

**影響：** 羽花を見て言葉を一覧で読む手段と、イベント全体から人物別の投稿状況を把握する機能が不足。

**ソース根拠：** `resources/templates/playpit/creator-detail.html:64–78`、`resources/templates/playpit/uka.html:13–32`。

<a id="A15"></a>
### A15　イベント・クリエイター・作品管理は閲覧一覧のみ

**判定：管理の更新機能が未実装｜重要度：高｜設計：詳細2、基本7・10 F-017〜F-019**

**設計の記載：** 初期範囲のイベント管理・クリエイター管理・作品管理を実装する。

**実装の事実：** AdminControllerの該当3経路はGET一覧だけ。AdminServiceもfindAllを返すのみ。テンプレートは一覧表で、情報登録・編集・更新用フォームや処理がない。

**影響：** 運営者が管理画面からイベントや作品情報を更新することができない。投稿の公開・非公開操作があることとコンテンツ管理完了は別。

**ソース根拠：** `java/controller/AdminController.java:87–105`、`java/service/AdminService.java:95–104`。

**判定の限界・留意：** この3管理機能の全入力項目や操作URLは設計書の詳細が十分ではない。ただし読取一覧しか存在しない事実は確定しており、勝手な入力仕様を追加して採点してはいない。

<a id="A16"></a>
### A16　管理者ログイン失敗時の一時制限がない

**判定：未実装｜重要度：高｜設計：詳細27.3**

**設計の記載：** 一定回数ログインに失敗した場合は一時制限する。

**実装の事実：** Spring Securityのフォーム認証・失敗表示はあるが、失敗回数保存、一時ロック判定、対応ハンドラ等がない。投稿用レート制限はログインには使われない。

**影響：** 管理者認証について設計で明記された防御が欠けている。

**ソース根拠：** `java/security/SecurityConfig.java:18–44`。

**判定の限界・留意：** ログインへの実HTTP試行は未実行。外部プロキシ等による制限は確認不能。

<a id="A17"></a>
### A17　既定管理者の自動登録が環境を限定していない

**判定：運用上の要確認事項｜重要度：高｜設計：詳細27・33、技術11（補足リスク）**

**設計の記載：** 管理者以外の操作を防止する。認証情報の実運用設定を確認する。

**実装の事実：** 既定の管理者パスワードが設定ファイルに用意され、AdminSeederはプロファイル制限なく管理者不存在時に登録する。値の存在を確認したが本報告にはパスワードそのものを転載しない。

**影響：** 環境変数を上書きせず外部公開すると既定認証情報が有効になり得る。ソースに「開発用」があるだけでは本番で動かない保証にならない。

**ソース根拠：** `resources/application.properties:17–19`、`java/config/AdminSeeder.java:1–28`、`java/service/AdminService.java:45–55`。

**判定の限界・留意：** 既定資格情報が現在のMacや公開環境で有効とは断定しない。設計に新たな認証方式を追加する提案ではなく、運用条件の確認事項として別扱い。

<a id="A18"></a>
### A18　管理者ログイン成功先が設計と異なる

**判定：不適合｜重要度：中｜設計：詳細27.1**

**設計の記載：** 成功後は/admin/dashboard。

**実装の事実：** defaultSuccessUrlは/admin/messages。/admin/dashboardのController・画面はない。

**影響：** ログイン後の遷移契約が一致しない。

**ソース根拠：** `java/security/SecurityConfig.java:30–36`。

<a id="A19"></a>
### A19　投稿管理の投稿日検索・画面名・責務が異なる

**判定：一部不適合｜重要度：中｜設計：詳細28**

**設計の記載：** イベント、人物、作品、公開状態、投稿日、キーワードで検索。admin/message-list.html、showMessageList()、MessageServiceの指定メソッド。

**実装の事実：** イベント/人物/作品/状態/キーワード検索と公開/非公開/論理削除は存在。投稿日条件はない。admin/messages.html、messages()、AdminService.searchMessages()/changeStatus()/deleteMessage()で実装している。

**影響：** 基本的なモデレーションは動作を想定した実装があるが、検索項目と内部の設計契約が揃わない。

**ソース根拠：** `java/controller/AdminController.java:32–84`、`java/service/AdminService.java:57–93`、`resources/templates/admin/messages.html:1–65`。

**判定の限界・留意：** 実DBで検索結果・状態変更の反映までは未確認。

<a id="A20"></a>
### A20　会場・作品別QRコードの生成・表示・保存項目がない

**判定：未実装｜重要度：中｜設計：要件14、基本4.1・9・10 F-007**

**設計の記載：** 作品別QRコードからアクセスできるようにし、QRコード表示・qr_code_urlを管理する。

**実装の事実：** イベント配下のURL自体はあるが、QR生成/表示処理、QR画像、qr_code_url列が見つからない。

**影響：** URLを手作業でQR化する運用は考えられるが、その作業を本実装が完了したとは判定できない。

**ソース根拠：** `java/entity/EventArtwork.java:12–28`、`resources/db/schema.sql:62–68`。

**判定の限界・留意：** 会場に既に外部作成のQRがあるかはZIPから確認不能。コード内ではQRCode/qrcode/qr_code_urlの一致なし。

<a id="A21"></a>
### A21　アクセス解析の計測処理がない

**判定：未実装／外部設定は確認不能｜重要度：中｜設計：要件13**

**設計の記載：** 個人ページ閲覧、フォーム表示、投稿数・完了率、人物/イベント投稿数、花びら押下、SNS遷移、QR流入を計測。本文は送らない。

**実装の事実：** アプリ内に分析イベント送信や計測属性、gtag/dataLayer等の実装が見つからない。単なるDB件数集計は利用行動全体の計測ではない。

**影響：** 指定の行動指標が設計どおり収集されることを確認できない。

**ソース根拠：** `resources/templates/common/head.html:1–15`、`resources/static/js/common.js:1–8`、`resources/static/js/message.js:1–151`。

**判定の限界・留意：** CDN等から注入する計測は未確認。要件のすべてをGoogle Analytics採用必須に読み替えてはいない。

<a id="A22"></a>
### A22　同梱テストはコンテキスト起動1件だけ

**判定：業務テストが未実装／実行は環境で阻止｜重要度：高｜設計：詳細35、技術13**

**設計の記載：** 投稿の正常/異常・境界値、所属、制限、表示、再送、羽花の境界、モーダル、非公開除外などをテストする。

**実装の事実：** src/testのJavaはCreveWebApplicationTests.javaの1ファイルで、@TestはcontextLoads()だけ。指定業務の検証コード・アサーションはない。

**影響：** 起動テストが成功したとしても、今回見つかった差異を担保するテストにならない。今回追加した監査プローブはプロジェクト同梱JUnitの代わりではない。

**ソース根拠：** `src/test/java/world/creve/CreveWebApplicationTests.java:1–13`。

**判定の限界・留意：** Mavenの配布先をこの実行環境で名前解決できず、同梱の1テスト自体も未実行。コンパイル不良とは断定しない。

<a id="A23"></a>
### A23　公開一覧・詳細画面の表示項目が不足

**判定：一部不適合｜重要度：中｜設計：詳細8・9・10・12・13**

**設計の記載：** イベント一覧は画像/会場/説明/開催状態等、人物一覧は紹介と出展数等、作品一覧は制作者情報、詳細は設計のModel・表示項目を扱う。

**実装の事実：** event-listは主に日時/名称/副題で、画像/会場/開催区分がない。creator-listは紹介文・出展数がない。artwork-listで制作者情報を表示せず、一覧取得も作品だけ。イベント詳細では取得したartworksを直接一覧表示せずリンク中心。

**影響：** ページファイルと経路が存在しても、掲載項目まで設計どおりとは言えない。

**ソース根拠：** `resources/templates/playpit/event-list.html:1–23`、`resources/templates/playpit/creator-list.html:1–29`、`resources/templates/playpit/artwork-list.html:1–23`、`java/controller/PlaypitController.java:53–61`。

<a id="A24"></a>
### A24　複数作品メディアの保存・表示・切替が揃わない

**判定：一部未実装｜重要度：中｜設計：基本4.2・9、詳細13、技術4.3**

**設計の記載：** 作品の複数画像、動画または音声、詳細のサブ画像、必要な切替を扱う。

**実装の事実：** Artworkにはsub_image_urlがあるが作品詳細で表示していない。作品詳細に動画・音声表示がなく、artwork.jsはコメントのみ。MediaFileのEntity/Repositoryがあっても画面利用へ接続していない。

**影響：** 作品の深い理解につながるメディア掲載要件が満たされていない。

**ソース根拠：** `java/entity/Artwork.java:13–55`、`resources/templates/playpit/artwork-detail.html:1–23`、`resources/static/js/artwork.js:1–1`。

<a id="A25"></a>
### A25　CreVeトップの取得条件・上限・Modelが異なる

**判定：不適合｜重要度：中｜設計：詳細7**

**設計の記載：** 公開中/開催予定を対象にPLAYPIT・ライブ各最大3件と最新お知らせを取得し、指定のService・Response・Model項目で渡す。

**実装の事実：** home()は全公開PLAYPIT/ライブを取得するメソッドを呼ぶ。開催予定用メソッドは別に存在するがここでは未使用。ModelはEntityのリストとnewsで、指定ResponseとnewsListではない。

**影響：** トップ用取得処理の条件・件数上限・内部契約が一致しない。

**ソース根拠：** `java/controller/CreveController.java:16–22`、`java/service/EventService.java:69–103`。

**判定の限界・留意：** HTMLが先頭1件だけを使うこと自体は「最大3件」違反の根拠にしない。問題は取得条件・上限制御・指定契約。

<a id="A26"></a>
### A26　開催区分の分類がなく、archiveも公開全件を取得

**判定：分類は不適合／archive対象範囲は文書差異｜重要度：中｜設計：詳細8.2、基本1・5、要件15**

**設計の記載：** イベント一覧を開催予定・開催中・終了済みに分類する。アーカイブについては各文書の時期差を保持する。

**実装の事実：** 開催日時降順の取得はあるが区分判定・区分表示がない。archive()も通常一覧と同じ公開全件取得を使う。

**影響：** 予定/開催中/終了済みを設計どおり区別できない。アーカイブのリリース時期は勝手に決めず別途文書差異として記録した。

**ソース根拠：** `java/controller/PlaypitController.java:39–50`、`resources/templates/playpit/event-list.html:1–23`。

<a id="A27"></a>
### A27　モバイルメニューの背景スクロール停止・フッター項目が不足

**判定：一部不適合｜重要度：中｜設計：詳細6**

**設計の記載：** メニュー開閉時に背景スクロールを停止。フッターにSNSリンクと著作権表示等。

**実装の事実：** common.jsはクラスの付け替えのみ。メニュー表示中も背景がスクロールした。footer.htmlにSNS・著作権表示がない。

**影響：** 共通画面の操作・掲載項目が設計と一致しない。

**ソース根拠：** `resources/static/js/common.js:1–8`、`resources/templates/common/footer.html:1–13`。

**個別検証：** B20：開閉は成功、B21：スクロール停止は失敗。

<a id="A28"></a>
### A28　クラスは存在するがメソッド・DTO・責務の契約が異なる

**判定：構造不適合｜重要度：中｜設計：詳細4・7〜13・18・19・23〜25・28・32**

**設計の記載：** 指定のクラス、メソッド名、引数/戻り値、画面用Response、呼び出し分担を実装する。

**実装の事実：** 指定の主要Controller/Service/Repository/Entityは存在。一方showTopPage()/showEventList()/showCreatorDetail()/showUka()/createMessage()等が別名。MessageServiceの引数にはHttpServletRequestが追加され、画面は指定のSummary/DetailResponseではなくEntityを渡す。UkaService.getUkaData()はない。

**影響：** 同じように見える画面があっても詳細設計の内部構造まで一致したとは言えない。設計からの変更承認はこのZIPでは確認できない。

**ソース根拠：** `java/controller/MessageController.java:33–40`、`java/service/MessageService.java:66–88`、`java/controller/UkaController.java:27–35`、`java/service/UkaService.java:28–51`。

<a id="A29"></a>
### A29　HTML要素とJavaScriptの指定関数が一致しない

**判定：構造不適合／一部機能は実装｜重要度：中｜設計：詳細6.1・14・15・26**

**設計の記載：** 指定id/name/hiddenと、message.jsの10関数、uka.jsの7関数、common.jsの3関数を持ち、指定の処理順で動く。

**実装の事実：** フォームはdata属性＋body/termsAgreedの構成。messageText/messageError/messageCount等の指定id、ID用hiddenはなし。20指定関数名は定義されず、イベントハンドラ・別名の関数にまとめられている。

**影響：** 機能が一部代替実装されている点と、設計のDOM/関数契約を満たさない点を分ける必要がある。

**ソース根拠：** `resources/templates/playpit/artwork-detail.html:26–46`、`resources/templates/playpit/creator-detail.html:42–62`、`resources/static/js/message.js:1–151`、`resources/static/js/uka.js:1–22`。

**判定の限界・留意：** 規約同意の画面文言は「投稿内容を確認しました」であり、設計の規約同意とは説明が異なる。法的効力までは評価していない。

<a id="A30"></a>
### A30　指定された業務ログの出力処理がない

**判定：未実装｜重要度：中｜設計：詳細30**

**設計の記載：** ログイン成功/失敗、投稿成功/失敗、状態変更、データ/API/DBエラーを記録し、機密や本文を含めない。

**実装の事実：** アプリ独自のLogger、ログ出力、対応イベントハンドラが見つからない。Springの標準起動・障害ログはあり得るが、業務イベントの記録実装ではない。

**影響：** 運営時に投稿・管理操作の成否を設計どおり追跡できることを確認できない。

**ソース根拠：** `java/service/MessageService.java:66–136`、`java/service/AdminService.java:57–93`。

**判定の限界・留意：** ログに秘密が漏れることを実測した指摘ではない。実運用ログ基盤は未確認。

<a id="A31"></a>
### A31　ライブ詳細とLPの目的に必要な項目・導線が不足

**判定：一部未実装｜重要度：中｜設計：基本7 LIV-002/LP-001、詳細2、技術4**

**設計の記載：** ライブ詳細で出演者・日時・チケット情報。LPから来場予約・募集・販売へ誘導する。

**実装の事実：** ライブ詳細はイベントの画像/日時/説明/会場が中心で出演者・チケットの項目なし。LPはタイトルと本文の表示だけで、誘導先ボタンや専用リンクがない。

**影響：** 画面は存在するが、初期範囲の役割を満たす情報・導線が足りない。

**ソース根拠：** `resources/templates/live/detail.html:1–24`、`resources/templates/lp/detail.html:1–16`。

**判定の限界・留意：** 将来のサイト内決済の未実装を指摘しているわけではない。

<a id="A32"></a>
### A32　お問い合わせ・規約・プライバシーはプレースホルダー

**判定：内容は未完成／確定本文は確認不能｜重要度：中｜設計：要件8、基本1、詳細6・14**

**設計の記載：** サイト内リンクの先に実際のお問い合わせ情報・規約・プライバシー情報を置き、規約同意と結び付く。

**実装の事実：** 経路とページは存在するが、「掲載します」等の短い案内だけ。連絡先や実際の規約・プライバシー本文は含まれない。

**影響：** ページの存在をもって運用情報が完成したとは扱えない。

**ソース根拠：** `resources/templates/contact/index.html:1–16`、`resources/templates/privacy.html:1–16`、`resources/templates/terms.html:1–16`。

**判定の限界・留意：** 設計書に確定本文はないため、こちらで文章や法的要件を作り足して不適合にしたものではない。

<a id="A33"></a>
### A33　公開感想GET APIで親コンテンツの公開確認がない

**判定：追加の静的リスク／実HTTPは未検証｜重要度：中｜設計：詳細9・11・13・33（公開境界の確認）**

**設計の記載：** 公開画面が公開対象・所属関係を確認する前提を、直接アクセス可能な公開APIでも保つ必要がある。

**実装の事実：** GET /api/messagesはeventIdの空チェック後、感想status=PUBLISHEDだけを条件に対象別取得へ進む。親イベント/人物/作品の公開状態や所属関係を検証しない。

**影響：** 親を非公開にしても公開感想を直接取得できる可能性がある。設計書にこのGET APIの詳細契約はないため、確定した仕様差ではなく公開範囲の要確認として分離する。

**ソース根拠：** `java/controller/MessageController.java:42–57`、`java/service/MessageService.java:144–170`。

**判定の限界・留意：** 実際に非公開データを取得した検証ではない。

<a id="A34"></a>
### A34　SQLファイルの定義が実DBへ反映される経路が確認できない

**判定：標準起動経路で不使用／実DBは確認不能｜重要度：中｜設計：詳細31・34、技術10**

**設計の記載：** DB定義・制約・性能用の定義が、実際に使うDBへ反映されていること。

**実装の事実：** db/schema.sqlには4インデックスがあるが、spring.sql.init.mode=never。Hibernateのddl-auto=updateでEntityから生成する構成で、Entityに同等のインデックス指定はない。READMEはschemaを定義メモとして説明している。

**影響：** SQLファイルを置いたことだけではインデックス・制約の適用済みを証明できない。

**ソース根拠：** `resources/application.properties:11–15`、`resources/db/schema.sql:89–92`。

**判定の限界・留意：** 手動でSQLを適用しているか、外部マイグレーションがあるかは確認不能。Flyway等の新しい製品採用を要求してはいない。

<a id="A35"></a>
### A35　通報された投稿を扱う仕組みがない

**判定：未実装｜重要度：中｜設計：要件7.1・7.2**

**設計の記載：** 即時公開方式での通報と、運営による通報済み投稿の確認を扱う。

**実装の事実：** 管理者の公開/非公開/削除操作はあるが、利用者による通報の受付、通報状態の保存、管理側の通報済み一覧・絞込みは見つからない。

**影響：** 不適切な投稿を運営が自ら見つける管理と、来場者からの通報を受け取る機能は別である。

**ソース根拠：** `java/controller/AdminController.java:32–84`、`resources/templates/admin/messages.html:1–65`、`resources/db/schema.sql:70–87`。

**判定の限界・留意：** 詳細設計に通報の入力項目・API定義はないため未定義部分は補完せず、「要件あり／具体実装なし」と記録。

<a id="scope"></a>
## 3. 初期リリース対象19項目の対応

`…` は `/playpit/{eventSlug}`。URLの変数名と実装中のeventIdという変数名の違いだけでURL不適合とは扱わず、実際のslug検索経路を確認した。

| No. | 対象 | 画面ID | 経路 | 実在テンプレート | 結果 | 関連ID |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | CreVeトップ | CRV-001 | / | index.html | 一部不適合：取得条件・内部DTO | A25,A28 |
| 2 | CreVeについて | CRV-002 | /about | about.html | 経路・本文あり／確定原稿との一致は確認不能 |  |
| 3 | PLAYPITトップ | PPT-001 | /playpit | playpit/index.html | 経路・コンセプトと特集イベントあり／原稿・実描画未検証 |  |
| 4 | PLAYPITイベント一覧 | PPT-002 | /playpit/events | playpit/event-list.html | 分類・表示項目が不足 | A23,A26 |
| 5 | PLAYPITイベント詳細 | PPT-003 | /playpit/{eventSlug} | playpit/event-detail.html | 関連取得はある／404・表示・DTO差 | A06,A23,A28 |
| 6 | 参加クリエイター一覧 | PPT-004 | …/creators | playpit/creator-list.html | 参加・公開・順序あり／紹介と件数不足 | A23 |
| 7 | クリエイター個人 | PPT-005 | …/creators/{creatorSlug} | playpit/creator-detail.html | 主要内容あり／羽花演出・文字一覧・内部契約差 | A08,A13,A14,A28 |
| 8 | 展示作品一覧 | ART-001 | …/artworks | playpit/artwork-list.html | 作品一覧あり／制作者情報不足 | A23 |
| 9 | 作品詳細 | ART-002 | …/artworks/{artworkSlug} | playpit/artwork-detail.html | サブメディア・羽花導線・投稿後件数不足 | A08,A09,A24 |
| 10 | 感想投稿 | UKA-002 | POST /api/messages | 人物/作品詳細に配置 | API契約・制限・演出の不一致 | A02,A05,A07,A08 |
| 11 | 羽花表示・感想表示 | UKA-001/003 | …/uka | playpit/uka.html＋dialog | 形・成長連携・上限・日付/作品名不足 | A10,A11,A12,A13,A14 |
| 12 | ライブ一覧 | LIV-001 | /live | live/list.html | 経路あり／実起動・全表示未検証 |  |
| 13 | ライブ詳細 | LIV-002 | /live/{eventSlug} | live/detail.html | 出演者・チケット不足 | A31 |
| 14 | LP | LP-001 | /lp/{lpId} | lp/detail.html | 本文のみ、予約・募集等の誘導なし | A31 |
| 15 | 管理者ログイン | 設計画面IDなし | /admin/login | admin/login.html | 基本認証あり／失敗制限・成功先差 | A16,A17,A18 |
| 16 | イベント管理 | 基本ADM-002に包含 | /admin/events | admin/events.html | 読取一覧のみ | A15 |
| 17 | クリエイター管理 | 基本ADM-002に包含 | /admin/creators | admin/creators.html | 読取一覧のみ | A15 |
| 18 | 作品管理 | 基本ADM-002に包含 | /admin/artworks | admin/artworks.html | 読取一覧のみ | A15 |
| 19 | 投稿管理 | 基本ADM-001／詳細ADM-006 | /admin/messages | admin/messages.html | 検索・操作あり／投稿日・命名差 | A19 |



### 機能ID F-001〜F-019

| 機能ID | 機能 | 対応結果 | 関連ID |
| --- | --- | --- | --- |
| F-001 | PLAYPITイベント一覧表示 | 部分実装：分類・掲載項目不足 | A23,A26 |
| F-002 | PLAYPITイベント詳細表示 | 部分実装：種別/公開確認と関連情報あり | A06,A23 |
| F-003 | イベント参加クリエイター一覧 | 参加・公開・順序の検索あり／紹介・件数不足 | A23 |
| F-004 | クリエイター詳細 | プロフィール・作品・感想あり／内部契約等に差 | A28 |
| F-005 | 展示作品一覧 | 展示関係と公開検索あり／制作者情報不足 | A23 |
| F-006 | 作品詳細 | 説明・背景等あり／メディア・羽花導線不足 | A09,A24 |
| F-007 | 作品別QRコード表示 | 該当処理・データ列なし | A20 |
| F-008 | クリエイターへの感想投稿 | 経路あり／API・制限・演出差 | A02,A05,A08 |
| F-009 | 作品への感想投稿 | 経路あり／投稿後の羽花未接続 | A08,A09 |
| F-010 | 感想保存 | save実装あり／DB契約・既定永続性差、実DB未検証 | A01,A03,A04 |
| F-011 | 感想一覧取得 | 公開・イベント/対象別の検索あり／GETの親確認未検証 | A33 |
| F-012 | 花びら表示 | 静的表示・モーダルあり／形・演出差 | A08,A10,A13 |
| F-013 | イベント全体の羽花 | 画面あり／上限・成長・文字一覧不足 | A11,A12,A14 |
| F-014 | クリエイター別投稿表示 | 人物別公開検索あり／本文一覧不足 | A14 |
| F-015 | 作品別投稿表示 | 作品別公開本文一覧あり／更新件数不一致 | A09 |
| F-016 | 投稿公開・非公開管理 | 状態操作・画面実装あり／実DB反映未検証 | A19 |
| F-017 | 作品情報管理 | 読取一覧のみ | A15 |
| F-018 | クリエイター情報管理 | 読取一覧のみ | A15 |
| F-019 | イベント情報管理 | 読取一覧のみ | A15 |



<a id="contracts"></a>
## 4. 内部構造・API・DOMの契約比較

### 4.1 指定クラスの存在と責務

| 区分 | 設計のクラス | 存在確認 | 実装ファイル |
| --- | --- | --- | --- |
| Controller | CreveController | 存在 | java/controller/CreveController.java |
| Controller | PlaypitController | 存在 | java/controller/PlaypitController.java |
| Controller | CreatorController | 存在 | java/controller/CreatorController.java |
| Controller | ArtworkController | 存在 | java/controller/ArtworkController.java |
| Controller | MessageController | 存在 | java/controller/MessageController.java |
| Controller | UkaController | 存在 | java/controller/UkaController.java |
| Controller | LiveController | 存在 | java/controller/LiveController.java |
| Controller | LandingPageController | 存在 | java/controller/LandingPageController.java |
| Controller | AdminController | 存在 | java/controller/AdminController.java |
| Service | EventService | 存在 | java/service/EventService.java |
| Service | CreatorService | 存在 | java/service/CreatorService.java |
| Service | ArtworkService | 存在 | java/service/ArtworkService.java |
| Service | MessageService | 存在 | java/service/MessageService.java |
| Service | UkaService | 存在 | java/service/UkaService.java |
| Service | LiveService | 存在 | java/service/LiveService.java |
| Service | AdminService | 存在 | java/service/AdminService.java |
| Repository | EventRepository | 存在 | java/repository/EventRepository.java |
| Repository | CreatorRepository | 存在 | java/repository/CreatorRepository.java |
| Repository | ArtworkRepository | 存在 | java/repository/ArtworkRepository.java |
| Repository | EventCreatorRepository | 存在 | java/repository/EventCreatorRepository.java |
| Repository | EventArtworkRepository | 存在 | java/repository/EventArtworkRepository.java |
| Repository | MessageRepository | 存在 | java/repository/MessageRepository.java |
| Repository | NewsRepository | 存在 | java/repository/NewsRepository.java |
| Repository | AdminRepository | 存在 | java/repository/AdminRepository.java |
| Entity | Event | 存在 | java/entity/Event.java |
| Entity | Creator | 存在 | java/entity/Creator.java |
| Entity | Artwork | 存在 | java/entity/Artwork.java |
| Entity | EventCreator | 存在 | java/entity/EventCreator.java |
| Entity | EventArtwork | 存在 | java/entity/EventArtwork.java |
| Entity | Message | 存在 | java/entity/Message.java |
| Entity | News | 存在 | java/entity/News.java |
| Entity | Admin | 存在 | java/entity/Admin.java |



**主要32クラスが存在することと、それぞれの指定処理が実装済みであることは別。** 追加のLandingPage/MediaFile等のクラスを、存在するだけで設計違反とは扱わない。指定の独自例外8種類はなく、NotFoundExceptionとBadRequestExceptionの2種類となっている。

### 4.2 主なメソッド契約

| クラス | 設計 | 実装 | 根拠 |
| --- | --- | --- | --- |
| CreveController | showTopPage() | home(Model) | java/controller/CreveController.java:16–22 |
| PlaypitController | showEventList() | events(Model) | java/controller/PlaypitController.java:39–43 |
| PlaypitController | showEventDetail() | eventDetail(String, Model) | java/controller/PlaypitController.java:53–61 |
| CreatorController | showCreatorList() | creators(String, Model) | java/controller/CreatorController.java:47–55 |
| CreatorController | showCreatorDetail() | creatorDetail(String, String, Model) | java/controller/CreatorController.java:57–74 |
| ArtworkController | showArtworkList() | artworks(String, Model) | java/controller/ArtworkController.java:31–38 |
| ArtworkController | showArtworkDetail() | artworkDetail(String, String, Model) | java/controller/ArtworkController.java:40–54 |
| MessageController | createMessage(MessageRequest) → ResponseEntity&lt;MessageResponse&gt; | create(MessageRequest, HttpServletRequest) → MessageResponse、@ResponseStatus(201) | java/controller/MessageController.java:33–40 |
| MessageService | registerMessage(MessageRequest) → MessageResponse | 同名だがHttpServletRequestを追加 | java/service/MessageService.java:67–136 |
| UkaController | showUka() | uka(String, Model)、UkaService未使用 | java/controller/UkaController.java:27–35 |
| UkaService | getUkaData(eventId) | getEventUka(String eventSlug)/eventUka(String eventSlug) | java/service/UkaService.java:28–51 |
| UkaService | calculateGrowthStage(long) | 同クラスにはなし。PetalLayoutUtilに存在 | java/util/PetalLayoutUtil.java:23–36 |
| AdminController | showMessageList() | messages(..., Model) | java/controller/AdminController.java:32–55 |
| MessageService | searchMessages()/changeStatus()/deleteMessage() | 対応機能をAdminServiceに配置 | java/service/AdminService.java:57–93 |



画面用のEventResponse/NewsResponse/EventDetailResponse/CreatorSummaryResponse/ArtworkSummaryResponseは存在せず、Entity等をModelへ渡している。RepositoryはJPAを用いて実装され、save()はJpaRepositoryから継承されるため「明示宣言がない＝save未実装」とは扱っていない。その他の検索メソッド名・シグネチャと実際のクエリは証跡のソース抜粋を参照。

### 4.3 投稿リクエスト全7項目

| 設計項目 | 設計の型・制約 | 実装 | 判定 |
| --- | --- | --- | --- |
| eventId | Long・必須 @NotNull | String @NotBlank @Size(max=64) | 型・Validation差 |
| creatorId | Long・少なくとも一つの対象 | String @Size(max=64) | 型差／Service対象検査はある |
| artworkId | Long・少なくとも一つの対象 | String @Size(max=64) | 型差／Service対象検査はある |
| message | String @NotBlank @Size(max=300) | body @NotBlank @Size(min=1,max=300) | 項目名差／長さ検査はある |
| displayName | String任意 @Size(max=30) | 同名String @Size(max=30) | このフィールドは一致 |
| anonymous | Boolean必須 @NotNull | boolean初期値true | 必須性・Java型差 |
| agreement | Boolean必須 @AssertTrue | termsAgreed boolean @AssertTrue | 項目名・Java型差 |



### 4.4 投稿レスポンス全6項目

| 設計項目 | 実装 | 判定 |
| --- | --- | --- |
| messageId: Long | id: Long | 項目名差 |
| status: String | status: MessageStatus | Java宣言はenum。JSON文字列化は実HTTP未検証 |
| petalType: Integer | petalType: int | 数値値域の単体確認は適合。Java型はprimitive |
| petalSeed: Long | petalSeed: long | 数値生成あり。Java型はprimitive |
| createdAt: String | createdAt: OffsetDateTime | Java型差。JSON文字列化自体はあり得るため通信型違反とは未判定 |
| completionMessage: String | なし | 欠落。JavaScriptが固定文言を持つ |



実装にはeventId/creatorId/artworkId/body/displayName/座標等の追加項目がある。追加したことのみではなく、指定項目の欠落・名前/ID型変更を不適合の根拠とする。

### 4.5 UkaResponse・PetalResponse全項目

| DTO | 設計項目 | 実装 | 結果 |
| --- | --- | --- | --- |
| UkaResponse | eventId Long | eventId String | 型差 |
| UkaResponse | eventTitle String | eventSlug Stringのみ | タイトル欠落 |
| UkaResponse | messageCount Long | totalMessages long | 項目名差 |
| UkaResponse | growthStage Integer | growthStage int | 数値あり／Java型差 |
| UkaResponse | petals List&lt;PetalResponse&gt; | 同項目あり | 存在 |
| PetalResponse | messageId Long | id Long | 項目名差 |
| PetalResponse | petalType Integer | petalType int | 存在／Java型差 |
| PetalResponse | positionX Double | petalX double | 項目名差 |
| PetalResponse | positionY Double | petalY double | 項目名差 |
| PetalResponse | rotation Double | petalAngle double | 項目名差 |
| PetalResponse | scale Double | petalSize double | 項目名差 |
| PetalResponse | message String | body String | 項目名差 |
| PetalResponse | displayName String | 同項目あり | 存在／匿名マスクを確認 |
| PetalResponse | artworkTitle String | なし | 欠落 |
| PetalResponse | creatorName String | なし | 欠落 |



### 4.6 投稿フォーム全項目

| 項目 | 設計 | 実装 | 結果 |
| --- | --- | --- | --- |
| 本文 | textarea id=messageText name=message | textarea name=body、idなし | 不一致 |
| 表示名 | input id=displayName name=displayName | nameは一致、idなし | 一部差 |
| 匿名 | checkbox id=anonymousFlag name=anonymous | nameは一致、idなし | 一部差 |
| 規約同意 | checkbox id=agreementFlag name=agreement | name=termsAgreed、idなし | 不一致 |
| 投稿ボタン | button id=submitMessageButton | button type=submit、idなし | 不一致 |
| エラー | div id=messageError | p data-form-message | 不一致 |
| 文字数 | span id=messageCount | span data-count | 不一致 |
| eventId | hidden項目 | フォームのdata-event-id | 構造差 |
| creatorId | hidden項目 | フォームのdata-creator-id | 構造差 |
| artworkId | hidden項目 | 作品フォームのdata-artwork-id | 構造差 |



CSRFのhidden入力は存在するが、eventId/creatorId/artworkIdのhidden入力を実装したことにはならない。

### 4.7 JavaScript指定関数20件

| ファイル | 設計関数 | 存在 |
| --- | --- | --- |
| common.js | openGlobalMenu() | 指定名の定義なし |
| common.js | closeGlobalMenu() | 指定名の定義なし |
| common.js | toggleGlobalMenu() | 指定名の定義なし |
| message.js | initializeMessageForm() | 指定名の定義なし |
| message.js | updateMessageCount() | 指定名の定義なし |
| message.js | validateMessageForm() | 指定名の定義なし |
| message.js | submitMessage() | 指定名の定義なし |
| message.js | disableSubmitButton() | 指定名の定義なし |
| message.js | enableSubmitButton() | 指定名の定義なし |
| message.js | showMessageError() | 指定名の定義なし |
| message.js | clearMessageError() | 指定名の定義なし |
| message.js | showPetalAnimation() | 指定名の定義なし |
| message.js | resetMessageForm() | 指定名の定義なし |
| uka.js | initializeUka() | 指定名の定義なし |
| uka.js | renderPetals() | 指定名の定義なし |
| uka.js | createPetalElement() | 指定名の定義なし |
| uka.js | openMessageModal() | 指定名の定義なし |
| uka.js | closeMessageModal() | 指定名の定義なし |
| uka.js | addNewPetal() | 指定名の定義なし |
| uka.js | resizeUkaCanvas() | 指定名の定義なし |



関数名がないことを「全機能がない」と読み替えない。文字数表示、入力チェック、ボタン制御、モーダル等は別名/インラインで実装される。一方、花びら移動・リサイズ時再配置等は対応ロジック自体が確認できない。

### 4.8 Repository指定15メソッドの対応

| Repository | 設計のメソッド | 実装の対応 | 実装行 |
| --- | --- | --- | --- |
| EventRepository | findBySlugAndStatus() | 同名あり | 13 |
| EventRepository | findByEventTypeAndStatusOrderByStartAtDesc() | 同名あり | 21 |
| EventRepository | findUpcomingEvents() | 同名あり。公開状態・nowを引数とする | 25–31 |
| CreatorRepository | findBySlugAndStatus() | 同名あり | 12 |
| CreatorRepository | findCreatorsByEventId() | 同名あり。関係/公開/順序のクエリあり | 16–26 |
| ArtworkRepository | findBySlugAndStatus() | 同名あり | 12 |
| ArtworkRepository | findArtworksByEventId() | 同名あり。関係/公開/順序のクエリあり | 14–24 |
| ArtworkRepository | findArtworksByEventIdAndCreatorId() | 同名あり | 26–38 |
| MessageRepository | save() | JpaRepositoryから継承 | 12 |
| MessageRepository | findPublishedMessagesByEventId() | ページング有/無の2本。HTMLは上限なし版 | 15–36 |
| MessageRepository | findPublishedMessagesByCreatorId() | 同名あり。eventIdも条件 | 38–49 |
| MessageRepository | findPublishedMessagesByArtworkId() | 同名あり。eventIdも条件 | 51–62 |
| MessageRepository | countPublishedMessagesByEventId() | countByEventIdAndStatus()へ改名 | 64 |
| MessageRepository | countRecentMessagesBySessionHash() | countBySessionHashAndCreatedAtAfter()へ改名 | 70 |
| MessageRepository | existsRecentDuplicateMessage() | existsBySessionHashAndEventIdAndBodyAndCreatedAtAfter()へ改名 | 72–77 |



各参照はjava/repository/配下の同名.java。Repositoryの同名メソッドは存在を確認したもので、Hibernateでのクエリ検証・実行成功を証明したものではない。IDの型は前掲DB/API比較のとおり。

<a id="database"></a>
## 5. DB列単位の比較（設計に列定義がある主要4テーブル・54列）

基準は詳細22・31。以下の実装欄は同梱db/schema.sqlの宣言で、Entity宣言も照合した。**このSQLが実DBへ適用済みという意味ではない。** 改名の対応候補は比較の便宜であり、仕様変更を承認したものではない。BIGSERIALとIDENTITYはともに自動採番になり得るため、DDLの違いと採番機能の有無を区別する。

### events

| 設計列 | 設計型・制約 | 実装DDL | 差異 | 設計本文行 |
| --- | --- | --- | --- | --- |
| event_id | BIGSERIAL / PK | id VARCHAR(64) PRIMARY KEY（schema.sql:2） | 列名差、型差 | 1791 |
| event_type | VARCHAR(20) / NOT NULL | event_type VARCHAR(24) NOT NULL（schema.sql:4） | 型差 | 1792 |
| slug | VARCHAR(100) / UNIQUE、NOT NULL | slug VARCHAR(80) NOT NULL UNIQUE（schema.sql:3） | 型差 | 1793 |
| title | VARCHAR(150) / NOT NULL | name VARCHAR(160) NOT NULL（schema.sql:6） | 列名差、型差 | 1794 |
| summary | VARCHAR(300) / NULL可 | summary VARCHAR(240) NOT NULL（schema.sql:8） | 型差、NULL条件差 | 1795 |
| description | TEXT / NULL可 | description TEXT NOT NULL（schema.sql:9） | NULL条件差 | 1796 |
| main_image_url | VARCHAR(500) / NULL可 | main_image_url TEXT NOT NULL（schema.sql:15） | 型差、NULL条件差 | 1797 |
| start_at | TIMESTAMP / NOT NULL | start_at TIMESTAMP WITH TIME ZONE NOT NULL（schema.sql:10） | 時刻型差 | 1798 |
| end_at | TIMESTAMP / NOT NULL | end_at TIMESTAMP WITH TIME ZONE NOT NULL（schema.sql:11） | 時刻型差 | 1799 |
| venue_name | VARCHAR(150) / NULL可 | venue_name VARCHAR(120) NOT NULL（schema.sql:13） | 型差、NULL条件差 | 1800 |
| address | VARCHAR(300) / NULL可 | 該当列なし | 欠落 | 1801 |
| access | TEXT / NULL可 | 該当列なし | 欠落 | 1802 |
| price | VARCHAR(100) / NULL可 | 該当列なし | 欠落 | 1803 |
| ticket_url | VARCHAR(500) / NULL可 | 該当列なし | 欠落 | 1804 |
| status | VARCHAR(20) / NOT NULL | status VARCHAR(24) NOT NULL（schema.sql:5） | 型差 | 1805 |
| created_at | TIMESTAMP / NOT NULL | 該当列なし | 欠落 | 1806 |
| updated_at | TIMESTAMP / NOT NULL | 該当列なし | 欠落 | 1807 |



### creators

| 設計列 | 設計型・制約 | 実装DDL | 差異 | 設計本文行 |
| --- | --- | --- | --- | --- |
| creator_id | BIGSERIAL / PK | id VARCHAR(64) PRIMARY KEY（schema.sql:21） | 列名差、型差 | 1810 |
| slug | VARCHAR(100) / UNIQUE、NOT NULL | slug VARCHAR(80) NOT NULL UNIQUE（schema.sql:22） | 型差 | 1811 |
| name | VARCHAR(100) / NOT NULL | name VARCHAR(120) NOT NULL（schema.sql:24） | 型差 | 1812 |
| name_kana | VARCHAR(100) / NULL可 | 該当列なし | 欠落 | 1813 |
| profile | TEXT / NULL可 | profile TEXT NOT NULL（schema.sql:27） | NULL条件差 | 1814 |
| concept | TEXT / NULL可 | concept TEXT NOT NULL（schema.sql:28） | NULL条件差 | 1815 |
| genre | VARCHAR(100) / NULL可 | category VARCHAR(80) NOT NULL（schema.sql:26） | 列名差、型差、NULL条件差 | 1816 |
| profile_image_url | VARCHAR(500) / NULL可 | icon_image_url TEXT NOT NULL（schema.sql:29） | 列名差、型差、NULL条件差 | 1817 |
| website_url | VARCHAR(500) / NULL可 | website VARCHAR(240)（schema.sql:33） | 列名差、型差 | 1818 |
| status | VARCHAR(20) / NOT NULL | status VARCHAR(24) NOT NULL（schema.sql:23） | 型差 | 1819 |
| created_at | TIMESTAMP / NOT NULL | 該当列なし | 欠落 | 1820 |
| updated_at | TIMESTAMP / NOT NULL | 該当列なし | 欠落 | 1821 |



### artworks

| 設計列 | 設計型・制約 | 実装DDL | 差異 | 設計本文行 |
| --- | --- | --- | --- | --- |
| artwork_id | BIGSERIAL / PK | id VARCHAR(64) PRIMARY KEY（schema.sql:38） | 列名差、型差 | 1824 |
| creator_id | BIGINT / FK、NOT NULL | creator_id VARCHAR(64) NOT NULL（schema.sql:41） | 型差、FKなし | 1825 |
| slug | VARCHAR(100) / UNIQUE、NOT NULL | slug VARCHAR(80) NOT NULL UNIQUE（schema.sql:39） | 型差 | 1826 |
| title | VARCHAR(150) / NOT NULL | title VARCHAR(160) NOT NULL（schema.sql:43） | 型差 | 1827 |
| description | TEXT / NULL可 | description TEXT NOT NULL（schema.sql:44） | NULL条件差 | 1828 |
| background | TEXT / NULL可 | background TEXT NOT NULL（schema.sql:47） | NULL条件差 | 1829 |
| concept | TEXT / NULL可 | intention TEXT NOT NULL（schema.sql:48） | 列名差、NULL条件差 | 1830 |
| materials | VARCHAR(300) / NULL可 | technique VARCHAR(120) NOT NULL（schema.sql:49） | 列名差、型差、NULL条件差 | 1831 |
| production_year | SMALLINT / NULL可 | production_year VARCHAR(40) NOT NULL（schema.sql:50） | 型差、NULL条件差 | 1832 |
| main_image_url | VARCHAR(500) / NULL可 | image_url TEXT NOT NULL（schema.sql:45） | 列名差、型差、NULL条件差 | 1833 |
| status | VARCHAR(20) / NOT NULL | status VARCHAR(24) NOT NULL（schema.sql:42） | 型差 | 1834 |
| created_at | TIMESTAMP / NOT NULL | 該当列なし | 欠落 | 1835 |
| updated_at | TIMESTAMP / NOT NULL | 該当列なし | 欠落 | 1836 |



### messages

| 設計列 | 設計型・制約 | 実装DDL | 差異 | 設計本文行 |
| --- | --- | --- | --- | --- |
| message_id | Long / BIGSERIAL / 不可 | id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY（schema.sql:71） | 列名差、型差 | 1615 |
| event_id | Long / BIGINT / 不可 | event_id VARCHAR(64) NOT NULL（schema.sql:72） | 型差 | 1616 |
| creator_id | Long / BIGINT / 可 | creator_id VARCHAR(64)（schema.sql:73） | 型差 | 1617 |
| artwork_id | Long / BIGINT / 可 | artwork_id VARCHAR(64)（schema.sql:74） | 型差 | 1618 |
| message | String / VARCHAR(300) / 不可 | body VARCHAR(300) NOT NULL（schema.sql:75） | 列名差 | 1619 |
| display_name | String / VARCHAR(30) / 可 | display_name VARCHAR(30) NOT NULL（schema.sql:76） | NULL条件差 | 1620 |
| is_anonymous | Boolean / BOOLEAN / 不可 | anonymous BOOLEAN NOT NULL（schema.sql:77） | 列名差 | 1621 |
| status | MessageStatus / VARCHAR(20) / 不可 | status VARCHAR(20) NOT NULL（schema.sql:79） | 列定義は一致 | 1622 |
| petal_type | Integer / SMALLINT / 不可 | petal_type INTEGER NOT NULL（schema.sql:80） | 型差／1〜5制約なし | 1623 |
| petal_seed | Long / BIGINT / 不可 | petal_seed BIGINT NOT NULL（schema.sql:81） | 列定義は一致 | 1624 |
| created_at | LocalDateTime / TIMESTAMP / 不可 | created_at TIMESTAMP WITH TIME ZONE NOT NULL（schema.sql:78） | 時刻型差 | 1625 |
| updated_at | LocalDateTime / TIMESTAMP / 不可 | 該当列なし | 欠落 | 1626 |



### 関連データ・追加列

| 対象 | 設計 | 実装・差異 |
| --- | --- | --- |
| event_creators | 参加ID、event_id、creator_id、exhibition_title、display_order、created_at | idとString関係ID・display_orderのみ。展示名・作成日時なし。ペアUNIQUEはある。 |
| event_artworks | event_artwork_id、event_id、artwork_id、exhibition_area、display_order、qr_code_url、created_at | idとString関係ID・display_orderのみ。展示場所・QR・作成日時なし。ペアUNIQUEはある。 |
| 作品のメディア | media_urls：追加画像・動画・音声 | main相当image_urlとsub_image_url各1本。MediaFile構造はあるが画面との関連付けなし。 |
| messagesの識別・配置 | petal_seed等と一時的な利用者識別 | petal_x/y/angle/size、session_hashを追加して保存。一時的ハッシュの保存期限・掃除処理は見つからない。 |
| 主要3テーブルの追加属性 | 設計定義を基準 | subtitle/schedule_text/venue_text/postable/display_order、artist_name/category等の追加/代替列。追加属性だけで機能違反とはせず、欠落する設計列と区別。 |
| artworks.event_id | 作品とイベントの関係はevent_artworksで管理 | artworksにも単一event_idを重複保持。実際の検索はevent_artworksを使用する箇所あり。複数イベント再展示の整合性は実DB未検証。 |



### 投稿Serviceの18手順対応

| 順 | 設計手順 | 実装対応 |
| --- | --- | --- |
| 1 | イベント取得 | 68行：findById(request.getEventId()) |
| 2 | 存在なし例外 | EventServiceがNotFoundException。指定EventNotFoundExceptionではない |
| 3 | 公開/投稿可確認 | 70–75行：PUBLISHEDとpostableを別々に確認 |
| 4 | 指定creator取得 | 83–86行・220–229行：作品から補完後にresolveCreator |
| 5 | イベント参加確認 | 220–229行：参加関係確認あり |
| 6 | 指定artwork取得 | 82行・233–242行：先にresolveArtwork |
| 7 | 作品の展示関係 | 233–242行：確認あり |
| 8 | 作者とcreator一致 | 87–89行：確認あり |
| 9 | trim | 77行：対象解決前に実施 |
| 10 | 禁止/不適切確認 | 91行：MessageValidator、簡易禁止・審査待ち判定あり |
| 11 | 連続/重複 | 92–105行：期間・回数・HTTP例外に差 |
| 12 | 花びら種類決定 | 110–134行のEntity生成中にUtil使用 |
| 13 | petalSeed生成 | 108–115行：本文等からCRC32。種別より先 |
| 14 | Entity生成 | 117–135行：別カラム構造を生成 |
| 15 | 公開状態設定 | 91行で計算したstatusを生成時に設定 |
| 16 | Repository.save | 136行：保存 |
| 17 | Response生成 | 136行：MessageResponse.from() |
| 18 | 返却 | 136行：返却 |



処理の意味が存在するものと、指定順序・例外・引数が変わったものを分離している。これは設計の順序をこちらで改良したものではない。

<a id="coverage"></a>
## 6. 全81章の確認表

全章を索引化し、確認した範囲と未検証を記録した。**章単位の確認表であり、その章の全条件が実環境で通ったことや、未発見の不具合がないことを保証するものではない。** 将来仕様・目的記述・設計間の差を無理に合否へ変換していない。

### 要件定義変更

| 章 | 設計本文行 | 判定 | 確認内容 | 指摘ID |
| --- | --- | --- | --- | --- |
| 1. 変更概要 | 11–18 | 一部不適合 | 人物ページ・投稿はある。花びら移動と羽形成が不足。 | A08, A10, A12 |
| 2. 参加型作品「羽花」の位置付け | 19–28 | 一部不適合 | 参加型表現の中心となる羽と成長が部分実装。 | A08, A10, A12 |
| 3. クリエイター個人ページ | 29–70 | 一部不適合／文書差異 | プロフィール・作品・SNS・投稿は存在。本文一覧等が不足。グローバルURLとイベント配下URLを区別。将来の本人編集等は今回の未実装判定から除外。 | A13, A14, A24 |
| 4. 感想・メッセージ投稿機能 | 71–101 | 一部不適合／文書差異 | 本文・匿名・制限等の実装あり。投稿対象は後段API契約と照合。項目変更と演出未接続。 | A02, A05, A08 |
| 5. 羽花の表示機能 | 102–142 | 一部不適合 | 件数計算・静的花びらはある。羽の形・重なり・日付/作品名が不足。 | A10, A12, A13 |
| 6. イベント全体の羽花 | 143–154 | 一部不適合 | イベント全体画面はある。人物別件数・過程表示が不足。 | A11, A12, A14 |
| 7. 投稿内容の表示・管理 | 155–183 | 一部不適合 | 簡易検査、PENDING/PUBLISHED、管理者公開/非公開/論理削除あり。投稿日検索や通報不足。 | A19, A35 |
| 8. サイト構成の変更 | 184–222 | 概ね経路あり／文書差異 | 後段サイト構成との変更を保持。規約・お問い合わせ等の内容は仮置き。 | A32 |
| 9. 初期インフラ要件の変更 | 223–257 | 一部確認／外部は確認不能 | Javaへの明示変更を保持。API/DBコードあり。CDN・監視・公開基盤の実設定は未検証。 | A01, A21, A34 |
| 10. データ要件 | 258–293 | 不適合 | データの概念はあるが列名・型・関係属性が変更・欠落。 | A03, A04, A20 |
| 11. 非機能要件の追加 | 294–314 | 一部不適合／負荷未検証 | 失敗時入力保持は確認。二重送信、視覚以外の一覧、永続性に差。同時アクセス性能は未実測。 | A01, A07, A14 |
| 12. セキュリティ要件の追加 | 315–324 | 一部確認／不適合 | CSRF・認証・textContent等の実装あり。投稿制限値が違う。IPを永続保存しない経路を確認。実HTTP安全性は未検証。 | A05, A16, A33 |
| 13. アクセス解析要件の追加 | 325–336 | 未実装／外部は確認不能 | 9指標のアプリ内計測がない。 | A21 |
| 14. 初期リリース対象の変更 | 337–349 | 一部未実装／文書差異 | 一覧・詳細・投稿等あり。演出・QRなど不足。イベント羽花の初期範囲は後段記載も保持。 | A08, A20 |
| 15. 初期リリースの完了条件 | 350–363 | 完了と判定できない | 演出・永続性など明記の完了条件に未達がある。過去イベントの扱いには後段の時期差。 | A01, A08, A22, A26 |
| 16. 将来のVR空間との連携 | 364–376 | 将来範囲／今の不適合とはしない | VR本体未実装を欠陥に数えない。現在のID/データ契約違いは別に記録。 | A02, A03 |
| 17. 本機能の役割 | 377–382 | 目的記述／部分実現 | Webに人・作品・言葉は配置されるが、羽花体験・永続性に未達。 | A01, A08, A10 |



### 基本設計変更

| 章 | 設計本文行 | 判定 | 確認内容 | 指摘ID |
| --- | --- | --- | --- | --- |
| 1. サイト構成 | 383–444 | 一部不適合／文書差異 | 主要経路あり。管理ダッシュボードや仮内容、アーカイブ分類に差。 | A18, A26, A32 |
| 2. PLAYPIT内の情報構造 | 445–460 | 一部適合 | イベントから人物・作品・投稿へ辿る経路と所属検査あり。投稿後の羽花導線不足。 | A08, A09 |
| 3. URL構成 | 461–482 | URL形状は概ね一致 | イベント配下の人物・作品・羽花ルートあり。変数名eventIdは実際はslug検索。 | A28 |
| 4. 作品詳細ページ | 483–516 | 一部不適合 | 作品説明・背景・意図・フォームあり。メディア、QR、羽花導線不足。 | A08, A09, A20, A24 |
| 5. 羽花の配置 | 517–528 | 一部不適合／将来別扱い | イベント羽花あり。人物/作品別の表示・文字一覧等に不足。VR/全体羽は将来扱い。 | A10, A11, A14 |
| 6. クリエイター個人ページの役割 | 529–539 | 一部適合 | 人物中心情報と当該イベント作品あり。イベント情報・文字一覧の不足は記録。 | A14, A23 |
| 7. 画面一覧の変更 | 540–558 | 一部未実装 | 主要画面の枠はあるがライブ/LP/コンテンツ管理等の役割が不足。ADM番号差は原文差として保持。 | A15, A19, A31 |
| 8. 画面遷移 | 559–595 | 一部不適合 | 作品・人物への閲覧経路あり。QR→作品→投稿→花びら→羽花の最後が繋がらない。 | A08, A09, A20 |
| 9. データ構造への影響 | 596–622 | 不適合 | artworks/event_artworksはあるが型・列、media_urls/展示場所/QR/日時等が異なる。 | A03, A04, A20, A24 |
| 10. 機能一覧への追加 | 623–643 | 一部未実装 | F-001〜F-019は別表に個別対応。QRとコンテンツ更新等が不足。 | A15, A20 |
| 11. 設計方針 | 644–657 | 目的記述／部分実現 | イベント・人物・作品・言葉は関連付く。羽花体験と保持は未達。 | A01, A08, A10 |



### 使用技術変更

| 章 | 設計本文行 | 判定 | 確認内容 | 指摘ID |
| --- | --- | --- | --- | --- |
| 1. 使用言語 | 658–665 | 使用言語は一致 | Java/HTML/CSS/JavaScript/SQLを確認。 |  |
| 2. 推奨システム構成 | 666–695 | アプリ構成は一部一致／外部未検証 | Spring Bootアプリは存在。Cloudflare/HTTPS/CDNの配備は不明。 | A01 |
| 3. バックエンド構成 | 696–720 | 一部適合 | Boot/MVC/Security/JPA/Mavenあり。Java業務の機能不足・引数/責務の差。 | A15, A28 |
| 4. フロントエンド構成 | 721–768 | 一部不適合 | HTML/CSS/JSあり。アニメーション/メディア切替/メニュー操作等不足。 | A08, A24, A27, A29 |
| 5. 画面作成方式 | 769–793 | 構成は一致／実起動未検証 | Spring Boot+Thymeleafの同一プロジェクト。 |  |
| 6. システム内部構成 | 794–822 | 層はある／詳細契約に差 | Controller→Service→Repository構造は存在。指定メソッド/DTO/HTTP引数の扱い等が異なる。 | A28 |
| 7. Javaの基本構成 | 823–863 | 主要クラス存在／後段と差あり | 後段の追加クラスも含めて存在を確認。存在を機能完了とはしない。 | A28 |
| 8. HTML・CSS・JavaScriptの構成 | 864–909 | 概ね存在／文書差異 | 旧events.html等と後段*-list.htmlを分ける。実装は後段のファイル名中心。 | A19, A29 |
| 9. 羽花機能の処理構成 | 910–951 | 一部不適合 | 投稿保存経路はある。花びら演出/羽花HTMLの成長・上限制御は未接続。 | A08, A11, A12 |
| 10. データベース | 952–977 | 不適合／実DB不明 | PostgreSQL依存はあるが既定H2、DB契約差あり。 | A01, A03, A04 |
| 11. 公開環境 | 978–999 | 確認不能 | ホスト/CDN/TLS/公開手順の実施証跡なし。候補製品の未採用を欠陥にしない。 | A01, A17 |
| 12. 開発ツール | 1000–1010 | 一部確認／開発機操作は不明 | Maven/Git用設定・IDE設定等はあるが、実際に使用したIDE/Postman等は証明不能。 |  |
| 13. テスト方針 | 1011–1036 | 不適合／環境で実行未了 | 同梱業務テストなし。監査側の個別プローブと本体試験を分ける。 | A22 |
| 14. 修正後の技術構成 | 1037–1056 | 一部不適合 | 言語・フレームワークは概ね一致。標準DB・テスト不足。 | A01, A22 |
| 15. 採用方針 | 1057–1067 | 一部適合／将来未判定 | Java中心の単一Webアプリ構成は一致。VR拡張の完成は初期に要求しない。 | A01, A02 |



### 詳細設計

| 章 | 設計本文行 | 判定 | 確認内容 | 指摘ID |
| --- | --- | --- | --- | --- |
| 1. 文書概要 | 1068–1084 | 目的記述 | 実装可能な粒度で照合する文書。構造/処理/例外/テストの契約差を別項で記録。 | A28, A29 |
| 2. 対象範囲 | 1085–1105 | 一部未実装 | 初期19項目を別表に整理。コンテンツ管理・ライブ/LP等が不足。 | A15, A31 |
| 3. システム構成 | 1106–1137 | 一部不適合 | JPAを含む技術は存在、既定DBはH2。 | A01 |
| 4. Javaパッケージ構成 | 1138–1188 | 主要クラス存在／一部不適合 | 指定クラスの存在とメソッドの適合を区別。 | A28 |
| 5. HTML・CSS・JavaScript構成 | 1189–1231 | 概ね存在／一部相違 | 公開HTML/CSS/JSの主要ファイルは存在。管理一覧名等に差。 | A19, A29 |
| 6. 共通画面設計 | 1232–1266 | 一部不適合 | メニュー開閉はある。背景スクロール停止、フッターSNS/著作権なし。 | A27 |
| 7. CreVeトップ画面詳細設計 | 1267–1292 | 不適合 | トップの取得条件・上限・Response/Model・メソッドが異なる。 | A25, A28 |
| 8. PLAYPITイベント一覧画面 | 1293–1315 | 一部不適合 | 公開PLAYPITと日時降順は実装。開催区分・表示項目・件数制限不足。 | A23, A26 |
| 9. PLAYPITイベント詳細画面 | 1316–1347 | 一部適合／例外未達 | イベント種別/公開/関連情報/件数を取得。404画面変換やResponse型に差。 | A06, A23, A28 |
| 10. クリエイター一覧画面 | 1348–1369 | 一部不適合 | 参加関係と表示順/公開条件あり。出展数・紹介文とDTO不足。 | A23, A28 |
| 11. クリエイター個人ページ | 1370–1397 | 一部不適合 | 所属検査/イベント作品/公開感想・件数は実装。メソッド・文字一覧・演出等に差。 | A06, A08, A14, A28 |
| 12. 作品一覧画面 | 1398–1412 | 一部不適合 | event_artworks順・公開除外あり。制作者情報の同時取得/表示不足。 | A23, A28 |
| 13. 作品詳細画面 | 1413–1444 | 一部不適合 | 所属検査/本文情報/感想あり。サブ画像・羽花リンク・投稿後件数不足。 | A09, A24, A28 |
| 14. 感想投稿フォーム詳細設計 | 1445–1465 | 機能一部適合／構造不適合 | 長さ・匿名・同意検査はある。id/name/hidden構造は別物。 | A02, A29 |
| 15. JavaScript感想投稿処理 | 1466–1501 | 一部不適合 | 入力検査/通信/失敗保持等は実装。関数名/送信ガード/演出/件数が不適合。 | A07, A08, A09, A29 |
| 16. 感想投稿API詳細設計 | 1502–1536 | 不適合 | POST/JSON/201は記述あり。送受信項目・型が異なる。実HTTP未検証。 | A02 |
| 17. MessageRequest設計 | 1537–1548 | 不適合 | 指定DTOフィールド・型・必須条件と異なる。 | A02 |
| 18. MessageController設計 | 1549–1562 | 不適合 | 業務をServiceへ委譲するが、名前/引数/戻り値/429応答が異なる。 | A02, A05, A28 |
| 19. MessageService設計 | 1563–1588 | 一部不適合 | イベント/公開/対象/所属/作者一致/フィルタ/saveは実装。処理順・引数・制限・型等に差。 | A02, A05, A28 |
| 20. 投稿公開状態 | 1589–1596 | コード・単体部分は適合 | 4状態と公開/審査待ちの分岐・論理削除を確認。DB反映は未検証。 |  |
| 21. 連続投稿判定 | 1597–1609 | 不適合 | 60秒は一致するが5回・同一本文1分。識別はsession hash、生IP保存なし。 | A05 |
| 22. Message Entity設計 | 1610–1633 | 不適合 | messagesの列名/型/NULL/日時/FK/値域制約に差。 | A03, A04 |
| 23. 羽花表示画面 | 1634–1659 | 不適合 | 指定UkaService呼び出しがなく、HTMLは全件取得。Responseも差。 | A11, A12, A28 |
| 24. 羽花成長判定 | 1660–1673 | 数値は適合／配置責務は差 | 全8境界値一致。ただしUkaServiceでなくUtilに実装。 | A12 |
| 25. 花びら情報生成 | 1674–1693 | 一部不適合 | 同seedの再現性と比率座標はある。Response項目、羽形、重なり調整不足。 | A10, A13 |
| 26. 羽花JavaScript設計 | 1694–1708 | 一部不適合 | HTML+CSS transformとモーダルあり。指定関数、描画/新規追加/リサイズ・成長連携不足。 | A08, A12, A29 |
| 27. 管理者ログイン | 1709–1726 | 一部不適合 | BCrypt/CSRF/session fixation設定はある。成功先と失敗制限に差。 | A16, A17, A18 |
| 28. 投稿管理画面 | 1727–1750 | 一部不適合 | 基本検索・公開/非公開/論理削除/詳細あり。投稿日/ファイル名/メソッド責務差。 | A19 |
| 29. 例外設計 | 1751–1771 | 不適合 | 指定例外と共通範囲を満たさず、429なし。画面HTTP実測は未了。 | A05, A06 |
| 30. ログ設計 | 1772–1787 | 未実装 | 指定業務ログの明示実装なし。 | A30 |
| 31. DB詳細設計 | 1788–1836 | 不適合 | 主要3テーブル全列比較を別表に記載。実DBのスキーマは未確認。 | A03, A04, A34 |
| 32. Repository詳細設計 | 1837–1861 | 一部適合／メソッド差 | JPA検索は存在。名前・引数の差と表示上限未使用を記録。 | A11, A28 |
| 33. セキュリティ設計 | 1862–1880 | 主要設定は存在／HTTP未検証 | 公開とADMIN区分・CSRFトークン経路あり。実HTTP認可・公開親確認は未検証。 | A16, A17, A33 |
| 34. 性能設計 | 1881–1887 | 一部不適合／負荷未実測 | HTML羽花は上限なし、一般一覧もページングなし。画像は同梱SVG中心、WebP必須と読み替えない。 | A11, A23, A34 |
| 35. テスト観点 | 1888–1915 | 業務テスト未実装 | 既存テストはcontextLoadsのみ。監査プローブ結果を別途明示。 | A22 |
| 36. 実装順序 | 1916–1937 | 順序は確認不能 | .gitが除外されているため開発順序は検証不可。本番公開・総合試験証跡もなし。 | A22 |
| 37. 詳細設計完了条件 | 1938–1953 | 設計側の完了条件／実装契約は不一致 | ここは設計完了の定義であり実装試験の合格宣言ではない。実装のクラス/DTO/API等に未一致。 | A02, A03, A28, A29 |
| 38. 詳細設計の結論 | 1954–1979 | 一部不適合 | 投稿→保存の実装経路はあるが、花びら演出→羽花の接続が未達。 | A08, A09, A11, A12 |



<a id="tests"></a>
## 7. 実施した検証と限界

| 検証 | 結果 | 意味 |
| --- | --- | --- |
| JavaScript構文チェック | 6ファイル／6成功 | node --check。DOM/APIとの結合を保証しない。 |
| 個別ブラウザプローブ | 21項目：13一致、8不一致 | 隔離Chromiumで元HTML/JS/CSSを使用。Thymeleaf/Spring/API/DBは未起動。API応答・Model由来data属性のみ監査用固定値。B19はプログラムによるsubmitイベント二重発火。 |
| 個別Javaプローブ | 36項目：27一致、9不一致 | 元の9 Javaファイルを変更せずjavacへ入力。依存する注釈・列挙型だけ最小スタブを使用。メソッド本体と型定義の検証であり、Spring・JPA・Bean Validation・HTTP・DBの動作検証ではない。 |
| 同梱Mavenテスト | 実行未了：配布先DNS失敗 | コンパイル・Spring起動・JUnit結果は未判定。 |
| 原本ハッシュ比較 | 177ファイルに変更なし | 監査前後の原本一致。 |
| 同梱画像/CSV参照 | SVG63件はXMLとして解析可。CSV30行・17列、参照画像60件の欠落0 | 実在人物/作品の内容の正確性・掲載権利・デザイン完成を証明しない。 |



### 7.1 ブラウザの個別結果

| 検査 | 期待 | 実測 | 結果 |
| --- | --- | --- | --- |
| B01 投稿JSONに設計のmessage項目がある | True | False | FAIL |
| B02 投稿JSONに設計のagreement項目がある | True | False | FAIL |
| B03 作品投稿成功後の花びら要素 | True | False | FAIL |
| B04 作品投稿成功後の画面件数 | 1 | 0 | FAIL |
| B05 作品投稿成功後の本文クリア |  |  | PASS |
| B06 作品投稿後の一覧への反映 | True | True | PASS |
| B07 成功後の送信ボタン再活性 | True | True | PASS |
| B08 人物投稿後に花びら要素追加 | 1 | 1 | PASS |
| B09 投稿の花びら移動アニメーション | True | False | FAIL |
| B10 人物ページ投稿後の件数 | 1 | 1 | PASS |
| B11 花びらからモーダルを開ける | True | True | PASS |
| B12 モーダルの本文 | 監査用の感想 | 監査用の感想 | PASS |
| B13 モーダル内の投稿日 | True | False | FAIL |
| B14 PENDINGは公開花びらに追加しない | 0 | 0 | PASS |
| B15 PENDINGの確認待ち表示 | 内容確認後に公開されます。 | 内容確認後に公開されます。 | PASS |
| B16 通信失敗時に本文を保持 | 監査用の感想 | 監査用の感想 | PASS |
| B17 通信失敗時に再送案内 | True | True | PASS |
| B18 空白だけの本文では送信しない | 0 | 0 | PASS |
| B19 送信中にsubmitイベントが再度来ても通信は1回 | 1 | 2 | FAIL |
| B20 スマートフォンメニュー開閉 | True | True | PASS |
| B21 メニュー展開中は背景スクロール停止 | 0 | 82 | FAIL |



ブラウザは隔離Chromiumの390×844表示で実施。作品投稿は成功JSONを返す仮のfetch応答。B19はsubmitイベントをプログラムで2回送った試験で、通常のボタンダブルクリックの実機試験ではない。B21のスクロール量82pxは観測時点の値であり、重要なのは0でないこと。

送信されたJSON（監査用データのみ）：
```json
{
  "eventId": "event-test",
  "creatorId": "creator-test",
  "artworkId": "artwork-test",
  "body": "監査用の感想",
  "displayName": "監査者",
  "anonymous": true,
  "termsAgreed": true
}
```

### 7.2 Javaの個別結果

| 検査 | 期待 | 実測 | 結果 |
| --- | --- | --- | --- |
| J-GROW-0 | 0 | 0 | PASS |
| J-GROW-1 | 1 | 1 | PASS |
| J-GROW-10 | 1 | 1 | PASS |
| J-GROW-11 | 2 | 2 | PASS |
| J-GROW-30 | 2 | 2 | PASS |
| J-GROW-31 | 3 | 3 | PASS |
| J-GROW-60 | 3 | 3 | PASS |
| J-GROW-61 | 4 | 4 | PASS |
| J-TYPE-1to5 | true | true | PASS |
| J-SEED-repeatable | 3519910291 | 3519910291 | PASS |
| J-RATE-attempt-1 | true | true | PASS |
| J-RATE-attempt-2 | true | true | PASS |
| J-RATE-attempt-3 | true | true | PASS |
| J-RATE-attempt-4 | false | true | FAIL |
| J-RATE-attempt-5 | false | true | FAIL |
| J-RATE-attempt-6 | false | false | PASS |
| J-BODY-empty | false | false | PASS |
| J-BODY-spaces | false | false | PASS |
| J-BODY-1 | true | true | PASS |
| J-BODY-300 | true | true | PASS |
| J-BODY-301 | false | false | PASS |
| J-NAME-30 | true | true | PASS |
| J-NAME-31 | false | false | PASS |
| J-BANNED-script | false | false | PASS |
| J-REVIEW-keyword | PENDING | PENDING | PASS |
| J-ANONYMOUS | 匿名 | 匿名 | PASS |
| J-ANONYMOUS-RESPONSE | 匿名 | 匿名 | PASS |
| J-HIDDEN | HIDDEN | HIDDEN | PASS |
| J-DELETED | DELETED | DELETED | PASS |
| J-API-eventId-type | Long | String | FAIL |
| J-API-message-field | String | ABSENT | FAIL |
| J-API-agreement-field | Boolean | ABSENT | FAIL |
| J-API-anonymous-type | Boolean | boolean | FAIL |
| J-API-response-messageId | Long | ABSENT | FAIL |
| J-API-completionMessage | String | ABSENT | FAIL |
| J-LAYOUT-distinct-inputs-avoid-complete-overlap | true | false | FAIL |



利用したスタブは依存注釈・型をjavacが解決するためのもの。JPA保存、Spring Security、Bean Validation制約の発火を代行する実装ではない。特に@NotNull/@AssertTrueのランタイム評価やJSONデシリアライズが正しいとは、この結果から言えない。

### 7.3 全体ビルド

Java 21.0.11の環境で、原本とは別コピーに対して次を試した。
```bash
timeout 210 bash mvnw -B test
```

取得前に停止したログ：
```text
wget: Failed to fetch https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip
BUILD_EXIT=1
```

接続の追加確認では `curl: (6) Could not resolve host: repo.maven.apache.org`。このため「ビルドが通らないコード」とは断定していない。依存取得後のコンパイル、Spring起動、JPAクエリ、PostgreSQL接続・永続化、実API・認証の一連の試験は未検証。

## 8. 設計に合っていることを確認できた部分

| 対象 | 確認できた内容 | 限界・根拠 |
| --- | --- | --- |
| 基本構成 | Java/Spring Boot/MVC/Thymeleaf/JPAのプロジェクト、Controller/Service/Repositoryの層がある | pom.xml、各パッケージ。実起動未検証。 |
| 対象の所属チェック | 人物のイベント参加、作品の出展関係、作品制作者と対象人物の一致を確認するコード | MessageService:82–89,220–242、CreatorService、ArtworkService。実DB結果は未検証。 |
| 公開対象の絞込み | 主要画面のイベント・人物・作品・感想にPUBLISHED条件を確認 | 各Repository/Service。直接GET APIの親確認はA33。 |
| 文字数・空白 | 本文空/空白拒否、1/300文字受理、301拒否、表示名30/31文字境界 | Javaプローブ。実HTTPのValidationは別。 |
| 匿名 | 匿名ONで入力済み表示名を匿名として返す | Message.getDisplayNameとMessageResponseの個別検証。 |
| 状態 | PENDING/PUBLISHED/HIDDEN/DELETED、非公開・論理削除の状態変更 | 元Entity/Validatorの単体検証、Adminの処理確認。DB反映未検証。 |
| 成長段階の数値 | 0/1/10/11/30/31/60/61の8境界が一致 | PetalLayoutUtil。画面成長の接続は別問題。 |
| 投稿失敗時 | 本文を保持し、再送案内・送信ボタン復帰 | モック失敗のブラウザ試験。 |
| 管理者認証の基本設定 | ADMINロール制限、BCrypt、セッション固定対策、CSRFの設定・トークン送信経路 | SecurityConfigとテンプレート/JS。実HTTPでの攻撃耐性を合格判定したものではない。 |



## 9. 設計書内の差異・設計にない決定・確認不能を分離

| 論点 | 保持した原文上の違い／扱い |
| --- | --- |
| 技術構成 | 前段のNext.js/Vercel案と、使用技術変更のJava/Thymeleaf案を区別。明示された変更は変更として照合し、前段を理由にJavaを不適合としない。 |
| DBアクセス | 前段JPAまたはMyBatis、詳細3.1はJPA。今回JPAを使う点は適合。 |
| 人物のURL | 前段は/creators、基本変更以降はイベント配下。両方の経路を持つこと自体は未承認機能追加と断定しない。 |
| テンプレート名 | 技術変更のevents.html/creators.html/artworks.htmlと、詳細の*-list.htmlを保持。後者の存在を確認。 |
| 管理画面ID | 基本ADM-001と詳細ADM-006の差をそのまま記録。コードに画面IDがないことだけでは欠陥としない。 |
| 羽花の初期範囲 | 人物別優先→イベント全体を初期対象とする記載がある。今回イベント全体の実装経路も照合。 |
| アーカイブ時期 | 初期完了条件の過去羽花閲覧と、基本変更で将来とされる複数イベントアーカイブの差は未解消のまま記録。 |
| PENDINGの演出 | 審査待ちの詳しい演出は明記不足。実装の「公開花びらに加えず確認待ちを案内」は確認した事実として扱い、独自演出を要求しない。 |
| artworkIdだけの投稿 | APIで許容される一方、Service手順のcreatorId一致確認に補完方法の明記がない。実装の作者補完を直ちに違反とはしない。 |
| 掲載コンテンツ | CSV人物30行・画像60参照、各人物の作品生成、公開サンプルイベント等がある。提供された正式原稿/許諾と一致するかは確認不能。DataSeederは環境限定なく動くため、公開データの確認は必要。 |
| 将来機能 | VR本体・デジタル販売・ファン/お気に入り・支援・本人編集を、初期未完成の根拠に数えない。 |
| デザインの好み | 配色やフォントの好みを仕様違反として採点していない。設計に書かれた表示項目・動作・羽の形を照合した。 |
| 環境・履歴 | Macの未共有.env/環境変数/外部DB、Cloudflare、HTTPS、本番公開、監視、Gitの実装順は確認不能。共有のために除外されたファイルを未作成と断定しない。 |



## 10. 受入判定

今回の提出物は、**画面と主要処理の骨組みを持つ部分実装**である。既に動作する部分をすべて否定する必要はないが、API・DB・投稿制限・羽花・管理機能の差が明確なので、「要件定義・詳細設計・内部構造まで、そのまま実装済み」という説明は裏付けられない。

設計適合の確認には、指摘した明示契約の差を解消したコードと、未検証に分けたSpring/DB/HTTPの実行証跡が必要。変更承認が別途存在する場合は、承認済み設計と対応させて判定すべきであり、実装に合わせてこちらが原設計を書き換えることはしない。

## 11. 証跡・参照情報

元ZIP SHA-256：
```text
8e8c56e668cd9f0df8a58beea224ad0d064e3b129ae6afc8fc223482bf9ba807
```

基準本文 SHA-256：
```text
da159e2be124b47117b3db14c6045d13dbf0e7604af8838f8b2cb3c481c4428f
```

証跡ZIPには、章別確認表JSON、指摘JSON、DB比較JSON、ファイル/メソッド棚卸し、ブラウザ/Javaプローブと結果、Maven取得失敗ログ、原本ハッシュ一覧、根拠ソースの行番号付き抜粋を同梱。元プロジェクトは変更しておらず、修正版コードは含めていない。

技術上の補足根拠（2026-09-14確認、本文仕様の根拠は上記提出資料）：
```text
H2 Features / In-Memory Databases
https://www.h2database.com/html/features.html#in_memory_databases
Spring Framework / Exception Handling
https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html
Spring Security / CSRF
https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
```
