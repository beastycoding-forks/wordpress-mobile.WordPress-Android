package org.wordpress.android.util.config.featureflags.remote

import org.wordpress.android.BuildConfig
import org.wordpress.android.annotation.RemoteFeatureFlagDefault
import org.wordpress.android.util.config.AppConfig
import org.wordpress.android.util.config.FeatureConfig
import javax.inject.Inject

private const val BLAZE_FEATURE_REMOTE_FIELD = "blaze"

@RemoteFeatureFlagDefault(BLAZE_FEATURE_REMOTE_FIELD, false)
class BlazeFeatureConfig @Inject constructor(
    appConfig: AppConfig
) : FeatureConfig(
    appConfig,
    BuildConfig.ENABLE_BLAZE_FEATURE,
    BLAZE_FEATURE_REMOTE_FIELD
)