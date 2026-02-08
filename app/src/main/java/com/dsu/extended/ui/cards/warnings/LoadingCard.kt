package com.dsu.extended.ui.cards.warnings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dsu.extended.R
import com.dsu.extended.ui.components.ExpressiveIndeterminateLoadingBar
import com.dsu.extended.ui.components.MiuixInfiniteLoadingIndicator
import com.dsu.extended.ui.components.SimpleCard
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle

@Composable
fun GrantingPermissionCard() {
    val uiStyle = LocalUiStyle.current
    SimpleCard(
        modifier = Modifier.fillMaxWidth(),
        cardTitle = stringResource(id = R.string.missing_permission),
        text = stringResource(id = R.string.granting_permission),
    ) {
        if (uiStyle == UiStyle.MIUIX) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                MiuixInfiniteLoadingIndicator()
            }
        } else {
            ExpressiveIndeterminateLoadingBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
            )
        }
    }
}
