package com.dsu.extended.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomSheetContent(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    val uiStyle = LocalUiStyle.current
    val titleStyle =
        if (uiStyle == UiStyle.MIUIX) {
            MiuixTheme.textStyles.title3
        } else {
            MaterialTheme.typography.headlineSmall
        }
    val iconTint =
        if (uiStyle == UiStyle.MIUIX) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onBackground
        }
    val titleColor =
        if (uiStyle == UiStyle.MIUIX) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (uiStyle == UiStyle.MIUIX) 8.dp else 12.dp)
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
    ) {
        Icon(
            tint = iconTint,
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 6.dp)
                .size(if (uiStyle == UiStyle.MIUIX) 24.dp else 26.dp),
        )
        Text(
            color = titleColor,
            text = title,
            style = titleStyle,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}
