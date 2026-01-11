# Android アイコンリサイズスクリプト
# 使用方法: .\resize-icon.ps1 -SourceImage "icon.png"

param(
    [Parameter(Mandatory=$true)]
    [string]$SourceImage
)

# ImageMagickがインストールされているか確認
$magickPath = Get-Command magick -ErrorAction SilentlyContinue
if (-not $magickPath) {
    Write-Host "エラー: ImageMagickがインストールされていません。" -ForegroundColor Red
    Write-Host "ImageMagickをインストールするか、オンラインツールを使用してください。" -ForegroundColor Yellow
    Write-Host "ダウンロード: https://imagemagick.org/script/download.php" -ForegroundColor Cyan
    exit 1
}

# 元画像の存在確認
if (-not (Test-Path $SourceImage)) {
    Write-Host "エラー: 元画像が見つかりません: $SourceImage" -ForegroundColor Red
    exit 1
}

# 各密度のサイズ定義
$sizes = @{
    "mdpi" = 108
    "hdpi" = 162
    "xhdpi" = 216
    "xxhdpi" = 324
    "xxxhdpi" = 432
}

Write-Host "アイコンをリサイズしています..." -ForegroundColor Green

foreach ($density in $sizes.Keys) {
    $size = $sizes[$density]
    $outputDir = "app\src\main\res\mipmap-$density"
    $outputFile = "$outputDir\ic_launcher_foreground.png"
    
    # ディレクトリが存在しない場合は作成
    if (-not (Test-Path $outputDir)) {
        New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
        Write-Host "ディレクトリを作成: $outputDir" -ForegroundColor Yellow
    }
    
    # 画像をリサイズ
    try {
        magick $SourceImage -resize "${size}x${size}" $outputFile
        Write-Host "✓ $density ($size×${size}px): $outputFile" -ForegroundColor Green
    } catch {
        Write-Host "✗ $density のリサイズに失敗: $_" -ForegroundColor Red
    }
}

Write-Host "`n完了しました！" -ForegroundColor Green
Write-Host "リサイズした画像は app\src\main\res\mipmap-* に配置されました。" -ForegroundColor Cyan
