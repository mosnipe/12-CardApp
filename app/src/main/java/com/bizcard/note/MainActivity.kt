package com.bizcard.note

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bizcard.note.data.BizCard
import com.bizcard.note.network.NetworkModule
import com.bizcard.note.network.NotionRepository
import com.bizcard.note.ui.camera.CameraScreen
import com.bizcard.note.ui.detail.CardDetailScreen
import com.bizcard.note.ui.edit.EditCardScreen
import com.bizcard.note.ui.home.HomeScreen
import com.bizcard.note.ui.list.CardListScreen
import com.bizcard.note.ui.theme.BizCardNoteTheme
import com.bizcard.note.ui.viewmodel.MainViewModel
import com.bizcard.note.util.CardInfo
import com.bizcard.note.BuildConfig
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Notion API KeyとDatabase ID (BuildConfigから読み込み)
        val notionApiKey = BuildConfig.NOTION_API_KEY
        val notionDatabaseId = BuildConfig.NOTION_DATABASE_ID

        val notionApi = NetworkModule.createNotionApi()
        val notionRepository = NotionRepository(notionApi, notionDatabaseId, notionApiKey)

        setContent {
            BizCardNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: MainViewModel = viewModel {
                        MainViewModel(notionRepository)
                    }

                    val errorMessage by viewModel.errorMessage.collectAsState()
                    val saveSuccess by viewModel.saveSuccess.collectAsState()

                    LaunchedEffect(errorMessage) {
                        errorMessage?.let {
                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }

                    LaunchedEffect(saveSuccess) {
                        if (saveSuccess) {
                            Toast.makeText(this@MainActivity, "保存しました", Toast.LENGTH_SHORT).show()
                            viewModel.clearSaveSuccess()
                            navController.popBackStack("home", inclusive = false)
                        }
                    }

                    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
                    var ocrResult by remember { mutableStateOf<CardInfo?>(null) }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onRegisterClick = {
                                    navController.navigate("camera")
                                },
                                onViewListClick = {
                                    viewModel.loadCards()
                                    navController.navigate("list")
                                }
                            )
                        }

                        composable("camera") {
                            CameraScreen(
                                onImageCaptured = { uri ->
                                    capturedImageUri = uri
                                    // OCR処理
                                    try {
                                        val inputStream: InputStream? = contentResolver.openInputStream(uri)
                                        val bitmap = BitmapFactory.decodeStream(inputStream)
                                        inputStream?.close()
                                        
                                        if (bitmap != null) {
                                            viewModel.processCardImageWithBitmap(bitmap) { cardInfo ->
                                                ocrResult = cardInfo
                                                navController.navigate("edit")
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "画像の読み込みに失敗しました",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "エラー: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("edit") {
                            EditCardScreen(
                                cardInfo = ocrResult,
                                cardImageUri = capturedImageUri,
                                onSave = { card ->
                                    viewModel.saveCard(card)
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("list") {
                            val cards by viewModel.cards.collectAsState()
                            val isLoading by viewModel.isLoading.collectAsState()

                            CardListScreen(
                                cards = cards,
                                isLoading = isLoading,
                                onCardClick = { card ->
                                    navController.navigate("detail/${card.id}")
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("detail/{cardId}") { backStackEntry ->
                            val cardId = backStackEntry.arguments?.getString("cardId")
                            val card = viewModel.cards.value.find { it.id == cardId }
                            
                            if (card != null) {
                                CardDetailScreen(
                                    card = card,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            } else {
                                // カードが見つからない場合
                                LaunchedEffect(Unit) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

