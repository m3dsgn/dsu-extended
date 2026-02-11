package com.dsu.extended.ui.screen.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dsu.extended.R
import com.dsu.extended.preferences.AppPrefs
import com.dsu.extended.ui.components.PreferenceItem
import com.dsu.extended.ui.theme.AppFontPreset
import com.dsu.extended.ui.theme.ColorPaletteStyle
import com.dsu.extended.ui.theme.ThemeMode
import com.dsu.extended.ui.theme.UiStyle
import com.dsu.extended.util.PreferredPrivilegedMode
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.extra.WindowListPopup
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsContentMiuix(
    uiState: SettingsUiState,
    settingsViewModel: SettingsViewModel,
    installationItems: List<@Composable () -> Unit>,
    developerItems: List<@Composable () -> Unit>,
    checkAllStatusRow: @Composable () -> Unit,
    aboutItem: @Composable () -> Unit,
) {
    val miuixOtherItems = buildList<@Composable () -> Unit> {
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
        add {
            MiuixFontPresetSpinner(
                selectedPreset = uiState.appFontPreset,
                onSelectPreset = { settingsViewModel.setAppFontPreset(it) },
            )
        }
        add {
            PreferenceItem(
                title = stringResource(id = R.string.dynamic_color_title),
                description = stringResource(id = R.string.dynamic_color_description_off),
                icon = Icons.Rounded.ColorLens,
                showToggle = true,
                isEnabled = false,
                isChecked = uiState.useDynamicColor,
                onClick = { settingsViewModel.setDynamicColor(!it) },
            )
        }
        add(checkAllStatusRow)
        add(aboutItem)
    }

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
        items = miuixOtherItems,
    )
}

@Composable
private fun MiuixFontPresetSpinner(
    selectedPreset: AppFontPreset,
    onSelectPreset: (AppFontPreset) -> Unit,
) {
    val options = listOf(
        MiuixSelectorOption(
            key = AppFontPreset.SYSTEM_DEFAULT.name,
            title = stringResource(id = R.string.app_font_system_default),
        ),
        MiuixSelectorOption(
            key = AppFontPreset.GOOGLE_SANS_FLEX.name,
            title = stringResource(id = R.string.app_font_google_sans_flex),
        ),
        MiuixSelectorOption(
            key = AppFontPreset.ROBOTO_FLEX.name,
            title = stringResource(id = R.string.app_font_roboto_flex),
        ),
        MiuixSelectorOption(
            key = AppFontPreset.MANROPE.name,
            title = stringResource(id = R.string.app_font_manrope),
        ),
    )
    val selectedKey = selectedPreset.name

    MiuixPopupSelector(
        title = stringResource(id = R.string.app_font_title),
        options = options,
        selectedKey = selectedKey,
        onSelect = { key ->
            runCatching { AppFontPreset.valueOf(key) }.getOrNull()?.let { preset ->
                if (preset != selectedPreset) {
                    onSelectPreset(preset)
                }
            }
        },
    )
}

@Composable
internal fun MiuixSettingsSection(
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

// ═══════════════════════════════════════════════════════════════
// Menus for Dialog use (preserved for compatibility/if triggered)
// ═══════════════════════════════════════════════════════════════

@Composable
internal fun UiStyleSelectorMiuixMenu(
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
    MiuixSelectionDialog(
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
internal fun OperationModeSelectorMiuixMenu(
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
    MiuixSelectionDialog(
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
internal fun ThemeModeSelectorMiuixMenu(
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
    MiuixSelectionDialog(
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
internal fun ColorStyleSelectorMiuixMenu(
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
    MiuixSelectionDialog(
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
internal fun MiuixSelectionDialog(
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
