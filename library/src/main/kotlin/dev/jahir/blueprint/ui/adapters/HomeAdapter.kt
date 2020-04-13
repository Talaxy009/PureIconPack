package dev.jahir.blueprint.ui.adapters

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.view.children
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.blueprint.R
import dev.jahir.blueprint.data.listeners.HomeItemsListener
import dev.jahir.blueprint.data.models.Counter
import dev.jahir.blueprint.data.models.HomeItem
import dev.jahir.blueprint.data.models.Icon
import dev.jahir.blueprint.data.models.IconsCounter
import dev.jahir.blueprint.data.models.KustomCounter
import dev.jahir.blueprint.data.models.WallpapersCounter
import dev.jahir.blueprint.data.models.ZooperCounter
import dev.jahir.blueprint.extensions.safeNotifySectionChanged
import dev.jahir.blueprint.ui.viewholders.AppLinkViewHolder
import dev.jahir.blueprint.ui.viewholders.CounterViewHolder
import dev.jahir.blueprint.ui.viewholders.IconsPreviewViewHolder
import dev.jahir.frames.extensions.views.gone
import dev.jahir.frames.extensions.views.inflate
import dev.jahir.frames.ui.viewholders.SectionHeaderViewHolder

@Suppress("MemberVisibilityCanBePrivate")
class HomeAdapter(
    showOverview: Boolean = true,
    private var listener: HomeItemsListener? = null
) : SectionedRecyclerViewAdapter<SectionedViewHolder>() {

    var showOverview: Boolean = showOverview
        set(value) {
            if (value == field) return
            field = value
            notifyDataSetChanged()
        }

    var wallpaper: Drawable? = null
        set(value) {
            if (field != null) return
            field = value
            safeNotifySectionChanged(ICONS_PREVIEW_SECTION)
        }

    var iconsPreviewList: ArrayList<Icon> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
            safeNotifySectionChanged(ICONS_PREVIEW_SECTION)
        }

    var homeItems: ArrayList<HomeItem> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
            safeNotifySectionChanged(MORE_APPS_SECTION - (if (showOverview) 0 else 1))
            safeNotifySectionChanged(USEFUL_LINKS_SECTION - (if (showOverview) 0 else 1))
        }

    private val appItems: ArrayList<HomeItem>
        get() = ArrayList(homeItems.filter { it.isAnApp })

    private val linkItems: ArrayList<HomeItem>
        get() = ArrayList(homeItems.filter { !it.isAnApp })

    var iconsCount: Int = 0
        set(value) {
            if (value == field) return
            field = value
            if (showOverview) safeNotifySectionChanged(OVERVIEW_SECTION)
        }

    var wallpapersCount: Int = 0
        set(value) {
            if (value == field) return
            field = value
            if (showOverview) safeNotifySectionChanged(OVERVIEW_SECTION)
        }

    var kustomCount: Int = 0
        set(value) {
            if (value == field) return
            field = value
            if (showOverview) safeNotifySectionChanged(OVERVIEW_SECTION)
        }

    var zooperCount: Int = 0
        set(value) {
            if (value == field) return
            field = value
            if (showOverview) safeNotifySectionChanged(OVERVIEW_SECTION)
        }

    private val counters: List<Counter>
        get() = if (showOverview) {
            arrayOf(
                IconsCounter(iconsCount),
                WallpapersCounter(wallpapersCount),
                KustomCounter(kustomCount),
                ZooperCounter(zooperCount)
            ).filter { it.count > 0 }
        } else listOf()

    init {
        shouldShowFooters(false)
        shouldShowHeadersForEmptySections(false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder =
        when (viewType) {
            ICONS_PREVIEW_SECTION -> IconsPreviewViewHolder(parent.inflate(R.layout.item_home_icons_preview))
            OVERVIEW_SECTION -> {
                if (showOverview) CounterViewHolder(parent.inflate(R.layout.item_stats))
                else AppLinkViewHolder(parent.inflate(R.layout.item_home_app_link))
            }
            MORE_APPS_SECTION, USEFUL_LINKS_SECTION ->
                AppLinkViewHolder(parent.inflate(R.layout.item_home_app_link))
            else -> SectionHeaderViewHolder(parent.inflate(R.layout.item_section_header))
        }

    override fun onBindHeaderViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        expanded: Boolean
    ) {
        if (section <= ICONS_PREVIEW_SECTION) {
            (holder?.itemView as? ViewGroup)?.children?.forEach { it.gone() }
        } else {
            (holder as? SectionHeaderViewHolder)?.let {
                when (section) {
                    OVERVIEW_SECTION -> {
                        if (showOverview) it.bind(R.string.overview, 0, false)
                        else it.bind(R.string.more_apps, 0, false)
                    }
                    MORE_APPS_SECTION -> {
                        if (showOverview) it.bind(R.string.more_apps, 0)
                        else it.bind(R.string.useful_links, 0)
                    }
                    USEFUL_LINKS_SECTION -> {
                        if (showOverview) it.bind(R.string.useful_links, 0)
                        else it.bind("", "")
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ) {
        (holder as? IconsPreviewViewHolder)?.bind(iconsPreviewList, wallpaper, listener)
        (holder as? CounterViewHolder)?.bind(counters.getOrNull(relativePosition), listener)
        val appItemsSection = OVERVIEW_SECTION + (if (showOverview) 1 else 0)
        (holder as? AppLinkViewHolder)?.bind(
            if (section == appItemsSection) appItems.getOrNull(relativePosition)
            else linkItems.getOrNull(relativePosition),
            listener
        )
    }

    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
    override fun getItemCount(section: Int): Int = when (section) {
        ICONS_PREVIEW_SECTION -> 1
        OVERVIEW_SECTION -> if (showOverview) counters.size else appItems.size
        MORE_APPS_SECTION -> if (showOverview) appItems.size else linkItems.size
        USEFUL_LINKS_SECTION -> linkItems.size
        else -> 0
    }

    override fun getSectionCount(): Int = SECTION_COUNT - (if (showOverview) 0 else 1)
    override fun getItemViewType(section: Int, relativePosition: Int, absolutePosition: Int): Int =
        section

    override fun getRowSpan(
        fullSpanSize: Int,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ): Int = if (section == OVERVIEW_SECTION && showOverview) 1 else 2

    companion object {
        private const val SECTION_COUNT = 4
        private const val ICONS_PREVIEW_SECTION = 0
        private const val OVERVIEW_SECTION = 1
        private const val MORE_APPS_SECTION = 2
        private const val USEFUL_LINKS_SECTION = 3
    }
}