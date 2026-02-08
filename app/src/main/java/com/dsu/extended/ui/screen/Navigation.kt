package com.dsu.extended.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dsu.extended.R
import com.dsu.extended.ui.screen.about.AboutScreen
import com.dsu.extended.ui.screen.adb.AdbScreen
import com.dsu.extended.ui.screen.home.Home
import com.dsu.extended.ui.screen.libraries.LibrariesScreen
import com.dsu.extended.ui.screen.logs.LogsScreen
import com.dsu.extended.ui.screen.settings.Settings
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem as MiuixNavigationItem

object Destinations {
    const val Homepage = "home"
    const val Logs = "logs"
    const val Preferences = "preferences"
    const val ADBInstallation = "adb_installation"
    const val About = "about"
    const val Libraries = "libraries"
    const val Up = "up"
}

private fun isMainTabRoute(route: String?): Boolean {
    return route == Destinations.Homepage || route == Destinations.Logs
}

private fun tabIndex(route: String?): Int {
    return when (route) {
        Destinations.Homepage -> 0
        Destinations.Logs -> 1
        else -> 0
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.mainTabEnterTransition(uiStyle: UiStyle): EnterTransition {
    if (uiStyle != UiStyle.MIUIX) return EnterTransition.None
    if (!isMainTabRoute(initialState.destination.route) || !isMainTabRoute(targetState.destination.route)) {
        return EnterTransition.None
    }
    val isForward = tabIndex(targetState.destination.route) >= tabIndex(initialState.destination.route)
    return slideInHorizontally(
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        initialOffsetX = { fullWidth -> if (isForward) fullWidth / 3 else -fullWidth / 3 },
    ) + fadeIn(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.mainTabExitTransition(uiStyle: UiStyle): ExitTransition {
    if (uiStyle != UiStyle.MIUIX) return ExitTransition.None
    if (!isMainTabRoute(initialState.destination.route) || !isMainTabRoute(targetState.destination.route)) {
        return ExitTransition.None
    }
    val isForward = tabIndex(targetState.destination.route) >= tabIndex(initialState.destination.route)
    return slideOutHorizontally(
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        targetOffsetX = { fullWidth -> if (isForward) -fullWidth / 3 else fullWidth / 3 },
    ) + fadeOut(animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing))
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val uiStyle = LocalUiStyle.current
    val showMainTabs = currentRoute == Destinations.Homepage || currentRoute == Destinations.Logs

    Scaffold(
        bottomBar = {
            if (showMainTabs) {
                MainBottomTabs(
                    uiStyle = uiStyle,
                    currentRoute = currentRoute.orEmpty(),
                    onSelectRoute = { destination ->
                        if (destination == currentRoute) {
                            return@MainBottomTabs
                        }
                        navController.navigate(destination) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Destinations.Homepage) {
                                saveState = true
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Homepage,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            fun navigate(destination: String) {
                if (destination == Destinations.Up) {
                    navController.navigateUp()
                } else {
                    navController.navigate(destination)
                }
            }

            composable(
                route = Destinations.Homepage,
                enterTransition = { mainTabEnterTransition(uiStyle) },
                exitTransition = { mainTabExitTransition(uiStyle) },
                popEnterTransition = { mainTabEnterTransition(uiStyle) },
                popExitTransition = { mainTabExitTransition(uiStyle) },
            ) { Home(navigate = { navigate(it) }) }
            composable(
                route = Destinations.Logs,
                enterTransition = { mainTabEnterTransition(uiStyle) },
                exitTransition = { mainTabExitTransition(uiStyle) },
                popEnterTransition = { mainTabEnterTransition(uiStyle) },
                popExitTransition = { mainTabExitTransition(uiStyle) },
            ) { LogsScreen(navigate = { navigate(it) }) }
            composable(Destinations.Preferences) { Settings(navigate = { navigate(it) }) }
            composable(Destinations.ADBInstallation) { AdbScreen(navigate = { navigate(it) }) }
            composable(Destinations.About) { AboutScreen(navigate = { navigate(it) }) }
            composable(Destinations.Libraries) { LibrariesScreen(navigate = { navigate(it) }) }
        }
    }
}

private data class MainTabItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
)

@Composable
private fun MainBottomTabs(
    uiStyle: UiStyle,
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val tabs = listOf(
        MainTabItem(
            route = Destinations.Homepage,
            titleRes = R.string.installation,
            icon = Icons.Rounded.InstallMobile,
        ),
        MainTabItem(
            route = Destinations.Logs,
            titleRes = R.string.logs_tab_title,
            icon = Icons.Rounded.Description,
        ),
    )
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    if (uiStyle == UiStyle.MIUIX) {
        val navItems =
            tabs.map { tab ->
                MiuixNavigationItem(
                    label = stringResource(id = tab.titleRes),
                    icon = tab.icon,
                )
            }
        MiuixNavigationBar(
            color = MaterialTheme.colorScheme.surfaceContainer,
            items = navItems,
            selected = selectedIndex,
            showDivider = true,
            onClick = { index ->
                val targetRoute = tabs.getOrNull(index)?.route ?: return@MiuixNavigationBar
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                onSelectRoute(targetRoute)
            },
        )
    } else {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
        ) {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onSelectRoute(tab.route)
                    },
                    icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                    label = { Text(text = stringResource(id = tab.titleRes)) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
