package com.dsu.extended.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch

@Composable
fun PreferenceItem(
    title: String,
    description: String = "",
    icon: ImageVector? = null,
    onClick: (Boolean) -> Unit = {},
    isChecked: Boolean = false,
    showToggle: Boolean = false,
    isEnabled: Boolean = true,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val uiStyle = LocalUiStyle.current
    if (uiStyle == UiStyle.MIUIX) {
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicComponent(
                title = title,
                summary = description.ifBlank { null },
                enabled = isEnabled,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick(isChecked)
                },
                endActions = {
                    if (showToggle) {
                        MiuixSwitch(
                            checked = isChecked,
                            enabled = isEnabled,
                            onCheckedChange = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onClick(isChecked)
                            },
                        )
                    } else if (icon == Icons.Filled.Check) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        }
        return
    }

    val titleStyle =
        MaterialTheme.typography.bodyMedium
    val descriptionStyle =
        MaterialTheme.typography.bodySmall
    val titleColor = MaterialTheme.colorScheme.onSurface
    val descriptionColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurface

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick(isChecked)
                },
                enabled = isEnabled,
            )
            .padding(
                start = 17.dp,
                end = 17.dp,
                bottom = 12.dp,
                top = 12.dp,
            ),
    ) {
        if (icon != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.padding(end = 14.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(22.dp),
                )
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                    color = titleColor,
                )
                if (description.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = descriptionStyle,
                        color = descriptionColor,
                        modifier = Modifier.alpha(0.8F),
                    )
                }
            }
        }
        if (showToggle) {
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isChecked,
                enabled = isEnabled,
                onCheckedChange = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick(isChecked)
                },
                thumbContent = {
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
            )
        }
    }
}
