package mega.privacy.android.domain.entity.chat.messages.meta

import mega.privacy.android.domain.entity.chat.messages.reactions.Reaction

/**
 * Giphy message
 *
 * @property chatGifInfo [ChatGifInfo]
 */
data class GiphyMessage(
    override val msgId: Long,
    override val time: Long,
    override val isMine: Boolean,
    override val userHandle: Long,
    override val shouldShowAvatar: Boolean,
    override val shouldShowTime: Boolean,
    override val shouldShowDate: Boolean,
    override val reactions: List<Reaction>,
    val chatGifInfo: ChatGifInfo?
) : MetaMessage