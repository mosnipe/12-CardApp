package com.bizcard.note.network

import android.util.Log
import com.bizcard.note.data.BizCard
import java.text.SimpleDateFormat
import java.util.*

class NotionRepository(
    private val api: NotionApi,
    private val databaseId: String,
    private val apiKey: String
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
    
    // データベース情報から取得した実際のID（ハイフン付き）を保存
    private var verifiedDatabaseId: String? = null


    // データベース情報を取得して、データベースIDが正しいか確認
    suspend fun verifyDatabase(): Result<String> {
        return try {
            val response = api.getDatabase(
                databaseId = databaseId,
                authorization = "Bearer $apiKey",
                notionVersion = "2022-06-28"
            )
            
            if (response.isSuccessful && response.body() != null) {
                val db = response.body()!!
                // データベース情報から取得したID（ハイフン付き）を保存
                verifiedDatabaseId = db.id
                Log.d("NotionRepository", "Database verified: ${db.id}")
                Log.d("NotionRepository", "Original database ID: $databaseId")
                Log.d("NotionRepository", "Verified database ID: ${db.id}")
                
                // プロパティ名をログに出力
                if (db.properties != null) {
                    Log.d("NotionRepository", "=== Database Properties ===")
                    db.properties.forEach { (key, value) ->
                        Log.d("NotionRepository", "Property: '$key' (type: ${value.type})")
                    }
                } else {
                    Log.w("NotionRepository", "Properties not found in database response")
                }
                
                Result.success(db.id)
            } else {
                val errorBody = try {
                    val errorBody = response.errorBody()
                    if (errorBody != null) {
                        val source = errorBody.source()
                        source.request(java.lang.Long.MAX_VALUE)
                        val errorBodyString = source.buffer().clone().readUtf8()
                        source.close()
                        errorBodyString
                    } else {
                        "No error body"
                    }
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }
                Log.e("NotionRepository", "Database verification failed: $errorBody")
                Result.failure(Exception("Database verification failed: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("NotionRepository", "Exception while verifying database", e)
            Result.failure(e)
        }
    }

    suspend fun saveBizCard(card: BizCard): Result<String> {
        return try {
            // データベースIDを取得（検証済みのIDがあればそれを使用、なければ元のIDを使用）
            val dbId = verifiedDatabaseId ?: databaseId
            
            // 検証済みのIDがない場合は、データベース情報を取得してIDを検証
            if (verifiedDatabaseId == null) {
                verifyDatabase().onSuccess { id ->
                    verifiedDatabaseId = id
                    Log.d("NotionRepository", "Database ID verified: $id")
                }
                // 再取得
                val dbIdAfterVerify = verifiedDatabaseId ?: databaseId
                Log.d("NotionRepository", "Using database ID: $dbIdAfterVerify")
            }
            
            val properties = mutableMapOf<String, Property>()

            // 名前（Title）- NotionではTitleプロパティは必須なので、空の場合はデフォルト値を設定
            val name = if (card.name.isNotEmpty()) card.name else "（名前なし）"
            properties["名前"] = Property(
                title = listOf(TitleContent(TextContent(name)))
            )

            // 会社名
            if (card.company.isNotEmpty()) {
                properties["会社名"] = Property(
                    rich_text = listOf(RichTextContent(TextContent(card.company)))
                )
            }

            // メール（プロパティ名を確認してから使用）
            if (card.email.isNotEmpty()) {
                // まずスペースなしを試す
                properties["メール"] = Property(email = card.email)
            }

            // 電話番号（プロパティ名を確認してから使用）
            if (card.phone.isNotEmpty()) {
                // まずスペースなしを試す
                properties["電話番号"] = Property(
                    rich_text = listOf(RichTextContent(TextContent(card.phone)))
                )
            }

            // メモ
            if (card.memo.isNotEmpty()) {
                properties["メモ"] = Property(
                    rich_text = listOf(RichTextContent(TextContent(card.memo)))
                )
            }

            // 会った場所
            if (card.meetingPlace.isNotEmpty()) {
                properties["会った場所"] = Property(
                    rich_text = listOf(RichTextContent(TextContent(card.meetingPlace)))
                )
            }

            // 会った日（プロパティ名を確認してから使用）
            if (card.meetingDate != null) {
                // まずスペースなしを試す
                properties["会った日"] = Property(
                    date = DateContent(dateFormat.format(card.meetingDate))
                )
            }

            // 名刺画像（URL形式で保存、実際のアップロードは別途必要）
            // 注意: ローカルファイルのURIはNotion APIでは直接使用できないため、
            // 実際の実装では画像をアップロードしてURLを取得する必要があります
            // 現在は空のままにしておきます
            // if (card.cardImageUri != null) {
            //     properties["名刺画像"] = Property(
            //         files = listOf(FileContent(external = ExternalFile(card.cardImageUri)))
            //     )
            // }

            // 最終的なデータベースIDを決定（検証済みのIDがあればそれを使用）
            val finalDbId = verifiedDatabaseId ?: databaseId
            
            val request = CreatePageRequest(
                parent = Parent(database_id = finalDbId),
                properties = properties
            )

            // デバッグ用：リクエストボディをJSON形式でログ出力
            val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
            val requestJson = gson.toJson(request)
            Log.e("NotionRepository", "=== リクエスト情報 ===")
            Log.e("NotionRepository", "Original Database ID: $databaseId")
            Log.e("NotionRepository", "Verified Database ID: $verifiedDatabaseId")
            Log.e("NotionRepository", "Final Database ID: $finalDbId")
            Log.e("NotionRepository", "Database ID length: ${finalDbId.length}")
            Log.e("NotionRepository", "Request URL will be: https://api.notion.com/v1/pages")
            Log.e("NotionRepository", "Request JSON:\n$requestJson")
            Log.e("NotionRepository", "Properties keys: ${properties.keys.joinToString()}")

            val response = api.createPage(
                authorization = "Bearer $apiKey",
                notionVersion = "2022-06-28",
                body = request
            )
            
            Log.e("NotionRepository", "Response code: ${response.code()}")
            Log.e("NotionRepository", "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                Log.d("NotionRepository", "Successfully saved card: ${response.body()!!.id}")
                Result.success(response.body()!!.id)
            } else {
                // エラーレスポンスの詳細を取得
                val errorBodyString = try {
                    val errorBody = response.errorBody()
                    if (errorBody != null) {
                        val source = errorBody.source()
                        source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body
                        val errorBodyString = source.buffer().clone().readUtf8()
                        source.close()
                        errorBodyString
                    } else {
                        "No error body"
                    }
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }
                
                val errorMessage = "Failed to save (${response.code()}): $errorBodyString"
                Log.e("NotionRepository", "Save failed: $errorMessage")
                Log.e("NotionRepository", "Response headers: ${response.headers()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("NotionRepository", "Exception while saving card", e)
            Result.failure(e)
        }
    }

    suspend fun getAllBizCards(): Result<List<BizCard>> {
        return try {
            // 会った日でソート（降順：新しい順）
            val request = QueryDatabaseRequest(
                sorts = listOf(Sort(property = "会った日", direction = "descending"))
            )

            // データベースIDを取得（検証済みのIDがあればそれを使用、なければ元のIDを使用）
            val dbId = verifiedDatabaseId ?: databaseId
            
            // 検証済みのIDがない場合は、データベース情報を取得してIDを検証
            if (verifiedDatabaseId == null) {
                verifyDatabase().onSuccess { id ->
                    verifiedDatabaseId = id
                    Log.d("NotionRepository", "Database ID verified: $id")
                }
            }
            
            val finalDbId = verifiedDatabaseId ?: databaseId
            
            val response = api.queryDatabase(
                databaseId = finalDbId,
                authorization = "Bearer $apiKey",
                notionVersion = "2022-06-28",
                body = request
            )

            if (response.isSuccessful && response.body() != null) {
                val cards = response.body()!!.results.map { page ->
                    val props = page.properties
                    BizCard(
                        id = page.id,
                        name = props["名前"]?.title?.firstOrNull()?.text?.content ?: "",
                        company = props["会社名"]?.rich_text?.firstOrNull()?.text?.content ?: "",
                        email = props["メール"]?.email ?: "",
                        phone = props["電話番号"]?.rich_text?.firstOrNull()?.text?.content ?: "",
                        memo = props["メモ"]?.rich_text?.firstOrNull()?.text?.content ?: "",
                        meetingPlace = props["会った場所"]?.rich_text?.firstOrNull()?.text?.content ?: "",
                        meetingDate = props["会った日"]?.date?.start?.let { 
                            try {
                                dateFormat.parse(it)
                            } catch (e: Exception) {
                                Log.w("NotionRepository", "Failed to parse date: $it", e)
                                null
                            }
                        },
                        cardImageUri = props["名刺画像"]?.files?.firstOrNull()?.external?.url
                            ?: props["名刺画像"]?.files?.firstOrNull()?.file?.url,
                        facePhotoUri = props["顔写真"]?.files?.firstOrNull()?.external?.url
                            ?: props["顔写真"]?.files?.firstOrNull()?.file?.url,
                        registrationDate = page.created_time?.let {
                            try {
                                dateTimeFormat.parse(it)
                            } catch (e: Exception) {
                                Log.w("NotionRepository", "Failed to parse created_time: $it", e)
                                null
                            }
                        }
                    )
                }
                Log.d("NotionRepository", "Successfully loaded ${cards.size} cards")
                Result.success(cards)
            } else {
                // エラーレスポンスの詳細を取得
                val errorBodyString = try {
                    val errorBody = response.errorBody()
                    if (errorBody != null) {
                        val source = errorBody.source()
                        source.request(java.lang.Long.MAX_VALUE)
                        val errorBodyString = source.buffer().clone().readUtf8()
                        source.close()
                        errorBodyString
                    } else {
                        "No error body"
                    }
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }
                
                val errorMessage = "Failed to fetch cards (${response.code()}): $errorBodyString"
                Log.e("NotionRepository", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

