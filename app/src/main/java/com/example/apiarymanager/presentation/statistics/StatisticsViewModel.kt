package com.example.apiarymanager.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiarymanager.domain.model.HoneyHarvest
import com.example.apiarymanager.domain.model.Feeding
import com.example.apiarymanager.domain.repository.ApiaryRepository
import com.example.apiarymanager.domain.repository.HiveRepository
import com.example.apiarymanager.domain.repository.HoneyHarvestRepository
import com.example.apiarymanager.domain.repository.FeedingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val apiaryRepository: ApiaryRepository,
    private val hiveRepository: HiveRepository,
    private val harvestRepository: HoneyHarvestRepository,
    private val feedingRepository: FeedingRepository
) : ViewModel() {

    private val _selectedApiaryId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = combine(
        apiaryRepository.getAllApiaries(),
        _selectedApiaryId
    ) { apiaries, selectedId -> apiaries to selectedId }
    .flatMapLatest { (apiaries, selectedId) ->
        if (apiaries.isEmpty()) return@flatMapLatest flowOf(StatisticsUiState(isLoading = false, apiaries = apiaries))

        // Collect all relevant harvest and feeding flows
        val apiaryIds = if (selectedId == null) apiaries.map { it.id } else listOf(selectedId)

        // Get hives for each apiary, then harvests and feedings
        val harvestFlows = apiaryIds.map { aId -> harvestRepository.getTotalHarvestKgByApiary(aId) }
        val feedingFlows = apiaryIds.map { aId -> feedingRepository.getTotalFeedingKgByApiary(aId) }

        combine(
            combine(harvestFlows) { arr -> arr.sum() },
            combine(feedingFlows) { arr -> arr.sum() }
        ) { totalHarvest, totalFeeding ->
            StatisticsUiState(
                isLoading      = false,
                apiaries       = apiaries,
                selectedApiaryId = selectedId,
                totalHoneyKg   = totalHarvest,
                totalFeedingKg = totalFeeding,
                // Simplified monthly data — real impl would query per month
                monthlyHarvest = buildMonthlyData(totalHarvest),
                monthlyFeeding = buildMonthlyData(totalFeeding)
            )
        }
    }
    .onStart { emit(StatisticsUiState(isLoading = true)) }
    .catch { emit(StatisticsUiState(isLoading = false)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    fun onApiarySelected(id: Long?) {
        _selectedApiaryId.update { id }
    }

    /** Creates a simple 12-month distribution for demo purposes. */
    private fun buildMonthlyData(total: Float): Map<Int, Float> {
        if (total == 0f) return emptyMap()
        val currentMonth = LocalDate.now().monthValue
        // Distribute across last 6 active months
        val months = (maxOf(1, currentMonth - 5)..currentMonth).toList()
        val perMonth = total / months.size
        return months.associateWith { perMonth }
    }
}
