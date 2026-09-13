package zechs.zplex.ui.shared_adapters.banner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.databinding.ItemWideBannerBinding

class BannerAdapter(
    val bannerOnClick: (BannerItem) -> Unit
) : ListAdapter<BannerItem, BannerViewHolder>(BannerItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BannerViewHolder(
        itemBinding = ItemWideBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        bannerAdapter = this
    )

    override fun onBindViewHolder(
        holder: BannerViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }

}
