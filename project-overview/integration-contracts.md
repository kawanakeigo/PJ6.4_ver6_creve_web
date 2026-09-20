# PJ間連携契約

## ローカル入口

| PJ | URL | Cookie | DB |
|---|---|---|---|
| CreVe | `http://localhost:8080` | `CREVE_SESSION` | `creve` |
| PLAYPIT | `http://localhost:8081/playpit` | `PLAYPIT_SESSION` | `playpit` |
| NLJ | `http://localhost:8082/index.html` | `NLJ_ADMIN_SESSION` | `nlj` |

## 境界

- PJ間でJavaクラス、Entity、Repository、DB接続、セッションを共有しない。
- CreVeのPJ入口は`project_link`に保存した公開URLを表示する。
- PLAYPITからCreVeへの公式導線は`CREVE_PUBLIC_BASE_URL`を使う。
- NLJからCreVeへの公式導線は`CREVE_PUBLIC_BASE_URL`を使う。
- PLAYPITのQRは`PLAYPIT_PUBLIC_BASE_URL`を基準に自PJの公開URLだけを生成する。
- 他PJの取得失敗を「該当なし」や成功へ読み替えない。

## Cookieと認証

- Cookie名をPJ別にし、localhostのport間で同名Cookieを上書きしない。
- PLAYPIT管理認証とCreVe会員認証を共有しない。
- NLJのLINE ID tokenは公式verify endpointで検証し、生tokenをDBやログへ保存しない。

## 未接続契約

NLJの既存DDL対応表は`nlj-platform-persistence/docs/schema-mapping.md`を正本とする。実列とV001-V015を受領するまで、`ExistingSchemaAdapter`は投票永続化を503で停止する。
