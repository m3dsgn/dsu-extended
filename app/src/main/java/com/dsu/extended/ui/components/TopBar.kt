package com.dsu.extended.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dsu.extended.ui.theme.LocalHazeState
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import top.yukonga.miuix.kmp.basic.SmallTopAppBar as MiuixSmallTopAppBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    barTitle: String,
    compactTitle: Boolean = false,
    icon: ImageVector? = null,
    iconContentDescription: String? = "icon",
    onClickIcon: () -> Unit = {},
    onClickBackButton: (() -> Unit)? = null,
) {
    val uiStyle = LocalUiStyle.current
    val colorScheme = MaterialTheme.colorScheme
    val isOledBlackTheme = colorScheme.surface == Color.Black && colorScheme.background == Color.Black
    val topBarContainerColor = if (isOledBlackTheme) Color.Black else colorScheme.surfaceContainer
    val scrolledTopBarContainerColor = if (isOledBlackTheme) Color.Black else colorScheme.surfaceContainerHigh
    if (uiStyle == UiStyle.MIUIX) {
        val hazeState = LocalHazeState.current
        val hazeStyle = remember(topBarContainerColor) {
            HazeStyle(
                backgroundColor = topBarContainerColor,
                tint = HazeTint(topBarContainerColor.copy(alpha = if (isOledBlackTheme) 0.76f else 0.82f)),
            )
        }
        val topBarModifier =
            if (hazeState != null) {
                Modifier
                    .fillMaxWidth()
                    .hazeEffect(hazeState) {
                        style = hazeStyle
                        blurRadius = 30.dp
                        noiseFactor = 0f
                    }
            } else {
                Modifier.fillMaxWidth()
            }
        Box(modifier = topBarModifier) {
            MiuixSmallTopAppBar(
                title = barTitle,
                color = if (hazeState != null) Color.Transparent else topBarContainerColor,
                navigationIcon = {
                    if (onClickBackButton != null) {
                        PressableTopBarIconButton(onClick = onClickBackButton) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = iconContentDescription,
                            )
                        }
                    }
                },
                actions = {
                    if (icon != null) {
                        PressableTopBarIconButton(onClick = onClickIcon) {
                            Icon(
                                imageVector = icon,
                                contentDescription = iconContentDescription,
                            )
                        }
                    }
                },
            )
        }
    } else {
        if (compactTitle) {
            TopAppBar(
                title = {
                    Text(
                        text = barTitle,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (onClickBackButton != null) {
                        PressableTopBarIconButton(onClick = onClickBackButton) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = iconContentDescription,
                            )
                        }
                    }
                },
                actions = {
                    if (icon != null) {
                        PressableTopBarIconButton(onClick = onClickIcon) {
                            Icon(
                                imageVector = icon,
                                contentDescription = iconContentDescription,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor,
                    scrolledContainerColor = scrolledTopBarContainerColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                expandedHeight = 56.dp,
                scrollBehavior = scrollBehavior,
            )
        } else {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = barTitle,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                navigationIcon = {
                    if (onClickBackButton != null) {
                        PressableTopBarIconButton(onClick = onClickBackButton) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = iconContentDescription,
                            )
                        }
                    }
                },
                actions = {
                    if (icon != null) {
                        PressableTopBarIconButton(onClick = onClickIcon) {
                            Icon(
                                imageVector = icon,
                                contentDescription = iconContentDescription,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor,
                    scrolledContainerColor = scrolledTopBarContainerColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                scrollBehavior = scrollBehavior,
            )
        }
    }
}

@Composable
private fun PressableTopBarIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "topBarIconScale",
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.scale(scale),
    ) {
        content()
    }
}
