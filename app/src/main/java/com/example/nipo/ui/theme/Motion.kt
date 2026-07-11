package com.example.nipo.ui.theme

/**
 * Shared timing constants for submit/seal overlay animations, mirroring the
 * mockup's `flapClose` (1s, 0.15s delay) + `sealPop` (0.4s, 1.05s delay) sequence.
 * Reused by Tips submit now and SOS submit in a future phase.
 */
object SealMotion {
    const val OVERLAY_TOTAL_MS = 1500
    const val FLAP_DURATION_MS = 1000
    const val FLAP_DELAY_MS = 150
    const val SEAL_POP_DURATION_MS = 400
    const val SEAL_POP_DELAY_MS = 1050
}
