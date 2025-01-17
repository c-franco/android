package mega.privacy.android.feature.sync.presentation

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mega.privacy.android.core.test.extension.CoroutineMainDispatcherExtension
import mega.privacy.android.domain.usecase.account.IsStorageOverQuotaUseCase
import mega.privacy.android.domain.usecase.file.GetExternalPathByContentUriUseCase
import mega.privacy.android.feature.sync.domain.entity.RemoteFolder
import mega.privacy.android.feature.sync.domain.usecase.sync.SyncFolderPairUseCase
import mega.privacy.android.feature.sync.domain.usecase.sync.option.MonitorSelectedMegaFolderUseCase
import mega.privacy.android.feature.sync.ui.newfolderpair.SyncNewFolderAction
import mega.privacy.android.feature.sync.ui.newfolderpair.SyncNewFolderState
import mega.privacy.android.feature.sync.ui.newfolderpair.SyncNewFolderViewModel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(CoroutineMainDispatcherExtension::class)
@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SyncNewFolderViewModelTest {

    private val getExternalPathByContentUriUseCase: GetExternalPathByContentUriUseCase = mock()
    private val monitorSelectedMegaFolderUseCase: MonitorSelectedMegaFolderUseCase = mock()
    private val syncFolderPairUseCase: SyncFolderPairUseCase = mock()
    private val isStorageOverQuotaUseCase: IsStorageOverQuotaUseCase = mock()
    private lateinit var underTest: SyncNewFolderViewModel

    @AfterEach
    fun resetAndTearDown() {
        reset(
            getExternalPathByContentUriUseCase,
            monitorSelectedMegaFolderUseCase,
            syncFolderPairUseCase,
            isStorageOverQuotaUseCase,
        )
    }

    @Test
    fun `test that local folder selected action results in updated state`() = runTest {
        whenever(monitorSelectedMegaFolderUseCase()).thenReturn(flowOf(mock()))
        initViewModel()
        val localFolderContentUri =
            "content://com.android.externalstorage.documents/tree/primary%3ASync%2FsomeFolder"
        val localFolderUri: Uri = mock()
        val localFolderFolderStoragePath = "/storage/emulated/0/Sync/someFolder"
        val expectedState = SyncNewFolderState(selectedLocalFolder = localFolderFolderStoragePath)
        whenever(getExternalPathByContentUriUseCase.invoke(localFolderContentUri)).thenReturn(
            localFolderFolderStoragePath
        )
        whenever(localFolderUri.toString()).thenReturn(localFolderContentUri)

        underTest.handleAction(SyncNewFolderAction.LocalFolderSelected(localFolderUri))

        assertThat(expectedState.selectedLocalFolder).isEqualTo(underTest.state.value.selectedLocalFolder)
    }

    @Test
    fun `test that when mega folder is updated state is also updated`() = runTest {
        val remoteFolder = RemoteFolder(123L, "someFolder")
        whenever(monitorSelectedMegaFolderUseCase()).thenReturn(flow {
            emit(remoteFolder)
            awaitCancellation()
        })
        whenever(monitorSelectedMegaFolderUseCase()).thenReturn(flow {
            emit(remoteFolder)
            awaitCancellation()
        })
        initViewModel()
        val expectedState = SyncNewFolderState(selectedMegaFolder = remoteFolder)

        assertThat(expectedState).isEqualTo(underTest.state.value)
    }

    @Test
    fun `test that next click creates new folder pair and navigates to next screen`() = runTest {
        val remoteFolder = RemoteFolder(123L, "someFolder")
        whenever(isStorageOverQuotaUseCase()).thenReturn(false)
        whenever(monitorSelectedMegaFolderUseCase()).thenReturn(flow {
            emit(remoteFolder)
            awaitCancellation()
        })
        whenever(
            syncFolderPairUseCase.invoke(
                name = remoteFolder.name,
                localPath = "",
                remotePath = remoteFolder
            )
        ).thenReturn(true)
        val state = SyncNewFolderState(
            selectedMegaFolder = remoteFolder
        )
        initViewModel()

        underTest.handleAction(SyncNewFolderAction.NextClicked)

        verify(syncFolderPairUseCase).invoke(
            name = remoteFolder.name,
            localPath = state.selectedLocalFolder,
            remotePath = remoteFolder
        )
        assertThat(underTest.state.value.openSyncListScreen).isEqualTo(triggered)
    }

    @Test
    fun `test that next click shows error when storage is over quota`() = runTest {
        whenever(monitorSelectedMegaFolderUseCase()).thenReturn(flow {
            awaitCancellation()
        })
        whenever(isStorageOverQuotaUseCase()).thenReturn(true)
        initViewModel()

        underTest.handleAction(SyncNewFolderAction.NextClicked)

        assertThat(underTest.state.value.showStorageOverQuota).isEqualTo(true)
    }

    @Test
    fun `test that storage over quota shown resets showStorageOverQuota event`() {
        whenever(monitorSelectedMegaFolderUseCase()).thenReturn(flow {
            awaitCancellation()
        })
        initViewModel()

        underTest.handleAction(SyncNewFolderAction.StorageOverquotaShown)

        assertThat(underTest.state.value.showStorageOverQuota).isEqualTo(false)
    }

    @Test
    fun `test that sync list screen opened resets openSyncListScreen event`() {
        whenever(monitorSelectedMegaFolderUseCase()).thenReturn(flow {
            awaitCancellation()
        })
        initViewModel()

        underTest.handleAction(SyncNewFolderAction.SyncListScreenOpened)

        assertThat(underTest.state.value.openSyncListScreen).isEqualTo(consumed)
    }

    private fun initViewModel() {
        underTest = SyncNewFolderViewModel(
            getExternalPathByContentUriUseCase,
            monitorSelectedMegaFolderUseCase,
            syncFolderPairUseCase,
            isStorageOverQuotaUseCase,
        )
    }
}
