package zechs.zplex.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.databinding.ItemHeadingBinding
import zechs.zplex.databinding.ItemAttributionBinding
import zechs.zplex.databinding.ItemDiscoveryHeadingBinding
import zechs.zplex.databinding.ItemListBinding

class HomeDataAdapter(
    val homeClickListener: HomeClickListener
) : ListAdapter<HomeDataModel, HomeViewHolder>(
    HomeDataModelDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): HomeViewHolder {

        return when (viewType) {
            R.layout.item_heading -> HomeViewHolder.HeadingViewHolder(
                ItemHeadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            R.layout.item_attribution -> HomeViewHolder.AttributionViewHolder(
                ItemAttributionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            R.layout.item_discovery_heading -> HomeViewHolder.DiscoveryTitleViewHolder(
                ItemDiscoveryHeadingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            R.layout.item_list -> HomeViewHolder.ListViewHolder(
                ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                this
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: HomeViewHolder,
        position: Int
    ) {
        val item = getItem(position)

        when (holder) {
            is HomeViewHolder.HeadingViewHolder ->
                holder.bind(item as HomeDataModel.Header)

            is HomeViewHolder.AttributionViewHolder ->
                holder.bind(item as HomeDataModel.Attribution)

            is HomeViewHolder.DiscoveryTitleViewHolder ->
                holder.bind(item as HomeDataModel.DiscoveryTitle)

            is HomeViewHolder.ListViewHolder -> {
                when (item) {
                    is HomeDataModel.Media -> holder.bindMedia(item)
                    is HomeDataModel.Banner -> holder.bindBanner(item)
                    is HomeDataModel.Watched -> holder.bindWatched(item)
                    else -> {}
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeDataModel.Header -> R.layout.item_heading
            is HomeDataModel.Attribution -> R.layout.item_attribution
            is HomeDataModel.DiscoveryTitle -> R.layout.item_discovery_heading
            is HomeDataModel.Media -> R.layout.item_list
            is HomeDataModel.Banner -> R.layout.item_list
            is HomeDataModel.Watched -> R.layout.item_list
        }
    }

}
