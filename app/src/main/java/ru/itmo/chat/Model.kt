package ru.itmo.chat

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class Message(
    val id: Long,
    val from: String,
    val time: Long,
    val content: MessageContent,
)

@Immutable
sealed interface MessageContent {
    data class Text(val text: String) : MessageContent
    data class Image(val link: String) : MessageContent
    data object Unsupported : MessageContent
}

enum class LoadState { Idle, Loading, Error, End }

@Immutable
data class ChatState(
    val messages: PersistentList<Message> = persistentListOf(),
    val loadState: LoadState = LoadState.Idle,
)

@Immutable
sealed interface ChannelsState {
    data object Loading : ChannelsState
    data object Error : ChannelsState
    data class Loaded(val channels: PersistentList<String>) : ChannelsState
}

@Immutable
data class ChatUiState(
    val channels: ChannelsState,
    val selectedChat: String?,
    val chat: ChatState,
    val openedImage: String?,
)

fun chatDisplayName(name: String): String = name.removeSuffix(CHANNEL_SUFFIX).ifBlank { name }

private const val CHANNEL_SUFFIX = "@channel"
