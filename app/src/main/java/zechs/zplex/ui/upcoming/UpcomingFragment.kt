package zechs.zplex.ui.upcoming

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentUpcomingBinding
import zechs.zplex.ui.shared_adapters.detailed_media.DetailedMediaAdapter
import zechs.zplex.utils.ext.TelevisionFocusNavigation
import zechs.zplex.utils.ext.TvListFocus
import zechs.zplex.utils.ext.focusTelevisionBackButton
import zechs.zplex.utils.ext.isInFirstTelevisionRow
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.ext.rememberTelevisionFocus
import zechs.zplex.utils.ext.restoreTelevisionFocus
import zechs.zplex.utils.state.Resource

class UpcomingFragment : Fragment(), TelevisionFocusNavigation {

    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    private val upcomingViewModel by activityViewModels<UpcomingViewModel>()

    private val upcomingAdapter by lazy {
        DetailedMediaAdapter { navigateMedia(it) }
    }

    private var isLoading = true
    private var isLastPage = true
    private var televisionFocus: TvListFocus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpcomingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireContext().isTelevision) {
            binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            if (televisionFocus == null) binding.toolbar.focusTelevisionBackButton()
        }
        setupRecyclerView()

        upcomingViewModel.upcoming.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    val results = response.data?.results.orEmpty().toList()
                    binding.pbBrowse.isVisible = false
                    binding.rvBrowse.isVisible = results.isNotEmpty()
                    binding.errorView.root.isVisible = results.isEmpty()
                    binding.errorView.errorTxt.setText(R.string.nothing_found)
                    binding.errorView.retryBtn.isVisible = false
                    upcomingAdapter.submitList(results) {
                        val list = _binding?.rvBrowse ?: return@submitList
                        televisionFocus?.let(list::restoreTelevisionFocus)
                        televisionFocus = null
                    }
                    isLastPage = response.data?.let { it.page >= it.total_pages } ?: true
                    isLoading = false
                }

                is Resource.Error -> {
                    binding.pbBrowse.isVisible = false
                    binding.rvBrowse.isVisible = false
                    binding.errorView.apply {
                        root.isVisible = true
                        errorTxt.text = response.message?.takeIf(String::isNotBlank)
                            ?: getString(R.string.something_went_wrong)
                        retryBtn.isVisible = true
                        retryBtn.setOnClickListener { upcomingViewModel.getUpcoming() }
                    }
                    isLoading = false
                }

                is Resource.Loading -> {
                    binding.pbBrowse.isVisible = upcomingAdapter.itemCount == 0
                    binding.errorView.root.isVisible = false
                    isLoading = true
                }
            }
        }

    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                val layoutManager = binding.rvBrowse.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.findLastCompletelyVisibleItemPosition() + 7
                val itemCount = layoutManager.itemCount

                if (visibleItemCount >= itemCount && !isLoading && !isLastPage) {
                    isLoading = true
                    upcomingViewModel.getUpcoming()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvBrowse.apply {
            adapter = upcomingAdapter
            layoutManager = if (context.isTelevision) GridLayoutManager(context, 2)
            else LinearLayoutManager(context)
            addOnScrollListener(this@UpcomingFragment.scrollListener)
        }
    }


    private fun navigateMedia(media: Media) {
        televisionFocus = binding.rvBrowse.rememberTelevisionFocus()
        val action = UpcomingFragmentDirections.actionUpcomingFragmentToFragmentMedia(
            media.copy(media_type = media.media_type ?: MediaType.movie)
        )
        findNavController().navigateSafe(action)
    }

    override fun moveTelevisionFocusUp(focused: View): Boolean =
        binding.rvBrowse.isInFirstTelevisionRow(focused, 2) &&
            binding.toolbar.focusTelevisionBackButton()

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvBrowse.adapter = null
        _binding = null
    }

}
