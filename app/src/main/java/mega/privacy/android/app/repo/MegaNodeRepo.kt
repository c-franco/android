package mega.privacy.android.app.repo

import android.util.Pair
import mega.privacy.android.app.DatabaseHandler
import mega.privacy.android.app.MegaApplication
import mega.privacy.android.app.MegaOffline
import mega.privacy.android.app.MimeTypeThumbnail
import mega.privacy.android.app.di.MegaApi
import mega.privacy.android.app.utils.Constants.*
import mega.privacy.android.app.utils.FileUtil
import mega.privacy.android.app.utils.LogUtil.logError
import mega.privacy.android.app.utils.OfflineUtils.getOfflineFile
import mega.privacy.android.app.utils.SortUtil.*
import mega.privacy.android.app.utils.Util
import nz.mega.sdk.MegaApiAndroid
import nz.mega.sdk.MegaApiJava.*
import nz.mega.sdk.MegaNode
import nz.mega.sdk.MegaNodeList
import java.io.File
import java.time.YearMonth
import java.util.*
import java.util.function.Function
import javax.inject.Inject
import kotlin.collections.ArrayList

class MegaNodeRepo @Inject constructor(
    @MegaApi private val megaApi: MegaApiAndroid,
    private val dbHandler: DatabaseHandler
) {

    fun getCuChildren(orderBy: Int): List<MegaNode> {
        var cuNode: MegaNode? = null
        var muNode: MegaNode? = null
        val pref = dbHandler.preferences

        if (pref?.camSyncHandle != null) {
            try {
                val cuHandle = pref.camSyncHandle.toLong()
                cuNode = megaApi.getNodeByHandle(cuHandle)
            } catch (e: NumberFormatException) {
                logError("parse getCamSyncHandle error $e")
            }
        }

        if (pref?.megaHandleSecondaryFolder != null) {
            try {
                val muHandle = pref.megaHandleSecondaryFolder.toLong()
                muNode = megaApi.getNodeByHandle(muHandle)
            } catch (e: NumberFormatException) {
                logError("parse MegaHandleSecondaryFolder error $e")
            }
        }

        if (cuNode == null && muNode == null) {
            return emptyList()
        }

        val nodeList = MegaNodeList.createInstance()

        if (cuNode != null) {
            nodeList.addNode(cuNode)
        }

        if (muNode != null) {
            nodeList.addNode(muNode)
        }

        return megaApi.getChildren(nodeList, orderBy)
    }

    /**
     * Get children of CU/MU, with the given order, and filter nodes by date (optional).
     *
     * @param orderBy order
     * @param filter search filter
     * filter[0] is the search type:
     * 1 means search for nodes in one day, then filter[1] is the day in millis.
     * 2 means search for nodes in last month (filter[2] is 1), or in last year (filter[2] is 2).
     * 3 means search for nodes between two days, filter[3] and filter[4] are start and end day in
     * millis.
     * @return list of pairs, whose first value is index used for
     * FullscreenImageViewer/AudioVideoPlayer, and second value is the node
     */
    fun getCuChildren(
        orderBy: Int,
        filter: LongArray?
    ): List<Pair<Int, MegaNode>> {
        val children = getCuChildren(orderBy)
        val nodes = ArrayList<Pair<Int, MegaNode>>()

        for ((index, node) in children.withIndex()) {
            if (node.isFolder) {
                continue
            }

            val mime = MimeTypeThumbnail.typeForName(node.name)
            if (mime.isImage || mime.isVideoReproducible) {
                // when not in search mode, index used by viewer is index in all siblings,
                // including non image/video nodes
                nodes.add(Pair.create(index, node))
            }
        }

        if (filter == null) {
            return nodes
        }

        val result = ArrayList<Pair<Int, MegaNode>>()
        var filterFunction: Function<MegaNode, Boolean>? = null

        when (filter[SEARCH_BY_DATE_FILTER_POS_TYPE]) {
            SEARCH_BY_DATE_FILTER_TYPE_ONE_DAY -> {
                val date = Util.fromEpoch(filter[SEARCH_BY_DATE_FILTER_POS_THE_DAY] / 1000)
                filterFunction = Function { node: MegaNode ->
                    date == Util.fromEpoch(node.modificationTime)
                }
            }
            SEARCH_BY_DATE_FILTER_TYPE_LAST_MONTH_OR_YEAR -> {
                when (filter[SEARCH_BY_DATE_FILTER_POS_MONTH_OR_YEAR]) {
                    SEARCH_BY_DATE_FILTER_LAST_MONTH -> {
                        val lastMonth = YearMonth.now().minusMonths(1)
                        filterFunction = Function { node: MegaNode ->
                            lastMonth == YearMonth.from(Util.fromEpoch(node.modificationTime))
                        }
                    }
                    SEARCH_BY_DATE_FILTER_LAST_YEAR -> {
                        val lastYear = YearMonth.now().year - 1
                        filterFunction = Function { node: MegaNode ->
                            Util.fromEpoch(node.modificationTime).year == lastYear
                        }
                    }
                }
            }
            SEARCH_BY_DATE_FILTER_TYPE_BETWEEN_TWO_DAYS -> {
                val from = Util.fromEpoch(filter[SEARCH_BY_DATE_FILTER_POS_START_DAY] / 1000)
                val to = Util.fromEpoch(filter[SEARCH_BY_DATE_FILTER_POS_END_DAY] / 1000)
                filterFunction = Function { node: MegaNode ->
                    val modifyDate = Util.fromEpoch(node.modificationTime)
                    !modifyDate.isBefore(from) && !modifyDate.isAfter(to)
                }
            }
        }

        if (filterFunction == null) {
            return result
        }

        // when in search mode, index used by viewer is also index in all siblings,
        // but all siblings are image/video, non image/video nodes are filtered by previous step
        var indexInSiblings = 0
        for (node in nodes) {
            if (filterFunction.apply(node.second)) {
                result.add(Pair.create(indexInSiblings, node.second))
                indexInSiblings++
            }
        }
        return result
    }

    fun findOfflineNode(handle: String): MegaOffline? {
        return dbHandler.findByHandle(handle)
    }

    fun loadOfflineNodes(path: String, order: Int, searchQuery: String?): List<MegaOffline> {
        val nodes = if (searchQuery != null && searchQuery.isNotEmpty()) {
            searchOfflineNodes(path, searchQuery)
        } else {
            dbHandler.findByPath(path)
        }

        when (order) {
            ORDER_DEFAULT_DESC -> {
                sortOfflineByNameDescending(nodes)
            }
            ORDER_DEFAULT_ASC -> {
                sortOfflineByNameAscending(nodes)
            }
            ORDER_MODIFICATION_ASC -> {
                sortOfflineByModificationDateAscending(nodes)
            }
            ORDER_MODIFICATION_DESC -> {
                sortOfflineByModificationDateDescending(nodes)
            }
            ORDER_SIZE_ASC -> {
                sortOfflineBySizeAscending(nodes)
            }
            ORDER_SIZE_DESC -> {
                sortOfflineBySizeDescending(nodes)
            }
            else -> {
            }
        }
        return nodes
    }

    private fun searchOfflineNodes(path: String, query: String): ArrayList<MegaOffline> {
        val result = ArrayList<MegaOffline>()

        val nodes = dbHandler.findByPath(path)
        for (node in nodes) {
            if (node.isFolder) {
                result.addAll(searchOfflineNodes(getChildPath(node), query))
            }

            if (node.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) &&
                FileUtil.isFileAvailable(getOfflineFile(MegaApplication.getInstance(), node))
            ) {
                result.add(node)
            }
        }

        return result
    }

    private fun getChildPath(offline: MegaOffline): String {
        return if (offline.path.endsWith(File.separator)) {
            offline.path + offline.name + File.separator
        } else {
            offline.path + File.separator + offline.name + File.separator
        }
    }

    companion object {
        const val CU_TYPE_CAMERA = 0
        const val CU_TYPE_MEDIA = 1
    }
}
