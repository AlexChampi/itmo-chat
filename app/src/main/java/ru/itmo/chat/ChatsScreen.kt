package ru.itmo.chat

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ChatsScreen(
    channels: ChannelsState,
    selectedChat: String?,
    showHero: Boolean,
    listState: LazyListState,
    onChatClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val heroHidden by remember(listState) { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val titleAlpha by animateFloatAsState(if (!showHero || heroHidden) 1f else 0f, label = "toolbarTitle")
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OneUiDimens.ToolbarHeight)
                .padding(horizontal = OneUiDimens.Space5),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = stringResource(R.string.chats_title),
                style = OneUiType.HeaderSmall,
                color = OneUiColors.Ink,
                modifier = Modifier
                    .graphicsLayer { alpha = titleAlpha }
                    .semantics { heading() },
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = OneUiDimens.Space3,
                end = OneUiDimens.Space3,
                bottom = OneUiDimens.Space5,
            ),
        ) {
            if (showHero) {
                item(key = HERO_KEY, contentType = HERO_KEY) {
                    Hero(channels)
                }
            }
            when (channels) {
                ChannelsState.Loading -> item(key = STATUS_KEY) {
                    LoadingBlock(Modifier.fillMaxWidth())
                }
                ChannelsState.Error -> item(key = STATUS_KEY) {
                    ErrorBlock(R.string.channels_error, onRetry, Modifier.fillMaxWidth())
                }
                is ChannelsState.Loaded -> {
                    if (channels.channels.isEmpty()) {
                        item(key = STATUS_KEY) {
                            HintText(R.string.channels_empty, Modifier.fillMaxWidth())
                        }
                    }
                    itemsIndexed(channels.channels, key = { _, name -> name }, contentType = { _, _ -> ROW_TYPE }) { index, name ->
                        ChannelRow(
                            name = name,
                            selected = name == selectedChat,
                            isFirst = index == 0,
                            isLast = index == channels.channels.lastIndex,
                            onClick = { onChatClick(name) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(channels: ChannelsState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(heroHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.chats_title),
            style = OneUiType.TitleLarge,
            color = OneUiColors.Ink,
            modifier = Modifier.semantics { heading() },
        )
        if (channels is ChannelsState.Loaded) {
            Spacer(Modifier.height(OneUiDimens.Space2))
            val count = channels.channels.size
            Text(
                text = pluralStringResource(R.plurals.channels_count, count, count),
                style = OneUiType.Subtitle,
                color = OneUiColors.InkMuted,
            )
        }
    }
}

@Composable
private fun ChannelRow(
    name: String,
    selected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val radius = OneUiDimens.CardRadius
    val shape = RoundedCornerShape(
        topStart = if (isFirst) radius else 0.dp,
        topEnd = if (isFirst) radius else 0.dp,
        bottomStart = if (isLast) radius else 0.dp,
        bottomEnd = if (isLast) radius else 0.dp,
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OneUiColors.Surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OneUiDimens.RowHeight)
                .background(if (selected) OneUiColors.Pill else OneUiColors.Surface)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = OneUiDimens.Space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(name)
            Spacer(Modifier.width(OneUiDimens.Space4))
            Text(
                text = chatDisplayName(name),
                style = OneUiType.ListPrimary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = OneUiColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = dividerInset, end = OneUiDimens.Space4),
                thickness = 1.dp,
                color = OneUiColors.Divider,
            )
        }
    }
}

private val heroHeight = 240.dp
private val dividerInset = OneUiDimens.Space4 + OneUiDimens.AvatarSize + OneUiDimens.Space4
private const val HERO_KEY = "hero"
private const val STATUS_KEY = "status"
private const val ROW_TYPE = "channel"
