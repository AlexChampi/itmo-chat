package ru.itmo.chat

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

@Composable
fun ImageScreen(link: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    var controlsVisible by rememberSaveable { mutableStateOf(true) }
    var painterState by remember(link) { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    DarkSystemBars()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = null, indication = null) { controlsVisible = !controlsVisible },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = fullImageUrl(link),
            contentDescription = null,
            onState = { painterState = it },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        )
        when (painterState) {
            is AsyncImagePainter.State.Loading -> CircularProgressIndicator(color = Color.White)
            is AsyncImagePainter.State.Error -> Text(
                text = stringResource(R.string.image_error),
                style = OneUiType.Body,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(OneUiDimens.Space5),
            )
            else -> Unit
        }
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(scrimColor, Color.Transparent)))
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(OneUiDimens.Space1),
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.cd_close_image),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun DarkSystemBars() {
    val window = LocalActivity.current?.window ?: return
    val view = LocalView.current
    DisposableEffect(window, view) {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        onDispose {
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }
}

private val scrimColor = Color(0x99000000)
