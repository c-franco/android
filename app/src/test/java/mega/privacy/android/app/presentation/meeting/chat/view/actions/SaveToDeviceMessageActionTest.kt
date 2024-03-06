package mega.privacy.android.app.presentation.meeting.chat.view.actions

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import mega.privacy.android.app.presentation.meeting.chat.model.ChatViewModel
import mega.privacy.android.domain.entity.chat.messages.NodeAttachmentMessage
import mega.privacy.android.domain.entity.chat.messages.invalid.InvalidMessage
import mega.privacy.android.domain.entity.chat.messages.normal.TextMessage
import mega.privacy.android.domain.entity.node.chat.ChatImageFile
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
internal class SaveToDeviceMessageActionTest {
    private lateinit var underTest: SaveToDeviceMessageAction
    private val chatViewModel: ChatViewModel = mock()

    @get:Rule
    var composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        underTest = SaveToDeviceMessageAction(chatViewModel)
    }

    @Test
    fun `test that action applies to node attachment messages`() {
        Truth.assertThat(underTest.appliesTo(setOf(mock<NodeAttachmentMessage>()))).isTrue()
    }

    @Test
    fun `test that action does not apply to non node attachment messages`() {
        Truth.assertThat(underTest.appliesTo(setOf(mock<TextMessage>()))).isFalse()
    }

    @Test
    fun `test that action does not apply to invalid messages`() {
        Truth.assertThat(underTest.appliesTo(setOf(mock<InvalidMessage>()))).isFalse()
    }

    @Test
    fun `test that save to device option shows correctly`() {
        val hideBottomSheet = mock<() -> Unit>()
        composeRule.setContent(
            underTest.bottomSheetMenuItem(
                setOf(mock<NodeAttachmentMessage>()),
                hideBottomSheet
            ) {}
        )
        with(composeRule) {
            onNodeWithTag(underTest.bottomSheetItemTestTag).assertIsDisplayed()
            onNodeWithText(composeRule.activity.getString(mega.privacy.android.app.R.string.general_save_to_device)).assertIsDisplayed()
            onNodeWithTag(underTest.bottomSheetItemTestTag).performClick()
            verify(hideBottomSheet).invoke()
        }
    }

    @Test
    fun `test that save to device option triggers download`() {
        val onHandled: () -> Unit = mock()
        val fileNode = mock<ChatImageFile>()
        val node = mock<NodeAttachmentMessage> {
            on { this.fileNode } doReturn fileNode
        }
        composeRule.setContent {
            underTest.OnTrigger(
                messages = setOf(node),
                onHandled = onHandled
            )
        }
        verify(onHandled).invoke()
        verify(chatViewModel).onDownloadNode(listOf(fileNode))
    }
}