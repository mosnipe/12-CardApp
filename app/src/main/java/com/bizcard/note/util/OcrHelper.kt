package com.bizcard.note.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OcrHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()
        return result.text
    }

    suspend fun extractCardInfo(bitmap: Bitmap): CardInfo {
        val text = recognizeText(bitmap)
        val lines = text.lines().filter { it.isNotBlank() }
        
        // 簡単なパターンマッチングで情報を抽出
        var name = ""
        var company = ""
        var email = ""
        var phone = ""

        for (line in lines) {
            // メールアドレス検出
            if (line.contains("@") && email.isEmpty()) {
                email = line.trim()
            }
            // 電話番号検出（数字とハイフン/括弧を含む）
            else if (line.matches(Regex(".*[0-9]{2,4}[-()0-9]+.*")) && phone.isEmpty()) {
                phone = line.trim()
            }
            // 会社名（株式会社、有限会社などが含まれる）
            else if (line.contains("株式会社") || line.contains("有限会社") || 
                     line.contains("合同会社") || line.contains("Inc") || 
                     line.contains("Ltd") || line.contains("Corp")) {
                if (company.isEmpty()) {
                    company = line.trim()
                }
            }
            // 名前（最初の非空行を名前として扱う）
            else if (name.isEmpty() && line.length > 1 && !line.contains("@") && 
                     !line.matches(Regex(".*[0-9]{2,4}[-()0-9]+.*"))) {
                name = line.trim()
            }
        }

        return CardInfo(
            name = name,
            company = company,
            email = email,
            phone = phone,
            rawText = text
        )
    }

    fun close() {
        recognizer.close()
    }
}

data class CardInfo(
    val name: String,
    val company: String,
    val email: String,
    val phone: String,
    val rawText: String
)

