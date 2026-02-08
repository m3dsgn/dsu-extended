package com.dsu.extended.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle

@Composable
fun Title(title: String, modifier: Modifier = Modifier) {
    val uiStyle = LocalUiStyle.current
    Text(
        text = title,
        fontSize = if (uiStyle == UiStyle.MIUIX) 13.sp else 14.sp,
        fontWeight = FontWeight.Medium,
        color = if (uiStyle == UiStyle.MIUIX) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(start = 17.dp)
            .padding(bottom = 8.dp)
            .padding(top = 8.dp),
    )
}
