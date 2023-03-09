package org.wordpress.android.ui.jetpackoverlay

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalPhase.PhaseFour
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalPhase.PhaseNewUsers
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalPhase.PhaseOne
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalPhase.PhaseThree
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalPhase.PhaseTwo
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalPhase.PhaseSelfHostedUsers
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalSiteCreationPhase.PHASE_ONE
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalSiteCreationPhase.PHASE_TWO
import org.wordpress.android.util.BuildConfigWrapper
import org.wordpress.android.util.config.featureflags.remote.JetpackFeatureRemovalNewUsersFlag
import org.wordpress.android.util.config.featureflags.remote.JetpackFeatureRemovalPhaseFourFlag
import org.wordpress.android.util.config.featureflags.remote.JetpackFeatureRemovalPhaseOneFlag
import org.wordpress.android.util.config.featureflags.remote.JetpackFeatureRemovalPhaseThreeFlag
import org.wordpress.android.util.config.featureflags.remote.JetpackFeatureRemovalPhaseTwoFlag
import org.wordpress.android.util.config.featureflags.remote.JetpackFeatureRemovalSelfHostedUsersFlag

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class JetpackFeatureRemovalPhaseHelperTest : BaseUnitTest() {
    @Mock
    private lateinit var buildConfigWrapper: BuildConfigWrapper

    @Mock
    private lateinit var jetpackFeatureRemovalPhaseOneConfig: JetpackFeatureRemovalPhaseOneFlag

    @Mock
    private lateinit var jetpackFeatureRemovalPhaseTwoConfig: JetpackFeatureRemovalPhaseTwoFlag

    @Mock
    private lateinit var jetpackFeatureRemovalPhaseThreeConfig: JetpackFeatureRemovalPhaseThreeFlag

    @Mock
    private lateinit var jetpackFeatureRemovalPhaseFourConfig: JetpackFeatureRemovalPhaseFourFlag

    @Mock
    private lateinit var jetpackFeatureRemovalNewUsersConfig: JetpackFeatureRemovalNewUsersFlag

    @Mock
    private lateinit var jetpackFeatureRemovalSelfHostedUsersConfig: JetpackFeatureRemovalSelfHostedUsersFlag

    private lateinit var jetpackFeatureRemovalPhaseHelper: JetpackFeatureRemovalPhaseHelper

    @Before
    fun setup() {
        jetpackFeatureRemovalPhaseHelper = JetpackFeatureRemovalPhaseHelper(
            buildConfigWrapper,
            jetpackFeatureRemovalPhaseOneConfig,
            jetpackFeatureRemovalPhaseTwoConfig,
            jetpackFeatureRemovalPhaseThreeConfig,
            jetpackFeatureRemovalPhaseFourConfig,
            jetpackFeatureRemovalNewUsersConfig,
            jetpackFeatureRemovalSelfHostedUsersConfig
        )
    }

    // general phase tests
    @Test
    fun `given jetpack app, when current phase is fetched, then return null`() {
        whenever(buildConfigWrapper.isJetpackApp).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getCurrentPhase()

        assertNull(currentPhase)
    }

    @Test
    fun `given phase one config true, when current phase is fetched, then return phase one`() {
        whenever(jetpackFeatureRemovalPhaseOneConfig.isEnabled()).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getCurrentPhase()

        assertEquals(currentPhase, PhaseOne)
    }

    @Test
    fun `given phase two config true, when current phase is fetched, then return phase two`() {
        whenever(jetpackFeatureRemovalPhaseTwoConfig.isEnabled()).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getCurrentPhase()

        assertEquals(currentPhase, PhaseTwo)
    }

    @Test
    fun `given phase three config true, when current phase is fetched, then return phase three`() {
        whenever(jetpackFeatureRemovalPhaseThreeConfig.isEnabled()).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getCurrentPhase()

        assertEquals(currentPhase, PhaseThree)
    }

    @Test
    fun `given phase four config true, when current phase is fetched, then return phase four`() {
        whenever(jetpackFeatureRemovalPhaseFourConfig.isEnabled()).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getCurrentPhase()

        assertEquals(currentPhase, PhaseFour)
    }

    @Test
    fun `given phase new users config true, when current phase is fetched, then return phase new users`() {
        whenever(jetpackFeatureRemovalNewUsersConfig.isEnabled()).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getCurrentPhase()

        assertEquals(currentPhase, PhaseNewUsers)
    }

    @Test
    fun `given self hosted users config true, when current phase is fetched, then return self hosted users config`() {
        whenever(jetpackFeatureRemovalSelfHostedUsersConfig.isEnabled()).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getCurrentPhase()

        assertEquals(currentPhase, PhaseSelfHostedUsers)
    }

    // site creation phase tests
    @Test
    fun `given jetpack app, when current site creation phase is fetched, then return null`() {
        whenever(buildConfigWrapper.isJetpackApp).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getSiteCreationPhase()

        assertNull(currentPhase)
    }

    @Test
    fun `given phase one config true, when current site creation phase is fetched, then return phase one`() {
        whenever(jetpackFeatureRemovalPhaseOneConfig.isEnabled()).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getSiteCreationPhase()

        assertEquals(currentPhase, PHASE_ONE)
    }

    @Test
    fun `given phase four config true, when current site creation phase is fetched, then return phase two`() {
        whenever(jetpackFeatureRemovalNewUsersConfig.isEnabled()).thenReturn(true)

        val currentPhase = jetpackFeatureRemovalPhaseHelper.getSiteCreationPhase()

        assertEquals(currentPhase, PHASE_TWO)
    }
}
