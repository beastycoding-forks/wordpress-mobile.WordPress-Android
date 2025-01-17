package org.wordpress.android.ui.stats.refresh.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wordpress.android.fluxc.network.utils.StatsGranularity
import org.wordpress.android.ui.stats.refresh.StatsViewModel.DateSelectorUiModel
import org.wordpress.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider
import org.wordpress.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider.SelectedDate
import org.wordpress.android.util.config.StatsTrafficTabFeatureConfig
import org.wordpress.android.util.perform
import javax.inject.Inject

class StatsDateSelector
constructor(
    private val selectedDateProvider: SelectedDateProvider,
    private val statsDateFormatter: StatsDateFormatter,
    private val siteProvider: StatsSiteProvider,
    private val statsGranularity: StatsGranularity,
    private val isGranularitySpinnerVisible: Boolean,
    private val statsTrafficTabFeatureConfig: StatsTrafficTabFeatureConfig
) {
    private val _dateSelectorUiModel = MutableLiveData<DateSelectorUiModel>()
    val dateSelectorData: LiveData<DateSelectorUiModel> = _dateSelectorUiModel

    val selectedDate = selectedDateProvider.granularSelectedDateChanged(statsGranularity)
        .perform {
            updateDateSelector()
        }

    fun start(startDate: SelectedDate) {
        selectedDateProvider.updateSelectedDate(startDate, statsGranularity)
    }

    fun updateDateSelector() {
        val updatedDate = getDateLabelForSection()
        val currentState = dateSelectorData.value
        val timeZone = if (statsTrafficTabFeatureConfig.isEnabled()) {
            null
        } else {
            statsDateFormatter.printTimeZone(siteProvider.siteModel)
        }
        val updatedState = DateSelectorUiModel(
            true,
            isGranularitySpinnerVisible,
            updatedDate,
            enableSelectPrevious = selectedDateProvider.hasPreviousDate(statsGranularity),
            enableSelectNext = selectedDateProvider.hasNextDate(statsGranularity),
            timeZone = timeZone
        )
        emitValue(currentState, updatedState)
    }

    private fun emitValue(
        currentState: DateSelectorUiModel?,
        updatedState: DateSelectorUiModel
    ) {
        if (currentState != updatedState) {
            _dateSelectorUiModel.value = updatedState
        }
    }

    private fun getDateLabelForSection(): String? {
        return statsDateFormatter.printGranularDate(
            selectedDateProvider.getSelectedDate(statsGranularity) ?: selectedDateProvider.getCurrentDate(),
            statsGranularity
        )
    }

    fun onNextDateSelected() {
        selectedDateProvider.selectNextDate(statsGranularity)
    }

    fun onPreviousDateSelected() {
        selectedDateProvider.selectPreviousDate(statsGranularity)
    }

    fun clear() {
        selectedDateProvider.clear(statsGranularity)
    }

    fun getSelectedDate(): SelectedDate {
        return selectedDateProvider.getSelectedDateState(statsGranularity)
    }

    class Factory
    @Inject constructor(
        private val selectedDateProvider: SelectedDateProvider,
        private val siteProvider: StatsSiteProvider,
        private val statsDateFormatter: StatsDateFormatter,
        private val statsTrafficTabFeatureConfig: StatsTrafficTabFeatureConfig
    ) {
        fun build(statsGranularity: StatsGranularity, isGranularitySpinnerVisible: Boolean = false): StatsDateSelector {
            return StatsDateSelector(
                selectedDateProvider,
                statsDateFormatter,
                siteProvider,
                statsGranularity,
                isGranularitySpinnerVisible,
                statsTrafficTabFeatureConfig
            )
        }
    }
}
