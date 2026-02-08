package com.dsu.extended.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle

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
    if (uiStyle == UiStyle.MIUIX) {
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
            scrollBehavior = scrollBehavior,
        )
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
