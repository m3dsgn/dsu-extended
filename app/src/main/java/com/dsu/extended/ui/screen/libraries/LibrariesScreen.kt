package com.dsu.extended.ui.screen.libraries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import java.util.Locale
import com.dsu.extended.R
import com.dsu.extended.ui.components.ApplicationScreen
import com.dsu.extended.ui.components.DynamicListItem
import com.dsu.extended.ui.components.PreferenceItem
import com.dsu.extended.ui.components.TopBar
import com.dsu.extended.ui.screen.Destinations
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import com.dsu.extended.util.AppLogger

private const val OFFICIAL_MIUIX_URL = "https://github.com/compose-miuix-ui/miuix"

private data class LibraryEntry(
    val name: String,
    val licenses: String,
    val website: String? = null,
)

private val fallbackLibraries = listOf(
    LibraryEntry("AndroidX Compose", "Apache-2.0", "https://developer.android.com/jetpack/compose"),
    LibraryEntry("Material 3", "Apache-2.0", "https://m3.material.io"),
    LibraryEntry("Navigation Compose", "Apache-2.0", "https://developer.android.com/jetpack/compose/navigation"),
    LibraryEntry("DataStore", "Apache-2.0", "https://developer.android.com/topic/libraries/architecture/datastore"),
    LibraryEntry("Dagger Hilt", "Apache-2.0", "https://dagger.dev/hilt"),
    LibraryEntry("Kotlin Serialization", "Apache-2.0", "https://github.com/Kotlin/kotlinx.serialization"),
    LibraryEntry("libsu", "Apache-2.0", "https://github.com/topjohnwu/libsu"),
    LibraryEntry("Shizuku", "Apache-2.0", "https://github.com/RikkaApps/Shizuku"),
    LibraryEntry("Dhizuku API", "Apache-2.0", "https://github.com/iamr0s/Dhizuku-API"),
    LibraryEntry("MIUIX KMP", "Apache-2.0", OFFICIAL_MIUIX_URL),
    LibraryEntry("AboutLibraries", "Apache-2.0", "https://github.com/mikepenz/AboutLibraries"),
    LibraryEntry("HiddenApiBypass", "Apache-2.0", "https://github.com/LSPosed/AndroidHiddenApiBypass"),
    LibraryEntry("XZ for Java", "Public Domain", "https://tukaani.org/xz/java.html"),
    LibraryEntry("Apache Commons Compress", "Apache-2.0", "https://commons.apache.org/proper/commons-compress/"),
)

private fun normalizeLibraryEntry(entry: LibraryEntry): LibraryEntry {
    val normalizedName = entry.name.trim()
    val normalizedWebsite = entry.website?.trim()
    val isMiuixByName = normalizedName.contains("miuix", ignoreCase = true)
    val isMiuixByUrl = normalizedWebsite?.contains("yukonga/miuix", ignoreCase = true) == true
    val website =
        if (isMiuixByName || isMiuixByUrl) {
            OFFICIAL_MIUIX_URL
        } else {
            normalizedWebsite
        }
    return entry.copy(name = normalizedName, website = website)
}

private fun mergeLibraries(
    generatedLibraries: List<LibraryEntry>,
    fallbackLibraries: List<LibraryEntry>,
): List<LibraryEntry> {
    val mergedByName = linkedMapOf<String, LibraryEntry>()
    (fallbackLibraries + generatedLibraries)
        .map(::normalizeLibraryEntry)
        .forEach { item ->
            val key = item.name.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "")
            val existing = mergedByName[key]
            if (existing == null) {
                mergedByName[key] = item
            } else {
                mergedByName[key] = existing.copy(
                    licenses = existing.licenses.ifBlank { item.licenses },
                    website = existing.website?.takeIf { it.isNotBlank() } ?: item.website,
                )
            }
        }
    return mergedByName.values.toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(
    navigate: (String) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val uiStyle = LocalUiStyle.current
    val libraries = remember(context) {
        val generatedLibraries =
            runCatching {
                Libs.Builder()
                    .withContext(context)
                    .build()
                    .libraries
                    .map { library ->
                        LibraryEntry(
                            name = library.name,
                            licenses = library.licenses.joinToString(", ") { it.name },
                            website = library.website,
                        )
                    }
                    .filter { it.name.isNotBlank() }
            }.onFailure {
                AppLogger.e("LibrariesScreen", "Failed to parse AboutLibraries metadata", it)
            }.getOrDefault(emptyList())

        mergeLibraries(generatedLibraries, fallbackLibraries)
    }

    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)

    ApplicationScreen(
        enableDefaultScrollBehavior = false,
        columnContent = false,
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = if (uiStyle == UiStyle.MIUIX) 0.dp else 10.dp,
            ),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.libraries_title),
                scrollBehavior = scrollBehavior,
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(libraries.size) { index ->
                val library = libraries[index]
                DynamicListItem(
                    listLength = libraries.lastIndex,
                    currentValue = index,
                ) {
                    PreferenceItem(
                        title = library.name,
                        description = library.licenses,
                        icon = Icons.Rounded.Description,
                        onClick = {
                            val url = library.website
                            if (!url.isNullOrBlank()) {
                                runCatching { uriHandler.openUri(url) }
                                    .onFailure {
                                        AppLogger.w(
                                            "LibrariesScreen",
                                            "Failed to open library url",
                                            "url" to url,
                                            "error" to (it.message ?: "unknown"),
                                        )
                                    }
                            }
                        },
                    )
                }
            }
            item { Spacer(modifier = Modifier.padding(26.dp)) }
        }
    }
}
