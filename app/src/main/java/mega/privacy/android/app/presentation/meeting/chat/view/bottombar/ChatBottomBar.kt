package mega.privacy.android.app.presentation.meeting.chat.view.bottombar

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import mega.privacy.android.app.R
import mega.privacy.android.app.extensions.navigateToAppSettings
import mega.privacy.android.app.presentation.meeting.chat.model.ChatUiState
import mega.privacy.android.app.presentation.meeting.chat.view.UserTypingView
import mega.privacy.android.core.ui.controls.chat.ChatInputTextToolbar
import mega.privacy.android.core.ui.controls.chat.VoiceClipRecordEvent
import mega.privacy.android.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.core.ui.utils.ComposableLifecycle
import mega.privacy.android.shared.theme.MegaAppTheme

@Composable
internal fun ChatBottomBar(
    param: ChatBottomBarParameter,
) {
    ChatBottomBar(
        uiState = param.uiState,
        showEmojiPicker = param.showEmojiPicker,
        onSendClick = param.onSendClick,
        onAttachmentClick = param.onAttachmentClick,
        onEmojiClick = param.onEmojiClick,
        interactionSourceTextInput = param.interactionSourceTextInput,
        onCloseEditing = param.onCloseEditing,
        onVoiceClipEvent = param.onVoiceClipEvent,
    )
}

/**
 * Chat bottom bar
 *
 * @param uiState chat ui state
 * @param showEmojiPicker show emoji picker
 * @param onSendClick send click
 * @param onAttachmentClick attachment click
 * @param onEmojiClick emoji click
 * @param interactionSourceTextInput interaction source text input
 */
@Composable
internal fun ChatBottomBar(
    uiState: ChatUiState,
    showEmojiPicker: Boolean,
    onSendClick: (String) -> Unit,
    onAttachmentClick: () -> Unit,
    onEmojiClick: () -> Unit,
    interactionSourceTextInput: MutableInteractionSource,
    onCloseEditing: () -> Unit,
    onVoiceClipEvent: (VoiceClipRecordEvent) -> Unit = {},
    viewModel: ChatBottomBarViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by rememberSaveable(
        inputs = arrayOf(uiState.sendingText, uiState.editingMessageContent),
        stateSaver = TextFieldValue.Saver
    ) {
        mutableStateOf(
            uiState.editingMessageContent?.let {
                val text = uiState.sendingText
                focusRequester.requestFocus()
                coroutineScope.launch { keyboardController?.show() }
                TextFieldValue(text, TextRange(text.length))
            } ?: TextFieldValue(uiState.sendingText)
        )
    }
    ComposableLifecycle(key = textFieldValue.text) {
        if (it == Lifecycle.Event.ON_PAUSE) {
            viewModel.saveDraftMessage(textFieldValue.text, uiState.editingMessageId)
        }
    }

    ChatBottomBarContent(
        uiState = uiState,
        textFieldValue = textFieldValue,
        showEmojiPicker = showEmojiPicker,
        onSendClick = onSendClick,
        onAttachmentClick = onAttachmentClick,
        onEmojiClick = onEmojiClick,
        interactionSourceTextInput = interactionSourceTextInput,
        onTextChange = { textFieldValue = it },
        onCloseEditing = onCloseEditing,
        onVoiceClipEvent = onVoiceClipEvent,
        focusRequester = focusRequester,
    )
}

/**
 * Chat bottom bar content
 *
 * @param uiState
 * @param textFieldValue
 * @param showEmojiPicker
 * @param onSendClick
 * @param onAttachmentClick
 * @param onEmojiClick
 * @param onTextChange
 * @param interactionSourceTextInput
 */
@Composable
fun ChatBottomBarContent(
    uiState: ChatUiState,
    textFieldValue: TextFieldValue,
    showEmojiPicker: Boolean,
    onSendClick: (String) -> Unit,
    onAttachmentClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    interactionSourceTextInput: MutableInteractionSource,
    onCloseEditing: () -> Unit = {},
    onVoiceClipEvent: (VoiceClipRecordEvent) -> Unit = {},
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val context = LocalContext.current
    Column {
        UserTypingView(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            usersTyping = uiState.usersTyping,
        )
        ChatInputTextToolbar(
            onAttachmentClick = onAttachmentClick,
            text = uiState.sendingText,
            placeholder = stringResource(
                R.string.type_message_hint_with_customized_title,
                uiState.title.orEmpty()
            ),
            showEmojiPicker = showEmojiPicker,
            onSendClick = {
                onSendClick(it)
                onTextChange(TextFieldValue(""))
            },
            onEmojiClick = onEmojiClick,
            interactionSource = interactionSourceTextInput,
            textFieldValue = textFieldValue,
            onTextChange = onTextChange,
            editingMessageId = uiState.editingMessageId,
            editMessageContent = uiState.editingMessageContent,
            onCloseEditing = onCloseEditing,
            onVoiceClipEvent = onVoiceClipEvent,
            onNavigateToAppSettings = { context.navigateToAppSettings() },
            focusRequester = focusRequester,
        )
    }
}

@CombinedThemePreviews
@Composable
private fun ChatBottomBarPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        ChatBottomBarContent(
            uiState = ChatUiState(
                sendingText = "Sending text",
                usersTyping = listOf("User 1", "User 2"),
            ),
            textFieldValue = TextFieldValue(""),
            showEmojiPicker = false,
            onSendClick = {},
            onAttachmentClick = {},
            onEmojiClick = {},
            onTextChange = {},
            interactionSourceTextInput = remember { MutableInteractionSource() },
        )
    }
}

internal data class ChatBottomBarParameter(
    val uiState: ChatUiState,
    val showEmojiPicker: Boolean,
    val onSendClick: (String) -> Unit,
    val onAttachmentClick: () -> Unit,
    val onEmojiClick: () -> Unit,
    val interactionSourceTextInput: MutableInteractionSource,
    val onCloseEditing: () -> Unit,
    val onVoiceClipEvent: (VoiceClipRecordEvent) -> Unit,
)