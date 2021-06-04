package mega.privacy.android.app.meeting.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_meeting.*
import mega.privacy.android.app.MegaApplication
import mega.privacy.android.app.R
import mega.privacy.android.app.activities.PasscodeActivity
import mega.privacy.android.app.databinding.ActivityMeetingBinding
import mega.privacy.android.app.meeting.fragments.MeetingBaseFragment
import nz.mega.sdk.MegaChatApiJava.MEGACHAT_INVALID_HANDLE

// FIXME: Keep Meeting Activity from implementing this and that listeners
// FIXME: And don't directly call megaChatApi in view layer, try don't put everything together and bloat the View layer file

@AndroidEntryPoint
class MeetingActivity : PasscodeActivity() {

    companion object {
        /** Tne name of actions denoting set
        JOIN/CREATE/JOIN AS GUEST/In-meeting screen as the initial screen */
        const val MEETING_ACTION_JOIN = "join_meeting"
        const val MEETING_ACTION_CREATE = "create_meeting"
        const val MEETING_ACTION_GUEST = "join_meeting_as_guest"
        const val MEETING_ACTION_IN = "in_meeting"
        const val MEETING_ACTION_RINGING = "ringing_meeting"
        const val MEETING_ACTION_RINGING_VIDEO_ON = "ringing_meeting_video_on"
        const val MEETING_ACTION_RINGING_VIDEO_OFF = "ringing_meeting_video_off"
        const val MEETING_ACTION_START = "start_meeting"

        /** The names of the Extra data being passed to the initial fragment */
        const val MEETING_NAME = "meeting_name"
        const val MEETING_LINK = "meeting_link"
        const val MEETING_CHAT_ID = "chat_id"
        const val MEETING_AUDIO_ENABLE = "audio_enable"
        const val MEETING_VIDEO_ENABLE = "video_enable"
    }

    private lateinit var binding: ActivityMeetingBinding
    private val meetingViewModel: MeetingActivityViewModel by viewModels()

    private var meetingAction: String? = null

    // TODO: Move to a more common place
    private fun View.setMarginTop(marginTop: Int) {
        val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
        menuLayoutParams.setMargins(0, marginTop, 0, 0)
        this.layoutParams = menuLayoutParams
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or 0x00000010

        binding = ActivityMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        meetingAction = intent.action

        initActionBar()
        initNavigation()
        setStatusBarTranslucent(window, true)
    }

    private fun setStatusBarTranslucent(window: Window, translucent: Boolean) {
        val decorView: View = window.decorView

        if (translucent) {
            decorView.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets? ->
                val defaultInsets = v.onApplyWindowInsets(insets)

                toolbar.setMarginTop(defaultInsets.systemWindowInsetTop)

                defaultInsets.replaceSystemWindowInsets(
                    defaultInsets.systemWindowInsetLeft,
                    0,
                    defaultInsets.systemWindowInsetRight,
                    defaultInsets.systemWindowInsetBottom
                )
            }
        } else {
            decorView.setOnApplyWindowInsetsListener(null)
        }

        ViewCompat.requestApplyInsets(decorView)
    }

    /**
     * Initialize Action Bar and set icon according to param
     */
    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setHomeButtonEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.title = ""

        when (meetingAction) {
            MEETING_ACTION_CREATE -> {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white)
                // Toolbar should be set to TRANSPARENT in "Create Meeting"
                actionBar.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            MEETING_ACTION_JOIN,  MEETING_ACTION_GUEST
            -> actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white)
            MEETING_ACTION_IN -> actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
        }
    }

    /**
     * Initialize Navigation and set startDestination(initial screen)
     * according to the meeting action
     */
    private fun initNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph: NavGraph =
            navHostFragment.navController.navInflater.inflate(R.navigation.meeting)


        // The args to be passed to startDestination
        val bundle = Bundle()

        bundle.putLong(
            MEETING_CHAT_ID,
            intent.getLongExtra(MEETING_CHAT_ID, MEGACHAT_INVALID_HANDLE)
        )

        // Pass the meeting data to Join Meeting screen
        if (meetingAction == MEETING_ACTION_GUEST || meetingAction == MEETING_ACTION_JOIN) {
            bundle.putString(MEETING_LINK, intent.dataString)
            bundle.putString(MEETING_NAME, intent.getStringExtra(MEETING_NAME))
        }

        if (meetingAction == MEETING_ACTION_IN) {
            bundle.putBoolean(
                MEETING_AUDIO_ENABLE, intent.getBooleanExtra(
                    MEETING_AUDIO_ENABLE,
                    false
                )
            )
            bundle.putBoolean(
                MEETING_VIDEO_ENABLE, intent.getBooleanExtra(
                    MEETING_VIDEO_ENABLE,
                    false
                )
            )
        }

        if(meetingAction == MEETING_ACTION_START) {
            bundle.putString("action", MEETING_ACTION_START)

            bundle.putLong(
                MEETING_CHAT_ID,
                intent.getLongExtra(MEETING_CHAT_ID, MEGACHAT_INVALID_HANDLE)
            )
        }

        navGraph.startDestination = when (meetingAction) {
            MEETING_ACTION_CREATE -> R.id.createMeetingFragment
            MEETING_ACTION_JOIN -> R.id.joinMeetingFragment
            MEETING_ACTION_GUEST -> R.id.joinMeetingAsGuestFragment
            MEETING_ACTION_START, MEETING_ACTION_IN -> R.id.inMeetingFragment
            MEETING_ACTION_RINGING -> R.id.ringingMeetingFragment
            else -> R.id.createMeetingFragment
        }

        // Remove app:navGraph="@navigation/meeting" and instead call navController.graph = navGraph
        // Change start destination dynamically
        navController.setGraph(navGraph, bundle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (!meetingViewModel.isChatCreated()) {
                    MegaApplication.getInstance().removeRTCAudioManager()
                }

                if(meetingAction == MEETING_ACTION_GUEST) {
                    super.onBackPressed()
                } else {
                    onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Get current fragment from navHostFragment
     */
    fun getCurrentFragment(): MeetingBaseFragment? {
        val navHostFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        return navHostFragment?.childFragmentManager?.fragments?.get(0) as MeetingBaseFragment?
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        meetingViewModel.inviteToChat(this, requestCode, resultCode, intent)
        super.onActivityResult(requestCode, resultCode, intent)
    }

    fun showSnackbar(content: String) {
        showSnackbar(binding.navHostFragment, content)
    }

    override fun onBackPressed() {
        if(meetingAction == MEETING_ACTION_GUEST) return

        super.onBackPressed()

        finish()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (app.isAnIncomingCallRinging) {
                    app.muteOrUnmute(false)
                }
                false
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (app.isAnIncomingCallRinging) {
                    app.muteOrUnmute(true)
                }
                false
            }
            else -> super.dispatchKeyEvent(event)
        }
    }
}