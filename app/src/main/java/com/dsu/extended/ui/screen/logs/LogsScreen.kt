package com.dsu.extended.ui.screen.logs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.dsu.extended.BuildConfig
import com.dsu.extended.R
import com.dsu.extended.ui.cards.LogcatCard
import com.dsu.extended.ui.components.ApplicationScreen
import com.dsu.extended.ui.components.CardBox
import com.dsu.extended.ui.components.MiuixCircularLoadingIndicator
import com.dsu.extended.ui.components.TopBar
import com.dsu.extended.ui.components.buttons.ActionButton
import com.dsu.extended.ui.components.buttons.PrimaryButton
import com.dsu.extended.ui.screen.Destinations
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import com.dsu.extended.util.StoredLogEntry
import com.dsu.extended.util.StoredLogType
import com.dsu.extended.util.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LogsScreen(
    navigate: (String) -> Unit,
    logsViewModel: LogsViewModel = hiltViewModel(),
) {
    val uiState by logsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uiStyle = LocalUiStyle.current

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        logsViewModel.consumeStatusMessage()
    }

    ApplicationScreen(
        modifier = Modifier.padding(
            start = 12.dp,
            end = 12.dp,
            top = if (uiStyle == UiStyle.MIUIX) 6.dp else 10.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(if (uiStyle == UiStyle.MIUIX) 6.dp else 10.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.logs_title),
                compactTitle = true,
                icon = Icons.Rounded.Settings,
                scrollBehavior = it,
                onClickIcon = { navigate(Destinations.Preferences) },
            )
        },
    ) {
        if (uiStyle == UiStyle.MIUIX) {
            MiuixLogsContent(
                uiState = uiState,
                logsViewModel = logsViewModel,
                onShare = { shareLogFile(context, it) },
            )
        } else {
            ExpressiveLogsContent(
                uiState = uiState,
                logsViewModel = logsViewModel,
                onShare = { shareLogFile(context, it) },
            )
        }
    }

    if (uiState.showLogViewer) {
        LogViewerDialog(
            uiStyle = uiStyle,
            title = uiState.selectedLogTitle,
            content = uiState.selectedLogContent,
            onDismiss = { logsViewModel.closeLogViewer() },
            onShare = {
                if (uiState.selectedLogPath.isNotBlank()) {
                    shareLogFile(context, uiState.selectedLogPath)
                }
            },
        )
    }

    if (uiState.renameTargetPath != null) {
        if (uiStyle == UiStyle.MIUIX) {
            MiuixRenameDialog(
                renameText = uiState.renameText,
                onRenameTextChange = { logsViewModel.updateRenameText(it) },
                onConfirm = { logsViewModel.applyRename() },
                onDismiss = { logsViewModel.cancelRename() },
            )
        } else {
            AlertDialog(
                onDismissRequest = { logsViewModel.cancelRename() },
                title = { Text(text = stringResource(id = R.string.logs_rename_title)) },
                text = {
                    OutlinedTextField(
                        value = uiState.renameText,
                        onValueChange = { logsViewModel.updateRenameText(it) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { logsViewModel.applyRename() }) {
                        Text(text = stringResource(id = R.string.logs_rename_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { logsViewModel.cancelRename() }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun ExpressiveLogsContent(
    uiState: LogsUiState,
    logsViewModel: LogsViewModel,
    onShare: (String) -> Unit,
) {
    CardBox(
        cardTitle = stringResource(id = R.string.logs_cat_title),
        addPadding = true,
    ) {
        Text(
            text = stringResource(id = R.string.logs_cat_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            PrimaryButton(
                text =
                if (uiState.isBusy) {
                    stringResource(id = R.string.logs_cat_running)
                } else {
                    stringResource(id = R.string.logs_cat_capture)
                },
                onClick = { logsViewModel.captureCatExperimental() },
                isEnabled = !uiState.isBusy,
            )
        }
    }

    CardBox(
        cardTitle = stringResource(id = R.string.logs_filters_title),
        addPadding = true,
    ) {
        FilterRow(
            title = stringResource(id = R.string.logs_filter_type),
            options = listOf(
                LogsTypeFilter.ALL to stringResource(id = R.string.logs_filter_type_all),
                LogsTypeFilter.INSTALLATION to stringResource(id = R.string.logs_filter_type_installation),
                LogsTypeFilter.AUTO_MODE to stringResource(id = R.string.logs_filter_type_auto),
                LogsTypeFilter.CAT to stringResource(id = R.string.logs_filter_type_cat),
            ),
            selected = uiState.typeFilter,
            onSelect = { logsViewModel.setTypeFilter(it) },
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        FilterRow(
            title = stringResource(id = R.string.logs_filter_date),
            options = listOf(
                LogsDateFilter.ALL to stringResource(id = R.string.logs_filter_date_all),
                LogsDateFilter.TODAY to stringResource(id = R.string.logs_filter_date_today),
                LogsDateFilter.WEEK to stringResource(id = R.string.logs_filter_date_week),
            ),
            selected = uiState.dateFilter,
            onSelect = { logsViewModel.setDateFilter(it) },
        )
    }

    if (uiState.filteredLogs.isEmpty()) {
        CardBox(addPadding = true) {
            Text(
                text = stringResource(id = R.string.logs_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        uiState.filteredLogs.forEach { log ->
            ExpressiveLogEntryCard(
                entry = log,
                onRead = { logsViewModel.openLog(log.filePath) },
                onShare = { onShare(log.filePath) },
                onRename = { logsViewModel.startRename(log.filePath) },
                onDelete = { logsViewModel.deleteLog(log.filePath) },
            )
        }
    }
}

@Composable
private fun MiuixLogsContent(
    uiState: LogsUiState,
    logsViewModel: LogsViewModel,
    onShare: (String) -> Unit,
) {
    MiuixCard(modifier = Modifier.padding(bottom = 10.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = stringResource(id = R.string.logs_cat_title),
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(id = R.string.logs_cat_description),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(modifier = Modifier.size(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                MiuixButton(
                    enabled = !uiState.isBusy,
                    colors = MiuixButtonDefaults.buttonColorsPrimary(),
                    onClick = { logsViewModel.captureCatExperimental() },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (uiState.isBusy) {
                            MiuixCircularLoadingIndicator(
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            text =
                            if (uiState.isBusy) {
                                stringResource(id = R.string.logs_cat_running)
                            } else {
                                stringResource(id = R.string.logs_cat_capture)
                            },
                            style = MiuixTheme.textStyles.button,
                            color = MiuixTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }

    MiuixCard(modifier = Modifier.padding(bottom = 10.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.logs_filters_title),
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface,
            )
            MiuixFilterRow(
                title = stringResource(id = R.string.logs_filter_type),
                options = listOf(
                    LogsTypeFilter.ALL to stringResource(id = R.string.logs_filter_type_all),
                    LogsTypeFilter.INSTALLATION to stringResource(id = R.string.logs_filter_type_installation),
                    LogsTypeFilter.AUTO_MODE to stringResource(id = R.string.logs_filter_type_auto),
                    LogsTypeFilter.CAT to stringResource(id = R.string.logs_filter_type_cat),
                ),
                selected = uiState.typeFilter,
                onSelect = { logsViewModel.setTypeFilter(it) },
            )
            MiuixFilterRow(
                title = stringResource(id = R.string.logs_filter_date),
                options = listOf(
                    LogsDateFilter.ALL to stringResource(id = R.string.logs_filter_date_all),
                    LogsDateFilter.TODAY to stringResource(id = R.string.logs_filter_date_today),
                    LogsDateFilter.WEEK to stringResource(id = R.string.logs_filter_date_week),
                ),
                selected = uiState.dateFilter,
                onSelect = { logsViewModel.setDateFilter(it) },
            )
        }
    }

    if (uiState.filteredLogs.isEmpty()) {
        MiuixCard(modifier = Modifier.padding(bottom = 10.dp)) {
            Text(
                text = stringResource(id = R.string.logs_empty),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }
    } else {
        uiState.filteredLogs.forEach { log ->
            MiuixLogEntryCard(
                entry = log,
                onRead = { logsViewModel.openLog(log.filePath) },
                onShare = { onShare(log.filePath) },
                onRename = { logsViewModel.startRename(log.filePath) },
                onDelete = { logsViewModel.deleteLog(log.filePath) },
            )
        }
    }
}

@Composable
private fun <T> FilterRow(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(text = label) },
            )
        }
    }
}

@Composable
private fun <T> MiuixFilterRow(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.body2,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            MiuixFilterChipItem(
                text = label,
                selected = selected == value,
                onClick = { onSelect(value) },
            )
        }
    }
}

@Composable
private fun MiuixFilterChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                if (selected) {
                    MiuixTheme.colorScheme.surfaceContainerHighest
                } else {
                    MiuixTheme.colorScheme.surfaceContainer
                },
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            color =
            if (selected) {
                MiuixTheme.colorScheme.onSurface
            } else {
                MiuixTheme.colorScheme.onSurfaceVariantSummary
            },
        )
    }
}

@Composable
private fun ExpressiveLogEntryCard(
    entry: StoredLogEntry,
    onRead: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    CardBox(addPadding = true) {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.padding(top = 3.dp))
        Text(
            text = buildMetaText(entry, logTypeLabel(entry.type)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ActionButton(
                text = stringResource(id = R.string.logs_action_read),
                onClick = onRead,
                icon = Icons.Rounded.Visibility,
                textButton = true,
            )
            ActionButton(
                text = stringResource(id = R.string.logs_action_share),
                onClick = onShare,
                icon = Icons.Rounded.IosShare,
                colorText = MaterialTheme.colorScheme.primary,
                textButton = true,
            )
            ActionButton(
                text = stringResource(id = R.string.logs_action_rename),
                onClick = onRename,
                icon = Icons.Rounded.Edit,
                textButton = true,
            )
            ActionButton(
                text = stringResource(id = R.string.logs_action_delete),
                onClick = onDelete,
                icon = Icons.Rounded.DeleteOutline,
                colorText = MaterialTheme.colorScheme.error,
                textButton = true,
            )
        }
    }
}

@Composable
private fun MiuixLogEntryCard(
    entry: StoredLogEntry,
    onRead: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    MiuixCard(modifier = Modifier.padding(bottom = 10.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = entry.title,
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.padding(top = 3.dp))
            Text(
                text = buildMetaText(entry, logTypeLabel(entry.type)),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MiuixActionButton(
                    text = stringResource(id = R.string.logs_action_read),
                    icon = Icons.Rounded.Visibility,
                    onClick = onRead,
                    colors = MiuixButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primaryContainer,
                    ),
                    contentColor = MiuixTheme.colorScheme.onPrimaryContainer,
                )
                MiuixActionButton(
                    text = stringResource(id = R.string.logs_action_share),
                    icon = Icons.Rounded.IosShare,
                    onClick = onShare,
                    colors = MiuixButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.tertiaryContainer,
                    ),
                    contentColor = MiuixTheme.colorScheme.onTertiaryContainer,
                )
                MiuixActionButton(
                    text = stringResource(id = R.string.logs_action_rename),
                    icon = Icons.Rounded.Edit,
                    onClick = onRename,
                    colors = MiuixButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                    ),
                    contentColor = MiuixTheme.colorScheme.onSecondaryContainer,
                )
                MiuixActionButton(
                    text = stringResource(id = R.string.logs_action_delete),
                    icon = Icons.Rounded.DeleteOutline,
                    onClick = onDelete,
                    colors = MiuixButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.errorContainer,
                    ),
                    contentColor = MiuixTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun MiuixActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    colors: top.yukonga.miuix.kmp.basic.ButtonColors,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    MiuixButton(
        onClick = onClick,
        colors = colors,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = text,
                style = MiuixTheme.textStyles.button,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun MiuixRenameDialog(
    renameText: String,
    onRenameTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            color = MiuixTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 26.dp)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.logs_rename_title),
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.size(10.dp))
                OutlinedTextField(
                    value = renameText,
                    onValueChange = onRenameTextChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.size(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    MiuixTextButton(
                        text = stringResource(id = R.string.cancel),
                        onClick = onDismiss,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    MiuixTextButton(
                        text = stringResource(id = R.string.logs_rename_confirm),
                        onClick = onConfirm,
                        colors = MiuixButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LogViewerDialog(
    uiStyle: UiStyle,
    title: String,
    content: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
) {
    val isMiuix = uiStyle == UiStyle.MIUIX

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        if (isMiuix) {
            Surface(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 26.dp)
                    .fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp),
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                        Text(
                            text = title,
                            style = MiuixTheme.textStyles.title3,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    LogcatCard(logs = content.ifBlank { "-" })
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))
                        MiuixTextButton(
                            text = stringResource(id = R.string.logs_action_share),
                            onClick = onShare,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        MiuixTextButton(
                            text = stringResource(id = R.string.close),
                            onClick = onDismiss,
                        )
                    }
                }
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 26.dp)
                    .fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp),
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    LogcatCard(logs = content.ifBlank { "-" })
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onShare) {
                            Text(text = stringResource(id = R.string.logs_action_share))
                        }
                        TextButton(onClick = onDismiss) {
                            Text(text = stringResource(id = R.string.close))
                        }
                    }
                }
            }
        }
    }
}

private fun shareLogFile(
    context: android.content.Context,
    filePath: String,
) {
    runCatching {
        val file = File(filePath)
        if (!file.exists()) {
            return
        }
        val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                context.getString(R.string.logs_share_chooser),
            ),
        )
    }.onFailure {
        Toast.makeText(context, context.getString(R.string.logs_status_share_failed), Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun logTypeLabel(type: StoredLogType): String {
    return when (type) {
        StoredLogType.INSTALLATION -> stringResource(id = R.string.logs_filter_type_installation)
        StoredLogType.AUTO_MODE -> stringResource(id = R.string.logs_filter_type_auto)
        StoredLogType.CAT -> stringResource(id = R.string.logs_filter_type_cat)
        StoredLogType.APP -> "App"
        StoredLogType.BOOT -> "Boot"
        StoredLogType.OTHER -> stringResource(id = R.string.logs_filter_type_all)
    }
}

private fun buildMetaText(
    entry: StoredLogEntry,
    typeLabel: String,
): String {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(entry.createdAtMillis))
    val size = formatSize(entry.sizeBytes)
    return "$typeLabel • $date • $size"
}

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) {
        return "${bytes}B"
    }
    val kb = bytes / 1024f
    if (kb < 1024f) {
        return String.format(Locale.US, "%.1fKB", kb)
    }
    val mb = kb / 1024f
    if (mb < 1024f) {
        return String.format(Locale.US, "%.1fMB", mb)
    }
    val gb = mb / 1024f
    return String.format(Locale.US, "%.2fGB", gb)
}
