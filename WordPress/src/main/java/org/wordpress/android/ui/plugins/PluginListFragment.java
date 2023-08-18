package org.wordpress.android.ui.plugins;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.model.plugin.ImmutablePluginModel;
import org.wordpress.android.models.networkresource.ListState;
import org.wordpress.android.ui.ActivityLauncher;
import org.wordpress.android.util.ColorUtils;
import org.wordpress.android.util.NetworkUtils;
import org.wordpress.android.util.StringUtils;
import org.wordpress.android.util.ToastUtils;
import org.wordpress.android.util.extensions.ContextExtensionsKt;
import org.wordpress.android.util.helpers.SwipeToRefreshHelper;
import org.wordpress.android.util.image.ImageManager;
import org.wordpress.android.util.image.ImageType;
import org.wordpress.android.viewmodel.plugins.PluginBrowserViewModel;
import org.wordpress.android.viewmodel.plugins.PluginBrowserViewModel.PluginListType;

import java.util.List;

import javax.inject.Inject;

import static org.wordpress.android.util.WPSwipeToRefreshHelper.buildSwipeToRefreshHelper;

public class PluginListFragment extends Fragment {
    public static final String TAG = PluginListFragment.class.getName();

    @Inject ViewModelProvider.Factory mViewModelFactory;
    @Inject ImageManager mImageManager;

    private static final String ARG_LIST_TYPE = "list_type";

    private PluginBrowserViewModel mViewModel;

    private RecyclerView mRecycler;
    private PluginListType mListType;
    private SwipeToRefreshHelper mSwipeToRefreshHelper;

    public static PluginListFragment newInstance(@NonNull SiteModel site, @NonNull PluginListType listType) {
        PluginListFragment fragment = new PluginListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(WordPress.SITE, site);
        bundle.putSerializable(ARG_LIST_TYPE, listType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WordPress) getActivity().getApplication()).component().inject(this);

        mListType = (PluginListType) getArguments().getSerializable(ARG_LIST_TYPE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // this enables us to clear the search icon in onCreateOptionsMenu when the list isn't showing search results
        setHasOptionsMenu(mListType != PluginListType.SEARCH);

        // Use the same view model as the PluginBrowserActivity
        mViewModel = new ViewModelProvider(getActivity(), mViewModelFactory).get(PluginBrowserViewModel.class);
        setupObservers();
    }

    private void setupObservers() {
        mViewModel.getSitePluginsLiveData()
                  .observe(getViewLifecycleOwner(), listState -> {
                      if (mListType == PluginListType.SITE) {
                          refreshPluginsAndProgressBars(listState);
                      }
                  });

        mViewModel.getFeaturedPluginsLiveData()
                  .observe(getViewLifecycleOwner(), listState -> {
                      if (mListType == PluginListType.FEATURED) {
                          refreshPluginsAndProgressBars(listState);
                      }
                  });

        mViewModel.getPopularPluginsLiveData()
                  .observe(getViewLifecycleOwner(), listState -> {
                      if (mListType == PluginListType.POPULAR) {
                          refreshPluginsAndProgressBars(listState);
                      }
                  });

        mViewModel.getNewPluginsLiveData()
                  .observe(getViewLifecycleOwner(), listState -> {
                      if (mListType == PluginListType.NEW) {
                          refreshPluginsAndProgressBars(listState);
                      }
                  });

        mViewModel.getSearchResultsLiveData()
                  .observe(getViewLifecycleOwner(), listState -> {
                      if (mListType == PluginListType.SEARCH) {
                          refreshPluginsAndProgressBars(listState);

                          if (listState instanceof ListState.Error) {
                              ToastUtils.showToast(getActivity(), R.string.plugin_search_error);
                          }

                          showEmptyView(mViewModel.shouldShowEmptySearchResultsView());
                      }
                  });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plugin_list_fragment, container, false);

        mRecycler = view.findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        mRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mSwipeToRefreshHelper = buildSwipeToRefreshHelper(
                view.findViewById(R.id.ptr_layout),
                () -> {
                    if (NetworkUtils.checkConnection(getActivity())) {
                        mViewModel.pullToRefresh(mListType);
                    } else {
                        mSwipeToRefreshHelper.setRefreshing(false);
                    }
                });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    PluginListType getListType() {
        return mListType;
    }

    private void refreshPluginsAndProgressBars(@Nullable ListState<ImmutablePluginModel> listState) {
        if (listState == null) {
            return;
        }
        final PluginListAdapter adapter;
        if (mRecycler.getAdapter() == null) {
            adapter = new PluginListAdapter(getActivity());
            mRecycler.setAdapter(adapter);
        } else {
            adapter = (PluginListAdapter) mRecycler.getAdapter();
        }
        adapter.setPlugins(listState.getData());
        refreshProgressBars(listState);
    }

    private void refreshProgressBars(@Nullable ListState listState) {
        if (!isAdded() || getView() == null || listState == null) {
            return;
        }
        // We want to show the swipe refresher for the initial fetch but not while loading more
        mSwipeToRefreshHelper.setRefreshing(listState.isFetchingFirstPage());
        // We want to show the progress bar at the bottom while loading more but not for initial fetch
        getView().findViewById(R.id.progress).setVisibility(
                listState.isLoadingMore() ? View.VISIBLE : View.GONE);
    }

    private void showEmptyView(boolean show) {
        if (isAdded() && getView() != null) {
            getView().findViewById(R.id.text_empty).setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private class PluginListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final PluginList mItems = new PluginList();
        private final LayoutInflater mLayoutInflater;

        PluginListAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
            setHasStableIds(true);
        }

        void setPlugins(@NonNull List<ImmutablePluginModel> items) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(mViewModel.getDiffCallback(mItems, items));
            mItems.clear();
            mItems.addAll(items);
            diffResult.dispatchUpdatesTo(this);
        }

        @Nullable
        private Object getItem(int position) {
            return mItems.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return mItems.getItemId(position);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.plugin_list_row, parent, false);
            return new PluginViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ImmutablePluginModel plugin = (ImmutablePluginModel) getItem(position);
            if (plugin == null) {
                return;
            }

            PluginViewHolder pluginHolder = (PluginViewHolder) holder;
            pluginHolder.mName.setText(plugin.getDisplayName());
            pluginHolder.mAuthor.setText(plugin.getAuthorName());
            mImageManager.load(pluginHolder.mIcon, ImageType.PLUGIN, StringUtils.notNullStr(plugin.getIcon()));

            if (plugin.isInstalled()) {
                @StringRes int textResId;
                @ColorRes int colorResId;
                @DrawableRes int drawableResId;
                if (PluginUtils.isAutoManaged(mViewModel.getSite(), plugin)) {
                    textResId = R.string.plugin_auto_managed;
                    colorResId = ContextExtensionsKt.getColorResIdFromAttribute(
                            pluginHolder.mStatusIcon.getContext(),
                            R.attr.wpColorSuccess
                    );
                    drawableResId = android.R.color.transparent;
                } else if (PluginUtils.isUpdateAvailable(plugin)) {
                    textResId = R.string.plugin_needs_update;
                    colorResId = ContextExtensionsKt.getColorResIdFromAttribute(
                            pluginHolder.mStatusIcon.getContext(),
                            R.attr.wpColorWarningDark
                    );
                    drawableResId = R.drawable.ic_sync_white_24dp;
                } else if (plugin.isActive()) {
                    textResId = R.string.plugin_active;
                    colorResId = ContextExtensionsKt.getColorResIdFromAttribute(
                            pluginHolder.mStatusIcon.getContext(),
                            R.attr.wpColorSuccess
                    );
                    drawableResId = R.drawable.ic_checkmark_white_24dp;
                } else {
                    textResId = R.string.plugin_inactive;
                    colorResId = ContextExtensionsKt.getColorResIdFromAttribute(
                            pluginHolder.mStatusIcon.getContext(),
                            R.attr.wpColorOnSurfaceMedium
                    );
                    drawableResId = R.drawable.ic_cross_white_24dp;
                }

                pluginHolder.mStatusText.setText(textResId);
                pluginHolder.mStatusText.setTextColor(
                        AppCompatResources.getColorStateList(pluginHolder.mStatusText.getContext(), colorResId));
                ColorUtils.INSTANCE.setImageResourceWithTint(pluginHolder.mStatusIcon, drawableResId, colorResId);
                pluginHolder.mStatusText.setVisibility(View.VISIBLE);
                pluginHolder.mStatusIcon.setVisibility(View.VISIBLE);
                pluginHolder.mRatingBar.setVisibility(View.GONE);
            } else {
                pluginHolder.mStatusText.setVisibility(View.GONE);
                pluginHolder.mStatusIcon.setVisibility(View.GONE);
                pluginHolder.mRatingBar.setVisibility(View.VISIBLE);
                pluginHolder.mRatingBar.setRating(plugin.getAverageStarRating());
            }

            if (position == getItemCount() - 1) {
                mViewModel.loadMore(mListType);
            }
        }

        private class PluginViewHolder extends RecyclerView.ViewHolder {
            private final TextView mName;
            private final TextView mAuthor;
            private final TextView mStatusText;
            private final ImageView mStatusIcon;
            private final ImageView mIcon;
            private final RatingBar mRatingBar;

            PluginViewHolder(View view) {
                super(view);
                mName = view.findViewById(R.id.plugin_name);
                mAuthor = view.findViewById(R.id.plugin_author);
                mStatusText = view.findViewById(R.id.plugin_status_text);
                mStatusIcon = view.findViewById(R.id.plugin_status_icon);
                mIcon = view.findViewById(R.id.plugin_icon);
                mRatingBar = view.findViewById(R.id.rating_bar);

                view.setOnClickListener(v -> {
                    int position = getBindingAdapterPosition();
                    ImmutablePluginModel plugin = (ImmutablePluginModel) getItem(position);
                    if (plugin == null) {
                        return;
                    }

                    ActivityLauncher.viewPluginDetail(getActivity(), mViewModel.getSite(),
                            plugin.getSlug());
                });
            }
        }
    }
}
