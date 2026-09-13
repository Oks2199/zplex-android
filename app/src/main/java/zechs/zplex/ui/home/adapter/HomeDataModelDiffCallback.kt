package zechs.zplex.ui.home.adapter

import androidx.recyclerview.widget.DiffUtil

class HomeDataModelDiffCallback : DiffUtil.ItemCallback<HomeDataModel>() {

    override fun areItemsTheSame(
        oldItem: HomeDataModel,
        newItem: HomeDataModel
    ): Boolean = when {
        oldItem is HomeDataModel.Header && newItem
                is HomeDataModel.Header && oldItem.section == newItem.section
            -> true

        oldItem is HomeDataModel.Attribution && newItem is HomeDataModel.Attribution
            -> true

        oldItem is HomeDataModel.DiscoveryTitle && newItem is HomeDataModel.DiscoveryTitle
            -> true

        oldItem is HomeDataModel.Media && newItem
                is HomeDataModel.Media && oldItem.section == newItem.section
            -> true

        oldItem is HomeDataModel.Banner && newItem is HomeDataModel.Banner
            -> true

        oldItem is HomeDataModel.Watched && newItem
                is HomeDataModel.Watched && oldItem.section == newItem.section
            -> true

        else -> false
    }

    override fun areContentsTheSame(
        oldItem: HomeDataModel, newItem: HomeDataModel
    ) = oldItem == newItem

}
