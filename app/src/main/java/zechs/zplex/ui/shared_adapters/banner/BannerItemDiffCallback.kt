package zechs.zplex.ui.shared_adapters.banner

import androidx.recyclerview.widget.DiffUtil

class BannerItemDiffCallback : DiffUtil.ItemCallback<BannerItem>() {

    override fun areItemsTheSame(oldItem: BannerItem, newItem: BannerItem) =
        oldItem.media.id == newItem.media.id &&
            oldItem.media.media_type == newItem.media.media_type

    override fun areContentsTheSame(oldItem: BannerItem, newItem: BannerItem) =
        oldItem == newItem
}
