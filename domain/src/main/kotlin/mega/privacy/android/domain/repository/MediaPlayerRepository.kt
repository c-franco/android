package mega.privacy.android.domain.repository

import kotlinx.coroutines.flow.Flow
import mega.privacy.android.domain.entity.SortOrder
import mega.privacy.android.domain.entity.mediaplayer.PlaybackInformation
import mega.privacy.android.domain.entity.node.TypedFileNode
import mega.privacy.android.domain.entity.node.UnTypedNode

/**
 * Repository for media player
 */
interface MediaPlayerRepository {

    /**
     * Get type node by handle
     *
     * @param handle node handle
     * @return [UnTypedNode]?
     */
    suspend fun getTypedNodeByHandle(handle: Long): UnTypedNode?

    /**
     * Returns a URL to a node in the local HTTP proxy server for folder link from MegaApiFolder
     *
     * @param nodeHandle node Handle
     * @return URL to the node in the local HTTP proxy server, otherwise NULL
     */
    suspend fun getLocalLinkForFolderLinkFromMegaApiFolder(nodeHandle: Long): String?

    /**
     * Returns a URL to a node in the local HTTP proxy server for folder link from MegaApi
     *
     * @param nodeHandle node Handle
     * @return URL to the node in the local HTTP proxy server, otherwise NULL
     */
    suspend fun getLocalLinkForFolderLinkFromMegaApi(nodeHandle: Long): String?

    /**
     * Returns a URL to a node in the local HTTP proxy server from MegaApi
     *
     * @param nodeHandle node Handle
     * @return URL to the node in the local HTTP proxy server, otherwise NULL
     */
    suspend fun getLocalLinkFromMegaApi(nodeHandle: Long): String?

    /**
     * Get all audio nodes
     *
     * @param order list order
     * @return audio nodes
     */
    suspend fun getAudioNodes(order: SortOrder): List<UnTypedNode>

    /**
     * Get all video nodes
     *
     * @param order list order
     * @return video nodes
     */
    suspend fun getVideoNodes(order: SortOrder): List<UnTypedNode>

    /**
     * Get thumbnail from MegaApiFolder
     *
     * @param nodeHandle node handle
     * @param path thumbnail path
     * @param finishedCallback callback of getting thumbnail finished
     */
    suspend fun getThumbnailFromMegaApiFolder(
        nodeHandle: Long,
        path: String,
        finishedCallback: (nodeHandle: Long) -> Unit,
    )

    /**
     * Get thumbnail from MegaApi
     *
     * @param nodeHandle node handle
     * @param path thumbnail path
     * @param finishedCallback callback of getting thumbnail finished
     */
    suspend fun getThumbnailFromMegaApi(
        nodeHandle: Long,
        path: String,
        finishedCallback: (nodeHandle: Long) -> Unit,
    )

    /**
     * Credentials whether is null
     *
     * @return true is null, otherwise is false
     */
    suspend fun credentialsIsNull(): Boolean

    /**
     * Get rubbish node
     *
     * @return [UnTypedNode]?
     */
    suspend fun getRubbishNode(): UnTypedNode?

    /**
     * Get inbox node
     *
     * @return [UnTypedNode]?
     */
    suspend fun getInboxNode(): UnTypedNode?

    /**
     * Get root node
     *
     * @return [UnTypedNode]?
     */
    suspend fun getRootNode(): UnTypedNode?

    /**
     * MegaApiFolder gets root node
     *
     * @return [UnTypedNode]?
     */
    suspend fun megaApiFolderGetRootNode(): UnTypedNode?

    /**
     * Get parent node by handle
     *
     * @param parentHandle node handle
     * @return [UnTypedNode]?
     */
    suspend fun getParentNodeByHandle(parentHandle: Long): UnTypedNode?

    /**
     * MegaApiFolder gets parent node by handle
     *
     * @param parentHandle node handle
     * @return [UnTypedNode]?
     */
    suspend fun megaApiFolderGetParentNode(parentHandle: Long): UnTypedNode?

    /**
     * Get children by parent node handle
     *
     * @param isAudio true is audio player, otherwise is false
     * @param parentHandle parent node handle
     * @param order list order
     * @return List<[UnTypedNode]>?
     */
    suspend fun getChildrenByParentHandle(
        isAudio: Boolean,
        parentHandle: Long,
        order: SortOrder,
    ): List<UnTypedNode>?

    /**
     * MegaApiFolder gets children by parent node handle
     *
     * @param isAudio true is audio player, otherwise is false
     * @param parentHandle parent node handle
     * @param order list order
     * @return List<[UnTypedNode]>?
     */
    suspend fun megaApiFolderGetChildrenByParentHandle(
        isAudio: Boolean,
        parentHandle: Long,
        order: SortOrder,
    ): List<UnTypedNode>?

    /**
     * Get nodes from public links
     * @param isAudio true is audio player, otherwise is false
     * @param order list order
     * @return List<[UnTypedNode]>
     */
    suspend fun getNodesFromPublicLinks(
        isAudio: Boolean,
        order: SortOrder,
    ): List<UnTypedNode>

    /**
     * Get nodes from InShares
     * @param isAudio true is audio player, otherwise is false
     * @param order list order
     * @return List<[UnTypedNode]>
     */
    suspend fun getNodesFromInShares(
        isAudio: Boolean,
        order: SortOrder,
    ): List<UnTypedNode>

    /**
     * Get nodes from OutShares
     * @param isAudio true is audio player, otherwise is false
     * @param order list order
     * @return List<[UnTypedNode]>
     */
    suspend fun getNodesFromOutShares(
        isAudio: Boolean,
        lastHandle: Long,
        order: SortOrder,
    ): List<UnTypedNode>

    /**
     * Get nodes by email
     *
     * @param isAudio true is audio player, otherwise is false
     * @param email email of account
     * @return List<[UnTypedNode]>?
     */
    suspend fun getNodesByEmail(isAudio: Boolean, email: String): List<UnTypedNode>?

    /**
     * Get username by email
     *
     * @param email email of account
     * @return username
     */
    suspend fun getUserNameByEmail(email: String): String?

    /**
     * Get nodes by handles
     *
     * @param isAudio true is audio player, otherwise is false
     * @param handles handle list
     * @return List<[UnTypedNode]>
     */
    suspend fun getNodesByHandles(isAudio: Boolean, handles: List<Long>): List<UnTypedNode>

    /**
     * MegaApi http server stop
     */
    suspend fun megaApiHttpServerStop()

    /**
     * MegaApiFolder http server stop
     */
    suspend fun megaApiFolderHttpServerStop()

    /**
     * MegaApi http server whether is running
     *
     * @return 0 if the server is not running. Otherwise the port in which it's listening to
     */
    suspend fun megaApiHttpServerIsRunning(): Int

    /**
     * MegaApiFolder http server whether is running
     *
     * @return 0 if the server is not running. Otherwise the port in which it's listening to
     */
    suspend fun megaApiFolderHttpServerIsRunning(): Int

    /**
     * MegaApi http server starts
     *
     * @return True if the server is ready, false if the initialization failed
     */
    suspend fun megaApiHttpServerStart(): Boolean

    /**
     * MegaApiFolder http server starts
     *
     * @return True if the server is ready, false if the initialization failed
     */
    suspend fun megaApiFolderHttpServerStart(): Boolean

    /**
     * MegaApi sets the maximum buffer size for the internal buffer
     *
     * @param bufferSize Maximum buffer size (in bytes) or a number <= 0 to use the
     *                   internal default value
     */
    suspend fun megaApiHttpServerSetMaxBufferSize(bufferSize: Int)

    /**
     * MegaApiFolder sets the maximum buffer size for the internal buffer
     *
     * @param bufferSize Maximum buffer size (in bytes) or a number <= 0 to use the
     *                   internal default value
     */
    suspend fun megaApiFolderHttpServerSetMaxBufferSize(bufferSize: Int)

    /**
     * Get the fingerprint of a file by path
     *
     * @param filePath file path
     * @return fingerprint
     */
    suspend fun getFingerprint(filePath: String): String?

    /**
     * Get the local folder path
     *
     * @param typedFileNode [TypedFileNode]
     * @return folder path or null
     */
    suspend fun getLocalFilePath(typedFileNode: TypedFileNode?): String?

    /**
     * Delete the playback information
     *
     * @param mediaId the media id of deleted item
     */
    suspend fun deletePlaybackInformation(mediaId: Long)

    /**
     * Save the playback times
     */
    suspend fun savePlaybackTimes()

    /**
     * Update playback information
     *
     * @param playbackInformation the new playback information
     */
    suspend fun updatePlaybackInformation(playbackInformation: PlaybackInformation)

    /**
     * Monitor playback times
     *
     * @return Flow<Map<Long, PlaybackInformation>?>
     */
    fun monitorPlaybackTimes(): Flow<Map<Long, PlaybackInformation>?>
}