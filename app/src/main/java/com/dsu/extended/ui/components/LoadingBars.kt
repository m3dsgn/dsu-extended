package com.dsu.extended.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator as MiuixCircularProgressIndicator
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator as MiuixInfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator as MiuixLinearProgressIndicator

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveIndeterminateLoadingBar(
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        LinearWavyProgressIndicator(
            modifier = modifier,
            color = progressColor,
            trackColor = trackColor,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color,
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        animationSpec = tween(durationMillis = 260),
        label = "expressiveProgress",
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        LinearWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = modifier,
            color = progressColor,
            trackColor = trackColor,
        )
    }
}

@Composable
fun MiuixProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color,
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        animationSpec = tween(durationMillis = 240),
        label = "miuixProgress",
    )
    val colors = ProgressIndicatorDefaults.progressIndicatorColors(
        foregroundColor = progressColor,
        disabledForegroundColor = progressColor.copy(alpha = 0.45f),
        backgroundColor = trackColor,
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        MiuixLinearProgressIndicator(
            modifier = modifier,
            progress = animatedProgress,
            colors = colors,
        )
    }
}

@Composable
fun MiuixIndeterminateLoadingBar(
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color,
) {
    val colors = ProgressIndicatorDefaults.progressIndicatorColors(
        foregroundColor = progressColor,
        disabledForegroundColor = progressColor.copy(alpha = 0.45f),
        backgroundColor = trackColor,
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        MiuixLinearProgressIndicator(
            modifier = modifier,
            progress = null,
            colors = colors,
        )
    }
}

@Composable
fun MiuixInfiniteLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    MiuixInfiniteProgressIndicator(modifier = modifier)
}

@Composable
fun MiuixCircularLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    MiuixCircularProgressIndicator(
        modifier = modifier,
        progress = null,
    )
}
