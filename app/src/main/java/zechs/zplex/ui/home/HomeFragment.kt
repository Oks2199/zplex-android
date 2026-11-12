package zechs.zplex.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import zechs.zplex.R
import zechs.zplex.data.model.entities.WatchedShow
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.ui.home.adapter.HomeClickListener
import zechs.zplex.ui.home.adapter.HomeDataAdapter
import zechs.zplex.ui.home.adapter.HomeDataModel
import zechs.zplex.ui.home.adapter.watched.WatchedDataModel
import zechs.zplex.ui.shared_viewmodels.SeasonViewModel
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.ext.TvListFocus
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.ext.isMovingAboveTelevisionRow
import zechs.zplex.utils.ext.rememberTelevisionFocus
import zechs.zplex.utils.ext.restoreTelevisionFocus
import zechs.zplex.utils.state.Resource


class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private var televisionFocus: TvListFocus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentListBinding.bind(view)

        binding.toolbar.apply {
            navigationIcon = null
            isTitleCentered = false
            title = null
            binding.homeLogo.isVisible = true
            inflateMenu(R.menu.main_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_search -> {
                        findNavController().navigateSafe(R.id.action_global_searchFragment)
                        true
                    }

                    R.id.action_settings -> {
                        findNavController().navigateSafe(R.id.action_homeFragment_to_settingsFragment)
                        true
                    }

                    else -> false
                }
            }
        }

        setupRecyclerView()
        setupTrendingObserver()
    }

    private fun setupTrendingObserver() {
        homeViewModel.homeMedia.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    Log.d(TAG, "Success")
                    isLoading(false)
                    homeMediaSuccess(response.data!!)
                }

                is Resource.Error -> {
                    isLoading(false)
                    Log.d(TAG, "Error: ${response.message}")
                    homeMediaError(response.message!!)
                }

                is Resource.Loading -> {
                    isLoading(true)
                    Log.d(TAG, "isLoading")
                }
            }
        }
    }

    private fun homeMediaError(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private val homeDataAdapter by lazy {
        HomeDataAdapter(homeClickListener)
    }

    private val homeClickListener = object : HomeClickListener {
        override fun onClickMedia(media: Media) {
            navigateToMedia(media)
        }

        override fun onClickWatched(watched: WatchedDataModel) {
            when (watched) {
                is WatchedDataModel.Movie -> navigateToMedia(watched.movie.toMedia())
                is WatchedDataModel.Show -> navigateToSeason(watched.show)
            }
        }

        override fun onLongClickWatched(watched: WatchedDataModel) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.remove_from_watched))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    homeViewModel.removeWatchedMedia(watched)
                }
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .show()
        }
    }

    private fun homeMediaSuccess(listResponse: List<HomeDataModel>) {
        val bannerAdded = listResponse.firstOrNull() is HomeDataModel.Banner &&
            homeDataAdapter.currentList.firstOrNull() !is HomeDataModel.Banner
        homeDataAdapter.submitList(listResponse) {
            val list = _binding?.rvList ?: return@submitList
            val focus = televisionFocus
            televisionFocus = null
            if (focus != null) {
                list.restoreTelevisionFocus(focus)
            } else if (bannerAdded && list.context.isTelevision && !list.hasFocus()) {
                val layout = list.layoutManager as LinearLayoutManager
                if (layout.findFirstVisibleItemPosition() <= 1) {
                    layout.scrollToPositionWithOffset(0, 0)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = homeDataAdapter
            layoutManager = object : LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            ) {
                override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
                    val firstRow = homeDataAdapter.currentList.indexOfFirst {
                        it is HomeDataModel.Banner || it is HomeDataModel.Media ||
                            it is HomeDataModel.Watched
                    }
                    if (binding.rvList.isMovingAboveTelevisionRow(focused, direction, firstRow + 1)) {
                        return binding.toolbar.findViewById(R.id.action_search)
                    }
                    return super.onInterceptFocusSearch(focused, direction)
                }
            }
        }
    }

    private fun navigateToSeason(show: WatchedShow) {
        televisionFocus = binding.rvList.rememberTelevisionFocus()
        seasonViewModel.setShowSeason(
            tmdbId = show.tmdbId,
            seasonName = null,
            seasonNumber = show.seasonNumber,
            showName = show.name,
            seasonPosterPath = null,
            showPoster = show.posterPath,
            seasons = listOf()
        )
        findNavController().navigateSafe(R.id.action_homeFragment_to_episodesListFragment)
    }

    private fun navigateToMedia(media: Media) {
        televisionFocus = binding.rvList.rememberTelevisionFocus()
        HomeFragmentDirections
            .actionHomeFragmentToFragmentMedia(media)
            .also {
                findNavController().navigateSafe(it)
            }
    }

    private fun isLoading(hide: Boolean) {
        binding.loading.isVisible = hide
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
