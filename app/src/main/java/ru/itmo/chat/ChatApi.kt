package ru.itmo.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "https://faerytea.name/"

interface ChatApi {

    @GET("channels")
    suspend fun channels(): List<String>

    @GET("channel/{name}")
    suspend fun channelMessages(
        @Path("name") name: String,
        @Query("lastKnownId") lastKnownId: Long,
        @Query("limit") limit: Int,
        @Query("reverse") reverse: Boolean,
    ): List<MessageDto>
}

@Serializable
data class MessageDto(
    val id: Long,
    val from: String,
    val data: MessageDataDto,
    val time: Long = 0L,
)

@Serializable
data class MessageDataDto(
    @SerialName("Text") val text: TextDto? = null,
    @SerialName("Image") val image: ImageDto? = null,
)

@Serializable
data class TextDto(val text: String)

@Serializable
data class ImageDto(val link: String = "")

fun MessageDto.toMessage(): Message = Message(
    id = id,
    from = from,
    time = time,
    content = when {
        data.text != null -> MessageContent.Text(data.text.text)
        data.image != null && data.image.link.isNotEmpty() -> MessageContent.Image(data.image.link)
        else -> MessageContent.Unsupported
    },
)

fun thumbUrl(link: String): String = imageUrl("thumb", link)

fun fullImageUrl(link: String): String = imageUrl("img", link)

private fun imageUrl(kind: String, link: String): String = BASE_URL.toHttpUrl()
    .newBuilder()
    .addPathSegment(kind)
    .addPathSegments(link.trimStart('/'))
    .build()
    .toString()
