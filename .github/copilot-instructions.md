# Copilot Instructions

## Project Overview

自作PC構成・総額シミュレーションWebアプリ（https://www.pcbuilding.link/）。
Spring Boot 2.6.7 + Java 17 + H2 + Thymeleaf 構成。
kakaku.com から価格データをスクレイピングし、Gemini AI による構成レビュー機能と Firebase 認証を持つ。

**現在 Java → Kotlin への段階的移行を進めている。**

## Architecture

```
presentation/
  controller/GeminiReviewController    POST /api/gemini/review
  controller/MigrationController       レガシー guestId データ → Firestore（Firebase UID 配下）への移行
  controller/SessionController         Firebase idToken 検証 → HttpSession に firebaseUid を保持
  controller/FirebaseWebConfigController Firebase 設定配信
  data/ReviewRequest|Response|...      リクエスト/レスポンスDTO
HomeController                         MVC: 全ページルーティング + アセンブリ管理
ApiResponseController                  REST: GET /api/devicelist, /api/update
DeviceInfoDao                          JdbcTemplate によるデータアクセス
KakakuClient                          kakaku.com スクレイピング
domain/gemini/GeminiServiceImpl        Gemini API呼び出し（gemini-2.5-flash）
domain/firestore/FirestoreServiceImpl  Firestore によるデータ永続化
domain/auth/FirebaseIdTokenVerifier    Firebase ID トークン検証
domain/migration/MigrationServiceImpl ゲストIDとFirebase UIDのマッピング
```

## Database Schema (H2)

- `devices` — パーツカタログ（`device` カラムでパーツ種別を区別、19種類）
- `assemblies` — **レガシー/移行用テーブル**。旧ゲスト構成の移行処理でのみ参照。**現在のユーザー選択パーツのソースオブトゥルースではない**
- `savehead` / `savelist` — 保存済み構成（`saveid` がそのまま公開URLになる）
- `systemvals` — 最終価格更新日時
- `uid_mapping` — Firebase UID と guestId のマッピング（移行用）

**Note:** ログインユーザーの構成データは Firestore `users/{firebaseUid}/assemblies` に保存される。新機能や保守でユーザー選択パーツの永続化先として H2 `assemblies` を前提にしないこと。

## Key Design Decisions (Do NOT flag these as issues)

- **認証/セッション設計**: Firebase Anonymous Auth を使用し、サーバー側 `HttpSession` に `firebaseUid` を保持して識別する。`guestId` は移行処理（`POST /api/migrate`）でのみ参照されるレガシー識別子であり、使用時のバリデーションは `matches("[0-9a-fA-F]{32}")` の正規表現で行う。
- **H2 はプロダクション DB**: テスト用ではなく本番でも H2 を使用。インメモリではなくファイル永続化。
- **TimerTask によるスケジューリング**: 起動時 + 毎日4:00 AM にスクレイピング実行。Spring Scheduler ではなく TimerTask を意図的に使用。
- **価格更新間隔**: 週1回フルスキャン（165時間ごと）、それ以外は差分更新。マジックナンバー 165 は意図的な設定値。
- **`index.html` がメインテンプレート**: 多くのページは `index.html` を使い、`th:if` で表示切替する設計。
- **saveId が公開URL**: `/rec/{32文字ID}` の形式で共有URL として機能する。セキュリティ上の問題ではない。
- **`savelist` の price カラム**: 保存時点の価格を記録するスナップショット。`devices.price` と一致しなくてよい。

## Review Focus Areas

### 高優先度
- **SQLインジェクション**: `JdbcTemplate` のクエリに文字列結合が含まれていないか確認。パラメータは必ず `?` プレースホルダーを使うこと。
- **Thymeleaf XSS**: `th:utext` の使用箇所。ユーザー入力が含まれる場合は `th:text` を使うこと。
- **Firebase IDトークン検証**: uid を直接信頼せず、必ず idToken を検証してから UID を取得すること。実装は `IdTokenVerifier` / `FirebaseIdTokenVerifier` の利用を推奨するが、`FirebaseAuth.getInstance().verifyIdToken(...)` による検証も許容する（`MigrationServiceImpl` 参照）。
- **guestId バリデーション**: `POST /api/migrate` など guestId を受け取るエンドポイントで `matches("[0-9a-fA-F]{32}")` の正規表現チェックが行われているか。新規エンドポイントは `HttpSession.firebaseUid` を使うこと（guestId は移行専用）。
- **Firestore 例外処理**: Firestore が失敗したとき、H2 フォールバックではなく 503 を返すこと（既修正済み設計）。

### 中優先度
- **Null 安全性**: Java コードでは `Optional` を適切に使うこと。Kotlin コードでは `?.` / `!!` の適切な使い分け。
- **スクレイピングのエラーハンドリング**: `KakakuClient` の HTTP エラー・パース失敗時に処理が継続できること。
- **Gemini API のタイムアウト**: AI 呼び出しにタイムアウトが設定されているか。
- **環境変数の直接参照**: `System.getenv()` を直接使わず `application.properties` 経由にすること。

### Kotlin 移行時の追加確認事項
- Java の `null` を返すメソッドを Kotlin から呼ぶ際のプラットフォーム型（`T!`）の扱い。
- Java 側の `record` と相互運用する必要がある場合にのみ、Kotlin の `data class` で `@JvmRecord` の適用可否を検討すること。
- Spring Bean の `@Autowired` は Kotlin では constructor injection を優先すること。
- Kotlin の `object` / `companion object` を使う場合、Java から `static` 相当として呼び出す必要がある箇所では JVM 相互運用性を確認すること。
- coroutines を導入する場合は `@Async` との混在を避けること。

## What NOT to flag

- `@SuppressWarnings("unchecked")` が `JdbcTemplate.query()` 周辺にある場合：型消去による警告抑制で意図的。
- `H2ConsoleAutoConfiguration` の有効化：開発・本番共通で H2 console を使う設計。
- `spring.datasource.initialization-mode=always`：スキーマ初期化を毎回行う設計。
- `TimerTask` の `run()` メソッド内での例外キャッチ：スケジューラーを止めないための設計。
- `OkHttp` と `okio` の明示的な依存宣言：`firebase-admin` の推移的依存バージョンを上書きするため意図的。

## Code Conventions

- パッケージ: `jp.developer.bbee.pcassem`
- デバイス種別キー: `pccase`, `motherboard`, `cpu` など英語キー（`deviceTypeList` に定義）
- guestId: 32文字 UUID（ハイフンなし）
- saveId: 32文字 UUID（ハイフンなし）、公開URLに使用
- タイムスタンプ: `LocalDateTime`、DB は H2 の `TIMESTAMP` 型
