package com.example.apiarymanager.presentation.inspection

import com.example.apiarymanager.domain.model.BroodCondition
import com.example.apiarymanager.domain.model.ColonyStrength
import com.example.apiarymanager.domain.model.Inspection
import java.time.LocalDate

data class InspectionFormUiState(
    val hiveId: Long = 0,
    val inspectionId: Long? = null,
    // Date
    val date: LocalDate = LocalDate.now(),
    // Checkboxes
    val queenSeen: Boolean = false,
    val broodSeen: Boolean = false,
    val queenCellsSeen: Boolean = false,
    // Colony strength
    val colonyStrength: ColonyStrength = ColonyStrength.NORMAL,
    // Frame management
    val framesManagementEnabled: Boolean = false,
    val superboxesAdded: Int = 0,
    val superboxesRemoved: Int = 0,
    val dryCombFrames: Int = 0,
    val foundationFrames: Int = 0,
    // Free text
    val problems: String = "",
    val notes: String = "",
    // Photos
    val photoPaths: List<String> = emptyList(),
    // Screen state
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

sealed interface InspectionFormEvent {
    data object NavigateBack : InspectionFormEvent
    data class ShowMessage(val message: String) : InspectionFormEvent
}

// ─── UiState ↔ Domain converters ─────────────────────────────────────────────

fun InspectionFormUiState.toInspection(): Inspection = Inspection(
    id               = inspectionId ?: 0L,
    hiveId           = hiveId,
    date             = date,
    queenSeen        = queenSeen,
    broodSeen        = broodSeen,
    queenCellsSeen   = queenCellsSeen,
    colonyStrength   = colonyStrength,
    broodCondition   = BroodCondition.GOOD, // not exposed in this form; kept as default
    superboxesAdded  = if (framesManagementEnabled) superboxesAdded  else 0,
    superboxesRemoved = if (framesManagementEnabled) superboxesRemoved else 0,
    dryCombFrames    = if (framesManagementEnabled) dryCombFrames    else 0,
    foundationFrames = if (framesManagementEnabled) foundationFrames else 0,
    problems         = problems.trim(),
    notes            = notes.trim(),
    photoPaths       = photoPaths
)

fun Inspection.toFormState(): InspectionFormUiState = InspectionFormUiState(
    hiveId           = hiveId,
    inspectionId     = id,
    date             = date,
    queenSeen        = queenSeen,
    broodSeen        = broodSeen,
    queenCellsSeen   = queenCellsSeen,
    colonyStrength   = colonyStrength,
    framesManagementEnabled = superboxesAdded > 0 || superboxesRemoved > 0
            || dryCombFrames > 0 || foundationFrames > 0,
    superboxesAdded  = superboxesAdded,
    superboxesRemoved = superboxesRemoved,
    dryCombFrames    = dryCombFrames,
    foundationFrames = foundationFrames,
    problems         = problems,
    notes            = notes,
    photoPaths       = photoPaths
)
