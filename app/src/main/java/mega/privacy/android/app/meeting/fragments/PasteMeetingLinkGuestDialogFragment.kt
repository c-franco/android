package mega.privacy.android.app.meeting.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_paste_meeting_link_guest.*
import mega.privacy.android.app.MegaApplication
import mega.privacy.android.app.OpenLinkActivity
import mega.privacy.android.app.R
import mega.privacy.android.app.listeners.ChatBaseListener
import mega.privacy.android.app.lollipop.megachat.AndroidMegaRichLinkMessage
import mega.privacy.android.app.utils.CallUtil
import mega.privacy.android.app.utils.ColorUtils.setErrorAwareInputAppearance
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.app.utils.TextUtil.isTextEmpty
import mega.privacy.android.app.utils.Util
import mega.privacy.android.app.utils.Util.showSnackbar
import nz.mega.sdk.MegaChatApi
import nz.mega.sdk.MegaChatApiJava
import nz.mega.sdk.MegaChatError
import nz.mega.sdk.MegaChatRequest

class PasteMeetingLinkGuestDialogFragment : DialogFragment() {

    private lateinit var linkEdit: EditText
    private lateinit var errorLayout: ViewGroup
    private lateinit var errorText: TextView

    private var meetingLink: String = ""

    private val megaChatApi = MegaApplication.getInstance().getMegaChatApi()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inflater = requireActivity().layoutInflater;
        val view = inflater.inflate(R.layout.dialog_paste_meeting_link_guest, null)

        linkEdit = view.findViewById(R.id.meeting_link)
        errorLayout = view.findViewById(R.id.error)
        errorText = view.findViewById(R.id.error_text)

        linkEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (errorLayout.visibility == View.VISIBLE) {
                    hideError()
                }
            }
        })

        builder.setTitle(R.string.paste_meeting_link_guest_dialog_title)
            .setMessage(getString(R.string.paste_meeting_link_guest_instruction))
            .setView(view)
            .setPositiveButton(R.string.general_ok, null)
            .setNegativeButton(R.string.general_cancel, null)

        val dialog = builder.create()
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        Util.showKeyboardDelayed(linkEdit)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            meetingLink = linkEdit.text.toString()

            if (TextUtils.isEmpty(meetingLink)) {
                showError(R.string.invalid_meeting_link_empty);
                return@setOnClickListener;
            }

            // Meeting Link and Chat Link are exactly the same format.
            // Using extra approach(getMegaHandleList of openChatPreview())
            // to judge if its a meeting link later on
            if (AndroidMegaRichLinkMessage.isChatLink(meetingLink)) {
                // TODO: +Meeting, use open link activity or self logic to process the link?
//                initMegaChat { checkMeetingLink() }
                // Need to call the async checkChatLink() to check if the chat has a call and
                // get the meeting name
                // Delegate the checking to OpenLinkActivity
                // If yes, show Join Meeting, If no, show Chat history
                startOpenLinkActivity()
                dismiss()
            } else {
                showError(R.string.invalid_meeting_link_args)
            }
        }

        return dialog
    }

    private fun startOpenLinkActivity() {
        val intent = Intent(requireContext(), OpenLinkActivity::class.java)
        intent.putExtra(ACTION_JOIN_AS_GUEST, "any")
        intent.data = Uri.parse(meetingLink)
        startActivity(intent)
    }

    private fun initMegaChat(doAfterConnect: (() -> Unit)?) {
        var initResult: Int = megaChatApi.getInitState()

        if (initResult < MegaChatApi.INIT_WAITING_NEW_SESSION) {
            initResult = megaChatApi.initAnonymous()
        }

        if (initResult == MegaChatApi.INIT_ERROR) {
            showError(R.string.error_meeting_link_init_error)
            return;
        }

        megaChatApi.connect(object : ChatBaseListener(requireContext()) {
            override fun onRequestFinish(
                api: MegaChatApiJava,
                request: MegaChatRequest,
                e: MegaChatError
            ) {
                if (request.type != MegaChatRequest.TYPE_CONNECT
                    || e.errorCode != MegaChatError.ERROR_OK
                ) {
                    showError(R.string.error_meeting_link_init_error)
                    return
                }

                doAfterConnect?.invoke()
            }
        })
    }

    private fun checkMeetingLink() = megaChatApi.checkChatLink(
        meetingLink,
        object : ChatBaseListener(requireContext()) {
            override fun onRequestFinish(
                api: MegaChatApiJava,
                request: MegaChatRequest,
                e: MegaChatError
            ) {
                if (e.errorCode == MegaChatError.ERROR_OK || e.errorCode == MegaChatError.ERROR_EXIST) {
                    if (isTextEmpty(request.link) && request.chatHandle == MegaChatApiJava.MEGACHAT_INVALID_HANDLE) {
                        showSnackbar(
                            requireContext(),
                            Constants.SNACKBAR_TYPE,
                            getString(R.string.error_meeting_link_init_error),
                            MegaChatApiJava.MEGACHAT_INVALID_HANDLE
                        )
                        return
                    }
                    CallUtil.openMeetingGuestMode(requireContext(), null, request.chatHandle, request.link)
                    dismiss()
                } else if (e.errorCode == MegaChatError.ERROR_NOENT) {
                    Util.showAlert(
                        requireContext(),
                        getString(R.string.invalid_link),
                        getString(R.string.meeting_link)
                    )
                } else {
                    showError(R.string.invalid_meeting_link_args)
                }
            }
        })

    private fun showError(errorStringId: Int) {
        setErrorAwareInputAppearance(linkEdit, true)
        errorLayout.visibility = View.VISIBLE
        errorText.text = getString(errorStringId)
    }

    private fun hideError() {
        setErrorAwareInputAppearance(linkEdit, false)
        errorLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        Util.showKeyboardDelayed(meeting_link)
    }

    companion object {
        const val TAG = "PasteMeetingLinkGuestDialog"

        const val ACTION_JOIN_AS_GUEST = "action_join_as_guest"
    }
}