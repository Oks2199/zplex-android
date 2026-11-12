package zechs.zplex.ui.episodes

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import zechs.zplex.R
import zechs.zplex.databinding.BottomSeasonsSheetFragmentBinding
import zechs.zplex.ui.shared_adapters.season.SeasonsAdapter
import zechs.zplex.utils.ext.enableTelevisionAppBarFocus
import zechs.zplex.utils.ext.focusTelevisionBackButton
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.ext.showTelevisionFocus
import zechs.zplex.utils.state.Resource

@AndroidEntryPoint
class SeasonsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSeasonsSheetFragmentBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel by activityViewModels<EpisodesSharedViewModel>()
    private val seasonsAdapter by lazy {
        SeasonsAdapter(
            showName = sharedViewModel.showName,
            seasonOnClick = {
                sharedViewModel.selectSeason(it.season_number)
                dismiss()
            }
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        if (requireContext().isTelevision) Dialog(requireContext(), theme)
        else super.onCreateDialog(savedInstanceState)

    override fun onStart() {
        super.onStart()
        if (!requireContext().isTelevision) return
        val window = dialog?.window ?: return
        val density = resources.displayMetrics.density
        val screen = resources.configuration
        val horizontal = resources.getDimensionPixelSize(R.dimen.tv_safe_horizontal)
        val vertical = resources.getDimensionPixelSize(R.dimen.tv_safe_vertical)
        val width = minOf((screen.screenWidthDp * density).toInt() - horizontal * 2, (840 * density).toInt())
        val height = minOf((screen.screenHeightDp * density).toInt() - vertical * 2, (420 * density).toInt())
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(width, height)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSeasonsSheetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        binding.tvSeasonToolbar?.let { toolbar ->
            view.enableTelevisionAppBarFocus()
            view.showTelevisionFocus()
            toolbar.setNavigationOnClickListener { dismiss() }
            toolbar.doOnNextLayout { toolbar.focusTelevisionBackButton() }
        }
        setupSeasonsObserver()
    }

    private fun setupSeasonsObserver() {
        sharedViewModel.seasons.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    TransitionManager.beginDelayedTransition(
                        binding.root,
                        MaterialFadeThrough()
                    )
                    val seasons = it.data
                    seasonsAdapter.submitList(seasons) {
                        val list = _binding?.rvList ?: return@submitList
                        if (!list.context.isTelevision || seasons.isNullOrEmpty()) return@submitList
                        val selected = seasons.indexOfFirst { season ->
                            season.season_number == sharedViewModel.selectedSeasonNumber.value
                        }.coerceAtLeast(0)
                        list.scrollToPosition(selected)
                        list.doOnNextLayout {
                            list.findViewHolderForAdapterPosition(selected)?.itemView?.requestFocus()
                        }
                    }
                    isLoading(false)
                    binding.tvSeasonError?.isVisible = seasons.isNullOrEmpty()
                }

                is Resource.Error -> {
                    if (binding.root.context.isTelevision) {
                        binding.loading.isInvisible = true
                        binding.rvList.isInvisible = true
                        binding.tvSeasonError?.isVisible = true
                    } else {
                        isLoading(true)
                    }
                }

                is Resource.Loading -> {
                    isLoading(true)
                    binding.tvSeasonError?.isVisible = false
                }
            }
        }
    }

    private fun isLoading(hide: Boolean) {
        binding.apply {
            loading.isInvisible = !hide
            rvList.isInvisible = hide
        }
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = seasonsAdapter
            layoutManager = if (context.isTelevision) GridLayoutManager(context, 2)
            else LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
