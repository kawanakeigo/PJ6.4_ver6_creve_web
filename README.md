# CreVe Web / PLAYPIT

PLAYPIT HP設計書に合わせた、CreVe公式サイト + PLAYPIT「羽花」機能のSpring Boot / Thymeleaf実装です。

## 技術構成

- Java 21
- Spring Boot 4.1.0
- Spring MVC / Thymeleaf
- Spring Data JPA
- Spring Security
- PostgreSQL想定、ローカル開発はH2
- HTML / CSS / JavaScript

## ディレクトリ構成

- `src/main/java/world/creve/controller`
  - `CreveController`, `PlaypitController`, `CreatorController`, `ArtworkController`, `MessageController`, `UkaController`, `LiveController`, `LandingPageController`, `AdminController`
- `src/main/java/world/creve/service`
  - `EventService`, `CreatorService`, `ArtworkService`, `MessageService`, `UkaService`, `LiveService`, `LandingPageService`, `AdminService`
- `src/main/java/world/creve/repository`
  - `EventRepository`, `CreatorRepository`, `ArtworkRepository`, `EventCreatorRepository`, `EventArtworkRepository`, `MessageRepository`, `NewsRepository`, `AdminRepository`, `LandingPageRepository`, `MediaFileRepository`
- `src/main/java/world/creve/entity`
  - `Event`, `Creator`, `Artwork`, `EventCreator`, `EventArtwork`, `Message`, `News`, `Admin`, `LandingPage`, `MediaFile`
- `src/main/java/world/creve/dto/request`
  - 投稿リクエストDTO
- `src/main/java/world/creve/dto/response`
  - 投稿、花びら、羽花レスポンスDTO
- `src/main/java/world/creve/security`
  - 管理画面認証、投稿レート制限
- `src/main/java/world/creve/validation`
  - 投稿バリデーション
- `src/main/java/world/creve/exception`
  - API例外レスポンス
- `src/main/java/world/creve/util`
  - セッションハッシュ、羽花配置計算
- `src/main/resources/templates`
  - CreVe、PLAYPIT、Live、LP、AdminのThymeleafテンプレート
- `src/main/resources/static`
  - CSS / JS / 画像アセット
- `src/main/resources/db/schema.sql`
  - 詳細設計に対応するDB定義メモ
- `src/main/resources/data/creators.csv`
  - 初期クリエイターデータ

## 主なURL

- `/`
- `/about`
- `/news`
- `/playpit`
- `/playpit/events`
- `/playpit/{eventId}`
- `/playpit/{eventId}/creators`
- `/playpit/{eventId}/creators/{creatorId}`
- `/playpit/{eventId}/artworks`
- `/playpit/{eventId}/artworks/{artworkId}`
- `/playpit/{eventId}/uka`
- `/playpit/archive`
- `/creators`
- `/creators/{creatorId}`
- `/live`
- `/live/{eventId}`
- `/lp/{lpId}`
- `/vr`
- `/contact`
- `/privacy`
- `/terms`
- `/admin/login`
- `/admin/messages`
- `/admin/events`
- `/admin/creators`
- `/admin/artworks`

## API

- `POST /api/messages`
  - CSRF必須
  - `201 Created`
  - `eventId`必須
  - `creatorId`または`artworkId`のどちらか必須
  - 本文300文字以内、表示名30文字以内
  - セッションハッシュで投稿制限
  - 公開可なら`PUBLISHED`、確認対象なら`PENDING`
- `GET /api/messages?eventId=...`
- `GET /api/playpit/{eventId}/uka`

## 起動

```bash
./mvnw spring-boot:run
```

ローカルURL:

```text
http://127.0.0.1:8080/playpit
```

## 管理画面

開発初期値:

```text
email: admin@creve.world
password: playpit-admin
```

本番・共有環境では以下の環境変数で上書きしてください。

```bash
CREVE_ADMIN_EMAIL=admin@example.com CREVE_ADMIN_PASSWORD=change-me ./mvnw spring-boot:run
```

## 検証

```bash
./mvnw test
```
