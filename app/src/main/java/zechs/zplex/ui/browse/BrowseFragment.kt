package zechs.zplex.ui.browse

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import androidx.appcompat.widget.ListPopupWindow
import androidx.constraintlayout.widget.Constraints
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.Order
import zechs.zplex.data.model.SortBy
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.keyword.TmdbKeyword
import zechs.zplex.data.model.tmdb.search.SearchResponse
import zechs.zplex.databinding.FragmentBrowseBinding
import zechs.zplex.ui.dialog.FiltersDialog
import zechs.zplex.ui.shared_adapters.media.MediaAdapter
import zechs.zplex.ui.shared_adapters.media.mediaGridLayoutManager
import zechs.zplex.ui.shared_viewmodels.FilterArgs
import zechs.zplex.ui.shared_viewmodels.FiltersViewModel
import zechs.zplex.utils.Constants.SEARCH_DELAY_AMOUNT
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.ext.TelevisionFocusNavigation
import zechs.zplex.utils.ext.TvListFocus
import zechs.zplex.utils.ext.fitTelevisionWindow
import zechs.zplex.utils.ext.isInFirstTelevisionRow
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.ext.rememberTelevisionFocus
import zechs.zplex.utils.ext.restoreTelevisionFocus
import zechs.zplex.utils.ext.showTelevisionFocus
import zechs.zplex.utils.ext.showWithTelevisionFocus
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.util.Keyboard

class BrowseFragment : Fragment(), TelevisionFocusNavigation {

    companion object {
        const val TAG = "BrowseFragment"
    }

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!

    private val filterModel by activityViewModels<FiltersViewModel>()
    private val browseViewModel by activityViewModels<BrowseViewModel>()

    private var _filtersDialog: FiltersDialog? = null
    private val filtersDialog: FiltersDialog get() = _filtersDialog!!
    private var requestedFilter: FilterArgs? = null
    private var televisionFocus: TvListFocus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBrowseBinding.bind(view)

        setupRecyclerView()
        setupFiltersObservers()
        setupBrowseObservers()
        setupKeywordsObserver()

        binding.btnFilters.setOnClickListener {
            showFiltersDialog(context = requireContext())
        }
        if (view.context.isTelevision) {
            binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            if (televisionFocus == null) {
                binding.btnFilters.doOnNextLayout { it.requestFocus() }
            }
        }

    }

    private fun keywordSearch(editText: EditText) {
        var job: Job? = null
        editText.apply {
            addTextChangedListener { editable ->
                job?.cancel()
                job = viewLifecycleOwner.lifecycleScope.launch {
                    delay(SEARCH_DELAY_AMOUNT)
                    if (_filtersDialog?.isShowing != true) return@launch
                    val query = editable?.toString().orEmpty()
                    if (query.isNotBlank()) {
                        browseViewModel.getSearch(query)
                    } else {
                        browseViewModel.clearKeywordList()
                    }
                }
            }
            setOnClickListener {
                this.requestFocus()
                Keyboard.show(it)
            }
        }
    }

    private fun setupBrowseObservers() {
        browseViewModel.browse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> response.data?.let { onSuccess(it) }
                is Resource.Error -> response.message?.let { onError(it) }
                is Resource.Loading -> browseViewModel.isLoading = true
            }
        }
    }

    private fun setupKeywordsObserver() {
        browseViewModel.keywordsList.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                val dialog = _filtersDialog?.takeIf { window -> window.isShowing } ?: return@let
                if (dialog.context.isTelevision || it.isNotEmpty()) {
                    setupKeywordList(requireContext(), dialog, it)
                }
            }
        }
    }

    private fun onSuccess(showsResponse: SearchResponse) {
        binding.apply {
            pbBrowse.isVisible = false
            val hasResults = showsResponse.results.isNotEmpty()
            rvBrowse.isVisible = hasResults
            errorView.root.isVisible = !hasResults
            errorView.retryBtn.isVisible = false
            if (!hasResults) errorView.errorTxt.setText(R.string.nothing_found)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            browseAdapter.submitList(showsResponse.results.toList()) {
                val list = _binding?.rvBrowse ?: return@submitList
                televisionFocus?.let(list::restoreTelevisionFocus)
                televisionFocus = null
            }
        }

        browseViewModel.isLoading = false

        when (filterModel.getFilter()?.mediaType) {
            MediaType.movie -> {
                binding.tvMediaType.text = getString(R.string.browsing_movies)
            }

            MediaType.tv -> {
                binding.tvMediaType.text = getString(R.string.browsing_tv)
            }

            else -> {}
        }

    }

    private fun onError(message: String) {
        val errorMsg = message.ifEmpty { resources.getString(R.string.something_went_wrong) }
        Log.e(TAG, errorMsg)
        binding.apply {
            pbBrowse.isVisible = false
            rvBrowse.isVisible = false
            errorView.root.isVisible = true
        }
        binding.errorView.apply {
            errorTxt.text = errorMsg
            retryBtn.isVisible = true
            retryBtn.setOnClickListener {
                filterModel.getFilter()?.let { filter ->
                    root.isVisible = false
                    binding.pbBrowse.isVisible = true
                    browseViewModel.getBrowse(filter)
                }
            }
        }
        browseViewModel.isLoading = false
    }

    private fun setupFiltersObservers() {
        filterModel.filterArgs.observe(viewLifecycleOwner) { filter ->
            if (binding.root.context.isTelevision) {
                if (requestedFilter == filter) return@observe
                requestedFilter = filter
                televisionFocus = null
                binding.errorView.root.isVisible = false
                binding.pbBrowse.isVisible = true
            }
            TransitionManager.beginDelayedTransition(binding.root)
            browseViewModel.getBrowse(filter)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                val layoutManager = binding.rvBrowse.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.findLastCompletelyVisibleItemPosition() + 3
                val itemCount = layoutManager.itemCount
                val filterArgs = filterModel.getFilter()

                Log.d(
                    "onScrolled",
                    "visibleItemCount=$visibleItemCount, itemCount=$itemCount," +
                            " isLoading=${browseViewModel.isLoading}, isLastPage=${browseViewModel.isLastPage}," +
                            " filterArgs=${filterArgs == null}"
                )

                if (visibleItemCount >= itemCount && !browseViewModel.isLoading && !browseViewModel.isLastPage) {
                    filterArgs?.let {
                        browseViewModel.isLoading = true
                        browseViewModel.getBrowse(it)
                    }
                }
            }
        }
    }

    private val browseAdapter by lazy {
        MediaAdapter(
            rating = true,
            mediaOnClick = { navigateToMedia(it) }
        )
    }

    private fun setupRecyclerView() {
        binding.rvBrowse.apply {
            adapter = browseAdapter
            layoutManager = if (context.isTelevision) context.mediaGridLayoutManager()
            else GridLayoutManager(context, 3)
            addOnScrollListener(this@BrowseFragment.scrollListener)
        }
    }

    override fun moveTelevisionFocusUp(focused: View): Boolean {
        val current = _binding ?: return false
        val columns = (current.rvBrowse.layoutManager as? GridLayoutManager)?.spanCount ?: return false
        return current.rvBrowse.isInFirstTelevisionRow(focused, columns) &&
            current.btnFilters.requestFocus()
    }

    private fun getMovieGenre(): LinkedHashMap<String, Int> {
        val genreMap = linkedMapOf<String, Int>()
        genreMap[getString(R.string.select_genre)] = 0
        genreMap[getString(R.string.genre_action)] = 28
        genreMap[getString(R.string.genre_adventure)] = 12
        genreMap[getString(R.string.genre_animation)] = 16
        genreMap[getString(R.string.genre_comedy)] = 35
        genreMap[getString(R.string.genre_crime)] = 80
        genreMap[getString(R.string.genre_documentary)] = 99
        genreMap[getString(R.string.genre_drama)] = 18
        genreMap[getString(R.string.genre_family)] = 10751
        genreMap[getString(R.string.genre_fantasy)] = 14
        genreMap[getString(R.string.genre_history)] = 36
        genreMap[getString(R.string.genre_horror)] = 27
        genreMap[getString(R.string.genre_music)] = 10402
        genreMap[getString(R.string.genre_mystery)] = 9648
        genreMap[getString(R.string.genre_romance)] = 10749
        genreMap[getString(R.string.genre_science_fiction)] = 878
        genreMap[getString(R.string.genre_tv_movie)] = 10770
        genreMap[getString(R.string.genre_thriller)] = 53
        genreMap[getString(R.string.genre_war)] = 10752
        genreMap[getString(R.string.genre_western)] = 37
        return genreMap
    }

    private fun getTvGenre(): LinkedHashMap<String, Int> {
        val genreMap = linkedMapOf<String, Int>()
        genreMap[getString(R.string.select_genre)] = 0
        genreMap[getString(R.string.genre_action)] = 10759
        genreMap[getString(R.string.genre_animation)] = 16
        genreMap[getString(R.string.genre_comedy)] = 35
        genreMap[getString(R.string.genre_crime)] = 80
        genreMap[getString(R.string.genre_documentary)] = 99
        genreMap[getString(R.string.genre_drama)] = 18
        genreMap[getString(R.string.genre_family)] = 10751
        genreMap[getString(R.string.genre_kids)] = 10762
        genreMap[getString(R.string.genre_mystery)] = 9648
        genreMap[getString(R.string.genre_news)] = 10763
        genreMap[getString(R.string.genre_reality)] = 10764
        genreMap[getString(R.string.genre_science_fiction)] = 10765
        genreMap[getString(R.string.genre_war)] = 10768
        genreMap[getString(R.string.genre_western)] = 37
        return genreMap
    }

    private fun getSorts(): LinkedHashMap<String, SortBy> {
        val sortMap = linkedMapOf<String, SortBy>()
        sortMap[getString(R.string.sort_popularity)] = SortBy.popularity
        sortMap[getString(R.string.sort_release_date)] = SortBy.release_date
        sortMap[getString(R.string.sort_revenue)] = SortBy.revenue
        sortMap[getString(R.string.sort_original_title)] = SortBy.original_title
        sortMap[getString(R.string.sort_average_vote)] = SortBy.vote_average
        sortMap[getString(R.string.sort_vote_count)] = SortBy.vote_count
        return sortMap
    }

    private fun getSort(key: SortBy): String =
        getSorts().entries.firstOrNull { it.value == key }?.key ?: getString(R.string.sort_popularity)

    private fun getGenre(name: String, genres: LinkedHashMap<String, Int>) = genres[name] ?: -1

    private fun getGenre(key: Int, genres: LinkedHashMap<String, Int>): String =
        genres.entries.firstOrNull { it.value == key }?.key ?: getString(R.string.select_genre)

    private fun navigateToMedia(media: Media) {
        televisionFocus = binding.rvBrowse.rememberTelevisionFocus()
        val mediaType = when {
            media.name == null -> MediaType.movie
            media.title == null -> MediaType.tv
            else -> MediaType.tv
        }
        val action = BrowseFragmentDirections.actionDiscoverFragmentToFragmentMedia(
            media.copy(media_type = mediaType)
        )
        findNavController().navigateSafe(action)
    }

    override fun onDestroyView() {
        _filtersDialog?.dismiss()
        _filtersDialog = null
        super.onDestroyView()
        binding.apply {
            rvBrowse.adapter = null
        }
        _binding = null
    }


    private fun showFiltersDialog(context: Context) {
        if (_filtersDialog == null) {
            _filtersDialog = FiltersDialog(context = requireContext())
        }

        filtersDialog.show()

        filtersDialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            if (context.isTelevision) {
                val density = resources.displayMetrics.density
                val inset = resources.getDimensionPixelSize(R.dimen.tv_safe_vertical)
                val height = (resources.configuration.screenHeightDp * density).toInt() - inset * 2
                filtersDialog.fitTelevisionWindow(680, minOf(height, (460 * density).toInt()))
                decorView.showTelevisionFocus(filtersDialog.findViewById(R.id.etKeyword))
            } else {
                setLayout(Constraints.LayoutParams.MATCH_PARENT, Constraints.LayoutParams.WRAP_CONTENT)
            }
        }

        filtersDialog.setOnDismissListener {
            _filtersDialog = null
            browseViewModel.clearKeywordList()
        }

        val dialogRoot = filtersDialog.findViewById<MaterialCardView>(R.id.dialog_root)

        val btnApply = filtersDialog.findViewById<MaterialButton>(R.id.btn_apply)
        val btnReset = filtersDialog.findViewById<MaterialButton>(R.id.btn_reset)
        val mediaChipGroup = filtersDialog.findViewById<ChipGroup>(R.id.chipGroup_media)
        val genreMenu = filtersDialog.findViewById<MaterialButton>(R.id.genre_menu)
        val sortMenu = filtersDialog.findViewById<MaterialButton>(R.id.sort_menu)
        val etKeyword = filtersDialog.findViewById<EditText>(R.id.etKeyword)
        val keywordGroup = filtersDialog.findViewById<ChipGroup>(R.id.chipGroupKeywords)
        if (context.isTelevision) {
            filtersDialog.findViewById<View>(R.id.tv_filter_cancel)?.setOnClickListener {
                _filtersDialog?.dismiss()
            }
            val suggestions = filtersDialog.findViewById<LinearLayout>(R.id.tvKeywordSuggestions)
            etKeyword.setOnEditorActionListener { input, action, _ ->
                if (action != EditorInfo.IME_ACTION_DONE) return@setOnEditorActionListener false
                Keyboard.hide(input)
                suggestions.getChildAt(0)?.requestFocus()
                true
            }
        }

        keywordSearch(etKeyword)

        val currentFilters = filterModel.getFilter()
        val moviesGenreList = getMovieGenre().keys.toList()
        val tvGenreList = getTvGenre().keys.toList()
        val keywordLayout = currentFilters?.withKeyword

        viewLifecycleOwner.lifecycleScope.launch {
            keywordGroup.removeAllViews()
            keywordLayout?.forEach { keyword ->
                addChip(
                    requireContext(),
                    keyword,
                    dialogRoot,
                    keywordGroup,
                )
            }


            if (currentFilters != null) {
                when (currentFilters.mediaType) {
                    MediaType.movie -> {
                        val movieChip = filtersDialog.findViewById<Chip>(R.id.chip_movie)
                        val tvChip = filtersDialog.findViewById<Chip>(R.id.chip_tv)
                        movieChip.isChecked = true
                        tvChip.isChecked = false
                        setupGenresMenu(context, filtersDialog, moviesGenreList)
                        if (currentFilters.withGenres == null) {
                            genreMenu.text = getString(R.string.select_genre)
                        } else {
                            genreMenu.text = getGenre(currentFilters.withGenres, getMovieGenre())
                        }
                    }

                    MediaType.tv -> {
                        val movieChip = filtersDialog.findViewById<Chip>(R.id.chip_movie)
                        val tvChip = filtersDialog.findViewById<Chip>(R.id.chip_tv)
                        movieChip.isChecked = false
                        tvChip.isChecked = true
                        setupGenresMenu(context, filtersDialog, tvGenreList)
                        if (currentFilters.withGenres == null) {
                            genreMenu.text = getString(R.string.select_genre)
                        } else {
                            genreMenu.text = getGenre(currentFilters.withGenres, getTvGenre())
                        }
                    }

                    else -> {}
                }
                sortMenu.text = getSort(currentFilters.sortBy)
            }
            setupSortMenu(context, filtersDialog)
            if (context.isTelevision) {
                filtersDialog.findViewById<Chip>(mediaChipGroup.checkedChipId)?.requestFocus()
            }
        }

        mediaChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            checkedIds.forEach { checkedId ->
                TransitionManager.beginDelayedTransition(dialogRoot)
                genreMenu.text = getString(R.string.select_genre)

                currentFilters?.let {
                    when (checkedId) {
                        R.id.chip_movie -> setupGenresMenu(context, filtersDialog, moviesGenreList)
                        R.id.chip_tv -> setupGenresMenu(context, filtersDialog, tvGenreList)
                        else -> {}
                    }
                }
            }
        }

        btnReset.setOnClickListener {
            filterModel.setFilter(
                mediaType = MediaType.movie,
                sortBy = SortBy.popularity,
                order = Order.desc,
                page = 1,
                withKeyword = null,
                withGenres = null
            )
            filtersDialog.dismiss()
        }

        btnApply.setOnClickListener {
            val checkedMediaId = mediaChipGroup.checkedChipId

            val movieGenre = getGenre(genreMenu.text.toString(), getMovieGenre())
            val tvGenre = getGenre(genreMenu.text.toString(), getTvGenre())

            val genre = when (checkedMediaId) {
                R.id.chip_movie -> if (movieGenre == -1 || movieGenre == 0) null else movieGenre
                R.id.chip_tv -> if (tvGenre == -1 || tvGenre == 0) null else tvGenre
                else -> null
            }
            val sortBy = getSorts()[sortMenu.text.toString()] ?: SortBy.popularity

            val mediaType = when (checkedMediaId) {
                R.id.chip_movie -> MediaType.movie
                R.id.chip_tv -> MediaType.tv
                else -> MediaType.movie
            }

            val keywords = keywordGroup.children.toList().map {
                it as Chip
            }.map {
                TmdbKeyword(
                    id = Integer.parseInt(it.tag.toString()),
                    name = it.text.toString()
                )
            }

            filterModel.setFilter(
                mediaType = mediaType,
                sortBy = sortBy,
                order = Order.desc,
                page = 1,
                withKeyword = keywords.ifEmpty { null },
                withGenres = genre
            )
            filtersDialog.dismiss()
        }

    }

    private fun setupGenresMenu(
        context: Context, filtersDialog: FiltersDialog,
        genreList: List<String>,
    ) {

        val dialogRoot = filtersDialog.findViewById<MaterialCardView>(R.id.dialog_root)
        val genreMenu = filtersDialog.findViewById<MaterialButton>(R.id.genre_menu)
        if (context.isTelevision) {
            genreMenu.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.genre)
                    .setSingleChoiceItems(genreList.toTypedArray(), genreList.indexOf(genreMenu.text.toString())) { dialog, position ->
                        genreMenu.text = genreList[position]
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .showWithTelevisionFocus()
            }
            return
        }

        val listPopupGenres = ListPopupWindow(
            context, null,
            androidx.appcompat.R.attr.listPopupWindowStyle
        )

        listPopupGenres.setAdapter(null)

        val adapter = ArrayAdapter(
            context,
            R.layout.item_dropdown,
            genreList
        )

        listPopupGenres.apply {
            isModal = true
            width = 450
            height = 700
            anchorView = genreMenu
            setAdapter(adapter)

            listPopupGenres.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                TransitionManager.beginDelayedTransition(dialogRoot)
                genreMenu.text = genreList[position]
                listPopupGenres.dismiss()
            }
        }

        genreMenu.setOnClickListener { listPopupGenres.show() }
    }

    private fun setupSortMenu(context: Context, filtersDialog: FiltersDialog) {

        val sortList = getSorts().keys.toList()

        val dialogRoot = filtersDialog.findViewById<MaterialCardView>(R.id.dialog_root)
        val sortMenu = filtersDialog.findViewById<MaterialButton>(R.id.sort_menu)
        if (context.isTelevision) {
            sortMenu.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.sort)
                    .setSingleChoiceItems(sortList.toTypedArray(), sortList.indexOf(sortMenu.text.toString())) { dialog, position ->
                        sortMenu.text = sortList[position]
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .showWithTelevisionFocus()
            }
            return
        }

        val listPopupSortBy = ListPopupWindow(
            context, null,
            androidx.appcompat.R.attr.listPopupWindowStyle
        )

        listPopupSortBy.setAdapter(null)

        val adapter = ArrayAdapter(
            context,
            R.layout.item_dropdown,
            sortList
        )

        listPopupSortBy.apply {
            isModal = true
            width = 450
            height = 700
            anchorView = sortMenu
            setAdapter(adapter)

            listPopupSortBy.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                TransitionManager.beginDelayedTransition(dialogRoot)
                sortMenu.text = sortList[position]
                listPopupSortBy.dismiss()
            }
        }

        sortMenu.setOnClickListener { listPopupSortBy.show() }
    }

    private fun setupKeywordList(
        context: Context, filtersDialog: FiltersDialog,
        keywordList: List<TmdbKeyword>,
    ) {
        val dialogRoot = filtersDialog.findViewById<MaterialCardView>(R.id.dialog_root)
        val tfKeyword = filtersDialog.findViewById<TextInputLayout>(R.id.tf_keyword)
        val etKeyword = filtersDialog.findViewById<EditText>(R.id.etKeyword)
        val keywordChips = filtersDialog.findViewById<ChipGroup>(R.id.chipGroupKeywords)
        if (context.isTelevision) {
            val section = filtersDialog.findViewById<View>(R.id.tvKeywordSuggestionSection)
            val suggestions = filtersDialog.findViewById<LinearLayout>(R.id.tvKeywordSuggestions)
            suggestions.removeAllViews()
            val active = keywordList.isNotEmpty() && !etKeyword.text.isNullOrBlank()
            section.isVisible = active
            if (!active) return
            val gap = resources.getDimensionPixelSize(R.dimen.tv_grid_half_gutter)
            val rowHeight = resources.getDimensionPixelSize(R.dimen.tv_option_min_height)
            keywordList.forEach { keyword ->
                val option = TextView(context).apply {
                    text = keyword.name
                    textSize = 17f
                    gravity = Gravity.CENTER_VERTICAL
                    isFocusable = true
                    minHeight = rowHeight
                    setTextColor(context.getColor(R.color.textColor))
                    setPadding(gap, 0, gap, 0)
                    setBackgroundResource(R.drawable.tv_focus_surface)
                    setOnClickListener {
                        addChip(context, keyword, dialogRoot, keywordChips)
                        etKeyword.requestFocus()
                        etKeyword.text.clear()
                        suggestions.removeAllViews()
                        section.isVisible = false
                    }
                }
                suggestions.addView(option, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            return
        }

        val listPopupKeyword = ListPopupWindow(
            context, null,
            androidx.appcompat.R.attr.listPopupWindowStyle
        )

        listPopupKeyword.setAdapter(null)

        val adapter = ArrayAdapter(
            context,
            R.layout.item_dropdown,
            keywordList.map { it.name }
        )

        listPopupKeyword.apply {
            isModal = true
            anchorView = tfKeyword
            setAdapter(adapter)

            setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val keyword = keywordList[position]
                addChip(requireContext(), keyword, dialogRoot, keywordChips)
                listPopupKeyword.dismiss()
                etKeyword.text.clear()
                Log.d(TAG, "Selected keyword=$keyword")
            }

            setOnDismissListener {
                browseViewModel.clearKeywordList()
            }

        }.also { it.show() }
    }

    private fun addChip(
        context: Context,
        keyword: TmdbKeyword,
        root: ViewGroup,
        chipGroup: ChipGroup
    ) {

        val layoutInflater = context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater

        val mChip = layoutInflater.inflate(
            R.layout.ic_keyword_chip,
            root, false
        ) as Chip

        mChip.text = keyword.name
        mChip.tag = keyword.id

        mChip.setOnCloseIconClickListener {
            chipGroup.removeView(mChip)
        }
        if (context.isTelevision) {
            mChip.contentDescription = getString(R.string.tv_remove_keyword, keyword.name)
            mChip.setOnClickListener {
                root.findViewById<EditText>(R.id.etKeyword)?.requestFocus()
                chipGroup.removeView(mChip)
            }
        }

        chipGroup.addView(mChip)
    }
}
