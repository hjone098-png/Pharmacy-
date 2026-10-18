package com.example.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * High-performance button container that triggers [onAction] on tap,
 * and if held down continuously, auto-repeats [onAction] every [repeatIntervalMillis].
 * Cancels gracefully on drag/scroll to ensure smooth 60fps scrolling without stutter.
 */
@Composable
fun RepeatingIconButton(
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    initialDelayMillis: Long = 260L,
    repeatIntervalMillis: Long = 65L,
    content: @Composable () -> Unit
) {
    val currentAction by rememberUpdatedState(onAction)

    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        coroutineScope {
                            var didRepeat = false
                            val repeatJob = launch {
                                delay(initialDelayMillis)
                                while (isActive) {
                                    didRepeat = true
                                    currentAction()
                                    delay(repeatIntervalMillis)
                                }
                            }
                            val released = tryAwaitRelease()
                            repeatJob.cancel()
                            if (released && !didRepeat) {
                                currentAction()
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
