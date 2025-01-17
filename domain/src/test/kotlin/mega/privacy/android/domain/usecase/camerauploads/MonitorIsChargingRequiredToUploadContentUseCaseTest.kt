package mega.privacy.android.domain.usecase.camerauploads

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mega.privacy.android.domain.repository.CameraUploadRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

/**
 * Test class for [MonitorIsChargingRequiredToUploadContentUseCase]
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MonitorIsChargingRequiredToUploadContentUseCaseTest {

    private lateinit var underTest: MonitorIsChargingRequiredToUploadContentUseCase

    private val cameraUploadRepository = mock<CameraUploadRepository>()

    @BeforeAll
    fun setUp() {
        underTest = MonitorIsChargingRequiredToUploadContentUseCase(cameraUploadRepository)
    }

    @BeforeEach
    fun resetMocks() {
        reset(cameraUploadRepository)
    }

    @Test
    fun `test that the charging state is returned`() = runTest {
        val chargingRequired = true
        whenever(cameraUploadRepository.monitorIsChargingRequiredToUploadContent()).thenReturn(
            flowOf(chargingRequired)
        )

        underTest().test {
            assertThat(awaitItem()).isEqualTo(chargingRequired)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that the charging state defaults to false if it cannot be retrieved`() = runTest {
        whenever(cameraUploadRepository.monitorIsChargingRequiredToUploadContent()).thenReturn(
            flowOf(null)
        )

        underTest().test {
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }
}