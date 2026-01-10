# BizCard Note - 名刺管理Androidアプリ

紙の名刺を撮影してデータ化し、人物の顔・会話内容・会った文脈と一緒に保存することで、後から「この人誰だっけ？」をすぐに思い出せるようにするAndroidアプリです。

## 機能

- 📸 名刺の撮影
- 🔍 OCR（Google ML Kit）による自動テキスト認識
- ✏️ OCR結果の確認・編集
- 💾 Notion APIへの保存
- 📋 名刺一覧表示
- 📄 名刺詳細表示

## 技術スタック

- **言語**: Kotlin
- **UI**: Jetpack Compose
- **OCR**: Google ML Kit Text Recognition
- **カメラ**: CameraX
- **API通信**: Retrofit + OkHttp
- **データ保存**: Notion API

## セットアップ

### 1. Notion APIの設定

1. [Notion Developers](https://www.notion.so/my-integrations)にアクセス
2. 「+ New integration」をクリック
3. 統合名を入力（例: "BizCard Note"）
4. ワークスペースを選択
5. 「Submit」をクリック
6. 「Internal Integration Token」をコピー（これがAPI Keyです）

### 2. Notionデータベースの作成

1. Notionで新しいページを作成
2. 「/database」と入力してデータベースを追加
3. 以下のプロパティを作成：

| プロパティ名 | 型            |
| ------ | ------------ |
| 名前     | Title        |
| 会社名    | Text         |
| メール    | Email        |
| 電話番号   | Text         |
| メモ     | Text         |
| 会った場所  | Text         |
| 会った日   | Date         |
| 名刺画像   | Files        |
| 顔写真    | Files        |
| 登録日    | Created time |

4. データベースのURLからDatabase IDを取得
   - URL例: `https://www.notion.so/workspace/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`
   - `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx` の部分がDatabase IDです

### 3. アプリの設定

`app/src/main/java/com/bizcard/note/MainActivity.kt` を開き、以下の値を設定：

```kotlin
val notionApiKey = "your_notion_api_key_here"  // ステップ1で取得したAPI Key
val notionDatabaseId = "your_notion_database_id_here"  // ステップ2で取得したDatabase ID
```

### 4. ビルドと実行

1. Android Studioでプロジェクトを開く
2. 必要な依存関係が自動的にダウンロードされます
3. 実機またはエミュレーターで実行

## 使用方法

1. **名刺を登録する**
   - ホーム画面で「名刺を登録する」をタップ
   - カメラで名刺を撮影
   - OCR結果を確認・編集
   - 必要に応じてメモ、会った場所、会った日、顔写真を追加
   - 「保存」をタップ

2. **名刺一覧を見る**
   - ホーム画面で「名刺一覧を見る」をタップ
   - 登録済みの名刺一覧が表示されます
   - 名刺をタップすると詳細画面が開きます

## 注意事項

- このアプリは常にオンライン前提です（オフライン対応なし）
- Notion APIの無料プランにはレート制限があります
- 画像のアップロード機能は簡易実装のため、実際のNotionへの画像アップロードには追加実装が必要です

## ライセンス

このプロジェクトは個人利用を想定しています。

