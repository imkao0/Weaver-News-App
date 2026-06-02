package dev.mkao.weaver.util

object AppConstants {
    const val SHIMMER_TIMEOUT_MS = 2500L

    // Timeout for converting cold flows to StateFlow via WhileSubscribed
    const val STATE_FLOW_TIMEOUT_MS = 5000L

    // Alpha values
    const val AlphaLow = 0.28f
    const val AlphaMedium = 0.7f
    const val AlphaHigh = 0.8f
    const val AlphaScrim = 0.72f
    const val AlphaScrimVideo = 0.75f
    const val AlphaSelectedScrim = 0.30f

    // Limits
    const val MaxLinesTitle = 2
    const val MaxLinesDescription = 3
}
