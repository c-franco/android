package mega.privacy.android.domain.usecase.chat.message

import mega.privacy.android.domain.entity.chat.ChatMessage
import mega.privacy.android.domain.entity.chat.messages.management.RetentionTimeUpdatedMessage
import javax.inject.Inject


internal class CreateRetentionTimeUpdatedMessageUseCase @Inject constructor() :
    CreateTypedMessageUseCase {

    override fun invoke(message: ChatMessage, isMine: Boolean) = RetentionTimeUpdatedMessage(
        msgId = message.msgId,
        time = message.timestamp,
        isMine = isMine,
        userHandle = message.userHandle,
        retentionTime = message.retentionTime
    )
}