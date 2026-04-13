package com.example.apiarymanager.presentation.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation 2.8 routes — each object/class IS the route.
// No string templates, no manual argument parsing.

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object DashboardRoute

/** [apiaryId] — which apiary's hives to show */
@Serializable
data class HiveListRoute(val apiaryId: Long)

/** [hiveId] — which hive to show in detail */
@Serializable
data class HiveDetailRoute(val hiveId: Long)

/**
 * [hiveId] — the hive this inspection belongs to.
 * [inspectionId] — null means "create new", non-null means "edit existing".
 */
@Serializable
data class InspectionFormRoute(val hiveId: Long, val inspectionId: Long? = null)
