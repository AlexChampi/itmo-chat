package ru.itmo.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val api: ChatApi,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val channels = MutableStateFlow<ChannelsState>(ChannelsState.Loading)
    private val chats = MutableStateFlow<PersistentMap<String, ChatState>>(persistentMapOf())
    private val selectedChat = savedState.getStateFlow<String?>(KEY_CHAT, null)
    private val openedImage = savedState.getStateFlow<String?>(KEY_IMAGE, null)

    val uiState: StateFlow<ChatUiState> = combine(
        channels,
        chats,
        selectedChat,
        openedImage,
        ::buildUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = buildUiState(channels.value, chats.value, selectedChat.value, openedImage.value),
    )

    init {
        loadChannels()
        selectedChat.value?.let(::loadMore)
    }

    fun loadChannels() {
        channels.value = ChannelsState.Loading
        viewModelScope.launch {
            channels.value = try {
                val names = api.channels()
                    .distinct()
                    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, ::chatDisplayName))
                ChannelsState.Loaded(names.toPersistentList())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                ChannelsState.Error
            }
        }
    }

    fun selectChat(name: String) {
        savedState[KEY_IMAGE] = null
        savedState[KEY_CHAT] = name
        if (name !in chats.value) {
            loadMore(name)
        }
    }

    fun closeChat() {
        savedState[KEY_IMAGE] = null
        savedState[KEY_CHAT] = null
    }

    fun openImage(link: String) {
        savedState[KEY_IMAGE] = link
    }

    fun closeImage() {
        savedState[KEY_IMAGE] = null
    }

    fun loadMore(name: String) {
        val current = chats.value[name] ?: ChatState()
        if (current.loadState == LoadState.Loading || current.loadState == LoadState.End) {
            return
        }
        updateChat(name) { it.copy(loadState = LoadState.Loading) }
        val lastKnownId = current.messages.lastOrNull()?.id ?: MAX_ID
        viewModelScope.launch {
            try {
                val page = api.channelMessages(name, lastKnownId, PAGE_SIZE, reverse = true)
                    .map(MessageDto::toMessage)
                updateChat(name) { chat ->
                    val knownIds = chat.messages.mapTo(HashSet(), Message::id)
                    chat.copy(
                        messages = chat.messages.addingAll(page.filter { it.id !in knownIds }),
                        loadState = if (page.size < PAGE_SIZE) LoadState.End else LoadState.Idle,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                updateChat(name) { it.copy(loadState = LoadState.Error) }
            }
        }
    }

    private fun updateChat(name: String, transform: (ChatState) -> ChatState) {
        chats.update { map -> map.putting(name, transform(map[name] ?: ChatState())) }
    }

    private fun buildUiState(
        channels: ChannelsState,
        chats: PersistentMap<String, ChatState>,
        selectedChat: String?,
        openedImage: String?,
    ): ChatUiState = ChatUiState(
        channels = channels,
        selectedChat = selectedChat,
        chat = selectedChat?.let(chats::get) ?: ChatState(),
        openedImage = openedImage,
    )

    companion object {
        const val PAGE_SIZE = 20
        private const val MAX_ID = 0xFFFFFFFFFFFFL
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val KEY_CHAT = "selected_chat"
        private const val KEY_IMAGE = "opened_image"

        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY]) as ChatApplication
                ChatViewModel(application.api, createSavedStateHandle())
            }
        }
    }
}
