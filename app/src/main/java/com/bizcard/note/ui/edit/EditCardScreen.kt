package com.bizcard.note.ui.edit

import android.app.DatePickerDialog
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bizcard.note.R
import com.bizcard.note.data.BizCard
import com.bizcard.note.util.CardInfo
import com.bizcard.note.util.DateFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    cardInfo: CardInfo?,
    cardImageUri: Uri?,
    onSave: (BizCard) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(cardInfo?.name ?: "") }
    var company by remember { mutableStateOf(cardInfo?.company ?: "") }
    var email by remember { mutableStateOf(cardInfo?.email ?: "") }
    var phone by remember { mutableStateOf(cardInfo?.phone ?: "") }
    var memo by remember { mutableStateOf("") }
    var meetingPlace by remember { mutableStateOf("") }
    var meetingDate by remember { mutableStateOf<Date?>(null) }
    var facePhotoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("名刺情報を確認・編集") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val card = BizCard(
                        name = name,
                        company = company,
                        email = email,
                        phone = phone,
                        memo = memo,
                        meetingPlace = meetingPlace,
                        meetingDate = meetingDate,
                        cardImageUri = cardImageUri?.toString(),
                        facePhotoUri = facePhotoUri?.toString()
                    )
                    onSave(card)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.save))
            }
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text(stringResource(R.string.company)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                label = { Text(stringResource(R.string.memo)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = meetingPlace,
                onValueChange = { meetingPlace = it },
                label = { Text(stringResource(R.string.meeting_place)) },
                modifier = Modifier.fillMaxWidth()
            )

            // 会った日（カレンダーピッカー）
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = DateFormatter.formatDate(meetingDate),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.meeting_date)) },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        meetingDate?.let {
                            calendar.time = it
                        }
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                                meetingDate = selectedCalendar.time
                            },
                            year,
                            month,
                            day
                        ).show()
                    }
                ) {
                    Text("選択")
                }
            }

            // 顔写真（簡易実装）
            Text(
                text = stringResource(R.string.face_photo),
                style = MaterialTheme.typography.labelLarge
            )
            Button(
                onClick = {
                    // カメラまたはギャラリーから選択（簡易実装ではスキップ）
                }
            ) {
                Text("顔写真を追加")
            }
        }
    }
}

