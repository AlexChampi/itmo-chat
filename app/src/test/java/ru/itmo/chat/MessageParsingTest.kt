package ru.itmo.chat

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesTextAndImageMessages() {
        val body = """
            [
              {"id":3,"from":"curl","to":"1@channel","data":{"Text":{"text":"hello there"}},"time":1665413035600},
              {"id":2,"from":"tool","to":"1@channel","data":{"Image":{"link":"tool/tmp/scala-spiral.png"}},"time":1665412956487},
              {"id":1,"from":"bot","data":{"Video":{"link":"x.mp4"}}}
            ]
        """.trimIndent()

        val messages = json.decodeFromString<List<MessageDto>>(body).map(MessageDto::toMessage)

        assertEquals(
            listOf(
                Message(3, "curl", 1665413035600, MessageContent.Text("hello there")),
                Message(2, "tool", 1665412956487, MessageContent.Image("tool/tmp/scala-spiral.png")),
                Message(1, "bot", 0, MessageContent.Unsupported),
            ),
            messages,
        )
    }

    @Test
    fun buildsEncodedImageUrls() {
        assertEquals("https://faerytea.name/thumb/tool/tmp/scala-spiral.png", thumbUrl("tool/tmp/scala-spiral.png"))
        assertEquals("https://faerytea.name/img/user/my%20pic.png", fullImageUrl("user/my pic.png"))
    }

    @Test
    fun stripsChannelSuffix() {
        assertEquals("1", chatDisplayName("1@channel"))
        assertEquals("@channel", chatDisplayName("@channel"))
    }
}
