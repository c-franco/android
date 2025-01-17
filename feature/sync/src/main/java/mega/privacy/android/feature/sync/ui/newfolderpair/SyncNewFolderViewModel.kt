package mega.privacy.android.feature.sync.ui.newfolderpair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mega.privacy.android.domain.usecase.account.IsStorageOverQuotaUseCase
import mega.privacy.android.domain.usecase.file.GetExternalPathByContentUriUseCase
import mega.privacy.android.feature.sync.domain.usecase.sync.SyncFolderPairUseCase
import mega.privacy.android.feature.sync.domain.usecase.sync.option.MonitorSelectedMegaFolderUseCase
import javax.inject.Inject

@HiltViewModel
internal class SyncNewFolderViewModel @Inject constructor(
    private val getExternalPathByContentUriUseCase: GetExternalPathByContentUriUseCase,
    private val monitorSelectedMegaFolderUseCase: MonitorSelectedMegaFolderUseCase,
    private val syncFolderPairUseCase: SyncFolderPairUseCase,
    private val isStorageOverQuotaUseCase: IsStorageOverQuotaUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SyncNewFolderState())
    val state: StateFlow<SyncNewFolderState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            monitorSelectedMegaFolderUseCase().collectLatest { folder ->
                _state.update { state ->
                    state.copy(selectedMegaFolder = folder)
                }
            }
        }
    }

    fun handleAction(action: SyncNewFolderAction) {
        when (action) {
            is SyncNewFolderAction.LocalFolderSelected -> {
                viewModelScope.launch {
                    getExternalPathByContentUriUseCase(action.path.toString())?.let { path ->
                        _state.update { state ->
                            state.copy(selectedLocalFolder = path)
                        }
                    }
                }
            }

            is SyncNewFolderAction.NextClicked -> {
                viewModelScope.launch {
                    when {
                        isStorageOverQuotaUseCase() -> {
                            _state.update { state ->
                                state.copy(showStorageOverQuota = true)
                            }
                        }

                        else -> {
                            state.value.selectedMegaFolder?.let { remoteFolder ->
                                syncFolderPairUseCase(
                                    name = remoteFolder.name,
                                    localPath = state.value.selectedLocalFolder,
                                    remotePath = remoteFolder,
                                )
                            }
                            _state.update { state ->
                                state.copy(openSyncListScreen = triggered)
                            }
                        }
                    }
                }
            }

            is SyncNewFolderAction.StorageOverquotaShown -> {
                _state.update { state ->
                    state.copy(showStorageOverQuota = false)
                }
            }

            SyncNewFolderAction.SyncListScreenOpened -> {
                _state.update { state ->
                    state.copy(openSyncListScreen = consumed)
                }
            }
        }
    }
}