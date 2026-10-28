package ru.itmo.chat

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun Avatar(name: String, modifier: Modifier = Modifier, size: Dp = OneUiDimens.AvatarSize) {
    val palette = OneUiColors.Avatars
    val index = name.hashCode().mod(palette.size)
    val brush = Brush.linearGradient(listOf(palette[index], palette[(index + 1) % palette.size]))
    val displayName = chatDisplayName(name).trim()
    val letter = if (displayName.isEmpty()) "#" else String(Character.toChars(displayName.codePointAt(0))).uppercase()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            color = Color.White,
            style = OneUiType.ListPrimary.copy(fontSize = avatarFontSize(size)),
        )
    }
}

private fun avatarFontSize(size: Dp): TextUnit = (size.value * AVATAR_LETTER_RATIO).sp

private const val AVATAR_LETTER_RATIO = 0.42f

@Composable
fun PillButton(@StringRes text: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = OneUiColors.Pill,
            contentColor = OneUiColors.Ink,
        ),
    ) {
        Text(text = stringResource(text), style = OneUiType.Button)
    }
}

@Composable
fun LoadingBlock(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(OneUiDimens.Space5), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = OneUiColors.InkMuted)
    }
}

@Composable
fun ErrorBlock(@StringRes message: Int, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(OneUiDimens.Space5),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OneUiDimens.Space3),
    ) {
        Text(
            text = stringResource(message),
            style = OneUiType.Body,
            color = OneUiColors.InkMuted,
            textAlign = TextAlign.Center,
        )
        PillButton(text = R.string.retry, onClick = onRetry)
    }
}

@Composable
fun HintText(@StringRes text: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(text),
        modifier = modifier.padding(OneUiDimens.Space4),
        style = OneUiType.Caption,
        color = OneUiColors.InkMuted,
        textAlign = TextAlign.Center,
    )
}
