package com.bizcard.note.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizcard.note.data.BizCard
import com.bizcard.note.network.NotionRepository
import com.bizcard.note.util.CardInfo
import com.bizcard.note.util.OcrHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

class MainViewModel(
    private val notionRepository: NotionRepository
) : ViewModel() {
    private val ocrHelper = OcrHelper()

    private val _cards = MutableStateFlow<List<BizCard>>(emptyList())
    val cards: StateFlow<List<BizCard>> = _cards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun loadCards() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            notionRepository.getAllBizCards()
                .onSuccess { cardList ->
                    _cards.value = cardList
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "エラーが発生しました"
                }
            _isLoading.value = false
        }
    }

    fun processCardImage(uri: Uri, onOcrComplete: (CardInfo) -> Unit) {
        viewModelScope.launch {
            try {
                // URIからBitmapを取得（簡易実装）
                // 実際の実装では適切な方法でBitmapを取得する必要があります
                onOcrComplete(CardInfo("", "", "", "", ""))
            } catch (e: Exception) {
                _errorMessage.value = "OCR処理に失敗しました: ${e.message}"
            }
        }
    }

    fun processCardImageWithBitmap(bitmap: Bitmap, onOcrComplete: (CardInfo) -> Unit) {
        viewModelScope.launch {
            try {
                val cardInfo = ocrHelper.extractCardInfo(bitmap)
                onOcrComplete(cardInfo)
            } catch (e: Exception) {
                _errorMessage.value = "OCR処理に失敗しました: ${e.message}"
            }
        }
    }

    fun saveCard(card: BizCard) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _saveSuccess.value = false
            
            notionRepository.saveBizCard(card)
                .onSuccess {
                    _saveSuccess.value = true
                    // 一覧を再読み込み
                    loadCards()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "保存に失敗しました"
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }

    override fun onCleared() {
        super.onCleared()
        ocrHelper.close()
    }
}

