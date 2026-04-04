# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

自作PC構成・総額シミュレーションWebアプリ（https://www.pcbuilding.link/）。
Spring Boot + H2 + Thymeleaf構成。kakaku.comから価格データをスクレイピングし、Gemini AIによる構成レビュー機能を持つ。

## Commands

```bash
# ビルド
./mvnw package

# テスト実行
./mvnw test

# 特定テストのみ実行
./mvnw test -Dtest=ApiResponseControllerTest

# ローカル起動（環境変数が必要）
export SPRING_DATASOURCE_URL=your_datasource_url_here
export SPRING_DATASOURCE_USERNAME=your_db_username_here
export SPRING_DATASOURCE_PASSWORD=your_db_password_here
export GEMINI_API_KEY=your_gemini_api_key_here
./mvnw spring-boot:run
```

## 必要な環境変数

| 変数名 | 用途 |
|--------|------|
| `SPRING_DATASOURCE_URL` | H2 DB接続URL |
| `SPRING_DATASOURCE_USERNAME` | DBユーザー名 |
| `SPRING_DATASOURCE_PASSWORD` | DBパスワード |
| `GEMINI_API_KEY` | Gemini AI API キー |

本番環境ではAWS Systems Manager Parameter Storeから取得（`buildspec.yml`参照）。

## Architecture

### レイヤー構成

```
presentation/
  controller/GeminiReviewController  - POST /api/gemini/review
  data/ReviewRequest|Response|...    - リクエスト/レスポンスDTO
HomeController                       - MVC: 全ページルーティング + アセンブリ管理
ApiResponseController                - REST: GET /api/devicelist, /api/update
DeviceInfoDao                        - JdbcTemplate によるデータアクセス
KakakuClient                        - kakaku.com スクレイピング
domain/gemini/GeminiServiceImpl      - Gemini API呼び出し（gemini-2.5-flash）
```

### データフロー

1. **価格データ更新**: 起動時 + 毎日4:00 AMにTimerTaskで`KakakuClient`がkakaku.comをスクレイピング → `devices`テーブルに格納
   - 週1回フルスキャン（165時間ごと）、それ以外は差分更新
2. **ページ表示**: `HomeController`が`DeviceInfoDao`からパーツ一覧を取得 → Thymeleaf `index.html`へ
3. **構成管理**: ゲストID（32文字UUID、クライアント保持）で`assemblies`テーブルに紐付け
4. **保存/共有**: `/save` POST → `savehead`+`savelist`に保存 → `/rec/{32文字ID}` でURL共有

### DBスキーマ（H2）

- `devices` - パーツカタログ（`device`カラムでパーツ種別を区別）
- `assemblies` - ユーザーの選択パーツ（guestidで紐付け）
- `savehead` / `savelist` - 保存済み構成（saveIDがそのまま公開URLになる）
- `systemvals` - 最終価格更新日時

### 重要な設計上の注意点

- **ユーザー認証なし**: ゲストIDによる識別のみ。`guestId.length() == 32` でバリデーション
- **`index.html`がメインテンプレート**: 多くのページは`index.html`を使い、Thymeleafの`th:if`で表示切替するが、`/policy_ja`や`/policy_en`など一部ルートは専用テンプレートを返す
- **パーツ種別**: `deviceTypeList`に定義された19種類の英語キー（`pccase`, `motherboard`等）で管理。日本語名は`deviceTypeJp`マップで変換
- **`HomeController`内のinner records**: `DeviceInfo`, `UserAssem`, `SaveHead`等のデータクラスがHomeControllerのinner recordsとして定義されており、`DeviceInfoDao`からも参照される

### デプロイ

AWS CodeBuild（`buildspec.yml`）→ S3 → CodeDeploy（`appspec.yml` + `update.sh`）
