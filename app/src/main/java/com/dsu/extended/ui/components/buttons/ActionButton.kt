package com.dsu.extended.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dsu.extended.ui.theme.DSUTextStyles

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    colorButton: Color? = null,
    colorText: Color? = null,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    textButton: Boolean = false,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    val resolvedTextColor = colorText ?: MaterialTheme.colorScheme.onSecondaryContainer
    val resolvedIconTint = iconTint ?: colorText ?: MaterialTheme.colorScheme.primary
    if (textButton) {
        TextButton(
            onClick = onClick,
            modifier = modifier,
            shapes = ButtonDefaults.shapes(),
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = colorText ?: MaterialTheme.colorScheme.primary,
                ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = resolvedIconTint,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text,
                    style = DSUTextStyles.buttonText,
                    color = colorText ?: MaterialTheme.colorScheme.primary,
                )
            }
        }
    } else {
        FilledTonalButton(
            modifier = modifier
                .height(46.dp),
            onClick = onClick,
            enabled = isEnabled,
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            colors = if (colorButton != null) {
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorButton,
                    contentColor = resolvedTextColor,
                )
            } else {
                ButtonDefaults.filledTonalButtonColors()
            },
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedIconTint,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = DSUTextStyles.buttonText,
                color = resolvedTextColor,
            )
            content()
        }
    }
}
