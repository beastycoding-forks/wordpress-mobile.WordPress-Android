package org.wordpress.android.ui.blaze.blazecampaigns.campaignlisting

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.wordpress.android.fluxc.model.blaze.BlazeCampaignModel
import org.wordpress.android.fluxc.store.blaze.BlazeCampaignsStore
import org.wordpress.android.ui.mysite.SelectedSiteRepository
import org.wordpress.android.ui.mysite.cards.blaze.CampaignStatus
import org.wordpress.android.ui.stats.refresh.utils.ONE_THOUSAND
import org.wordpress.android.ui.stats.refresh.utils.StatsUtils
import org.wordpress.android.ui.utils.UiString
import javax.inject.Inject


class CampaignPagingSource constructor(
    private val blazeCampaignsStore: BlazeCampaignsStore,
    private val selectedSiteRepository: SelectedSiteRepository,
    private val campaignDomainMapper: CampaignDomainMapper
) : PagingSource<Int, CampaignModel>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult.Page<Int, CampaignModel> {
        val page = params.key ?: 1
        val blazeCampaignModel = blazeCampaignsStore.getBlazeCampaigns(selectedSiteRepository.getSelectedSite()!!)
        val campaigns = blazeCampaignModel.campaigns.map {
            campaignDomainMapper.mapToCampaignModel(it)
        }
        return LoadResult.Page(
            data = campaigns,
            prevKey = params.key?.minus(1),
            nextKey = page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, CampaignModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1) }
    }
}

class CampaignDomainMapper @Inject constructor(
    private val statsUtils: StatsUtils,
){
    fun mapToCampaignModel(blazeCampaignModel: BlazeCampaignModel): CampaignModel {
        return CampaignModel(
            id = blazeCampaignModel.campaignId.toString(),
            title = UiString.UiStringText(blazeCampaignModel.title),
            status = CampaignStatus.fromString(blazeCampaignModel.uiStatus),
            featureImageUrl = blazeCampaignModel.imageUrl,
            impressions = mapToStatsStringIfNeeded(blazeCampaignModel.impressions),
            clicks = mapToStatsStringIfNeeded(blazeCampaignModel.clicks),
            budget = convertToDollars(blazeCampaignModel.budgetCents)
        )
    }

    private fun mapToStatsStringIfNeeded(value: Long): UiString? {
        return if (value != 0L) {
            val formattedString = statsUtils.toFormattedString(value, ONE_THOUSAND)
            UiString.UiStringText(formattedString)
        } else {
            null
        }
    }

    private fun convertToDollars(budgetCents: Long): UiString {
        return UiString.UiStringText("$" + (budgetCents / CENTS_IN_DOLLARS).toString())
    }

}
