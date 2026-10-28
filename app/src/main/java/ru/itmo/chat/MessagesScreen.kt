package ru.itmo.chat

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MessagesScreen(
    chatName: String,
    chat: ChatState,
    listState: LazyListState,
    @DrawableRes navigationIcon: Int,
    @StringRes navigationDescription: Int,
    onNavigate: () -> Unit,
    onLoadMore: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nearEnd by remember(listState) {
        derivedStateOf {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= layout.totalItemsCount - PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(chatName, nearEnd, chat.loadState) {
        if (nearEnd && chat.loadState == LoadState.Idle) {
            onLoadMore()
        }
    }

    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
    }

    Column(modifier = modifier.fillMaxSize()) {
        MessagesToolbar(chatName, navigationIcon, navigationDescription, onNavigate)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = OneUiDimens.Space3, vertical = OneUiDimens.Space3),
            verticalArrangement = Arrangement.spacedBy(OneUiDimens.Space3),
        ) {
            items(chat.messages, key = Message::id, contentType = { it.content is MessageContent.Image }) { message ->
                MessageItem(
                    message = message,
                    time = formatter.format(Instant.ofEpochMilli(message.time).atZone(ZoneId.systemDefault())),
                    onImageClick = onImageClick,
                )
            }
            item(key = FOOTER_KEY, contentType = FOOTER_KEY) {
                MessagesFooter(chat, onLoadMore)
            }
        }
    }
}

@Composable
private fun MessagesToolbar(
    chatName: String,
    @DrawableRes navigationIcon: Int,
    @StringRes navigationDescription: Int,
    onNavigate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(OneUiDimens.ToolbarHeight)
            .padding(horizontal = OneUiDimens.Space1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigate) {
            Icon(
                painter = painterResource(navigationIcon),
                contentDescription = stringResource(navigationDescription),
                tint = OneUiColors.Ink,
            )
        }
        Spacer(Modifier.width(OneUiDimens.Space1))
        Avatar(chatName, size = toolbarAvatarSize)
        Spacer(Modifier.width(OneUiDimens.Space3))
        Text(
            text = chatDisplayName(chatName),
            style = OneUiType.ChatTitle,
            color = OneUiColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
    }
}

@Composable
private fun MessageItem(message: Message, time: String, onImageClick: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Avatar(message.from, size = messageAvatarSize)
        Spacer(Modifier.width(OneUiDimens.Space2))
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = message.from,
                style = OneUiType.Sender,
                color = OneUiColors.InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = OneUiDimens.Space3, bottom = OneUiDimens.Space1),
            )
            Column(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(OneUiColors.Surface)
                    .padding(OneUiDimens.Space2),
            ) {
                when (val content = message.content) {
                    is MessageContent.Text -> Text(
                        text = content.text,
                        style = OneUiType.Body,
                        color = OneUiColors.Ink,
                        modifier = Modifier.padding(horizontal = OneUiDimens.Space1),
                    )
                    is MessageContent.Image -> AsyncImage(
                        model = thumbUrl(content.link),
                        contentDescription = stringResource(R.string.cd_image, message.from),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_broken_image),
                        modifier = Modifier
                            .size(width = thumbWidth, height = thumbHeight)
                            .clip(thumbShape)
                            .background(OneUiColors.Pill)
                            .clickable(role = Role.Image) { onImageClick(content.link) },
                    )
                    MessageContent.Unsupported -> Text(
                        text = stringResource(R.string.unsupported_message),
                        style = OneUiType.Body,
                        color = OneUiColors.InkMuted,
                        modifier = Modifier.padding(horizontal = OneUiDimens.Space1),
                    )
                }
                Text(
                    text = time,
                    style = OneUiType.Caption,
                    color = OneUiColors.InkMuted,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = OneUiDimens.Space1, start = OneUiDimens.Space1, end = OneUiDimens.Space1),
                )
            }
        }
    }
}

@Composable
private fun MessagesFooter(chat: ChatState, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        when (chat.loadState) {
            LoadState.Idle, LoadState.Loading -> LoadingBlock()
            LoadState.Error -> ErrorBlock(R.string.messages_error, onRetry)
            LoadState.End -> HintText(if (chat.messages.isEmpty()) R.string.messages_empty else R.string.chat_start)
        }
    }
}

@Composable
fun SelectChatPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.select_chat), style = OneUiType.HeaderSmall, color = OneUiColors.Ink)
        Spacer(Modifier.height(OneUiDimens.Space2))
        Text(text = stringResource(R.string.select_chat_hint), style = OneUiType.Subtitle, color = OneUiColors.InkMuted)
    }
}

private val toolbarAvatarSize = 36.dp
private val messageAvatarSize = 32.dp
private val thumbWidth = 220.dp
private val thumbHeight = 165.dp
private val bubbleShape = RoundedCornerShape(18.dp)
private val thumbShape = RoundedCornerShape(12.dp)
private const val PREFETCH_DISTANCE = 5
private const val FOOTER_KEY = "footer"
