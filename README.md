# Spring Box API

Box API を使用してZIPファイルを解凍し、複数のCSVファイルを並列アップロードする Spring Boot アプリケーションです。

## 機能

- ZIPファイルの自動解凍
- 並列ファイルアップロード（並列度設定可能）
- アップロード対象ファイル数の制限
- 処理時間の測定とログ出力
- Cloud Run 対応

## 必要要件

- Java 17 以上
- Maven 3.6 以上
- Box Developer Token
- Docker（コンテナ実行の場合）

## 環境変数

| 変数名 | 説明 | デフォルト値 | 必須 |
|--------|------|------------|------|
| `BOX_DEVELOPER_TOKEN` | Box API 開発者トークン | - | ✓ |
| `BOX_FOLDER_ID` | アップロード先フォルダID | `0` (ルート) | |
| `ZIP_PATH` | 解凍対象のZIPファイルパス | `data/data_uniform.zip` | |
| `PARALLELISM` | 並列アップロード数 | `4` | |
| `FILE_COUNT` | アップロード対象ファイル数 | すべて | |

## セットアップ

### 1. Box Developer Token の取得

1. [Box Developer Console](https://app.box.com/developers/console) にアクセス
2. アプリケーションを作成
3. Configuration > Developer Token > Generate Developer Token
4. トークンをコピー（60分で期限切れ）

### 2. テストデータの作成

```powershell
.\create_test_data_random_fast.ps1
```

生成されるファイル:
- `data/data_uniform.zip` - 300KB × 100ファイル
- `data/data_variety_in_size.zip` - 100KB～1MB × 100ファイル

## ローカル実行

### Maven で実行

```bash
# 環境変数設定
export BOX_DEVELOPER_TOKEN=your_token_here
export BOX_FOLDER_ID=123456789
export ZIP_PATH=data/data_uniform.zip
export PARALLELISM=4
export FILE_COUNT=10

# 実行
mvn spring-boot:run -Dspring-boot.run.arguments="--BOX_DEVELOPER_TOKEN=XXXXXX --BOX_FOLDER_ID=XXXXXX --PARALLELISM=3"
```

## Docker 実行

### Docker ビルド

```bash
docker build -t spring-box-api .
```

### Docker 実行

```bash
docker run --rm \
  -e BOX_DEVELOPER_TOKEN=your_token \
  -e BOX_FOLDER_ID=123456789 \
  -e ZIP_PATH=data/data_uniform.zip \
  -e PARALLELISM=4 \
  -e FILE_COUNT=10 \
  spring-box-api
```

## Google Cloud Run デプロイ

### 1. Artifact Registry にプッシュ

```bash
# 変数設定
export PROJECT_ID=your-project-id
export REGION=asia-northeast1
export REPOSITORY=spring-box-api
export IMAGE_NAME=spring-box-api

# リポジトリ作成（初回のみ）
gcloud artifacts repositories create $REPOSITORY \
  --repository-format=docker \
  --location=$REGION \
  --description="Spring Box API repository"

# Docker 認証
gcloud auth configure-docker ${REGION}-docker.pkg.dev

# ビルド & プッシュ
docker build -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:latest .
docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:latest
```

### 2. Cloud Run デプロイ

```bash
gcloud run deploy spring-box-api \
  --image=${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:latest \
  --platform=managed \
  --region=${REGION} \
  --set-env-vars=BOX_DEVELOPER_TOKEN=your_token,BOX_FOLDER_ID=123456789,ZIP_PATH=data/data_uniform.zip,PARALLELISM=4,FILE_COUNT=10 \
  --allow-unauthenticated
```

## プロジェクト構成

```
spring-box-api/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/demo/
│       │       ├── BoxUploadApplication.java      # エントリーポイント
│       │       ├── application/
│       │       │   └── BoxUploadJob.java          # メインロジック
│       │       ├── service/
│       │       │   └── BoxUploadService.java      # アップロード処理
│       │       └── infrastructure/
│       │           └── BoxClient.java             # Box API ラッパー
│       └── resources/
│           └── application.properties              # 設定ファイル
├── data/                                           # テストデータ
│   ├── data_uniform.zip
│   └── data_variety_in_size.zip
├── Dockerfile                                      # Docker イメージ定義
├── pom.xml                                         # Maven 設定
├── create_test_data_random_fast.ps1               # テストデータ生成スクリプト
└── README.md
```

## 処理フロー

1. 環境変数から設定を読み込み
2. ZIPファイルを `extracted/` ディレクトリに解凍
3. CSVファイルのリストを取得・ソート
4. `FILE_COUNT` で対象ファイルを絞り込み
5. 指定された並列度でBox APIにアップロード
6. 処理時間をログ出力

## ログ出力例

```
========== Configuration ==========
ZIP_PATH: data/data_uniform.zip
PARALLELISM: 4
FILE_COUNT: 10
BOX_FOLDER_ID: 123456789
===================================
Extracting ZIP file: data/data_uniform.zip
Found 100 CSV files
Target file count: 10
Uploading 10 files with parallelism=4
Upload destination folder ID: 123456789
Uploading: file_001.csv
Successfully uploaded: file_001.csv
...
All uploads completed in 5432 ms (5.432 seconds)
```

## トラブルシューティング

### Box API 認証エラー

- Developer Token の有効期限を確認（60分）
- トークンを再生成して環境変数を更新

### ファイルが見つからない

```bash
# テストデータを再生成
.\create_test_data_random_fast.ps1
```

### 並列度が高すぎる

- `PARALLELISM` を減らす（推奨: 4-8）
- Box API のレート制限に注意

## 開発

### テストの実行

```bash
mvn test
```

### ビルド（テストスキップ）

```bash
mvn clean package -DskipTests
```

## ライセンス

MIT License

## 作成者

Your Name

## 参考リンク

- [Box Java SDK](https://github.com/box/box-java-sdk)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Google Cloud Run](https://cloud.google.com/run)