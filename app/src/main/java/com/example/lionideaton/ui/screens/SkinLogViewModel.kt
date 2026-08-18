package com.example.lionideaton.ui.screens

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.lionideaton.data.model.SkinLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

// Activity-scoped, shared with Report (Before/After) and My (history count) — see CartViewModel
// for the same pattern.
class SkinLogViewModel : ViewModel() {

    private var nextId = 1L

    private val _entries = MutableStateFlow<List<SkinLogEntry>>(emptyList())
    val entries: StateFlow<List<SkinLogEntry>> = _entries.asStateFlow()

    fun addEntry(photo: Bitmap?, troubleLevel: Int, oilLevel: Int, drynessLevel: Int, memo: String?) {
        _entries.update {
            it + SkinLogEntry(
                id = nextId++,
                loggedAt = LocalDateTime.now(),
                photo = photo,
                troubleLevel = troubleLevel,
                oilLevel = oilLevel,
                drynessLevel = drynessLevel,
                memo = memo?.takeIf { m -> m.isNotBlank() }
            )
        }
    }

    fun earliestPhoto(): Bitmap? = entries.value.firstOrNull { it.photo != null }?.photo
    fun latestPhoto(): Bitmap? = entries.value.lastOrNull { it.photo != null }?.photo
}
