package org.wordpress.android.ui.sitecreation

import org.wordpress.android.ui.sitecreation.SiteCreationStep.DOMAINS
import org.wordpress.android.ui.sitecreation.SiteCreationStep.INTENTS
import org.wordpress.android.ui.sitecreation.SiteCreationStep.SITE_DESIGNS
import org.wordpress.android.ui.sitecreation.SiteCreationStep.SITE_NAME
import org.wordpress.android.ui.sitecreation.SiteCreationStep.SITE_PREVIEW
import org.wordpress.android.util.config.featureflags.local.SiteIntentQuestionFeatureFlag
import org.wordpress.android.util.config.featureflags.local.SiteNameFeatureFlag
import org.wordpress.android.util.wizard.WizardStep
import javax.inject.Inject
import javax.inject.Singleton

enum class SiteCreationStep : WizardStep {
    SITE_DESIGNS, DOMAINS, SITE_PREVIEW, INTENTS, SITE_NAME;
}

@Singleton
class SiteCreationStepsProvider @Inject constructor(
    private val siteIntentQuestionFeatureConfig: SiteIntentQuestionFeatureFlag,
    private val siteNameFeatureConfig: SiteNameFeatureFlag
) {
    private val isSiteNameEnabled get() = siteNameFeatureConfig.isEnabled()
    private val isIntentsEnabled get() = siteIntentQuestionFeatureConfig.isEnabled()

    fun getSteps(): List<SiteCreationStep> = when {
        isSiteNameEnabled -> listOf(INTENTS, SITE_NAME, SITE_DESIGNS, SITE_PREVIEW)
        isIntentsEnabled -> listOf(INTENTS, SITE_DESIGNS, DOMAINS, SITE_PREVIEW)
        else -> listOf(SITE_DESIGNS, DOMAINS, SITE_PREVIEW)
    }
}
