package com.example.ui.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object SathiHapticEngine {
    fun trigger(context: Context, hook: HapticHook) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (!vibrator.hasVibrator()) return

        when (hook) {
            HapticHook.USER_SEND_PRESS -> {
                // Single crisp, high-frequency, low-duration click ensuring instant action acknowledgment.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12, 180)) // Crisp, clean 12ms click
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(12)
                }
            }
            HapticHook.SATHI_THINKING_START -> {
                // Ultra-low intensity, soft, sweeping transient vibration that signals cognitive processing.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(35, 30)) // Soft amplitude (30/255)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(20)
                }
            }
            HapticHook.FIRST_TOKEN_DELIVERY -> {
                // A distinct, deeply satisfying micro-pulse anchoring the physical arrival of the text stream.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(18, 110)) // Medium crisp amplitude
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(15)
                }
            }
            HapticHook.STREAM_COMPLETE -> {
                // A tightly integrated dual-micro tap sequence spaced precisely 45ms apart.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 8, 45, 8)
                    val amplitudes = intArrayOf(0, 140, 0, 140)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 10, 45, 10), -1)
                }
            }
            HapticHook.NETWORK_LATENCY_ERROR -> {
                // Clear, heavy, low-frequency triple-pulse warning pattern ([10ms, 50ms off, 10ms, 50ms off, 25ms])
                val pattern = longArrayOf(0, 10, 50, 10, 50, 25)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val amplitudes = intArrayOf(0, 200, 0, 200, 0, 240) // Heavy low-frequency warning pulses
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        }
    }
}

enum class HapticHook {
    USER_SEND_PRESS,
    SATHI_THINKING_START,
    FIRST_TOKEN_DELIVERY,
    STREAM_COMPLETE,
    NETWORK_LATENCY_ERROR
}
