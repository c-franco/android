@file:OptIn(ExperimentalCoroutinesApi::class)

package mega.privacy.android.app.presentation.photos.mediadiscovery

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import de.palm.composestateevents.StateEventWithContentConsumed
import de.palm.composestateevents.StateEventWithContentTriggered
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import mega.privacy.android.app.domain.usecase.AuthorizeNode
import mega.privacy.android.app.domain.usecase.GetNodeByHandle
import mega.privacy.android.app.domain.usecase.GetNodeListByIds
import mega.privacy.android.app.domain.usecase.GetPublicNodeListByIds
import mega.privacy.android.app.featuretoggle.AppFeatures
import mega.privacy.android.app.namecollision.usecase.CheckNameCollisionUseCase
import mega.privacy.android.app.presentation.copynode.mapper.CopyRequestMessageMapper
import mega.privacy.android.app.presentation.transfers.starttransfer.model.TransferTriggerEvent
import mega.privacy.android.core.test.extension.CoroutineMainDispatcherExtension
import mega.privacy.android.domain.entity.AccountType
import mega.privacy.android.domain.entity.SortOrder
import mega.privacy.android.domain.entity.account.AccountDetail
import mega.privacy.android.domain.entity.account.AccountLevelDetail
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.entity.node.TypedFileNode
import mega.privacy.android.domain.entity.photos.Photo
import mega.privacy.android.domain.usecase.GetCameraSortOrder
import mega.privacy.android.domain.usecase.GetFileUrlByNodeHandleUseCase
import mega.privacy.android.domain.usecase.GetLocalFolderLinkFromMegaApiUseCase
import mega.privacy.android.domain.usecase.GetNodeByIdUseCase
import mega.privacy.android.domain.usecase.HasCredentialsUseCase
import mega.privacy.android.domain.usecase.IsHiddenNodesOnboardedUseCase
import mega.privacy.android.domain.usecase.MonitorMediaDiscoveryView
import mega.privacy.android.domain.usecase.SetCameraSortOrder
import mega.privacy.android.domain.usecase.SetMediaDiscoveryView
import mega.privacy.android.domain.usecase.UpdateNodeSensitiveUseCase
import mega.privacy.android.domain.usecase.account.MonitorAccountDetailUseCase
import mega.privacy.android.domain.usecase.featureflag.GetFeatureFlagValueUseCase
import mega.privacy.android.domain.usecase.file.GetFingerprintUseCase
import mega.privacy.android.domain.usecase.folderlink.GetPublicChildNodeFromIdUseCase
import mega.privacy.android.domain.usecase.mediaplayer.MegaApiHttpServerIsRunningUseCase
import mega.privacy.android.domain.usecase.mediaplayer.MegaApiHttpServerStartUseCase
import mega.privacy.android.domain.usecase.network.MonitorConnectivityUseCase
import mega.privacy.android.domain.usecase.node.CopyNodesUseCase
import mega.privacy.android.domain.usecase.node.IsNodeInRubbishBinUseCase
import mega.privacy.android.domain.usecase.photos.GetPhotosByFolderIdInFolderLinkUseCase
import mega.privacy.android.domain.usecase.photos.GetPhotosByFolderIdUseCase
import mega.privacy.android.domain.usecase.setting.MonitorShowHiddenItemsUseCase
import mega.privacy.android.domain.usecase.setting.MonitorSubFolderMediaDiscoverySettingsUseCase
import mega.privacy.android.domain.usecase.viewtype.SetViewType
import nz.mega.sdk.MegaNode
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@ExtendWith(CoroutineMainDispatcherExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MediaDiscoveryViewModelTest {
    private lateinit var underTest: MediaDiscoveryViewModel

    private val getNodeListByIds = mock<GetNodeListByIds>()
    private val savedStateHandle = mock<SavedStateHandle>()
    private val getPhotosByFolderIdUseCase = mock<GetPhotosByFolderIdUseCase>()
    private val getPhotosByFolderIdInFolderLinkUseCase =
        mock<GetPhotosByFolderIdInFolderLinkUseCase>()
    private val getCameraSortOrder = mock<GetCameraSortOrder>()
    private val setCameraSortOrder = mock<SetCameraSortOrder>()
    private val monitorMediaDiscoveryView = mock<MonitorMediaDiscoveryView>()
    private val setMediaDiscoveryView = mock<SetMediaDiscoveryView>()
    private val getNodeByHandle = mock<GetNodeByHandle>()
    private val getFingerprintUseCase = mock<GetFingerprintUseCase>()
    private val megaApiHttpServerIsRunningUseCase = mock<MegaApiHttpServerIsRunningUseCase>()
    private val megaApiHttpServerStartUseCase = mock<MegaApiHttpServerStartUseCase>()
    private val getFileUrlByNodeHandleUseCase = mock<GetFileUrlByNodeHandleUseCase>()
    private val getLocalFolderLinkFromMegaApiUseCase = mock<GetLocalFolderLinkFromMegaApiUseCase>()
    private val monitorConnectivityUseCase = mock<MonitorConnectivityUseCase>()
    private val checkNameCollisionUseCase = mock<CheckNameCollisionUseCase>()
    private val authorizeNode = mock<AuthorizeNode>()
    private val copyNodesUseCase = mock<CopyNodesUseCase>()
    private val copyRequestMessageMapper = mock<CopyRequestMessageMapper>()
    private val hasCredentialsUseCase = mock<HasCredentialsUseCase>()
    private val getPublicNodeListByIds = mock<GetPublicNodeListByIds>()
    private val setViewType = mock<SetViewType>()
    private val monitorSubFolderMediaDiscoverySettingsUseCase =
        mock<MonitorSubFolderMediaDiscoverySettingsUseCase>()
    private val getFeatureFlagValueUseCase = mock<GetFeatureFlagValueUseCase>()
    private val isNodeInRubbishBinUseCase = mock<IsNodeInRubbishBinUseCase>()
    private val getNodeByIdUseCase = mock<GetNodeByIdUseCase>()
    private val getPublicChildNodeFromIdUseCase = mock<GetPublicChildNodeFromIdUseCase>()
    private val updateNodeSensitiveUseCase = mock<UpdateNodeSensitiveUseCase>()
    private val monitorShowHiddenItemsUseCase = mock<MonitorShowHiddenItemsUseCase>()
    private val monitorAccountDetailUseCase = mock<MonitorAccountDetailUseCase> {
        on {
            invoke()
        }.thenReturn(flowOf(AccountDetail()))
    }
    private val isHiddenNodesOnboardedUseCase = mock<IsHiddenNodesOnboardedUseCase> {
        on {
            runBlocking { invoke() }
        }.thenReturn(false)
    }

    @BeforeAll
    fun setup() {
        commonStub()
        initViewModel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initViewModel() {
        underTest = MediaDiscoveryViewModel(
            getNodeListByIds = getNodeListByIds,
            savedStateHandle = savedStateHandle,
            getPhotosByFolderIdUseCase = getPhotosByFolderIdUseCase,
            getPhotosByFolderIdInFolderLinkUseCase = getPhotosByFolderIdInFolderLinkUseCase,
            getCameraSortOrder = getCameraSortOrder,
            setCameraSortOrder = setCameraSortOrder,
            monitorMediaDiscoveryView = monitorMediaDiscoveryView,
            setMediaDiscoveryView = setMediaDiscoveryView,
            getNodeByHandle = getNodeByHandle,
            getFingerprintUseCase = getFingerprintUseCase,
            megaApiHttpServerIsRunningUseCase = megaApiHttpServerIsRunningUseCase,
            megaApiHttpServerStartUseCase = megaApiHttpServerStartUseCase,
            getFileUrlByNodeHandleUseCase = getFileUrlByNodeHandleUseCase,
            getLocalFolderLinkFromMegaApiUseCase = getLocalFolderLinkFromMegaApiUseCase,
            monitorConnectivityUseCase = monitorConnectivityUseCase,
            checkNameCollisionUseCase = checkNameCollisionUseCase,
            authorizeNode = authorizeNode,
            copyNodesUseCase = copyNodesUseCase,
            copyRequestMessageMapper = copyRequestMessageMapper,
            hasCredentialsUseCase = hasCredentialsUseCase,
            getPublicNodeListByIds = getPublicNodeListByIds,
            setViewType = setViewType,
            monitorSubFolderMediaDiscoverySettingsUseCase = monitorSubFolderMediaDiscoverySettingsUseCase,
            isNodeInRubbishBinUseCase = isNodeInRubbishBinUseCase,
            getNodeByIdUseCase = getNodeByIdUseCase,
            getPublicChildNodeFromIdUseCase = getPublicChildNodeFromIdUseCase,
            updateNodeSensitiveUseCase = updateNodeSensitiveUseCase,
            monitorAccountDetailUseCase = monitorAccountDetailUseCase,
            monitorShowHiddenItemsUseCase = monitorShowHiddenItemsUseCase,
            getFeatureFlagValueUseCase = getFeatureFlagValueUseCase,
            defaultDispatcher = UnconfinedTestDispatcher(),
            isHiddenNodesOnboardedUseCase = isHiddenNodesOnboardedUseCase,
        )
    }

    private fun commonStub() = runTest {
        whenever(getCameraSortOrder()).thenReturn(SortOrder.ORDER_DEFAULT_ASC)
        whenever(monitorConnectivityUseCase()).thenReturn(emptyFlow())
        whenever(monitorMediaDiscoveryView()).thenReturn(emptyFlow())
        whenever(monitorShowHiddenItemsUseCase()).thenReturn(emptyFlow())
        whenever(monitorAccountDetailUseCase()).thenReturn(emptyFlow())
        whenever(savedStateHandle.get<Long>(any())) doReturn (null)
        whenever(savedStateHandle.get<Boolean>(any())) doReturn (null)
        whenever(getFeatureFlagValueUseCase(any())).thenReturn(true)
    }

    @BeforeEach
    fun resetMocks() = reset(
        getNodeListByIds,
        savedStateHandle,
        getPhotosByFolderIdUseCase,
        getPhotosByFolderIdInFolderLinkUseCase,
        getCameraSortOrder,
        setCameraSortOrder,
        monitorMediaDiscoveryView,
        setMediaDiscoveryView,
        getNodeByHandle,
        getFingerprintUseCase,
        megaApiHttpServerIsRunningUseCase,
        megaApiHttpServerStartUseCase,
        getFileUrlByNodeHandleUseCase,
        getLocalFolderLinkFromMegaApiUseCase,
        monitorConnectivityUseCase,
        checkNameCollisionUseCase,
        authorizeNode,
        copyNodesUseCase,
        copyRequestMessageMapper,
        hasCredentialsUseCase,
        getPublicNodeListByIds,
        setViewType,
        monitorSubFolderMediaDiscoverySettingsUseCase,
        getFeatureFlagValueUseCase,
        isNodeInRubbishBinUseCase,
        getNodeByIdUseCase,
        getPublicChildNodeFromIdUseCase,
        monitorAccountDetailUseCase,
        monitorShowHiddenItemsUseCase,
    )

    @Test
    fun `test that start download node event is launched with correct value when on save to device is invoked with download worker feature flag is enabled`() =
        runTest {
            val node = stubSelectedNode()
            whenever(getFeatureFlagValueUseCase(AppFeatures.DownloadWorker)).thenReturn(true)

            underTest.onSaveToDeviceClicked(mock())
            assertThat(underTest.state.value.downloadEvent)
                .isInstanceOf(StateEventWithContentTriggered::class.java)
            val downloadEvent =
                underTest.state.value.downloadEvent as StateEventWithContentTriggered
            assertThat(downloadEvent.content)
                .isInstanceOf(TransferTriggerEvent.StartDownloadNode::class.java)
            val startEvent = downloadEvent.content as TransferTriggerEvent.StartDownloadNode
            assertThat((startEvent.nodes)).containsExactly(node)
        }

    private suspend fun stubSelectedNode(): TypedFileNode {
        val node = mock<TypedFileNode> {
            on { id } doReturn NodeId(1L)
        }
        val megaNode = mock<MegaNode> {
            on { handle } doReturn 1L
        }
        whenever(getNodeListByIds(any())).thenReturn(listOf(megaNode))
        whenever(getNodeByIdUseCase(NodeId(any()))).thenReturn(node)
        underTest.togglePhotoSelection(1L)
        return node
    }

    @Test
    fun `test that legacy lambda is launched when on save to device is invoked with download worker feature flag is disabled`() =
        runTest {
            whenever(getFeatureFlagValueUseCase(AppFeatures.DownloadWorker)).thenReturn(false)
            val legacySaveToDevice = mock<() -> Unit>()
            underTest.onSaveToDeviceClicked(legacySaveToDevice)
            verify(legacySaveToDevice)()
        }

    @Test
    fun `test that download event initial value is consumed `() =
        runTest {
            assertThat(underTest.state.value.downloadEvent)
                .isInstanceOf(StateEventWithContentConsumed::class.java)
        }

    @Test
    fun `test that download event is consumed when on consume download event is invoked`() =
        runTest {
            stubSelectedNode()
            whenever(getFeatureFlagValueUseCase(AppFeatures.DownloadWorker)).thenReturn(true)

            underTest.onSaveToDeviceClicked(mock())

            assertThat(underTest.state.value.downloadEvent)
                .isInstanceOf(StateEventWithContentTriggered::class.java)

            underTest.consumeDownloadEvent()

            assertThat(underTest.state.value.downloadEvent)
                .isInstanceOf(StateEventWithContentConsumed::class.java)
        }

    @Test
    fun `test that turn on flag HiddenNodes and it is not from folder link then the flow is correct`() =
        runTest {
            commonStub()
            whenever(getFeatureFlagValueUseCase(AppFeatures.HiddenNodes)) doReturn true
            whenever(savedStateHandle.get<Boolean>(MediaDiscoveryActivity.INTENT_KEY_FROM_FOLDER_LINK)) doReturn false
            initViewModel()
            verify(monitorShowHiddenItemsUseCase, times(1)).invoke()
            verify(monitorAccountDetailUseCase, times(1)).invoke()
        }

    @Test
    fun `test that turn off flag HiddenNodes and it is not from folder link then the flow is correct`() =
        runTest {
            commonStub()
            whenever(getFeatureFlagValueUseCase(AppFeatures.HiddenNodes)) doReturn false
            whenever(savedStateHandle.get<Boolean>(MediaDiscoveryActivity.INTENT_KEY_FROM_FOLDER_LINK)) doReturn false
            initViewModel()
            verifyNoInteractions(monitorShowHiddenItemsUseCase)
            verifyNoInteractions(monitorAccountDetailUseCase)
        }

    @Test
    fun `test that turn on flag HiddenNodes and it is from folder link then the flow is correct`() =
        runTest {
            commonStub()
            whenever(getFeatureFlagValueUseCase(AppFeatures.HiddenNodes)) doReturn true
            whenever(savedStateHandle.get<Boolean>(MediaDiscoveryActivity.INTENT_KEY_FROM_FOLDER_LINK)) doReturn true
            initViewModel()
            verifyNoInteractions(monitorShowHiddenItemsUseCase)
            verifyNoInteractions(monitorAccountDetailUseCase)
        }

    @Test
    fun `test that turn off flag HiddenNodes and it is from folder link then the flow is correct`() =
        runTest {
            commonStub()
            whenever(getFeatureFlagValueUseCase(AppFeatures.HiddenNodes)) doReturn false
            whenever(savedStateHandle.get<Boolean>(MediaDiscoveryActivity.INTENT_KEY_FROM_FOLDER_LINK)) doReturn true
            initViewModel()
            verifyNoInteractions(monitorShowHiddenItemsUseCase)
            verifyNoInteractions(monitorAccountDetailUseCase)
        }

    @Test
    fun `test that filterNonSensitivePhotos return photos when showHiddenItems is null and isPaid`() =
        runTest {
            commonStub()
            initViewModel()
            val photos = listOf(
                createNonSensitivePhoto(),
                createSensitivePhoto(),
            )
            val expectedPhotos = underTest.filterNonSensitivePhotos(photos, true)
            assertThat(expectedPhotos).isEqualTo(photos)
        }

    @Test
    fun `test that filterNonSensitivePhotos return photos when showHiddenItems is true and isPaid`() =
        runTest {
            commonStub()
            whenever(monitorShowHiddenItemsUseCase()).thenReturn(flowOf(true))
            initViewModel()
            val photos = listOf(
                createNonSensitivePhoto(),
                createSensitivePhoto(),
            )
            val expectedPhotos = underTest.filterNonSensitivePhotos(photos, true)
            assertThat(expectedPhotos).isEqualTo(photos)
        }


    @Test
    fun `test that filterNonSensitivePhotos return photos when showHiddenItems is true and isNotPaid`() =
        runTest {
            commonStub()
            whenever(monitorShowHiddenItemsUseCase()).thenReturn(flowOf(true))
            initViewModel()
            val photos = listOf(
                createNonSensitivePhoto(),
                createSensitivePhoto(),
            )
            val expectedPhotos = underTest.filterNonSensitivePhotos(photos, false)
            assertThat(expectedPhotos).isEqualTo(photos)
        }

    @Test
    fun `test that filterNonSensitivePhotos return photos when showHiddenItems is true and isPaid is null`() =
        runTest {
            commonStub()
            whenever(monitorShowHiddenItemsUseCase()).thenReturn(flowOf(true))
            initViewModel()
            val photos = listOf(
                createNonSensitivePhoto(),
                createSensitivePhoto(),
            )
            val expectedPhotos = underTest.filterNonSensitivePhotos(photos, null)
            assertThat(expectedPhotos).isEqualTo(photos)
        }

    @Test
    fun `test that filterNonSensitivePhotos return non-sensitive photos when showHiddenItems is false and isPaid`() =
        runTest {
            commonStub()
            whenever(monitorShowHiddenItemsUseCase()).thenReturn(flowOf(false))
            initViewModel()
            val nonSensitivePhoto = createNonSensitivePhoto()
            val photos = listOf(
                nonSensitivePhoto,
                createSensitivePhoto(),
            )
            val expectedPhotos = underTest.filterNonSensitivePhotos(photos, true)
            assertThat(expectedPhotos.size).isEqualTo(1)
            assertThat(expectedPhotos).isEqualTo(listOf(nonSensitivePhoto))
        }

    @Test
    fun `test that monitorShowHiddenItems works correctly`() =
        runTest {
            whenever(monitorShowHiddenItemsUseCase()).thenReturn(flowOf(true))
            underTest.monitorShowHiddenItems(
                loadPhotosDone = true,
                listOf(
                    createNonSensitivePhoto(),
                )
            )
            verify(monitorShowHiddenItemsUseCase, times(1)).invoke()
            assertThat(underTest.showHiddenItems).isTrue()
        }

    @Test
    fun `test that monitorAccountDetail works correctly`() =
        runTest {
            val accountLevelDetail = mock<AccountLevelDetail> {
                on { accountType } doReturn AccountType.FREE
            }
            val accountDetail = mock<AccountDetail> {
                on { levelDetail } doReturn accountLevelDetail
            }
            whenever(monitorAccountDetailUseCase()).thenReturn(
                flowOf(accountDetail)
            )
            underTest.monitorAccountDetail(
                loadPhotosDone = true,
                listOf(
                    createNonSensitivePhoto(),
                )
            )
            verify(monitorAccountDetailUseCase, times(1)).invoke()
            underTest.state.test {
                assertThat(awaitItem().accountType).isEqualTo(AccountType.FREE)
            }
        }

    @Test
    fun `test that handleFolderPhotosAndLogic should update state to go back when sourcePhotos is empty and MD folder is in rubbish`() =
        runTest {
            val photos = listOf<Photo.Image>()
            commonStub()
            whenever(savedStateHandle.get<Long>(MediaDiscoveryFragment.INTENT_KEY_CURRENT_FOLDER_ID)) doReturn 1L
            whenever(isNodeInRubbishBinUseCase(NodeId(1L))) doReturn true
            initViewModel()
            underTest.handleFolderPhotosAndLogic(photos)

            underTest.state.test {
                assertThat(awaitItem().shouldGoBack).isTrue()
            }
        }

    @Test
    fun `test that handleFolderPhotosAndLogic should not update state to go back when sourcePhotos is empty and MD folder is not in rubbish`() =
        runTest {
            val photos = listOf<Photo.Image>()
            commonStub()
            whenever(savedStateHandle.get<Long>(MediaDiscoveryFragment.INTENT_KEY_CURRENT_FOLDER_ID)) doReturn 1L
            whenever(isNodeInRubbishBinUseCase(NodeId(1L))) doReturn false
            initViewModel()
            underTest.handleFolderPhotosAndLogic(photos)

            underTest.state.test {
                assertThat(awaitItem().shouldGoBack).isFalse()
            }
        }

    @Test
    fun `test that handleFolderPhotosAndLogic should not update state to go back when sourcePhotos is not empty and MD folder is in rubbish`() =
        runTest {
            val photos = listOf(createNonSensitivePhoto())
            commonStub()
            whenever(savedStateHandle.get<Long>(MediaDiscoveryFragment.INTENT_KEY_CURRENT_FOLDER_ID)) doReturn 1L
            whenever(isNodeInRubbishBinUseCase(NodeId(1L))) doReturn true
            initViewModel()
            underTest.handleFolderPhotosAndLogic(photos)

            underTest.state.test {
                assertThat(awaitItem().shouldGoBack).isFalse()
            }
        }

    @Test
    fun `test that handleFolderPhotosAndLogic should not update state to go back when sourcePhotos is not empty and MD folder is not in rubbish`() =
        runTest {
            val photos = listOf(createNonSensitivePhoto())
            commonStub()
            whenever(savedStateHandle.get<Long>(MediaDiscoveryFragment.INTENT_KEY_CURRENT_FOLDER_ID)) doReturn 1L
            whenever(isNodeInRubbishBinUseCase(NodeId(1L))) doReturn false
            initViewModel()
            underTest.handleFolderPhotosAndLogic(photos)

            underTest.state.test {
                assertThat(awaitItem().shouldGoBack).isFalse()
            }
        }

    private fun createNonSensitivePhoto(): Photo.Image {
        return mock<Photo.Image> {
            on { isSensitive } doReturn false
            on { isSensitiveInherited } doReturn false
            on { modificationTime } doReturn LocalDateTime.now()
        }
    }

    private fun createSensitivePhoto(
        isSensitive: Boolean = true,
        isSensitiveInherited: Boolean = true,
    ): Photo.Image {
        return mock<Photo.Image> {
            on { this.isSensitive } doReturn isSensitive
            on { this.isSensitiveInherited } doReturn isSensitiveInherited
        }
    }
}