package org.wordpress.android.ui.blaze.blazecampaigns.campaignlisting

import org.wordpress.android.R
import org.wordpress.android.ui.mysite.cards.blaze.CampaignStatus
import org.wordpress.android.ui.utils.UiString

sealed class CampaignListingUiState {
    object Loading : CampaignListingUiState()

    data class Error(
        val title: UiString,
        val description: UiString,
        val button: ErrorButton? = null
    ) : CampaignListingUiState() {
        data class ErrorButton(
            val text: UiString,
            val click: () -> Unit
        )

        companion object {
            val noCampaign = Error(
                title = UiString.UiStringRes(R.string.campaign_listing_page_no_campaigns_message_title),
                description = UiString.UiStringRes(R.string.campaign_listing_page_no_campaigns_message_description),
                button = CampaignListingUiState.Error.ErrorButton(
                    text = UiString.UiStringRes(R.string.campaign_listing_page_no_campaigns_button_text),
                    click = { }
                )
            )
        }
    }

    data class Success(
        val campaigns: List<CampaignModel>,
        val itemClick: (CampaignModel) -> Unit,
        val createCampaignClick: () -> Unit,
        val loadingMore: Boolean = false
    ) : CampaignListingUiState()
}

data class CampaignModel(
    val id: String,
    val title: UiString,
    val status: CampaignStatus?,
    val featureImageUrl: String?,
    val impressions: UiString?,
    val clicks: UiString?,
    val budget: UiString,
)


