package com.dsu.extended.ui.components.buttons

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dsu.extended.ui.theme.DSUTextStyles
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    glow: Boolean = false,
) {
    val uiStyle = LocalUiStyle.current
    val baseShape = RoundedCornerShape(18.dp)
    val pressedShape = RoundedCornerShape(10.dp)
    val miuixShape = RoundedCornerShape(16.dp)
    val isStrongGlow = glow && isEnabled
    val glowColor = MaterialTheme.colorScheme.primary.copy(
        alpha =
            if (isStrongGlow) {
                if (uiStyle == UiStyle.MIUIX) 0.38f else 0.46f
            } else if (isEnabled) {
                if (uiStyle == UiStyle.MIUIX) 0.10f else 0.14f
            } else {
                0f
            },
    )
    val glowElevation =
        when {
            !isEnabled -> 0.dp
            isStrongGlow -> 10.dp
            else -> 4.dp
        }
    val glowModifier =
        if (glowElevation > 0.dp) {
            Modifier.shadow(
                elevation = glowElevation,
                shape = if (uiStyle == UiStyle.MIUIX) miuixShape else baseShape,
                ambientColor = glowColor,
                spotColor = glowColor,
                clip = false,
            )
        } else {
            Modifier
        }

    if (uiStyle == UiStyle.MIUIX) {
        MiuixButton(
            modifier = glowModifier.then(modifier),
            onClick = onClick,
            enabled = isEnabled,
            cornerRadius = 16.dp,
            minHeight = 46.dp,
            colors = MiuixButtonDefaults.buttonColorsPrimary(),
        ) {
            Text(
                text = text,
                style = MiuixTheme.textStyles.button,
                color = MiuixTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        return
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "primaryButtonScale",
    )
    Button(
        modifier = glowModifier
            .then(modifier)
            .defaultMinSize(minHeight = 46.dp)
            .scale(scale),
        onClick = onClick,
        enabled = isEnabled,
        interactionSource = interactionSource,
        shapes = ButtonDefaults.shapes(
            shape = baseShape,
            pressedShape = pressedShape,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 9.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
    ) {
        Text(
            text = text,
            style = DSUTextStyles.buttonText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
