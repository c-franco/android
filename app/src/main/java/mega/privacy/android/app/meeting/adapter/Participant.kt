package mega.privacy.android.app.meeting.adapter

import mega.privacy.android.app.meeting.listeners.MeetingVideoListener
import java.io.File
import java.io.Serializable

data class Participant(
    val peerId: Long,
    val clientId: Long,
    var name: String,
    val avatar: File?,
    val avatarBackground: String,
    val isMe: Boolean,
    var isModerator: Boolean,
    var isAudioOn: Boolean,
    var isVideoOn: Boolean,
    var isContact: Boolean = true,
    var isSelected: Boolean = false,
    var hasHiRes: Boolean = false,
    var videoListener: MeetingVideoListener? = null
) : Serializable