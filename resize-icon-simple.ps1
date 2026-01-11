# Android アイコンリサイズスクリプト（.NET使用版）
# 使用方法: .\resize-icon-simple.ps1 -SourceImage "icon.png"

param(
    [Parameter(Mandatory=$true)]
    [string]$SourceImage
)

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

# System.Drawingを使用
Add-Type -AssemblyName System.Drawing

try {
    # 元画像を読み込み
    $sourcePath = (Resolve-Path $SourceImage).Path
    $sourceImage = [System.Drawing.Bitmap]::FromFile($sourcePath)
    
    foreach ($density in $sizes.Keys) {
        $size = $sizes[$density]
        $outputDir = "app\src\main\res\mipmap-$density"
        $outputFile = "$outputDir\ic_launcher_foreground.png"
        
        # ディレクトリが存在しない場合は作成
        if (-not (Test-Path $outputDir)) {
            New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
            Write-Host "ディレクトリを作成: $outputDir" -ForegroundColor Yellow
        }
        
        # リサイズした画像を作成
        $resizedImage = New-Object System.Drawing.Bitmap($size, $size)
        $graphics = [System.Drawing.Graphics]::FromImage($resizedImage)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        
        # 画像をリサイズして描画
        $graphics.DrawImage($sourceImage, 0, 0, $size, $size)
        
        # PNG形式で保存
        $resizedImage.Save($outputFile, [System.Drawing.Imaging.ImageFormat]::Png)
        
        # リソースを解放
        $graphics.Dispose()
        $resizedImage.Dispose()
        
        Write-Host "✓ $density ($size×${size}px): $outputFile" -ForegroundColor Green
    }
    
    # 元画像を解放
    $sourceImage.Dispose()
    
    Write-Host "`n完了しました！" -ForegroundColor Green
    Write-Host "リサイズした画像は app\src\main\res\mipmap-* に配置されました。" -ForegroundColor Cyan
    Write-Host "`n次のステップ:" -ForegroundColor Yellow
    Write-Host "1. Android Studioで Clean Project を実行" -ForegroundColor Cyan
    Write-Host "2. Rebuild Project を実行" -ForegroundColor Cyan
    Write-Host "3. アプリをアンインストールしてから再インストール" -ForegroundColor Cyan
    
} catch {
    Write-Host "エラー: $_" -ForegroundColor Red
    exit 1
}
