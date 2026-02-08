package com.dsu.extended.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

/**
 * Material 3 Expressive Animation System
 * Spring-based animations for natural, responsive feel
 */
object DSUAnimations {

    // Duration constants
    private const val DURATION_SHORT = 150
    private const val DURATION_MEDIUM = 300
    private const val DURATION_LONG = 500
    private const val DURATION_EXTRA_LONG = 700

    // ═══════════════════════════════════════════════════════════════
    // Spring Configurations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Bouncy spring for playful, expressive animations
     */
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    /**
     * Gentle spring for subtle, smooth animations
     */
    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow,
    )

    /**
     * Snappy spring for quick, responsive animations
     */
    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessHigh,
    )

    /**
     * Default spring for general use
     */
    val DefaultSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    // ═══════════════════════════════════════════════════════════════
    // Card Animations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Card enter animation - fade + scale up
     */
    fun cardEnterAnimation(index: Int = 0): EnterTransition {
        val delay = index * 50
        return fadeIn(
            animationSpec = tween(
                durationMillis = DURATION_MEDIUM,
                delayMillis = delay,
                easing = FastOutSlowInEasing,
            ),
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = DURATION_MEDIUM,
                delayMillis = delay,
                easing = FastOutSlowInEasing,
            ),
            initialScale = 0.92f,
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = DURATION_MEDIUM,
                delayMillis = delay,
                easing = FastOutSlowInEasing,
            ),
            initialOffsetY = { it / 10 },
        )
    }

    /**
     * Card exit animation - fade + scale down
     */
    fun cardExitAnimation(): ExitTransition {
        return fadeOut(
            animationSpec = tween(
                durationMillis = DURATION_SHORT,
                easing = FastOutSlowInEasing,
            ),
        ) + scaleOut(
            animationSpec = tween(
                durationMillis = DURATION_SHORT,
                easing = FastOutSlowInEasing,
            ),
            targetScale = 0.95f,
        )
    }

    // ═══════════════════════════════════════════════════════════════
    // Navigation Animations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Screen enter animation - slide from right + fade
     */
    val screenEnterAnimation: EnterTransition = slideInHorizontally(
        animationSpec = tween(
            durationMillis = DURATION_MEDIUM,
            easing = FastOutSlowInEasing,
        ),
        initialOffsetX = { it / 4 },
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = DURATION_MEDIUM,
            easing = FastOutSlowInEasing,
        ),
    )

    /**
     * Screen exit animation - slide to left + fade
     */
    val screenExitAnimation: ExitTransition = slideOutHorizontally(
        animationSpec = tween(
            durationMillis = DURATION_MEDIUM,
            easing = FastOutSlowInEasing,
        ),
        targetOffsetX = { -it / 4 },
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
    )

    /**
     * Screen pop enter animation - slide from left
     */
    val screenPopEnterAnimation: EnterTransition = slideInHorizontally(
        animationSpec = tween(
            durationMillis = DURATION_MEDIUM,
            easing = FastOutSlowInEasing,
        ),
        initialOffsetX = { -it / 4 },
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = DURATION_MEDIUM,
            easing = FastOutSlowInEasing,
        ),
    )

    /**
     * Screen pop exit animation - slide to right
     */
    val screenPopExitAnimation: ExitTransition = slideOutHorizontally(
        animationSpec = tween(
            durationMillis = DURATION_MEDIUM,
            easing = FastOutSlowInEasing,
        ),
        targetOffsetX = { it / 4 },
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
    )

    // ═══════════════════════════════════════════════════════════════
    // Bottom Sheet Animations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Bottom sheet enter animation - slide up + fade
     */
    val bottomSheetEnterAnimation: EnterTransition = slideInVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        initialOffsetY = { it },
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
    )

    /**
     * Bottom sheet exit animation - slide down + fade
     */
    val bottomSheetExitAnimation: ExitTransition = slideOutVertically(
        animationSpec = tween(
            durationMillis = DURATION_MEDIUM,
            easing = FastOutSlowInEasing,
        ),
        targetOffsetY = { it },
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
    )

    // ═══════════════════════════════════════════════════════════════
    // Content Transitions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Smooth content transition for state changes
     */
    val contentTransition: ContentTransform = fadeIn(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
    ) + scaleIn(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
        initialScale = 0.95f,
    ) togetherWith fadeOut(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
    ) + scaleOut(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
        targetScale = 0.95f,
    )

    // ═══════════════════════════════════════════════════════════════
    // Button Animations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Button press scale animation spec
     */
    val buttonPressSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh,
    )

    /**
     * Button release scale animation spec
     */
    val buttonReleaseSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    // ═══════════════════════════════════════════════════════════════
    // Progress Animations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Progress bar animation spec
     */
    val progressSpec = tween<Float>(
        durationMillis = DURATION_MEDIUM,
        easing = FastOutSlowInEasing,
    )

    /**
     * Indeterminate progress animation duration
     */
    const val INDETERMINATE_DURATION = 1200

    // ═══════════════════════════════════════════════════════════════
    // FAB Animations
    // ═══════════════════════════════════════════════════════════════

    /**
     * FAB enter animation - scale + fade
     */
    val fabEnterAnimation: EnterTransition = scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        initialScale = 0f,
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
    )

    /**
     * FAB exit animation - scale + fade
     */
    val fabExitAnimation: ExitTransition = scaleOut(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
        targetScale = 0f,
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = DURATION_SHORT,
            easing = FastOutSlowInEasing,
        ),
    )

    // ═══════════════════════════════════════════════════════════════
    // List Item Animations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates staggered enter animation for list items
     */
    fun listItemEnterAnimation(index: Int): EnterTransition {
        val delay = (index * 30).coerceAtMost(300)
        return fadeIn(
            animationSpec = tween(
                durationMillis = DURATION_MEDIUM,
                delayMillis = delay,
                easing = FastOutSlowInEasing,
            ),
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = DURATION_MEDIUM,
                delayMillis = delay,
                easing = FastOutSlowInEasing,
            ),
            initialOffsetY = { it / 5 },
        )
    }
}

/**
 * Constants for haptic feedback
 */
object HapticConstants {
    const val TICK_DURATION = 10L
    const val CLICK_DURATION = 20L
    const val HEAVY_CLICK_DURATION = 30L
    const val DOUBLE_CLICK_DURATION = 40L
}
