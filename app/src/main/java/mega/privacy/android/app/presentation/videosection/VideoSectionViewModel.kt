package mega.privacy.android.app.presentation.videosection

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mega.privacy.android.app.R
import mega.privacy.android.app.domain.usecase.GetNodeByHandle
import mega.privacy.android.app.presentation.videosection.mapper.VideoPlaylistUIEntityMapper
import mega.privacy.android.app.presentation.videosection.mapper.VideoUIEntityMapper
import mega.privacy.android.app.presentation.videosection.model.DurationFilterOption
import mega.privacy.android.app.presentation.videosection.model.LocationFilterOption
import mega.privacy.android.app.presentation.videosection.model.VideoPlaylistUIEntity
import mega.privacy.android.app.presentation.videosection.model.VideoSectionState
import mega.privacy.android.app.presentation.videosection.model.VideoSectionTab
import mega.privacy.android.app.presentation.videosection.model.VideoSectionTabState
import mega.privacy.android.app.presentation.videosection.model.VideoUIEntity
import mega.privacy.android.app.utils.Constants.INTENT_EXTRA_KEY_NEED_STOP_HTTP_SERVER
import mega.privacy.android.app.utils.FileUtil.getDownloadLocation
import mega.privacy.android.app.utils.FileUtil.getLocalFile
import mega.privacy.android.app.utils.FileUtil.isFileAvailable
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.entity.node.TypedNode
import mega.privacy.android.domain.usecase.GetCloudSortOrder
import mega.privacy.android.domain.usecase.GetFileUrlByNodeHandleUseCase
import mega.privacy.android.domain.usecase.GetNodeByIdUseCase
import mega.privacy.android.domain.usecase.IsHiddenNodesOnboardedUseCase
import mega.privacy.android.domain.usecase.UpdateNodeSensitiveUseCase
import mega.privacy.android.domain.usecase.account.MonitorAccountDetailUseCase
import mega.privacy.android.domain.usecase.file.GetFingerprintUseCase
import mega.privacy.android.domain.usecase.mediaplayer.MegaApiHttpServerIsRunningUseCase
import mega.privacy.android.domain.usecase.mediaplayer.MegaApiHttpServerStartUseCase
import mega.privacy.android.domain.usecase.node.MonitorNodeUpdatesUseCase
import mega.privacy.android.domain.usecase.offline.MonitorOfflineNodeUpdatesUseCase
import mega.privacy.android.domain.usecase.photos.GetNextDefaultAlbumNameUseCase
import mega.privacy.android.domain.usecase.videosection.AddVideosToPlaylistUseCase
import mega.privacy.android.domain.usecase.videosection.CreateVideoPlaylistUseCase
import mega.privacy.android.domain.usecase.videosection.GetAllVideosUseCase
import mega.privacy.android.domain.usecase.videosection.GetSyncUploadsFolderIdsUseCase
import mega.privacy.android.domain.usecase.videosection.GetVideoPlaylistsUseCase
import mega.privacy.android.domain.usecase.videosection.MonitorVideoPlaylistSetsUpdateUseCase
import mega.privacy.android.domain.usecase.videosection.RemoveVideoPlaylistsUseCase
import mega.privacy.android.domain.usecase.videosection.RemoveVideosFromPlaylistUseCase
import mega.privacy.android.domain.usecase.videosection.UpdateVideoPlaylistTitleUseCase
import nz.mega.sdk.MegaNode
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Videos section view model
 */
@HiltViewModel
class VideoSectionViewModel @Inject constructor(
    private val getAllVideosUseCase: GetAllVideosUseCase,
    private val videoUIEntityMapper: VideoUIEntityMapper,
    private val getCloudSortOrder: GetCloudSortOrder,
    private val monitorNodeUpdatesUseCase: MonitorNodeUpdatesUseCase,
    private val monitorOfflineNodeUpdatesUseCase: MonitorOfflineNodeUpdatesUseCase,
    private val getNodeByHandle: GetNodeByHandle,
    private val getFingerprintUseCase: GetFingerprintUseCase,
    private val megaApiHttpServerIsRunningUseCase: MegaApiHttpServerIsRunningUseCase,
    private val megaApiHttpServerStartUseCase: MegaApiHttpServerStartUseCase,
    private val getFileUrlByNodeHandleUseCase: GetFileUrlByNodeHandleUseCase,
    private val getNodeByIdUseCase: GetNodeByIdUseCase,
    private val getVideoPlaylistsUseCase: GetVideoPlaylistsUseCase,
    private val videoPlaylistUIEntityMapper: VideoPlaylistUIEntityMapper,
    private val createVideoPlaylistUseCase: CreateVideoPlaylistUseCase,
    private val addVideosToPlaylistUseCase: AddVideosToPlaylistUseCase,
    private val getNextDefaultAlbumNameUseCase: GetNextDefaultAlbumNameUseCase,
    private val removeVideoPlaylistsUseCase: RemoveVideoPlaylistsUseCase,
    private val updateVideoPlaylistTitleUseCase: UpdateVideoPlaylistTitleUseCase,
    private val getSyncUploadsFolderIdsUseCase: GetSyncUploadsFolderIdsUseCase,
    private val removeVideosFromPlaylistUseCase: RemoveVideosFromPlaylistUseCase,
    private val monitorVideoPlaylistSetsUpdateUseCase: MonitorVideoPlaylistSetsUpdateUseCase,
    private val updateNodeSensitiveUseCase: UpdateNodeSensitiveUseCase,
    private val monitorAccountDetailUseCase: MonitorAccountDetailUseCase,
    private val isHiddenNodesOnboardedUseCase: IsHiddenNodesOnboardedUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(VideoSectionState())

    /**
     * The state regarding the business logic
     */
    val state: StateFlow<VideoSectionState> = _state.asStateFlow()

    private val _tabState = MutableStateFlow(VideoSectionTabState())

    /**
     * The state regarding the tabs
     */
    val tabState = _tabState.asStateFlow()

    private var searchQuery = ""
    private val originalData = mutableListOf<VideoUIEntity>()
    private val originalPlaylistData = mutableListOf<VideoPlaylistUIEntity>()

    private var createVideoPlaylistJob: Job? = null

    init {
        refreshNodesIfAnyUpdates()
        viewModelScope.launch {
            monitorVideoPlaylistSetsUpdateUseCase().conflate()
                .catch {
                    Timber.e(it)
                }.collect {
                    if (it.isNotEmpty()) {
                        if (_state.value.currentVideoPlaylist != null) {
                            refreshVideoPlaylistsWithUpdateCurrentVideoPlaylist()
                        } else {
                            loadVideoPlaylists()
                        }
                    }
                }
        }
        monitorAccountDetail()
        monitorIsHiddenNodesOnboarded()
    }

    private fun refreshNodesIfAnyUpdates() {
        viewModelScope.launch {
            merge(
                monitorNodeUpdatesUseCase(),
                monitorOfflineNodeUpdatesUseCase()
            ).conflate()
                .catch {
                    Timber.e(it)
                }.collect {
                    setPendingRefreshNodes()
                }
        }
    }

    private fun loadVideoPlaylists() {
        viewModelScope.launch {
            val videoPlaylists =
                getVideoPlaylists().updateOriginalPlaylistData().filterVideoPlaylistsBySearchQuery()
            _state.update {
                it.copy(
                    videoPlaylists = videoPlaylists,
                    isPlaylistProgressBarShown = false,
                    scrollToTop = false
                )
            }
        }
    }

    private suspend fun getVideoPlaylists() = getVideoPlaylistsUseCase().map { videoPlaylist ->
        videoPlaylistUIEntityMapper(videoPlaylist)
    }

    private fun List<VideoPlaylistUIEntity>.updateOriginalPlaylistData() = also { data ->
        if (originalPlaylistData.isNotEmpty()) {
            originalPlaylistData.clear()
        }
        originalPlaylistData.addAll(data)
    }

    private fun List<VideoPlaylistUIEntity>.filterVideoPlaylistsBySearchQuery() =
        filter { playlist ->
            playlist.title.contains(searchQuery, true)
        }

    private fun setPendingRefreshNodes() = _state.update { it.copy(isPendingRefresh = true) }

    internal fun refreshNodes() = viewModelScope.launch {
        val videoList = getVideoUIEntityList()
            .updateOriginalData()
            .filterVideosBySearchQuery()
            .filterVideosByDuration()
            .filterVideosByLocation()
        val sortOrder = getCloudSortOrder()
        _state.update {
            it.copy(
                allVideos = videoList,
                sortOrder = sortOrder,
                progressBarShowing = false,
                scrollToTop = false
            )
        }
    }

    private fun List<VideoUIEntity>.filterVideosBySearchQuery() =
        filter { video ->
            video.name.contains(searchQuery, true)
        }

    private fun List<VideoUIEntity>.updateOriginalData() = also { data ->
        if (originalData.isNotEmpty()) {
            originalData.clear()
        }
        originalData.addAll(data)
    }

    private suspend fun getVideoUIEntityList() =
        getAllVideosUseCase().map { videoUIEntityMapper(it) }

    private suspend fun List<VideoUIEntity>.filterVideosByLocation(): List<VideoUIEntity> {
        val syncUploadsFolderIds = getSyncUploadsFolderIdsUseCase()
        return filter {
            when (_state.value.locationSelectedFilterOption) {
                LocationFilterOption.AllLocations -> true

                LocationFilterOption.CloudDrive ->
                    it.parentId.longValue !in syncUploadsFolderIds

                LocationFilterOption.CameraUploads ->
                    it.parentId.longValue in syncUploadsFolderIds

                LocationFilterOption.SharedItems -> it.isSharedItems
            }
        }
    }


    private fun List<VideoUIEntity>.filterVideosByDuration() =
        filter {
            val seconds = it.duration.inWholeSeconds
            val minutes = it.duration.inWholeMinutes
            when (_state.value.durationSelectedFilterOption) {
                DurationFilterOption.AllDurations -> true

                DurationFilterOption.LessThan10Seconds -> seconds < 10

                DurationFilterOption.Between10And60Seconds -> seconds in 10..60

                DurationFilterOption.Between1And4 -> minutes in 1..4

                DurationFilterOption.Between4And20 -> minutes in 4..20

                DurationFilterOption.MoreThan20 -> minutes > 20
            }
        }

    internal fun markHandledPendingRefresh() = _state.update { it.copy(isPendingRefresh = false) }

    internal fun onTabSelected(selectTab: VideoSectionTab) {
        if (selectTab == VideoSectionTab.Playlists && originalPlaylistData.isEmpty()) {
            loadVideoPlaylists()
        }
        if (_state.value.searchMode) {
            exitSearch()
        }
        if (_state.value.actionMode) {
            setActionMode(false)
        }
        _tabState.update {
            it.copy(selectedTab = selectTab)
        }
    }

    internal fun refreshWhenOrderChanged() =
        viewModelScope.launch {
            val sortOrder = getCloudSortOrder()
            _state.update {
                it.copy(
                    sortOrder = sortOrder,
                    progressBarShowing = true,
                    isPlaylistProgressBarShown = true
                )
            }
            setPendingRefreshNodes()
            loadVideoPlaylists()
        }

    internal fun shouldShowSearchMenu() =
        _state.value.currentVideoPlaylist == null && _state.value.allVideos.isNotEmpty()

    internal fun searchReady() {
        if (_state.value.searchMode)
            return

        _state.update { it.copy(searchMode = true) }
        searchQuery = ""
    }

    internal fun searchQuery(query: String) {
        if (searchQuery == query)
            return

        searchQuery = query

        if (_tabState.value.selectedTab == VideoSectionTab.All) {
            searchNodeByQueryString()
        } else {
            searchPlaylistByQueryString()
        }
    }

    private fun searchNodeByQueryString() {
        val videos = originalData.filter { video ->
            video.name.contains(searchQuery, true)
        }
        _state.update {
            it.copy(
                allVideos = videos,
                scrollToTop = true
            )
        }
    }

    private fun searchPlaylistByQueryString() {
        val playlists = originalPlaylistData.filter { playlist ->
            playlist.title.contains(searchQuery, true)
        }
        _state.update {
            it.copy(
                videoPlaylists = playlists,
                scrollToTop = true
            )
        }
    }

    internal fun exitSearch() {
        _state.update { it.copy(searchMode = false) }
        searchQuery = ""

        if (_tabState.value.selectedTab == VideoSectionTab.All) {
            refreshNodes()
        } else {
            loadVideoPlaylists()
        }
    }

    /**
     * Detect the node whether is local file
     *
     * @param handle node handle
     * @return true is local file, otherwise is false
     */
    internal suspend fun isLocalFile(
        handle: Long,
    ): String? =
        getNodeByHandle(handle)?.let { node ->
            val localPath = getLocalFile(node)
            File(getDownloadLocation(), node.name).let { file ->
                if (localPath != null && ((isFileAvailable(file) && file.length() == node.size)
                            || (node.fingerprint == getFingerprintUseCase(localPath)))
                ) {
                    localPath
                } else {
                    null
                }
            }
        }

    /**
     * Update intent
     *
     * @param handle node handle
     * @param type node type
     * @param intent Intent
     * @return updated intent
     */
    internal suspend fun updateIntent(
        handle: Long,
        type: String,
        intent: Intent,
    ): Intent {
        if (megaApiHttpServerIsRunningUseCase() == 0) {
            megaApiHttpServerStartUseCase()
            intent.putExtra(INTENT_EXTRA_KEY_NEED_STOP_HTTP_SERVER, true)
        }

        getFileUrlByNodeHandleUseCase(handle)?.let { url ->
            Uri.parse(url)?.let { uri ->
                intent.setDataAndType(uri, type)
            }
        }

        return intent
    }

    internal fun clearAllSelectedVideos() {
        val videos = clearVideosSelected()
        _state.update {
            it.copy(
                allVideos = videos,
                selectedVideoHandles = emptyList(),
                isInSelection = false
            )
        }
    }

    private fun clearVideosSelected() = _state.value.allVideos.map {
        it.copy(isSelected = false)
    }

    internal fun clearAllSelectedVideoPlaylists() {
        val playlists = clearVideoPlaylistsSelected()
        _state.update {
            it.copy(
                videoPlaylists = playlists,
                selectedVideoPlaylistHandles = emptyList(),
                isInSelection = false
            )
        }
    }

    internal fun clearAllSelectedVideosOfPlaylist() {
        _state.value.currentVideoPlaylist?.let { playlist ->
            val updatedVideos = clearVideosSelectedOfPlaylist(playlist) ?: return@let
            val updatedPlaylist = playlist.copy(videos = updatedVideos)
            _state.update {
                it.copy(
                    currentVideoPlaylist = updatedPlaylist,
                    selectedVideoElementIDs = emptyList(),
                    isInSelection = false
                )
            }
        }
    }

    private fun clearVideoPlaylistsSelected() = _state.value.videoPlaylists.map {
        it.copy(isSelected = false)
    }

    private fun clearVideosSelectedOfPlaylist(playlist: VideoPlaylistUIEntity) =
        playlist.videos?.map { it.copy(isSelected = false) }

    internal fun selectAllNodes() {
        val videos = _state.value.allVideos.map { item ->
            item.copy(isSelected = true)
        }
        val selectedHandles = _state.value.allVideos.map { item ->
            item.id.longValue
        }
        _state.update {
            it.copy(
                allVideos = videos,
                selectedVideoHandles = selectedHandles,
                isInSelection = true
            )
        }
    }

    internal fun selectAllVideoPlaylists() {
        val playlists = _state.value.videoPlaylists.map { item ->
            item.copy(isSelected = true)
        }
        val selectedHandles = _state.value.videoPlaylists.map { item ->
            item.id.longValue
        }
        _state.update {
            it.copy(
                videoPlaylists = playlists,
                selectedVideoPlaylistHandles = selectedHandles,
                isInSelection = true
            )
        }
    }

    internal fun selectAllVideosOfPlaylist() =
        _state.value.currentVideoPlaylist?.let { playlist ->
            if (playlist.videos == null) return@let

            val selectedHandles = playlist.videos.mapNotNull { it.elementID }

            if (playlist.videos.size == selectedHandles.size) {
                val videos = playlist.videos.map { item ->
                    item.copy(isSelected = true)
                }
                val updatedCurrentVideoPlaylist = playlist.copy(videos = videos)
                _state.update {
                    it.copy(
                        currentVideoPlaylist = updatedCurrentVideoPlaylist,
                        selectedVideoElementIDs = selectedHandles,
                        isInSelection = true
                    )
                }
            }

        }

    internal fun onItemClicked(item: VideoUIEntity, index: Int) =
        updateVideoItemInSelectionState(item = item, index = index)

    private fun updateVideoItemInSelectionState(item: VideoUIEntity, index: Int) {
        val isSelected = !item.isSelected
        val selectedHandles = updateSelectedHandles(
            videoID = item.id.longValue,
            isSelected = isSelected,
            selectedHandles = _state.value.selectedVideoHandles
        )
        val videos = _state.value.allVideos.updateItemSelectedState(index, isSelected)
        _state.update {
            it.copy(
                allVideos = videos,
                selectedVideoHandles = selectedHandles,
                isInSelection = selectedHandles.isNotEmpty()
            )
        }
    }

    private fun List<VideoUIEntity>.updateItemSelectedState(index: Int, isSelected: Boolean) =
        if (index in indices) {
            toMutableList().also { list ->
                list[index] = list[index].copy(isSelected = isSelected)
            }
        } else this


    private fun updateSelectedHandles(
        videoID: Long,
        isSelected: Boolean,
        selectedHandles: List<Long>,
    ) =
        selectedHandles.toMutableList().also { handles ->
            if (isSelected) {
                handles.add(videoID)
            } else {
                handles.remove(videoID)
            }
        }

    internal fun onVideoPlaylistItemClicked(item: VideoPlaylistUIEntity, index: Int) =
        updateVideoPlaylistItemInSelectionState(item = item, index = index)

    private fun updateVideoPlaylistItemInSelectionState(item: VideoPlaylistUIEntity, index: Int) {
        val isSelected = !item.isSelected
        val selectedHandles = updateSelectedHandles(
            videoID = item.id.longValue,
            isSelected = isSelected,
            selectedHandles = _state.value.selectedVideoPlaylistHandles
        )
        val updatedPlaylists =
            _state.value.videoPlaylists.updateVideoPlaylistItemSelectedState(index, isSelected)
        _state.update {
            it.copy(
                videoPlaylists = updatedPlaylists,
                selectedVideoPlaylistHandles = selectedHandles,
                isInSelection = selectedHandles.isNotEmpty()
            )
        }
    }

    private fun List<VideoPlaylistUIEntity>.updateVideoPlaylistItemSelectedState(
        index: Int,
        isSelected: Boolean,
    ) =
        if (index in indices) {
            toMutableList().also { list ->
                list[index] = list[index].copy(isSelected = isSelected)
            }
        } else this

    internal fun onVideoItemOfPlaylistClicked(item: VideoUIEntity, index: Int) =
        updateVideoItemOfPlaylistInSelectionState(item = item, index = index)

    private fun updateVideoItemOfPlaylistInSelectionState(item: VideoUIEntity, index: Int) =
        _state.value.currentVideoPlaylist?.let { playlist ->
            if (playlist.videos == null || item.elementID == null) return@let
            val isSelected = !item.isSelected
            val updatedVideos =
                playlist.videos.updateItemSelectedState(index, isSelected)
            val selectedHandles = updateSelectedHandles(
                videoID = item.elementID,
                isSelected = isSelected,
                selectedHandles = _state.value.selectedVideoElementIDs
            )
            val updatedCurrentPlaylist = playlist.copy(videos = updatedVideos)
            _state.update {
                it.copy(
                    currentVideoPlaylist = updatedCurrentPlaylist,
                    selectedVideoElementIDs = selectedHandles,
                    isInSelection = selectedHandles.isNotEmpty()
                )
            }
        }

    internal suspend fun getSelectedNodes(): List<TypedNode> =
        _state.value.selectedVideoHandles.mapNotNull {
            runCatching {
                getNodeByIdUseCase(NodeId(it))
            }.getOrNull()
        }

    internal suspend fun getSelectedMegaNode(): List<MegaNode> =
        _state.value.selectedVideoHandles.mapNotNull {
            runCatching {
                getNodeByHandle(it)
            }.getOrNull()
        }

    /**
     * Create new video playlist
     *
     * @param title video playlist title
     */
    internal fun createNewPlaylist(title: String) {
        if (createVideoPlaylistJob?.isActive == true) return
        title.ifEmpty {
            _state.value.createVideoPlaylistPlaceholderTitle
        }.trim()
            .takeIf { it.isNotEmpty() && checkVideoPlaylistTitleValidity(it) }
            ?.let { playlistTitle ->
                createVideoPlaylistJob = viewModelScope.launch {
                    setShouldCreateVideoPlaylist(false)
                    runCatching {
                        createVideoPlaylistUseCase(playlistTitle)
                    }.onSuccess { videoPlaylist ->
                        _state.update {
                            it.copy(
                                currentVideoPlaylist = videoPlaylistUIEntityMapper(
                                    videoPlaylist
                                ),
                                isVideoPlaylistCreatedSuccessfully = true
                            )
                        }
                        loadVideoPlaylists()
                        Timber.d("Current video playlist: ${videoPlaylist.title}")
                    }.onFailure { exception ->
                        Timber.e(exception)
                        _state.update {
                            it.copy(isVideoPlaylistCreatedSuccessfully = false)
                        }
                    }
                }
            }
    }

    internal fun removeVideoPlaylists(deletedList: List<VideoPlaylistUIEntity>) =
        viewModelScope.launch {
            runCatching {
                removeVideoPlaylistsUseCase(deletedList.map { it.id })
            }.onSuccess { deletedPlaylistIDs ->
                val deletedPlaylistTitles =
                    getDeletedVideoPlaylistTitles(
                        playlistIDs = deletedPlaylistIDs,
                        deletedPlaylist = deletedList
                    )
                Timber.d("removeVideoPlaylists deletedPlaylistTitles: $deletedPlaylistTitles")
                _state.update {
                    it.copy(
                        deletedVideoPlaylistTitles = deletedPlaylistTitles,
                        shouldDeleteVideoPlaylist = false,
                        shouldDeleteSingleVideoPlaylist = false,
                        areVideoPlaylistsRemovedSuccessfully = true
                    )
                }
                loadVideoPlaylists()
            }.onFailure { exception ->
                Timber.e(exception)
                _state.update {
                    it.copy(
                        shouldDeleteVideoPlaylist = false,
                        shouldDeleteSingleVideoPlaylist = false,
                        areVideoPlaylistsRemovedSuccessfully = false
                    )
                }
            }
        }

    private fun getDeletedVideoPlaylistTitles(
        playlistIDs: List<Long>,
        deletedPlaylist: List<VideoPlaylistUIEntity>,
    ): List<String> = playlistIDs.mapNotNull { id ->
        deletedPlaylist.firstOrNull { it.id.longValue == id }?.title
    }

    /**
     * Add videos to the playlist
     *
     * @param playlistID playlist id
     * @param videoIDs added video ids
     */
    internal fun addVideosToPlaylist(playlistID: NodeId, videoIDs: List<NodeId>) =
        viewModelScope.launch {
            runCatching {
                addVideosToPlaylistUseCase(playlistID, videoIDs)
            }.onSuccess { numberOfAddedVideos ->
                _state.update {
                    it.copy(
                        numberOfAddedVideos = numberOfAddedVideos
                    )
                }
                refreshVideoPlaylistsWithUpdateCurrentVideoPlaylist()
            }.onFailure { exception ->
                Timber.e(exception)
            }
        }

    private fun refreshVideoPlaylistsWithUpdateCurrentVideoPlaylist() =
        viewModelScope.launch {
            val videoPlaylists =
                getVideoPlaylists().updateOriginalPlaylistData().filterVideoPlaylistsBySearchQuery()
            val updatedCurrentVideoPlaylist = videoPlaylists.firstOrNull {
                it.id == _state.value.currentVideoPlaylist?.id
            }
            _state.update {
                it.copy(
                    videoPlaylists = videoPlaylists,
                    isPlaylistProgressBarShown = false,
                    scrollToTop = false,
                    currentVideoPlaylist = updatedCurrentVideoPlaylist
                )
            }
        }

    internal fun removeVideosFromPlaylist(playlistID: NodeId, videoElementIDs: List<Long>) =
        viewModelScope.launch {
            runCatching {
                removeVideosFromPlaylistUseCase(playlistID, videoElementIDs)
            }.onSuccess { numberOfRemovedItems ->
                _state.update {
                    it.copy(
                        numberOfRemovedItems = numberOfRemovedItems
                    )
                }
                refreshVideoPlaylistsWithUpdateCurrentVideoPlaylist()
            }.onFailure { exception ->
                Timber.e(exception)
            }
        }

    internal fun updateVideoPlaylistTitle(playlistID: NodeId, newTitle: String) =
        newTitle.trim()
            .takeIf { it.isNotEmpty() && checkVideoPlaylistTitleValidity(it) }
            ?.let { title ->
                viewModelScope.launch {
                    runCatching {
                        updateVideoPlaylistTitleUseCase(playlistID, title)
                    }.onSuccess { title ->
                        Timber.d("Updated video playlist title: $title")
                        _state.update {
                            it.copy(
                                shouldRenameVideoPlaylist = false
                            )
                        }
                        refreshVideoPlaylistsWithUpdatedTitle(title)
                    }.onFailure { exception ->
                        Timber.e(exception)
                        _state.update {
                            it.copy(
                                shouldRenameVideoPlaylist = false
                            )
                        }
                    }
                }
            }

    private fun refreshVideoPlaylistsWithUpdatedTitle(newTitle: String) =
        viewModelScope.launch {
            val videoPlaylists =
                getVideoPlaylists().updateOriginalPlaylistData().filterVideoPlaylistsBySearchQuery()
            val updatedCurrentVideoPlaylist =
                _state.value.currentVideoPlaylist?.copy(title = newTitle)
            _state.update {
                it.copy(
                    videoPlaylists = videoPlaylists,
                    isPlaylistProgressBarShown = false,
                    scrollToTop = false,
                    currentVideoPlaylist = updatedCurrentVideoPlaylist
                )
            }
        }

    internal fun setShouldRenameVideoPlaylist(value: Boolean) = _state.update {
        it.copy(shouldRenameVideoPlaylist = value)
    }

    internal fun updateCurrentVideoPlaylist(playlist: VideoPlaylistUIEntity?) {
        _state.update {
            it.copy(currentVideoPlaylist = playlist)
        }
    }

    internal fun setShouldCreateVideoPlaylist(value: Boolean) = _state.update {
        it.copy(shouldCreateVideoPlaylist = value)
    }

    internal fun setPlaceholderTitle(placeholderTitle: String) {
        val playlistTitles = getAllVideoPlaylistTitles()
        _state.update {
            it.copy(
                createVideoPlaylistPlaceholderTitle = getNextDefaultAlbumNameUseCase(
                    defaultName = placeholderTitle,
                    currentNames = playlistTitles
                )
            )
        }
    }

    private fun getAllVideoPlaylistTitles() = _state.value.videoPlaylists.map { it.title }

    internal fun setNewPlaylistTitleValidity(valid: Boolean) = _state.update {
        it.copy(isInputTitleValid = valid)
    }

    private fun checkVideoPlaylistTitleValidity(
        title: String,
    ): Boolean {
        var errorMessage: Int? = null
        var isTitleValid = true

        if (title.isBlank()) {
            isTitleValid = false
            errorMessage = R.string.invalid_string
        } else if (title in getAllVideoPlaylistTitles()) {
            isTitleValid = false
            errorMessage = ERROR_MESSAGE_REPEATED_TITLE
        } else if ("[\\\\*/:<>?\"|]".toRegex().containsMatchIn(title)) {
            isTitleValid = false
            errorMessage = R.string.invalid_characters_defined
        }

        _state.update {
            it.copy(
                isInputTitleValid = isTitleValid,
                createDialogErrorMessage = errorMessage
            )
        }

        return isTitleValid
    }

    internal fun setActionMode(value: Boolean) = _state.update { it.copy(actionMode = value) }

    internal fun setIsVideoPlaylistCreatedSuccessfully(value: Boolean) = _state.update {
        it.copy(isVideoPlaylistCreatedSuccessfully = value)
    }

    internal fun setShouldDeleteVideoPlaylist(value: Boolean) = _state.update {
        it.copy(shouldDeleteVideoPlaylist = value)
    }

    internal fun setShouldDeleteVideosFromPlaylist(value: Boolean) = _state.update {
        it.copy(shouldDeleteVideosFromPlaylist = value)
    }

    internal fun setShouldDeleteSingleVideoPlaylist(value: Boolean) = _state.update {
        it.copy(shouldDeleteSingleVideoPlaylist = value)
    }

    internal fun clearDeletedVideoPlaylistTitles() = _state.update {
        it.copy(deletedVideoPlaylistTitles = emptyList())
    }

    internal fun setShouldShowMoreVideoPlaylistOptions(value: Boolean) = _state.update {
        it.copy(shouldShowMoreVideoPlaylistOptions = value)
    }

    internal fun setAreVideoPlaylistsRemovedSuccessfully(value: Boolean) = _state.update {
        it.copy(areVideoPlaylistsRemovedSuccessfully = value)
    }

    internal fun setCurrentDestinationRoute(route: String?) = _state.update {
        it.copy(currentDestinationRoute = route)
    }

    internal fun setLocationSelectedFilterOption(locationFilterOption: LocationFilterOption) =
        _state.update {
            it.copy(
                locationSelectedFilterOption = locationFilterOption,
                progressBarShowing = true,
                isPendingRefresh = true
            )
        }

    internal fun setDurationSelectedFilterOption(durationFilterOption: DurationFilterOption) =
        _state.update {
            it.copy(
                durationSelectedFilterOption = durationFilterOption,
                progressBarShowing = true,
                isPendingRefresh = true
            )
        }

    internal fun setUpdateToolbarTitle(value: String?) = _state.update {
        it.copy(updateToolbarTitle = value)
    }

    internal fun clearNumberOfAddedVideos() = _state.update { it.copy(numberOfAddedVideos = 0) }

    internal fun clearNumberOfRemovedItems() = _state.update { it.copy(numberOfRemovedItems = 0) }

    internal fun hideOrUnhideNodes(nodeIds: List<NodeId>, hide: Boolean) = viewModelScope.launch {
        for (nodeId in nodeIds) {
            async {
                runCatching {
                    updateNodeSensitiveUseCase(nodeId = nodeId, isSensitive = hide)
                }.onFailure { Timber.e("Update sensitivity failed: $it") }
            }
        }
    }

    private fun monitorAccountDetail() {
        monitorAccountDetailUseCase()
            .onEach { accountDetail ->
                _state.update {
                    it.copy(accountDetail = accountDetail)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun monitorIsHiddenNodesOnboarded() {
        viewModelScope.launch {
            val isHiddenNodesOnboarded = isHiddenNodesOnboardedUseCase()
            _state.update {
                it.copy(isHiddenNodesOnboarded = isHiddenNodesOnboarded)
            }
        }
    }

    /**
     * Mark hidden nodes onboarding has shown
     */
    fun setHiddenNodesOnboarded() {
        _state.update {
            it.copy(isHiddenNodesOnboarded = true)
        }
    }

    companion object {
        private const val ERROR_MESSAGE_REPEATED_TITLE = 0
    }
}
