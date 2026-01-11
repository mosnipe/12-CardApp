# Android アイコン作成ガイド

## アダプティブアイコンの要件

Android 8.0（API 26）以降では、アダプティブアイコンを使用します。アダプティブアイコンは、前景画像と背景で構成されます。

### 基本要件

1. **前景画像（Foreground）**
   - サイズ: **1024×1024px** の正方形
   - 形式: PNG（透明背景可）
   - 安全領域: 中央の **66%**（約 676×676px）に重要な要素を配置
   - 外側の 17% は切り取られる可能性があるため、重要な要素は中央に配置

2. **背景（Background）**
   - サイズ: **1024×1024px** の正方形
   - 形式: PNG または 色（XMLで定義）
   - 現在のプロジェクトでは `@color/ic_launcher_background` を使用

### 各密度でのサイズ

アダプティブアイコンの前景画像は、以下のサイズにリサイズされます：

| 密度 | フォルダ | サイズ（px） |
|------|----------|--------------|
| mdpi | `mipmap-mdpi` | 108×108 |
| hdpi | `mipmap-hdpi` | 162×162 |
| xhdpi | `mipmap-xhdpi` | 216×216 |
| xxhdpi | `mipmap-xxhdpi` | 324×324 |
| xxxhdpi | `mipmap-xxxhdpi` | 432×432 |

## アイコン作成の手順

### 1. 元画像の準備

1. **1024×1024px** の正方形画像を作成
2. 重要な要素は中央の **676×676px** 以内に配置
3. 背景は透明または単色
4. PNG形式で保存

### 2. 画像のリサイズ

各密度用のサイズにリサイズする必要があります。以下の方法があります：

#### 方法A: ImageMagickを使用（推奨）

```powershell
# ImageMagickをインストール後
$sourceImage = "icon-foreground.png"  # 1024x1024pxの元画像
$sizes = @{
    "mdpi" = 108
    "hdpi" = 162
    "xhdpi" = 216
    "xxhdpi" = 324
    "xxxhdpi" = 432
}

foreach ($density in $sizes.Keys) {
    $size = $sizes[$density]
    $outputDir = "app\src\main\res\mipmap-$density"
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
    magick $sourceImage -resize "${size}x${size}" "$outputDir\ic_launcher_foreground.png"
}
```

#### 方法B: オンラインツールを使用

- [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html)
- [App Icon Generator](https://appicon.co/)

#### 方法C: Photoshop/GIMPで手動リサイズ

各密度のサイズに手動でリサイズして保存

### 3. 背景色の設定

`app/src/main/res/values/colors.xml` で背景色を設定：

```xml
<color name="ic_launcher_background">#FFFFFF</color>
```

### 4. ファイルの配置

リサイズした画像を以下の場所に配置：

```
app/src/main/res/
├── mipmap-mdpi/
│   └── ic_launcher_foreground.png (108×108)
├── mipmap-hdpi/
│   └── ic_launcher_foreground.png (162×162)
├── mipmap-xhdpi/
│   └── ic_launcher_foreground.png (216×216)
├── mipmap-xxhdpi/
│   └── ic_launcher_foreground.png (324×324)
└── mipmap-xxxhdpi/
    └── ic_launcher_foreground.png (432×432)
```

## デザインのコツ

1. **安全領域を意識**: 重要な要素は中央の 66% に配置
2. **シンプルに**: 小さなサイズでも認識できるデザイン
3. **コントラスト**: 背景色とのコントラストを確保
4. **角丸対応**: システムが自動的に角丸や円形に変換するため、角が切れても問題ないデザイン

## トラブルシューティング

### アイコンが大きすぎる/小さすぎる

- 安全領域（中央 66%）を意識してデザイン
- 各密度のサイズが正しいか確認

### アイコンが切れる

- 重要な要素を中央の 676×676px 以内に配置
- 外側の 17% は切り取られる可能性がある

### 背景色が表示されない

- `colors.xml` の `ic_launcher_background` を確認
- `ic_launcher.xml` の設定を確認
