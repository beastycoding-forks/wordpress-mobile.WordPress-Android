package org.wordpress.android.ui.blaze.blazecampaigns.campaignlisting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.wordpress.android.R
import org.wordpress.android.fluxc.store.blaze.BlazeCampaignsStore
import org.wordpress.android.modules.BG_THREAD
import org.wordpress.android.ui.blaze.BlazeFeatureUtils
import org.wordpress.android.ui.blaze.BlazeFlowSource
import org.wordpress.android.ui.blaze.blazecampaigns.campaigndetail.CampaignDetailPageSource
import org.wordpress.android.ui.mysite.SelectedSiteRepository
import org.wordpress.android.ui.utils.UiString
import org.wordpress.android.util.NetworkUtilsWrapper
import org.wordpress.android.viewmodel.Event
import org.wordpress.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

const val CENTS_IN_DOLLARS = 100

@HiltViewModel
class CampaignListingViewModel @Inject constructor(
    @param:Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
    private val blazeFeatureUtils: BlazeFeatureUtils,
    private val blazeCampaignsStore: BlazeCampaignsStore,
    private val campaignDomainMapper: CampaignDomainMapper,
    private val selectedSiteRepository: SelectedSiteRepository,
    private val networkUtilsWrapper: NetworkUtilsWrapper
) : ScopedViewModel(bgDispatcher) {
    private val _uiState = MutableLiveData<CampaignListingUiState>()
    val uiState: LiveData<CampaignListingUiState> = _uiState

    private val _navigation = MutableLiveData<Event<CampaignListingNavigation>>()
    val navigation = _navigation

    fun start(campaignListingPageSource: CampaignListingPageSource) {
        blazeFeatureUtils.trackCampaignListingPageShown(campaignListingPageSource)
        _uiState.postValue(CampaignListingUiState.Loading)
    }

    val campaigns: Flow<PagingData<CampaignModel>> = Pager(
        pagingSourceFactory = {
            CampaignPagingSource(
                blazeCampaignsStore,
                selectedSiteRepository,
                campaignDomainMapper
            )
        },
        config = PagingConfig(pageSize = 10)
    ).flow.cachedIn(viewModelScope)

    private fun showCampaigns(campaigns: List<CampaignModel>) {
        _uiState.postValue(
            CampaignListingUiState.Success(
                campaigns,
                this::onCampaignClicked,
                this::createCampaignClick
            )
        )
    }

    private fun onCampaignClicked(campaignModel: CampaignModel) {
        _navigation.postValue(Event(CampaignListingNavigation.CampaignDetailPage(campaignModel.id.toInt())))
    }

    private fun showNoCampaigns() {
        _uiState.postValue(
            CampaignListingUiState.Error(
                title = UiString.UiStringRes(R.string.campaign_listing_page_no_campaigns_message_title),
                description = UiString.UiStringRes(R.string.campaign_listing_page_no_campaigns_message_description),
                button = CampaignListingUiState.Error.ErrorButton(
                    text = UiString.UiStringRes(R.string.campaign_listing_page_no_campaigns_button_text),
                    click = this::createCampaignClick
                )
            )
        )
    }

    private fun createCampaignClick() {
        _navigation.postValue(Event(CampaignListingNavigation.CampaignCreatePage()))
    }
}

enum class CampaignListingPageSource(val trackingName: String) {
    DASHBOARD_CARD("dashboard_card"),
    MENU_ITEM("menu_item"),
    UNKNOWN("unknown")
}

sealed class CampaignListingNavigation {
    data class CampaignDetailPage(
        val campaignId: Int,
        val campaignDetailPageSource: CampaignDetailPageSource = CampaignDetailPageSource.CAMPAIGN_LISTING_PAGE
    ) : CampaignListingNavigation()

    data class CampaignCreatePage(
        val blazeFlowSource: BlazeFlowSource = BlazeFlowSource.CAMPAIGN_LISTING_PAGE
    ) : CampaignListingNavigation()
}


