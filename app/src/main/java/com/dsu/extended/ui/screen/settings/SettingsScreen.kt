package com.dsu.extended.ui.screen.settings

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dsu.extended.MainActivity
import com.dsu.extended.R
import com.dsu.extended.preferences.AppPrefs
import com.dsu.extended.ui.components.ApplicationScreen
import com.dsu.extended.ui.components.DialogLikeBottomSheet
import com.dsu.extended.ui.components.PreferenceItem
import com.dsu.extended.ui.components.TopBar
import com.dsu.extended.ui.screen.Destinations
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import com.dsu.extended.util.OperationMode
import com.dsu.extended.util.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navigate: (String) -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    var dialogSheetDisplay by remember { mutableStateOf(DialogSheetState.NONE) }
    val activity = LocalContext.current as? MainActivity

    LaunchedEffect(Unit) {
        settingsViewModel.checkDevOpt()
        settingsViewModel.refreshPrivilegedChecks()
    }

    val installationItems = buildList<@Composable () -> Unit> {
        add {
            PreferenceItem(
                title = stringResource(id = R.string.builtin_installer),
                description =
                if (settingsViewModel.isAndroidQ()) {
                    stringResource(id = R.string.unsupported)
                } else if (uiState.isRoot) {
                    stringResource(id = R.string.builtin_installer_description)
                } else {
                    stringResource(R.string.requires_root)
                },
                icon = Icons.Rounded.InstallMobile,
                showToggle = true,
                isEnabled = uiState.isRoot && !settingsViewModel.isAndroidQ(),
                isChecked = uiState.preferences[AppPrefs.USE_BUILTIN_INSTALLER]!!,
                onClick = {
                    if (!it) {
                        dialogSheetDisplay = DialogSheetState.BUILT_IN_INSTALLER
                    }
                    settingsViewModel.togglePreference(AppPrefs.USE_BUILTIN_INSTALLER, !it)
                },
            )
        }
        add {
            PreferenceItem(
                title = stringResource(id = R.string.unmount_sd_title),
                description = stringResource(id = R.string.unmount_sd_description),
                icon = Icons.Rounded.Storage,
                showToggle = true,
                isChecked = uiState.preferences[AppPrefs.UMOUNT_SD]!!,
                onClick = { settingsViewModel.togglePreference(AppPrefs.UMOUNT_SD, !it) },
            )
        }
        add {
            PreferenceItem(
                title = stringResource(id = R.string.keep_screen_on),
                icon = Icons.Rounded.Settings,
                showToggle = true,
                isChecked = uiState.preferences[AppPrefs.KEEP_SCREEN_ON]!!,
                onClick = { settingsViewModel.togglePreference(AppPrefs.KEEP_SCREEN_ON, !it) },
            )
        }
    }

    val developerItems = buildList<@Composable () -> Unit> {
        if (uiState.isDevOptEnabled) {
            val showFullLogcatLogging = settingsViewModel.getOperationMode() != OperationMode.ADB
            add {
                PreferenceItem(
                    title = stringResource(id = R.string.storage_check_title),
                    description = stringResource(id = R.string.storage_check_description),
                    icon = Icons.Rounded.WarningAmber,
                    showToggle = true,
                    isChecked = uiState.preferences[AppPrefs.DISABLE_STORAGE_CHECK]!!,
                    onClick = {
                        if (!it) {
                            dialogSheetDisplay = DialogSheetState.DISABLE_STORAGE_CHECK
                        }
                        settingsViewModel.togglePreference(AppPrefs.DISABLE_STORAGE_CHECK, !it)
                    },
                )
            }
            if (showFullLogcatLogging) {
                add {
                    PreferenceItem(
                        title = stringResource(id = R.string.full_logcat_logging_title),
                        description = stringResource(id = R.string.full_logcat_logging_description),
                        icon = Icons.Rounded.Description,
                        showToggle = true,
                        isChecked = uiState.preferences[AppPrefs.FULL_LOGCAT_LOGGING]!!,
                        onClick = { settingsViewModel.togglePreference(AppPrefs.FULL_LOGCAT_LOGGING, !it) },
                    )
                }
            }
        }
    }

    val checkAllItem = @Composable {
        CheckAllStatusRow(
            uiStyle = uiState.uiStyle,
            summary = buildPrivilegedCheckSummary(uiState),
            onRecheck = {
                activity?.requestPermissionsForCheckAll {
                    settingsViewModel.refreshPrivilegedChecks()
                } ?: settingsViewModel.refreshPrivilegedChecks()
            },
        )
    }

    val aboutItem = @Composable {
        PreferenceItem(
            title = stringResource(id = R.string.about),
            description = stringResource(id = R.string.about_description),
            icon = Icons.Rounded.Description,
            onClick = { navigate(Destinations.About) },
        )
    }

    ApplicationScreen(
        modifier = Modifier.padding(horizontal = 12.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.settings),
                scrollBehavior = it,
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
    ) {
        if (uiState.uiStyle == UiStyle.MIUIX) {
            SettingsContentMiuix(
                uiState = uiState,
                settingsViewModel = settingsViewModel,
                installationItems = installationItems,
                developerItems = developerItems,
                checkAllStatusRow = checkAllItem,
                aboutItem = aboutItem,
            )
        } else {
            SettingsContentExpressive(
                uiState = uiState,
                settingsViewModel = settingsViewModel,
                installationItems = installationItems,
                developerItems = developerItems,
                checkAllStatusRow = checkAllItem,
                aboutItem = aboutItem,
                onOpenDialog = { dialogSheetDisplay = it },
            )
        }
    }

    when (dialogSheetDisplay) {
        DialogSheetState.BUILT_IN_INSTALLER ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.experimental_feature),
                icon = Icons.Rounded.NewReleases,
                text = stringResource(id = R.string.experimental_feature_description),
                confirmText = stringResource(id = R.string.yes),
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.togglePreference(AppPrefs.USE_BUILTIN_INSTALLER, false)
                    dialogSheetDisplay = DialogSheetState.NONE
                },
                onClickConfirm = { dialogSheetDisplay = DialogSheetState.NONE },
            )

        DialogSheetState.DISABLE_STORAGE_CHECK ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.warning_storage_check_title),
                icon = Icons.Rounded.WarningAmber,
                text = stringResource(id = R.string.warning_storage_check_description),
                confirmText = stringResource(id = R.string.continue_anyway),
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.togglePreference(AppPrefs.DISABLE_STORAGE_CHECK, false)
                    dialogSheetDisplay = DialogSheetState.NONE
                },
                onClickConfirm = { dialogSheetDisplay = DialogSheetState.NONE },
            )

        DialogSheetState.UI_STYLE_SELECTOR ->
            if (uiState.uiStyle == UiStyle.MIUIX) {
                UiStyleSelectorMiuixMenu(
                    selectedStyle = uiState.uiStyle,
                    onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                    onSelectStyle = { style -> settingsViewModel.setUiStyle(style) },
                )
            } else {
                UiStyleSelectorExpressiveMenu(
                    selectedStyle = uiState.uiStyle,
                    onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                    onSelectStyle = { style -> settingsViewModel.setUiStyle(style) },
                )
            }

        DialogSheetState.OPERATION_MODE_SELECTOR ->
            if (uiState.uiStyle == UiStyle.MIUIX) {
                OperationModeSelectorMiuixMenu(
                    selectedMode = uiState.preferredPrivilegedMode,
                    onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                    onSelectMode = { mode ->
                        settingsViewModel.setPreferredPrivilegedMode(mode)
                    },
                )
            } else {
                OperationModeSelectorExpressiveMenu(
                    selectedMode = uiState.preferredPrivilegedMode,
                    onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                    onSelectMode = { mode ->
                        settingsViewModel.setPreferredPrivilegedMode(mode)
                    },
                )
            }

        DialogSheetState.THEME_MODE_SELECTOR ->
            if (uiState.uiStyle == UiStyle.MIUIX) {
                ThemeModeSelectorMiuixMenu(
                    selectedMode = uiState.themeMode,
                    onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                    onSelectMode = { mode ->
                        settingsViewModel.setThemeMode(mode)
                    },
                )
            } else {
                ThemeModeSelectorExpressiveMenu(
                    selectedMode = uiState.themeMode,
                    onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                    onSelectMode = { mode ->
                        settingsViewModel.setThemeMode(mode)
                    },
                )
            }

        DialogSheetState.COLOR_STYLE_SELECTOR ->
            if (uiState.uiStyle == UiStyle.MIUIX) {
                ColorStyleSelectorMiuixMenu(
                    selectedStyle = uiState.colorPaletteStyle,
                    onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                    onSelectStyle = { style ->
                        settingsViewModel.setColorPaletteStyle(style)
                    },
                )
            } else {
                ColorStyleSelectorExpressiveMenu(
                    selectedStyle = uiState.colorPaletteStyle,
                    onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                    onSelectStyle = { style ->
                        settingsViewModel.setColorPaletteStyle(style)
                    },
                )
            }

        DialogSheetState.FONT_SELECTOR ->
            FontPresetSelectorExpressiveMenu(
                selectedPreset = uiState.appFontPreset,
                onDismiss = { dialogSheetDisplay = DialogSheetState.NONE },
                onSelectPreset = { preset ->
                    settingsViewModel.setAppFontPreset(preset)
                },
            )

        else -> {}
    }
}

@Composable
private fun buildPrivilegedCheckSummary(uiState: SettingsUiState): String {
    val available = stringResource(id = R.string.check_all_status_available)
    val unavailable = stringResource(id = R.string.check_all_status_unavailable)
    val rootStatus = stringResource(id = R.string.check_all_root_short, if (uiState.hasRootAccess) available else unavailable)
    val shizukuStatus =
        stringResource(id = R.string.check_all_shizuku_short, if (uiState.hasShizukuAccess) available else unavailable)
    val dhizukuStatus =
        stringResource(id = R.string.check_all_dhizuku_short, if (uiState.hasDhizukuAccess) available else unavailable)
    val result =
        if (uiState.canLoadGsiPrivileged) {
            stringResource(id = R.string.check_all_result_ready)
        } else {
            stringResource(id = R.string.check_all_result_unavailable)
        }
    return "$rootStatus · $shizukuStatus · $dhizukuStatus\n$result"
}

@Composable
internal fun CheckAllStatusRow(
    uiStyle: UiStyle,
    summary: String,
    onRecheck: () -> Unit,
) {
    val titleStyle =
        if (uiStyle == UiStyle.MIUIX) {
            MiuixTheme.textStyles.title3
        } else {
            MaterialTheme.typography.titleMedium
        }
    val summaryStyle =
        if (uiStyle == UiStyle.MIUIX) {
            MiuixTheme.textStyles.body2
        } else {
            MaterialTheme.typography.bodyMedium
        }
    val titleColor =
        if (uiStyle == UiStyle.MIUIX) {
            MiuixTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    val summaryColor =
        if (uiStyle == UiStyle.MIUIX) {
            MiuixTheme.colorScheme.onSurfaceVariantSummary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.check_all_title),
                style = titleStyle,
                color = titleColor,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = summary,
                style = summaryStyle,
                color = summaryColor,
            )
        }
        if (uiStyle == UiStyle.MIUIX) {
            MiuixTextButton(
                text = stringResource(id = R.string.check_all_action_recheck),
                onClick = onRecheck,
            )
        } else {
            TextButton(onClick = onRecheck) {
                Text(text = stringResource(id = R.string.check_all_action_recheck))
            }
        }
    }
}

internal data class SelectionOption(
    val key: String,
    val title: String,
    val description: String = "",
    val icon: ImageVector = Icons.Rounded.Settings,
)
