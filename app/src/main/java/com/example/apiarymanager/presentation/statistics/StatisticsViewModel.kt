package com.example.apiarymanager.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.repository.ApiaryRepository
import com.example.apiarymanager.domain.repository.FeedingRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.domain.repository.HoneyHarvestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val apiaryRepository: ApiaryRepository,
    private val hiveRepository: HiveRepository,
    private val harvestRepository: HoneyHarvestRepository,
    private val feedingRepository: FeedingRepository
) : ViewModel() {

    private val _selectedApiaryId = MutableStateFlow<Long?>(null)
    private val _selectedYear = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = combine(
        apiaryRepository.getAllApiaries(),
        _selectedApiaryId,
        _selectedYear
    ) { apiaries, selectedId, selectedYear -> Triple(apiaries, selectedId, selectedYear) }
    .flatMapLatest { (apiaries, selectedId, selectedYear) ->
        if (apiaries.isEmpty()) return@flatMapLatest flowOf(StatisticsUiState(isLoading = false, apiaries = apiaries))

        val targetIds = if (selectedId == null) apiaries.map { it.id } else listOf(selectedId)

        combineToFlatList(targetIds.map { hiveRepository.getHivesByApiary(it) })
            .flatMapLatest { hives ->
                if (hives.isEmpty()) return@flatMapLatest flowOf(
                    StatisticsUiState(isLoading = false, apiaries = apiaries, selectedApiaryId = selectedId, selectedYear = selectedYear)
                )

                combine(
                    combineToFlatList(hives.map { harvestRepository.getHarvestsByHive(it.id) }),
                    combineToFlatList(hives.map { feedingRepository.getFeedingsByHive(it.id) })
                ) { allHarvests, allFeedings ->
                    val years = (allHarvests.map { it.date.year } + allFeedings.map { it.date.year })
                        .toSortedSet().toList()

                    val harvests = if (selectedYear == null) allHarvests
                        else allHarvests.filter { it.date.year == selectedYear }
                    val feedings = if (selectedYear == null) allFeedings
                        else allFeedings.filter { it.date.year == selectedYear }

                    StatisticsUiState(
                        isLoading        = false,
                        apiaries         = apiaries,
                        selectedApiaryId = selectedId,
                        selectedYear     = selectedYear,
                        availableYears   = years,
                        hiveCount        = hives.size,
                        totalHoneyKg     = harvests.sumOf { it.weightKg.toDouble() }.toFloat(),
                        totalFeedingKg   = feedings.sumOf { it.weightKg.toDouble() }.toFloat(),
                        monthlyHarvest   = harvests
                            .groupBy { it.date.monthValue }
                            .mapValues { (_, list) -> list.sumOf { it.weightKg.toDouble() }.toFloat() }
                            .toSortedMap(),
                        monthlyFeeding   = feedings
                            .groupBy { it.date.monthValue }
                            .mapValues { (_, list) -> list.sumOf { it.weightKg.toDouble() }.toFloat() }
                            .toSortedMap(),
                        harvestByHoneyType = harvests
                            .groupBy { it.honeyType.ifBlank { "Inny" } }
                            .mapValues { (_, list) -> list.sumOf { it.weightKg.toDouble() }.toFloat() }
                            .entries.sortedByDescending { it.value }
                            .associate { it.key to it.value }
                    )
                }
            }
    }
    .onStart { emit(StatisticsUiState(isLoading = true)) }
    .catch { emit(StatisticsUiState(isLoading = false)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    fun onApiarySelected(id: Long?) = _selectedApiaryId.update { id }
    fun onYearSelected(year: Int?) = _selectedYear.update { year }

    private fun <T> combineToFlatList(flows: List<Flow<List<T>>>): Flow<List<T>> =
        if (flows.isEmpty()) flowOf(emptyList())
        else combine(flows) { arrays -> arrays.flatMap { it } }
}
