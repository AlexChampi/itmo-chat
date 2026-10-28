package ru.itmo.chat

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatApp(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val selectedChat = state.selectedChat
    val openedImage = state.openedImage
    val chatsListState = rememberLazyListState()
    val messagesListState = rememberSaveable(selectedChat, saver = LazyListState.Saver) { LazyListState() }

    BackHandler(enabled = openedImage == null && selectedChat != null, onBack = viewModel::closeChat)
    BackHandler(enabled = openedImage != null, onBack = viewModel::closeImage)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OneUiColors.Bg),
    ) {
        val content = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
        if (landscape) {
            Row(modifier = content) {
                ChatsScreen(
                    channels = state.channels,
                    selectedChat = selectedChat,
                    showHero = false,
                    listState = chatsListState,
                    onChatClick = viewModel::selectChat,
                    onRetry = viewModel::loadChannels,
                    modifier = Modifier.weight(LIST_WEIGHT),
                )
                VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp), color = OneUiColors.Divider)
                Box(modifier = Modifier.weight(DETAIL_WEIGHT)) {
                    if (selectedChat == null) {
                        SelectChatPlaceholder()
                    } else {
                        MessagesScreen(
                            chatName = selectedChat,
                            chat = state.chat,
                            listState = messagesListState,
                            navigationIcon = R.drawable.ic_close,
                            navigationDescription = R.string.cd_close_chat,
                            onNavigate = viewModel::closeChat,
                            onLoadMore = { viewModel.loadMore(selectedChat) },
                            onImageClick = viewModel::openImage,
                        )
                    }
                }
            }
        } else if (selectedChat == null) {
            ChatsScreen(
                channels = state.channels,
                selectedChat = null,
                showHero = true,
                listState = chatsListState,
                onChatClick = viewModel::selectChat,
                onRetry = viewModel::loadChannels,
                modifier = content,
            )
        } else {
            MessagesScreen(
                chatName = selectedChat,
                chat = state.chat,
                listState = messagesListState,
                navigationIcon = R.drawable.ic_arrow_back,
                navigationDescription = R.string.cd_back,
                onNavigate = viewModel::closeChat,
                onLoadMore = { viewModel.loadMore(selectedChat) },
                onImageClick = viewModel::openImage,
                modifier = content,
            )
        }
        if (openedImage != null) {
            ImageScreen(link = openedImage, onClose = viewModel::closeImage)
        }
    }
}

private const val LIST_WEIGHT = 2f
private const val DETAIL_WEIGHT = 3f
