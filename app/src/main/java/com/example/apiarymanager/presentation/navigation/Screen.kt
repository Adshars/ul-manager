package com.example.apiarymanager.presentation.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation 2.8 routes — each object/class IS the route.

// ─── Auth ────────────────────────────────────────────────────────────────────

@Serializable data object LoginRoute

@Serializable data object RegisterRoute

@Serializable data object ForgotPasswordRoute

// ─── Onboarding ───────────────────────────────────────────────────────────────

@Serializable data object OnboardingCarouselRoute

@Serializable data object OnboardingPinRoute

// ─── Main app ────────────────────────────────────────────────────────────────

@Serializable data object DashboardRoute

@Serializable data object ApiaryListRoute

@Serializable data object TaskListRoute

@Serializable data object StatisticsRoute

@Serializable data object SettingsRoute

// ─── Apiary ───────────────────────────────────────────────────────────────────

/** Add or edit an apiary. [apiaryId] = null → add; non-null → edit. */
@Serializable data class ApiaryFormRoute(val apiaryId: Long? = null)

// ─── Hive list ────────────────────────────────────────────────────────────────

/** [apiaryId] — which apiary's hives to show */
@Serializable data class HiveListRoute(val apiaryId: Long)

/** Add or edit a hive. [hiveId] = null → add; non-null → edit. */
@Serializable data class HiveFormRoute(val apiaryId: Long, val hiveId: Long? = null)

// ─── Hive detail ──────────────────────────────────────────────────────────────

/** [hiveId] — which hive to show in detail; [initialTab] — tab index to open */
@Serializable data class HiveDetailRoute(val hiveId: Long, val initialTab: Int = 0)

/** QR code view and management for a specific hive */
@Serializable data class HiveQrRoute(val hiveId: Long)

// ─── Inspection ───────────────────────────────────────────────────────────────

@Serializable data class InspectionFormRoute(val hiveId: Long, val inspectionId: Long? = null)

/** Camera capture — saves photo to [outputDir], then pops back with result in savedStateHandle */
@Serializable data class CameraRoute(val outputDir: String)

// ─── Activity forms ───────────────────────────────────────────────────────────

@Serializable data class HarvestFormRoute(val hiveId: Long, val harvestId: Long? = null)

@Serializable data class TreatmentFormRoute(val hiveId: Long, val treatmentId: Long? = null)

@Serializable data class FeedingFormRoute(val hiveId: Long, val feedingId: Long? = null)

// ─── Tasks ────────────────────────────────────────────────────────────────────

/**
 * Add or edit a task.
 * Pre-fills scope if [apiaryId] or [hiveId] is provided.
 * [taskId] = null → add new task.
 */
@Serializable data class TaskFormRoute(
    val apiaryId: Long? = null,
    val hiveId: Long? = null,
    val taskId: Long? = null
)
