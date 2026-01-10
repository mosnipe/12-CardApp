package com.bizcard.note.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bizcard.note.R
import com.bizcard.note.data.BizCard
import com.bizcard.note.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    card: BizCard,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("名刺詳細") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (card.facePhotoUri != null) {
                // 顔写真表示（簡易実装）
                Text(
                    text = stringResource(R.string.face_photo),
                    style = MaterialTheme.typography.titleMedium
                )
                // 画像表示は後で実装
            }

            Text(
                text = card.name.ifEmpty { "（名前なし）" },
                style = MaterialTheme.typography.headlineMedium
            )

            if (card.company.isNotEmpty()) {
                Text(
                    text = card.company,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (card.email.isNotEmpty()) {
                Text(
                    text = "${stringResource(R.string.email)}: ${card.email}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (card.phone.isNotEmpty()) {
                Text(
                    text = "${stringResource(R.string.phone)}: ${card.phone}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (card.memo.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.memo),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = card.memo,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (card.meetingPlace.isNotEmpty()) {
                Text(
                    text = "${stringResource(R.string.meeting_place)}: ${card.meetingPlace}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (card.meetingDate != null) {
                Text(
                    text = "${stringResource(R.string.meeting_date)}: ${DateFormatter.formatDate(card.meetingDate)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (card.cardImageUri != null) {
                Text(
                    text = stringResource(R.string.card_image),
                    style = MaterialTheme.typography.titleMedium
                )
                // 名刺画像表示（簡易実装）
            }
        }
    }
}

