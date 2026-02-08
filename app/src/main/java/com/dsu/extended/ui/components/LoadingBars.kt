package com.dsu.extended.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator as MiuixCircularProgressIndicator
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator as MiuixInfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator as MiuixLinearProgressIndicator

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveIndeterminateLoadingBar(
    modifier: Modifier = Modifier,
) {
    LinearWavyProgressIndicator(modifier = modifier)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 320),
        label = "expressiveProgress",
    )
    LinearWavyProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier,
    )
}

@Composable
fun MiuixProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 240),
        label = "miuixProgress",
    )
    MiuixLinearProgressIndicator(
        modifier = modifier,
        progress = animatedProgress,
    )
}

@Composable
fun MiuixIndeterminateLoadingBar(
    modifier: Modifier = Modifier,
) {
    MiuixLinearProgressIndicator(
        modifier = modifier,
        progress = null,
    )
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
