package com.dsu.extended.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Shape System
 * Larger, more pronounced corner radii for modern look
 */
val ExpressiveShapes = Shapes(
    // For small components like chips, small buttons
    extraSmall = RoundedCornerShape(8.dp),

    // For input fields, small cards
    small = RoundedCornerShape(12.dp),

    // For medium cards, dialogs
    medium = RoundedCornerShape(20.dp),

    // For large cards, bottom sheets
    large = RoundedCornerShape(28.dp),

    // For full-height elements, large containers
    extraLarge = RoundedCornerShape(36.dp),
)

val MiuixShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * Custom shape definitions for specific components
 */
object DSUShapes {
    // Main content cards
    val CardShape = RoundedCornerShape(24.dp)

    // Primary action buttons
    val ButtonShape = RoundedCornerShape(16.dp)

    // FAB and circular buttons
    val FabShape = RoundedCornerShape(20.dp)

    // Bottom sheet
    val BottomSheetShape = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )

    // Dialog
    val DialogShape = RoundedCornerShape(28.dp)

    // Input fields
    val InputShape = RoundedCornerShape(14.dp)

    // Chips and tags
    val ChipShape = RoundedCornerShape(8.dp)

    // Progress bar
    val ProgressShape = RoundedCornerShape(8.dp)

    // Snackbar
    val SnackbarShape = RoundedCornerShape(12.dp)

    // Image containers
    val ImageShape = RoundedCornerShape(16.dp)

    // Icon buttons
    val IconButtonShape = RoundedCornerShape(12.dp)

    // Toggle/Switch track
    val ToggleShape = RoundedCornerShape(50)

    // Top bar (when scrolled)
    val TopBarShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 20.dp,
        bottomEnd = 20.dp,
    )

    // Status indicators
    val StatusShape = RoundedCornerShape(6.dp)

    // Installation progress card (larger)
    val InstallationCardShape = RoundedCornerShape(32.dp)
}
