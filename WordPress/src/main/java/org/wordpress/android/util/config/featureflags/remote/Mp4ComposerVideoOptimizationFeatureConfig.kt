package org.wordpress.android.util.config.featureflags.remote

import org.wordpress.android.BuildConfig
import org.wordpress.android.annotation.RemoteFeatureFlagDefault
import org.wordpress.android.util.config.AppConfig
import org.wordpress.android.util.config.FeatureConfig
import org.wordpress.android.util.config.featureflags.remote.Mp4ComposerVideoOptimizationFeatureConfig.Companion.MP4_COMPOSER_REMOTE_FIELD
import javax.inject.Inject

/**
 * Configuration of the Mp4Composer Video Optimizer
 */
@RemoteFeatureFlagDefault(MP4_COMPOSER_REMOTE_FIELD, true)
class Mp4ComposerVideoOptimizationFeatureConfig
@Inject constructor(appConfig: AppConfig) : FeatureConfig(
    appConfig,
    BuildConfig.MP4_COMPOSER_VIDEO_OPTIMIZATION,
    MP4_COMPOSER_REMOTE_FIELD
) {
    companion object {
        const val MP4_COMPOSER_REMOTE_FIELD = "mp4_composer_video_optimization_enabled"
    }
}