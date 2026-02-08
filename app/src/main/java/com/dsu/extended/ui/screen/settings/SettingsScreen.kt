package com.dsu.extended.ui.screen.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dsu.extended.MainActivity
import com.dsu.extended.R
import com.dsu.extended.preferences.AppPrefs
import com.dsu.extended.ui.components.ApplicationScreen
import com.dsu.extended.ui.components.DialogLikeBottomSheet
import com.dsu.extended.ui.components.DynamicListItem
import com.dsu.extended.ui.components.PreferenceItem
import com.dsu.extended.ui.components.TopBar
import com.dsu.extended.ui.screen.Destinations
import com.dsu.extended.ui.theme.ColorPaletteStyle
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.ThemeMode
import com.dsu.extended.ui.theme.UiStyle
import com.dsu.extended.util.OperationMode
import com.dsu.extended.util.PreferredPrivilegedMode
import com.dsu.extended.util.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.extra.WindowListPopup
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.icon.basic.Check
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

    val otherItems = buildList<@Composable () -> Unit> {
        if (uiState.uiStyle == UiStyle.MIUIX) {
            add {
                MiuixUiStyleSpinner(
                    selectedStyle = uiState.uiStyle,
                    onSelectStyle = { settingsViewModel.setUiStyle(it) },
                )
            }
            add {
                MiuixOperationModeSpinner(
                    selectedMode = uiState.preferredPrivilegedMode,
                    onSelectMode = { settingsViewModel.setPreferredPrivilegedMode(it) },
                )
            }
            add {
                MiuixThemeModeSpinner(
                    selectedMode = uiState.themeMode,
                    onSelectMode = { settingsViewModel.setThemeMode(it) },
                )
            }
        } else {
            add {
                PreferenceItem(
                    title = stringResource(id = R.string.ui_engine_title),
                    description = stringResource(id = R.string.ui_engine_expressive),
                    icon = Icons.Rounded.Palette,
                    onClick = {
                        dialogSheetDisplay = DialogSheetState.UI_STYLE_SELECTOR
                    },
                )
            }
            add {
                PreferenceItem(
                    title = stringResource(id = R.string.operation_mode),
                    description = settingsViewModel.checkOperationMode() + " · " + when (uiState.preferredPrivilegedMode) {
                        PreferredPrivilegedMode.ALL -> stringResource(id = R.string.operation_mode_preferred_all)
                        PreferredPrivilegedMode.AUTO -> stringResource(id = R.string.operation_mode_preferred_all)
                        PreferredPrivilegedMode.ROOT -> stringResource(id = R.string.operation_mode_preferred_root)
                        PreferredPrivilegedMode.SHIZUKU -> stringResource(id = R.string.operation_mode_preferred_shizuku)
                        PreferredPrivilegedMode.DHIZUKU -> stringResource(id = R.string.operation_mode_preferred_dhizuku)
                    },
                    icon = Icons.Rounded.Settings,
                    onClick = {
                        dialogSheetDisplay = DialogSheetState.OPERATION_MODE_SELECTOR
                    },
                )
            }
            add {
                PreferenceItem(
                    title = stringResource(id = R.string.theme_mode),
                    description =
                    when (uiState.themeMode) {
                        ThemeMode.SYSTEM -> stringResource(id = R.string.theme_mode_system)
                        ThemeMode.LIGHT -> stringResource(id = R.string.theme_mode_light)
                        ThemeMode.DARK -> stringResource(id = R.string.theme_mode_dark)
                        ThemeMode.OLED -> stringResource(id = R.string.theme_mode_oled)
                    },
                    icon = Icons.Rounded.Settings,
                    onClick = {
                        dialogSheetDisplay = DialogSheetState.THEME_MODE_SELECTOR
                    },
                )
            }
            add {
                PreferenceItem(
                    title = stringResource(id = R.string.material_color_style),
                    description =
                    when (uiState.colorPaletteStyle) {
                        ColorPaletteStyle.TONAL_SPOT -> stringResource(id = R.string.material_color_style_tonal_spot)
                        ColorPaletteStyle.EXPRESSIVE -> stringResource(id = R.string.material_color_style_expressive)
                        ColorPaletteStyle.VIBRANT -> stringResource(id = R.string.material_color_style_vibrant)
                        ColorPaletteStyle.MONOCHROME -> stringResource(id = R.string.material_color_style_monochrome)
                    },
                    icon = Icons.Rounded.Palette,
                    onClick = {
                        dialogSheetDisplay = DialogSheetState.COLOR_STYLE_SELECTOR
                    },
                )
            }
        }
        add {
            PreferenceItem(
                title = stringResource(id = R.string.dynamic_color_title),
                description = if (uiState.uiStyle == UiStyle.MIUIX) {
                    stringResource(id = R.string.dynamic_color_description_off)
                } else if (uiState.useDynamicColor) {
                    stringResource(id = R.string.dynamic_color_description_on)
                } else {
                    stringResource(id = R.string.dynamic_color_description_off)
                },
                icon = Icons.Rounded.ColorLens,
                showToggle = true,
                isEnabled = uiState.uiStyle != UiStyle.MIUIX,
                isChecked = uiState.useDynamicColor,
                onClick = { settingsViewModel.setDynamicColor(!it) },
            )
        }
        add {
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
        add {
            PreferenceItem(
                title = stringResource(id = R.string.about),
                description = stringResource(id = R.string.about_description),
                icon = Icons.Rounded.Description,
                onClick = { navigate(Destinations.About) },
            )
        }
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
            MiuixSettingsSection(
                title = stringResource(id = R.string.installation),
                items = installationItems,
            )
            if (developerItems.isNotEmpty()) {
                MiuixSettingsSection(
                    title = stringResource(id = R.string.developer_options),
                    items = developerItems,
                )
            }
            MiuixSettingsSection(
                title = stringResource(id = R.string.other),
                items = otherItems,
            )
        } else {
            ExpressiveSettingsSection(
                title = stringResource(id = R.string.installation),
                items = installationItems,
            )
            if (developerItems.isNotEmpty()) {
                ExpressiveSettingsSection(
                    title = stringResource(id = R.string.developer_options),
                    items = developerItems,
                )
            }
            ExpressiveSettingsSection(
                title = stringResource(id = R.string.other),
                items = otherItems,
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

        else -> {}
    }
}

@Composable
private fun ExpressiveSettingsSection(
    title: String,
    items: List<@Composable () -> Unit>,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 8.dp),
    )
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items.forEachIndexed { index, item ->
            DynamicListItem(listLength = items.lastIndex, currentValue = index) {
                item()
            }
        }
    }
}

@Composable
private fun MiuixSettingsSection(
    title: String,
    items: List<@Composable () -> Unit>,
) {
    SmallTitle(title)
    MiuixCard(
        modifier = Modifier.padding(bottom = 10.dp),
    ) {
        items.forEach { item ->
            item()
        }
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
private fun CheckAllStatusRow(
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

@Composable
private fun MiuixUiStyleSpinner(
    selectedStyle: UiStyle,
    onSelectStyle: (UiStyle) -> Unit,
) {
    val options = listOf(
        MiuixSelectorOption(
            key = UiStyle.EXPRESSIVE.name,
            title = stringResource(id = R.string.ui_engine_expressive),
            summary = stringResource(id = R.string.ui_engine_expressive_desc),
        ),
        MiuixSelectorOption(
            key = UiStyle.MIUIX.name,
            title = stringResource(id = R.string.ui_engine_miuix),
            summary = stringResource(id = R.string.ui_engine_miuix_desc),
        ),
    )
    val selectedKey = selectedStyle.name

    MiuixPopupSelector(
        title = stringResource(id = R.string.ui_engine_title),
        options = options,
        selectedKey = selectedKey,
        onSelect = { key ->
            runCatching { UiStyle.valueOf(key) }.getOrNull()?.let { mode ->
                if (mode != selectedStyle) {
                    onSelectStyle(mode)
                }
            }
        },
    )
}

@Composable
private fun MiuixOperationModeSpinner(
    selectedMode: PreferredPrivilegedMode,
    onSelectMode: (PreferredPrivilegedMode) -> Unit,
) {
    val options = listOf(
        MiuixSelectorOption(
            key = PreferredPrivilegedMode.ALL.name,
            title = stringResource(id = R.string.operation_mode_force_all),
            summary = stringResource(id = R.string.operation_mode_force_all_description),
        ),
        MiuixSelectorOption(
            key = PreferredPrivilegedMode.ROOT.name,
            title = stringResource(id = R.string.operation_mode_force_root),
            summary = stringResource(id = R.string.operation_mode_force_root_description),
        ),
        MiuixSelectorOption(
            key = PreferredPrivilegedMode.SHIZUKU.name,
            title = stringResource(id = R.string.operation_mode_force_shizuku),
            summary = stringResource(id = R.string.operation_mode_force_shizuku_description),
        ),
        MiuixSelectorOption(
            key = PreferredPrivilegedMode.DHIZUKU.name,
            title = stringResource(id = R.string.operation_mode_force_dhizuku),
            summary = stringResource(id = R.string.operation_mode_force_dhizuku_description),
        ),
    )
    val selectedKey = selectedMode.name

    MiuixPopupSelector(
        title = stringResource(id = R.string.operation_mode),
        options = options,
        selectedKey = selectedKey,
        onSelect = { key ->
            runCatching { PreferredPrivilegedMode.valueOf(key) }.getOrNull()?.let { mode ->
                if (mode != selectedMode) {
                    onSelectMode(mode)
                }
            }
        },
    )
}

@Composable
private fun MiuixThemeModeSpinner(
    selectedMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
) {
    val options = listOf(
        MiuixSelectorOption(
            key = ThemeMode.LIGHT.name,
            title = stringResource(id = R.string.theme_mode_light),
        ),
        MiuixSelectorOption(
            key = ThemeMode.DARK.name,
            title = stringResource(id = R.string.theme_mode_dark),
        ),
        MiuixSelectorOption(
            key = ThemeMode.SYSTEM.name,
            title = stringResource(id = R.string.theme_mode_system),
        ),
        MiuixSelectorOption(
            key = ThemeMode.OLED.name,
            title = stringResource(id = R.string.theme_mode_oled),
        ),
    )
    val selectedKey = selectedMode.name

    MiuixPopupSelector(
        title = stringResource(id = R.string.theme_mode),
        options = options,
        selectedKey = selectedKey,
        onSelect = { key ->
            runCatching { ThemeMode.valueOf(key) }.getOrNull()?.let { mode ->
                if (mode != selectedMode) {
                    onSelectMode(mode)
                }
            }
        },
    )
}

private data class MiuixSelectorOption(
    val key: String,
    val title: String,
    val summary: String = "",
)

private val MiuixDropdownPositionProvider =
    object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowBounds: IntRect,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
            popupMargin: IntRect,
            alignment: PopupPositionProvider.Align,
        ): IntOffset {
            val offsetX =
                if (alignment == PopupPositionProvider.Align.End) {
                    anchorBounds.right - popupContentSize.width - popupMargin.right
                } else {
                    anchorBounds.left + popupMargin.left
                }
            val offsetY =
                if (windowBounds.bottom - anchorBounds.bottom > popupContentSize.height) {
                    anchorBounds.bottom + popupMargin.bottom
                } else if (anchorBounds.top - windowBounds.top > popupContentSize.height) {
                    anchorBounds.top - popupContentSize.height - popupMargin.top
                } else {
                    anchorBounds.top + anchorBounds.height / 2 - popupContentSize.height / 2
                }
            return IntOffset(
                x =
                    offsetX.coerceIn(
                        windowBounds.left,
                        (windowBounds.right - popupContentSize.width - popupMargin.right).coerceAtLeast(windowBounds.left),
                    ),
                y =
                    offsetY.coerceIn(
                        (windowBounds.top + popupMargin.top).coerceAtMost(windowBounds.bottom - popupContentSize.height - popupMargin.bottom),
                        windowBounds.bottom - popupContentSize.height - popupMargin.bottom,
                    ),
            )
        }

        override fun getMargins(): PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    }

@Composable
private fun MiuixPopupSelector(
    title: String,
    options: List<MiuixSelectorOption>,
    selectedKey: String,
    onSelect: (String) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val selectedTitle = options.firstOrNull { it.key == selectedKey }?.title.orEmpty()

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    expanded.value = !expanded.value
                    if (expanded.value) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    }
                }
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = selectedTitle,
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = 172.dp),
            )
            Icon(
                imageVector = MiuixIcons.Basic.ArrowUpDown,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        }

        WindowListPopup(
            show = expanded,
            popupPositionProvider = MiuixDropdownPositionProvider,
            alignment = PopupPositionProvider.Align.End,
            enableWindowDim = true,
            onDismissRequest = { expanded.value = false },
            maxHeight = 320.dp,
            minWidth = 220.dp,
        ) {
            ListPopupColumn {
                options.forEachIndexed { index, option ->
                    DropdownImpl(
                        text = option.title,
                        optionSize = options.size,
                        isSelected = selectedKey == option.key,
                        index = index,
                        onSelectedIndexChange = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            expanded.value = false
                            onSelect(option.key)
                        },
                    )
                }
            }
        }
    }
}

private data class SelectionOption(
    val key: String,
    val title: String,
    val description: String = "",
    val icon: ImageVector = Icons.Rounded.Settings,
)

@Composable
private fun UiStyleSelectorMiuixMenu(
    selectedStyle: UiStyle,
    onDismiss: () -> Unit,
    onSelectStyle: (UiStyle) -> Unit,
) {
    val options = listOf(
        SelectionOption(
            key = UiStyle.EXPRESSIVE.name,
            title = stringResource(id = R.string.ui_engine_expressive),
            description = stringResource(id = R.string.ui_engine_expressive_desc),
        ),
        SelectionOption(
            key = UiStyle.MIUIX.name,
            title = stringResource(id = R.string.ui_engine_miuix),
            description = stringResource(id = R.string.ui_engine_miuix_desc),
        ),
    )
    SelectionDialog(
        title = stringResource(id = R.string.ui_engine_title),
        options = options,
        selectedKey = selectedStyle.name,
        onDismiss = onDismiss,
        onSelect = { selected ->
            onSelectStyle(UiStyle.valueOf(selected))
        },
    )
}

@Composable
private fun UiStyleSelectorExpressiveMenu(
    selectedStyle: UiStyle,
    onDismiss: () -> Unit,
    onSelectStyle: (UiStyle) -> Unit,
) {
    val options = listOf(
        SelectionOption(
            key = UiStyle.EXPRESSIVE.name,
            icon = Icons.Rounded.Palette,
            title = stringResource(id = R.string.ui_engine_expressive),
            description = stringResource(id = R.string.ui_engine_expressive_desc),
        ),
        SelectionOption(
            key = UiStyle.MIUIX.name,
            icon = Icons.Rounded.Settings,
            title = stringResource(id = R.string.ui_engine_miuix),
            description = stringResource(id = R.string.ui_engine_miuix_desc),
        ),
    )
    SelectionDialog(
        title = stringResource(id = R.string.ui_engine_title),
        options = options,
        selectedKey = selectedStyle.name,
        onDismiss = onDismiss,
        onSelect = { selected ->
            onSelectStyle(UiStyle.valueOf(selected))
        },
    )
}

@Composable
private fun OperationModeSelectorMiuixMenu(
    selectedMode: PreferredPrivilegedMode,
    onDismiss: () -> Unit,
    onSelectMode: (PreferredPrivilegedMode) -> Unit,
) {
    val options = listOf(
        SelectionOption(
            key = PreferredPrivilegedMode.ALL.name,
            title = stringResource(id = R.string.operation_mode_force_all),
            description = stringResource(id = R.string.operation_mode_force_all_description),
        ),
        SelectionOption(
            key = PreferredPrivilegedMode.ROOT.name,
            title = stringResource(id = R.string.operation_mode_force_root),
            description = stringResource(id = R.string.operation_mode_force_root_description),
        ),
        SelectionOption(
            key = PreferredPrivilegedMode.SHIZUKU.name,
            title = stringResource(id = R.string.operation_mode_force_shizuku),
            description = stringResource(id = R.string.operation_mode_force_shizuku_description),
        ),
        SelectionOption(
            key = PreferredPrivilegedMode.DHIZUKU.name,
            title = stringResource(id = R.string.operation_mode_force_dhizuku),
            description = stringResource(id = R.string.operation_mode_force_dhizuku_description),
        ),
    )
    SelectionDialog(
        title = stringResource(id = R.string.operation_mode),
        options = options,
        selectedKey = selectedMode.name,
        onDismiss = onDismiss,
        onSelect = { selected ->
            onSelectMode(PreferredPrivilegedMode.valueOf(selected))
        },
    )
}

@Composable
private fun ThemeModeSelectorMiuixMenu(
    selectedMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelectMode: (ThemeMode) -> Unit,
) {
    val options = listOf(
        SelectionOption(
            key = ThemeMode.LIGHT.name,
            title = stringResource(id = R.string.theme_mode_light),
        ),
        SelectionOption(
            key = ThemeMode.DARK.name,
            title = stringResource(id = R.string.theme_mode_dark),
        ),
        SelectionOption(
            key = ThemeMode.SYSTEM.name,
            title = stringResource(id = R.string.theme_mode_system),
        ),
        SelectionOption(
            key = ThemeMode.OLED.name,
            title = stringResource(id = R.string.theme_mode_oled),
        ),
    )
    SelectionDialog(
        title = stringResource(id = R.string.theme_mode),
        options = options,
        selectedKey = selectedMode.name,
        onDismiss = onDismiss,
        onSelect = { selected ->
            onSelectMode(ThemeMode.valueOf(selected))
        },
    )
}

@Composable
private fun ColorStyleSelectorMiuixMenu(
    selectedStyle: ColorPaletteStyle,
    onDismiss: () -> Unit,
    onSelectStyle: (ColorPaletteStyle) -> Unit,
) {
    val options = listOf(
        SelectionOption(
            key = ColorPaletteStyle.TONAL_SPOT.name,
            title = stringResource(id = R.string.material_color_style_tonal_spot),
        ),
        SelectionOption(
            key = ColorPaletteStyle.EXPRESSIVE.name,
            title = stringResource(id = R.string.material_color_style_expressive),
        ),
        SelectionOption(
            key = ColorPaletteStyle.VIBRANT.name,
            title = stringResource(id = R.string.material_color_style_vibrant),
        ),
        SelectionOption(
            key = ColorPaletteStyle.MONOCHROME.name,
            title = stringResource(id = R.string.material_color_style_monochrome),
        ),
    )
    SelectionDialog(
        title = stringResource(id = R.string.material_color_style),
        options = options,
        selectedKey = selectedStyle.name,
        onDismiss = onDismiss,
        onSelect = { selected ->
            onSelectStyle(ColorPaletteStyle.valueOf(selected))
        },
    )
}

@Composable
private fun OperationModeSelectorExpressiveMenu(
    selectedMode: PreferredPrivilegedMode,
    onDismiss: () -> Unit,
    onSelectMode: (PreferredPrivilegedMode) -> Unit,
) {
    val options = listOf(
        SelectionOption(
            key = PreferredPrivilegedMode.ALL.name,
            icon = Icons.Rounded.Settings,
            title = stringResource(id = R.string.operation_mode_force_all),
            description = stringResource(id = R.string.operation_mode_force_all_description),
        ),
        SelectionOption(
            key = PreferredPrivilegedMode.ROOT.name,
            icon = Icons.Rounded.WarningAmber,
            title = stringResource(id = R.string.operation_mode_force_root),
            description = stringResource(id = R.string.operation_mode_force_root_description),
        ),
        SelectionOption(
            key = PreferredPrivilegedMode.SHIZUKU.name,
            icon = Icons.Rounded.NewReleases,
            title = stringResource(id = R.string.operation_mode_force_shizuku),
            description = stringResource(id = R.string.operation_mode_force_shizuku_description),
        ),
        SelectionOption(
            key = PreferredPrivilegedMode.DHIZUKU.name,
            icon = Icons.Rounded.Palette,
            title = stringResource(id = R.string.operation_mode_force_dhizuku),
            description = stringResource(id = R.string.operation_mode_force_dhizuku_description),
        ),
    )
    SelectionDialog(
        title = stringResource(id = R.string.operation_mode),
        options = options,
        selectedKey = selectedMode.name,
        onDismiss = onDismiss,
        onSelect = { selected ->
            onSelectMode(PreferredPrivilegedMode.valueOf(selected))
        },
    )
}

@Composable
private fun ThemeModeSelectorExpressiveMenu(
    selectedMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelectMode: (ThemeMode) -> Unit,
) {
    val options = listOf(
        SelectionOption(
            key = ThemeMode.SYSTEM.name,
            icon = Icons.Rounded.Settings,
            title = stringResource(id = R.string.theme_mode_system),
        ),
        SelectionOption(
            key = ThemeMode.LIGHT.name,
            icon = Icons.Rounded.NewReleases,
            title = stringResource(id = R.string.theme_mode_light),
        ),
        SelectionOption(
            key = ThemeMode.DARK.name,
            icon = Icons.Rounded.WarningAmber,
            title = stringResource(id = R.string.theme_mode_dark),
        ),
        SelectionOption(
            key = ThemeMode.OLED.name,
            icon = Icons.Rounded.Storage,
            title = stringResource(id = R.string.theme_mode_oled),
        ),
    )
    SelectionDialog(
        title = stringResource(id = R.string.theme_mode),
        options = options,
        selectedKey = selectedMode.name,
        onDismiss = onDismiss,
        onSelect = { selected ->
            onSelectMode(ThemeMode.valueOf(selected))
        },
    )
}

@Composable
private fun ColorStyleSelectorExpressiveMenu(
    selectedStyle: ColorPaletteStyle,
    onDismiss: () -> Unit,
    onSelectStyle: (ColorPaletteStyle) -> Unit,
) {
    val options = listOf(
        SelectionOption(
            key = ColorPaletteStyle.TONAL_SPOT.name,
            icon = Icons.Rounded.Settings,
            title = stringResource(id = R.string.material_color_style_tonal_spot),
        ),
        SelectionOption(
            key = ColorPaletteStyle.EXPRESSIVE.name,
            icon = Icons.Rounded.NewReleases,
            title = stringResource(id = R.string.material_color_style_expressive),
        ),
        SelectionOption(
            key = ColorPaletteStyle.VIBRANT.name,
            icon = Icons.Rounded.WarningAmber,
            title = stringResource(id = R.string.material_color_style_vibrant),
        ),
        SelectionOption(
            key = ColorPaletteStyle.MONOCHROME.name,
            icon = Icons.Rounded.Storage,
            title = stringResource(id = R.string.material_color_style_monochrome),
        ),
    )
    SelectionDialog(
        title = stringResource(id = R.string.material_color_style),
        options = options,
        selectedKey = selectedStyle.name,
        onDismiss = onDismiss,
        onSelect = { selected ->
            onSelectStyle(ColorPaletteStyle.valueOf(selected))
        },
    )
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<SelectionOption>,
    selectedKey: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixSelectionDialog(
            title = title,
            options = options,
            selectedKey = selectedKey,
            onDismiss = onDismiss,
            onSelect = onSelect,
        )
    } else {
        ExpressiveSelectionDialog(
            title = title,
            options = options,
            selectedKey = selectedKey,
            onDismiss = onDismiss,
            onSelect = onSelect,
        )
    }
}

@Composable
private fun MiuixSelectionDialog(
    title: String,
    options: List<SelectionOption>,
    selectedKey: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(horizontal = 34.dp)
                .widthIn(max = 340.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                options.forEach { option ->
                    MiuixSelectionRow(
                        title = option.title,
                        description = option.description,
                        selected = selectedKey == option.key,
                        onClick = {
                            onSelect(option.key)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixSelectionRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val rowColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            } else {
                Color.Transparent
            },
        animationSpec = spring(),
        label = "miuixSelectionRowColor",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            }
            .padding(horizontal = 8.dp)
            .background(rowColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.title4,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MiuixTheme.textStyles.body2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = MiuixIcons.Basic.Check,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        )
    }
}

@Composable
private fun ExpressiveSelectionDialog(
    title: String,
    options: List<SelectionOption>,
    selectedKey: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    ExpressiveSelectionRow(
                        option = option,
                        selected = selectedKey == option.key,
                        onClick = {
                            onSelect(option.key)
                            onDismiss()
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.close))
            }
        },
    )
}

@Composable
private fun ExpressiveSelectionRow(
    option: SelectionOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val rowScale by animateFloatAsState(
        targetValue = if (selected) 1.01f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "expressiveSelectorRowScale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = spring(),
        label = "expressiveSelectorRowContainer",
    )
    val titleColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = spring(),
        label = "expressiveSelectorRowTitle",
    )
    val descriptionColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = spring(),
        label = "expressiveSelectorRowDescription",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = rowScale
                scaleY = rowScale
            }
            .clickable(onClick = onClick)
            .background(
                color = containerColor,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
            )
            if (option.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = descriptionColor,
                )
            }
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}
