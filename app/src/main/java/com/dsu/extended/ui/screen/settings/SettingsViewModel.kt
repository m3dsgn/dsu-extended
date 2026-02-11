package com.dsu.extended.ui.screen.settings

import android.app.Application
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dsu.extended.core.BaseViewModel
import com.dsu.extended.model.Session
import com.dsu.extended.preferences.AppPrefs
import com.dsu.extended.ui.theme.AppFontPreset
import com.dsu.extended.ui.theme.ColorPaletteStyle
import com.dsu.extended.ui.theme.ThemeMode
import com.dsu.extended.ui.theme.UiStyle
import com.dsu.extended.util.OperationMode
import com.dsu.extended.util.OperationModeUtils
import com.dsu.extended.util.PreferredPrivilegedMode
import com.dsu.extended.util.AppLogger
import com.dsu.extended.util.DataStoreUtils

@HiltViewModel
class SettingsViewModel @Inject constructor(
    override val dataStore: DataStore<Preferences>,
    private val session: Session,
    val application: Application,
) : BaseViewModel(dataStore) {

    private val tag = this.javaClass.simpleName

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun reloadPreferences() {
        uiState.value.preferences.forEach { entry ->
            viewModelScope.launch {
                val isEnabled = readBoolPref(entry.key)
                togglePreference(entry.key, isEnabled)
            }
        }

        viewModelScope.launch {
            val preferredMode = PreferredPrivilegedMode.fromPreference(readStringPref(AppPrefs.OPERATION_MODE_OVERRIDE))
            val uiStyle = UiStyle.fromPreference(readStringPref(AppPrefs.UI_STYLE))
            val savedAppFontPreset = AppFontPreset.fromPreference(readStringPref(AppPrefs.APP_FONT_PRESET))
            val appFontPreset =
                if (uiStyle == UiStyle.MIUIX && savedAppFontPreset == AppFontPreset.SYSTEM_DEFAULT) {
                    updateStringPref(AppPrefs.APP_FONT_PRESET, AppFontPreset.MANROPE.value)
                    AppFontPreset.MANROPE
                } else {
                    savedAppFontPreset
                }
            val themeMode = ThemeMode.fromPreference(readStringPref(AppPrefs.THEME_MODE))
            val colorStyle = ColorPaletteStyle.fromPreference(readStringPref(AppPrefs.MATERIAL_COLOR_STYLE))
            val dynamicColor = DataStoreUtils.readBoolPref(dataStore, AppPrefs.USE_DYNAMIC_COLOR, false)
            val normalizedColorStyle = if (uiStyle == UiStyle.MIUIX) ColorPaletteStyle.TONAL_SPOT else colorStyle
            val normalizedDynamicColor = if (uiStyle == UiStyle.MIUIX) false else dynamicColor
            if (uiStyle == UiStyle.MIUIX && (dynamicColor || colorStyle != ColorPaletteStyle.TONAL_SPOT)) {
                updateBoolPref(AppPrefs.USE_DYNAMIC_COLOR, false)
                updateStringPref(AppPrefs.MATERIAL_COLOR_STYLE, ColorPaletteStyle.TONAL_SPOT.value)
            }
            val hasRoot = Shell.getShell().isRoot
            val hasShizuku = OperationModeUtils.isShizukuPermissionGranted(application)
            val hasDhizuku = OperationModeUtils.isDhizukuPermissionGranted(application)
            val hasSystemDsu = OperationModeUtils.isDsuPermissionGranted(application)
            val canLoadGsiPrivileged = hasRoot || hasShizuku || hasDhizuku || hasSystemDsu

            _uiState.update {
                it.copy(
                    isRoot = session.isRoot(),
                    hasRootAccess = hasRoot,
                    hasShizukuAccess = hasShizuku,
                    hasDhizukuAccess = hasDhizuku,
                    canLoadGsiPrivileged = canLoadGsiPrivileged,
                    preferredPrivilegedMode = preferredMode,
                    uiStyle = uiStyle,
                    appFontPreset = appFontPreset,
                    themeMode = themeMode,
                    useDynamicColor = normalizedDynamicColor,
                    colorPaletteStyle = normalizedColorStyle,
                )
            }
        }
    }

    init {
        reloadPreferences()
    }

    fun togglePreference(preference: String, value: Boolean) {
        viewModelScope.launch {
            updateBoolPref(preference, value) {
                _uiState.update {
                    val cloneMap = hashMapOf<String, Boolean>()
                    cloneMap.putAll(uiState.value.preferences)
                    cloneMap[preference] = value
                    AppLogger.d(tag, "Preference updated", "preference" to preference, "value" to value)
                    it.copy(preferences = cloneMap)
                }
            }
        }
    }

    fun isAndroidQ(): Boolean = Build.VERSION.SDK_INT == 29

    fun updateSheetDisplay(sheet: DialogSheetState) {
        _uiState.update { it.copy(dialogSheetDisplay = sheet) }
    }

    fun checkOperationMode(): String {
        return OperationModeUtils.getOperationModeAsString(session.getOperationMode())
    }

    fun getOperationMode(): OperationMode {
        return session.getOperationMode()
    }

    fun setPreferredPrivilegedMode(mode: PreferredPrivilegedMode) {
        viewModelScope.launch {
            updateStringPref(AppPrefs.OPERATION_MODE_OVERRIDE, mode.value)
            _uiState.update { it.copy(preferredPrivilegedMode = mode) }
            refreshPrivilegedChecks()
            AppLogger.i(
                tag,
                "Preferred privileged mode updated",
                "preferredMode" to mode,
            )
        }
    }

    fun refreshPrivilegedChecks() {
        viewModelScope.launch {
            val hasRoot = Shell.getShell().isRoot
            val hasShizuku = OperationModeUtils.isShizukuPermissionGranted(application)
            val hasDhizuku = OperationModeUtils.isDhizukuPermissionGranted(application)
            val hasSystemDsu = OperationModeUtils.isDsuPermissionGranted(application)
            val canLoadGsiPrivileged = hasRoot || hasShizuku || hasDhizuku || hasSystemDsu

            _uiState.update {
                it.copy(
                    isRoot = session.isRoot(),
                    hasRootAccess = hasRoot,
                    hasShizukuAccess = hasShizuku,
                    hasDhizukuAccess = hasDhizuku,
                    canLoadGsiPrivileged = canLoadGsiPrivileged,
                )
            }
            AppLogger.i(
                tag,
                "Check ALL completed",
                "root" to hasRoot,
                "shizuku" to hasShizuku,
                "dhizuku" to hasDhizuku,
                "canLoadGsiPrivileged" to canLoadGsiPrivileged,
            )
        }
    }

    fun setUiStyle(style: UiStyle) {
        viewModelScope.launch {
            updateStringPref(AppPrefs.UI_STYLE, style.value)
            if (style == UiStyle.MIUIX) {
                updateBoolPref(AppPrefs.USE_DYNAMIC_COLOR, false)
                updateStringPref(AppPrefs.MATERIAL_COLOR_STYLE, ColorPaletteStyle.TONAL_SPOT.value)
                updateStringPref(AppPrefs.APP_FONT_PRESET, AppFontPreset.MANROPE.value)
                _uiState.update {
                    it.copy(
                        uiStyle = style,
                        appFontPreset = AppFontPreset.MANROPE,
                        useDynamicColor = false,
                        colorPaletteStyle = ColorPaletteStyle.TONAL_SPOT,
                    )
                }
            } else {
                _uiState.update { it.copy(uiStyle = style) }
            }
            AppLogger.i(tag, "UI style updated", "uiStyle" to style)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            updateStringPref(AppPrefs.THEME_MODE, mode.value)
            _uiState.update { it.copy(themeMode = mode) }
            AppLogger.i(tag, "Theme mode updated", "themeMode" to mode)
        }
    }

    fun setAppFontPreset(preset: AppFontPreset) {
        viewModelScope.launch {
            updateStringPref(AppPrefs.APP_FONT_PRESET, preset.value)
            _uiState.update { it.copy(appFontPreset = preset) }
            AppLogger.i(tag, "App font preset updated", "preset" to preset)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            if (uiState.value.uiStyle == UiStyle.MIUIX) {
                updateBoolPref(AppPrefs.USE_DYNAMIC_COLOR, false)
                _uiState.update { it.copy(useDynamicColor = false) }
                AppLogger.i(tag, "Dynamic color reset to stock for MIUIX")
            } else {
                updateBoolPref(AppPrefs.USE_DYNAMIC_COLOR, enabled)
                _uiState.update { it.copy(useDynamicColor = enabled) }
                AppLogger.i(tag, "Dynamic color updated", "enabled" to enabled)
            }
        }
    }

    fun setColorPaletteStyle(style: ColorPaletteStyle) {
        viewModelScope.launch {
            updateStringPref(AppPrefs.MATERIAL_COLOR_STYLE, style.value)
            _uiState.update { it.copy(colorPaletteStyle = style) }
            AppLogger.i(tag, "Material color style updated", "style" to style)
        }
    }

    fun checkDevOpt() {
        viewModelScope.launch {
            val isDevOptEnabled = readBoolPref(AppPrefs.DEVELOPER_OPTIONS)
            _uiState.update { it.copy(isDevOptEnabled = isDevOptEnabled) }
            if (isDevOptEnabled) {
                reloadPreferences()
            }
        }
    }
}
