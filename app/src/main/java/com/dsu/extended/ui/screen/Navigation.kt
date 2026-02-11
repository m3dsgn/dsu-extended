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
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
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
import com.dsu.extended.ui.theme.LocalHazeState
import com.dsu.extended.ui.theme.LocalUiStyle
import com.dsu.extended.ui.theme.UiStyle
import com.dsu.extended.ui.theme.DSUAnimations
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
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
    if (!isMainTabRoute(initialState.destination.route) || !isMainTabRoute(targetState.destination.route)) {
        return EnterTransition.None
    }
    if (uiStyle == UiStyle.MIUIX) {
        val isForward = tabIndex(targetState.destination.route) >= tabIndex(initialState.destination.route)
        return slideInHorizontally(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> if (isForward) fullWidth / 3 else -fullWidth / 3 },
        ) + fadeIn(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
    }
    return DSUAnimations.screenEnterAnimation
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.mainTabExitTransition(uiStyle: UiStyle): ExitTransition {
    if (!isMainTabRoute(initialState.destination.route) || !isMainTabRoute(targetState.destination.route)) {
        return ExitTransition.None
    }
    if (uiStyle == UiStyle.MIUIX) {
        val isForward = tabIndex(targetState.destination.route) >= tabIndex(initialState.destination.route)
        return slideOutHorizontally(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> if (isForward) -fullWidth / 3 else fullWidth / 3 },
        ) + fadeOut(animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing))
    }
    return DSUAnimations.screenExitAnimation
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val uiStyle = LocalUiStyle.current
    val showMainTabs = currentRoute == Destinations.Homepage || currentRoute == Destinations.Logs
    val hazeState = remember(uiStyle) { if (uiStyle == UiStyle.MIUIX) HazeState() else null }

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Scaffold(
            bottomBar = {
                if (showMainTabs) {
                    MainBottomTabs(
                        uiStyle = uiStyle,
                        currentRoute = currentRoute.orEmpty(),
                        hazeState = hazeState,
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
                modifier = Modifier
                    .then(
                        if (uiStyle == UiStyle.MIUIX && hazeState != null) {
                            Modifier.hazeSource(state = hazeState)
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (uiStyle == UiStyle.MIUIX && showMainTabs) {
                            Modifier
                        } else {
                            Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        },
                    ),
            ) {
                fun navigate(destination: String) {
                    if (destination == Destinations.Up) {
                        navController.navigateUp()
                        return
                    }

                    if (isMainTabRoute(destination)) {
                        navController.navigate(destination) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Destinations.Homepage) {
                                saveState = true
                            }
                        }
                        return
                    }

                    navController.navigate(destination)
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
                composable(
                    route = Destinations.Preferences,
                    enterTransition = { DSUAnimations.screenEnterAnimation },
                    exitTransition = { DSUAnimations.screenExitAnimation },
                    popEnterTransition = { DSUAnimations.screenPopEnterAnimation },
                    popExitTransition = { DSUAnimations.screenPopExitAnimation },
                ) { Settings(navigate = { navigate(it) }) }
                composable(
                    route = Destinations.ADBInstallation,
                    enterTransition = { DSUAnimations.screenEnterAnimation },
                    exitTransition = { DSUAnimations.screenExitAnimation },
                    popEnterTransition = { DSUAnimations.screenPopEnterAnimation },
                    popExitTransition = { DSUAnimations.screenPopExitAnimation },
                ) { AdbScreen(navigate = { navigate(it) }) }
                composable(
                    route = Destinations.About,
                    enterTransition = { DSUAnimations.screenEnterAnimation },
                    exitTransition = { DSUAnimations.screenExitAnimation },
                    popEnterTransition = { DSUAnimations.screenPopEnterAnimation },
                    popExitTransition = { DSUAnimations.screenPopExitAnimation },
                ) { AboutScreen(navigate = { navigate(it) }) }
                composable(
                    route = Destinations.Libraries,
                    enterTransition = { DSUAnimations.screenEnterAnimation },
                    exitTransition = { DSUAnimations.screenExitAnimation },
                    popEnterTransition = { DSUAnimations.screenPopEnterAnimation },
                    popExitTransition = { DSUAnimations.screenPopExitAnimation },
                ) { LibrariesScreen(navigate = { navigate(it) }) }
            }
        }
    }
}

private data class MainTabItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
)

private fun Modifier.topEdgeBorder(
    color: Color,
    width: Dp,
): Modifier {
    return drawWithContent {
        drawContent()
        val strokeWidth = width.toPx()
        val y = strokeWidth / 2f
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
private fun MainBottomTabs(
    uiStyle: UiStyle,
    currentRoute: String,
    hazeState: HazeState?,
    onSelectRoute: (String) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val colorScheme = MaterialTheme.colorScheme
    val isOledBlackTheme = colorScheme.background == Color.Black && colorScheme.surface == Color.Black
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
        val hazeBaseColor = if (isOledBlackTheme) Color.Black else colorScheme.surfaceContainer
        val hazeStyle = remember(hazeBaseColor) {
            HazeStyle(
                backgroundColor = hazeBaseColor,
                tint = HazeTint(hazeBaseColor.copy(alpha = if (isOledBlackTheme) 0.76f else 0.82f)),
            )
        }
        val topLineColor = colorScheme.outline.copy(alpha = if (isOledBlackTheme) 0.58f else 0.42f)
        val barModifier =
            if (hazeState != null) {
                Modifier
                    .hazeEffect(hazeState) {
                        style = hazeStyle
                        blurRadius = 30.dp
                        noiseFactor = 0f
                    }
                    .topEdgeBorder(
                        width = 0.55.dp,
                        color = topLineColor,
                    )
            } else {
                Modifier.topEdgeBorder(
                    width = 0.55.dp,
                    color = topLineColor,
                )
            }
        MiuixNavigationBar(
            modifier = barModifier,
            color = Color.Transparent,
            items = navItems,
            selected = selectedIndex,
            showDivider = false,
            onClick = { index ->
                val targetRoute = tabs.getOrNull(index)?.route ?: return@MiuixNavigationBar
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                onSelectRoute(targetRoute)
            },
        )
    } else {
        FlexibleBottomAppBar(
            containerColor = if (isOledBlackTheme) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
            expandedHeight = 74.dp,
            horizontalArrangement = BottomAppBarDefaults.FlexibleFixedHorizontalArrangement,
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
