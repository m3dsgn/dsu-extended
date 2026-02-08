package com.dsu.extended.ui.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dsu.extended.ui.theme.DSUTextStyles

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    OutlinedButton(
        modifier = modifier
            .height(46.dp),
        onClick = onClick,
        enabled = isEnabled,
        shapes = ButtonDefaults.shapes(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        border = ButtonDefaults.outlinedButtonBorder,
    ) {
        Text(
            text = text,
            style = DSUTextStyles.buttonText,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
