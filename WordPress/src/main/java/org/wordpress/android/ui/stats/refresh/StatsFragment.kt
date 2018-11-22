package org.wordpress.android.ui.stats.refresh

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.pages_fragment.*
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.ui.stats.refresh.types.StatsListFragment
import org.wordpress.android.ui.stats.refresh.types.StatsListViewModel.StatsListType.DAYS
import org.wordpress.android.ui.stats.refresh.types.StatsListViewModel.StatsListType.INSIGHTS
import org.wordpress.android.ui.stats.refresh.types.StatsListViewModel.StatsListType.MONTHS
import org.wordpress.android.ui.stats.refresh.types.StatsListViewModel.StatsListType.WEEKS
import org.wordpress.android.util.WPSwipeToRefreshHelper
import org.wordpress.android.util.helpers.SwipeToRefreshHelper
import javax.inject.Inject

class StatsFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: StatsViewModel
    private lateinit var swipeToRefreshHelper: SwipeToRefreshHelper
    private lateinit var actionMenuItem: MenuItem

    private var restorePreviousSearch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.stats_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nonNullActivity = checkNotNull(activity)

        initializeViews(nonNullActivity)
        initializeViewModels(nonNullActivity, savedInstanceState == null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    private fun initializeViews(activity: FragmentActivity) {
        pagesPager.adapter = StatsPagerAdapter(activity, childFragmentManager)
        tabLayout.setupWithViewPager(pagesPager)

        swipeToRefreshHelper = WPSwipeToRefreshHelper.buildSwipeToRefreshHelper(pullToRefresh) {
            viewModel.onPullToRefresh()
        }
    }

    private fun initializeViewModels(activity: FragmentActivity, isFirstStart: Boolean) {
        viewModel = ViewModelProviders.of(activity, viewModelFactory).get(StatsViewModel::class.java)

        setupObservers(activity)

        val site = activity.intent?.getSerializableExtra(WordPress.SITE) as SiteModel?
        val nonNullSite = checkNotNull(site)
        viewModel.start(nonNullSite)
        if (!isFirstStart) {
            restorePreviousSearch = true
        }
    }

    private fun setupObservers(activity: FragmentActivity) {
        viewModel.isRefreshing.observe(this, Observer {
            it?.let { isRefreshing ->
                swipeToRefreshHelper.isRefreshing = isRefreshing
            }
        })

        viewModel.showSnackbarMessage.observe(this, Observer { holder ->
            val parent = activity.findViewById<View>(R.id.coordinatorLayout)
            if (holder != null && parent != null) {
                if (holder.buttonTitleRes == null) {
                    Snackbar.make(parent, getString(holder.messageRes), Snackbar.LENGTH_LONG).show()
                } else {
                    val snackbar = Snackbar.make(parent, getString(holder.messageRes), Snackbar.LENGTH_LONG)
                    snackbar.setAction(getString(holder.buttonTitleRes)) { holder.buttonAction() }
                    snackbar.show()
                }
            }
        })
    }
}

class StatsPagerAdapter(val context: Context, val fm: FragmentManager) : FragmentPagerAdapter(fm) {
    companion object {
        val statsTypes = listOf(INSIGHTS, DAYS, WEEKS, MONTHS)
    }

    override fun getCount(): Int = statsTypes.size

    override fun getItem(position: Int): Fragment {
        return StatsListFragment.newInstance(statsTypes[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(statsTypes[position].titleRes)
    }
}
