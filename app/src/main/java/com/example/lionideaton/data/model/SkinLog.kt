package com.example.lionideaton.data.model

import android.graphics.Bitmap
import java.time.LocalDateTime

// Mirrors the backend `skin_log` table (this draft has no redness/hongjo column —
// only trouble/oil/dryness self-report — so this model follows that, not the older spec draft).
data class SkinLogEntry(
    val id: Long,
    val loggedAt: LocalDateTime,
    val photo: Bitmap? = null,
    val troubleLevel: Int,
    val oilLevel: Int,
    val drynessLevel: Int,
    val memo: String? = null
)
