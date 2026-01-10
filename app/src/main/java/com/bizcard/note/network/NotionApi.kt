package com.bizcard.note.network

import retrofit2.Response
import retrofit2.http.*

interface NotionApi {
    @GET("databases/{databaseId}")
    suspend fun getDatabase(
        @Path("databaseId") databaseId: String,
        @Header("Authorization") authorization: String,
        @Header("Notion-Version") notionVersion: String
    ): Response<GetDatabaseResponse>

    @POST("pages")
    suspend fun createPage(
        @Header("Authorization") authorization: String,
        @Header("Notion-Version") notionVersion: String,
        @Body body: CreatePageRequest
    ): Response<CreatePageResponse>

    @POST("databases/{databaseId}/query")
    suspend fun queryDatabase(
        @Path("databaseId") databaseId: String,
        @Header("Authorization") authorization: String,
        @Header("Notion-Version") notionVersion: String,
        @Body body: QueryDatabaseRequest
    ): Response<QueryDatabaseResponse>
}

data class GetDatabaseResponse(
    val id: String,
    val title: List<TitleContent>,
    val properties: Map<String, DatabaseProperty>? = null
)

data class DatabaseProperty(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null
)

data class CreatePageRequest(
    val parent: Parent,
    val properties: Map<String, Property>
)

data class Parent(
    val database_id: String
)

data class Property(
    val title: List<TitleContent>? = null,
    val rich_text: List<RichTextContent>? = null,
    val email: String? = null,
    val date: DateContent? = null,
    val files: List<FileContent>? = null
)

data class TitleContent(val text: TextContent)
data class RichTextContent(val text: TextContent)
data class TextContent(val content: String)
data class DateContent(val start: String)
data class FileContent(val external: ExternalFile? = null, val file: ExternalFile? = null)
data class ExternalFile(val url: String)

data class CreatePageResponse(val id: String)

data class QueryDatabaseRequest(
    val sorts: List<Sort>? = null
)

data class Sort(
    val property: String,
    val direction: String = "descending"
)

data class QueryDatabaseResponse(
    val results: List<PageResult>
)

data class PageResult(
    val id: String,
    val created_time: String? = null,
    val properties: Map<String, PropertyResult>
)

data class PropertyResult(
    val title: List<TitleContent>? = null,
    val rich_text: List<RichTextContent>? = null,
    val email: String? = null,
    val date: DateContent? = null,
    val files: List<FileContent>? = null
)

