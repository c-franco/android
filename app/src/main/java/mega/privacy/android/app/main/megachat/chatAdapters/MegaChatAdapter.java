package mega.privacy.android.app.main.megachat.chatAdapters;

import static mega.privacy.android.app.activities.GiphyPickerActivity.GIF_DATA;
import static mega.privacy.android.app.components.textFormatter.TextFormatterViewCompat.getFormattedText;
import static mega.privacy.android.app.utils.AvatarUtil.getAvatarUri;
import static mega.privacy.android.app.utils.AvatarUtil.getColorAvatar;
import static mega.privacy.android.app.utils.AvatarUtil.getDefaultAvatar;
import static mega.privacy.android.app.utils.AvatarUtil.getSpecificAvatarColor;
import static mega.privacy.android.app.utils.CallUtil.milliSecondsToTimer;
import static mega.privacy.android.app.utils.ChatUtil.StatusIconLocation;
import static mega.privacy.android.app.utils.ChatUtil.checkForwardVisibilityInContactMsg;
import static mega.privacy.android.app.utils.ChatUtil.checkForwardVisibilityInOwnMsg;
import static mega.privacy.android.app.utils.ChatUtil.converterShortCodes;
import static mega.privacy.android.app.utils.ChatUtil.getInvalidMetaMessage;
import static mega.privacy.android.app.utils.ChatUtil.getMaxWidthItem;
import static mega.privacy.android.app.utils.ChatUtil.getReactionsList;
import static mega.privacy.android.app.utils.ChatUtil.getTitleChat;
import static mega.privacy.android.app.utils.ChatUtil.getUserStatus;
import static mega.privacy.android.app.utils.ChatUtil.getVoiceClipDuration;
import static mega.privacy.android.app.utils.ChatUtil.isMsgRemovedOrHasRejectedOrManualSendingStatus;
import static mega.privacy.android.app.utils.ChatUtil.isVoiceClip;
import static mega.privacy.android.app.utils.ChatUtil.setContactStatus;
import static mega.privacy.android.app.utils.ChatUtil.transformSecondsInString;
import static mega.privacy.android.app.utils.Constants.ACTION_PREVIEW_GIPHY;
import static mega.privacy.android.app.utils.Constants.AVATAR_PRIMARY_COLOR;
import static mega.privacy.android.app.utils.Constants.AVATAR_SIZE;
import static mega.privacy.android.app.utils.Constants.EMOJI_SIZE;
import static mega.privacy.android.app.utils.Constants.EMOJI_SIZE_EXTRA_HIGH;
import static mega.privacy.android.app.utils.Constants.EMOJI_SIZE_HIGH;
import static mega.privacy.android.app.utils.Constants.EMOJI_SIZE_MEDIUM;
import static mega.privacy.android.app.utils.Constants.ERROR_VOICE_CLIP_TRANSFER;
import static mega.privacy.android.app.utils.Constants.INVALID_POSITION;
import static mega.privacy.android.app.utils.Constants.INVALID_VALUE;
import static mega.privacy.android.app.utils.Constants.RICH_WARNING_CONFIRMATION;
import static mega.privacy.android.app.utils.Constants.RICH_WARNING_FALSE;
import static mega.privacy.android.app.utils.Constants.RICH_WARNING_TRUE;
import static mega.privacy.android.app.utils.Constants.SNACKBAR_TYPE;
import static mega.privacy.android.app.utils.Constants.TYPE_VOICE_CLIP;
import static mega.privacy.android.app.utils.ContactUtil.getMegaUserNameDB;
import static mega.privacy.android.app.utils.ContactUtil.isContact;
import static mega.privacy.android.app.utils.FileUtil.JPG_EXTENSION;
import static mega.privacy.android.app.utils.FileUtil.getLocalFile;
import static mega.privacy.android.app.utils.FileUtil.isFileAvailable;
import static mega.privacy.android.app.utils.FrescoUtils.loadGifMessage;
import static mega.privacy.android.app.utils.GiphyUtil.getOriginalGiphySrc;
import static mega.privacy.android.app.utils.LinksUtil.interceptLinkClicks;
import static mega.privacy.android.app.utils.LinksUtil.isIsClickAlreadyIntercepted;
import static mega.privacy.android.app.utils.LinksUtil.resetIsClickAlreadyIntercepted;
import static mega.privacy.android.app.utils.PreviewUtils.createVideoPreview;
import static mega.privacy.android.app.utils.PreviewUtils.getBitmapForCache;
import static mega.privacy.android.app.utils.PreviewUtils.getPreview;
import static mega.privacy.android.app.utils.PreviewUtils.getPreviewFolder;
import static mega.privacy.android.app.utils.PreviewUtils.getPreviewFromCache;
import static mega.privacy.android.app.utils.PreviewUtils.getPreviewFromFolder;
import static mega.privacy.android.app.utils.PreviewUtils.previewCache;
import static mega.privacy.android.app.utils.PreviewUtils.resizeBitmapUpload;
import static mega.privacy.android.app.utils.PreviewUtils.setPreviewCache;
import static mega.privacy.android.app.utils.ScheduledMeetingDateUtil.getAppropriateStringForScheduledMeetingDate;
import static mega.privacy.android.app.utils.TextUtil.isTextEmpty;
import static mega.privacy.android.app.utils.TextUtil.replaceFormatChatMessages;
import static mega.privacy.android.app.utils.ThumbnailUtils.getThumbnailFromCache;
import static mega.privacy.android.app.utils.ThumbnailUtils.getThumbnailFromFolder;
import static mega.privacy.android.app.utils.TimeUtils.DATE_SHORT_FORMAT;
import static mega.privacy.android.app.utils.TimeUtils.formatDate;
import static mega.privacy.android.app.utils.TimeUtils.formatTime;
import static mega.privacy.android.app.utils.TimeUtils.getVideoDuration;
import static mega.privacy.android.app.utils.Util.convertToDegrees;
import static mega.privacy.android.app.utils.Util.dp2px;
import static mega.privacy.android.app.utils.Util.getSizeString;
import static mega.privacy.android.app.utils.Util.isOnline;
import static mega.privacy.android.app.utils.Util.isScreenInPortrait;
import static mega.privacy.android.app.utils.Util.rotateBitmap;
import static mega.privacy.android.app.utils.Util.scaleHeightPx;
import static mega.privacy.android.app.utils.Util.scaleWidthPx;
import static mega.privacy.android.app.utils.Util.toCDATA;
import static nz.mega.sdk.MegaApiJava.INVALID_HANDLE;
import static nz.mega.sdk.MegaChatApiJava.MEGACHAT_INVALID_HANDLE;
import static nz.mega.sdk.MegaChatMessage.END_CALL_REASON_CANCELLED;
import static nz.mega.sdk.MegaChatMessage.END_CALL_REASON_NO_ANSWER;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;
import mega.privacy.android.app.LegacyDatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.activities.GiphyViewerActivity;
import mega.privacy.android.app.components.EqualSpacingItemDecoration;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.components.twemoji.EmojiManager;
import mega.privacy.android.app.components.twemoji.EmojiTextView;
import mega.privacy.android.app.components.twemoji.reaction.AutoFitRecyclerView;
import mega.privacy.android.app.components.voiceClip.DetectorSeekBar;
import mega.privacy.android.app.listeners.GetPeerAttributesListener;
import mega.privacy.android.app.main.adapters.ReactionAdapter;
import mega.privacy.android.app.main.adapters.RotatableAdapter;
import mega.privacy.android.app.main.controllers.ChatController;
import mega.privacy.android.app.main.listeners.ChatNonContactNameListener;
import mega.privacy.android.app.main.megachat.ChatActivity;
import mega.privacy.android.app.main.megachat.MessageVoiceClip;
import mega.privacy.android.app.main.megachat.RemovedMessage;
import mega.privacy.android.app.mediaplayer.service.AudioPlayerService;
import mega.privacy.android.app.objects.GifData;
import mega.privacy.android.app.presentation.chat.ChatViewModel;
import mega.privacy.android.app.usecase.GetAvatarUseCase;
import mega.privacy.android.app.usecase.GetNodeUseCase;
import mega.privacy.android.app.utils.CacheFolderManager;
import mega.privacy.android.app.utils.ColorUtils;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MeetingUtil;
import mega.privacy.android.app.utils.TextUtil;
import mega.privacy.android.app.utils.Util;
import mega.privacy.android.data.model.chat.AndroidMegaChatMessage;
import mega.privacy.android.domain.entity.chat.ChatScheduledMeeting;
import mega.privacy.android.domain.entity.chat.PendingMessage;
import mega.privacy.android.domain.entity.chat.PendingMessageState;
import mega.privacy.android.domain.entity.contacts.ContactLink;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatContainsMeta;
import nz.mega.sdk.MegaChatGiphy;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaHandleList;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaStringList;
import nz.mega.sdk.MegaUser;
import nz.mega.sdk.MegaUtilsAndroid;
import timber.log.Timber;

public class MegaChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener, RotatableAdapter {
    private static final int MAX_SIZE_GIF_AUTO_PLAY = 1024 * 1024 * 4; // 4MB

    private static int MAX_WIDTH_FILENAME_LAND = 455;
    private static int MAX_WIDTH_FILENAME_PORT = 180;

    //margins of management message and hour
    private final static int MANAGEMENT_MESSAGE_LAND = 28;
    private final static int MANAGEMENT_MESSAGE_PORT = 48;

    //margins of management message and hour in a CALL
    private static int MANAGEMENT_MESSAGE_CALL_LAND = 40;
    private static int MANAGEMENT_MESSAGE_CALL_PORT = 60;

    //paddings of hours (right and left)
    private static int PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND = 9;
    private static int PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT = 16;

    //margins of normal/attachment contacts messages and hour
    private static int CONTACT_MESSAGE_LAND = 28;
    private static int CONTACT_MESSAGE_PORT = 48;

    private final static int MAX_WIDTH_NAME_LAND = 340;
    private final static int MAX_WIDTH_NAME_PORT = 200;

    private final static int MAX_WIDTH_MESSAGE_LAND = 310;
    private final static int MAX_WIDTH_MESSAGE_PORT = 275;

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_ITEM = 1;

    private final static int INVALID_INFO = -1;

    private final static int LAYOUT_WIDTH = 330;
    private static int REACTION_SPACE = 8;

    Context context;
    private int positionClicked;
    private ArrayList<AndroidMegaChatMessage> messages = new ArrayList<>();
    private ArrayList<RemovedMessage> removedMessages = new ArrayList<>();

    private RecyclerView listFragment;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;
    private LegacyDatabaseHandler dbH;
    boolean multipleSelect;
    HashMap<Long, Integer> messagesSelectedInChat = new HashMap<>();

    private MegaChatAdapter megaChatAdapter;
    private ArrayList<MessageVoiceClip> messagesPlaying;
    private int placeholderCount = 0;

    private Handler handlerVoiceNotes;
    private Runnable runnableVC;
    ChatController cC;

    private ChatViewModel viewModel;

    private long myUserHandle = -1;

    DisplayMetrics outMetrics;


    MegaChatRoom chatRoom;

    private EqualSpacingItemDecoration itemDecorationContact = new EqualSpacingItemDecoration(REACTION_SPACE, EqualSpacingItemDecoration.HORIZONTAL);
    private EqualSpacingItemDecoration itemDecorationOwn = new EqualSpacingItemDecoration(REACTION_SPACE, EqualSpacingItemDecoration.VERTICAL);
    private HashMap<Long, Long> pendingPreviews = new HashMap<>();

    private ArrayList<Uri> animationsPlaying = new ArrayList<>();

    private GetAvatarUseCase getAvatarUseCase;
    private GetNodeUseCase getNodeUseCase;

    public boolean areTransfersPaused;

    private class ChatVoiceClipAsyncTask extends AsyncTask<MegaNodeList, Void, Integer> {
        MegaChatAdapter.ViewHolderMessageChat holder;
        int position;
        long userHandle;
        MegaNodeList nodeList;

        public ChatVoiceClipAsyncTask(MegaChatAdapter.ViewHolderMessageChat holder, int position, long userHandle) {
            this.holder = holder;
            this.userHandle = userHandle;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            if (holder == null) {
                holder = (ViewHolderMessageChat) listFragment.findViewHolderForAdapterPosition(position);
            }
            if (holder == null) return;

            Timber.d("ChatVoiceClipAsyncTask:onPreExecute");
            if (userHandle == myUserHandle) {
                holder.uploadingOwnProgressbarVoiceclip.setVisibility(View.VISIBLE);
                holder.contentOwnMessageVoiceClipPlay.setVisibility(View.GONE);
                holder.notAvailableOwnVoiceclip.setVisibility(View.GONE);
            } else {
                holder.uploadingContactProgressbarVoiceclip.setVisibility(View.VISIBLE);
                holder.contentContactMessageVoiceClipPlay.setVisibility(View.GONE);
                holder.notAvailableContactVoiceclip.setVisibility(View.GONE);
            }
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(MegaNodeList... params) {
            nodeList = params[0];
            ((ChatActivity) context).sendToDownload(nodeList);
            return 1;
        }
    }

    private class ChatPreviewAsyncTask extends AsyncTask<MegaNode, Void, Integer> {

        MegaChatAdapter.ViewHolderMessageChat holder;
        MegaNode node;
        Bitmap preview;
        long msgId;

        public ChatPreviewAsyncTask(MegaChatAdapter.ViewHolderMessageChat holder, long msgId) {
            this.holder = holder;
            this.msgId = msgId;
        }

        @Override
        protected Integer doInBackground(MegaNode... params) {
            node = params[0];
            preview = getPreviewFromFolder(node, context);

            if (preview != null) {
                previewCache.put(node.getHandle(), preview);
                return 0;
            } else if (pendingPreviews.containsKey(node.getHandle())) {
                Timber.d("The preview is already downloaded or added to the list");
                return 1;
            } else {
                return 2;
            }
        }

        @Override
        protected void onPostExecute(Integer param) {
            if (param == 0) {
                AndroidMegaChatMessage message = getMessageAtAdapterPosition(holder.getAdapterPosition());
                if (message == null) {
                    Timber.w("Messages removed");
                    return;
                }

                if (message.getMessage() != null && message.getMessage().getMegaNodeList() != null
                        && message.getMessage().getMegaNodeList().get(0) != null) {
                    long nodeMessageHandle = message.getMessage().getMegaNodeList().get(0).getHandle();
                    if (nodeMessageHandle != node.getHandle()) {
                        Timber.w("The nodeHandles are not equal!");
                        return;
                    }

                    if (message.getMessage().getUserHandle() == megaChatApi.getMyUserHandle()) {
                        setOwnPreview(holder, preview, node, checkForwardVisibilityInOwnMsg(removedMessages, message.getMessage(), isMultipleSelect(), cC), message.getMessage());
                        if (isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message.getMessage())) {
                            setErrorStateOnPreview(holder, preview, message.getMessage().getStatus());
                        }
                    } else {
                        setContactPreview(holder, preview, node);
                    }
                }
            } else if (param == 2) {
                File previewFile = new File(getPreviewFolder(context), node.getBase64Handle() + JPG_EXTENSION);
                Timber.d("GET PREVIEW OF HANDLE: %d to download here: %s", node.getHandle(), previewFile.getAbsolutePath());
                pendingPreviews.put(node.getHandle(), msgId);
                PreviewDownloadListener listener = new PreviewDownloadListener(context, holder, megaChatAdapter, node);
                megaApi.getPreview(node, previewFile.getAbsolutePath(), listener);
            }
        }
    }

    private class ChatLocalPreviewAsyncTask extends AsyncTask<MegaNode, Void, Integer> {

        MegaNode node;
        Bitmap preview;
        File cacheDir;
        File destination;
        long msgId;
        MegaChatAdapter.ViewHolderMessageChat holder;

        public ChatLocalPreviewAsyncTask(MegaChatAdapter.ViewHolderMessageChat holder, long msgId) {
            this.holder = holder;
            this.msgId = msgId;
        }

        @Override
        protected Integer doInBackground(MegaNode... params) {
            Timber.d("ChatLocalPreviewAsyncTask-doInBackground");

            node = params[0];

            if (node == null) {
                return 3;
            }
            preview = getPreviewFromFolder(node, context);

            if (preview != null) {
                previewCache.put(node.getHandle(), preview);
                return 0;
            } else {
                destination = CacheFolderManager.buildPreviewFile(node.getName());

                if (isFileAvailable(destination)) {
                    if (destination.length() == node.getSize()) {
                        File previewDir = getPreviewFolder(context);
                        File previewFile = new File(previewDir, node.getBase64Handle() + ".jpg");
                        Timber.d("Base 64 handle: %s, Handle: %d", node.getBase64Handle(), node.getHandle());
                        boolean previewCreated = MegaUtilsAndroid.createPreview(destination, previewFile);

                        if (previewCreated) {
                            preview = getBitmapForCache(previewFile, context);
                            destination.delete();
                            return 0;
                        } else {
                            return 1;
                        }
                    } else {
                        destination.delete();
                        return 1;
                    }
                }

                if (pendingPreviews.containsKey(node.getHandle())) {
                    Timber.d("The image is already downloaded or added to the list");
                    return 1;
                } else {
                    return 2;
                }
            }
        }

        @Override
        protected void onPostExecute(Integer param) {
            Timber.d("ChatLocalPreviewAsyncTask-onPostExecute");

            if (param == 0) {
                int position = holder.getAdapterPosition();

                AndroidMegaChatMessage message = messages.get(position - 1);

                long nodeMessageHandle = message.getMessage().getMegaNodeList().get(0).getHandle();

                if (nodeMessageHandle == node.getHandle()) {
                    if (message.getMessage().getUserHandle() == megaChatApi.getMyUserHandle()) {
                        setOwnPreview(holder, preview, node, checkForwardVisibilityInOwnMsg(removedMessages, message.getMessage(), isMultipleSelect(), cC), message.getMessage());
                        if (isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message.getMessage())) {
                            setErrorStateOnPreview(holder, preview, message.getMessage().getStatus());
                        }
                    }
                } else {
                    Timber.w("The nodeHandles are not equal!");
                }
            } else if (param == 2) {
                Timber.w("No preview and no generated correctly");
            }
        }
    }

    private class ChatUploadingPreviewAsyncTask extends AsyncTask<String, Void, Boolean> {
        String filePath;
        MegaChatAdapter adapter;
        int position;

        public ChatUploadingPreviewAsyncTask(MegaChatAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(String... params) {

            filePath = params[0];
            File currentFile = new File(filePath);
            if (MimeTypeList.typeForName(filePath).isImage()) {
                Timber.d("Is image");

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                //ARGB_8888 would create huge memory pressure to app, since we are creating preview, we don't need to have ARGB_8888 as the standard
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap preview;

                ExifInterface exif;
                int orientation = ExifInterface.ORIENTATION_NORMAL;
                try {
                    exif = new ExifInterface(currentFile.getAbsolutePath());
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                } catch (IOException e) {
                    Timber.w(e, "EXCEPTION");
                }

                // Calculate inSampleSize
                options.inSampleSize = Util.calculateInSampleSize(options, 1000, 1000);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;

                preview = BitmapFactory.decodeFile(currentFile.getAbsolutePath(), options);
                if (preview != null) {
                    preview = rotateBitmap(preview, orientation);

                    long fingerprintCache = MegaApiAndroid.base64ToHandle(megaApi.getFingerprint(filePath));
                    if (preview != null) {
                        //put preview bitmap to memory cache
                        setPreviewCache(fingerprintCache, preview);
                        return true;
                    }
                    return false;
                }
            } else if (MimeTypeList.typeForName(filePath).isPdf()) {
                Timber.d("Is pdf");

                FileOutputStream out = null;
                int pageNumber = 0;
                try {

                    PdfiumCore pdfiumCore = new PdfiumCore(context);
                    File previewDir = getPreviewFolder(context);
                    File previewFile = new File(previewDir, currentFile.getName() + ".jpg");

                    PdfDocument pdfDocument = pdfiumCore.newDocument(ParcelFileDescriptor.open(currentFile, ParcelFileDescriptor.MODE_READ_ONLY));
                    pdfiumCore.openPage(pdfDocument, pageNumber);
                    int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
                    int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
                    Bitmap preview = resizeBitmapUpload(bmp, width, height);
                    out = new FileOutputStream(previewFile);
                    boolean result = preview.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                    pdfiumCore.closeDocument(pdfDocument);

                    if (preview != null && result) {
                        Timber.d("Compress OK");
                        long fingerprintCache = MegaApiAndroid.base64ToHandle(megaApi.getFingerprint(previewFile.getPath()));
                        //put preview bitmap to memory cache
                        setPreviewCache(fingerprintCache, preview);
                        return true;
                    } else if (!result) {
                        Timber.w("Not Compress");
                        return false;
                    }
                } catch (Exception e) {
                    Timber.e(e, "Pdf thumbnail could not be created");
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (Exception e) {
                        Timber.w(e, "Error closing FileOutputStream");
                    }
                }
            } else if (MimeTypeList.typeForName(filePath).isVideo()) {
                Timber.d("Is video");
                File previewDir = getPreviewFolder(context);
                File previewFile = new File(previewDir, currentFile.getName() + ".jpg");

                Bitmap bmPreview = createVideoPreview(filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                if (bmPreview == null) {
                    Timber.w("Create video preview NULL");
                } else {
                    Timber.d("Create Video preview worked!");
                }

                if (bmPreview != null) {
                    try {
                        previewFile.createNewFile();
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(previewFile);
                            boolean result = bmPreview.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                            if (result) {
                                Timber.d("Compress OK");
                                long fingerprintCache = MegaApiAndroid.base64ToHandle(megaApi.getFingerprint(previewFile.getPath()));
                                setPreviewCache(fingerprintCache, bmPreview);
                                return true;
                            } else {
                                return false;
                            }
                        } catch (Exception e) {
                            Timber.e(e, "Error with FileOutputStream");
                        } finally {
                            try {
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException e) {
                                Timber.e(e, "Error closing FileOutputStream");
                            }
                        }

                    } catch (IOException e1) {
                        Timber.e(e1, "Error creating new preview file");
                    }
                } else {
                    Timber.w("Create video preview NULL");
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean isContinue) {
            Timber.d("ChatUploadingPreviewAsyncTask-onPostExecute");
            if (isContinue) {
                //notify adapter to update view
                adapter.notifyItemChanged(position);
            } else {
                Timber.w("The preview is NULL!");
            }
        }
    }

    public MegaChatAdapter(Context _context, MegaChatRoom chatRoom,
                           ArrayList<AndroidMegaChatMessage> _messages,
                           ArrayList<MessageVoiceClip> _messagesPlaying,
                           ArrayList<RemovedMessage> _removedMessages,
                           RecyclerView _listView,
                           GetAvatarUseCase getAvatarUseCase, GetNodeUseCase getNodeUseCase,
                           ChatViewModel viewModel, MegaApiAndroid megaApi,
                           MegaChatApiAndroid megaChatApi, LegacyDatabaseHandler dbH) {
        Timber.d("New adapter");
        this.context = _context;
        this.messages = _messages;
        this.positionClicked = INVALID_POSITION;
        this.chatRoom = chatRoom;
        this.removedMessages = _removedMessages;
        this.messagesPlaying = _messagesPlaying;
        this.getAvatarUseCase = getAvatarUseCase;
        this.getNodeUseCase = getNodeUseCase;
        this.viewModel =  viewModel;
        this.megaApi = MegaApplication.getInstance().getMegaApi();
        this.megaChatApi = MegaApplication.getInstance().getMegaChatApi();
        this.dbH = MegaApplication.getInstance().getDbH();

        listFragment = _listView;

        megaChatAdapter = this;

        if (messages != null) {
            Timber.d("Number of messages: %s", messages.size());
        } else {
            Timber.w("Number of messages: NULL");
        }

        myUserHandle = megaChatApi.getMyUserHandle();
        Timber.d("MyUserHandle: %s", myUserHandle);
    }

    public void updateChatRoom(MegaChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public static class ViewHolderMessageChat extends RecyclerView.ViewHolder {
        public ViewHolderMessageChat(View view) {
            super(view);
        }

        boolean contentVisible;
        long userHandle;
        String fullNameTitle;
        boolean nameRequestedAction = false;
        boolean isPlayingAnimation;

        RelativeLayout itemLayout;

        RelativeLayout previewFramePort;
        RelativeLayout previewFrameLand;

        RelativeLayout dateLayout;
        TextView dateText;
        RelativeLayout ownMessageLayout;
        RelativeLayout titleOwnMessage;
        TextView timeOwnText;
        RelativeLayout contentOwnMessageLayout;
        private RelativeLayout ownMessageReactionsLayout;
        private AutoFitRecyclerView ownMessageReactionsRecycler;
        private ReactionAdapter ownReactionsAdapter = null;
        private RelativeLayout ownMessageSelectLayout;
        private ImageView ownMessageSelectIcon;
        private EmojiTextView contentOwnMessageText;

        //Own rich links
        RelativeLayout urlOwnMessageLayout;
        RelativeLayout urlOwnMessageTextrl;
        private ImageView forwardOwnRichLinks;

        private EmojiTextView urlOwnMessageText;
        LinearLayout urlOwnMessageWarningButtonsLayout;
        Button neverRichLinkButton;
        Button alwaysAllowRichLinkButton;
        Button notNowRichLinkButton;
        RelativeLayout urlOwnMessageTitleLayout;
        private EmojiTextView urlOwnMessageTitle;
        TextView urlOwnMessageDescription;

        LinearLayout urlOwnMessageDisableButtonsLayout;
        Button noDisableButton;
        Button yesDisableButton;

        LinearLayout urlOwnMessageIconAndLinkLayout;
        ImageView urlOwnMessageIcon;
        TextView urlOwnMessageLink;

        RoundedImageView urlOwnMessageImage;

        RelativeLayout urlOwnMessageGroupAvatarLayout;
        RoundedImageView urlOwnMessageGroupAvatar;
        TextView urlOwnMessageGroupAvatarText;

        RelativeLayout urlContactMessageGroupAvatarLayout;
        RoundedImageView urlContactMessageGroupAvatar;
        TextView urlContactMessageGroupAvatarText;

        //Contact's rich links
        RelativeLayout urlContactMessageLayout;
        private EmojiTextView urlContactMessageText;
        RelativeLayout urlContactMessageTitleLayout;
        private EmojiTextView urlContactMessageTitle;
        TextView urlContactMessageDescription;
        private ImageView forwardContactRichLinks;

        LinearLayout urlContactMessageIconAndLinkLayout;
        ImageView urlContactMessageIcon;
        TextView urlContactMessageLink;
        RelativeLayout errorUploadingRichLink;
        RoundedImageView urlContactMessageImage;

        RoundedImageView contentOwnMessageThumbLand;
        ImageView gifIconOwnMessageThumbLand;
        ProgressBar gifProgressOwnMessageThumbLand;
        SimpleDraweeView gifViewOwnMessageThumbLand;
        ImageView videoIconOwnMessageThumbLand;
        TextView videoTimecontentOwnMessageThumbLand;

        RoundedImageView contentOwnMessageThumbPort;
        ImageView gifIconOwnMessageThumbPort;
        ProgressBar gifProgressOwnMessageThumbPort;
        SimpleDraweeView gifViewOwnMessageThumbPort;
        ImageView videoIconOwnMessageThumbPort;
        TextView videoTimecontentOwnMessageThumbPort;

        RelativeLayout contentOwnMessageFileLayout;
        ImageView contentOwnMessageFileThumb;
        TextView contentOwnMessageFileName;
        TextView contentOwnMessageFileSize;
        RelativeLayout errorUploadingFile;
        RelativeLayout errorUploadingContact;

        RelativeLayout contentOwnMessageVoiceClipLayout;
        ImageView contentOwnMessageVoiceClipPlay;
        private DetectorSeekBar contentOwnMessageVoiceClipSeekBar;
        TextView contentOwnMessageVoiceClipDuration;
        RelativeLayout errorUploadingVoiceClip;
        long totalDurationOfVoiceClip;
        RelativeLayout uploadingOwnProgressbarVoiceclip;
        ImageView notAvailableOwnVoiceclip;
        RelativeLayout uploadingContactProgressbarVoiceclip;
        ImageView notAvailableContactVoiceclip;

        RelativeLayout contentOwnMessageContactLayout;
        RelativeLayout contentOwnMessageContactLayoutAvatar;
        SimpleDraweeView contentOwnMessageContactThumb;
        private ImageView contentOwnMessageContactVerified;
        private ImageView contentOwnMessageContactStatus;
        private EmojiTextView contentOwnMessageContactName;
        public EmojiTextView contentOwnMessageContactEmail;
        private ImageView forwardOwnContact;

        //Own contact links
        private RelativeLayout ownContactLinkMessage;
        private RelativeLayout ownContactLinkTextLayout;
        private EmojiTextView ownContactLinkText;
        private EmojiTextView ownContactLinkName;
        private EmojiTextView ownContactLinkSubtitle;
        private SimpleDraweeView ownContactLinkAvatar;
        private ImageView ownContactLinkForward;
        private ImageView ownContactLinkError;

        ImageView iconOwnTypeDocLandPreview;
        ImageView iconOwnTypeDocPortraitPreview;

        RelativeLayout transparentCoatingLandscape;
        RelativeLayout transparentCoatingPortrait;
        RelativeLayout uploadingProgressBarPort;
        RelativeLayout uploadingProgressBarLand;

        RelativeLayout errorUploadingPortrait;
        RelativeLayout errorUploadingLandscape;

        private ImageView forwardOwnPortrait;
        private ImageView forwardOwnLandscape;
        private ImageView forwardOwnFile;

        LinearLayout newMessagesLayout;
        TextView newMessagesText;

        TextView retryAlert;
        ImageView triangleIcon;

        //Location message
        RelativeLayout transparentCoatingLocation;
        RelativeLayout uploadingProgressBarLocation;
        private ImageView forwardOwnMessageLocation;
        RelativeLayout mainOwnMessageItemLocation;
        RoundedImageView previewOwnLocation;
        RelativeLayout separatorPreviewOwnLocation;
        RelativeLayout triangleErrorLocation;
        RelativeLayout pinnedOwnLocationLayout;
        TextView pinnedOwnLocationInfoText;
        TextView pinnedLocationTitleText;

        //Contact's message

        RelativeLayout contactMessageLayout;
        RelativeLayout titleContactMessage;

        TextView timeContactText;
        private EmojiTextView nameContactText;

        SimpleDraweeView contactImageView;
        RelativeLayout contentContactMessageLayout;
        private RelativeLayout contactMessageReactionsLayout;
        private AutoFitRecyclerView contactMessageReactionsRecycler;
        private ReactionAdapter contactReactionsAdapter = null;
        private RelativeLayout contactMessageSelectLayout;
        private ImageView contactMessageSelectIcon;
        private EmojiTextView contentContactMessageText;

        RoundedImageView contentContactMessageThumbLand;
        ImageView gifIconContactMessageThumbLand;
        ProgressBar gifProgressContactMessageThumbLand;
        SimpleDraweeView gifViewContactMessageThumbLand;
        ImageView videoIconContactMessageThumbLand;
        TextView videoTimecontentContactMessageThumbLand;
        private ImageView forwardContactPreviewLandscape;

        RoundedImageView contentContactMessageThumbPort;
        ImageView gifIconContactMessageThumbPort;
        ProgressBar gifProgressContactMessageThumbPort;
        SimpleDraweeView gifViewContactMessageThumbPort;
        ImageView videoIconContactMessageThumbPort;
        TextView videoTimecontentContactMessageThumbPort;
        private ImageView forwardContactPreviewPortrait;

        RelativeLayout contentContactMessageAttachLayout;

        RelativeLayout contentContactMessageFile;
        private ImageView forwardContactFile;
        ImageView contentContactMessageFileThumb;
        TextView contentContactMessageFileName;
        TextView contentContactMessageFileSize;

        RelativeLayout layoutAvatarMessages;

        RelativeLayout contentContactMessageContactLayout;
        private ImageView forwardContactContact;
        RelativeLayout contentContactMessageContactLayoutAvatar;
        SimpleDraweeView contentContactMessageContactThumb;
        private ImageView contentContactMessageContactVerified;
        private ImageView contentContactMessageContactStatus;
        private EmojiTextView contentContactMessageContactName;
        public EmojiTextView contentContactMessageContactEmail;

        //Others contact link
        private RelativeLayout othersContactLinkMessage;
        private EmojiTextView othersContactLinkText;
        private EmojiTextView othersContactLinkName;
        private EmojiTextView othersContactLinkSubtitle;
        private SimpleDraweeView othersContactLinkAvatar;
        private ImageView othersContactLinkForward;

        RelativeLayout contentContactMessageVoiceClipLayout;
        ImageView contentContactMessageVoiceClipPlay;
        private DetectorSeekBar contentContactMessageVoiceClipSeekBar;
        TextView contentContactMessageVoiceClipDuration;

        ImageView iconContactTypeDocLandPreview;
        ImageView iconContactTypeDocPortraitPreview;

        RelativeLayout ownManagementMessageLayout;
        private EmojiTextView ownManagementMessageText;
        ImageView ownManagementMessageIcon;

        private EmojiTextView contactManagementMessageText;
        ImageView contactManagementMessageIcon;
        RelativeLayout contactManagementMessageLayout;

        //Location message
        private ImageView forwardContactMessageLocation;
        RelativeLayout mainContactMessageItemLocation;
        RoundedImageView previewContactLocation;
        RelativeLayout separatorPreviewContactLocation;
        TextView pinnedContactLocationTitleText;
        RelativeLayout pinnedContactLocationLayout;
        TextView pinnedContactLocationInfoText;

        public String filePathUploading;

        public long getUserHandle() {
            return userHandle;
        }
    }

    public static class ViewHolderHeaderChat extends RecyclerView.ViewHolder {
        public ViewHolderHeaderChat(View view) {
            super(view);
        }

        RelativeLayout firstMessage;
        RelativeLayout scheduledMeetingLayout;
        TextView scheduledMeetingTitle;
        TextView scheduledMeetingMessage;
        ImageView loadingMessages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item_chat, parent, false);
            ViewHolderHeaderChat holder = new ViewHolderHeaderChat(v);

            holder.firstMessage = v.findViewById(R.id.first_message_chat);
            holder.scheduledMeetingLayout = v.findViewById(R.id.scheduled_meeting_info_layout);
            holder.scheduledMeetingTitle = v.findViewById(R.id.scheduled_meeting_title);
            holder.scheduledMeetingMessage = v.findViewById(R.id.scheduled_meeting_info);
            holder.loadingMessages = v.findViewById(R.id.loading_messages_image);

            return holder;
        } else {
            Timber.d("Create item message");
            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            cC = new ChatController(context);

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_chat, parent, false);
            ViewHolderMessageChat holder = new ViewHolderMessageChat(v);
            holder.contentVisible = true;
            holder.itemLayout = v.findViewById(R.id.message_chat_item_layout);
            holder.dateLayout = v.findViewById(R.id.message_chat_date_layout);
            //Margins
            RelativeLayout.LayoutParams dateLayoutParams = (RelativeLayout.LayoutParams) holder.dateLayout.getLayoutParams();
            dateLayoutParams.setMargins(0, scaleHeightPx(8, outMetrics), 0, scaleHeightPx(8, outMetrics));
            holder.dateLayout.setLayoutParams(dateLayoutParams);

            holder.dateText = v.findViewById(R.id.message_chat_date_text);

            holder.newMessagesLayout = v.findViewById(R.id.message_chat_new_relative_layout);
            holder.newMessagesText = v.findViewById(R.id.message_chat_new_text);

            if (((ChatActivity) context).getDeviceDensity() == 1) {

                MANAGEMENT_MESSAGE_CALL_LAND = 45;
                MANAGEMENT_MESSAGE_CALL_PORT = 65;

                CONTACT_MESSAGE_LAND = 31;
                CONTACT_MESSAGE_PORT = 55;

                PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND = 10;
                PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT = 18;
            }

            //Own messages

            holder.ownMessageLayout = v.findViewById(R.id.message_chat_own_message_layout);
            holder.titleOwnMessage = v.findViewById(R.id.title_own_message_layout);
            holder.timeOwnText = v.findViewById(R.id.message_chat_time_text);

            holder.titleOwnMessage.setGravity(Gravity.RIGHT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }

            holder.ownMessageReactionsLayout = v.findViewById(R.id.own_message_reactions_layout);
            holder.ownMessageReactionsRecycler = v.findViewById(R.id.own_message_reactions_recycler);
            holder.ownMessageReactionsRecycler.initialization(true);
            ((SimpleItemAnimator) holder.ownMessageReactionsRecycler.getItemAnimator()).setSupportsChangeAnimations(true);
            holder.ownMessageReactionsRecycler.setHasFixedSize(true);
            holder.ownMessageReactionsRecycler.getItemAnimator().setChangeDuration(0);
            holder.ownMessageReactionsRecycler.addItemDecoration(itemDecorationOwn);
            holder.ownMessageReactionsLayout.setVisibility(View.GONE);

            holder.previewFramePort = v.findViewById(R.id.preview_frame_portrait);
            holder.previewFrameLand = v.findViewById(R.id.preview_frame_landscape);

            holder.contentOwnMessageLayout = v.findViewById(R.id.content_own_message_layout);
            holder.ownMessageSelectLayout = v.findViewById(R.id.own_message_select_layout);
            holder.ownMessageSelectIcon = v.findViewById(R.id.own_message_select_icon);
            holder.ownMessageSelectLayout.setVisibility(View.GONE);

            holder.contentOwnMessageText = v.findViewById(R.id.content_own_message_text);
            holder.contentOwnMessageText.setTag(holder);
            holder.contentOwnMessageText.setOnClickListener(this);
            holder.contentOwnMessageText.setNeccessaryShortCode(false);

            //Own rich links message
            holder.urlOwnMessageLayout = v.findViewById(R.id.url_own_message_layout);
            holder.urlOwnMessageLayout.setVisibility(View.GONE);
            holder.urlOwnMessageTextrl = v.findViewById(R.id.url_own_message_text_rl);

            if (((ChatActivity) context).getDeviceDensity() == 1 && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                holder.urlOwnMessageLayout.getLayoutParams().width = LAYOUT_WIDTH;
            }
            holder.forwardOwnRichLinks = v.findViewById(R.id.forward_own_rich_links);
            holder.forwardOwnRichLinks.setTag(holder);
            holder.forwardOwnRichLinks.setVisibility(View.GONE);

            holder.urlOwnMessageText = v.findViewById(R.id.url_own_message_text);
            holder.urlOwnMessageText.setNeccessaryShortCode(false);
            holder.urlOwnMessageText.setTag(holder);

            holder.urlOwnMessageWarningButtonsLayout = v.findViewById(R.id.url_own_message_buttons_warning_layout);
            holder.neverRichLinkButton = v.findViewById(R.id.url_never_button);
            holder.alwaysAllowRichLinkButton = v.findViewById(R.id.url_always_allow_button);
            holder.notNowRichLinkButton = v.findViewById(R.id.url_not_now_button);

            holder.urlOwnMessageDisableButtonsLayout = v.findViewById(R.id.url_own_message_buttons_disable_layout);
            holder.yesDisableButton = v.findViewById(R.id.url_yes_disable_button);
            holder.noDisableButton = v.findViewById(R.id.url_no_disable_button);
            holder.urlOwnMessageTitleLayout = v.findViewById(R.id.url_own_message_enable_layout_inside);
            holder.urlOwnMessageTitle = v.findViewById(R.id.url_own_message_title);
            holder.urlOwnMessageDescription = v.findViewById(R.id.url_own_message_description);

            holder.urlOwnMessageIconAndLinkLayout = v.findViewById(R.id.url_own_message_icon_link_layout);
            holder.urlOwnMessageIcon = v.findViewById(R.id.url_own_message_icon);
            holder.urlOwnMessageLink = v.findViewById(R.id.url_own_message_link);

            holder.urlOwnMessageImage = v.findViewById(R.id.url_own_message_image);
            int radiusImageRL = scaleWidthPx(10, outMetrics);
            holder.urlOwnMessageImage.setCornerRadius(radiusImageRL);
            holder.urlOwnMessageImage.setBorderWidth(0);
            holder.urlOwnMessageImage.setOval(false);

            //Group avatar of chat links
            holder.urlOwnMessageGroupAvatarLayout = v.findViewById(R.id.url_chat_own_message_image);
            holder.urlOwnMessageGroupAvatar = v.findViewById(R.id.content_url_chat_own_message_contact_thumb);
            holder.urlOwnMessageGroupAvatarText = v.findViewById(R.id.content_url_chat_own_message_contact_initial_letter);

            //Group avatar of chat links
            holder.urlContactMessageGroupAvatarLayout = v.findViewById(R.id.url_chat_contact_message_image);
            holder.urlContactMessageGroupAvatar = v.findViewById(R.id.content_url_chat_contact_message_contact_thumb);
            holder.urlContactMessageGroupAvatarText = v.findViewById(R.id.content_url_chat_contact_message_contact_initial_letter);

            int radius = dp2px(10, outMetrics);
            int colors[] = {0x70000000, 0x00000000};
            GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadii(new float[]{radius, 0, radius, 0, radius, radius, radius, radius});

            holder.contentOwnMessageThumbLand = v.findViewById(R.id.content_own_message_thumb_landscape);
            holder.contentOwnMessageThumbLand.setCornerRadius(radius);
            holder.contentOwnMessageThumbLand.setBorderWidth(Util.dp2px(1, outMetrics));
            holder.contentOwnMessageThumbLand.setBorderColor(ContextCompat.getColor(context, R.color.grey_012_white_012));
            holder.contentOwnMessageThumbLand.setOval(false);

            holder.gifIconOwnMessageThumbLand = v.findViewById(R.id.content_own_message_thumb_landscape_gif);
            holder.gifProgressOwnMessageThumbLand = v.findViewById(R.id.content_own_message_thumb_landscape_gif_progressbar);
            holder.gifViewOwnMessageThumbLand = v.findViewById(R.id.content_own_message_thumb_landscape_gif_view);
            holder.gifViewOwnMessageThumbLand.setTag(holder);
            holder.gifViewOwnMessageThumbLand.setOnClickListener(this);
            holder.gifViewOwnMessageThumbLand.setOnLongClickListener(this);

            holder.videoIconOwnMessageThumbLand = v.findViewById(R.id.video_icon_own_message_thumb_landscape);
            holder.videoTimecontentOwnMessageThumbLand = v.findViewById(R.id.video_time_own_message_thumb_landscape);

            holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
            holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

            holder.contentOwnMessageThumbPort = v.findViewById(R.id.content_own_message_thumb_portrait);
            holder.contentOwnMessageThumbPort.setCornerRadius(radius);
            holder.contentOwnMessageThumbPort.setBorderWidth(Util.dp2px(1, outMetrics));
            holder.contentOwnMessageThumbPort.setBorderColor(ContextCompat.getColor(context, R.color.grey_012_white_012));
            holder.contentOwnMessageThumbPort.setOval(false);

            holder.gifIconOwnMessageThumbPort = v.findViewById(R.id.content_own_message_thumb_portrait_gif);
            holder.gifProgressOwnMessageThumbPort = v.findViewById(R.id.content_own_message_thumb_portrait_gif_progressbar);
            holder.gifViewOwnMessageThumbPort = v.findViewById(R.id.content_own_message_thumb_portrait_gif_view);
            holder.gifViewOwnMessageThumbPort.setTag(holder);
            holder.gifViewOwnMessageThumbPort.setOnClickListener(this);
            holder.gifViewOwnMessageThumbPort.setOnLongClickListener(this);

            holder.errorUploadingFile = v.findViewById(R.id.error_uploading_file);
            holder.errorUploadingContact = v.findViewById(R.id.error_uploading_contact);
            holder.errorUploadingRichLink = v.findViewById(R.id.error_uploading_rich_link);

            holder.videoIconOwnMessageThumbPort = v.findViewById(R.id.video_icon_own_message_thumb_portrait);
            holder.videoTimecontentOwnMessageThumbPort = v.findViewById(R.id.video_time_own_message_thumb_portrait);

            holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
            holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

            holder.contentOwnMessageFileLayout = v.findViewById(R.id.content_own_message_file_layout);
            holder.forwardOwnFile = v.findViewById(R.id.forward_own_file);
            holder.forwardOwnFile.setTag(holder);
            holder.forwardOwnFile.setVisibility(View.GONE);

            holder.contentOwnMessageFileThumb = v.findViewById(R.id.content_own_message_file_thumb);
            holder.contentOwnMessageFileName = v.findViewById(R.id.content_own_message_file_name);
            holder.contentOwnMessageFileSize = v.findViewById(R.id.content_own_message_file_size);

            holder.totalDurationOfVoiceClip = 0;

            //my voice clip:
            holder.contentOwnMessageVoiceClipLayout = v.findViewById(R.id.content_own_message_voice_clip_layout);
            holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
            holder.contentOwnMessageVoiceClipPlay = v.findViewById(R.id.content_own_message_voice_clip_play_pause);
            holder.contentOwnMessageVoiceClipPlay.setTag(holder);
            holder.contentOwnMessageVoiceClipSeekBar = v.findViewById(R.id.content_own_message_voice_clip_seekBar);
            holder.contentOwnMessageVoiceClipSeekBar.setProgress(0);
            holder.contentOwnMessageVoiceClipDuration = v.findViewById(R.id.content_own_message_voice_clip_duration);
            holder.contentOwnMessageVoiceClipDuration.setText(milliSecondsToTimer(0));
            holder.uploadingOwnProgressbarVoiceclip = v.findViewById(R.id.uploading_own_progressbar_voiceclip);
            holder.uploadingOwnProgressbarVoiceclip.setVisibility(View.GONE);

            holder.notAvailableOwnVoiceclip = v.findViewById(R.id.content_own_message_voice_clip_not_available);
            holder.notAvailableOwnVoiceclip.setVisibility(View.GONE);
            holder.notAvailableOwnVoiceclip.setTag(holder);
            holder.notAvailableOwnVoiceclip.setOnClickListener(this);

            holder.errorUploadingVoiceClip = v.findViewById(R.id.error_uploading_voice_clip);

            holder.contentOwnMessageContactLayout = v.findViewById(R.id.content_own_message_contact_layout);
            holder.contentOwnMessageContactLayoutAvatar = v.findViewById(R.id.content_own_message_contact_layout_avatar);
            holder.contentOwnMessageContactThumb = v.findViewById(R.id.content_own_message_contact_thumb);
            holder.contentOwnMessageContactVerified = v.findViewById(R.id.content_own_message_contact_verified_icon);
            holder.contentOwnMessageContactStatus = v.findViewById(R.id.content_own_message_contact_status);
            holder.contentOwnMessageContactName = v.findViewById(R.id.content_own_message_contact_name);
            holder.contentOwnMessageContactName.setNeccessaryShortCode(false);
            holder.contentOwnMessageContactEmail = v.findViewById(R.id.content_own_message_contact_email);

            holder.forwardOwnContact = v.findViewById(R.id.forward_own_contact);
            holder.forwardOwnContact.setTag(holder);
            holder.forwardOwnContact.setVisibility(View.GONE);

            holder.iconOwnTypeDocLandPreview = v.findViewById(R.id.own_attachment_type_icon_lands);
            holder.iconOwnTypeDocPortraitPreview = v.findViewById(R.id.own_attachment_type_icon_portrait);

            holder.retryAlert = v.findViewById(R.id.not_sent_own_message_text);
            holder.triangleIcon = v.findViewById(R.id.own_triangle_icon);

            holder.transparentCoatingPortrait = v.findViewById(R.id.transparent_coating_portrait);
            holder.transparentCoatingPortrait.setVisibility(View.GONE);

            holder.transparentCoatingLandscape = v.findViewById(R.id.transparent_coating_landscape);
            holder.transparentCoatingLandscape.setVisibility(View.GONE);

            holder.uploadingProgressBarPort = v.findViewById(R.id.uploadingProgressBarPort);
            holder.uploadingProgressBarPort.setVisibility(View.GONE);
            holder.uploadingProgressBarLand = v.findViewById(R.id.uploadingProgressBarLand);
            holder.uploadingProgressBarLand.setVisibility(View.GONE);

            holder.errorUploadingPortrait = v.findViewById(R.id.error_uploading_portrait);
            holder.errorUploadingPortrait.setVisibility(View.GONE);
            holder.errorUploadingLandscape = v.findViewById(R.id.error_uploading_landscape);
            holder.errorUploadingLandscape.setVisibility(View.GONE);

            holder.forwardOwnPortrait = v.findViewById(R.id.forward_own_preview_portrait);
            holder.forwardOwnPortrait.setTag(holder);

            holder.forwardOwnPortrait.setVisibility(View.GONE);
            holder.forwardOwnPortrait.setOnClickListener(this);

            holder.forwardOwnLandscape = v.findViewById(R.id.forward_own_preview_landscape);
            holder.forwardOwnLandscape.setTag(holder);
            holder.forwardOwnLandscape.setVisibility(View.GONE);
            holder.forwardOwnLandscape.setOnClickListener(this);

            holder.ownManagementMessageText = v.findViewById(R.id.own_management_message_text);
            holder.ownManagementMessageText.setNeccessaryShortCode(false);
            holder.ownManagementMessageLayout = v.findViewById(R.id.own_management_message_layout);
            holder.ownManagementMessageIcon = v.findViewById(R.id.own_management_message_icon);

            holder.ownContactLinkMessage = v.findViewById(R.id.own_contact_link_container);
            holder.ownContactLinkMessage.setTag(holder);
            holder.ownContactLinkMessage.setOnClickListener(this);
            holder.ownContactLinkMessage.setOnLongClickListener(this);
            holder.ownContactLinkTextLayout = v.findViewById(R.id.own_contact_link_text_layout);
            holder.ownContactLinkText = v.findViewById(R.id.own_contact_link_text);
            holder.ownContactLinkName = v.findViewById(R.id.own_contact_link_name);
            holder.ownContactLinkSubtitle = v.findViewById(R.id.own_contact_link_subtitle);
            holder.ownContactLinkAvatar = v.findViewById(R.id.own_contact_link_avatar);
            holder.ownContactLinkForward = v.findViewById(R.id.own_contact_link_forward);
            holder.ownContactLinkForward.setTag(holder);
            holder.ownContactLinkForward.setOnClickListener(this);
            holder.ownContactLinkError = v.findViewById(R.id.own_contact_link_error_uploading);

            //Contact messages
            holder.contactMessageLayout = v.findViewById(R.id.message_chat_contact_message_layout);
            holder.titleContactMessage = v.findViewById(R.id.title_contact_message_layout);

            holder.contactImageView = v.findViewById(R.id.contact_thumbnail);
            holder.timeContactText = v.findViewById(R.id.contact_message_chat_time_text);
            holder.nameContactText = v.findViewById(R.id.contact_message_chat_name_text);

            holder.contentContactMessageLayout = v.findViewById(R.id.content_contact_message_layout);
            holder.contactMessageSelectLayout = v.findViewById(R.id.contact_message_select_layout);
            holder.contactMessageSelectIcon = v.findViewById(R.id.contact_message_select_icon);
            holder.contactMessageSelectLayout.setVisibility(View.GONE);

            holder.contentContactMessageText = v.findViewById(R.id.content_contact_message_text);
            holder.contentContactMessageText.setNeccessaryShortCode(false);

            holder.contactMessageReactionsLayout = v.findViewById(R.id.contact_message_reactions_layout);
            holder.contactMessageReactionsRecycler = v.findViewById(R.id.contact_message_reactions_recycler);
            holder.contactMessageReactionsRecycler.initialization(false);
            ((SimpleItemAnimator) holder.contactMessageReactionsRecycler.getItemAnimator()).setSupportsChangeAnimations(true);
            holder.contactMessageReactionsRecycler.setHasFixedSize(true);
            holder.contactMessageReactionsRecycler.getItemAnimator().setChangeDuration(0);
            holder.contactMessageReactionsRecycler.addItemDecoration(itemDecorationContact);
            holder.contactMessageReactionsLayout.setVisibility(View.GONE);

            holder.contentContactMessageThumbLand = v.findViewById(R.id.content_contact_message_thumb_landscape);
            holder.contentContactMessageThumbLand.setCornerRadius(radius);
            holder.contentContactMessageThumbLand.setBorderWidth(1);
            holder.contentContactMessageThumbLand.setBorderColor(ContextCompat.getColor(context, R.color.grey_012_white_012));
            holder.contentContactMessageThumbLand.setOval(false);

            holder.gifIconContactMessageThumbLand = v.findViewById(R.id.content_contact_message_thumb_landscape_gif);
            holder.gifProgressContactMessageThumbLand = v.findViewById(R.id.content_contact_message_thumb_landscape_gif_progressbar);
            holder.gifViewContactMessageThumbLand = v.findViewById(R.id.content_contact_message_thumb_landscape_gif_view);
            holder.gifViewContactMessageThumbLand.setTag(holder);
            holder.gifViewContactMessageThumbLand.setOnClickListener(this);
            holder.gifViewContactMessageThumbLand.setOnLongClickListener(this);

            holder.forwardContactPreviewLandscape = v.findViewById(R.id.forward_contact_preview_landscape);
            holder.forwardContactPreviewLandscape.setTag(holder);
            holder.forwardContactPreviewLandscape.setVisibility(View.GONE);

            //Contact rich links message
            holder.urlContactMessageLayout = v.findViewById(R.id.url_contact_message_layout);
            if (((ChatActivity) context).getDeviceDensity() == 1 && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                holder.urlContactMessageLayout.getLayoutParams().width = LAYOUT_WIDTH;
            }

            holder.forwardContactRichLinks = v.findViewById(R.id.forward_contact_rich_links);
            holder.forwardContactRichLinks.setTag(holder);
            holder.forwardContactRichLinks.setVisibility(View.GONE);

            holder.urlContactMessageText = v.findViewById(R.id.url_contact_message_text);
            holder.urlContactMessageText.setNeccessaryShortCode(false);
            holder.urlContactMessageText.setTag(holder);

            holder.urlContactMessageTitleLayout = v.findViewById(R.id.url_contact_message_enable_layout_inside);
            holder.urlContactMessageTitle = v.findViewById(R.id.url_contact_message_title);
            holder.urlContactMessageDescription = v.findViewById(R.id.url_contact_message_description);

            holder.urlContactMessageIconAndLinkLayout = v.findViewById(R.id.url_contact_message_icon_link_layout);
            holder.urlContactMessageIcon = v.findViewById(R.id.url_contact_message_icon);
            holder.urlContactMessageLink = v.findViewById(R.id.url_contact_message_link);

            holder.urlContactMessageImage = v.findViewById(R.id.url_contact_message_image);
            holder.urlContactMessageImage.setCornerRadius(radiusImageRL);
            holder.urlContactMessageImage.setBorderWidth(0);
            holder.urlContactMessageImage.setOval(false);

            holder.contentContactMessageThumbPort = v.findViewById(R.id.content_contact_message_thumb_portrait);
            holder.contentContactMessageThumbPort.setCornerRadius(radius);
            holder.contentContactMessageThumbPort.setBorderWidth(1);
            holder.contentContactMessageThumbPort.setBorderColor(ContextCompat.getColor(context, R.color.grey_012_white_012));
            holder.contentContactMessageThumbPort.setOval(false);

            holder.gifIconContactMessageThumbPort = v.findViewById(R.id.content_contact_message_thumb_portrait_gif);
            holder.gifProgressContactMessageThumbPort = v.findViewById(R.id.content_contact_message_thumb_portrait_gif_progressbar);
            holder.gifViewContactMessageThumbPort = v.findViewById(R.id.content_contact_message_thumb_portrait_gif_view);
            holder.gifViewContactMessageThumbPort.setTag(holder);
            holder.gifViewContactMessageThumbPort.setOnClickListener(this);
            holder.gifViewContactMessageThumbPort.setOnLongClickListener(this);

            holder.forwardContactPreviewPortrait = v.findViewById(R.id.forward_contact_preview_portrait);
            holder.forwardContactPreviewPortrait.setTag(holder);
            holder.forwardContactPreviewPortrait.setVisibility(View.GONE);

            holder.videoIconContactMessageThumbLand = v.findViewById(R.id.video_icon_contact_message_thumb_landscape);
            holder.videoTimecontentContactMessageThumbLand = v.findViewById(R.id.video_time_contact_message_thumb_landscape);

            holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
            holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

            holder.videoIconContactMessageThumbPort = v.findViewById(R.id.video_icon_contact_message_thumb_portrait);
            holder.videoTimecontentContactMessageThumbPort = v.findViewById(R.id.video_time_contact_message_thumb_portrait);

            holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
            holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);

            holder.contentContactMessageAttachLayout = v.findViewById(R.id.content_contact_message_attach_layout);

            holder.contentContactMessageFile = v.findViewById(R.id.content_contact_message_file);
            holder.forwardContactFile = v.findViewById(R.id.forward_contact_file);
            holder.forwardContactFile.setTag(holder);
            holder.forwardContactFile.setVisibility(View.GONE);
            holder.contentContactMessageFileThumb = v.findViewById(R.id.content_contact_message_file_thumb);
            holder.contentContactMessageFileName = v.findViewById(R.id.content_contact_message_file_name);
            holder.contentContactMessageFileSize = v.findViewById(R.id.content_contact_message_file_size);

            holder.layoutAvatarMessages = v.findViewById(R.id.layout_avatar);
            holder.contentContactMessageContactLayout = v.findViewById(R.id.content_contact_message_contact_layout);

            //contact voice clip:
            holder.contentContactMessageVoiceClipLayout = v.findViewById(R.id.content_contact_message_voice_clip_layout);
            RelativeLayout.LayoutParams paramsVoiceClip = (RelativeLayout.LayoutParams) holder.contentContactMessageVoiceClipLayout.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsVoiceClip.leftMargin = scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics);
            } else {
                paramsVoiceClip.leftMargin = scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics);
            }
            holder.contentContactMessageVoiceClipLayout.setLayoutParams(paramsVoiceClip);
            holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);
            holder.contentContactMessageVoiceClipPlay = v.findViewById(R.id.content_contact_message_voice_clip_play_pause);
            holder.contentContactMessageVoiceClipPlay.setTag(holder);
            holder.contentContactMessageVoiceClipSeekBar = v.findViewById(R.id.content_contact_message_voice_clip_seekBar);
            holder.contentContactMessageVoiceClipSeekBar.setProgress(0);
            holder.contentContactMessageVoiceClipDuration = v.findViewById(R.id.content_contact_message_voice_clip_duration);
            holder.contentContactMessageVoiceClipDuration.setText(milliSecondsToTimer(0));
            holder.uploadingContactProgressbarVoiceclip = v.findViewById(R.id.uploading_contact_progressbar_voiceclip);
            holder.uploadingContactProgressbarVoiceclip.setVisibility(View.GONE);
            holder.notAvailableContactVoiceclip = v.findViewById(R.id.content_contact_message_voice_clip_not_available);
            holder.notAvailableContactVoiceclip.setVisibility(View.GONE);
            holder.notAvailableContactVoiceclip.setTag(holder);
            holder.notAvailableContactVoiceclip.setOnClickListener(this);

            holder.forwardContactContact = v.findViewById(R.id.forward_contact_contact);
            holder.forwardContactContact.setTag(holder);
            holder.forwardContactContact.setVisibility(View.GONE);

            holder.contentContactMessageContactLayoutAvatar = v.findViewById(R.id.content_contact_message_contact_layout_avatar);
            holder.contentContactMessageContactThumb = v.findViewById(R.id.content_contact_message_contact_thumb);
            holder.contentContactMessageContactVerified = v.findViewById(R.id.content_contact_message_contact_verified_icon);
            holder.contentContactMessageContactStatus = v.findViewById(R.id.content_contact_message_contact_status);
            holder.contentContactMessageContactName = v.findViewById(R.id.content_contact_message_contact_name);
            holder.contentContactMessageContactName.setNeccessaryShortCode(false);
            holder.contentContactMessageContactEmail = v.findViewById(R.id.content_contact_message_contact_email);

            holder.iconContactTypeDocLandPreview = v.findViewById(R.id.contact_attachment_type_icon_lands);
            holder.iconContactTypeDocPortraitPreview = v.findViewById(R.id.contact_attachment_type_icon_portrait);

            holder.contactManagementMessageLayout = v.findViewById(R.id.contact_management_message_layout);
            holder.contactManagementMessageText = v.findViewById(R.id.contact_management_message_text);
            holder.contactManagementMessageText.setNeccessaryShortCode(false);
            holder.contactManagementMessageIcon = v.findViewById(R.id.contact_management_message_icon);

            //Location message
            holder.transparentCoatingLocation = v.findViewById(R.id.transparent_coating_location);
            holder.uploadingProgressBarLocation = v.findViewById(R.id.uploadingProgressBarLocation);
            holder.forwardOwnMessageLocation = v.findViewById(R.id.forward_own_location);
            holder.forwardOwnMessageLocation.setTag(holder);
            holder.forwardOwnMessageLocation.setVisibility(View.GONE);
            holder.mainOwnMessageItemLocation = v.findViewById(R.id.own_main_item_location);
            holder.previewOwnLocation = v.findViewById(R.id.own_rounded_imageview_location);
            holder.previewOwnLocation.setCornerRadius(dp2px(12, outMetrics));
            holder.previewOwnLocation.setBorderWidth(0);
            holder.previewOwnLocation.setOval(false);
            holder.separatorPreviewOwnLocation = v.findViewById(R.id.own_separator_imageview_location);

            if (((ChatActivity) context).getDeviceDensity() == 1 && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                holder.previewOwnLocation.getLayoutParams().width = LAYOUT_WIDTH;
                holder.separatorPreviewOwnLocation.getLayoutParams().width = LAYOUT_WIDTH;
            }

            holder.triangleErrorLocation = v.findViewById(R.id.error_uploading_location);

            holder.pinnedOwnLocationLayout = v.findViewById(R.id.own_pinned_location_layout);
            holder.pinnedOwnLocationInfoText = v.findViewById(R.id.own_info_pinned_location);
            holder.pinnedLocationTitleText = v.findViewById(R.id.own_title_pinned_location);

            holder.forwardContactMessageLocation = v.findViewById(R.id.forward_contact_location);
            holder.forwardContactMessageLocation.setTag(holder);
            holder.forwardContactMessageLocation.setVisibility(View.GONE);
            holder.mainContactMessageItemLocation = v.findViewById(R.id.contact_main_item_location);
            holder.previewContactLocation = v.findViewById(R.id.contact_rounded_imageview_location);
            holder.previewContactLocation.setCornerRadius(dp2px(12, outMetrics));
            holder.previewContactLocation.setBorderWidth(0);
            holder.previewContactLocation.setOval(false);
            holder.separatorPreviewContactLocation = v.findViewById(R.id.contact_separator_imageview_location);
            if (((ChatActivity) context).getDeviceDensity() == 1 && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                holder.previewContactLocation.getLayoutParams().width = LAYOUT_WIDTH;
                holder.separatorPreviewContactLocation.getLayoutParams().width = LAYOUT_WIDTH;
            }

            holder.pinnedContactLocationTitleText = v.findViewById(R.id.contact_title_pinned_location);
            holder.pinnedContactLocationLayout = v.findViewById(R.id.contact_pinned_location_layout);
            holder.pinnedContactLocationInfoText = v.findViewById(R.id.contact_info_pinned_location);

            RelativeLayout.LayoutParams paramsLocation = (RelativeLayout.LayoutParams) holder.mainContactMessageItemLocation.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsLocation.leftMargin = scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics);
            } else {
                paramsLocation.leftMargin = scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics);
            }
            holder.mainContactMessageItemLocation.setLayoutParams(paramsLocation);

            if (((ChatActivity) context).getDeviceDensity() == 1) {
                MAX_WIDTH_FILENAME_LAND = 290;
                MAX_WIDTH_FILENAME_PORT = 140;
            }

            RelativeLayout.LayoutParams paramsContactContact = (RelativeLayout.LayoutParams) holder.contentContactMessageContactLayout.getLayoutParams();
            RelativeLayout.LayoutParams paramsContactManagement = (RelativeLayout.LayoutParams) holder.contactManagementMessageText.getLayoutParams();
            RelativeLayout.LayoutParams paramsContactAttach = (RelativeLayout.LayoutParams) holder.contentContactMessageAttachLayout.getLayoutParams();
            RelativeLayout.LayoutParams paramsContactRichLink = (RelativeLayout.LayoutParams) holder.urlContactMessageLayout.getLayoutParams();

            RelativeLayout.LayoutParams paramsOwnManagement = (RelativeLayout.LayoutParams) holder.ownManagementMessageText.getLayoutParams();

            if (!isScreenInPortrait(context)) {
                paramsContactContact.leftMargin = scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics);
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
                paramsContactManagement.rightMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
                paramsContactAttach.leftMargin = scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics);
                paramsContactRichLink.leftMargin = scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics);

                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
                paramsOwnManagement.rightMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
                holder.nameContactText.setMaxWidthEmojis(dp2px(MAX_WIDTH_NAME_LAND, outMetrics));
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                paramsContactContact.leftMargin = scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics);
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
                paramsContactManagement.rightMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
                paramsContactAttach.leftMargin = scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics);
                paramsContactRichLink.leftMargin = scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics);
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
                paramsOwnManagement.rightMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);

                holder.nameContactText.setMaxWidthEmojis(dp2px(MAX_WIDTH_NAME_PORT, outMetrics));
                holder.titleContactMessage.setPadding(dp2px(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }

            holder.contentContactMessageContactLayout.setLayoutParams(paramsContactContact);
            holder.contactManagementMessageText.setLayoutParams(paramsContactManagement);
            holder.contentContactMessageAttachLayout.setLayoutParams(paramsContactAttach);
            holder.urlContactMessageLayout.setLayoutParams(paramsContactRichLink);
            holder.ownManagementMessageText.setLayoutParams(paramsOwnManagement);

            holder.othersContactLinkMessage = v.findViewById(R.id.others_contact_link_container);
            holder.othersContactLinkMessage.setTag(holder);
            holder.othersContactLinkMessage.setOnClickListener(this);
            holder.othersContactLinkMessage.setOnLongClickListener(this);
            holder.othersContactLinkText = v.findViewById(R.id.others_contact_link_text);
            holder.othersContactLinkName = v.findViewById(R.id.others_contact_link_name);
            holder.othersContactLinkSubtitle = v.findViewById(R.id.others_contact_link_subtitle);
            holder.othersContactLinkAvatar = v.findViewById(R.id.others_contact_link_avatar);
            holder.othersContactLinkForward = v.findViewById(R.id.others_contact_link_forward);
            holder.othersContactLinkForward.setTag(holder);
            holder.othersContactLinkForward.setOnClickListener(this);

            v.setTag(holder);

            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderHeaderChat) {
            ViewHolderHeaderChat holderHeaderChat = (ViewHolderHeaderChat) holder;

            boolean isFullHistoryLoaded = megaChatApi.isFullHistoryLoaded(chatRoom.getChatId());
            holderHeaderChat.firstMessage.setVisibility(isFullHistoryLoaded ? View.VISIBLE : View.GONE);

            ChatScheduledMeeting scheduledMeeting = viewModel.getMeeting();
            if (scheduledMeeting != null) {
                holderHeaderChat.scheduledMeetingLayout.setVisibility(View.VISIBLE);
                holderHeaderChat.scheduledMeetingMessage.setText(getAppropriateStringForScheduledMeetingDate(context, viewModel.is24HourFormat(), scheduledMeeting));
                holderHeaderChat.scheduledMeetingTitle.setText(getTitleChat(megaChatApi.getChatRoom(chatRoom.getChatId())));
            } else {
                holderHeaderChat.scheduledMeetingLayout.setVisibility(View.GONE);
            }

            holderHeaderChat.loadingMessages.setVisibility(isFullHistoryLoaded ? View.GONE : View.VISIBLE);
            holderHeaderChat.loadingMessages.setImageDrawable(ContextCompat.getDrawable(context,
                    isScreenInPortrait(context) ? R.drawable.loading_chat_messages : R.drawable.loading_chat_messages_landscape));
        } else {
            hideLayoutsGiphyAndGifMessages(position, (ViewHolderMessageChat) holder);
            hideForwardOptions(position, (ViewHolderMessageChat) holder);
            hideLayoutsContactLinkMessages(position, (ViewHolderMessageChat) holder);
            hideLayoutsVoiceClipMessages(position, (ViewHolderMessageChat) holder);
            hideLayoutsNormalMessages(position, (ViewHolderMessageChat) holder);
            hideLayoutsAttachmentMessages(position, (ViewHolderMessageChat) holder);

            AndroidMegaChatMessage androidMessage = messages.get(position - 1);
            if (androidMessage.isUploading()) {
                Timber.d("isUploading");
                onBindViewHolderUploading(holder, position);
            } else {
                Timber.d("isSent");
                onBindViewHolderMessage(holder, position);
            }
        }
    }

    public void onBindViewHolderUploading(RecyclerView.ViewHolder holder, int position) {
        Timber.d("position: %s", position);

        ((ViewHolderMessageChat) holder).itemLayout.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ((ViewHolderMessageChat) holder).itemLayout.setLayoutParams(params);
        ((ViewHolderMessageChat) holder).ownMessageSelectLayout.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).ownMessageReactionsLayout.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).triangleIcon.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingContact.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingFile.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingVoiceClip.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingRichLink.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).retryAlert.setText(R.string.manual_retry_alert);

        ((ViewHolderMessageChat) holder).newMessagesLayout.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).ownManagementMessageLayout.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).titleOwnMessage.setGravity(Gravity.RIGHT);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
        } else {
            ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
        }

        ((ViewHolderMessageChat) holder).contentOwnMessageText.setVisibility(View.VISIBLE);
        ((ViewHolderMessageChat) holder).iconOwnTypeDocLandPreview.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);

        AndroidMegaChatMessage message = messages.get(position - 1);
        ((ViewHolderMessageChat) holder).itemLayout.setTag(holder);
        ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(this);
        ((ViewHolderMessageChat) holder).itemLayout.setOnLongClickListener(this);

        if (message.isUploading()) {
            if (message.getInfoToShow() != -1) {
                setInfoToShow(position, ((ViewHolderMessageChat) holder), true, message.getInfoToShow(),
                        formatDate(message.getPendingMessage().getUploadTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message.getPendingMessage().getUploadTimestamp()));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageText.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).previewFrameLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageThumbLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).previewFramePort.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconOwnMessageThumbPort.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconOwnMessageThumbLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

            hideLayoutsContactAttachmentMessages(position, (ViewHolderMessageChat) holder);

            ((ViewHolderMessageChat) holder).urlOwnMessageLayout.setVisibility(View.GONE);

            hideLayoutsLocationMessages(position, ((ViewHolderMessageChat) holder));

            String path = message.getPendingMessage().getFilePath();
            File voiceClipDir = CacheFolderManager.getCacheFolder(CacheFolderManager.VOICE_CLIP_FOLDER);
            String name = message.getPendingMessage().getName();
            int type = message.getPendingMessage().getType();

            if (areTransfersPaused
                    && message.getPendingMessage().getState() != PendingMessageState.ERROR_UPLOADING.getValue()
                    && message.getPendingMessage().getState() != PendingMessageState.ERROR_ATTACHING.getValue()) {
                ((ViewHolderMessageChat) holder).retryAlert.setText(R.string.manual_resume_alert);
            }

            if (path != null) {
                if (isVoiceClip(path) && (type == TYPE_VOICE_CLIP) || path.contains(voiceClipDir.getAbsolutePath())) {
                    Timber.d("TYPE_VOICE_CLIP - message.getPendingMessage().getState() %s", message.getPendingMessage().getState());
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipLayout.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipLayout.setBackgroundResource(R.drawable.light_rounded_chat_own_message);
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipPlay.setImageResource(R.drawable.ic_play_voice_clip);

                    ((ViewHolderMessageChat) holder).uploadingOwnProgressbarVoiceclip.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipPlay.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).notAvailableOwnVoiceclip.setVisibility(View.GONE);

                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipDuration.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipDuration.setText("--:--");

                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipSeekBar.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipSeekBar.setProgress(0);
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipSeekBar.setEnabled(false);
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);

                    ((ViewHolderMessageChat) holder).contentOwnMessageFileLayout.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileThumb.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileName.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileSize.setVisibility(View.GONE);

                    ((ViewHolderMessageChat) holder).errorUploadingVoiceClip.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.GONE);

                    if (message.getPendingMessage().getState() == PendingMessageState.ERROR_ATTACHING.getValue() || areTransfersPaused) {
                        ((ViewHolderMessageChat) holder).errorUploadingVoiceClip.setVisibility(View.VISIBLE);
                        ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.VISIBLE);
                        ((ViewHolderMessageChat) holder).notAvailableOwnVoiceclip.setVisibility(View.VISIBLE);
                        ((ViewHolderMessageChat) holder).uploadingOwnProgressbarVoiceclip.setVisibility(View.GONE);
                        ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipPlay.setVisibility(View.GONE);
                    }

                } else {
                    ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileLayout.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileThumb.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileName.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileSize.setVisibility(View.VISIBLE);

                    ((ViewHolderMessageChat) holder).filePathUploading = path;

                    Bitmap preview = null;
                    if (MimeTypeList.typeForName(path).isImage() || MimeTypeList.typeForName(path).isPdf() || MimeTypeList.typeForName(path).isVideo()) {
                        ((ViewHolderMessageChat) holder).errorUploadingFile.setVisibility(View.GONE);

                        preview = getPreview(path, context);

                        if (preview != null) {
                            setUploadingPreview((ViewHolderMessageChat) holder, preview);
                        } else {
                            try {
                                new ChatUploadingPreviewAsyncTask(this, position).execute(path);
                            } catch (Exception e) {
                                Timber.w(e, "Error creating preview (Too many AsyncTasks)");
                            }
                        }
                    }

                    if (preview == null && (message.getPendingMessage().getState() == PendingMessageState.ERROR_UPLOADING.getValue() || message.getPendingMessage().getState() == PendingMessageState.ERROR_ATTACHING.getValue()
                            || areTransfersPaused)) {
                        ((ViewHolderMessageChat) holder).errorUploadingFile.setVisibility(View.VISIBLE);
                        ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.VISIBLE);
                    }

                    Timber.d("Node handle: %s", message.getPendingMessage().getNodeHandle());

                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Timber.d("Landscape configuration");
                        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_WIDTH_FILENAME_LAND, context.getResources().getDisplayMetrics());
                        ((ViewHolderMessageChat) holder).contentOwnMessageFileName.setMaxWidth((int) width);
                        ((ViewHolderMessageChat) holder).contentOwnMessageFileSize.setMaxWidth((int) width);
                    } else {
                        Timber.d("Portrait configuration");
                        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_WIDTH_FILENAME_PORT, context.getResources().getDisplayMetrics());
                        ((ViewHolderMessageChat) holder).contentOwnMessageFileName.setMaxWidth((int) width);
                        ((ViewHolderMessageChat) holder).contentOwnMessageFileSize.setMaxWidth((int) width);
                    }

                    ((ViewHolderMessageChat) holder).contentOwnMessageFileName.setText(name);
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileThumb.setImageResource(MimeTypeList.typeForName(name).getIconResourceId());
                    ((ViewHolderMessageChat) holder).contentOwnMessageFileLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.light_rounded_chat_own_message));

                    PendingMessage pendingMsg = message.getPendingMessage();
                    Timber.d("State of the message: %s", pendingMsg.getState());

                    String state = null;

                    if (pendingMsg.getState() == PendingMessageState.ERROR_UPLOADING.getValue()
                            || pendingMsg.getState() == PendingMessageState.ERROR_ATTACHING.getValue()) {
                        state = context.getString(R.string.attachment_uploading_state_error);
                    } else if (pendingMsg.getState() == PendingMessageState.COMPRESSING.getValue()) {
                        state = context.getString(R.string.attachment_uploading_state_compressing);
                    } else if (!areTransfersPaused) {
                        state = context.getString(R.string.attachment_uploading_state_uploading);
                    }

                    if (state != null) {
                        ((ViewHolderMessageChat) holder).contentOwnMessageFileSize.setText(state);
                    }

                    ((ViewHolderMessageChat) holder).contentOwnMessageFileSize.setVisibility(areTransfersPaused ? View.GONE : View.VISIBLE);
                }

            } else {
                Timber.w("Path is null");
            }
        } else {
            Timber.w("ERROR: The message is no UPLOADING");
        }
    }

    private void hideLayoutsLocationMessages(int position, ViewHolderMessageChat holder) {
        if (isHolderNull(position, holder)) {
            return;
        }

        holder.transparentCoatingLocation.setVisibility(View.GONE);
        holder.uploadingProgressBarLocation.setVisibility(View.GONE);
        holder.forwardOwnMessageLocation.setVisibility(View.GONE);
        holder.mainOwnMessageItemLocation.setVisibility(View.GONE);
        holder.previewOwnLocation.setVisibility(View.GONE);
        holder.triangleErrorLocation.setVisibility(View.GONE);
        holder.pinnedOwnLocationLayout.setVisibility(View.GONE);
        holder.pinnedOwnLocationInfoText.setVisibility(View.GONE);

        holder.forwardContactMessageLocation.setVisibility(View.GONE);
        holder.mainContactMessageItemLocation.setVisibility(View.GONE);
        holder.previewContactLocation.setVisibility(View.GONE);
        holder.pinnedContactLocationLayout.setVisibility(View.GONE);
        holder.pinnedContactLocationInfoText.setVisibility(View.GONE);

    }

    public void onBindViewHolderMessage(RecyclerView.ViewHolder holder, int position) {
        Timber.d("Position: %s", position);

        ((ViewHolderMessageChat) holder).itemLayout.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams paramsDefault = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ((ViewHolderMessageChat) holder).itemLayout.setLayoutParams(paramsDefault);

        ((ViewHolderMessageChat) holder).ownMessageReactionsLayout.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).contactMessageReactionsLayout.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).ownMessageSelectLayout.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).contactMessageSelectLayout.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).triangleIcon.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingContact.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingFile.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingVoiceClip.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingRichLink.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).transparentCoatingLandscape.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).transparentCoatingPortrait.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).uploadingProgressBarPort.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).uploadingProgressBarLand.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).errorUploadingPortrait.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).errorUploadingLandscape.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).iconOwnTypeDocLandPreview.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).iconContactTypeDocLandPreview.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).iconContactTypeDocPortraitPreview.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).urlOwnMessageLayout.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).urlContactMessageLayout.setVisibility(View.GONE);

        hideLayoutsLocationMessages(position, ((ViewHolderMessageChat) holder));

        ((ViewHolderMessageChat) holder).contentContactMessageText.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).contentContactMessageVoiceClipLayout.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).contentContactMessageContactLayout.setVisibility(View.GONE);

        ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).contentContactMessageAttachLayout.setVisibility(View.GONE);

        AndroidMegaChatMessage androidMessage = messages.get(position - 1);
        MegaChatMessage message = androidMessage.getMessage();
        ((ViewHolderMessageChat) holder).userHandle = message.getUserHandle();

        int messageType = message.getType();
        Timber.d("Message type: %s", messageType);

        if (isKnownMessage(messageType)) {
            ((ViewHolderMessageChat) holder).itemLayout.setTag(holder);
            ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(this);
            ((ViewHolderMessageChat) holder).itemLayout.setOnLongClickListener(this);
            ((ViewHolderMessageChat) holder).contentContactMessageText.setTag(holder);
            ((ViewHolderMessageChat) holder).contentContactMessageText.setOnClickListener(this);
            ((ViewHolderMessageChat) holder).contentContactMessageText.setOnLongClickListener(this);
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setTag(holder);
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setOnClickListener(this);
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setOnLongClickListener(this);
        } else {
            Timber.w("Not known message: disable click - position: %d", position);
            ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(null);
            ((ViewHolderMessageChat) holder).itemLayout.setOnLongClickListener(null);
            ((ViewHolderMessageChat) holder).contentContactMessageText.setOnClickListener(null);
            ((ViewHolderMessageChat) holder).contentContactMessageText.setOnLongClickListener(null);
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setOnClickListener(null);
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setOnLongClickListener(null);
        }

        if (messageType != MegaChatMessage.TYPE_CONTACT_ATTACHMENT) {
            hideLayoutsContactAttachmentMessages(position, (ViewHolderMessageChat) holder);
        }

        switch (messageType) {
            case MegaChatMessage.TYPE_ALTER_PARTICIPANTS: {
                Timber.d("ALTER PARTICIPANT MESSAGE!!");
                bindAlterParticipantsMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_PRIV_CHANGE: {
                Timber.d("PRIVILEGE CHANGE message");
                bindPrivChangeMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_CONTAINS_META: {
                Timber.d("TYPE_CONTAINS_META");
                bindContainsMetaMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_NORMAL: {
                Timber.d("TYPE_NORMAL");
                String contactLink = extractContactLink(message.getContent());

                if (androidMessage.getRichLinkMessage() != null) {
                    bindMegaLinkMessage((ViewHolderMessageChat) holder, androidMessage, position);
                } else if (contactLink != null) {
                    if (androidMessage.getContactLinkResult() != null) {
                        bindContactLinkMessage((ViewHolderMessageChat) holder, androidMessage, position);
                    } else {
                        viewModel.getContactLinkByHandle(getContactLinkHandle(contactLink), contactLinkResult -> {
                            androidMessage.setContactLinkResult(contactLinkResult);

                            if (position == holder.getBindingAdapterPosition()) {
                                bindContactLinkMessage((ViewHolderMessageChat) holder, androidMessage, position);
                            }
                            return Unit.INSTANCE;
                        });
                    }
                } else {
                    bindNormalMessage((ViewHolderMessageChat) holder, androidMessage, position);
                }

                break;
            }
            case MegaChatMessage.TYPE_NODE_ATTACHMENT: {
                Timber.d("TYPE_NODE_ATTACHMENT");
                bindNodeAttachmentMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_VOICE_CLIP: {
                Timber.d("TYPE_VOICE_CLIP");
                bindVoiceClipAttachmentMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_CONTACT_ATTACHMENT: {
                Timber.d("TYPE_CONTACT_ATTACHMENT");
                bindContactAttachmentMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_CHAT_TITLE: {
                Timber.d("TYPE_CHAT_TITLE");
                bindChangeTitleMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_TRUNCATE:
                Timber.d("TYPE_TRUNCATE");
                bindTruncateMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;

            case MegaChatMessage.TYPE_SET_RETENTION_TIME:
                Timber.d("TYPE_SET_RETENTION_TIME");
                bindRetentionTimeMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;

            case MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT: {
                Timber.d("TYPE_REVOKE_NODE_ATTACHMENT");
                bindRevokeNodeMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_CALL_ENDED:
            case MegaChatMessage.TYPE_CALL_STARTED: {
                Timber.d("TYPE_CALL_ENDED or TYPE_CALL_STARTED");
                bindCallMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_PUBLIC_HANDLE_CREATE:
            case MegaChatMessage.TYPE_PUBLIC_HANDLE_DELETE:
            case MegaChatMessage.TYPE_SET_PRIVATE_MODE: {
                bindChatLinkMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_SCHED_MEETING: {
                Timber.d("TYPE_SCHED_MEETING");
                bindSchedMeetingMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_INVALID: {
                Timber.w("TYPE_INVALID");
                bindNoTypeMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            case MegaChatMessage.TYPE_UNKNOWN: {
                Timber.w("TYPE_UNKNOWN");
                hideMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
            default: {
                Timber.d("DEFAULT MegaChatMessage");
                hideMessage((ViewHolderMessageChat) holder, androidMessage, position);
                break;
            }
        }

        ChatActivity activity = (ChatActivity) context;
        long unreadCount = Math.abs(activity.getGeneralUnreadCount());
        if (unreadCount == 0 || activity.getLastIdMsgSeen() == MEGACHAT_INVALID_HANDLE || activity.getLastIdMsgSeen() != message.getMsgId()) {
            ((ViewHolderMessageChat) holder).newMessagesLayout.setVisibility(View.GONE);
            return;
        }

        if (position >= messages.size()) {
            //There is no next message
            ((ViewHolderMessageChat) holder).newMessagesLayout.setVisibility(View.GONE);
            ((ChatActivity) context).setLastIdMsgSeen(message.getMsgId());
            return;
        }

        MegaChatMessage nextMessage = messages.get(position).getMessage();
        int typeMessage = nextMessage.getType();
        int codeMessage = nextMessage.getCode();

        if (typeMessage >= MegaChatMessage.TYPE_LOWEST_MANAGEMENT && typeMessage <= MegaChatMessage.TYPE_SET_PRIVATE_MODE
                && (typeMessage != MegaChatMessage.TYPE_CALL_ENDED || (codeMessage != END_CALL_REASON_CANCELLED && codeMessage != END_CALL_REASON_NO_ANSWER))) {
            ((ViewHolderMessageChat) holder).newMessagesLayout.setVisibility(View.GONE);
            ((ChatActivity) context).setLastIdMsgSeen(nextMessage.getMsgId());
            return;
        }

        Timber.d("Last message ID match!");
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((ViewHolderMessageChat) holder).newMessagesLayout.getLayoutParams();
        long userHandle = (message.getType() == MegaChatMessage.TYPE_ALTER_PARTICIPANTS || message.getType() == MegaChatMessage.TYPE_PRIV_CHANGE) ? message.getHandleOfAction() : message.getUserHandle();

        params.addRule(RelativeLayout.BELOW, userHandle == myUserHandle ? R.id.message_chat_own_message_layout : R.id.message_chat_contact_message_layout);
        ((ViewHolderMessageChat) holder).newMessagesLayout.setLayoutParams(params);

        String numberString;
        long unreadMessages = Math.abs(((ChatActivity) context).getGeneralUnreadCount());
        if (((ChatActivity) context).getGeneralUnreadCount() < 0) {
            numberString = "+" + unreadMessages;
        } else {
            numberString = unreadMessages + "";
        }

        String contentUnreadText = context.getResources().getQuantityString(R.plurals.number_unread_messages, (int) unreadMessages, numberString);
        ((ViewHolderMessageChat) holder).newMessagesText.setText(contentUnreadText);
        ((ViewHolderMessageChat) holder).newMessagesLayout.setVisibility(View.VISIBLE);
        ((ChatActivity) context).setPositionNewMessagesLayout(position);
    }

    public boolean isKnownMessage(int messageType) {
        switch (messageType) {

            case MegaChatMessage.TYPE_ALTER_PARTICIPANTS:
            case MegaChatMessage.TYPE_PRIV_CHANGE:
            case MegaChatMessage.TYPE_CONTAINS_META:
            case MegaChatMessage.TYPE_NORMAL:
            case MegaChatMessage.TYPE_NODE_ATTACHMENT:
            case MegaChatMessage.TYPE_VOICE_CLIP:
            case MegaChatMessage.TYPE_CONTACT_ATTACHMENT:
            case MegaChatMessage.TYPE_CHAT_TITLE:
            case MegaChatMessage.TYPE_TRUNCATE:
            case MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT:
            case MegaChatMessage.TYPE_CALL_STARTED:
            case MegaChatMessage.TYPE_CALL_ENDED: {
                return true;
            }
            case MegaChatMessage.TYPE_UNKNOWN:
            case MegaChatMessage.TYPE_INVALID:
            default: {
                return false;
            }
        }
    }

    public void bindCallMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindCallMessage");

        ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.GONE);
        MegaChatMessage message = androidMessage.getMessage();

        if (message.getUserHandle() == myUserHandle) {
            Timber.d("MY message!!");
            Timber.d("MY message ID!!: %s", message.getMsgId());

            ((ViewHolderMessageChat) holder).titleOwnMessage.setGravity(Gravity.LEFT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_CALL_LAND, outMetrics), 0, 0, 0);
            } else {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_CALL_PORT, outMetrics), 0, 0, 0);
            }


            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);

            Spanned textToShow = null;

            if (message.getType() == MegaChatMessage.TYPE_CALL_STARTED) {
                holder.ownManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_started));
                textToShow = MeetingUtil.getAppropriateStringForCallStarted(context);
            } else {
                switch (message.getTermCode()) {
                    case MegaChatMessage.END_CALL_REASON_ENDED:
                    case MegaChatMessage.END_CALL_REASON_BY_MODERATOR:
                        holder.ownManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_ended));
                        textToShow = MeetingUtil.getAppropriateStringForCallEnded(chatRoom, message.getDuration(), context);
                        break;

                    case MegaChatMessage.END_CALL_REASON_REJECTED:
                        holder.ownManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_rejected));
                        textToShow = MeetingUtil.getAppropriateStringForCallRejected(context);
                        break;

                    case END_CALL_REASON_NO_ANSWER:
                        holder.ownManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_failed));
                        textToShow = MeetingUtil.getAppropriateStringForCallNoAnswered(message.getUserHandle(), context);
                        break;

                    case MegaChatMessage.END_CALL_REASON_FAILED:
                        holder.ownManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_failed));
                        textToShow = MeetingUtil.getAppropriateStringForCallFailed(context);
                        break;

                    case END_CALL_REASON_CANCELLED:
                        holder.ownManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_cancelled));
                        textToShow = MeetingUtil.getAppropriateStringForCallCancelled(message.getUserHandle(), context);
                        break;
                }
            }

            ((ViewHolderMessageChat) holder).contentOwnMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).ownManagementMessageIcon.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams paramsOwnManagement = (RelativeLayout.LayoutParams) holder.ownManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_CALL_LAND, outMetrics);
            } else {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_CALL_PORT, outMetrics);
            }
            holder.ownManagementMessageText.setLayoutParams(paramsOwnManagement);
            ((ViewHolderMessageChat) holder).ownManagementMessageText.setText(textToShow);

        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_CALL_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_CALL_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }
            ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

            ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactManagementMessageIcon.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams paramsContactManagement = (RelativeLayout.LayoutParams) holder.contactManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_CALL_LAND, outMetrics);
            } else {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_CALL_PORT, outMetrics);
            }
            holder.contactManagementMessageText.setLayoutParams(paramsContactManagement);

            Spanned textToShow = null;

            if (message.getType() == MegaChatMessage.TYPE_CALL_STARTED) {
                holder.contactManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_started));
                if (chatRoom != null && chatRoom.isGroup()) {
                    holder.nameContactText.setVisibility(View.VISIBLE);
                }
                textToShow = MeetingUtil.getAppropriateStringForCallStarted(context);

            } else {
                switch (message.getTermCode()) {
                    case MegaChatMessage.END_CALL_REASON_BY_MODERATOR:
                    case MegaChatMessage.END_CALL_REASON_ENDED:
                        holder.contactManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_ended));
                        textToShow = MeetingUtil.getAppropriateStringForCallEnded(chatRoom, message.getDuration(), context);
                        break;

                    case MegaChatMessage.END_CALL_REASON_REJECTED:
                        holder.contactManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_rejected));
                        textToShow = MeetingUtil.getAppropriateStringForCallRejected(context);
                        break;

                    case END_CALL_REASON_NO_ANSWER:
                        holder.contactManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_missed));
                        textToShow = MeetingUtil.getAppropriateStringForCallNoAnswered(message.getUserHandle(), context);
                        break;

                    case MegaChatMessage.END_CALL_REASON_FAILED:
                        holder.contactManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_failed));
                        textToShow = MeetingUtil.getAppropriateStringForCallFailed(context);
                        break;

                    case END_CALL_REASON_CANCELLED:
                        holder.contactManagementMessageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_cancelled));
                        textToShow = MeetingUtil.getAppropriateStringForCallCancelled(message.getUserHandle(), context);
                        break;
                }
            }

            ((ViewHolderMessageChat) holder).contactManagementMessageText.setText(textToShow);
        }
    }

    public void bindAlterParticipantsMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindAlterParticipantsMessage");
        ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.GONE);

        MegaChatMessage message = androidMessage.getMessage();

        if (message.getHandleOfAction() == myUserHandle) {

            ((ViewHolderMessageChat) holder).titleOwnMessage.setGravity(Gravity.LEFT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            Timber.d("Me alter participant");
            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);

            int privilege = message.getPrivilege();
            Timber.d("Privilege of me: %s", privilege);
            String textToShow = "";
            String fullNameAction = getContactMessageName(position, holder, message.getUserHandle());

            if (privilege != MegaChatRoom.PRIV_RM) {
                Timber.d("I was added");

                if (message.getUserHandle() == message.getHandleOfAction()) {
                    textToShow = String.format(context.getString(R.string.message_joined_public_chat_autoinvitation), toCDATA(megaChatApi.getMyFullname()));
                } else {
                    textToShow = String.format(context.getString(R.string.message_add_participant), toCDATA(megaChatApi.getMyFullname()), toCDATA(fullNameAction));
                }

                try {
                    textToShow = textToShow.replace("[A]", "<font color=\'"
                            + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                            + "\'>");
                    textToShow = textToShow.replace("[/A]", "</font>");
                    textToShow = textToShow.replace("[B]", "<font color=\'"
                            + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                            + "\'>");
                    textToShow = textToShow.replace("[/B]", "</font>");
                    textToShow = textToShow.replace("[C]", "<font color=\'"
                            + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                            + "\'>");
                    textToShow = textToShow.replace("[/C]", "</font>");
                } catch (Exception e) {
                }
            } else {
                Timber.d("I was removed or left");
                if (message.getUserHandle() == message.getHandleOfAction()) {
                    Timber.d("I left the chat");
                    textToShow = String.format(context.getString(R.string.message_participant_left_group_chat), toCDATA(megaChatApi.getMyFullname()));
                    try {
                        textToShow = textToShow.replace("[A]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/A]", "</font>");
                        textToShow = textToShow.replace("[B]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                                + "\'>");
                        textToShow = textToShow.replace("[/B]", "</font>");
                    } catch (Exception e) {
                    }
                } else {
                    textToShow = String.format(context.getString(R.string.message_remove_participant), toCDATA(megaChatApi.getMyFullname()), toCDATA(fullNameAction));
                    try {
                        textToShow = textToShow.replace("[A]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/A]", "</font>");
                        textToShow = textToShow.replace("[B]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                                + "\'>");
                        textToShow = textToShow.replace("[/B]", "</font>");
                        textToShow = textToShow.replace("[C]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/C]", "</font>");
                    } catch (Exception e) {
                    }
                }
            }

            Spanned result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);
            ((ViewHolderMessageChat) holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).ownManagementMessageIcon.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageLayout.setVisibility(View.GONE);
            RelativeLayout.LayoutParams paramsOwnManagement = (RelativeLayout.LayoutParams) holder.ownManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
            } else {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
            }
            holder.ownManagementMessageText.setLayoutParams(paramsOwnManagement);
            ((ViewHolderMessageChat) holder).ownManagementMessageText.setText(result);

        } else {
            Timber.d("CONTACT Message type ALTER PARTICIPANTS");
            int privilege = message.getPrivilege();
            Timber.d("Privilege of the user: %s", privilege);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }
            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactManagementMessageIcon.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsContactManagement = (RelativeLayout.LayoutParams) holder.contactManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
            } else {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
            }
            holder.contactManagementMessageText.setLayoutParams(paramsContactManagement);
            ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).nameContactText.setVisibility(View.GONE);

            setContactMessageName(position, holder, message.getHandleOfAction(), false);

            String textToShow = "";
            if (privilege != MegaChatRoom.PRIV_RM) {
                Timber.d("Participant was added");
                if (message.getUserHandle() == myUserHandle) {
                    Timber.d("By me");

                    if (message.getUserHandle() == message.getHandleOfAction()) {
                        textToShow = String.format(context.getString(R.string.message_joined_public_chat_autoinvitation), toCDATA(holder.fullNameTitle));
                    } else {
                        textToShow = String.format(context.getString(R.string.message_add_participant), toCDATA(holder.fullNameTitle), toCDATA(megaChatApi.getMyFullname()));
                    }

                    try {
                        textToShow = textToShow.replace("[A]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/A]", "</font>");
                        textToShow = textToShow.replace("[B]", "<font color=\'#"
                                + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                                + "\'>");
                        textToShow = textToShow.replace("[/B]", "</font>");
                        textToShow = textToShow.replace("[C]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/C]", "</font>");
                    } catch (Exception e) {
                    }
                } else {
                    Timber.d("By other");
                    String fullNameAction = getContactMessageName(position, holder, message.getUserHandle());

                    if (message.getUserHandle() == message.getHandleOfAction()) {
                        textToShow = String.format(context.getString(R.string.message_joined_public_chat_autoinvitation), toCDATA(holder.fullNameTitle));
                    } else {
                        textToShow = String.format(context.getString(R.string.message_add_participant), toCDATA(holder.fullNameTitle), toCDATA(fullNameAction));
                    }

                    try {
                        textToShow = textToShow.replace("[A]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/A]", "</font>");
                        textToShow = textToShow.replace("[B]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                                + "\'>");
                        textToShow = textToShow.replace("[/B]", "</font>");
                        textToShow = textToShow.replace("[C]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/C]", "</font>");
                    } catch (Exception e) {
                    }

                }
            }//END participant was added
            else {
                Timber.d("Participant was removed or left");
                if (message.getUserHandle() == myUserHandle) {
                    textToShow = String.format(context.getString(R.string.message_remove_participant), toCDATA(holder.fullNameTitle), toCDATA(megaChatApi.getMyFullname()));
                    try {
                        textToShow = textToShow.replace("[A]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/A]", "</font>");
                        textToShow = textToShow.replace("[B]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                                + "\'>");
                        textToShow = textToShow.replace("[/B]", "</font>");
                        textToShow = textToShow.replace("[C]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/C]", "</font>");
                    } catch (Exception e) {
                    }
                } else {

                    if (message.getUserHandle() == message.getHandleOfAction()) {
                        Timber.d("The participant left the chat");

                        textToShow = String.format(context.getString(R.string.message_participant_left_group_chat), toCDATA(holder.fullNameTitle));
                        try {
                            textToShow = textToShow.replace("[A]", "<font color=\'"
                                    + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                    + "\'>");
                            textToShow = textToShow.replace("[/A]", "</font>");
                            textToShow = textToShow.replace("[B]", "<font color=\'"
                                    + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                                    + "\'>");
                            textToShow = textToShow.replace("[/B]", "</font>");
                        } catch (Exception e) {
                        }

                    } else {
                        Timber.d("The participant was removed");
                        String fullNameAction = getContactMessageName(position, holder, message.getUserHandle());

                        textToShow = String.format(context.getString(R.string.message_remove_participant), toCDATA(holder.fullNameTitle), toCDATA(fullNameAction));
                        try {
                            textToShow = textToShow.replace("[A]", "<font color=\'"
                                    + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                    + "\'>");
                            textToShow = textToShow.replace("[/A]", "</font>");
                            textToShow = textToShow.replace("[B]", "<font color=\'"
                                    + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                                    + "\'>");
                            textToShow = textToShow.replace("[/B]", "</font>");
                            textToShow = textToShow.replace("[C]", "<font color=\'"
                                    + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                    + "\'>");
                            textToShow = textToShow.replace("[/C]", "</font>");
                        } catch (Exception e) {
                        }
                    }
                }
            } //END participant removed

            Spanned result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);
            ((ViewHolderMessageChat) holder).contactManagementMessageText.setText(result);

        }
    }

    /**
     * Get permissions change management message
     *
     * @param message  The MegaChatMessage
     * @param position Position in adapter
     * @param holder   ViewHolderMessageChat
     * @return The formatted text
     */
    private Spanned getPrivilegesChangeMessage(MegaChatMessage message, int position, ViewHolderMessageChat holder) {
        int privilege = message.getPrivilege();
        Timber.d("Privilege of the user: %s", privilege);

        String participantsNameWhosePermissionsWereChanged = toCDATA(message.getHandleOfAction() == myUserHandle ? megaChatApi.getMyFullname() : getContactMessageName(position, holder, message.getHandleOfAction()));
        String participantsNameWhoMadeTheAction = toCDATA(message.getUserHandle() == myUserHandle ? megaChatApi.getMyFullname() : getContactMessageName(position, holder, message.getUserHandle()));

        String textToShow = "";
        switch (privilege) {
            case MegaChatRoom.PRIV_MODERATOR:
                textToShow = context.getString(R.string.chat_chat_room_message_permissions_changed_to_host, participantsNameWhosePermissionsWereChanged, participantsNameWhoMadeTheAction);
                break;
            case MegaChatRoom.PRIV_STANDARD:
                textToShow = context.getString(R.string.chat_chat_room_message_permissions_changed_to_standard, participantsNameWhosePermissionsWereChanged, participantsNameWhoMadeTheAction);
                break;
            case MegaChatRoom.PRIV_RO:
                textToShow = context.getString(R.string.chat_chat_room_message_permissions_changed_to_read_only, participantsNameWhosePermissionsWereChanged, participantsNameWhoMadeTheAction);
                break;
        }

        return TextUtil.replaceFormatChatMessages(context, textToShow, true);
    }

    public void bindPrivChangeMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindPrivChangeMessage");
        ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.GONE);

        MegaChatMessage message = androidMessage.getMessage();

        if (message.getHandleOfAction() == myUserHandle) {

            ((ViewHolderMessageChat) holder).titleOwnMessage.setGravity(Gravity.LEFT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            Timber.d("A moderator change my privilege");
            int privilege = message.getPrivilege();
            Timber.d("Privilege of the user: %s", privilege);

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).ownManagementMessageIcon.setVisibility(View.GONE);
            RelativeLayout.LayoutParams paramsOwnManagement = (RelativeLayout.LayoutParams) holder.ownManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
            } else {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
            }
            holder.ownManagementMessageText.setLayoutParams(paramsOwnManagement);

            ((ViewHolderMessageChat) holder).ownManagementMessageText.setText(getPrivilegesChangeMessage(message, position, holder));

            Timber.d("Visible own management message!");

        } else {
            Timber.d("Participant privilege change!");

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }
            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

            ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactManagementMessageIcon.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsContactManagement = (RelativeLayout.LayoutParams) holder.contactManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
            } else {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
            }
            holder.contactManagementMessageText.setLayoutParams(paramsContactManagement);
            ((ViewHolderMessageChat) holder).nameContactText.setVisibility(View.GONE);

            setContactMessageName(position, holder, message.getHandleOfAction(), false);

            ((ViewHolderMessageChat) holder).contactManagementMessageText.setText(getPrivilegesChangeMessage(message, position, holder));
        }
    }

    public void bindContainsMetaMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindContainsMetaMessage()");
        MegaChatMessage message = androidMessage.getMessage();
        MegaChatContainsMeta meta = message.getContainsMeta();
        if (meta == null) {
            bindNoTypeMessage(holder, androidMessage, position);

        } else if (meta != null && meta.getType() == MegaChatContainsMeta.CONTAINS_META_RICH_PREVIEW) {
            String urlString = meta.getRichPreview().getUrl();
            try {
                URL url = new URL(urlString);
                urlString = url.getHost();

            } catch (MalformedURLException e) {
                Timber.e(e, "EXCEPTION");
            }

            String title = meta.getRichPreview().getTitle();
            String text = meta.getRichPreview().getText();
            String description = meta.getRichPreview().getDescription();
            String imageFormat = meta.getRichPreview().getImageFormat();
            String image = meta.getRichPreview().getImage();
            String icon = meta.getRichPreview().getIcon();


            Bitmap bitmapImage = getBitmapFromString(image);
            Bitmap bitmapIcon = getBitmapFromString(icon);

            if (message.getUserHandle() == myUserHandle) {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
                holder.titleOwnMessage.setGravity(Gravity.RIGHT);

                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
                } else {
                    holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
                }

                Timber.d("MY message handle!!: %s", message.getMsgId());
                if (messages.get(position - 1).getInfoToShow() != -1) {
                    setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                            formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                            formatTime(message));
                }

                //Forwards element (own messages):
                if (checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                    holder.forwardOwnRichLinks.setVisibility(View.VISIBLE);
                    holder.forwardOwnRichLinks.setOnClickListener(this);
                    holder.forwardOwnRichLinks.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
                } else {
                    holder.forwardOwnRichLinks.setVisibility(View.GONE);
                }

                holder.urlOwnMessageTextrl.setBackgroundResource(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ?
                        R.drawable.light_background_text_rich_link :
                        R.drawable.dark_background_text_rich_link);

                int status = message.getStatus();
                if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                    Timber.d("Show triangle retry!");
                    ((ViewHolderMessageChat) holder).errorUploadingRichLink.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.VISIBLE);
                } else if ((status == MegaChatMessage.STATUS_SENDING)) {
                    ((ViewHolderMessageChat) holder).errorUploadingRichLink.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.GONE);
                } else {
                    Timber.d("Status: %s", message.getStatus());
                    ((ViewHolderMessageChat) holder).errorUploadingRichLink.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.GONE);
                }

                holder.contactMessageLayout.setVisibility(View.GONE);
                holder.ownMessageLayout.setVisibility(View.VISIBLE);

                holder.ownManagementMessageLayout.setVisibility(View.GONE);
                holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);

                holder.contentOwnMessageText.setVisibility(View.GONE);
                holder.urlOwnMessageLayout.setVisibility(View.VISIBLE);

                holder.previewFrameLand.setVisibility(View.GONE);
                holder.previewFramePort.setVisibility(View.GONE);

                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);

                holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);

                holder.contentOwnMessageContactLayout.setVisibility(View.GONE);
                holder.urlOwnMessageWarningButtonsLayout.setVisibility(View.GONE);
                holder.urlOwnMessageDisableButtonsLayout.setVisibility(View.GONE);

                checkEmojiSize(text, holder.urlOwnMessageText);
                holder.urlOwnMessageText.setText(text);

                holder.urlOwnMessageTitle.setVisibility(View.VISIBLE);
                holder.urlOwnMessageTitle.setText(title);
                holder.urlOwnMessageTitle.setMaxLines(1);
                holder.urlOwnMessageDescription.setText(description);
                holder.urlOwnMessageDescription.setMaxLines(2);
                holder.urlOwnMessageIconAndLinkLayout.setVisibility(View.VISIBLE);
                holder.urlOwnMessageLink.setText(urlString);

                if (bitmapImage != null) {
                    holder.urlOwnMessageImage.setImageBitmap(bitmapImage);
                    holder.urlOwnMessageImage.setVisibility(View.VISIBLE);
                    holder.urlOwnMessageGroupAvatarLayout.setVisibility(View.GONE);
                    holder.urlOwnMessageTitleLayout.setGravity(Gravity.RIGHT);
                } else {
                    holder.urlOwnMessageGroupAvatarLayout.setVisibility(View.GONE);
                    holder.urlOwnMessageImage.setVisibility(View.GONE);
                    holder.urlOwnMessageTitleLayout.setGravity(Gravity.LEFT);
                }

                if (bitmapIcon != null) {
                    holder.urlOwnMessageIcon.setImageBitmap(bitmapIcon);
                    holder.urlOwnMessageIcon.setVisibility(View.VISIBLE);
                } else {
                    holder.urlOwnMessageIcon.setVisibility(View.GONE);
                }

                if (isOnline(context)) {
                    if (isMultipleSelect()) {
                        holder.urlOwnMessageText.setLinksClickable(false);
                    } else {
                        holder.urlOwnMessageText.setLinksClickable(true);
                        Linkify.addLinks(holder.urlOwnMessageText, Linkify.WEB_URLS);
                    }
                } else {
                    holder.urlOwnMessageText.setLinksClickable(false);
                }

                checkMultiselectionMode(position, holder, true, message.getMsgId());
                interceptLinkClicks(context, holder.urlOwnMessageText);
            } else {
                long userHandle = message.getUserHandle();
                Timber.d("Contact message!!: %s", userHandle);

                setContactMessageName(position, holder, userHandle, true);

                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
                } else {
                    holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
                }

                if (messages.get(position - 1).getInfoToShow() != -1) {
                    setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                            formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                            formatTime(message));
                }

                if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                    holder.layoutAvatarMessages.setVisibility(View.VISIBLE);
                    setContactAvatar(holder, userHandle, holder.fullNameTitle);

                } else {
                    holder.layoutAvatarMessages.setVisibility(View.GONE);
                }

                holder.ownMessageLayout.setVisibility(View.GONE);
                holder.contactMessageLayout.setVisibility(View.VISIBLE);

                holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
                holder.contactManagementMessageLayout.setVisibility(View.GONE);

                holder.contentContactMessageText.setVisibility(View.GONE);
                holder.urlContactMessageLayout.setVisibility(View.VISIBLE);
                holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);

                //Forwards element (contact messages):
                if (checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                    holder.forwardContactRichLinks.setVisibility(View.VISIBLE);
                    holder.forwardContactRichLinks.setOnClickListener(this);
                    holder.forwardContactRichLinks.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
                } else {
                    holder.forwardContactRichLinks.setVisibility(View.GONE);
                }

                holder.contentContactMessageAttachLayout.setVisibility(View.GONE);
                holder.contentContactMessageContactLayout.setVisibility(View.GONE);

                //Rick link
                holder.urlOwnMessageWarningButtonsLayout.setVisibility(View.GONE);
                holder.urlOwnMessageDisableButtonsLayout.setVisibility(View.GONE);

                holder.urlContactMessageTitle.setVisibility(View.VISIBLE);
                holder.urlContactMessageTitle.setText(title);
                holder.urlContactMessageDescription.setText(description);
                holder.urlContactMessageIconAndLinkLayout.setVisibility(View.VISIBLE);
                holder.urlContactMessageLink.setText(urlString);

                checkEmojiSize(text, holder.urlContactMessageText);

                //Color always status SENT
                holder.urlContactMessageText.setText(text);

                if (bitmapImage != null) {
                    holder.urlContactMessageImage.setImageBitmap(bitmapImage);
                    holder.urlContactMessageImage.setVisibility(View.VISIBLE);
                    holder.urlContactMessageGroupAvatarLayout.setVisibility(View.GONE);
                    holder.urlContactMessageTitleLayout.setGravity(Gravity.RIGHT);
                } else {
                    holder.urlContactMessageGroupAvatarLayout.setVisibility(View.GONE);
                    holder.urlContactMessageImage.setVisibility(View.GONE);
                    holder.urlContactMessageTitleLayout.setGravity(Gravity.LEFT);
                }

                if (bitmapIcon != null) {
                    holder.urlContactMessageIcon.setImageBitmap(bitmapIcon);
                    holder.urlContactMessageIcon.setVisibility(View.VISIBLE);
                } else {
                    holder.urlContactMessageIcon.setVisibility(View.GONE);
                }

                if (isOnline(context)) {
                    if (isMultipleSelect()) {
                        ((ViewHolderMessageChat) holder).urlContactMessageText.setLinksClickable(false);
                    } else {
                        ((ViewHolderMessageChat) holder).urlContactMessageText.setLinksClickable(true);
                        Linkify.addLinks(((ViewHolderMessageChat) holder).urlContactMessageText, Linkify.WEB_URLS);
                    }
                } else {
                    ((ViewHolderMessageChat) holder).urlContactMessageText.setLinksClickable(false);
                }

                checkMultiselectionMode(position, holder, false, message.getMsgId());
                interceptLinkClicks(context, holder.urlContactMessageText);
            }

            checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);

        } else if (meta.getType() == MegaChatContainsMeta.CONTAINS_META_INVALID) {
            String invalidMetaMessage = getInvalidMetaMessage(message, context);

            if (message.getUserHandle() == myUserHandle) {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
                holder.titleOwnMessage.setGravity(Gravity.RIGHT);

                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
                } else {
                    holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
                }

                if (messages.get(position - 1).getInfoToShow() != -1) {
                    setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                            formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                            formatTime(message));
                }

                holder.ownMessageLayout.setVisibility(View.VISIBLE);
                holder.contactMessageLayout.setVisibility(View.GONE);
                holder.ownManagementMessageLayout.setVisibility(View.GONE);
                holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);

                holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
                holder.ownManagementMessageLayout.setVisibility(View.GONE);
                holder.contentOwnMessageText.setVisibility(View.VISIBLE);

                holder.previewFrameLand.setVisibility(View.GONE);
                holder.previewFramePort.setVisibility(View.GONE);

                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

                holder.contentOwnMessageText.setBackgroundResource(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ?
                        R.drawable.light_rounded_chat_own_message :
                        R.drawable.dark_rounded_chat_own_message);

                int status = message.getStatus();
                if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                    Timber.d("Show triangle retry!");
                    holder.triangleIcon.setVisibility(View.VISIBLE);
                    holder.retryAlert.setVisibility(View.VISIBLE);
                } else if ((status == MegaChatMessage.STATUS_SENDING)) {
                    holder.triangleIcon.setVisibility(View.GONE);
                    holder.retryAlert.setVisibility(View.GONE);
                } else {
                    Timber.d("Status: %s", message.getStatus());
                    holder.triangleIcon.setVisibility(View.GONE);
                    holder.retryAlert.setVisibility(View.GONE);
                }

                ((ViewHolderMessageChat) holder).contentOwnMessageText.setTextColor(Color.WHITE);
                ((ViewHolderMessageChat) holder).contentOwnMessageText.setLinkTextColor(Color.WHITE);
                holder.contentOwnMessageText.setText(context.getString(R.string.error_meta_message_invalid));

                if (isOnline(context)) {
                    if (isMultipleSelect()) {
                        holder.contentOwnMessageText.setLinksClickable(false);
                    } else {
                        holder.contentOwnMessageText.setLinksClickable(true);
                        Linkify.addLinks(holder.contentOwnMessageText, Linkify.WEB_URLS);
                    }
                } else {
                    holder.contentOwnMessageText.setLinksClickable(false);
                }


            } else {
                long userHandle = message.getUserHandle();
                Timber.d("Contact message!!: %s", userHandle);

                setContactMessageName(position, holder, userHandle, true);

                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
                } else {
                    holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
                }

                if (messages.get(position - 1).getInfoToShow() != -1) {
                    setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                            formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                            formatTime(message));
                }

                holder.ownMessageLayout.setVisibility(View.GONE);
                holder.contactMessageLayout.setVisibility(View.VISIBLE);

                holder.contactManagementMessageLayout.setVisibility(View.GONE);
                holder.contentContactMessageLayout.setVisibility(View.VISIBLE);


                if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                    ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.VISIBLE);
                    setContactAvatar(((ViewHolderMessageChat) holder), userHandle, ((ViewHolderMessageChat) holder).fullNameTitle);
                } else {
                    ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.GONE);
                }

                ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

                ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.VISIBLE);

                ((ViewHolderMessageChat) holder).contentContactMessageText.setVisibility(View.VISIBLE);

                ((ViewHolderMessageChat) holder).contentContactMessageAttachLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).urlContactMessageLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contentContactMessageContactLayout.setVisibility(View.GONE);

                //Color always status SENT
                ((ViewHolderMessageChat) holder).contentContactMessageText.setTextColor(ContextCompat.getColor(context, R.color.grey_087_white));
                ((ViewHolderMessageChat) holder).contentContactMessageText.setLinkTextColor(ContextCompat.getColor(context, R.color.grey_087_white));
                ((ViewHolderMessageChat) holder).contentContactMessageText.setText(context.getString(R.string.error_meta_message_invalid));
            }

            checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
        } else if (meta.getType() == MegaChatContainsMeta.CONTAINS_META_GEOLOCATION) {
            bindGeoLocationMessage(holder, androidMessage, position);
        } else if (meta.getType() == MegaChatContainsMeta.CONTAINS_META_GIPHY) {
            bindGiphyOrGifMessage(holder, androidMessage, position, true);
        } else {
            Timber.w("Link to bind as a no type message");
            bindNoTypeMessage(holder, androidMessage, position);
        }
    }

    private Bitmap getBitmapFromString(String imageString) {

        if (imageString != null) {
            try {
                byte[] decodedBytes = Base64.decode(imageString, 0);
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            } catch (Exception e) {
                Timber.e(e, "Error getting image");
            }
        }
        return null;
    }

    private Bitmap getResizeBitmap(Bitmap originalBitmap) {
        if (originalBitmap == null) return null;

        int widthResizeBitmap = originalBitmap.getWidth();
        int heightResizeBitmap = originalBitmap.getWidth() / 2;
        int topResizeBitmap = heightResizeBitmap / 2;
        int bottomResizeBitmap = topResizeBitmap + heightResizeBitmap;
        Bitmap resizeBitmap = Bitmap.createBitmap(widthResizeBitmap, heightResizeBitmap, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(resizeBitmap);
        Rect desRect = new Rect(0, 0, widthResizeBitmap, heightResizeBitmap);
        Rect srcRect = new Rect(0, topResizeBitmap, widthResizeBitmap, bottomResizeBitmap);
        canvas.drawBitmap(originalBitmap, srcRect, desRect, null);

        return resizeBitmap;
    }

    public void bindGeoLocationMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        MegaChatMessage message = androidMessage.getMessage();
        MegaChatContainsMeta meta = message.getContainsMeta();
        boolean isMyMessage = message.getUserHandle() == myUserHandle;

        String image = meta.getGeolocation().getImage();
        float latitude = meta.getGeolocation().getLatitude();
        float longitude = meta.getGeolocation().getLongitude();
        String location = convertToDegrees(latitude, longitude);

        Bitmap bitmapImage = null;

        if (image != null) {
            byte[] decodedBytes = Base64.decode(image, 0);
            bitmapImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            bitmapImage = getResizeBitmap(bitmapImage);
        }

        if (messages.get(position - 1).getInfoToShow() != -1) {
            setInfoToShow(position, holder, isMyMessage, messages.get(position - 1).getInfoToShow(),
                    formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                    formatTime(message));
        }

        boolean isLandscapeMode = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (isMyMessage) {
            holder.layoutAvatarMessages.setVisibility(View.GONE);
            holder.titleOwnMessage.setGravity(Gravity.END);
            holder.titleOwnMessage.setPadding(0, 0,
                    scaleWidthPx(isLandscapeMode ? PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND
                            : PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            holder.contactMessageLayout.setVisibility(View.GONE);
            holder.ownMessageLayout.setVisibility(View.VISIBLE);
            holder.ownManagementMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
            holder.contentOwnMessageText.setVisibility(View.GONE);
            holder.urlOwnMessageLayout.setVisibility(View.GONE);

            holder.forwardOwnPortrait.setVisibility(View.GONE);
            holder.forwardOwnLandscape.setVisibility(View.GONE);
            holder.forwardOwnFile.setVisibility(View.GONE);
            holder.forwardOwnContact.setVisibility(View.GONE);
            holder.previewFrameLand.setVisibility(View.GONE);
            holder.previewFramePort.setVisibility(View.GONE);
            holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
            holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
            holder.contentOwnMessageContactLayout.setVisibility(View.GONE);
            holder.urlOwnMessageWarningButtonsLayout.setVisibility(View.GONE);
            holder.urlOwnMessageDisableButtonsLayout.setVisibility(View.GONE);
            holder.mainOwnMessageItemLocation.setVisibility(View.VISIBLE);
            holder.previewOwnLocation.setVisibility(View.VISIBLE);
            holder.pinnedOwnLocationLayout.setVisibility(View.VISIBLE);
            holder.pinnedOwnLocationInfoText.setVisibility(View.VISIBLE);
            holder.pinnedOwnLocationInfoText.setText(location);

            if (bitmapImage != null) {
                holder.previewOwnLocation.setImageBitmap(bitmapImage);
            }

            //Forwards element (own messages):
            if (checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                holder.forwardOwnMessageLocation.setVisibility(View.VISIBLE);
                holder.forwardOwnMessageLocation.setOnClickListener(this);
                holder.forwardOwnMessageLocation.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.forwardOwnMessageLocation.setVisibility(View.GONE);
            }

            switch (message.getStatus()) {
                case MegaChatMessage.STATUS_SERVER_REJECTED:
                case MegaChatMessage.STATUS_SENDING_MANUAL:
                    holder.retryAlert.setVisibility(View.VISIBLE);
                    holder.transparentCoatingLocation.setVisibility(View.GONE);
                    holder.uploadingProgressBarLocation.setVisibility(View.GONE);
                    holder.triangleErrorLocation.setVisibility(View.VISIBLE);
                    holder.pinnedOwnLocationInfoText.setTextColor(ContextCompat.getColor(context, R.color.grey_054_white_054));
                    holder.pinnedLocationTitleText.setTextColor(ContextCompat.getColor(context, R.color.grey_054_white_054));
                    break;

                case MegaChatMessage.STATUS_SENDING:
                    holder.retryAlert.setVisibility(View.GONE);
                    holder.transparentCoatingLocation.setVisibility(View.VISIBLE);
                    holder.uploadingProgressBarLocation.setVisibility(View.VISIBLE);
                    holder.triangleErrorLocation.setVisibility(View.GONE);
                    holder.pinnedOwnLocationInfoText.setTextColor(ContextCompat.getColor(context, R.color.grey_054_white_054));
                    holder.pinnedLocationTitleText.setTextColor(ContextCompat.getColor(context, R.color.grey_054_white_054));
                    break;

                default:
                    holder.retryAlert.setVisibility(View.GONE);
                    holder.transparentCoatingLocation.setVisibility(View.GONE);
                    holder.uploadingProgressBarLocation.setVisibility(View.GONE);
                    holder.triangleErrorLocation.setVisibility(View.GONE);
                    holder.pinnedOwnLocationInfoText.setTextColor(ContextCompat.getColor(context, R.color.grey_087_white_087));
                    holder.pinnedLocationTitleText.setTextColor(ContextCompat.getColor(context, R.color.grey_087_white_087));
            }

            if (message.isEdited()) {
                Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                edited.setSpan(new RelativeSizeSpan(0.70f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_087_white_087)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.pinnedLocationTitleText.setText(context.getString(R.string.title_geolocation_message) + " ");
                holder.pinnedLocationTitleText.append(edited);
            } else {
                holder.pinnedLocationTitleText.setText(context.getString(R.string.title_geolocation_message));
            }
        } else {
            long userHandle = message.getUserHandle();

            setContactMessageName(position, holder, userHandle, true);

            holder.titleContactMessage.setPadding(scaleWidthPx(isLandscapeMode ? CONTACT_MESSAGE_LAND
                    : CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);

            if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                holder.layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(holder, userHandle, holder.fullNameTitle);
            } else {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
            }

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);
            holder.mainContactMessageItemLocation.setVisibility(View.VISIBLE);
            holder.previewContactLocation.setVisibility(View.VISIBLE);
            holder.pinnedContactLocationLayout.setVisibility(View.VISIBLE);
            holder.pinnedContactLocationInfoText.setVisibility(View.VISIBLE);
            holder.pinnedContactLocationInfoText.setText(location);

            holder.forwardContactRichLinks.setVisibility(View.GONE);
            holder.forwardContactPreviewPortrait.setVisibility(View.GONE);
            holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
            holder.forwardContactFile.setVisibility(View.GONE);
            holder.forwardContactContact.setVisibility(View.GONE);

            if (bitmapImage != null) {
                holder.previewContactLocation.setImageBitmap(bitmapImage);
            }

            if (message.isEdited()) {
                Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                edited.setSpan(new RelativeSizeSpan(0.70f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_087_white_087)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.pinnedContactLocationTitleText.setText(context.getString(R.string.title_geolocation_message) + " ");
                holder.pinnedContactLocationTitleText.append(edited);
            } else {
                holder.pinnedContactLocationTitleText.setText(context.getString(R.string.title_geolocation_message));
            }

            //Forwards element (contact messages):
            if (checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                holder.forwardContactMessageLocation.setVisibility(View.VISIBLE);
                holder.forwardContactMessageLocation.setOnClickListener(this);
                holder.forwardContactMessageLocation.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.forwardContactMessageLocation.setVisibility(View.GONE);
            }
        }
        checkMultiselectionMode(position, holder, isMyMessage, message.getMsgId());
        checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
    }

    public void bindMegaLinkMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindMegaLinkMessage()");
        MegaChatMessage message = androidMessage.getMessage();
        boolean isFile = androidMessage.getRichLinkMessage().isFile();
        boolean isFolder = androidMessage.getRichLinkMessage().getFolderName() != null;
        MegaNode node = androidMessage.getRichLinkMessage().getNode();

        if (message.getUserHandle() == myUserHandle) {
            Timber.d("MY message handle!!: %s", message.getMsgId());

            holder.layoutAvatarMessages.setVisibility(View.GONE);
            holder.titleOwnMessage.setGravity(Gravity.RIGHT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                switch (messages.get(position - 1).getInfoToShow()) {
                    case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL: {
                        holder.dateLayout.setVisibility(View.VISIBLE);
                        holder.dateText.setText(formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context));
                        holder.titleOwnMessage.setVisibility(View.VISIBLE);
                        holder.timeOwnText.setText(formatTime(message));
                        break;
                    }
                    case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME: {
                        Timber.d("CHAT_ADAPTER_SHOW_TIME");
                        holder.dateLayout.setVisibility(View.GONE);
                        holder.titleOwnMessage.setVisibility(View.VISIBLE);
                        holder.timeOwnText.setText(formatTime(message));
                        break;
                    }
                    case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING: {
                        Timber.d("CHAT_ADAPTER_SHOW_NOTHING");
                        holder.dateLayout.setVisibility(View.GONE);
                        holder.titleOwnMessage.setVisibility(View.GONE);
                        break;
                    }
                }
            }

            holder.contactMessageLayout.setVisibility(View.GONE);
            holder.ownMessageLayout.setVisibility(View.VISIBLE);

            holder.ownManagementMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);

            holder.contentOwnMessageText.setVisibility(View.GONE);
            holder.urlOwnMessageLayout.setVisibility(View.VISIBLE);

            //Forwards element (own messages):
            if (checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                holder.forwardOwnRichLinks.setVisibility(View.VISIBLE);
                holder.forwardOwnRichLinks.setOnClickListener(this);
                holder.forwardOwnRichLinks.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.forwardOwnRichLinks.setVisibility(View.GONE);
            }

            holder.urlOwnMessageIconAndLinkLayout.setVisibility(View.GONE);
            holder.previewFrameLand.setVisibility(View.GONE);
            holder.previewFramePort.setVisibility(View.GONE);
            holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
            holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
            holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

            //MEGA link
            holder.urlOwnMessageWarningButtonsLayout.setVisibility(View.GONE);
            holder.urlOwnMessageDisableButtonsLayout.setVisibility(View.GONE);

            String messageContent = "";
            if (message.getContent() != null) {
                messageContent = converterShortCodes(message.getContent());
            }

            holder.urlOwnMessageTextrl.setBackgroundResource(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ?
                    R.drawable.light_background_text_rich_link :
                    R.drawable.dark_background_text_rich_link);

            if (message.isEdited()) {
                Spannable content = new SpannableString(messageContent);
                int status = message.getStatus();
                if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                    Timber.d("Show triangle retry!");
                    content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.errorUploadingRichLink.setVisibility(View.VISIBLE);
                    holder.retryAlert.setVisibility(View.VISIBLE);
                } else if ((status == MegaChatMessage.STATUS_SENDING)) {
                    Timber.d("Status not received by server: %s", message.getStatus());
                    content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.errorUploadingRichLink.setVisibility(View.GONE);
                    holder.retryAlert.setVisibility(View.GONE);
                } else {
                    Timber.d("Status: %s", message.getStatus());
                    content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.errorUploadingRichLink.setVisibility(View.GONE);
                    holder.retryAlert.setVisibility(View.GONE);
                }

                holder.urlOwnMessageText.setText(content + " ");
                Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                edited.setSpan(new RelativeSizeSpan(0.70f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.urlOwnMessageText.append(edited);

                checkEmojiSize(messageContent, holder.urlOwnMessageText);
            } else {
                int status = message.getStatus();

                if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                    Timber.d("Show triangle retry!");
                    ((ViewHolderMessageChat) holder).errorUploadingRichLink.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.VISIBLE);
                } else if ((status == MegaChatMessage.STATUS_SENDING)) {
                    ((ViewHolderMessageChat) holder).errorUploadingRichLink.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.GONE);
                } else {
                    Timber.d("Status: %s", message.getStatus());
                    ((ViewHolderMessageChat) holder).errorUploadingRichLink.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).retryAlert.setVisibility(View.GONE);
                }

                checkEmojiSize(messageContent, holder.urlOwnMessageText);
                holder.urlOwnMessageText.setText(messageContent);
            }

            holder.urlOwnMessageIconAndLinkLayout.setVisibility(View.VISIBLE);
            holder.urlOwnMessageLink.setText(androidMessage.getRichLinkMessage().getServer());

            holder.urlOwnMessageIcon.setImageResource(R.drawable.ic_launcher);
            holder.urlOwnMessageIcon.setVisibility(View.VISIBLE);

            holder.urlOwnMessageText.setLinksClickable(false);
            Linkify.addLinks(holder.urlOwnMessageText, Linkify.WEB_URLS);
            holder.urlOwnMessageText.setOnClickListener(this);
            holder.urlOwnMessageText.setOnLongClickListener(this);

            if (isFile || isFolder) {
                ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setVisibility(View.VISIBLE);

                ((ViewHolderMessageChat) holder).urlOwnMessageImage.setVisibility(View.VISIBLE);
                ((ViewHolderMessageChat) holder).urlOwnMessageGroupAvatarLayout.setVisibility(View.GONE);

                ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setMaxLines(1);
                ((ViewHolderMessageChat) holder).urlOwnMessageDescription.setMaxLines(2);

                if (isFile) {
                    ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setText(node.getName());
                    Bitmap thumb;
                    thumb = getThumbnailFromCache(node);
                    if (thumb != null) {
                        previewCache.put(node.getHandle(), thumb);
                        ((ViewHolderMessageChat) holder).urlOwnMessageImage.setImageBitmap(thumb);
                    } else {
                        thumb = getThumbnailFromFolder(node, context);
                        if (thumb != null) {
                            previewCache.put(node.getHandle(), thumb);
                            ((ViewHolderMessageChat) holder).urlOwnMessageImage.setImageBitmap(thumb);
                        } else {
                            ((ViewHolderMessageChat) holder).urlOwnMessageImage.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());
                        }
                    }
                    ((ViewHolderMessageChat) holder).urlOwnMessageDescription.setText(getSizeString(node.getSize(), context));
                } else {
                    ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setText(androidMessage.getRichLinkMessage().getFolderName());
                    holder.urlOwnMessageImage.setImageResource(mega.privacy.android.core.R.drawable.ic_folder_list);
                    ((ViewHolderMessageChat) holder).urlOwnMessageDescription.setText(androidMessage.getRichLinkMessage().getFolderContent());
                }
            } else if (androidMessage.getRichLinkMessage() != null && androidMessage.getRichLinkMessage().isChat()) {
                long numParticipants = androidMessage.getRichLinkMessage().getNumParticipants();

                if (numParticipants != -1) {
                    holder.urlOwnMessageText.setOnClickListener(this);
                    holder.urlOwnMessageText.setOnLongClickListener(this);

                    ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setText(androidMessage.getRichLinkMessage().getTitle());
                    ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setMaxLines(1);
                    ((ViewHolderMessageChat) holder).urlOwnMessageDescription.setVisibility(View.VISIBLE);

                    ((ViewHolderMessageChat) holder).urlOwnMessageDescription.setText(context.getString(R.string.number_of_participants, numParticipants));

                    ((ViewHolderMessageChat) holder).urlOwnMessageImage.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).urlOwnMessageGroupAvatarLayout.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).urlOwnMessageGroupAvatar.setImageResource(R.drawable.calls);
                } else {
                    if (isOnline(context)) {
                        if (isMultipleSelect()) {
                            ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(this);
                            ((ViewHolderMessageChat) holder).urlOwnMessageText.setOnClickListener(this);
                        } else {
                            ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(null);
                            ((ViewHolderMessageChat) holder).urlOwnMessageText.setOnClickListener(null);
                        }
                    } else {
                        ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(null);
                        ((ViewHolderMessageChat) holder).urlOwnMessageText.setOnClickListener(null);
                    }

                    ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setVisibility(View.VISIBLE);
                    ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setText(context.getString(R.string.invalid_chat_link));
                    ((ViewHolderMessageChat) holder).urlOwnMessageTitle.setMaxLines(2);
                    ((ViewHolderMessageChat) holder).urlOwnMessageDescription.setVisibility(View.INVISIBLE);

                    ((ViewHolderMessageChat) holder).urlOwnMessageImage.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).urlOwnMessageGroupAvatarLayout.setVisibility(View.GONE);
                }
            } else {
                Timber.w("Chat-link error: null");
            }

            checkMultiselectionMode(position, holder, true, message.getMsgId());
        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);
            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(((ViewHolderMessageChat) holder), userHandle, ((ViewHolderMessageChat) holder).fullNameTitle);
            } else {
                ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.GONE);
            }

            String messageContent = "";
            if (message.getContent() != null) {
                messageContent = converterShortCodes(message.getContent());
            }

            if (message.isEdited()) {
                Timber.d("Message is edited");
                Spannable content = new SpannableString(messageContent);
                content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_087_white_087)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.urlContactMessageText.setText(content + " ");

                Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                edited.setSpan(new RelativeSizeSpan(0.70f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_087_white_087)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.urlContactMessageText.append(edited);
                checkEmojiSize(messageContent, holder.urlContactMessageText);

            } else {
                holder.urlContactMessageText.setText(messageContent);
            }

            checkEmojiSize(messageContent, holder.urlContactMessageText);

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);

            holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
            holder.contactManagementMessageLayout.setVisibility(View.GONE);

            holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);

            holder.contentContactMessageText.setVisibility(View.GONE);
            holder.urlContactMessageLayout.setVisibility(View.VISIBLE);

            //Forwards element (contact messages):
            if (checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                holder.forwardContactRichLinks.setVisibility(View.VISIBLE);
                holder.forwardContactRichLinks.setOnClickListener(this);
                holder.forwardContactRichLinks.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.forwardContactRichLinks.setVisibility(View.GONE);
            }

            holder.contentContactMessageAttachLayout.setVisibility(View.GONE);
            holder.contentContactMessageContactLayout.setVisibility(View.GONE);

            //MEGA link
            holder.urlOwnMessageWarningButtonsLayout.setVisibility(View.GONE);
            holder.urlOwnMessageDisableButtonsLayout.setVisibility(View.GONE);

            holder.urlContactMessageIconAndLinkLayout.setVisibility(View.VISIBLE);
            holder.urlContactMessageLink.setText(androidMessage.getRichLinkMessage().getServer());

            holder.urlContactMessageIcon.setImageResource(R.drawable.ic_launcher);
            holder.urlContactMessageIcon.setVisibility(View.VISIBLE);

            holder.urlContactMessageText.setLinksClickable(false);
            Linkify.addLinks(holder.urlContactMessageText, Linkify.WEB_URLS);
            holder.urlContactMessageText.setOnClickListener(this);
            holder.urlContactMessageText.setOnLongClickListener(this);

            if (isFile || isFolder) {
                ((ViewHolderMessageChat) holder).urlContactMessageTitle.setVisibility(View.VISIBLE);
                ((ViewHolderMessageChat) holder).urlContactMessageTitle.setMaxLines(1);

                ((ViewHolderMessageChat) holder).urlContactMessageImage.setVisibility(View.VISIBLE);
                ((ViewHolderMessageChat) holder).urlContactMessageGroupAvatarLayout.setVisibility(View.GONE);

                if (isFile) {
                    ((ViewHolderMessageChat) holder).urlContactMessageTitle.setText(node.getName());
                    Bitmap thumb = null;
                    thumb = getThumbnailFromCache(node);

                    if (thumb != null) {
                        previewCache.put(node.getHandle(), thumb);
                        ((ViewHolderMessageChat) holder).urlContactMessageImage.setImageBitmap(thumb);
                    } else {
                        thumb = getThumbnailFromFolder(node, context);
                        if (thumb != null) {
                            previewCache.put(node.getHandle(), thumb);
                            ((ViewHolderMessageChat) holder).urlContactMessageImage.setImageBitmap(thumb);
                        } else {
                            ((ViewHolderMessageChat) holder).urlContactMessageImage.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());

                        }
                    }
                    ((ViewHolderMessageChat) holder).urlContactMessageDescription.setText(getSizeString(node.getSize(), context));
                } else {
                    ((ViewHolderMessageChat) holder).urlContactMessageTitle.setText(androidMessage.getRichLinkMessage().getFolderName());
                    ((ViewHolderMessageChat) holder).urlContactMessageImage.setImageResource(mega.privacy.android.core.R.drawable.ic_folder_list);
                    ((ViewHolderMessageChat) holder).urlContactMessageDescription.setText(androidMessage.getRichLinkMessage().getFolderContent());
                }
            } else {
                if (androidMessage.getRichLinkMessage() != null && androidMessage.getRichLinkMessage().isChat()) {

                    ((ViewHolderMessageChat) holder).urlContactMessageImage.setVisibility(View.GONE);
                    ((ViewHolderMessageChat) holder).urlContactMessageGroupAvatarLayout.setVisibility(View.VISIBLE);

                    long numParticipants = androidMessage.getRichLinkMessage().getNumParticipants();
                    if (numParticipants != -1) {
                        holder.urlContactMessageText.setOnClickListener(this);
                        holder.urlContactMessageText.setOnLongClickListener(this);

                        ((ViewHolderMessageChat) holder).urlContactMessageTitle.setVisibility(View.VISIBLE);

                        ((ViewHolderMessageChat) holder).urlContactMessageTitle.setText(androidMessage.getRichLinkMessage().getTitle());
                        ((ViewHolderMessageChat) holder).urlContactMessageTitle.setMaxLines(1);
                        ((ViewHolderMessageChat) holder).urlContactMessageDescription.setVisibility(View.VISIBLE);

                        ((ViewHolderMessageChat) holder).urlContactMessageDescription.setText(context.getString(R.string.number_of_participants, numParticipants));
                        ((ViewHolderMessageChat) holder).urlContactMessageGroupAvatar.setImageResource(R.drawable.calls);
                    } else {
                        if (isOnline(context)) {
                            if (isMultipleSelect()) {
                                ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(this);
                                ((ViewHolderMessageChat) holder).urlContactMessageText.setOnClickListener(this);
                            } else {
                                ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(null);
                                ((ViewHolderMessageChat) holder).urlContactMessageText.setOnClickListener(null);
                            }
                        } else {
                            ((ViewHolderMessageChat) holder).itemLayout.setOnClickListener(null);
                            ((ViewHolderMessageChat) holder).urlContactMessageText.setOnClickListener(null);
                        }

                        ((ViewHolderMessageChat) holder).urlContactMessageTitle.setVisibility(View.VISIBLE);

                        ((ViewHolderMessageChat) holder).urlContactMessageTitle.setText(context.getString(R.string.invalid_chat_link));
                        ((ViewHolderMessageChat) holder).urlContactMessageTitle.setMaxLines(2);
                        ((ViewHolderMessageChat) holder).urlContactMessageDescription.setVisibility(View.INVISIBLE);

                        holder.urlContactMessageGroupAvatarLayout.setVisibility(View.GONE);
                    }
                } else {
                    Timber.w("Chat-link error: richLinkMessage is NULL");
                }
            }

            checkMultiselectionMode(position, holder, false, message.getMsgId());
        }

        checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
    }

    public void bindNormalMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        MegaChatMessage message = androidMessage.getMessage();
        if (message.getUserHandle() == myUserHandle) {
            Timber.d("MY message handle!!: %s", message.getMsgId());
            holder.layoutAvatarMessages.setVisibility(View.GONE);
            holder.titleOwnMessage.setGravity(Gravity.RIGHT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            holder.ownMessageLayout.setVisibility(View.VISIBLE);
            holder.contactMessageLayout.setVisibility(View.GONE);
            holder.ownManagementMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);

            String messageContent = "";

            if (message.getContent() != null) {
                messageContent = converterShortCodes(message.getContent());
            }

            int lastPosition = messages.size();

            if (lastPosition == position) {

                if (MegaChatApi.hasUrl(messageContent)) {
                    if (((ChatActivity) context).checkMegaLink(message) == -1) {
                        Timber.d("Is a link - not from MEGA");
                        if (MegaApplication.isShowRichLinkWarning()) {
                            Timber.w("SDK - show link rich warning");
                            if (((ChatActivity) context).showRichLinkWarning == RICH_WARNING_TRUE) {
                                Timber.w("ANDROID - show link rich warning");

                                holder.urlOwnMessageLayout.setVisibility(View.VISIBLE);
                                holder.urlOwnMessageText.setText(messageContent);
                                holder.urlOwnMessageTitle.setVisibility(View.VISIBLE);
                                holder.urlOwnMessageTitle.setText(context.getString(R.string.title_enable_rich_links));
                                holder.urlOwnMessageTitle.setMaxLines(10);

                                holder.urlOwnMessageDescription.setText(context.getString(R.string.text_enable_rich_links));
                                holder.urlOwnMessageDescription.setMaxLines(30);

                                holder.urlOwnMessageImage.setVisibility(View.VISIBLE);
                                holder.urlOwnMessageGroupAvatarLayout.setVisibility(View.GONE);

                                holder.urlOwnMessageImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rich_link));

                                holder.urlOwnMessageIcon.setVisibility(View.GONE);

                                holder.alwaysAllowRichLinkButton.setOnClickListener(this);
                                holder.alwaysAllowRichLinkButton.setTag(holder);

                                holder.notNowRichLinkButton.setOnClickListener(this);
                                holder.notNowRichLinkButton.setTag(holder);

                                holder.contentOwnMessageText.setVisibility(View.GONE);
                                holder.previewFrameLand.setVisibility(View.GONE);
                                holder.previewFramePort.setVisibility(View.GONE);

                                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                                holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

                                holder.urlOwnMessageWarningButtonsLayout.setVisibility(View.VISIBLE);
                                ((ChatActivity) context).hideKeyboard();
                                holder.urlOwnMessageDisableButtonsLayout.setVisibility(View.GONE);

                                int notNowCounter = MegaApplication.getCounterNotNowRichLinkWarning();

                                if (notNowCounter >= 3) {
                                    holder.neverRichLinkButton.setVisibility(View.VISIBLE);
                                    holder.neverRichLinkButton.setOnClickListener(this);
                                    holder.neverRichLinkButton.setTag(holder);
                                } else {
                                    holder.neverRichLinkButton.setVisibility(View.GONE);
                                    holder.neverRichLinkButton.setOnClickListener(null);
                                }

                                return;
                            } else if (((ChatActivity) context).showRichLinkWarning == RICH_WARNING_CONFIRMATION) {
                                Timber.w("ANDROID - show link disable rich link confirmation");

                                holder.urlOwnMessageLayout.setVisibility(View.VISIBLE);
                                holder.urlOwnMessageText.setText(messageContent);
                                holder.urlOwnMessageTitle.setVisibility(View.VISIBLE);
                                holder.urlOwnMessageTitle.setText(context.getString(R.string.title_confirmation_disable_rich_links));
                                holder.urlOwnMessageTitle.setMaxLines(10);

                                holder.urlOwnMessageDescription.setText(context.getString(R.string.text_confirmation_disable_rich_links));
                                holder.urlOwnMessageDescription.setMaxLines(30);

                                holder.urlOwnMessageImage.setVisibility(View.VISIBLE);
                                ((ViewHolderMessageChat) holder).urlOwnMessageGroupAvatarLayout.setVisibility(View.GONE);
                                holder.urlOwnMessageImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rich_link));

                                holder.urlOwnMessageIcon.setVisibility(View.GONE);

                                holder.noDisableButton.setOnClickListener(this);
                                holder.noDisableButton.setTag(holder);

                                holder.yesDisableButton.setOnClickListener(this);
                                holder.yesDisableButton.setTag(holder);

                                holder.contentOwnMessageText.setVisibility(View.GONE);
                                holder.previewFrameLand.setVisibility(View.GONE);
                                holder.previewFramePort.setVisibility(View.GONE);

                                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                                holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

                                holder.urlOwnMessageWarningButtonsLayout.setVisibility(View.GONE);
                                holder.urlOwnMessageDisableButtonsLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        Timber.w("It a MEGA link: wait for info update");
                    }
                }
            }

            holder.contentOwnMessageText.setBackgroundResource(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ?
                    R.drawable.light_rounded_chat_own_message :
                    R.drawable.dark_rounded_chat_own_message);

            if (message.isEdited()) {
                Timber.d("MY Message is edited");
                holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
                holder.contentOwnMessageText.setVisibility(View.VISIBLE);

                holder.previewFrameLand.setVisibility(View.GONE);
                holder.previewFramePort.setVisibility(View.GONE);

                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
                holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

                int status = message.getStatus();

                if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                    Timber.d("Show triangle retry!");
                    holder.triangleIcon.setVisibility(View.VISIBLE);
                    holder.retryAlert.setVisibility(View.VISIBLE);
                } else if ((status == MegaChatMessage.STATUS_SENDING)) {
                    Timber.d("Status not received by server: %s", message.getStatus());
                    holder.triangleIcon.setVisibility(View.GONE);
                    holder.retryAlert.setVisibility(View.GONE);
                } else {
                    Timber.d("Status: %s", message.getStatus());
                    holder.triangleIcon.setVisibility(View.GONE);
                    holder.retryAlert.setVisibility(View.GONE);
                }

                Spannable content = new SpannableString(messageContent);
                content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.contentOwnMessageText.setText(content + " ");

                Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                edited.setSpan(new RelativeSizeSpan(0.70f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white_alpha_087)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.contentOwnMessageText.append(edited);

                checkEmojiSize(messageContent, holder.contentOwnMessageText);

                if (isOnline(context)) {
                    if (isMultipleSelect()) {
                        holder.contentOwnMessageText.setLinksClickable(false);
                    } else {
                        holder.contentOwnMessageText.setLinksClickable(true);
                        Linkify.addLinks(holder.contentOwnMessageText, Linkify.WEB_URLS);
                    }
                } else {
                    holder.contentOwnMessageText.setLinksClickable(false);
                }

                checkMultiselectionMode(position, holder, true, message.getMsgId());

            } else if (message.isDeleted()) {
                Timber.d("MY Message is deleted");
                holder.contentOwnMessageLayout.setVisibility(View.GONE);
                holder.ownManagementMessageText.setText(context.getString(R.string.text_deleted_message));

                holder.ownManagementMessageLayout.setVisibility(View.GONE);

                holder.previewFrameLand.setVisibility(View.GONE);
                holder.contentOwnMessageThumbLand.setVisibility(View.GONE);
                holder.previewFramePort.setVisibility(View.GONE);
                holder.contentOwnMessageThumbPort.setVisibility(View.GONE);

                holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

                holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
                holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

            } else {
                Timber.d("Message not edited not deleted");

                holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
                holder.ownManagementMessageLayout.setVisibility(View.GONE);
                holder.contentOwnMessageText.setVisibility(View.VISIBLE);

                holder.previewFrameLand.setVisibility(View.GONE);
                holder.previewFramePort.setVisibility(View.GONE);

                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
                holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

                int status = message.getStatus();
                if (status == MegaChatMessage.STATUS_SERVER_REJECTED || status == MegaChatMessage.STATUS_SENDING_MANUAL) {
                    Timber.d("Show triangle retry!");
                    holder.triangleIcon.setVisibility(View.VISIBLE);
                    holder.retryAlert.setVisibility(View.VISIBLE);

                } else if ((status == MegaChatMessage.STATUS_SENDING)) {
                    holder.triangleIcon.setVisibility(View.GONE);
                    holder.retryAlert.setVisibility(View.GONE);
                } else {
                    Timber.d("Status: %s", message.getStatus());

                    holder.triangleIcon.setVisibility(View.GONE);
                    holder.retryAlert.setVisibility(View.GONE);
                }

                checkEmojiSize(messageContent, holder.contentOwnMessageText);
                ((ViewHolderMessageChat) holder).contentOwnMessageText.setTextColor(Color.WHITE);
                ((ViewHolderMessageChat) holder).contentOwnMessageText.setLinkTextColor(Color.WHITE);

                if (!MegaChatApi.hasUrl(messageContent)) {
                    holder.contentOwnMessageText.setText(getFormattedText(messageContent));
                } else {
                    holder.contentOwnMessageText.setText(messageContent);
                }

                if (isOnline(context)) {
                    if (isMultipleSelect()) {
                        holder.contentOwnMessageText.setLinksClickable(false);
                    } else {
                        holder.contentOwnMessageText.setLinksClickable(true);
                        Linkify.addLinks(holder.contentOwnMessageText, Linkify.WEB_URLS);
                    }
                } else {
                    holder.contentOwnMessageText.setLinksClickable(false);
                }

                checkMultiselectionMode(position, holder, true, message.getMsgId());
            }

            interceptLinkClicks(context, holder.contentOwnMessageText);
        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);

            holder.contactManagementMessageLayout.setVisibility(View.GONE);
            holder.contentContactMessageLayout.setVisibility(View.VISIBLE);

            holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);

            if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(((ViewHolderMessageChat) holder), userHandle, ((ViewHolderMessageChat) holder).fullNameTitle);
            } else {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
            }

            String messageContent = "";
            if (message.getContent() != null) {
                messageContent = converterShortCodes(message.getContent());
            }

            if (message.isEdited()) {
                Timber.d("Message is edited");
                ((ViewHolderMessageChat) holder).contentContactMessageText.setVisibility(View.VISIBLE);
                ((ViewHolderMessageChat) holder).urlContactMessageLayout.setVisibility(View.GONE);

                ((ViewHolderMessageChat) holder).contentContactMessageAttachLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contentContactMessageContactLayout.setVisibility(View.GONE);


                Spannable content = new SpannableString(messageContent);
                content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_087_white_087)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.contentContactMessageText.setText(content + " ");

                Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                edited.setSpan(new RelativeSizeSpan(0.70f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_087_white_087)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.contentContactMessageText.append(edited);

                if (isOnline(context)) {
                    if (isMultipleSelect()) {
                        ((ViewHolderMessageChat) holder).contentContactMessageText.setLinksClickable(false);
                    } else {
                        Linkify.addLinks(((ViewHolderMessageChat) holder).contentContactMessageText, Linkify.WEB_URLS);
                        ((ViewHolderMessageChat) holder).contentContactMessageText.setLinksClickable(true);
                    }
                } else {
                    ((ViewHolderMessageChat) holder).contentContactMessageText.setLinksClickable(false);
                }

                checkMultiselectionMode(position, holder, false, message.getMsgId());

            } else if (message.isDeleted()) {
                Timber.d("Message is deleted");

                ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);

                ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).nameContactText.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contentContactMessageThumbLand.setVisibility(View.GONE);

                ((ViewHolderMessageChat) holder).videoIconContactMessageThumbLand.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

                ((ViewHolderMessageChat) holder).contentContactMessageThumbPort.setVisibility(View.GONE);

                ((ViewHolderMessageChat) holder).videoIconContactMessageThumbPort.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);

                ((ViewHolderMessageChat) holder).contentContactMessageAttachLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contentContactMessageContactLayout.setVisibility(View.GONE);

                if (chatRoom != null && chatRoom.isGroup()) {
                    String textToShow = String.format(context.getString(R.string.text_deleted_message_by), toCDATA(((ViewHolderMessageChat) holder).fullNameTitle));
                    try {
                        textToShow = textToShow.replace("[A]", "<font color=\'"
                                + ColorUtils.getThemeColorHexString(context, com.google.android.material.R.attr.colorSecondary)
                                + "\'>");
                        textToShow = textToShow.replace("[/A]", "</font>");
                        textToShow = textToShow.replace("[B]", "<font color=\'"
                                + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                                + "\'>");
                        textToShow = textToShow.replace("[/B]", "</font>");
                    } catch (Exception e) {
                    }
                    Spanned result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);
                    ((ViewHolderMessageChat) holder).contactManagementMessageText.setText(result);
                } else {
                    ((ViewHolderMessageChat) holder).contactManagementMessageText.setText(context.getString(R.string.text_deleted_message));
                }
            } else {

                ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

                ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.VISIBLE);

                ((ViewHolderMessageChat) holder).contentContactMessageText.setVisibility(View.VISIBLE);

                ((ViewHolderMessageChat) holder).contentContactMessageAttachLayout.setVisibility(View.GONE);
                ((ViewHolderMessageChat) holder).urlContactMessageLayout.setVisibility(View.GONE);

                ((ViewHolderMessageChat) holder).contentContactMessageContactLayout.setVisibility(View.GONE);

                checkEmojiSize(messageContent, holder.contentContactMessageText);

                //Color always status SENT
                ((ViewHolderMessageChat) holder).contentContactMessageText.setTextColor(ContextCompat.getColor(context, R.color.grey_087_white));
                ((ViewHolderMessageChat) holder).contentContactMessageText.setLinkTextColor(ContextCompat.getColor(context, R.color.grey_087_white));

                if (!MegaChatApi.hasUrl(messageContent)) {
                    holder.contentContactMessageText.setText(getFormattedText(messageContent));
                } else {
                    holder.contentContactMessageText.setText(messageContent);
                }

                if (isOnline(context)) {
                    if (isMultipleSelect()) {
                        ((ViewHolderMessageChat) holder).contentContactMessageText.setLinksClickable(false);
                    } else {
                        Linkify.addLinks(((ViewHolderMessageChat) holder).contentContactMessageText, Linkify.WEB_URLS);
                        ((ViewHolderMessageChat) holder).contentContactMessageText.setLinksClickable(true);
                    }
                } else {
                    ((ViewHolderMessageChat) holder).contentContactMessageText.setLinksClickable(false);
                }

                checkMultiselectionMode(position, holder, false, message.getMsgId());
            }

            interceptLinkClicks(context, holder.contentContactMessageText);
        }

        checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
    }

    private void checkEmojiSize(String message, EmojiTextView textView) {
        if (EmojiManager.getInstance().isOnlyEmojis(message)) {
            int numEmojis = EmojiManager.getInstance().getNumEmojis(message);
            textView.setLineSpacing(1, 1.2f);
            switch (numEmojis) {
                case 1: {
                    textView.setEmojiSize(dp2px(EMOJI_SIZE_EXTRA_HIGH, outMetrics));
                    break;
                }
                case 2: {
                    textView.setEmojiSize(dp2px(EMOJI_SIZE_HIGH, outMetrics));
                    break;
                }
                case 3: {
                    textView.setEmojiSize(dp2px(EMOJI_SIZE_MEDIUM, outMetrics));
                    break;
                }
                default: {
                    textView.setEmojiSize(dp2px(EMOJI_SIZE, outMetrics));
                    break;
                }
            }
        } else {
            textView.setLineSpacing(1, 1.0f);
            textView.setEmojiSize(dp2px(EMOJI_SIZE, outMetrics));
        }
    }

    public void bindNodeAttachmentMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("position: %s", position);
        MegaChatMessage message = androidMessage.getMessage();
        MegaNodeList nodeList = message.getMegaNodeList();
        if (nodeList != null && nodeList.size() == 1 && nodeList.get(0) != null
                && MimeTypeList.typeForName(nodeList.get(0).getName()).isGIF()) {
            bindGiphyOrGifMessage(holder, androidMessage, position, false);
            return;
        }

        if (message.getUserHandle() == myUserHandle) {
            Timber.d("MY message!!");
            holder.layoutAvatarMessages.setVisibility(View.GONE);

            holder.titleOwnMessage.setGravity(Gravity.RIGHT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }
            Timber.d("MY message handle!!: %s", message.getMsgId());
            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            holder.ownMessageLayout.setVisibility(View.VISIBLE);
            holder.contactMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
            holder.ownManagementMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageText.setVisibility(View.GONE);
            holder.urlOwnMessageLayout.setVisibility(View.GONE);

            //Forwards element (own messages):
            holder.forwardOwnPortrait.setVisibility(View.GONE);
            holder.forwardOwnLandscape.setVisibility(View.GONE);
            if (checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                holder.forwardOwnFile.setVisibility(View.VISIBLE);
                holder.forwardOwnFile.setOnClickListener(this);
                holder.forwardOwnFile.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
                getNodeUseCase.get(message.getMegaNodeList().get(0).getHandle())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((result, throwable) -> {
                            if (throwable != null) {
                                holder.forwardOwnFile.setVisibility(View.GONE);
                            }
                        });
            } else {
                holder.forwardOwnFile.setVisibility(View.GONE);
            }

            holder.previewFrameLand.setVisibility(View.GONE);
            holder.previewFramePort.setVisibility(View.GONE);

            holder.contentOwnMessageFileLayout.setVisibility(View.VISIBLE);
            holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
            holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

            int status = message.getStatus();
            Timber.d("Status: %s", message.getStatus());

            holder.contentOwnMessageFileLayout.setBackgroundResource(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ?
                    R.drawable.light_rounded_chat_own_message :
                    R.drawable.dark_rounded_chat_own_message);

            holder.contentOwnMessageFileThumb.setVisibility(View.VISIBLE);
            holder.contentOwnMessageFileName.setVisibility(View.VISIBLE);
            holder.contentOwnMessageFileSize.setVisibility(View.VISIBLE);

            checkMultiselectionMode(position, holder, true, message.getMsgId());

            if (nodeList != null) {
                if (nodeList.size() == 1) {
                    MegaNode node = nodeList.get(0);

                    Timber.d("Node Handle: %s", node.getHandle());

                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Timber.d("Landscape configuration");
                        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_WIDTH_FILENAME_LAND, context.getResources().getDisplayMetrics());
                        holder.contentOwnMessageFileName.setMaxWidth((int) width);
                        holder.contentOwnMessageFileSize.setMaxWidth((int) width);
                    } else {
                        Timber.d("Portrait configuration");
                        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_WIDTH_FILENAME_PORT, context.getResources().getDisplayMetrics());
                        holder.contentOwnMessageFileName.setMaxWidth((int) width);
                        holder.contentOwnMessageFileSize.setMaxWidth((int) width);
                    }
                    holder.contentOwnMessageFileName.setText(node.getName());

                    long nodeSize = node.getSize();
                    holder.contentOwnMessageFileSize.setText(getSizeString(nodeSize, context));
                    holder.contentOwnMessageFileThumb.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());

                    Bitmap preview = null;
                    if (node.hasPreview()) {
                        preview = getPreviewFromCache(node);
                        if (preview != null) {
                            previewCache.put(node.getHandle(), preview);
                            setOwnPreview(holder, preview, node, checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC), message);
                            if (isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message)) {
                                setErrorStateOnPreview(holder, preview, status);
                            }
                        } else {
                            if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                                holder.errorUploadingFile.setVisibility(View.VISIBLE);
                                holder.retryAlert.setVisibility(View.VISIBLE);
                            }

                            long msgId = message.getMsgId() != MEGACHAT_INVALID_HANDLE ? message.getMsgId() : message.getTempId();
                            try {
                                new MegaChatAdapter.ChatPreviewAsyncTask(holder, msgId).execute(node);
                            } catch (Exception ex) {
                                //Too many AsyncTasks
                                Timber.e(ex, "Too many AsyncTasks");
                            }


                        }
                    } else {
                        Timber.w("Node has no preview on servers");

                        preview = getPreviewFromCache(node);
                        if (preview != null) {

                            previewCache.put(node.getHandle(), preview);
                            if (preview.getWidth() < preview.getHeight()) {
                                setBitmapAndUpdateDimensions(holder.contentOwnMessageThumbPort, preview);

                                if (MimeTypeList.typeForName(node.getName()).isPdf()) {
                                    Timber.d("Is pfd preview");
                                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.VISIBLE);
                                    holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                                    holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

                                } else if (MimeTypeList.typeForName(node.getName()).isVideo()) {
                                    Timber.d("Is video preview");
                                    holder.videoIconOwnMessageThumbPort.setVisibility(View.VISIBLE);
                                    holder.videoTimecontentOwnMessageThumbPort.setText(timeVideo(node));
                                    holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.VISIBLE);
                                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);

                                } else {
                                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);
                                    holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                                    holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
                                }

                                holder.previewFramePort.setVisibility(View.VISIBLE);
                                holder.contentOwnMessageThumbPort.setVisibility(View.VISIBLE);
                                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                                holder.previewFrameLand.setVisibility(View.GONE);
                                holder.contentOwnMessageThumbLand.setVisibility(View.GONE);
                                holder.errorUploadingLandscape.setVisibility(View.GONE);
                                holder.transparentCoatingLandscape.setVisibility(View.GONE);

                                holder.transparentCoatingPortrait.setVisibility(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ? View.VISIBLE : View.GONE);

                                //Forwards element (own messages):
                                holder.forwardOwnFile.setVisibility(View.GONE);
                                holder.forwardOwnLandscape.setVisibility(View.GONE);
                                if (checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                                    holder.forwardOwnPortrait.setVisibility(View.VISIBLE);
                                    holder.forwardOwnPortrait.setOnClickListener(this);
                                    holder.forwardOwnPortrait.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
                                    getNodeUseCase.get(message.getMegaNodeList().get(0).getHandle())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe((result, throwable) -> {
                                                if (throwable != null) {
                                                    holder.forwardOwnPortrait.setVisibility(View.GONE);
                                                }
                                            });

                                } else {
                                    holder.forwardOwnPortrait.setVisibility(View.GONE);
                                }

                                if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                                    holder.errorUploadingPortrait.setVisibility(View.VISIBLE);
                                    holder.retryAlert.setVisibility(View.VISIBLE);
                                } else {
                                    holder.errorUploadingPortrait.setVisibility(View.GONE);
                                    holder.retryAlert.setVisibility(View.GONE);
                                }

                                holder.errorUploadingFile.setVisibility(View.GONE);
                                holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                                holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

                            } else {
                                setBitmapAndUpdateDimensions(holder.contentOwnMessageThumbLand, preview);

                                if (MimeTypeList.typeForName(node.getName()).isPdf()) {
                                    Timber.d("Is pfd preview");
                                    holder.iconOwnTypeDocLandPreview.setVisibility(View.VISIBLE);
                                    holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                                    holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

                                } else if (MimeTypeList.typeForName(node.getName()).isVideo()) {
                                    Timber.d("Is video preview");
                                    holder.videoIconOwnMessageThumbLand.setVisibility(View.VISIBLE);
                                    holder.videoTimecontentOwnMessageThumbLand.setText(timeVideo(node));
                                    holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.VISIBLE);
                                    holder.iconOwnTypeDocLandPreview.setVisibility(View.GONE);
                                } else {
                                    holder.iconOwnTypeDocLandPreview.setVisibility(View.GONE);
                                    holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                                    holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
                                }

                                holder.previewFrameLand.setVisibility(View.VISIBLE);
                                holder.contentOwnMessageThumbLand.setVisibility(View.VISIBLE);
                                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                                holder.previewFramePort.setVisibility(View.GONE);
                                holder.contentOwnMessageThumbPort.setVisibility(View.GONE);
                                holder.errorUploadingPortrait.setVisibility(View.GONE);

                                holder.transparentCoatingPortrait.setVisibility(View.GONE);
                                holder.transparentCoatingLandscape.setVisibility(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ? View.VISIBLE : View.GONE);

                                //Forwards element (own messages):
                                holder.forwardOwnFile.setVisibility(View.GONE);
                                holder.forwardOwnPortrait.setVisibility(View.GONE);
                                if (checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                                    holder.forwardOwnLandscape.setVisibility(View.VISIBLE);
                                    holder.forwardOwnLandscape.setOnClickListener(this);
                                    holder.forwardOwnLandscape.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
                                    getNodeUseCase.get(message.getMegaNodeList().get(0).getHandle())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe((result, throwable) -> {
                                                if (throwable != null) {
                                                    holder.forwardOwnLandscape.setVisibility(View.GONE);
                                                }
                                            });
                                } else {
                                    holder.forwardOwnLandscape.setVisibility(View.GONE);
                                }

                                if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                                    holder.errorUploadingLandscape.setVisibility(View.VISIBLE);
                                    holder.retryAlert.setVisibility(View.VISIBLE);
                                } else {
                                    holder.errorUploadingLandscape.setVisibility(View.GONE);
                                    holder.retryAlert.setVisibility(View.GONE);
                                }

                                holder.errorUploadingFile.setVisibility(View.GONE);
                                holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                                holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
                            }
                        } else {
                            if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                                holder.errorUploadingFile.setVisibility(View.VISIBLE);
                                holder.retryAlert.setVisibility(View.VISIBLE);
                            }
                            try {
                                new MegaChatAdapter.ChatLocalPreviewAsyncTask(((ViewHolderMessageChat) holder), message.getMsgId()).execute(node);

                            } catch (Exception ex) {
                                //Too many AsyncTasks
                                Timber.e(ex, "Too many AsyncTasks");
                            }
                        }
                    }
                } else {
                    long totalSize = 0;
                    int count = 0;
                    for (int i = 0; i < nodeList.size(); i++) {
                        MegaNode temp = nodeList.get(i);
                        count++;
                        Timber.d("Node Handle: %s", temp.getHandle());
                        totalSize = totalSize + temp.getSize();
                    }

                    holder.contentOwnMessageFileSize.setText(getSizeString(totalSize, context));

                    MegaNode node = nodeList.get(0);
                    holder.contentOwnMessageFileThumb.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());
                    if (count == 1) {
                        holder.contentOwnMessageFileName.setText(node.getName());
                    } else {
                        holder.contentOwnMessageFileName.setText(context.getResources().getQuantityString(R.plurals.new_general_num_files, count, count));
                    }
                }
            }
        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
            holder.contactManagementMessageLayout.setVisibility(View.GONE);

            holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);
            if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                holder.layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(holder, userHandle, ((ViewHolderMessageChat) holder).fullNameTitle);
            } else {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
            }

            holder.contentContactMessageText.setVisibility(View.GONE);
            holder.urlContactMessageLayout.setVisibility(View.GONE);

            holder.contentContactMessageThumbLand.setVisibility(View.GONE);

            holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
            holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

            holder.contentContactMessageThumbPort.setVisibility(View.GONE);

            holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
            holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);

            holder.contentContactMessageAttachLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageFile.setVisibility(View.VISIBLE);

            //Forwards element (contact messages):
            holder.forwardContactPreviewPortrait.setVisibility(View.GONE);
            holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
            if (checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                holder.forwardContactFile.setVisibility(View.VISIBLE);
                holder.forwardContactFile.setOnClickListener(this);
                holder.forwardContactFile.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.forwardContactFile.setVisibility(View.GONE);
            }

            holder.contentContactMessageFileThumb.setVisibility(View.VISIBLE);
            holder.contentContactMessageFileName.setVisibility(View.VISIBLE);
            holder.contentContactMessageFileSize.setVisibility(View.VISIBLE);
            holder.contentContactMessageContactLayout.setVisibility(View.GONE);

            checkMultiselectionMode(position, holder, false, message.getMsgId());

            if (nodeList != null) {
                if (nodeList.size() == 1) {
                    MegaNode node = nodeList.get(0);

                    Timber.d("Node Handle: %s", node.getHandle());

                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Timber.d("Landscape configuration");
                        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_WIDTH_FILENAME_LAND, context.getResources().getDisplayMetrics());
                        holder.contentContactMessageFileName.setMaxWidth((int) width);
                        holder.contentContactMessageFileSize.setMaxWidth((int) width);
                    } else {
                        Timber.d("Portrait configuration");
                        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_WIDTH_FILENAME_PORT, context.getResources().getDisplayMetrics());
                        holder.contentContactMessageFileName.setMaxWidth((int) width);
                        holder.contentContactMessageFileSize.setMaxWidth((int) width);
                    }
                    holder.contentContactMessageFileName.setText(node.getName());
                    long nodeSize = node.getSize();
                    holder.contentContactMessageFileSize.setText(getSizeString(nodeSize, context));
                    holder.contentContactMessageFileThumb.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());

                    Timber.d("Get preview of node");
                    Bitmap preview = null;
                    if (node.hasPreview()) {
                        Timber.d("Get preview of node");
                        preview = getPreviewFromCache(node);
                        if (preview != null) {
                            previewCache.put(node.getHandle(), preview);

                            if (preview.getWidth() < preview.getHeight()) {
                                setBitmapAndUpdateDimensions(holder.contentContactMessageThumbPort, preview);

                                if (MimeTypeList.typeForName(node.getName()).isPdf()) {
                                    Timber.d("Contact message - Is pfd preview");
                                    holder.iconContactTypeDocPortraitPreview.setVisibility(View.VISIBLE);
                                    holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
                                    holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);

                                } else if (MimeTypeList.typeForName(node.getName()).isVideo()) {
                                    Timber.d("Contact message - Is video preview");
                                    holder.videoIconContactMessageThumbPort.setVisibility(View.VISIBLE);
                                    holder.videoTimecontentContactMessageThumbPort.setText(timeVideo(node));
                                    holder.videoTimecontentContactMessageThumbPort.setVisibility(View.VISIBLE);
                                    holder.iconContactTypeDocPortraitPreview.setVisibility(View.GONE);

                                } else {
                                    holder.iconContactTypeDocPortraitPreview.setVisibility(View.GONE);
                                    holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
                                    holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
                                }

                                holder.contentContactMessageThumbPort.setVisibility(View.VISIBLE);

                                //Forwards element (contact messages):
                                holder.forwardContactFile.setVisibility(View.GONE);
                                holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
                                if (checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                                    holder.forwardContactPreviewPortrait.setVisibility(View.VISIBLE);
                                    holder.forwardContactPreviewPortrait.setOnClickListener(this);
                                    holder.forwardContactPreviewPortrait.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
                                } else {
                                    holder.forwardContactPreviewPortrait.setVisibility(View.GONE);
                                }

                                holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
                                holder.forwardContactFile.setVisibility(View.GONE);

                                holder.contentContactMessageThumbLand.setVisibility(View.GONE);
                                holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
                                holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);
                                holder.contentContactMessageFile.setVisibility(View.GONE);

                                holder.contentContactMessageFileThumb.setVisibility(View.GONE);
                                holder.contentContactMessageFileName.setVisibility(View.GONE);
                                holder.contentContactMessageFileSize.setVisibility(View.GONE);
                            } else {
                                setBitmapAndUpdateDimensions(holder.contentContactMessageThumbLand, preview);

                                if (MimeTypeList.typeForName(node.getName()).isPdf()) {
                                    Timber.d("Contact message - Is pfd preview");
                                    holder.iconContactTypeDocLandPreview.setVisibility(View.VISIBLE);
                                    holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
                                    holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

                                } else if (MimeTypeList.typeForName(node.getName()).isVideo()) {
                                    Timber.d("Contact message - Is video preview");
                                    holder.videoIconContactMessageThumbLand.setVisibility(View.VISIBLE);
                                    holder.videoTimecontentContactMessageThumbLand.setText(timeVideo(node));
                                    holder.videoTimecontentContactMessageThumbLand.setVisibility(View.VISIBLE);
                                    holder.iconContactTypeDocLandPreview.setVisibility(View.GONE);
                                } else {
                                    holder.iconContactTypeDocLandPreview.setVisibility(View.GONE);
                                    holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
                                    holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);
                                }

                                holder.contentContactMessageThumbLand.setVisibility(View.VISIBLE);

                                //Forwards element (contact messages):
                                holder.forwardContactFile.setVisibility(View.GONE);
                                holder.forwardContactPreviewPortrait.setVisibility(View.GONE);
                                if (checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                                    holder.forwardContactPreviewLandscape.setVisibility(View.VISIBLE);
                                    holder.forwardContactPreviewLandscape.setOnClickListener(this);
                                    holder.forwardContactPreviewLandscape.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
                                } else {
                                    holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
                                }

                                holder.contentContactMessageThumbPort.setVisibility(View.GONE);
                                holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
                                holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
                                holder.contentContactMessageFile.setVisibility(View.GONE);

                                holder.contentContactMessageFileThumb.setVisibility(View.GONE);
                                holder.contentContactMessageFileName.setVisibility(View.GONE);
                                holder.contentContactMessageFileSize.setVisibility(View.GONE);
                            }

                        } else {
                            long msgId = message.getMsgId() != MEGACHAT_INVALID_HANDLE ? message.getMsgId() : message.getTempId();
                            try {
                                new MegaChatAdapter.ChatPreviewAsyncTask(holder, msgId).execute(node);

                            } catch (Exception ex) {
                                //Too many AsyncTasks
                                Timber.e(ex, "Too many AsyncTasks");
                            }
                        }

                    } else {
                        Timber.w("Node has no preview on servers");

                        preview = getPreviewFromCache(node);
                        if (preview != null) {
                            previewCache.put(node.getHandle(), preview);
                            setContactPreview(holder, preview, node);
                        } else {

                            try {
                                new MegaChatAdapter.ChatLocalPreviewAsyncTask(holder, message.getMsgId()).execute(node);

                            } catch (Exception ex) {
                                //Too many AsyncTasks
                                Timber.e(ex, "Too many AsyncTasks");
                            }
                        }
                    }
                } else {
                    long totalSize = 0;
                    int count = 0;
                    for (int i = 0; i < nodeList.size(); i++) {
                        MegaNode temp = nodeList.get(i);
                        count++;
                        totalSize = totalSize + temp.getSize();
                    }
                    ((ViewHolderMessageChat) holder).contentContactMessageFileSize.setText(getSizeString(totalSize, context));
                    MegaNode node = nodeList.get(0);
                    ((ViewHolderMessageChat) holder).contentContactMessageFileThumb.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());
                    if (count == 1) {
                        ((ViewHolderMessageChat) holder).contentContactMessageFileName.setText(node.getName());
                    } else {
                        ((ViewHolderMessageChat) holder).contentContactMessageFileName.setText(context.getResources().getQuantityString(R.plurals.new_general_num_files, count, count));
                    }
                }
            }
        }

        checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
    }

    /**
     * Hides all the layouts related to attachment messages.
     *
     * @param position Position of holder in adapter.
     * @param holder   ViewHolderMessageChat from which the layouts have to be hidden.
     */
    private void hideLayoutsAttachmentMessages(int position, ViewHolderMessageChat holder) {
        if (isHolderNull(position, holder)) {
            return;
        }

        holder.contentOwnMessageFileThumb.setVisibility(View.GONE);
        holder.contentOwnMessageFileName.setVisibility(View.GONE);
        holder.contentOwnMessageFileSize.setVisibility(View.GONE);

        holder.previewFramePort.setVisibility(View.GONE);
        holder.contentOwnMessageThumbPort.setVisibility(View.GONE);
        holder.contentOwnMessageThumbPort.setImageBitmap(null);
        holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
        holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
        holder.iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);

        holder.transparentCoatingPortrait.setVisibility(View.GONE);
        holder.uploadingProgressBarPort.setVisibility(View.GONE);
        holder.errorUploadingPortrait.setVisibility(View.GONE);

        holder.previewFrameLand.setVisibility(View.GONE);
        holder.contentOwnMessageThumbLand.setVisibility(View.GONE);
        holder.contentOwnMessageThumbLand.setImageBitmap(null);
        holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
        holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
        holder.iconOwnTypeDocLandPreview.setVisibility(View.GONE);
        holder.transparentCoatingLandscape.setVisibility(View.GONE);
        holder.uploadingProgressBarLand.setVisibility(View.GONE);
        holder.errorUploadingLandscape.setVisibility(View.GONE);

        holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
        holder.errorUploadingFile.setVisibility(View.GONE);
        holder.retryAlert.setVisibility(View.GONE);

        holder.contentContactMessageFileThumb.setVisibility(View.GONE);
        holder.contentContactMessageFileName.setVisibility(View.GONE);
        holder.contentContactMessageFileSize.setVisibility(View.GONE);

        holder.contentContactMessageAttachLayout.setVisibility(View.GONE);
        holder.contentContactMessageThumbPort.setVisibility(View.GONE);
        holder.contentContactMessageThumbPort.setImageBitmap(null);
        holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
        holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
        holder.iconContactTypeDocPortraitPreview.setVisibility(View.GONE);

        holder.contentContactMessageThumbLand.setVisibility(View.GONE);
        holder.contentContactMessageThumbLand.setImageBitmap(null);
        holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
        holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);
        holder.iconContactTypeDocLandPreview.setVisibility(View.GONE);

        holder.contentContactMessageFile.setVisibility(View.GONE);
    }

    /**
     * Draws a GIF message.
     *
     * @param holder         ViewHolderMessageChat where the message is going to be drawn.
     * @param androidMessage AndroidMegaChatMessage to draw.
     * @param position       Position in adapter.
     * @param isGiphy        True if the message makes reference to a giphy message, false if the message makes reference to a GIF attachment message.
     */
    public void bindGiphyOrGifMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position, boolean isGiphy) {
        MegaChatMessage message = androidMessage.getMessage();
        boolean isPortraitMode = isScreenInPortrait(context);
        boolean enableForward = positionClicked == INVALID_POSITION || positionClicked != position;
        float maxWidth = dp2px(isPortraitMode ? MAX_WIDTH_FILENAME_PORT : MAX_WIDTH_FILENAME_LAND, outMetrics);
        boolean isOwnMessage = message.getUserHandle() == myUserHandle;
        Bitmap preview = null;
        boolean orientationPortrait = true;
        MegaChatGiphy giphy = null;
        MegaNode node = null;
        String gifName = null;
        long gifSize = 0;

        hideLayoutsAttachmentMessages(position, holder);

        if (isGiphy) {
            if (message.getContainsMeta() != null && message.getContainsMeta().getGiphy() != null) {
                giphy = message.getContainsMeta().getGiphy();
                if (giphy == null) {
                    Timber.e("MegaChatGiphy is null");
                    return;
                }

                gifName = giphy.getTitle();
                gifSize = giphy.getWebpSize();
                orientationPortrait = giphy.getWidth() < giphy.getHeight();
            }
        } else if (message.getMegaNodeList() != null) {
            node = message.getMegaNodeList().get(0);
            if (node == null) {
                Timber.e("MegaNode is null");
                return;
            }

            gifName = node.getName();
            gifSize = node.getSize();
            preview = getPreviewFromCache(node);

            if (preview != null) {
                previewCache.put(node.getHandle(), preview);
                orientationPortrait = preview.getWidth() < preview.getHeight();
            } else {
                if (isOwnMessage && checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                    holder.forwardOwnFile.setVisibility(View.VISIBLE);
                    holder.forwardOwnFile.setOnClickListener(this);
                } else {
                    holder.forwardOwnFile.setVisibility(View.GONE);
                }

                if (!isOwnMessage && checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                    holder.forwardContactFile.setVisibility(View.VISIBLE);
                    holder.forwardContactFile.setOnClickListener(this);
                } else {
                    holder.forwardContactFile.setVisibility(View.GONE);
                }

                try {
                    new MegaChatAdapter.ChatPreviewAsyncTask(holder, message.getMsgId()).execute(node);
                } catch (Exception ex) {
                    Timber.e(ex, "Too many AsyncTasks");
                }
            }
        }

        boolean showForwardOrientation = (isGiphy || preview != null) &&
                ((isOwnMessage && checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC) ||
                        (!isOwnMessage && checkForwardVisibilityInContactMsg(isMultipleSelect(), cC))));

        if (messages.get(position - 1).getInfoToShow() != -1) {
            setInfoToShow(position, holder, isOwnMessage, messages.get(position - 1).getInfoToShow(),
                    formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                    formatTime(message));
        }

        if (isOwnMessage) {
            holder.layoutAvatarMessages.setVisibility(View.GONE);
            holder.titleOwnMessage.setGravity(Gravity.END);

            holder.titleOwnMessage.setPadding(0, 0,
                    scaleWidthPx(isPortraitMode ? PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT
                            : PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);


            holder.ownMessageLayout.setVisibility(View.VISIBLE);
            holder.contactMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
            holder.ownManagementMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageText.setVisibility(View.GONE);
            holder.urlOwnMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
            holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

            int status = message.getStatus();
            boolean statusRejectedOrSendingManual = status == MegaChatMessage.STATUS_SERVER_REJECTED || status == MegaChatMessage.STATUS_SENDING_MANUAL;

            checkMultiselectionMode(position, holder, true, message.getMsgId());

            holder.forwardOwnFile.setVisibility(View.GONE);
            if (orientationPortrait) {
                holder.previewFramePort.setVisibility(View.VISIBLE);
                holder.forwardOwnLandscape.setVisibility(View.GONE);

                if (showForwardOrientation) {
                    holder.forwardOwnPortrait.setVisibility(View.VISIBLE);
                    holder.forwardOwnPortrait.setOnClickListener(this);
                    holder.forwardOwnPortrait.setEnabled(enableForward);
                }
            } else {
                holder.previewFrameLand.setVisibility(View.VISIBLE);
                holder.forwardOwnPortrait.setVisibility(View.GONE);

                if (showForwardOrientation) {
                    holder.forwardOwnLandscape.setVisibility(View.VISIBLE);
                    holder.forwardOwnLandscape.setOnClickListener(this);
                    holder.forwardOwnLandscape.setEnabled(enableForward);
                }
            }

            if (isGiphy) {
                setGiphyProperties(giphy, holder, orientationPortrait, true);
            } else if (preview == null) {
                holder.contentOwnMessageFileLayout.setVisibility(View.VISIBLE);
                holder.contentOwnMessageFileLayout.setBackgroundResource(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ?
                        R.drawable.light_rounded_chat_own_message :
                        R.drawable.dark_rounded_chat_own_message);

                holder.contentOwnMessageFileThumb.setVisibility(View.VISIBLE);
                holder.contentOwnMessageFileName.setVisibility(View.VISIBLE);
                holder.contentOwnMessageFileSize.setVisibility(View.VISIBLE);

                holder.contentOwnMessageFileName.setMaxWidth((int) maxWidth);
                holder.contentOwnMessageFileSize.setMaxWidth((int) maxWidth);
                holder.contentOwnMessageFileName.setText(gifName);
                holder.contentOwnMessageFileSize.setText(getSizeString(gifSize, context));

                holder.contentOwnMessageFileThumb.setImageResource(MimeTypeList.typeForName(gifName).getIconResourceId());

                if (statusRejectedOrSendingManual) {
                    holder.errorUploadingFile.setVisibility(View.VISIBLE);
                    holder.retryAlert.setVisibility(View.VISIBLE);
                }
            } else {
                if (node.hasPreview()) {
                    setOwnPreview(holder, preview, node, checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC), message);
                } else {
                    setGIFProperties(node, holder, orientationPortrait, true);
                    setBitmapAndUpdateDimensions(orientationPortrait ? holder.contentOwnMessageThumbPort : holder.contentOwnMessageThumbLand, preview);
                }

                if (isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message)) {
                    setErrorStateOnPreview(holder, preview, message.getStatus());
                }
            }
        } else {
            long userHandle = message.getUserHandle();
            setContactMessageName(position, holder, userHandle, true);

            holder.titleContactMessage.setPadding(scaleWidthPx(isPortraitMode ? CONTACT_MESSAGE_PORT : CONTACT_MESSAGE_LAND, outMetrics),
                    0, 0, 0);

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
            holder.contactManagementMessageLayout.setVisibility(View.GONE);
            holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);
            holder.contentContactMessageText.setVisibility(View.GONE);
            holder.urlContactMessageLayout.setVisibility(View.GONE);

            if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                holder.layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(holder, userHandle, holder.fullNameTitle);
            } else {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
            }

            holder.contentContactMessageAttachLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageContactLayout.setVisibility(View.GONE);

            checkMultiselectionMode(position, holder, false, message.getMsgId());

            holder.forwardContactFile.setVisibility(View.GONE);
            if (orientationPortrait) {
                holder.forwardContactPreviewLandscape.setVisibility(View.GONE);

                if (showForwardOrientation) {
                    holder.forwardContactPreviewPortrait.setVisibility(View.VISIBLE);
                    holder.forwardContactPreviewPortrait.setOnClickListener(this);
                    holder.forwardContactPreviewPortrait.setEnabled(enableForward);
                }
            } else {
                holder.forwardContactPreviewPortrait.setVisibility(View.GONE);

                if (showForwardOrientation) {
                    holder.forwardContactPreviewLandscape.setVisibility(View.VISIBLE);
                    holder.forwardContactPreviewLandscape.setOnClickListener(this);
                    holder.forwardContactPreviewLandscape.setEnabled(enableForward);
                }
            }

            if (isGiphy) {
                setGiphyProperties(giphy, holder, orientationPortrait, false);
            } else if (preview == null) {
                holder.contentContactMessageFile.setVisibility(View.VISIBLE);
                holder.contentContactMessageFileThumb.setVisibility(View.VISIBLE);
                holder.contentContactMessageFileName.setVisibility(View.VISIBLE);
                holder.contentContactMessageFileSize.setVisibility(View.VISIBLE);

                holder.contentContactMessageFileName.setMaxWidth((int) maxWidth);
                holder.contentContactMessageFileSize.setMaxWidth((int) maxWidth);
                holder.contentContactMessageFileName.setText(gifName);
                holder.contentContactMessageFileSize.setText(getSizeString(gifSize, context));

                holder.contentContactMessageFileThumb.setImageResource(MimeTypeList.typeForName(gifName).getIconResourceId());
            } else if (node.hasPreview()) {
                setGIFProperties(node, holder, orientationPortrait, false);
                setBitmapAndUpdateDimensions(orientationPortrait ? holder.contentContactMessageThumbPort : holder.contentContactMessageThumbLand, preview);
            } else {
                setContactPreview(holder, preview, node);
            }
        }

        checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
    }

    /**
     * Updates the dimensions of a ImageView in which a preview is shown,
     * depending on the max size available.
     *
     * @param view       ImageView to update.
     * @param realWidth  Width of the preview.
     * @param realHeight Height of the preview.
     */
    public static void updateViewDimensions(ImageView view, int realWidth, int realHeight) {
        if (view == null) {
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        int maxSize = view.getMaxWidth();
        float factor;

        if (realWidth == realHeight) {
            //Square
            params.height = params.width = maxSize;
        } else if (realHeight > realWidth) {
            //Portrait
            params.height = maxSize;
            factor = (float) maxSize / (float) realHeight;
            params.width = (int) (factor * realWidth);
        } else {
            //Landscape
            params.width = maxSize;
            factor = (float) maxSize / (float) realWidth;
            params.height = (int) (factor * realHeight);
        }

        view.setLayoutParams(params);
    }

    /**
     * Sets a bitmap into ImageView and updates its dimensions depending on
     * the max size available and preview dimensions.
     *
     * @param view   ImageView in which the Bitmap has to be set and the dimensions has to be updated.
     * @param bitmap Bitmap to set into Imageview.
     */
    private void setBitmapAndUpdateDimensions(ImageView view, Bitmap bitmap) {
        if (view == null || bitmap == null) {
            return;
        }

        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }

        updateViewDimensions(view, bitmap.getWidth(), bitmap.getHeight());
        view.setImageBitmap(bitmap);
    }

    /**
     * Updates the background and dimensions of gif view.
     *
     * @param holder          ViewHolderMessageChat where the background and dimensions have to be updated.
     * @param backgroundColor The color to set as background.
     * @param isPortrait      True if the view is portrait, false otherwise.
     * @param isOwnMessage    True if the message is own, false otherwise.
     * @param width           Width of the GIF.
     * @param height          Height of the GIF.
     */
    private void updateGifViewBackgroundAndDimensions(ViewHolderMessageChat holder, int backgroundColor, boolean isPortrait, boolean isOwnMessage, int width, int height) {
        if (holder == null) {
            return;
        }

        if (isPortrait) {
            if (isOwnMessage) {
                holder.gifViewOwnMessageThumbPort.setBackgroundColor(backgroundColor);
                updateViewDimensions(holder.gifViewOwnMessageThumbPort, width, height);
            } else {
                holder.gifViewContactMessageThumbPort.setBackgroundColor(backgroundColor);
                updateViewDimensions(holder.gifViewContactMessageThumbPort, width, height);
            }
        } else if (isOwnMessage) {
            holder.gifViewOwnMessageThumbLand.setBackgroundColor(backgroundColor);
            updateViewDimensions(holder.gifViewOwnMessageThumbLand, width, height);
        } else {
            holder.gifViewContactMessageThumbLand.setBackgroundColor(backgroundColor);
            updateViewDimensions(holder.gifViewContactMessageThumbLand, width, height);
        }
    }

    /**
     * Updates GIF layouts and attributes depending on the orientation, if the messages is own or of a contact
     * and if the Giphy should auto-play itself or not.
     *
     * @param giphy        MegaChatGiphy of the messages containing the Giphy.
     * @param holder       ViewHolderMessageChat where the layouts and attributes have to be updated.
     * @param isPortrait   True if the GIF has portrait orientation, false if has landscape orientation.
     * @param isOwnMessage True if the message is own, false if it is of a contact.
     */
    private void setGiphyProperties(MegaChatGiphy giphy, ViewHolderMessageChat holder, boolean isPortrait, boolean isOwnMessage) {
        if (giphy == null || holder == null) {
            return;
        }

        boolean shouldAutoPlay = giphy.getWebpSize() <= MAX_SIZE_GIF_AUTO_PLAY;

        if (isPortrait) {
            if (isOwnMessage) {
                holder.gifViewOwnMessageThumbPort.setVisibility(View.VISIBLE);
            } else {
                holder.gifViewContactMessageThumbPort.setVisibility(View.VISIBLE);
            }
        } else if (isOwnMessage) {
            holder.gifViewOwnMessageThumbLand.setVisibility(View.VISIBLE);
        } else {
            holder.gifViewContactMessageThumbLand.setVisibility(View.VISIBLE);
        }

        updateGifViewBackgroundAndDimensions(holder, ContextCompat.getColor(context, R.color.grey_050_grey_800),
                isPortrait, isOwnMessage, giphy.getWidth(), giphy.getHeight());
        setGIFAndGiphyProperties(shouldAutoPlay, shouldAutoPlay ? getOriginalGiphySrc(giphy.getWebpSrc()) : null, holder, isPortrait, isOwnMessage);
    }

    /**
     * Updates GIF layouts and attributes depending on the orientation, if the messages is own or of a contact
     * and if the GIF should auto-play itself or not.
     *
     * @param node         MegaNode of the messages containing the GIF.
     * @param holder       ViewHolderMessageChat where the layouts and attributes have to be updated.
     * @param isPortrait   True if the GIF has portrait orientation, false if has landscape orientation.
     * @param isOwnMessage True if the message is own, false if it is of a contact.
     */
    private void setGIFProperties(MegaNode node, ViewHolderMessageChat holder, boolean isPortrait, boolean isOwnMessage) {
        if (node == null || holder == null) {
            return;
        }

        Bitmap preview = getPreviewFromCache(node);
        if (preview != null) {
            updateGifViewBackgroundAndDimensions(holder, Color.TRANSPARENT, isPortrait, isOwnMessage, preview.getWidth(), preview.getHeight());
        }

        setGIFAndGiphyProperties(node.getSize() <= MAX_SIZE_GIF_AUTO_PLAY, getUri(node), holder, isPortrait, isOwnMessage);
    }

    /**
     * Updates GIF layouts and attributes depending on the orientation, if the messages is own or of a contact
     * and if the GIF/Giphy should auto-play itself or not.
     *
     * @param shouldAutoPlay True if should auto-play, false otherwise.
     * @param uri            The Uri to animate if should auto-play, null otherwise.
     * @param holder         ViewHolderMessageChat where the layouts and attributes have to be updated.
     * @param isPortrait     True if the GIF has portrait orientation, false if has landscape orientation.
     * @param isOwnMessage   True if the message is own, false if it is of a contact.
     */
    private void setGIFAndGiphyProperties(boolean shouldAutoPlay, Uri uri, ViewHolderMessageChat holder, boolean isPortrait, boolean isOwnMessage) {
        if (holder == null) {
            return;
        }

        boolean alreadyPlayed = animationsPlaying.contains(uri);
        if (alreadyPlayed || shouldAutoPlay) {
            if (uri != null) {
                if (!alreadyPlayed) {
                    animationsPlaying.add(uri);
                }

                holder.isPlayingAnimation = true;

                if (isPortrait) {
                    if (isOwnMessage) {
                        holder.gifIconOwnMessageThumbPort.setVisibility(View.GONE);
                        loadGifMessage(holder.gifViewOwnMessageThumbPort, holder.gifProgressOwnMessageThumbPort, holder.contentOwnMessageThumbPort, holder.contentOwnMessageFileLayout, uri);
                    } else {
                        holder.gifIconContactMessageThumbPort.setVisibility(View.GONE);
                        loadGifMessage(holder.gifViewContactMessageThumbPort, holder.gifProgressContactMessageThumbPort, holder.contentContactMessageThumbPort, holder.contentContactMessageFile, uri);
                    }
                } else if (isOwnMessage) {
                    holder.gifIconOwnMessageThumbLand.setVisibility(View.GONE);
                    loadGifMessage(holder.gifViewOwnMessageThumbLand, holder.gifProgressOwnMessageThumbLand, holder.contentOwnMessageThumbLand, holder.contentOwnMessageFileLayout, uri);
                } else {
                    holder.gifIconContactMessageThumbLand.setVisibility(View.GONE);
                    loadGifMessage(holder.gifViewContactMessageThumbLand, holder.gifProgressContactMessageThumbLand, holder.contentContactMessageThumbLand, holder.contentContactMessageFile, uri);
                }
            }
        } else {
            holder.isPlayingAnimation = false;

            if (isPortrait) {
                if (isOwnMessage) {
                    holder.gifIconOwnMessageThumbPort.setVisibility(View.VISIBLE);
                } else {
                    holder.gifIconContactMessageThumbPort.setVisibility(View.VISIBLE);
                }
            } else if (isOwnMessage) {
                holder.gifIconOwnMessageThumbLand.setVisibility(View.VISIBLE);
            } else {
                holder.gifIconContactMessageThumbLand.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * If the node is already downloaded, get the local Uri.
     * If not, get the streaming one.
     *
     * @param node MegaNode from which the Uri has to been get.
     * @return The Uri if success, null otherwise.
     */
    private Uri getUri(MegaNode node) {
        String localPath = getLocalFile(node);
        if (localPath != null) {
            return UriUtil.getUriForFile(new File(localPath));
        } else {
            if (megaApi.httpServerIsRunning() == 0) {
                megaApi.httpServerStart();
            }

            String url = megaApi.httpServerGetLocalLink(node);
            if (url != null) {
                return Uri.parse(url);
            }
        }

        return null;
    }

    /**
     * Hides all the layouts related to GIF or Giphy messages.
     *
     * @param position Position of holder in adapter.
     * @param holder   ViewHolderMessageChat from which the layouts have to be hidden.
     */
    private void hideLayoutsGiphyAndGifMessages(int position, ViewHolderMessageChat holder) {
        if (isHolderNull(position, holder)) {
            return;
        }

        holder.gifIconOwnMessageThumbPort.setVisibility(View.GONE);
        holder.gifProgressOwnMessageThumbPort.setVisibility(View.GONE);
        holder.gifViewOwnMessageThumbPort.setVisibility(View.GONE);

        holder.gifIconOwnMessageThumbLand.setVisibility(View.GONE);
        holder.gifProgressOwnMessageThumbLand.setVisibility(View.GONE);
        holder.gifViewOwnMessageThumbLand.setVisibility(View.GONE);

        holder.gifIconContactMessageThumbPort.setVisibility(View.GONE);
        holder.gifProgressContactMessageThumbPort.setVisibility(View.GONE);
        holder.gifViewContactMessageThumbPort.setVisibility(View.GONE);

        holder.gifIconContactMessageThumbLand.setVisibility(View.GONE);
        holder.gifProgressContactMessageThumbLand.setVisibility(View.GONE);
        holder.gifViewContactMessageThumbLand.setVisibility(View.GONE);
    }

    /**
     * Hides all the layouts related to contact link messages.
     *
     * @param position Position of holder in adapter.
     * @param holder   ViewHolderMessageChat from which the layouts have to be hidden.
     */
    private void hideLayoutsContactLinkMessages(int position, ViewHolderMessageChat holder) {
        if (isHolderNull(position, holder)) {
            return;
        }

        holder.ownContactLinkMessage.setVisibility(View.GONE);
        holder.othersContactLinkMessage.setVisibility(View.GONE);
    }

    /**
     * Hides all the layouts related to voice clip messages.
     *
     * @param position Position of holder in adapter.
     * @param holder   ViewHolderMessageChat from which the layouts have to be hidden.
     */
    private void hideLayoutsVoiceClipMessages(int position, ViewHolderMessageChat holder) {
        if (isHolderNull(position, holder)) {
            return;
        }

        holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);
        holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);
    }

    /**
     * Hides all the layouts related to normal messages.
     *
     * @param position Position of holder in adapter.
     * @param holder   ViewHolderMessageChat from which the layouts have to be hidden.
     */
    private void hideLayoutsNormalMessages(int position, ViewHolderMessageChat holder) {
        if (isHolderNull(position, holder)) {
            return;
        }

        holder.contentOwnMessageText.setVisibility(View.GONE);
        holder.contentContactMessageText.setVisibility(View.GONE);
    }

    /**
     * Hides all the layouts related to contact attachment messages.
     *
     * @param position Position of holder in adapter.
     * @param holder   ViewHolderMessageChat from which the layouts have to be hidden.
     */
    private void hideLayoutsContactAttachmentMessages(int position, ViewHolderMessageChat holder) {
        if (isHolderNull(position, holder)) {
            return;
        }

        holder.contentOwnMessageContactLayout.setVisibility(View.GONE);
        holder.contentOwnMessageContactLayoutAvatar.setVisibility(View.GONE);
        holder.contentOwnMessageContactThumb.setVisibility(View.GONE);
        holder.contentOwnMessageContactVerified.setVisibility(View.GONE);
        holder.contentOwnMessageContactStatus.setVisibility(View.GONE);
        holder.contentOwnMessageContactName.setVisibility(View.GONE);
        holder.contentOwnMessageContactEmail.setVisibility(View.GONE);
        holder.forwardOwnContact.setVisibility(View.GONE);
        holder.contentContactMessageContactLayout.setVisibility(View.GONE);
        holder.forwardContactContact.setVisibility(View.GONE);
        holder.contentContactMessageContactLayoutAvatar.setVisibility(View.GONE);
        holder.contentContactMessageContactThumb.setVisibility(View.GONE);
        holder.contentContactMessageContactVerified.setVisibility(View.GONE);
        holder.contentContactMessageContactStatus.setVisibility(View.GONE);
        holder.contentContactMessageContactName.setVisibility(View.GONE);
        holder.contentContactMessageContactEmail.setVisibility(View.GONE);
    }

    /**
     * Hides forward option in messages.
     *
     * @param position Position of holder in adapter.
     * @param holder   ViewHolderMessageChat from which the forward icons have to be hidden.
     */
    private void hideForwardOptions(int position, ViewHolderMessageChat holder) {
        if (isHolderNull(position, holder)) {
            return;
        }
        holder.forwardOwnContact.setVisibility(View.GONE);
        holder.forwardOwnFile.setVisibility(View.GONE);
        holder.forwardOwnPortrait.setVisibility(View.GONE);
        holder.forwardOwnLandscape.setVisibility(View.GONE);
        holder.forwardOwnMessageLocation.setVisibility(View.GONE);
        holder.forwardOwnRichLinks.setVisibility(View.GONE);
        holder.forwardContactContact.setVisibility(View.GONE);
        holder.forwardContactFile.setVisibility(View.GONE);
        holder.forwardContactPreviewPortrait.setVisibility(View.GONE);
        holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
        holder.forwardContactMessageLocation.setVisibility(View.GONE);
        holder.forwardContactRichLinks.setVisibility(View.GONE);
    }

    public void bindVoiceClipAttachmentMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int positionInAdapter) {
        Timber.d("positionInAdapter: %s", positionInAdapter);

        MegaChatMessage message = androidMessage.getMessage();
        final long messageUserHandle = message.getUserHandle();
        final long messageId = message.getMsgId();
        long messageHandle = -1;

        holder.totalDurationOfVoiceClip = 0;
        MegaNodeList nodeListOwn = message.getMegaNodeList();
        if (nodeListOwn.size() >= 1 && isVoiceClip(nodeListOwn.get(0).getName())) {
            holder.totalDurationOfVoiceClip = getVoiceClipDuration(nodeListOwn.get(0));
            messageHandle = message.getMegaNodeList().get(0).getHandle();

        }

        if (messagesPlaying == null) messagesPlaying = new ArrayList<>();
        boolean exist = false;
        if (!messagesPlaying.isEmpty()) {
            for (MessageVoiceClip m : messagesPlaying) {
                if (m.getIdMessage() == messageId) {
                    exist = true;
                    break;
                }
            }
        }
        if (!exist) {
            MessageVoiceClip messagePlaying = new MessageVoiceClip(messageId, messageUserHandle, messageHandle);
            messagesPlaying.add(messagePlaying);
        }

        MessageVoiceClip currentMessagePlaying = null;
        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getIdMessage() == messageId) {
                currentMessagePlaying = m;
                break;
            }
        }


        if (message.getUserHandle() == myUserHandle) {
            Timber.d("MY message!!");
            holder.layoutAvatarMessages.setVisibility(View.GONE);
            holder.titleOwnMessage.setGravity(Gravity.RIGHT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }

            Timber.d("MY message handle!!: %s", message.getMsgId());
            if (messages.get(positionInAdapter - 1).getInfoToShow() != -1) {
                setInfoToShow(positionInAdapter, holder, true, messages.get(positionInAdapter - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            holder.ownMessageLayout.setVisibility(View.VISIBLE);
            holder.contactMessageLayout.setVisibility(View.GONE);

            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
            holder.ownManagementMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageText.setVisibility(View.GONE);
            holder.urlOwnMessageLayout.setVisibility(View.GONE);

            holder.previewFrameLand.setVisibility(View.GONE);
            holder.previewFramePort.setVisibility(View.GONE);

            holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
            holder.contentOwnMessageContactLayout.setVisibility(View.GONE);

            //voice clip elements
            holder.contentOwnMessageVoiceClipLayout.setVisibility(View.VISIBLE);
            holder.contentOwnMessageVoiceClipSeekBar.setMax((int) holder.totalDurationOfVoiceClip);

            holder.contentOwnMessageVoiceClipLayout.setBackgroundResource(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ?
                    R.drawable.light_rounded_chat_own_message :
                    R.drawable.dark_rounded_chat_own_message);

            int status = message.getStatus();
            if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                Timber.d("myMessage: STATUS_SERVER_REJECTED || STATUS_SENDING_MANUAL");
                holder.notAvailableOwnVoiceclip.setVisibility(View.VISIBLE);
                holder.contentOwnMessageVoiceClipPlay.setVisibility(View.GONE);
                holder.uploadingOwnProgressbarVoiceclip.setVisibility(View.GONE);

                holder.errorUploadingVoiceClip.setVisibility(View.VISIBLE);
                holder.retryAlert.setVisibility(View.VISIBLE);

                holder.contentOwnMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);
                holder.contentOwnMessageVoiceClipSeekBar.setEnabled(false);
                holder.contentOwnMessageVoiceClipSeekBar.setProgress(0);
                holder.contentOwnMessageVoiceClipDuration.setText("-:--");

            } else if (status == MegaChatMessage.STATUS_SENDING) {
                Timber.d("myMessage: STATUS_SENDING ");
                holder.uploadingOwnProgressbarVoiceclip.setVisibility(View.VISIBLE);
                holder.notAvailableOwnVoiceclip.setVisibility(View.GONE);
                holder.contentOwnMessageVoiceClipPlay.setVisibility(View.GONE);

                holder.errorUploadingVoiceClip.setVisibility(View.GONE);
                holder.retryAlert.setVisibility(View.GONE);

                holder.contentOwnMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);
                holder.contentOwnMessageVoiceClipSeekBar.setEnabled(false);
                holder.contentOwnMessageVoiceClipSeekBar.setProgress(0);
                holder.contentOwnMessageVoiceClipDuration.setText("-:--");

            } else {
                if ((holder.totalDurationOfVoiceClip == 0) || (currentMessagePlaying.getIsAvailable() == ERROR_VOICE_CLIP_TRANSFER)) {
                    Timber.w("myMessage: SENT -> duraton 0 or available == error");
                    holder.notAvailableOwnVoiceclip.setVisibility(View.VISIBLE);
                    holder.uploadingOwnProgressbarVoiceclip.setVisibility(View.GONE);
                    holder.contentOwnMessageVoiceClipPlay.setVisibility(View.GONE);
                    holder.contentOwnMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);
                    holder.contentOwnMessageVoiceClipSeekBar.setEnabled(false);
                    holder.contentOwnMessageVoiceClipSeekBar.setProgress(0);
                    holder.contentOwnMessageVoiceClipDuration.setText("--:--");

                } else {
                    Timber.d("myMessage: SENT -> available ");
                    boolean isDownloaded = false;
                    File vcFile = CacheFolderManager.buildVoiceClipFile(message.getMegaNodeList().get(0).getName());
                    if (isFileAvailable(vcFile) && vcFile.length() == message.getMegaNodeList().get(0).getSize()) {
                        isDownloaded = true;
                    }

                    if (!isDownloaded) {
                        Timber.d("myMessage: is not downloaded ");
                        holder.uploadingOwnProgressbarVoiceclip.setVisibility(View.VISIBLE);
                        holder.contentOwnMessageVoiceClipPlay.setVisibility(View.GONE);
                        holder.notAvailableOwnVoiceclip.setVisibility(View.GONE);
                        holder.contentOwnMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);
                        holder.contentOwnMessageVoiceClipSeekBar.setEnabled(false);
                        holder.contentOwnMessageVoiceClipSeekBar.setProgress(0);
                        holder.contentOwnMessageVoiceClipDuration.setText("--:--");
                        downloadVoiceClip(holder, positionInAdapter, message.getUserHandle(), message.getMegaNodeList());
                    } else {
                        Timber.d("myMessage: id %dis downloaded", message.getMsgId());
                        if (isDownloaded && currentMessagePlaying.isPlayingWhenTheScreenRotated()) {
                            currentMessagePlaying.setPlayingWhenTheScreenRotated(false);
                            playVoiceClip(currentMessagePlaying, vcFile.getAbsolutePath());
                        }
                        holder.contentOwnMessageVoiceClipPlay.setVisibility(View.VISIBLE);
                        holder.notAvailableOwnVoiceclip.setVisibility(View.GONE);
                        holder.uploadingOwnProgressbarVoiceclip.setVisibility(View.GONE);
                        if (currentMessagePlaying.getMediaPlayer().isPlaying()) {
                            holder.contentOwnMessageVoiceClipPlay.setImageResource(R.drawable.ic_pause_voice_clip);
                        } else {
                            holder.contentOwnMessageVoiceClipPlay.setImageResource(R.drawable.ic_play_voice_clip);
                        }

                        if (currentMessagePlaying.getProgress() == 0) {
                            holder.contentOwnMessageVoiceClipDuration.setText(milliSecondsToTimer(holder.totalDurationOfVoiceClip));
                        } else {
                            holder.contentOwnMessageVoiceClipDuration.setText(milliSecondsToTimer(currentMessagePlaying.getProgress()));
                        }

                        holder.contentOwnMessageVoiceClipSeekBar.setProgress(currentMessagePlaying.getProgress());
                        holder.contentOwnMessageVoiceClipSeekBar.setEnabled(true);
                        holder.contentOwnMessageVoiceClipSeekBar.setEventListener(new DetectorSeekBar.IListener() {
                            @Override
                            public void onClick(DetectorSeekBar detectorSeekBar) {
                            }

                            @Override
                            public void onLongClick(DetectorSeekBar detectorSeekBar) {
                                if (!multipleSelect) {
                                    ((ChatActivity) context).itemLongClick(positionInAdapter);
                                }
                            }
                        });

                        holder.contentOwnMessageVoiceClipSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    updatingSeekBar(messageId, progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }
                        });
                    }
                }

                holder.contentOwnMessageVoiceClipDuration.setVisibility(View.VISIBLE);
                holder.contentOwnMessageVoiceClipSeekBar.setVisibility(View.VISIBLE);

                holder.errorUploadingVoiceClip.setVisibility(View.GONE);
                holder.retryAlert.setVisibility(View.GONE);
            }

            checkMultiselectionMode(positionInAdapter, holder, true, message.getMsgId());

            if (multipleSelect) {
                holder.contentOwnMessageVoiceClipPlay.setOnClickListener(null);
                holder.contentOwnMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);
                holder.contentOwnMessageVoiceClipSeekBar.setEnabled(false);
                if (currentMessagePlaying.getMediaPlayer().isPlaying()) {
                    holder.contentOwnMessageVoiceClipPlay.setImageResource(R.drawable.ic_play_voice_clip);
                    currentMessagePlaying.getMediaPlayer().pause();
                    currentMessagePlaying.setProgress(currentMessagePlaying.getMediaPlayer().getCurrentPosition());
                    currentMessagePlaying.setPaused(true);
                    removeCallBacks();
                }
            } else {
                holder.contentOwnMessageVoiceClipPlay.setOnClickListener(this);
            }

        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message: %s", userHandle);

            setContactMessageName(positionInAdapter, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(positionInAdapter - 1).getInfoToShow() != -1) {
                setInfoToShow(positionInAdapter, holder, false, messages.get(positionInAdapter - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
            holder.contactManagementMessageLayout.setVisibility(View.GONE);

            if (messages.get(positionInAdapter - 1).isShowAvatar() && !isMultipleSelect()) {
                holder.layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(holder, userHandle, holder.fullNameTitle);
            } else {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
            }

            holder.contentContactMessageText.setVisibility(View.GONE);
            holder.urlContactMessageLayout.setVisibility(View.GONE);

            holder.contentContactMessageThumbLand.setVisibility(View.GONE);

            holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
            holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

            holder.contentContactMessageThumbPort.setVisibility(View.GONE);

            holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
            holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);

            holder.contentContactMessageAttachLayout.setVisibility(View.GONE);
            holder.contentContactMessageFile.setVisibility(View.GONE);

            holder.contentContactMessageFileThumb.setVisibility(View.GONE);
            holder.contentContactMessageFileName.setVisibility(View.GONE);
            holder.contentContactMessageFileSize.setVisibility(View.GONE);
            holder.contentContactMessageContactLayout.setVisibility(View.GONE);

            //Voice clip elements:
            holder.contentContactMessageVoiceClipLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageVoiceClipSeekBar.setMax((int) holder.totalDurationOfVoiceClip);

            if ((holder.totalDurationOfVoiceClip == 0) || (currentMessagePlaying.getIsAvailable() == ERROR_VOICE_CLIP_TRANSFER)) {
                Timber.w("ContMessage:SENT -> duraton 0 or available == error");
                holder.notAvailableContactVoiceclip.setVisibility(View.VISIBLE);
                holder.uploadingContactProgressbarVoiceclip.setVisibility(View.GONE);
                holder.contentContactMessageVoiceClipPlay.setVisibility(View.GONE);
                holder.contentContactMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);
                holder.contentContactMessageVoiceClipSeekBar.setEnabled(false);
                holder.contentContactMessageVoiceClipSeekBar.setProgress(0);
                holder.contentContactMessageVoiceClipDuration.setText("--:--");

            } else {

                boolean isDownloaded = false;

                File vcFile = CacheFolderManager.buildVoiceClipFile(message.getMegaNodeList().get(0).getName());
                if (isFileAvailable(vcFile) && vcFile.length() == message.getMegaNodeList().get(0).getSize()) {
                    isDownloaded = true;
                }

                if (!isDownloaded) {
                    Timber.d("ContMessage -> is not downloaded -> downloadVoiceClip");
                    holder.uploadingContactProgressbarVoiceclip.setVisibility(View.VISIBLE);
                    holder.contentContactMessageVoiceClipPlay.setVisibility(View.GONE);
                    holder.notAvailableContactVoiceclip.setVisibility(View.GONE);
                    holder.contentContactMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);
                    holder.contentContactMessageVoiceClipSeekBar.setEnabled(false);
                    holder.contentContactMessageVoiceClipSeekBar.setProgress(0);
                    holder.contentContactMessageVoiceClipDuration.setText("--:--");
                    downloadVoiceClip(holder, positionInAdapter, message.getUserHandle(), message.getMegaNodeList());

                } else {
                    Timber.d("ContMessage -> is downloaded");
                    if (isDownloaded && currentMessagePlaying.isPlayingWhenTheScreenRotated()) {
                        currentMessagePlaying.setPlayingWhenTheScreenRotated(false);
                        playVoiceClip(currentMessagePlaying, vcFile.getAbsolutePath());
                    }

                    holder.contentContactMessageVoiceClipPlay.setVisibility(View.VISIBLE);
                    holder.notAvailableContactVoiceclip.setVisibility(View.GONE);
                    holder.uploadingContactProgressbarVoiceclip.setVisibility(View.GONE);

                    if (currentMessagePlaying.getMediaPlayer().isPlaying()) {
                        holder.contentContactMessageVoiceClipPlay.setImageResource(R.drawable.ic_pause_voice_clip);
                    } else {
                        holder.contentContactMessageVoiceClipPlay.setImageResource(R.drawable.ic_play_voice_clip);
                    }

                    if (currentMessagePlaying.getProgress() == 0) {
                        holder.contentContactMessageVoiceClipDuration.setText(milliSecondsToTimer(holder.totalDurationOfVoiceClip));
                    } else {
                        holder.contentContactMessageVoiceClipDuration.setText(milliSecondsToTimer(currentMessagePlaying.getProgress()));
                    }

                    holder.contentContactMessageVoiceClipSeekBar.setProgress(currentMessagePlaying.getProgress());
                    holder.contentContactMessageVoiceClipSeekBar.setEnabled(true);
                    holder.contentContactMessageVoiceClipSeekBar.setEventListener(new DetectorSeekBar.IListener() {
                        @Override
                        public void onClick(DetectorSeekBar detectorSeekBar) {
                        }

                        @Override
                        public void onLongClick(DetectorSeekBar detectorSeekBar) {
                            if (!multipleSelect) {
                                ((ChatActivity) context).itemLongClick(positionInAdapter);
                            }
                        }
                    });

                    holder.contentContactMessageVoiceClipSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                updatingSeekBar(messageId, progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                }
            }

            holder.contentContactMessageVoiceClipDuration.setVisibility(View.VISIBLE);
            holder.contentContactMessageVoiceClipSeekBar.setVisibility(View.VISIBLE);

            checkMultiselectionMode(positionInAdapter, holder, false, message.getMsgId());

            if (multipleSelect) {
                holder.contentContactMessageVoiceClipPlay.setOnClickListener(null);
                holder.contentContactMessageVoiceClipSeekBar.setOnSeekBarChangeListener(null);
                holder.contentContactMessageVoiceClipSeekBar.setEnabled(false);

                if (currentMessagePlaying.getMediaPlayer().isPlaying()) {
                    holder.contentContactMessageVoiceClipPlay.setImageResource(R.drawable.ic_play_voice_clip);
                    currentMessagePlaying.getMediaPlayer().pause();
                    currentMessagePlaying.setProgress(currentMessagePlaying.getMediaPlayer().getCurrentPosition());
                    currentMessagePlaying.setPaused(true);
                    removeCallBacks();
                }
            } else {
                holder.contentContactMessageVoiceClipPlay.setOnClickListener(this);
            }
        }

        checkReactionsInMessage(positionInAdapter, holder, chatRoom.getChatId(), androidMessage);
    }

    /**
     * Draws a contact link message.
     *
     * @param holder         ViewHolder in which the message has to be drawn.
     * @param androidMessage Android message to draw.
     * @param position       Adapter position.
     */
    private void bindContactLinkMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindContactLinkMessage");

        ContactLink result = androidMessage.getContactLinkResult();
        MegaChatMessage message = androidMessage.getMessage();
        boolean isLandscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean isOwnMessage = message.getUserHandle() == myUserHandle;
        String contentText = converterShortCodes(message.getContent());

        if (messages.get(position - 1).getInfoToShow() != INVALID_VALUE) {
            setInfoToShow(position, holder, isOwnMessage, messages.get(position - 1).getInfoToShow(),
                    formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                    formatTime(message));
        }

        if (isOwnMessage) {
            holder.ownContactLinkMessage.setVisibility(View.VISIBLE);
            holder.contentOwnMessageContactLayout.setVisibility(View.GONE);
            holder.layoutAvatarMessages.setVisibility(View.GONE);
            holder.titleOwnMessage.setGravity(Gravity.END);

            holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(isLandscape
                    ? PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND
                    : PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);

            holder.ownMessageLayout.setVisibility(View.VISIBLE);
            holder.contactMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
            holder.ownManagementMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageText.setVisibility(View.GONE);
            holder.previewFrameLand.setVisibility(View.GONE);
            holder.previewFramePort.setVisibility(View.GONE);
            holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
            holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);

            holder.ownContactLinkTextLayout.setBackgroundResource(
                    isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message)
                            ? R.drawable.light_background_text_rich_link
                            : R.drawable.dark_background_text_rich_link);

            //Forwards element (own messages):
            if (checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                holder.ownContactLinkForward.setVisibility(View.VISIBLE);
                holder.ownContactLinkForward.setOnClickListener(this);
                holder.ownContactLinkForward.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.ownContactLinkForward.setVisibility(View.GONE);
            }

            int status = message.getStatus();
            Timber.d("Status: %s", message.getStatus());

            switch (status) {
                case MegaChatMessage.STATUS_SERVER_REJECTED:
                case MegaChatMessage.STATUS_SENDING_MANUAL:
                    holder.ownContactLinkError.setVisibility(View.VISIBLE);
                    holder.retryAlert.setVisibility(View.VISIBLE);
                    break;

                default:
                    holder.retryAlert.setVisibility(View.GONE);
                    holder.ownContactLinkError.setVisibility(View.GONE);
            }

            if (message.isEdited()) {
                Spannable content = new SpannableString(contentText + " ");
                content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.ownContactLinkText.setText(content);

                Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                edited.setSpan(new RelativeSizeSpan(0.70f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white_alpha_087)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.ownContactLinkText.append(edited);
            } else {
                holder.ownContactLinkText.setText(contentText);
            }

            holder.ownContactLinkName.setText(converterShortCodes(result.getFullName()));
            holder.ownContactLinkSubtitle.setText(result.getEmail());
            checkEmojiSize(contentText, holder.ownContactLinkText);
        } else {
            holder.othersContactLinkMessage.setVisibility(View.VISIBLE);

            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);

            holder.titleContactMessage.setPadding(scaleWidthPx(isLandscape
                    ? CONTACT_MESSAGE_LAND
                    : CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
            holder.contactManagementMessageLayout.setVisibility(View.GONE);

            holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);

            if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                holder.layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(holder, userHandle, holder.fullNameTitle);
            } else {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
            }

            holder.contentContactMessageText.setVisibility(View.GONE);
            holder.contentContactMessageAttachLayout.setVisibility(View.GONE);
            holder.urlContactMessageLayout.setVisibility(View.GONE);

            //Forwards element (contact messages):
            if (checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                holder.othersContactLinkForward.setVisibility(View.VISIBLE);
                holder.othersContactLinkForward.setOnClickListener(this);
                holder.othersContactLinkForward.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.othersContactLinkForward.setVisibility(View.GONE);
            }

            holder.othersContactLinkText.setText(contentText);

            if (message.isEdited()) {
                Spannable content = new SpannableString(contentText + " ");
                content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_087_white_087)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.othersContactLinkText.setText(content);

                Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                edited.setSpan(new RelativeSizeSpan(0.70f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_087_white_087)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.othersContactLinkText.append(edited);
            } else {
                holder.othersContactLinkText.setText(contentText);
            }

            holder.othersContactLinkName.setText(converterShortCodes(result.getFullName()));
            holder.othersContactLinkSubtitle.setText(result.getEmail());
            checkEmojiSize(contentText, holder.othersContactLinkText);
        }

        setContactLinkAvatar(holder, isOwnMessage, result);
        checkMultiselectionMode(position, holder, isOwnMessage, message.getMsgId());
        checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
    }

    public void bindContactAttachmentMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindContactAttachmentMessage");

        MegaChatMessage message = androidMessage.getMessage();
        String email = message.getUserEmail(0);
        boolean isContact = isContact(email);
        MegaUser megaUser = megaApi.getContact(email);
        boolean showVerified = isContact && megaApi.areCredentialsVerified(megaUser);
        boolean showStatus = isContact && !showVerified;

        if (message.getUserHandle() == myUserHandle) {
            holder.layoutAvatarMessages.setVisibility(View.GONE);
            holder.titleOwnMessage.setGravity(Gravity.RIGHT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }

            Timber.d("MY message!!");
            Timber.d("MY message handle!!: %s", message.getMsgId());

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            holder.ownMessageLayout.setVisibility(View.VISIBLE);
            holder.contactMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
            holder.ownManagementMessageLayout.setVisibility(View.GONE);
            holder.contentOwnMessageText.setVisibility(View.GONE);
            holder.previewFrameLand.setVisibility(View.GONE);
            holder.previewFramePort.setVisibility(View.GONE);
            holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
            holder.contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);

            holder.contentOwnMessageContactLayout.setVisibility(View.VISIBLE);
            holder.contentOwnMessageContactLayoutAvatar.setVisibility(View.VISIBLE);
            holder.contentOwnMessageContactThumb.setVisibility(View.VISIBLE);
            holder.contentOwnMessageContactName.setVisibility(View.VISIBLE);
            holder.contentOwnMessageContactEmail.setVisibility(View.VISIBLE);

            holder.contentOwnMessageContactVerified.setVisibility(showVerified ? View.VISIBLE : View.GONE);
            holder.contentOwnMessageContactStatus.setVisibility(showStatus ? View.VISIBLE : View.GONE);
            if (showStatus) {
                setContactStatus(getUserStatus(megaUser.getHandle()), holder.contentOwnMessageContactStatus, StatusIconLocation.STANDARD);
            }

            holder.contentOwnMessageContactLayout.setBackgroundResource(isMsgRemovedOrHasRejectedOrManualSendingStatus(removedMessages, message) ?
                    R.drawable.light_rounded_chat_own_message :
                    R.drawable.dark_rounded_chat_own_message);

            //Forwards element (own messages):
            if (checkForwardVisibilityInOwnMsg(removedMessages, message, isMultipleSelect(), cC)) {
                holder.forwardOwnContact.setVisibility(View.VISIBLE);
                holder.forwardOwnContact.setOnClickListener(this);
                holder.forwardOwnContact.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.forwardOwnContact.setVisibility(View.GONE);
            }

            int status = message.getStatus();
            Timber.d("Status: %s", message.getStatus());
            if ((status == MegaChatMessage.STATUS_SERVER_REJECTED) || (status == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                holder.errorUploadingContact.setVisibility(View.VISIBLE);
                holder.retryAlert.setVisibility(View.VISIBLE);
            } else if (status == MegaChatMessage.STATUS_SENDING) {
                holder.retryAlert.setVisibility(View.GONE);
                holder.errorUploadingContact.setVisibility(View.GONE);
            } else {
                holder.retryAlert.setVisibility(View.GONE);
                holder.errorUploadingContact.setVisibility(View.GONE);
            }

            if (!isScreenInPortrait(context)) {
                Timber.d("Landscape configuration");
                holder.contentOwnMessageContactName.setMaxWidthEmojis(dp2px(MAX_WIDTH_FILENAME_LAND, outMetrics));
                holder.contentOwnMessageContactEmail.setMaxWidthEmojis(dp2px(MAX_WIDTH_FILENAME_LAND, outMetrics));
            } else {
                Timber.d("Portrait configuration");
                holder.contentOwnMessageContactName.setMaxWidthEmojis(dp2px(MAX_WIDTH_FILENAME_PORT, outMetrics));
                holder.contentOwnMessageContactEmail.setMaxWidthEmojis(dp2px(MAX_WIDTH_FILENAME_PORT, outMetrics));
            }

            long userCount = message.getUsersCount();

            if (userCount == 1) {
                holder.contentOwnMessageContactName.setText(converterShortCodes(getNameContactAttachment(message)));
                holder.contentOwnMessageContactEmail.setText(message.getUserEmail(0));
                setUserAvatar(holder, message);
            } else {
                //Show default avatar with userCount
                StringBuilder name = new StringBuilder("");
                name.append(message.getUserName(0));
                for (int i = 1; i < userCount; i++) {
                    name.append(", " + message.getUserName(i));
                }
                holder.contentOwnMessageContactEmail.setText(name);
                String numContacts = context.getResources().getQuantityString(R.plurals.general_selection_num_contacts, (int) userCount, userCount);
                holder.contentOwnMessageContactName.setText(numContacts);
                Drawable drawableDefaultAvatar = getAvatarUseCase.getDefaultAvatarDrawable(context,
                        userCount + "", getSpecificAvatarColor(AVATAR_PRIMARY_COLOR), GetAvatarUseCase.AvatarType.GENERAL);
                holder.contentOwnMessageContactThumb.getHierarchy().setPlaceholderImage(drawableDefaultAvatar);
            }

            checkMultiselectionMode(position, holder, true, message.getMsgId());

        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }
            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
            holder.contactManagementMessageLayout.setVisibility(View.GONE);

            holder.contentContactMessageVoiceClipLayout.setVisibility(View.GONE);

            if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                holder.layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(holder, userHandle, holder.fullNameTitle);
            } else {
                holder.layoutAvatarMessages.setVisibility(View.GONE);
            }

            holder.contentContactMessageText.setVisibility(View.GONE);
            holder.contentContactMessageAttachLayout.setVisibility(View.GONE);
            holder.urlContactMessageLayout.setVisibility(View.GONE);
            holder.contentContactMessageContactLayout.setVisibility(View.VISIBLE);

            holder.contentContactMessageContactVerified.setVisibility(showVerified ? View.VISIBLE : View.GONE);
            holder.contentContactMessageContactStatus.setVisibility(showStatus ? View.VISIBLE : View.GONE);
            if (showStatus) {
                setContactStatus(getUserStatus(megaUser.getHandle()), holder.contentContactMessageContactStatus, StatusIconLocation.STANDARD);
            }

            //Forwards element (contact messages):
            if (checkForwardVisibilityInContactMsg(isMultipleSelect(), cC)) {
                holder.forwardContactContact.setVisibility(View.VISIBLE);
                holder.forwardContactContact.setOnClickListener(this);
                holder.forwardContactContact.setEnabled(positionClicked == INVALID_POSITION || positionClicked != position);
            } else {
                holder.forwardContactContact.setVisibility(View.GONE);
            }

            holder.contentContactMessageContactLayoutAvatar.setVisibility(View.VISIBLE);
            holder.contentContactMessageContactThumb.setVisibility(View.VISIBLE);
            holder.contentContactMessageContactName.setVisibility(View.VISIBLE);
            holder.contentContactMessageContactEmail.setVisibility(View.VISIBLE);

            if (!isScreenInPortrait(context)) {
                holder.contentContactMessageContactName.setMaxWidthEmojis(dp2px(MAX_WIDTH_FILENAME_LAND, outMetrics));
                holder.contentContactMessageContactEmail.setMaxWidthEmojis(dp2px(MAX_WIDTH_FILENAME_LAND, outMetrics));
            } else {
                holder.contentContactMessageContactName.setMaxWidthEmojis(dp2px(MAX_WIDTH_FILENAME_PORT, outMetrics));
                holder.contentContactMessageContactEmail.setMaxWidthEmojis(dp2px(MAX_WIDTH_FILENAME_PORT, outMetrics));
            }

            long userCount = message.getUsersCount();

            if (userCount == 1) {
                holder.contentContactMessageContactName.setText(converterShortCodes(getNameContactAttachment(message)));
                holder.contentContactMessageContactEmail.setText(message.getUserEmail(0));
                setUserAvatar(holder, message);

            } else {
                //Show default avatar with userCount
                StringBuilder name = new StringBuilder("");
                name.append(converterShortCodes(message.getUserName(0)));
                for (int i = 1; i < userCount; i++) {
                    name.append(", " + message.getUserName(i));
                }
                holder.contentContactMessageContactEmail.setText(name);
                String numContacts = context.getResources().getQuantityString(R.plurals.general_selection_num_contacts, (int) userCount, userCount);
                holder.contentContactMessageContactName.setText(numContacts);
                Bitmap bitmap = getDefaultAvatar(getSpecificAvatarColor(AVATAR_PRIMARY_COLOR), userCount + "", AVATAR_SIZE, true);
                holder.contentContactMessageContactThumb.setImageBitmap(bitmap);
            }

            checkMultiselectionMode(position, holder, false, message.getMsgId());
        }

        checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
    }

    /**
     * Method for obtaining the name of an attached contact
     *
     * @param message The message sent or received.
     * @return The name or nick of the contact.
     */
    private String getNameContactAttachment(MegaChatMessage message) {
        String email = message.getUserEmail(0);
        MegaUser megaUser = megaApi.getContact(email);
        String name = getMegaUserNameDB(megaUser);
        if (name == null) {
            name = message.getUserName(0);
            if (isTextEmpty(name)) {
                name = email;
            }
        }
        return name;
    }

    public void bindChangeTitleMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindChangeTitleMessage");
        ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.GONE);

        MegaChatMessage message = androidMessage.getMessage();

        if (message.getUserHandle() == myUserHandle) {

            ((ViewHolderMessageChat) holder).titleOwnMessage.setGravity(Gravity.LEFT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            Timber.d("MY message!!");
            Timber.d("MY message handle!!: %s", message.getMsgId());
            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);

            String messageContent = message.getContent();

            String textToShow = String.format(context.getString(R.string.change_title_messages), toCDATA(megaChatApi.getMyFullname()), toCDATA(messageContent));
            try {
                textToShow = textToShow.replace("[A]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                        + "\'>");
                textToShow = textToShow.replace("[/A]", "</font>");
                textToShow = textToShow.replace("[B]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                        + "\'>");
                textToShow = textToShow.replace("[/B]", "</font>");
                textToShow = textToShow.replace("[C]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                        + "\'>");
                textToShow = textToShow.replace("[/C]", "</font>");
            } catch (Exception e) {
            }

            Spanned result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);

            ((ViewHolderMessageChat) holder).contentOwnMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).ownManagementMessageIcon.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsOwnManagement = (RelativeLayout.LayoutParams) holder.ownManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
            } else {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
            }
            holder.ownManagementMessageText.setLayoutParams(paramsOwnManagement);

            ((ViewHolderMessageChat) holder).ownManagementMessageText.setText(result);

        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

            ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactManagementMessageIcon.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsContactManagement = (RelativeLayout.LayoutParams) holder.contactManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
            } else {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
            }
            holder.contactManagementMessageText.setLayoutParams(paramsContactManagement);

            ((ViewHolderMessageChat) holder).nameContactText.setVisibility(View.GONE);

            String messageContent = message.getContent();

            String textToShow = String.format(context.getString(R.string.change_title_messages), toCDATA(holder.fullNameTitle), toCDATA(messageContent));
            try {
                textToShow = textToShow.replace("[A]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                        + "\'>");
                textToShow = textToShow.replace("[/A]", "</font>");
                textToShow = textToShow.replace("[B]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                        + "\'>");
                textToShow = textToShow.replace("[/B]", "</font>");
                textToShow = textToShow.replace("[C]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                        + "\'>");
                textToShow = textToShow.replace("[/C]", "</font>");
            } catch (Exception e) {
            }

            Spanned result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);

            ((ViewHolderMessageChat) holder).contactManagementMessageText.setText(result);
        }
    }

    public void bindChatLinkMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindChatLinkMessage");

        MegaChatMessage message = androidMessage.getMessage();
        long userHandle = message.getUserHandle();
        Timber.d("Contact message!!: %s", userHandle);

        if (userHandle == myUserHandle) {
            ((ViewHolderMessageChat) holder).fullNameTitle = megaChatApi.getMyFullname();
        } else {
            setContactMessageName(position, holder, userHandle, false);
        }

        ((ViewHolderMessageChat) holder).nameContactText.setVisibility(View.GONE);

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
        } else {
            holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
        }

        if (messages.get(position - 1).getInfoToShow() != -1) {
            setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                    formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                    formatTime(message));
        }

        ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

        ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.GONE);
        ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);
        ((ViewHolderMessageChat) holder).contactManagementMessageIcon.setVisibility(View.GONE);

        checkMultiselectionMode(position, holder, false, message.getMsgId());

        String textToShow = "";
        int messageType = message.getType();
        if (messageType == MegaChatMessage.TYPE_PUBLIC_HANDLE_CREATE) {
            textToShow = String.format(context.getString(R.string.message_created_chat_link), toCDATA(holder.fullNameTitle));
            try {
                textToShow = textToShow.replace("[A]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                        + "\'>");
                textToShow = textToShow.replace("[/A]", "</font>");
                textToShow = textToShow.replace("[B]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                        + "\'>");
                textToShow = textToShow.replace("[/B]", "</font>");
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e(e, "Exception formating the string: %s", context.getString(R.string.message_created_chat_link));
            }
        } else if (messageType == MegaChatMessage.TYPE_PUBLIC_HANDLE_DELETE) {
            textToShow = String.format(context.getString(R.string.message_deleted_chat_link), toCDATA(holder.fullNameTitle));
            try {
                textToShow = textToShow.replace("[A]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                        + "\'>");
                textToShow = textToShow.replace("[/A]", "</font>");
                textToShow = textToShow.replace("[B]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                        + "\'>");
                textToShow = textToShow.replace("[/B]", "</font>");
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e(e, "Exception formating the string: %s", context.getString(R.string.message_deleted_chat_link));
            }
        } else {
            textToShow = String.format(context.getString(R.string.message_set_chat_private), toCDATA(holder.fullNameTitle));
            try {
                textToShow = textToShow.replace("[A]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                        + "\'>");
                textToShow = textToShow.replace("[/A]", "</font>");
                textToShow = textToShow.replace("[B]", "<font color=\'"
                        + ColorUtils.getColorHexString(context, R.color.grey_500_grey_400)
                        + "\'><b>");
                textToShow = textToShow.replace("[/B]", "</b></font><br/><br/>");
                textToShow += context.getString(R.string.subtitle_chat_message_enabled_ERK);
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e(e, "Exception formating the string: %s", context.getString(R.string.message_set_chat_private));
            }
        }

        Spanned result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);
        ((ViewHolderMessageChat) holder).contactManagementMessageText.setText(result);

        checkReactionsInMessage(position, holder, chatRoom.getChatId(), androidMessage);
    }

    /**
     * Draws a Retention Time message.
     *
     * @param holder         ViewHolderMessageChat where the message is going to be drawn.
     * @param androidMessage AndroidMegaChatMessage to draw.
     * @param position       Position in adapter.
     */
    public void bindRetentionTimeMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        holder.layoutAvatarMessages.setVisibility(View.VISIBLE);
        MegaChatMessage message = androidMessage.getMessage();

        if (messages.get(position - 1).getInfoToShow() != INVALID_INFO) {
            setInfoToShow(position, holder, isMyMessage(message), messages.get(position - 1).getInfoToShow(),
                    formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                    formatTime(message));
        }

        String timeFormatted = transformSecondsInString(androidMessage.getMessage().getRetentionTime());

        if (isMyMessage(message)) {
            Timber.d("MY message handle!!: %s", message.getMsgId());

            holder.titleOwnMessage.setGravity(Gravity.START);
            holder.titleOwnMessage.setPadding(scaleWidthPx(isScreenInPortrait(context) ?
                    MANAGEMENT_MESSAGE_PORT :
                    MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);

            holder.ownMessageLayout.setVisibility(View.VISIBLE);
            holder.contactMessageLayout.setVisibility(View.GONE);

            holder.contentOwnMessageLayout.setVisibility(View.GONE);
            holder.ownManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.grey_600_white_087));
            holder.ownManagementMessageText.setText(getRetentionTimeString(megaChatApi.getMyFullname(), timeFormatted));

            holder.ownManagementMessageLayout.setVisibility(View.VISIBLE);
            holder.ownManagementMessageIcon.setVisibility(View.GONE);
            RelativeLayout.LayoutParams paramsOwnManagement = (RelativeLayout.LayoutParams) holder.ownManagementMessageText.getLayoutParams();
            paramsOwnManagement.leftMargin = scaleWidthPx(isScreenInPortrait(context) ? MANAGEMENT_MESSAGE_PORT : MANAGEMENT_MESSAGE_LAND, outMetrics);
            holder.ownManagementMessageText.setLayoutParams(paramsOwnManagement);
        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);
            holder.titleContactMessage.setPadding(scaleWidthPx(isScreenInPortrait(context) ?
                    MANAGEMENT_MESSAGE_PORT :
                    MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);

            holder.ownMessageLayout.setVisibility(View.GONE);
            holder.contactMessageLayout.setVisibility(View.VISIBLE);
            holder.contentContactMessageLayout.setVisibility(View.GONE);
            holder.contactManagementMessageLayout.setVisibility(View.VISIBLE);
            holder.contactManagementMessageIcon.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsContactManagement = (RelativeLayout.LayoutParams) holder.contactManagementMessageText.getLayoutParams();
            paramsContactManagement.leftMargin = scaleWidthPx(isScreenInPortrait(context) ?
                    MANAGEMENT_MESSAGE_PORT :
                    MANAGEMENT_MESSAGE_LAND, outMetrics);
            holder.contactManagementMessageText.setLayoutParams(paramsContactManagement);
            holder.nameContactText.setVisibility(View.GONE);

            holder.contactManagementMessageText.setText(getRetentionTimeString(holder.fullNameTitle, timeFormatted));
        }
    }

    /**
     * Method for obtaining the correct retention time changed message String.
     *
     * @param fullName      The name of the user
     * @param timeFormatted The retention time
     * @return The text of the msg
     */
    private Spanned getRetentionTimeString(String fullName, String timeFormatted) {
        String textToShow;
        if (isTextEmpty(timeFormatted)) {
            textToShow = String.format(context.getString(R.string.retention_history_disabled), toCDATA(fullName));
        } else {
            textToShow = String.format(context.getString(R.string.retention_history_changed_by), toCDATA(fullName), timeFormatted);
        }
        return replaceFormatChatMessages(context, textToShow, true);
    }

    public void bindTruncateMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindTruncateMessage");
        ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.VISIBLE);
        MegaChatMessage message = androidMessage.getMessage();
        if (message.getUserHandle() == myUserHandle) {

            ((ViewHolderMessageChat) holder).titleOwnMessage.setGravity(Gravity.LEFT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }
            Timber.d("MY message!!");
            Timber.d("MY message handle!!: %s", message.getMsgId());

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageLayout.setVisibility(View.GONE);

            String textToShow = String.format(context.getString(R.string.history_cleared_by), toCDATA(megaChatApi.getMyFullname()));
            holder.ownManagementMessageText.setText(replaceFormatChatMessages(context, textToShow, true));

            ((ViewHolderMessageChat) holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).ownManagementMessageIcon.setVisibility(View.GONE);
            RelativeLayout.LayoutParams paramsOwnManagement = (RelativeLayout.LayoutParams) holder.ownManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
            } else {
                paramsOwnManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
            }
            holder.ownManagementMessageText.setLayoutParams(paramsOwnManagement);

        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

            ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactManagementMessageIcon.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsContactManagement = (RelativeLayout.LayoutParams) holder.contactManagementMessageText.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
            } else {
                paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
            }
            holder.contactManagementMessageText.setLayoutParams(paramsContactManagement);

            ((ViewHolderMessageChat) holder).nameContactText.setVisibility(View.GONE);

            String textToShow = String.format(context.getString(R.string.history_cleared_by), toCDATA(holder.fullNameTitle));
            holder.contactManagementMessageText.setText(replaceFormatChatMessages(context, textToShow, false));
        }
    }

    public void bindRevokeNodeMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindRevokeNodeMessage()");
        ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.VISIBLE);
        MegaChatMessage message = androidMessage.getMessage();

        if (message.getUserHandle() == myUserHandle) {
            Timber.d("MY message!!");
            Timber.d("MY message handle!!: %s", message.getMsgId());

            ((ViewHolderMessageChat) holder).titleOwnMessage.setGravity(Gravity.LEFT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);

            String messageContent = "";

            if (message.getContent() != null) {
                messageContent = message.getContent();
            }

            AndroidMegaChatMessage androidMsg = messages.get(position - 1);

            ((ViewHolderMessageChat) holder).contentOwnMessageText.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).previewFrameLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageThumbLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).previewFramePort.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconOwnMessageThumbPort.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconOwnMessageThumbLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageFileLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageFileThumb.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageFileName.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageFileSize.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageContactLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageContactThumb.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageContactName.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageContactEmail.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setTextColor(Color.WHITE);
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setLinkTextColor(Color.WHITE);
            messageContent = "Attachment revoked";
            holder.contentOwnMessageText.setText(getFormattedText(messageContent));
        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);

            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

            if (messages.get(position - 1).isShowAvatar() && !isMultipleSelect()) {
                ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.VISIBLE);
                setContactAvatar(((ViewHolderMessageChat) holder), userHandle, ((ViewHolderMessageChat) holder).fullNameTitle);
            } else {
                ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.GONE);
            }

            ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.GONE);

            String messageContent = "";

            ((ViewHolderMessageChat) holder).contentContactMessageText.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contentContactMessageThumbLand.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageVoiceClipLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconContactMessageThumbLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconContactMessageThumbPort.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageAttachLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageFile.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageFileThumb.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageFileName.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageFileSize.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageContactLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageContactThumb.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageContactName.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageContactEmail.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageText.setTextColor(ContextCompat.getColor(context, R.color.grey_087_white));
            ((ViewHolderMessageChat) holder).contentContactMessageText.setLinkTextColor(ContextCompat.getColor(context, R.color.grey_087_white));
            messageContent = "Attachment revoked";
            holder.contentContactMessageText.setText(getFormattedText(messageContent));
        }
    }

    public void hideMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("hideMessage");
        ((ViewHolderMessageChat) holder).itemLayout.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, 0);
        params.height = 0;
        ((ViewHolderMessageChat) holder).itemLayout.setLayoutParams(params);
    }

    public void bindNoTypeMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindNoTypeMessage()");

        ((ViewHolderMessageChat) holder).layoutAvatarMessages.setVisibility(View.VISIBLE);
        MegaChatMessage message = androidMessage.getMessage();
        if (message.getUserHandle() == myUserHandle) {
            Timber.d("MY message handle!!: %s", message.getMsgId());

            ((ViewHolderMessageChat) holder).titleOwnMessage.setGravity(Gravity.RIGHT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                ((ViewHolderMessageChat) holder).titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, true, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setTextColor(ColorUtils.getThemeColor(context, com.google.android.material.R.attr.colorError));
            ((ViewHolderMessageChat) holder).contentOwnMessageText.setLinkTextColor(ColorUtils.getThemeColor(context, com.google.android.material.R.attr.colorError));


            if (message.getType() == MegaChatMessage.TYPE_INVALID) {
                if (message.getCode() == MegaChatMessage.INVALID_FORMAT) {
                    ((ViewHolderMessageChat) holder).contentOwnMessageText.setText(context.getString(R.string.error_message_invalid_format));
                } else if (message.getCode() == MegaChatMessage.INVALID_SIGNATURE) {
                    ((ViewHolderMessageChat) holder).contentOwnMessageText.setText(context.getString(R.string.error_message_invalid_signature));
                } else {
                    ((ViewHolderMessageChat) holder).contentOwnMessageText.setText(context.getString(R.string.error_message_unrecognizable));
                }
            } else {
                ((ViewHolderMessageChat) holder).contentOwnMessageText.setText(context.getString(R.string.error_message_unrecognizable));
            }

            ((ViewHolderMessageChat) holder).contentOwnMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).ownManagementMessageLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).previewFrameLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageThumbLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).previewFramePort.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconOwnMessageThumbPort.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconOwnMessageThumbLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageFileLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageFileThumb.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageFileName.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageFileSize.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageVoiceClipLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageContactLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentOwnMessageContactThumb.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageContactName.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentOwnMessageContactEmail.setVisibility(View.GONE);

        } else {
            long userHandle = message.getUserHandle();
            Timber.d("Contact message!!: %s", userHandle);
            setContactMessageName(position, holder, userHandle, true);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_LAND, outMetrics), 0, 0, 0);
            } else {
                holder.titleContactMessage.setPadding(scaleWidthPx(CONTACT_MESSAGE_PORT, outMetrics), 0, 0, 0);
            }

            if (messages.get(position - 1).getInfoToShow() != -1) {
                setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(),
                        formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context),
                        formatTime(message));
            }

            ((ViewHolderMessageChat) holder).ownMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contactMessageLayout.setVisibility(View.VISIBLE);

            ((ViewHolderMessageChat) holder).contentContactMessageLayout.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contactManagementMessageLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageText.setTextColor(ColorUtils.getThemeColor(context, com.google.android.material.R.attr.colorError));
            ((ViewHolderMessageChat) holder).contentContactMessageText.setLinkTextColor(ColorUtils.getThemeColor(context, com.google.android.material.R.attr.colorError));


            if (message.getType() == MegaChatMessage.TYPE_INVALID) {
                if (message.getCode() == MegaChatMessage.INVALID_FORMAT) {
                    ((ViewHolderMessageChat) holder).contentContactMessageText.setText(context.getString(R.string.error_message_invalid_format));
                } else if (message.getCode() == MegaChatMessage.INVALID_SIGNATURE) {
                    ((ViewHolderMessageChat) holder).contentContactMessageText.setText(context.getString(R.string.error_message_invalid_signature));
                } else {
                    ((ViewHolderMessageChat) holder).contentContactMessageText.setText(context.getString(R.string.error_message_unrecognizable));
                }
            } else {
                ((ViewHolderMessageChat) holder).contentContactMessageText.setText(context.getString(R.string.error_message_unrecognizable));
            }

            ((ViewHolderMessageChat) holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            ((ViewHolderMessageChat) holder).contentContactMessageText.setVisibility(View.VISIBLE);
            ((ViewHolderMessageChat) holder).contentContactMessageThumbLand.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageVoiceClipLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconContactMessageThumbLand.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).videoIconContactMessageThumbPort.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageAttachLayout.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageFile.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageFileThumb.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageFileName.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageFileSize.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageContactLayout.setVisibility(View.GONE);

            ((ViewHolderMessageChat) holder).contentContactMessageContactThumb.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageContactName.setVisibility(View.GONE);
            ((ViewHolderMessageChat) holder).contentContactMessageContactEmail.setVisibility(View.GONE);
        }
    }

    public void bindSchedMeetingMessage(ViewHolderMessageChat holder, AndroidMegaChatMessage androidMessage, int position) {
        Timber.d("bindSchedMeetingMessage");

        MegaChatMessage message = androidMessage.getMessage();

        holder.layoutAvatarMessages.setVisibility(View.GONE);
        setContactMessageName(position, holder, message.getUserHandle(), true);

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics), 0, 0, 0);
        } else {
            holder.titleContactMessage.setPadding(scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics), 0, 0, 0);
        }

        if (messages.get(position - 1).getInfoToShow() != -1) {
            setInfoToShow(position, holder, false, messages.get(position - 1).getInfoToShow(), formatDate(message.getTimestamp(), DATE_SHORT_FORMAT, context), formatTime(message));
        }

        holder.ownMessageLayout.setVisibility(View.GONE);
        holder.contactMessageLayout.setVisibility(View.VISIBLE);

        holder.contentContactMessageLayout.setVisibility(View.GONE);
        holder.contactManagementMessageLayout.setVisibility(View.VISIBLE);
        holder.contactManagementMessageIcon.setVisibility(View.GONE);

        RelativeLayout.LayoutParams paramsContactManagement = (RelativeLayout.LayoutParams) holder.contactManagementMessageText.getLayoutParams();
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_LAND, outMetrics);
        } else {
            paramsContactManagement.leftMargin = scaleWidthPx(MANAGEMENT_MESSAGE_PORT, outMetrics);
        }
        holder.contactManagementMessageText.setLayoutParams(paramsContactManagement);

        holder.nameContactText.setVisibility(View.GONE);

        String textToShow = String.format(context.getString(R.string.chat_chat_room_message_updated_scheduled_meeting), toCDATA(holder.fullNameTitle));
        holder.contactManagementMessageText.setText(replaceFormatChatMessages(context, textToShow, true));
    }

    /**
     * Sets the avatar of a MegaUser in a contact link message.
     *
     * @param holder     ViewHolder where the avatar has to be set.
     * @param ownMessage True if the message is own, false otherwise.
     */
    private void setContactLinkAvatar(ViewHolderMessageChat holder, boolean ownMessage, ContactLink result) {
        if (result == null
                || result.getContactHandle() == null
                || result.getEmail() == null
                || result.getFullName() == null) {
            return;
        }

        long handle = result.getContactHandle();
        String userHandleEncoded = MegaApiAndroid.userHandleToBase64(handle);
        String email = result.getEmail();

        int color = result.isContact() ? getColorAvatar(megaApi.getContact(email)) : getColorAvatar(userHandleEncoded);
        Drawable drawableDefaultAvatar = getAvatarUseCase.getDefaultAvatarDrawable(context,
                result.getFullName(), color, GetAvatarUseCase.AvatarType.LINK);

        if (ownMessage) {
            holder.ownContactLinkAvatar.getHierarchy().setPlaceholderImage(drawableDefaultAvatar);
        } else {
            holder.othersContactLinkAvatar.getHierarchy().setPlaceholderImage(drawableDefaultAvatar);
        }

        File avatar = CacheFolderManager.buildAvatarFile(result.getEmail() + JPG_EXTENSION);
        Uri uri = getAvatarUri(avatar);

        if (uri == null) {
            getAvatarUseCase.get(email, avatar.getAbsolutePath())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((uriResult, throwable) -> {
                        if (throwable == null) {
                            if (ownMessage) {
                                holder.ownContactLinkAvatar.setImageRequest(ImageRequest.fromUri(uriResult));
                            } else {
                                holder.othersContactLinkAvatar.setImageRequest(ImageRequest.fromUri(uriResult));
                            }
                        }
                    });
        } else if (ownMessage) {
            holder.ownContactLinkAvatar.setImageRequest(ImageRequest.fromUri(uri));
        } else {
            holder.othersContactLinkAvatar.setImageRequest(ImageRequest.fromUri(uri));
        }
    }

    public void setUserAvatar(ViewHolderMessageChat holder, MegaChatMessage message) {
        Timber.d("setUserAvatar");

        String name = message.getUserName(0);
        if (name.trim().isEmpty()) {
            name = message.getUserEmail(0);
        }
        String email = message.getUserEmail(0);
        String userHandleEncoded = MegaApiAndroid.userHandleToBase64(message.getUserHandle(0));

        int color = getColorAvatar(userHandleEncoded);
        Drawable drawableDefaultAvatar = getAvatarUseCase.getDefaultAvatarDrawable(context, name,
                color, GetAvatarUseCase.AvatarType.GENERAL);

        if (myUserHandle == message.getUserHandle()) {
            holder.contentOwnMessageContactThumb.getHierarchy().setPlaceholderImage(drawableDefaultAvatar);
        } else {
            holder.contentContactMessageContactThumb.getHierarchy().setPlaceholderImage(drawableDefaultAvatar);
        }

        File avatar = CacheFolderManager.buildAvatarFile(email + JPG_EXTENSION);
        Uri uri = getAvatarUri(avatar);

        if (uri == null) {
            getAvatarUseCase.get(email, avatar.getAbsolutePath())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((result, throwable) -> {
                        if (throwable == null) {
                            if (myUserHandle == message.getUserHandle()) {
                                holder.contentOwnMessageContactThumb.setImageRequest(ImageRequest.fromUri(result));
                            } else {
                                holder.contentContactMessageContactThumb.setImageRequest(ImageRequest.fromUri(result));
                            }
                        }
                    });
        } else if (myUserHandle == message.getUserHandle()) {
            holder.contentOwnMessageContactThumb.setImageRequest(ImageRequest.fromUri(uri));
        } else {
            holder.contentContactMessageContactThumb.setImageRequest(ImageRequest.fromUri(uri));
        }
    }

    private void setContactAvatar(ViewHolderMessageChat holder, long userHandle, String name) {
        /*Default Avatar*/
        String userHandleEncoded = MegaApiAndroid.userHandleToBase64(userHandle);
        holder.contactImageView.getHierarchy().setPlaceholderImage(getAvatarUseCase
                .getDefaultAvatarDrawable(context, name, getColorAvatar(userHandle), GetAvatarUseCase.AvatarType.MINI));

        /*Avatar*/
        holder.contactImageView.setImageRequest(null);
        File avatar = CacheFolderManager.buildAvatarFile(userHandleEncoded + JPG_EXTENSION);
        Uri uri = getAvatarUri(avatar);

        if (uri == null) {
            getAvatarUseCase.get(userHandleEncoded, avatar.getAbsolutePath())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((result, throwable) -> {
                        if (throwable == null) {
                            holder.contactImageView.setImageRequest(ImageRequest.fromUri(result));
                        }
                    });
        } else {
            holder.contactImageView.setImageRequest(ImageRequest.fromUri(uri));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    public boolean isMultipleSelect() {
        return multipleSelect;
    }

    public void setMultipleSelect(boolean multipleSelect) {
        Timber.d("setMultipleSelect");
        if (this.multipleSelect != multipleSelect) {
            this.multipleSelect = multipleSelect;
            notifyDataSetChanged();
        }
        if (this.multipleSelect) {
            messagesSelectedInChat.clear();
        }
    }

    /**
     * Method for selecting or deselecting a chat message.
     *
     * @param msgId The messages ID.
     */
    public void toggleSelection(long msgId) {
        Timber.d("The message selected is %s", msgId);
        int position = INVALID_POSITION;
        for (AndroidMegaChatMessage message : messages) {
            if (message != null && message.getMessage() != null && message.getMessage().getMsgId() == msgId) {
                position = messages.indexOf(message);
            }
        }

        if (position == INVALID_POSITION) {
            return;
        }

        if (messagesSelectedInChat.get(msgId) != null) {
            Timber.d("Message removed");
            messagesSelectedInChat.remove(msgId);
        } else {
            Timber.d("Message selected");
            messagesSelectedInChat.put(msgId, position);
        }

        notifyItemChanged(position + 1);

        if (messagesSelectedInChat.isEmpty()) {
            ((ChatActivity) context).updateActionModeTitle();
        }
    }

    /**
     * Method for selecting all chat messages.
     */
    public void selectAll() {
        for (AndroidMegaChatMessage message : messages) {
            if (!isItemChecked(message.getMessage().getMsgId())) {
                toggleSelection(message.getMessage().getMsgId());
            }

        }
    }

    /**
     * Method for deselecting all chat messages.
     */
    public void clearSelections() {
        for (AndroidMegaChatMessage message : messages) {
            if (message != null && message.getMessage() != null &&
                    isItemChecked(message.getMessage().getMsgId())) {
                toggleSelection(message.getMessage().getMsgId());
            }
        }
    }

    /**
     * Method to know if a message is selected or not.
     *
     * @param msgId The message ID.
     * @return True, if selected. False, if not selected.
     */
    private boolean isItemChecked(long msgId) {
        return messagesSelectedInChat.get(msgId) != null;
    }

    /**
     * Method for obtaining how many messages are selected.
     *
     * @return The number of selected messages.
     */
    public int getSelectedItemCount() {
        return messagesSelectedInChat.size();
    }

    /**
     * Method for obtaining the selected messages.
     *
     * @return The selected messages.
     */
    public ArrayList<Integer> getSelectedItems() {
        if (messagesSelectedInChat == null || messagesSelectedInChat.isEmpty())
            return null;

        ArrayList<Integer> positionsMessagesSelected = new ArrayList<>();
        for (HashMap.Entry<Long, Integer> message : messagesSelectedInChat.entrySet()) {
            positionsMessagesSelected.add(message.getValue());
        }
        return positionsMessagesSelected;
    }

    /*
     * Get list of all selected chats
     */
    public ArrayList<AndroidMegaChatMessage> getSelectedMessages() {
        ArrayList<AndroidMegaChatMessage> returnedMessages = new ArrayList<>();
        if (messagesSelectedInChat == null || messagesSelectedInChat.isEmpty())
            return returnedMessages;

        HashMap<Long, Integer> selectedMessagesSorted = sortByValue(messagesSelectedInChat);

        for (HashMap.Entry<Long, Integer> messageSelected : selectedMessagesSorted.entrySet()) {
            for (AndroidMegaChatMessage message : messages) {
                if (message.getMessage().getMsgId() == messageSelected.getKey()) {
                    returnedMessages.add(message);
                    break;
                }
            }
        }
        return returnedMessages;
    }

    /**
     * Method to sort the selected messages depending on the value.
     *
     * @param listMessages HashMap of current selected messages.
     * @return HashMap of selected messages in order.
     */
    private static HashMap<Long, Integer> sortByValue(HashMap<Long, Integer> listMessages) {
        List<Map.Entry<Long, Integer>> list = new LinkedList<>(listMessages.entrySet());
        Collections.sort(list, (o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));
        HashMap<Long, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }

        return temp;
    }

    @Override
    public int getFolderCount() {
        return 0;
    }

    @Override
    public int getPlaceholderCount() {
        return placeholderCount;
    }

    @Override
    public int getUnhandledItem() {
        return 0;
    }

    public Object getItem(int position) {
        return messages.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setMessages(ArrayList<AndroidMegaChatMessage> messages) {
        this.messages = messages;
        placeholderCount = 0;
        notifyDataSetChanged();
    }

    public void modifyMessage(ArrayList<AndroidMegaChatMessage> messages, int position) {
        this.messages = messages;
        notifyItemChanged(position);
    }

    public void addMessage(ArrayList<AndroidMegaChatMessage> messages, int position) {
        this.messages = messages;
        notifyItemInserted(position);
        notifyRangeChanged(position);
    }

    public void removeMessage(int position, ArrayList<AndroidMegaChatMessage> messages) {
        this.messages = messages;
        notifyItemRemoved(position);
        notifyRangeChanged(position);
    }

    /**
     * Notifies the adapter if the range changed due to and addition or deletion in adapter.
     *
     * @param position where the item was added or removed.
     */
    private void notifyRangeChanged(int position) {
        if (position != messages.size()) {
            int itemCount = messages.size() - position + 1;
            notifyItemRangeChanged(position, itemCount + 1);
        }
    }

    /**
     * Gets the message at specified position in the Adapter.
     *
     * @param positionInAdapter The position in adapter.
     * @return The message.
     */
    public AndroidMegaChatMessage getMessageAtAdapterPosition(int positionInAdapter) {
        int positionInMessages = positionInAdapter - 1;
        return getMessageAtMessagesPosition(positionInMessages);
    }

    /**
     * Gets the message at specified position in messages array.
     *
     * @param positionInMessages The position in messages array.
     * @return The message.
     */
    public AndroidMegaChatMessage getMessageAtMessagesPosition(int positionInMessages) {
        return messages != null && positionInMessages >= 0 && positionInMessages < messages.size() ? messages.get(positionInMessages) : null;
    }

    public void loadPreviousMessages(ArrayList<AndroidMegaChatMessage> messages, int counter) {
        Timber.d("counter: %s", counter);
        this.messages = messages;
        notifyItemRangeInserted(0, counter);
    }

    private void setErrorStateOnPreview(MegaChatAdapter.ViewHolderMessageChat holder, Bitmap bitmap, int status) {
        Timber.d("setErrorStateOnPreview()");
        //Error
        holder.uploadingProgressBarPort.setVisibility(View.GONE);
        holder.uploadingProgressBarLand.setVisibility(View.GONE);
        holder.forwardOwnFile.setVisibility(View.GONE);
        holder.forwardOwnPortrait.setVisibility(View.GONE);
        holder.forwardOwnLandscape.setVisibility(View.GONE);

        boolean statusRejectedOrSendingManual = status == MegaChatMessage.STATUS_SERVER_REJECTED ||
                status == MegaChatMessage.STATUS_SENDING_MANUAL;

        String name = holder.contentOwnMessageFileName.getText().toString();

        if (bitmap.getWidth() < bitmap.getHeight()) {
            Timber.d("Portrait");
            holder.errorUploadingLandscape.setVisibility(View.GONE);
            holder.transparentCoatingLandscape.setVisibility(View.GONE);
            holder.errorUploadingFile.setVisibility(View.GONE);
            holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

            if (MimeTypeList.typeForName(name).isVideo()) {
                holder.videoIconOwnMessageThumbPort.setVisibility(View.VISIBLE);
            } else {
                holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
            }

            holder.errorUploadingPortrait.setVisibility(statusRejectedOrSendingManual ? View.VISIBLE : View.GONE);
            holder.transparentCoatingPortrait.setVisibility(View.VISIBLE);
            holder.retryAlert.setVisibility(statusRejectedOrSendingManual ? View.VISIBLE : View.GONE);
        } else {
            Timber.d("Landscape");
            holder.errorUploadingPortrait.setVisibility(View.GONE);
            holder.transparentCoatingPortrait.setVisibility(View.GONE);
            holder.errorUploadingFile.setVisibility(View.GONE);

            holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

            if (MimeTypeList.typeForName(name).isVideo()) {
                holder.videoIconOwnMessageThumbLand.setVisibility(View.VISIBLE);
            } else {
                holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
            }

            holder.errorUploadingLandscape.setVisibility(statusRejectedOrSendingManual ? View.VISIBLE : View.GONE);
            holder.transparentCoatingLandscape.setVisibility(View.VISIBLE);
            holder.retryAlert.setVisibility(statusRejectedOrSendingManual ? View.VISIBLE : View.GONE);
        }
    }

    private void setOwnPreview(MegaChatAdapter.ViewHolderMessageChat holder, Bitmap bitmap, MegaNode node, boolean shouldForwardBeVisible, MegaChatMessage message) {
        Timber.d("setOwnPreview()");

        if (holder != null) {
            if (bitmap.getWidth() < bitmap.getHeight()) {
                setBitmapAndUpdateDimensions(holder.contentOwnMessageThumbPort, bitmap);

                if (MimeTypeList.typeForName(node.getName()).isPdf()) {
                    Timber.d("Is pfd preview");
                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.VISIBLE);

                    holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                    holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

                } else if (MimeTypeList.typeForName(node.getName()).isVideo()) {
                    Timber.d("Is video preview");
                    holder.videoIconOwnMessageThumbPort.setVisibility(View.VISIBLE);
                    holder.videoTimecontentOwnMessageThumbPort.setText(timeVideo(node));
                    holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.VISIBLE);

                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);

                } else {
                    if (MimeTypeList.typeForName(node.getName()).isGIF()) {
                        setGIFProperties(node, holder, true, true);
                    }

                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);
                    holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                    holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
                }

                holder.previewFramePort.setVisibility(View.VISIBLE);
                holder.contentOwnMessageThumbPort.setVisibility(View.VISIBLE);

                holder.forwardOwnFile.setVisibility(View.GONE);
                holder.forwardOwnLandscape.setVisibility(View.GONE);
                if (shouldForwardBeVisible) {
                    holder.forwardOwnPortrait.setVisibility(View.VISIBLE);
                    getNodeUseCase.get(message.getMegaNodeList().get(0).getHandle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result, throwable) -> {
                                if (throwable != null) {
                                    holder.forwardOwnPortrait.setVisibility(View.GONE);
                                }
                            });

                } else {
                    holder.forwardOwnPortrait.setVisibility(View.GONE);
                }

                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                holder.previewFrameLand.setVisibility(View.GONE);
                holder.contentOwnMessageThumbLand.setVisibility(View.GONE);
                holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
            } else {
                setBitmapAndUpdateDimensions(holder.contentOwnMessageThumbLand, bitmap);

                if (MimeTypeList.typeForName(node.getName()).isPdf()) {
                    Timber.d("Is pfd preview");
                    holder.iconOwnTypeDocLandPreview.setVisibility(View.VISIBLE);

                    holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                    holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

                } else if (MimeTypeList.typeForName(node.getName()).isVideo()) {
                    Timber.d("Is video preview");
                    holder.videoIconOwnMessageThumbLand.setVisibility(View.VISIBLE);
                    holder.videoTimecontentOwnMessageThumbLand.setText(timeVideo(node));
                    holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.VISIBLE);

                    holder.iconOwnTypeDocLandPreview.setVisibility(View.GONE);

                } else {
                    if (MimeTypeList.typeForName(node.getName()).isGIF()) {
                        setGIFProperties(node, holder, false, true);
                    }

                    holder.iconOwnTypeDocLandPreview.setVisibility(View.GONE);
                    holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                    holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
                }

                holder.previewFrameLand.setVisibility(View.VISIBLE);
                holder.contentOwnMessageThumbLand.setVisibility(View.VISIBLE);
                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                holder.previewFramePort.setVisibility(View.GONE);
                holder.contentOwnMessageThumbPort.setVisibility(View.GONE);

                holder.forwardOwnFile.setVisibility(View.GONE);
                holder.forwardOwnPortrait.setVisibility(View.GONE);
                if (shouldForwardBeVisible) {
                    holder.forwardOwnLandscape.setVisibility(View.VISIBLE);
                    getNodeUseCase.get(message.getMegaNodeList().get(0).getHandle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result, throwable) -> {
                                if (throwable != null) {
                                    holder.forwardOwnLandscape.setVisibility(View.GONE);
                                }
                            });
                } else {
                    holder.forwardOwnLandscape.setVisibility(View.GONE);
                }

                holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
            }
        }
    }

    private void setContactPreview(MegaChatAdapter.ViewHolderMessageChat holder, Bitmap bitmap, MegaNode node) {
        Timber.d("setContactPreview()");
        if (bitmap.getWidth() < bitmap.getHeight()) {
            setBitmapAndUpdateDimensions(holder.contentContactMessageThumbPort, bitmap);

            if (MimeTypeList.typeForName(node.getName()).isPdf()) {
                Timber.d("Contact message - Is pfd preview");
                holder.iconContactTypeDocPortraitPreview.setVisibility(View.VISIBLE);

                holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
                holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);

            } else if (MimeTypeList.typeForName(node.getName()).isVideo()) {
                Timber.d("Contact message - Is video preview");
                holder.videoIconContactMessageThumbPort.setVisibility(View.VISIBLE);
                holder.videoTimecontentContactMessageThumbPort.setText(timeVideo(node));
                holder.videoTimecontentContactMessageThumbPort.setVisibility(View.VISIBLE);

                holder.iconContactTypeDocPortraitPreview.setVisibility(View.GONE);

            } else {
                if (MimeTypeList.typeForName(node.getName()).isGIF()) {
                    setGIFProperties(node, holder, true, false);
                }

                holder.iconContactTypeDocPortraitPreview.setVisibility(View.GONE);
                holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
                holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
            }

            holder.contentContactMessageThumbPort.setVisibility(View.VISIBLE);

            if (cC.isInAnonymousMode() || isMultipleSelect()) {
                holder.forwardContactPreviewPortrait.setVisibility(View.GONE);
            } else {
                holder.forwardContactPreviewPortrait.setVisibility(View.VISIBLE);
            }
            if (isMultipleSelect()) {
                holder.forwardContactPreviewPortrait.setOnClickListener(null);
            } else {
                holder.forwardContactPreviewPortrait.setOnClickListener(this);
            }
            holder.forwardContactFile.setVisibility(View.GONE);
            holder.forwardContactPreviewLandscape.setVisibility(View.GONE);

            holder.contentContactMessageThumbLand.setVisibility(View.GONE);
            holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
            holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);
            holder.contentContactMessageFile.setVisibility(View.GONE);
            holder.contentContactMessageFileThumb.setVisibility(View.GONE);
            holder.contentContactMessageFileName.setVisibility(View.GONE);
            holder.contentContactMessageFileSize.setVisibility(View.GONE);
        } else {
            setBitmapAndUpdateDimensions(holder.contentContactMessageThumbLand, bitmap);

            if (MimeTypeList.typeForName(node.getName()).isPdf()) {
                Timber.d("Contact message - Is pfd preview");
                holder.iconContactTypeDocLandPreview.setVisibility(View.VISIBLE);
                holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
                holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

            } else if (MimeTypeList.typeForName(node.getName()).isVideo()) {
                Timber.d("Contact message - Is video preview");
                holder.videoIconContactMessageThumbLand.setVisibility(View.VISIBLE);
                holder.videoTimecontentContactMessageThumbLand.setText(timeVideo(node));
                holder.videoTimecontentContactMessageThumbLand.setVisibility(View.VISIBLE);
                holder.iconContactTypeDocLandPreview.setVisibility(View.GONE);

            } else {
                if (MimeTypeList.typeForName(node.getName()).isGIF()) {
                    setGIFProperties(node, holder, false, false);
                }

                holder.iconContactTypeDocLandPreview.setVisibility(View.GONE);
                holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
                holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);
            }

            holder.contentContactMessageThumbLand.setVisibility(View.VISIBLE);

            if (cC.isInAnonymousMode() || isMultipleSelect()) {
                holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
            } else {
                holder.forwardContactPreviewLandscape.setVisibility(View.VISIBLE);
            }
            if (isMultipleSelect()) {
                holder.forwardContactPreviewLandscape.setOnClickListener(null);
            } else {
                holder.forwardContactPreviewLandscape.setOnClickListener(this);
            }
            holder.forwardContactPreviewPortrait.setVisibility(View.GONE);
            holder.forwardContactFile.setVisibility(View.GONE);

            holder.contentContactMessageThumbPort.setVisibility(View.GONE);
            holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
            holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
            holder.contentContactMessageFile.setVisibility(View.GONE);
            holder.contentContactMessageFileThumb.setVisibility(View.GONE);
            holder.contentContactMessageFileName.setVisibility(View.GONE);
            holder.contentContactMessageFileSize.setVisibility(View.GONE);
        }
    }

    private void setPreview(long handle, MegaChatAdapter.ViewHolderMessageChat holder, MegaNode node, long msgId) {
        if (holder == null) {
            Timber.w("Holder is null. Handle: %s", handle);
            return;
        }

        int adapterPosition = holder.getAdapterPosition();
        AndroidMegaChatMessage megaMessage = getMessageAtAdapterPosition(adapterPosition);
        if (megaMessage == null || megaMessage.getMessage() == null) {
            Timber.w("Message is null. Handle: %s", handle);
            return;
        }

        if (megaMessage.getMessage().getMsgId() != msgId) {
            adapterPosition = ((ChatActivity) context).getPositionOfAttachmentMessageIfVisible(handle);
            if (adapterPosition == INVALID_POSITION) {
                Timber.w("The message is not visible or does not exist. Handle: %s", handle);
                return;
            }
        }

        File previewDir = getPreviewFolder(context);
        String base64 = MegaApiJava.handleToBase64(handle);
        File preview = new File(previewDir, base64 + JPG_EXTENSION);
        if (!preview.exists() || preview.length() <= 0) {
            Timber.w("Preview not exists. Handle: %s", handle);
            return;
        }

        Bitmap bitmap = getBitmapForCache(preview, context);
        if (bitmap == null) {
            Timber.w("Bitmap is null. Handle: %s", handle);
            return;
        }

        previewCache.put(handle, bitmap);

        if (holder.userHandle == megaChatApi.getMyUserHandle()) {
            String name = holder.contentOwnMessageFileName.getText().toString();

            if (bitmap.getWidth() < bitmap.getHeight()) {
                setBitmapAndUpdateDimensions(holder.contentOwnMessageThumbPort, bitmap);

                if (MimeTypeList.typeForName(name).isPdf()) {
                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.VISIBLE);
                    holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                    holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
                } else if (MimeTypeList.typeForName(name).isVideo()) {
                    holder.videoIconOwnMessageThumbPort.setVisibility(View.VISIBLE);

                    if (node != null) {
                        holder.videoTimecontentOwnMessageThumbPort.setText(timeVideo(node));
                        holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.VISIBLE);
                    } else {
                        holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
                    }

                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);

                } else {
                    if (MimeTypeList.typeForName(node.getName()).isGIF()) {
                        setGIFProperties(node, holder, true, true);
                    }

                    holder.iconOwnTypeDocPortraitPreview.setVisibility(View.GONE);
                    holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                    holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
                }

                holder.previewFramePort.setVisibility(View.VISIBLE);
                holder.contentOwnMessageThumbPort.setVisibility(View.VISIBLE);

                holder.forwardOwnLandscape.setVisibility(View.GONE);
                holder.forwardOwnFile.setVisibility(View.GONE);
                if (checkForwardVisibilityInOwnMsg(removedMessages, megaMessage.getMessage(), isMultipleSelect(), cC)) {
                    holder.forwardOwnPortrait.setVisibility(View.VISIBLE);
                    holder.forwardOwnPortrait.setOnClickListener(this);
                    getNodeUseCase.get(megaMessage.getMessage().getMegaNodeList().get(0).getHandle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result, throwable) -> {
                                if (throwable != null) {
                                    holder.forwardOwnPortrait.setVisibility(View.GONE);
                                }
                            });
                } else {
                    holder.forwardOwnPortrait.setVisibility(View.GONE);
                }

                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                holder.previewFrameLand.setVisibility(View.GONE);
                holder.contentOwnMessageThumbLand.setVisibility(View.GONE);
                holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
            } else {
                setBitmapAndUpdateDimensions(holder.contentOwnMessageThumbLand, bitmap);

                if (MimeTypeList.typeForName(name).isPdf()) {
                    holder.iconOwnTypeDocLandPreview.setVisibility(View.VISIBLE);
                    holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
                    holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                } else if (MimeTypeList.typeForName(name).isVideo()) {
                    holder.videoIconOwnMessageThumbLand.setVisibility(View.VISIBLE);

                    if (node != null) {
                        holder.videoTimecontentOwnMessageThumbLand.setText(timeVideo(node));
                        holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.VISIBLE);
                    } else {
                        holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
                    }

                    holder.iconOwnTypeDocLandPreview.setVisibility(View.GONE);
                } else {
                    if (MimeTypeList.typeForName(node.getName()).isGIF()) {
                        setGIFProperties(node, holder, false, true);
                    }

                    holder.iconOwnTypeDocLandPreview.setVisibility(View.GONE);
                    holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
                    holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                }

                holder.previewFrameLand.setVisibility(View.VISIBLE);
                holder.contentOwnMessageThumbLand.setVisibility(View.VISIBLE);
                holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                holder.previewFramePort.setVisibility(View.GONE);
                holder.contentOwnMessageThumbPort.setVisibility(View.GONE);

                holder.forwardOwnPortrait.setVisibility(View.GONE);
                holder.forwardOwnFile.setVisibility(View.GONE);
                if (checkForwardVisibilityInOwnMsg(removedMessages, megaMessage.getMessage(), isMultipleSelect(), cC)) {
                    holder.forwardOwnLandscape.setVisibility(View.VISIBLE);
                    holder.forwardOwnLandscape.setOnClickListener(this);
                    getNodeUseCase.get(megaMessage.getMessage().getMegaNodeList().get(0).getHandle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result, throwable) -> {
                                if (throwable != null) {
                                    holder.forwardOwnLandscape.setVisibility(View.GONE);
                                }
                            });
                } else {
                    holder.forwardOwnLandscape.setVisibility(View.GONE);
                }

                holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
            }
        } else {
            String name = holder.contentContactMessageFileName.getText().toString();

            if (bitmap.getWidth() < bitmap.getHeight()) {
                setBitmapAndUpdateDimensions(holder.contentContactMessageThumbPort, bitmap);

                if (MimeTypeList.typeForName(name).isPdf()) {
                    holder.iconContactTypeDocPortraitPreview.setVisibility(View.VISIBLE);
                    holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
                    holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
                } else if (MimeTypeList.typeForName(name).isVideo()) {
                    holder.videoIconContactMessageThumbPort.setVisibility(View.VISIBLE);

                    if (node != null) {
                        holder.videoTimecontentContactMessageThumbPort.setText(timeVideo(node));
                        holder.videoTimecontentContactMessageThumbPort.setVisibility(View.VISIBLE);
                    } else {
                        holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
                    }

                    holder.iconContactTypeDocPortraitPreview.setVisibility(View.GONE);
                } else {
                    if (MimeTypeList.typeForName(node.getName()).isGIF()) {
                        setGIFProperties(node, holder, true, false);
                    }

                    holder.iconContactTypeDocPortraitPreview.setVisibility(View.GONE);
                    holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
                    holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
                }

                holder.contentContactMessageThumbPort.setVisibility(View.VISIBLE);

                if (cC.isInAnonymousMode() || isMultipleSelect()) {
                    holder.forwardContactPreviewPortrait.setVisibility(View.GONE);
                } else {
                    holder.forwardContactPreviewPortrait.setVisibility(View.VISIBLE);
                }

                if (isMultipleSelect()) {
                    holder.forwardContactPreviewPortrait.setOnClickListener(null);
                } else {
                    holder.forwardContactPreviewPortrait.setOnClickListener(this);
                }

                holder.contentContactMessageThumbLand.setVisibility(View.GONE);
                holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
                holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
                holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);

                holder.contentContactMessageFile.setVisibility(View.GONE);
                holder.forwardContactFile.setVisibility(View.GONE);

                holder.contentContactMessageFileThumb.setVisibility(View.GONE);
                holder.contentContactMessageFileName.setVisibility(View.GONE);
                holder.contentContactMessageFileSize.setVisibility(View.GONE);
            } else {
                setBitmapAndUpdateDimensions(holder.contentContactMessageThumbLand, bitmap);

                if (MimeTypeList.typeForName(name).isPdf()) {
                    holder.iconContactTypeDocLandPreview.setVisibility(View.VISIBLE);
                    holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
                    holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);
                } else if (MimeTypeList.typeForName(name).isVideo()) {
                    holder.videoIconContactMessageThumbLand.setVisibility(View.VISIBLE);

                    if (node != null) {
                        holder.videoTimecontentContactMessageThumbLand.setText(timeVideo(node));
                        holder.videoTimecontentContactMessageThumbLand.setVisibility(View.VISIBLE);
                    } else {
                        holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);
                    }

                    holder.iconContactTypeDocLandPreview.setVisibility(View.GONE);
                } else {
                    if (MimeTypeList.typeForName(node.getName()).isGIF()) {
                        setGIFProperties(node, holder, false, false);
                    }

                    holder.iconContactTypeDocLandPreview.setVisibility(View.GONE);
                    holder.videoIconContactMessageThumbLand.setVisibility(View.GONE);
                    holder.videoTimecontentContactMessageThumbLand.setVisibility(View.GONE);
                }

                holder.contentContactMessageThumbLand.setVisibility(View.VISIBLE);

                if (cC.isInAnonymousMode() || isMultipleSelect()) {
                    holder.forwardContactPreviewLandscape.setVisibility(View.GONE);
                } else {
                    holder.forwardContactPreviewLandscape.setVisibility(View.VISIBLE);
                }

                if (isMultipleSelect()) {
                    holder.forwardContactPreviewLandscape.setOnClickListener(null);
                } else {
                    holder.forwardContactPreviewLandscape.setOnClickListener(this);
                }

                holder.contentContactMessageThumbPort.setVisibility(View.GONE);
                holder.forwardContactPreviewPortrait.setVisibility(View.GONE);

                holder.videoIconContactMessageThumbPort.setVisibility(View.GONE);
                holder.videoTimecontentContactMessageThumbPort.setVisibility(View.GONE);
                holder.contentContactMessageFile.setVisibility(View.GONE);
                holder.forwardContactFile.setVisibility(View.GONE);

                holder.contentContactMessageFileThumb.setVisibility(View.GONE);
                holder.contentContactMessageFileName.setVisibility(View.GONE);
                holder.contentContactMessageFileSize.setVisibility(View.GONE);
            }
        }

        notifyItemChanged(adapterPosition);
    }

    private void setUploadingPreview(MegaChatAdapter.ViewHolderMessageChat holder, Bitmap bitmap) {
        Timber.d("holder.filePathUploading: %s", holder.filePathUploading);

        if (holder != null) {

            holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
            holder.urlOwnMessageLayout.setVisibility(View.GONE);

            holder.forwardOwnRichLinks.setVisibility(View.GONE);
            holder.forwardOwnPortrait.setVisibility(View.GONE);
            holder.forwardOwnLandscape.setVisibility(View.GONE);
            holder.forwardOwnFile.setVisibility(View.GONE);
            holder.forwardOwnContact.setVisibility(View.GONE);

            holder.ownManagementMessageLayout.setVisibility(View.GONE);

            holder.titleOwnMessage.setGravity(Gravity.RIGHT);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_LAND, outMetrics), 0);
            } else {
                holder.titleOwnMessage.setPadding(0, 0, scaleWidthPx(PADDING_RIGHT_HOUR_OF_OWN_MESSAGE_PORT, outMetrics), 0);
            }

            if (bitmap != null) {
                Timber.d("Bitmap not null - Update uploading my preview");

                int currentPosition = holder.getLayoutPosition();
                Timber.d("currentPosition holder: %s", currentPosition);

                if (currentPosition == -1) {
                    Timber.w("The position cannot be recovered - had changed");
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        AndroidMegaChatMessage message = messages.get(i);
                        if (message.isUploading()) {
                            String path = message.getPendingMessage().getFilePath();
                            if (path.equals(holder.filePathUploading)) {
                                currentPosition = i + 1;
                                Timber.d("Found current position: %s", currentPosition);
                                break;
                            }
                        }
                    }
                }
                Timber.d("Messages size: %s", messages.size());
                if (currentPosition > messages.size()) {
                    Timber.w("Position not valid");
                    return;
                }

                AndroidMegaChatMessage message = messages.get(currentPosition - 1);
                if (message.getPendingMessage() != null) {
                    Timber.d("State of the message: %s", message.getPendingMessage().getState());
                    Timber.d("Attachment: %s", message.getPendingMessage().getFilePath());

                    if (bitmap.getWidth() < bitmap.getHeight()) {
                        setBitmapAndUpdateDimensions(holder.contentOwnMessageThumbPort, bitmap);
                        holder.previewFramePort.setVisibility(View.VISIBLE);

                        holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                        holder.previewFrameLand.setVisibility(View.GONE);
                        holder.contentOwnMessageThumbLand.setVisibility(View.GONE);

                        holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                        holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
                        holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                        holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

                    } else {
                        setBitmapAndUpdateDimensions(holder.contentOwnMessageThumbLand, bitmap);
                        holder.previewFrameLand.setVisibility(View.VISIBLE);

                        holder.contentOwnMessageFileLayout.setVisibility(View.GONE);
                        holder.previewFramePort.setVisibility(View.GONE);
                        holder.contentOwnMessageThumbPort.setVisibility(View.GONE);
                        holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                        holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);
                        holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                        holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);
                    }

                    if (message.getPendingMessage().getState() == PendingMessageState.ERROR_UPLOADING.getValue() || message.getPendingMessage().getState() == PendingMessageState.ERROR_ATTACHING.getValue()
                            || areTransfersPaused) {
                        Timber.w("Message is on ERROR state");
                        holder.urlOwnMessageLayout.setVisibility(View.GONE);
                        String name = holder.contentOwnMessageFileName.getText().toString();

                        //Error
                        holder.uploadingProgressBarPort.setVisibility(View.GONE);
                        holder.uploadingProgressBarLand.setVisibility(View.GONE);
                        holder.errorUploadingFile.setVisibility(View.GONE);

                        holder.retryAlert.setVisibility(View.VISIBLE);

                        if (bitmap.getWidth() < bitmap.getHeight()) {
                            Timber.d("Portrait");
                            holder.errorUploadingLandscape.setVisibility(View.GONE);
                            holder.transparentCoatingLandscape.setVisibility(View.GONE);
                            holder.videoTimecontentOwnMessageThumbPort.setVisibility(View.GONE);

                            if (MimeTypeList.typeForName(name).isVideo()) {
                                holder.videoIconOwnMessageThumbPort.setVisibility(View.VISIBLE);
                            } else {
                                holder.videoIconOwnMessageThumbPort.setVisibility(View.GONE);
                            }

                            holder.errorUploadingPortrait.setVisibility(View.VISIBLE);
                            holder.transparentCoatingPortrait.setVisibility(View.VISIBLE);

                        } else {
                            Timber.d("Landscape");
                            holder.transparentCoatingPortrait.setVisibility(View.GONE);
                            holder.errorUploadingPortrait.setVisibility(View.GONE);
                            holder.videoTimecontentOwnMessageThumbLand.setVisibility(View.GONE);

                            if (MimeTypeList.typeForName(name).isVideo()) {
                                holder.videoIconOwnMessageThumbLand.setVisibility(View.VISIBLE);
                            } else {
                                holder.videoIconOwnMessageThumbLand.setVisibility(View.GONE);
                            }

                            holder.errorUploadingLandscape.setVisibility(View.VISIBLE);
                            holder.transparentCoatingLandscape.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Timber.d("Message is in progress state");
                        holder.urlOwnMessageLayout.setVisibility(View.GONE);

                        //In progress
                        holder.retryAlert.setVisibility(View.GONE);
                        holder.errorUploadingFile.setVisibility(View.GONE);
                        holder.errorUploadingPortrait.setVisibility(View.GONE);
                        holder.errorUploadingLandscape.setVisibility(View.GONE);

                        if (bitmap.getWidth() < bitmap.getHeight()) {
                            Timber.d("Portrait");
                            holder.transparentCoatingLandscape.setVisibility(View.GONE);
                            holder.transparentCoatingPortrait.setVisibility(View.VISIBLE);
                            holder.uploadingProgressBarPort.setVisibility(View.VISIBLE);
                            holder.uploadingProgressBarLand.setVisibility(View.GONE);
                        } else {
                            Timber.d("Landscape");
                            holder.transparentCoatingPortrait.setVisibility(View.GONE);
                            holder.transparentCoatingLandscape.setVisibility(View.VISIBLE);
                            holder.uploadingProgressBarLand.setVisibility(View.VISIBLE);
                            holder.uploadingProgressBarPort.setVisibility(View.GONE);
                        }
                    }
                } else {

                    Timber.w("The pending message is NULL-- cannot set preview");
                }
            } else {
                Timber.w("Bitmap is NULL");
            }
        } else {
            Timber.w("Holder is NULL");
        }
    }

    private class PreviewDownloadListener implements MegaRequestListenerInterface {
        Context context;
        MegaChatAdapter.ViewHolderMessageChat holder;
        MegaChatAdapter adapter;
        MegaNode node;

        PreviewDownloadListener(Context context, MegaChatAdapter.ViewHolderMessageChat holder, MegaChatAdapter adapter, MegaNode node) {
            this.context = context;
            this.holder = holder;
            this.adapter = adapter;
            this.node = node;
        }

        @Override
        public void onRequestStart(MegaApiJava api, MegaRequest request) {

        }

        @Override
        public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {

            Timber.d("onRequestFinish: %d__%s", request.getType(), request.getRequestString());
            Timber.d("onRequestFinish: Node: %s", request.getNodeHandle());

            if (request.getType() == MegaRequest.TYPE_GET_ATTR_FILE) {
                if (e.getErrorCode() == MegaError.API_OK) {

                    long handle = request.getNodeHandle();
                    long msgId = INVALID_HANDLE;
                    if (pendingPreviews.containsKey(handle)) {
                        msgId = pendingPreviews.get(handle);
                        pendingPreviews.remove(handle);
                    }

                    setPreview(handle, holder, node, msgId);

                } else {
                    Timber.e("ERROR: %s___%s", e.getErrorCode(), e.getErrorString());
                }
            }
        }

        @Override
        public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {

        }

        @Override
        public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

        }
    }

    public String timeVideo(MegaNode n) {
        Timber.d("timeVideo");
        return getVideoDuration(n.getDuration());
    }

    private void checkItem(View v, ViewHolderMessageChat holder, int[] screenPosition, int[] dimens) {

        ImageView imageView = null;

        int position = holder.getAdapterPosition();
        if (position <= messages.size()) {
            AndroidMegaChatMessage message = messages.get(position - 1);
            if (message.getMessage() != null && message.getMessage().getMegaNodeList() != null
                    && message.getMessage().getMegaNodeList().get(0) != null) {
                if (message.getMessage().getUserHandle() == myUserHandle) {
                    if (holder.contentOwnMessageThumbPort.getVisibility() == View.VISIBLE) {
                        imageView = (RoundedImageView) v.findViewById(R.id.content_own_message_thumb_portrait);
                    } else if (holder.contentOwnMessageThumbLand.getVisibility() == View.VISIBLE) {
                        imageView = (RoundedImageView) v.findViewById(R.id.content_own_message_thumb_landscape);
                    } else if (holder.contentOwnMessageFileThumb.getVisibility() == View.VISIBLE) {
                        imageView = v.findViewById(R.id.content_own_message_file_thumb);
                    } else if (holder.gifViewOwnMessageThumbPort.getVisibility() == View.VISIBLE) {
                        imageView = (SimpleDraweeView) v.findViewById(R.id.content_own_message_thumb_portrait_gif_view);
                    } else if (holder.gifViewOwnMessageThumbLand.getVisibility() == View.VISIBLE) {
                        imageView = (SimpleDraweeView) v.findViewById(R.id.content_own_message_thumb_landscape_gif_view);
                    }
                } else if (holder.contentContactMessageThumbPort.getVisibility() == View.VISIBLE) {
                    imageView = (RoundedImageView) v.findViewById(R.id.content_contact_message_thumb_portrait);
                } else if (holder.contentContactMessageThumbLand.getVisibility() == View.VISIBLE) {
                    imageView = (RoundedImageView) v.findViewById(R.id.content_contact_message_thumb_landscape);
                } else if (holder.contentContactMessageFileThumb.getVisibility() == View.VISIBLE) {
                    imageView = v.findViewById(R.id.content_contact_message_file_thumb);
                } else if (holder.gifViewContactMessageThumbPort.getVisibility() == View.VISIBLE) {
                    imageView = (SimpleDraweeView) v.findViewById(R.id.content_contact_message_thumb_portrait_gif_view);
                } else if (holder.gifViewContactMessageThumbLand.getVisibility() == View.VISIBLE) {
                    imageView = (SimpleDraweeView) v.findViewById(R.id.content_contact_message_thumb_landscape_gif_view);
                }
            }
        }

        ((ChatActivity) context).holder_imageDrag = holder;
        ((ChatActivity) context).position_imageDrag = position;

        if (imageView != null) {
            imageView.getLocationOnScreen(screenPosition);
            dimens[0] = screenPosition[0];
            dimens[1] = screenPosition[1];
            dimens[2] = imageView.getWidth();
            dimens[3] = imageView.getHeight();
        }
    }

    public void setNodeAttachmentVisibility(boolean visible, ViewHolderMessageChat holder, int position) {
        Timber.d("position: %s", position);
        if (holder != null) {
            holder.contentVisible = visible;
            notifyItemChanged(position);
        }
    }

    @Override
    public void onClick(View v) {
        Timber.d("onClick()");

        ViewHolderMessageChat holder = (ViewHolderMessageChat) v.getTag();
        if (holder == null) {
            return;
        }

        int currentPositionInAdapter = holder.getAbsoluteAdapterPosition();
        if (currentPositionInAdapter < 0) {
            Timber.w("Current position error - not valid value");
            return;
        }

        int id = v.getId();
        if (id == R.id.content_own_message_voice_clip_play_pause || id == R.id.content_contact_message_voice_clip_play_pause) {
            if (!(((ChatActivity) context).isRecordingNow())) {
                playOrPauseVoiceClip(currentPositionInAdapter, holder);
            }
        } else if (id == R.id.content_own_message_voice_clip_not_available || id == R.id.content_contact_message_voice_clip_not_available) {
            ((ChatActivity) context).showSnackbar(SNACKBAR_TYPE, context.getString(R.string.error_message_voice_clip), -1);
        } else if (id == R.id.forward_own_rich_links || id == R.id.forward_own_contact || id == R.id.forward_own_file || id == R.id.forward_own_preview_portrait || id == R.id.forward_own_preview_landscape || id == R.id.forward_contact_rich_links || id == R.id.forward_contact_contact || id == R.id.forward_contact_file || id == R.id.forward_contact_preview_portrait || id == R.id.forward_contact_preview_landscape || id == R.id.forward_own_location || id == R.id.forward_contact_location || id == R.id.own_contact_link_forward || id == R.id.others_contact_link_forward) {
            ArrayList<AndroidMegaChatMessage> messageArray = new ArrayList<>();
            int currentPositionInMessages = currentPositionInAdapter - 1;
            messageArray.add(messages.get(currentPositionInMessages));
            ((ChatActivity) context).forwardMessages(messageArray);
        } else if (id == R.id.content_own_message_text || id == R.id.content_contact_message_text || id == R.id.url_own_message_text || id == R.id.url_contact_message_text) {
            if (isIsClickAlreadyIntercepted()) {
                resetIsClickAlreadyIntercepted();
                return;
            }


            if (!isMultipleSelect() && checkIfIsGiphyOrGifMessage(currentPositionInAdapter - 1, holder)) {
                return;
            }

            int[] screenPosition = new int[2];
            int[] dimens = new int[4];
            checkItem(v, holder, screenPosition, dimens);

            ((ChatActivity) context).itemClick(currentPositionInAdapter, dimens);
        } else if (id == R.id.message_chat_item_layout || id == R.id.content_own_message_thumb_portrait_gif_view || id == R.id.content_own_message_thumb_landscape_gif_view || id == R.id.content_contact_message_thumb_portrait_gif_view || id == R.id.content_contact_message_thumb_landscape_gif_view) {
            if (!isMultipleSelect() && checkIfIsGiphyOrGifMessage(currentPositionInAdapter - 1, holder)) {
                return;
            }

            int[] screenPosition = new int[2];
            int[] dimens = new int[4];
            checkItem(v, holder, screenPosition, dimens);

            ((ChatActivity) context).itemClick(currentPositionInAdapter, dimens);
        } else if (id == R.id.url_always_allow_button) {
            ((ChatActivity) context).showRichLinkWarning = RICH_WARNING_FALSE;
            megaApi.enableRichPreviews(true);
            this.notifyItemChanged(currentPositionInAdapter);
        } else if (id == R.id.url_no_disable_button || id == R.id.url_not_now_button) {
            ((ChatActivity) context).showRichLinkWarning = RICH_WARNING_FALSE;
            int counter = MegaApplication.getCounterNotNowRichLinkWarning();
            if (counter < 1) {
                counter = 1;
            } else {
                counter++;
            }
            megaApi.setRichLinkWarningCounterValue(counter);
            this.notifyItemChanged(currentPositionInAdapter);
        } else if (id == R.id.url_never_button) {
            ((ChatActivity) context).showRichLinkWarning = RICH_WARNING_CONFIRMATION;
            this.notifyItemChanged(currentPositionInAdapter);
        } else if (id == R.id.url_yes_disable_button) {
            ((ChatActivity) context).showRichLinkWarning = RICH_WARNING_FALSE;
            megaApi.enableRichPreviews(false);
            this.notifyItemChanged(currentPositionInAdapter);
        } else if (id == R.id.own_contact_link_container || id == R.id.others_contact_link_container) {
            int[] dimens;
            int[] screenPosition;
            if (isMultipleSelect()) {
                screenPosition = new int[2];
                dimens = new int[4];
                checkItem(v, holder, screenPosition, dimens);
                ((ChatActivity) context).itemClick(currentPositionInAdapter, dimens);
            } else {
                int positionInMessages = currentPositionInAdapter - 1;
                ContactLink result = messages.get(positionInMessages).getContactLinkResult();
                ((ChatActivity) context).openContactLinkMessage(result);
            }
        }
    }

    /**
     * Checks if the message clicked is a Giphy or GIF.
     * If it is a Giphy or GIF and the animation is not playing, it plays it.
     * If it is a Giphy message and  the animation is already playing, it opens the full screen view.     *
     *
     * @param positionInMessages Position of the message in the array of messages.
     * @param holder             Holder representing the message clicked.
     * @return True if some of the cases described below match, false otherwise.
     */
    private boolean checkIfIsGiphyOrGifMessage(int positionInMessages, ViewHolderMessageChat holder) {
        if (messages == null || messages.isEmpty() || messages.size() <= positionInMessages)
            return false;

        AndroidMegaChatMessage message = messages.get(positionInMessages);

        if (message != null && message.getMessage() != null) {
            MegaChatMessage megaChatMessage = message.getMessage();
            boolean isOwnMessage = megaChatMessage.getUserHandle() == myUserHandle;

            if (megaChatMessage.getContainsMeta() != null && megaChatMessage.getContainsMeta().getGiphy() != null) {
                //Giphy message
                MegaChatGiphy giphy = message.getMessage().getContainsMeta().getGiphy();

                if (holder.isPlayingAnimation) {
                    //Already playing animation
                    GifData gifData = new GifData(giphy.getMp4Src(), giphy.getWebpSrc(), giphy.getMp4Size(), giphy.getWebpSize(),
                            giphy.getWidth(), giphy.getHeight(), giphy.getTitle());

                    context.startActivity(new Intent(context, GiphyViewerActivity.class)
                            .putExtra(GIF_DATA, gifData)
                            .setAction(ACTION_PREVIEW_GIPHY));
                } else {
                    //Not playing animation, play
                    int width = giphy.getWidth();
                    int height = giphy.getHeight();
                    boolean isPortrait = width < height;
                    updateGifViewBackgroundAndDimensions(holder, ContextCompat.getColor(context, R.color.grey_050_grey_800), isPortrait, isOwnMessage, width, height);
                    setGIFAndGiphyProperties(true, Uri.parse(giphy.getWebpSrc()), holder, giphy.getWidth() < giphy.getHeight(), isOwnMessage);
                }

                return true;
            } else if (megaChatMessage.getMegaNodeList() != null && megaChatMessage.getMegaNodeList().size() == 1
                    && megaChatMessage.getMegaNodeList().get(0) != null) {
                MegaNode node = megaChatMessage.getMegaNodeList().get(0);

                if (MimeTypeList.typeForName(node.getName()).isGIF() && !holder.isPlayingAnimation) {
                    //GIF message not playing animation, play
                    Bitmap preview = getPreviewFromCache(node);
                    if (preview != null) {
                        int width = preview.getWidth();
                        int height = preview.getHeight();
                        boolean isPortrait = width < height;
                        updateGifViewBackgroundAndDimensions(holder, Color.TRANSPARENT, isPortrait, isOwnMessage, width, height);
                        setGIFAndGiphyProperties(true, getUri(node), holder, isPortrait, isOwnMessage);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    /*
     * Set appropriate values ​​when a playback starts
     */
    private void prepareMediaPlayer(final long msgId) {
        if (handlerVoiceNotes == null) {
            handlerVoiceNotes = new Handler();
        }
        if (messagesPlaying == null || messagesPlaying.isEmpty()) return;

        for (MessageVoiceClip m : messagesPlaying) {
            if ((m.getIdMessage() == msgId) && (m.getMediaPlayer().isPlaying())) {
                Timber.d("prepareMediaPlayer");

                runnableVC = new Runnable() {
                    @Override
                    public void run() {
                        statePlaying(msgId);
                        handlerVoiceNotes.postDelayed(this, 50);
                    }
                };
                statePlaying(msgId);
                handlerVoiceNotes.postDelayed(runnableVC, 50);
            }
        }
    }

    /*
     * Update the view while a voice clip is playing
     */
    private void statePlaying(long msgId) {
        if (messages == null || messages.isEmpty() || messagesPlaying == null || messagesPlaying.isEmpty())
            return;

        for (MessageVoiceClip m : messagesPlaying) {
            if ((m.getIdMessage() == msgId) && (m.getMediaPlayer().isPlaying())) {
                m.setProgress(m.getMediaPlayer().getCurrentPosition());
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.get(i).getMessage().getMsgId() == m.getIdMessage()) {
                        int positionInAdapter = i + 1;
                        ViewHolderMessageChat holder = (ViewHolderMessageChat) listFragment.findViewHolderForAdapterPosition(positionInAdapter);
                        if (holder != null) {
                            if (m.getUserHandle() == megaChatApi.getMyUserHandle()) {
                                holder.contentOwnMessageVoiceClipPlay.setVisibility(View.VISIBLE);
                                holder.contentOwnMessageVoiceClipPlay.setImageResource(R.drawable.ic_pause_voice_clip);
                                holder.contentOwnMessageVoiceClipSeekBar.setProgress(m.getProgress());
                                holder.contentOwnMessageVoiceClipDuration.setText(milliSecondsToTimer(m.getProgress()));
                            } else {
                                holder.contentContactMessageVoiceClipPlay.setVisibility(View.VISIBLE);
                                holder.contentContactMessageVoiceClipPlay.setImageResource(R.drawable.ic_pause_voice_clip);
                                holder.contentContactMessageVoiceClipSeekBar.setProgress(m.getProgress());
                                holder.contentContactMessageVoiceClipDuration.setText(milliSecondsToTimer(m.getProgress()));
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    /*
     * Download a voice note using an asynctask
     */
    private void downloadVoiceClip(final ViewHolderMessageChat holder, int position, long userHandle, final MegaNodeList nodeList) {
        Timber.d("downloadVoiceClip() ");
        try {
            new MegaChatAdapter.ChatVoiceClipAsyncTask(holder, position, userHandle).execute(nodeList);
        } catch (Exception ex) {
            Timber.e(ex, "Too many AsyncTasks");
        }
    }

    /*
     * When the download of a voice clip ends, update the view with the result
     */
    public void finishedVoiceClipDownload(long nodeHandle, int resultTransfer) {
        if (messages == null || messages.isEmpty() || messagesPlaying == null || messagesPlaying.isEmpty())
            return;
        Timber.d("nodeHandle = %d, the result of transfer is %d", nodeHandle, resultTransfer);

        for (MessageVoiceClip messagevc : messagesPlaying) {
            if (messagevc.getMessageHandle() == nodeHandle) {
                messagevc.setIsAvailable(resultTransfer);
                for (int posArray = 0; posArray < messages.size(); posArray++) {
                    if (messages.get(posArray).getMessage().getMsgId() == messagevc.getIdMessage()) {
                        int positionInAdapter = posArray + 1;
                        ViewHolderMessageChat holder = (ViewHolderMessageChat) listFragment.findViewHolderForAdapterPosition(positionInAdapter);
                        if (holder != null) notifyItemChanged(positionInAdapter);
                        break;
                    }
                }
            }
        }
    }

    /*
     * Updating the seekBar element and the mediaplayer progress
     */
    private void updatingSeekBar(long messageId, int progress) {
        if (messages == null || messages.isEmpty() || messagesPlaying == null || messagesPlaying.isEmpty())
            return;

        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getIdMessage() == messageId) {
                Timber.d("Update mediaplayer");
                m.setProgress(progress);
                m.getMediaPlayer().seekTo(m.getProgress());
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.get(i).getMessage().getMsgId() == m.getIdMessage()) {
                        int positionInAdapter = i + 1;
                        ViewHolderMessageChat holder = (ViewHolderMessageChat) listFragment.findViewHolderForAdapterPosition(positionInAdapter);
                        if (holder != null) {
                            Timber.d("Update holder views");
                            if (m.getUserHandle() == megaChatApi.getMyUserHandle()) {
                                holder.contentOwnMessageVoiceClipDuration.setText(milliSecondsToTimer(m.getProgress()));
                            } else {
                                holder.contentContactMessageVoiceClipDuration.setText(milliSecondsToTimer(m.getProgress()));
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
    }


    /*
     * Play or pause a voice clip
     */
    private void playOrPauseVoiceClip(int positionInAdapter, ViewHolderMessageChat holder) {
        AndroidMegaChatMessage currentMessage = getMessageAtAdapterPosition(positionInAdapter);
        if (currentMessage == null || currentMessage.getMessage() == null ||
                currentMessage.getMessage().getType() != MegaChatMessage.TYPE_VOICE_CLIP ||
                messagesPlaying == null || messagesPlaying.isEmpty())
            return;

        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getIdMessage() == currentMessage.getMessage().getMsgId()) {
                if (m.getMediaPlayer().isPlaying()) {
                    Timber.d("isPlaying: PLAY -> PAUSE");
                    pauseVoiceClip(m, positionInAdapter);
                    return;
                }
                if (m.isPaused()) {
                    Timber.d("notPlaying: PAUSE -> PLAY");
                    playVoiceClip(m, null);
                    return;
                }

                Timber.d("notPlaying: find voice clip");
                MegaNodeList nodeList = currentMessage.getMessage().getMegaNodeList();
                if (nodeList.size() < 1 || !isVoiceClip(nodeList.get(0).getName())) break;

                File vcFile = CacheFolderManager.buildVoiceClipFile(nodeList.get(0).getName());
                if (!isFileAvailable(vcFile) || vcFile.length() != nodeList.get(0).getSize())
                    downloadVoiceClip(holder, positionInAdapter, currentMessage.getMessage().getUserHandle(), nodeList);

                playVoiceClip(m, vcFile.getAbsolutePath());
                break;
            }
        }
    }


    /*
     * Pause the voice clip
     */
    private void pauseVoiceClip(MessageVoiceClip m, int positionInAdapter) {
        m.getMediaPlayer().pause();
        m.setProgress(m.getMediaPlayer().getCurrentPosition());
        m.setPaused(true);
        removeCallBacks();
        notifyItemChanged(positionInAdapter);
    }

    /*
     * Play the voice clip
     */
    private void playVoiceClip(MessageVoiceClip m, String voiceClipPath) {
        AudioPlayerService.pauseAudioPlayer(context);

        stopAllReproductionsInProgress();
        final long mId = m.getIdMessage();
        ((ChatActivity) context).startProximitySensor();

        if (voiceClipPath == null) {
            m.getMediaPlayer().seekTo(m.getProgress());
            m.getMediaPlayer().start();
            m.setPaused(false);
            prepareMediaPlayer(mId);
        } else {
            try {
                m.getMediaPlayer().reset();
                m.getMediaPlayer().setDataSource(voiceClipPath);
                m.getMediaPlayer().setLooping(false);
                m.getMediaPlayer().prepare();
                m.setPaused(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            m.getMediaPlayer().seekTo(m.getProgress());
        }

        m.getMediaPlayer().setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Timber.d("mediaPlayerVoiceNotes:onError");
                ((ChatActivity) context).stopProximitySensor();
                mediaPlayer.reset();
                return true;
            }
        });
        m.getMediaPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Timber.d("mediaPlayerVoiceNotes:onPrepared");
                mediaPlayer.start();
                prepareMediaPlayer(mId);

            }
        });

        m.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Timber.d("mediaPlayerVoiceNotes:setOnCompletionListener");
                removeCallBacks();
                completionMediaPlayer(mId);
            }
        });
    }

    /**
     * Update media player when switching AudioManager mode
     */
    public void refreshVoiceClipPlayback() {
        if (messagesPlaying == null || messagesPlaying.isEmpty()) return;
        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getMediaPlayer().isPlaying()) {
                m.getMediaPlayer().seekTo(0);
                break;
            }
        }
    }

    /*
     * Stop the playing when this message is removed*/
    public void stopPlaying(long msgId) {
        ((ChatActivity) context).stopProximitySensor();

        if (messagesPlaying == null || messagesPlaying.isEmpty()) return;

        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getIdMessage() == msgId) {
                if (m.getMediaPlayer().isPlaying()) {
                    m.getMediaPlayer().stop();
                }
                m.getMediaPlayer().release();
                m.setMediaPlayer(null);
                messagesPlaying.remove(m);
                break;
            }
        }
    }

    public void pausePlaybackInProgress() {
        if (messagesPlaying == null || messagesPlaying.isEmpty()) return;
        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getMediaPlayer().isPlaying()) {
                int positionInAdapter = getAdapterItemPosition(m.getIdMessage());
                if (positionInAdapter != -1) pauseVoiceClip(m, positionInAdapter);
                break;
            }
        }
    }

    /*
     * Restore values when a playback ends
     */
    private void completionMediaPlayer(long msgId) {
        if (messages == null || messages.isEmpty() || messagesPlaying == null || messagesPlaying.isEmpty())
            return;

        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getIdMessage() == msgId) {
                Timber.d("completionMediaPlayer ");
                m.setProgress(0);
                m.setPaused(false);
                if (m.getMediaPlayer().isPlaying()) {
                    m.getMediaPlayer().stop();
                }
                m.getMediaPlayer().seekTo(m.getProgress());
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.get(i).getMessage().getMsgId() == msgId) {
                        int positionInAdapter = i + 1;
                        notifyItemChanged(positionInAdapter);
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * Get the voice clip that it is playing
     * @return The voice clip that it is playing
     */
    public MessageVoiceClip getVoiceClipPlaying() {
        if (messagesPlaying == null || messagesPlaying.isEmpty()) return null;
        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getMediaPlayer().isPlaying()) {
                return m;
            }
        }
        return null;
    }

    /*
     * Stop the voice notes that are playing and update the necessary views
     */
    public void stopAllReproductionsInProgress() {

        if (messagesPlaying == null || messagesPlaying.isEmpty()) return;
        removeCallBacks();

        for (MessageVoiceClip m : messagesPlaying) {
            if (m.getMediaPlayer().isPlaying()) {
                Timber.d("PLAY -> STOP");
                completionMediaPlayer(m.getIdMessage());
            }
        }
    }

    public void destroyVoiceElemnts() {
        Timber.d("destroyVoiceElemnts()");
        removeCallBacks();
        if (messagesPlaying == null || messagesPlaying.isEmpty()) return;
        for (MessageVoiceClip m : messagesPlaying) {
            m.getMediaPlayer().release();
            m.setMediaPlayer(null);
        }
        messagesPlaying.clear();
    }

    private void activateMultiselectMode(int currentPosition) {
        setMultipleSelect(true);
        ((ChatActivity) context).activateActionMode();
        ((ChatActivity) context).itemClick(currentPosition, null);
    }

    @Override
    public boolean onLongClick(View view) {
        megaChatApi.signalPresenceActivity();

        ViewHolderMessageChat holder = (ViewHolderMessageChat) view.getTag();
        int currentPosition = holder.getAdapterPosition();

        if (isMultipleSelect() || currentPosition < 1 || messages.get(currentPosition - 1).isUploading()) {
            return true;
        }
        if (MegaApplication.isShowInfoChatMessages()) {
            ((ChatActivity) context).showMessageInfo(currentPosition);
        } else {
            ((ChatActivity) context).itemLongClick(currentPosition);
        }
        return true;
    }

    private void removeCallBacks() {
        Timber.d("removeCallBacks()");

        if (handlerVoiceNotes == null) return;

        if (runnableVC != null) {
            handlerVoiceNotes.removeCallbacks(runnableVC);
        }
        ((ChatActivity) context).stopProximitySensor();

        runnableVC = null;
        handlerVoiceNotes.removeCallbacksAndMessages(null);
        handlerVoiceNotes = null;
    }

    private void setContactMessageName(int pos, ViewHolderMessageChat holder, long handle, boolean visibility) {
        if (isHolderNull(pos, holder)) {
            return;
        }
        holder.fullNameTitle = getContactMessageName(pos, holder, handle);
        if (!visibility) return;

        if (chatRoom.isGroup()) {
            holder.nameContactText.setVisibility(View.VISIBLE);
            holder.nameContactText.setText(holder.fullNameTitle);
        } else {
            holder.nameContactText.setVisibility(View.GONE);
        }
    }

    private String getContactMessageName(int pos, ViewHolderMessageChat holder, long handle) {
        if (isHolderNull(pos, holder)) {
            return null;
        }

        String name = cC.getParticipantFullName(handle);
        if (!isTextEmpty(name)) {
            return name;
        }

        Timber.w("NOT found in DB");
        name = context.getString(R.string.unknown_name_label);
        if (!holder.nameRequestedAction) {
            Timber.d("Call for nonContactName: %s", handle);
            holder.nameRequestedAction = true;
            int privilege = chatRoom.getPeerPrivilegeByHandle(handle);
            if (privilege == MegaChatRoom.PRIV_UNKNOWN || privilege == MegaChatRoom.PRIV_RM) {
                ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, handle, chatRoom.isPreview(), pos);
                megaChatApi.getUserFirstname(handle, chatRoom.getAuthorizationToken(), listener);
                megaChatApi.getUserLastname(handle, chatRoom.getAuthorizationToken(), listener);
                megaChatApi.getUserEmail(handle, listener);
            } else {
                MegaHandleList handleList = MegaHandleList.createInstance();
                handleList.addMegaHandle(handle);
                megaChatApi.loadUserAttributes(chatRoom.getChatId(), handleList,
                        new GetPeerAttributesListener(context, holder, this));
            }
        } else {
            Timber.w("Name already asked and no name received: %s", handle);
        }
        return name;
    }

    boolean isHolderNull(int pos, ViewHolderMessageChat holder) {
        if (holder == null) {
            holder = (ViewHolderMessageChat) listFragment.findViewHolderForAdapterPosition(pos);
            if (holder == null) {
                notifyItemChanged(pos);
                return true;
            }
        }

        return false;
    }

    public ViewHolderMessageChat queryIfHolderNull(int pos) {
        ViewHolderMessageChat holder = (ViewHolderMessageChat) listFragment.findViewHolderForAdapterPosition(pos);
        if (holder == null) {
            return null;
        }

        return holder;
    }

    private void setInfoToShow(int position, final ViewHolderMessageChat holder, boolean ownMessage, int infotToShow, String dateText, String timeText) {
        if (isHolderNull(position, holder)) {
            return;
        }

        if (ownMessage) {
            switch (infotToShow) {
                case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL: {
                    holder.dateLayout.setVisibility(View.VISIBLE);
                    holder.dateText.setText(dateText);
                    holder.titleOwnMessage.setVisibility(View.VISIBLE);
                    holder.timeOwnText.setText(timeText);
                    break;
                }
                case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME: {
                    holder.dateLayout.setVisibility(View.GONE);
                    holder.titleOwnMessage.setVisibility(View.VISIBLE);
                    holder.timeOwnText.setText(timeText);
                    break;
                }
                case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING: {
                    holder.dateLayout.setVisibility(View.GONE);
                    holder.titleOwnMessage.setVisibility(View.GONE);
                    break;
                }
            }
        } else {
            switch (infotToShow) {
                case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL: {
                    holder.dateLayout.setVisibility(View.VISIBLE);
                    holder.dateText.setText(dateText);
                    holder.titleContactMessage.setVisibility(View.VISIBLE);
                    holder.timeContactText.setText(timeText);
                    holder.timeContactText.setVisibility(View.VISIBLE);
                    break;
                }
                case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME: {
                    holder.dateLayout.setVisibility(View.GONE);
                    holder.titleContactMessage.setVisibility(View.VISIBLE);
                    holder.timeContactText.setText(timeText);
                    holder.timeContactText.setVisibility(View.VISIBLE);
                    break;
                }
                case AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING: {
                    holder.dateLayout.setVisibility(View.GONE);
                    holder.timeContactText.setVisibility(View.GONE);
                    holder.titleContactMessage.setVisibility(View.GONE);
                    break;
                }
            }
        }
    }

    /**
     * Method to know if the message is sent or received.
     *
     * @param message The MegaMessage.
     * @return True, if sent. False if received.
     */
    private boolean isMyMessage(MegaChatMessage message) {
        return message.getUserHandle() == myUserHandle &&
                message.getType() != MegaChatMessage.TYPE_PUBLIC_HANDLE_CREATE &&
                message.getType() != MegaChatMessage.TYPE_PUBLIC_HANDLE_DELETE &&
                message.getType() != MegaChatMessage.TYPE_SET_PRIVATE_MODE;
    }

    /**
     * Method for updating reactions if necessary.
     *
     * @param chatId   Chat ID.
     * @param message  Message.
     * @param reaction The reaction.
     * @param count    total number of users who have used that reaction in that message.
     */
    public void checkReactionUpdated(long chatId, MegaChatMessage message, String reaction, int count) {
        if (message == null || chatRoom.getChatId() != chatId) {
            Timber.d("Message is null or is a different chat ");
            return;
        }

        int positionInAdapter = INVALID_POSITION;
        AndroidMegaChatMessage megaMessage = null;

        for (AndroidMegaChatMessage msg : messages) {
            if (msg == null || msg.getMessage() == null) {
                Timber.w("The message is not valid");
                continue;
            }

            if (msg.getMessage().getMsgId() == message.getMsgId()) {
                positionInAdapter = messages.indexOf(msg) + 1;
                megaMessage = msg;
                break;
            }
        }

        if (positionInAdapter == INVALID_POSITION) {
            Timber.e("Message doesn't exist ");
            return;
        }

        ViewHolderMessageChat holder = (ViewHolderMessageChat) listFragment.findViewHolderForAdapterPosition(positionInAdapter);
        if (holder == null) {
            notifyItemChanged(positionInAdapter);
            return;
        }

        checkReactionsLayout(chatId, megaMessage, holder, reaction, count);
    }

    /**
     * Method for displaying the reaction layout.
     *
     * @param chatId      The chat room ID.
     * @param megaMessage The message.
     * @param holder      The holder.
     * @param reaction    The reaction.
     * @param count       The number of reactions in this message.
     */
    private void checkReactionsLayout(long chatId, AndroidMegaChatMessage megaMessage, final ViewHolderMessageChat holder, String reaction, int count) {
        MegaStringList listReactions = megaChatApi.getMessageReactions(chatId, megaMessage.getMessage().getMsgId());
        if (noReactions(listReactions, megaMessage.getMessage(), holder)) {
            return;
        }

        if (isMyMessage(megaMessage.getMessage())) {
            if (holder.ownReactionsAdapter != null && holder.ownReactionsAdapter.isSameAdapter(chatId, megaMessage.getMessage().getMsgId())) {
                if (count == 0) {
                    holder.ownReactionsAdapter.removeItem(reaction, chatId, megaMessage.getMessage().getMsgId());
                } else {
                    holder.ownMessageReactionsRecycler.getRecycledViewPool().clear();
                    holder.ownReactionsAdapter.updateItem(reaction, chatId, megaMessage.getMessage().getMsgId());
                }

                if (holder.ownReactionsAdapter.getItemCount() > 0) {
                    int maxSize = getMaxWidthItem(chatId, megaMessage.getMessage().getMsgId(), holder.ownReactionsAdapter.getListReactions(), outMetrics);
                    holder.ownMessageReactionsRecycler.columnWidth(maxSize);
                    holder.ownMessageReactionsLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.ownMessageReactionsLayout.setVisibility(View.GONE);
                    holder.ownReactionsAdapter = null;
                    holder.ownMessageReactionsRecycler.setAdapter(holder.ownReactionsAdapter);

                }
            } else {
                createReactionsAdapter(listReactions, true, chatId, megaMessage, holder);
            }
        } else {
            if (holder.contactReactionsAdapter != null && holder.contactReactionsAdapter.isSameAdapter(chatId, megaMessage.getMessage().getMsgId())) {
                if (count == 0) {
                    holder.contactReactionsAdapter.removeItem(reaction, chatId, megaMessage.getMessage().getMsgId());
                } else {
                    holder.contactMessageReactionsRecycler.getRecycledViewPool().clear();
                    holder.contactReactionsAdapter.updateItem(reaction, chatId, megaMessage.getMessage().getMsgId());
                }

                if (holder.contactReactionsAdapter.getItemCount() > 0) {
                    int maxSize = getMaxWidthItem(chatId, megaMessage.getMessage().getMsgId(), holder.contactReactionsAdapter.getListReactions(), outMetrics);
                    holder.contactMessageReactionsRecycler.columnWidth(maxSize);
                    holder.contactMessageReactionsLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.contactMessageReactionsLayout.setVisibility(View.GONE);
                    holder.contactReactionsAdapter = null;
                    holder.contactMessageReactionsRecycler.setAdapter(holder.contactReactionsAdapter);
                }
            } else {
                createReactionsAdapter(listReactions, false, chatId, megaMessage, holder);
            }
        }
    }

    /**
     * Method for creating the reaction array of a message.
     *
     * @param listReactions The reactions list.
     * @param ownMessage    If the message is a own message.
     * @param chatId        The chat ID.
     * @param megaMessage   The message.
     * @param holder        The holder.
     */
    private void createReactionsAdapter(MegaStringList listReactions, boolean ownMessage, long chatId, AndroidMegaChatMessage megaMessage, final ViewHolderMessageChat holder) {
        ArrayList<String> list = getReactionsList(listReactions, true);
        int maxSize = getMaxWidthItem(chatId, megaMessage.getMessage().getMsgId(), list, outMetrics);
        if (ownMessage) {
            holder.ownMessageReactionsLayout.setVisibility(View.VISIBLE);
            holder.ownMessageReactionsRecycler.columnWidth(maxSize);
            holder.ownReactionsAdapter = new ReactionAdapter(context, holder.ownMessageReactionsRecycler, list, chatId, megaMessage);
            holder.ownMessageReactionsRecycler.setAdapter(holder.ownReactionsAdapter);
        } else {
            holder.contactMessageReactionsLayout.setVisibility(View.VISIBLE);
            holder.contactMessageReactionsRecycler.columnWidth(maxSize);
            holder.contactReactionsAdapter = new ReactionAdapter(context, holder.contactMessageReactionsRecycler, list, chatId, megaMessage);
            holder.contactMessageReactionsRecycler.setAdapter(holder.contactReactionsAdapter);
        }
    }

    /**
     * Method to check if there are no reactions in a message.
     *
     * @param listReactions The reactions list.
     * @param message       The message.
     * @param holder        The holder.
     * @return True if there isn't reaction. False, if there are some reactions.
     */
    private boolean noReactions(MegaStringList listReactions, MegaChatMessage message, final ViewHolderMessageChat holder) {
        if (listReactions == null || listReactions.size() <= 0) {
            if (isMyMessage(message)) {
                holder.ownMessageReactionsLayout.setVisibility(View.GONE);
                holder.ownReactionsAdapter = null;
                holder.ownMessageReactionsRecycler.setAdapter(holder.ownReactionsAdapter);
            } else {
                holder.contactMessageReactionsLayout.setVisibility(View.GONE);
                holder.contactReactionsAdapter = null;
                holder.contactMessageReactionsRecycler.setAdapter(holder.contactReactionsAdapter);
            }

            return true;
        }

        return false;
    }

    /**
     * Check if exists reactions in a message.
     *
     * @param positionInAdapter The position of this message.
     * @param holder            The holder.
     * @param chatId            The chat ID.
     * @param megaMessage       The message.
     */
    private void checkReactionsInMessage(int positionInAdapter, ViewHolderMessageChat holder, long chatId, AndroidMegaChatMessage megaMessage) {
        if (holder == null) {
            holder = (ViewHolderMessageChat) listFragment.findViewHolderForAdapterPosition(positionInAdapter);
            if (holder == null) {
                notifyItemChanged(positionInAdapter);
                return;
            }
        }

        MegaStringList listReactions = megaChatApi.getMessageReactions(chatId, megaMessage.getMessage().getMsgId());

        if (noReactions(listReactions, megaMessage.getMessage(), holder)) {
            holder.ownMessageReactionsLayout.setVisibility(View.GONE);
            holder.contactMessageReactionsLayout.setVisibility(View.GONE);
            return;
        }

        if (isMyMessage(megaMessage.getMessage())) {
            holder.ownMessageReactionsLayout.setVisibility(View.VISIBLE);
            if (holder.ownReactionsAdapter != null && holder.ownReactionsAdapter.isSameAdapter(chatId, megaMessage.getMessage().getMsgId())) {
                ArrayList<String> list = getReactionsList(listReactions, true);
                int maxSize = getMaxWidthItem(chatId, megaMessage.getMessage().getMsgId(), list, outMetrics);
                holder.ownMessageReactionsRecycler.columnWidth(maxSize);
                holder.ownReactionsAdapter.setReactions(list, chatId, megaMessage.getMessage().getMsgId());
            } else {
                createReactionsAdapter(listReactions, true, chatId, megaMessage, holder);
            }
        } else {
            holder.contactMessageReactionsLayout.setVisibility(View.VISIBLE);
            if (holder.contactReactionsAdapter != null && holder.contactReactionsAdapter.isSameAdapter(chatId, megaMessage.getMessage().getMsgId())) {
                ArrayList<String> list = getReactionsList(listReactions, true);
                int maxSize = getMaxWidthItem(chatId, megaMessage.getMessage().getMsgId(), list, outMetrics);
                holder.contactMessageReactionsRecycler.columnWidth(maxSize);
                holder.contactReactionsAdapter.setReactions(list, chatId, megaMessage.getMessage().getMsgId());
            } else {
                createReactionsAdapter(listReactions, false, chatId, megaMessage, holder);
            }
        }
    }

    private void checkMultiselectionMode(int positionInAdapter, final ViewHolderMessageChat holder, boolean ownMessage, long messageId) {
        if (isHolderNull(positionInAdapter, holder))
            return;

        if (multipleSelect) {
            if (ownMessage) {
                holder.ownMessageSelectLayout.setVisibility(View.VISIBLE);
                if (this.isItemChecked(messageId)) {
                    holder.ownMessageSelectIcon.setImageResource(mega.privacy.android.core.R.drawable.ic_select_folder);
                    holder.ownMessageSelectIcon.setColorFilter(null);
                } else {
                    holder.ownMessageSelectIcon.setImageResource(R.drawable.ic_unselect_chatroom);
                    holder.ownMessageSelectIcon.setColorFilter(
                            ContextCompat.getColor(holder.ownMessageSelectIcon.getContext(), R.color.grey_600_white_087),
                            PorterDuff.Mode.SRC_IN);
                }
            } else {
                holder.contactMessageSelectLayout.setVisibility(View.VISIBLE);
                if (this.isItemChecked(messageId)) {
                    holder.contactMessageSelectIcon.setImageResource(mega.privacy.android.core.R.drawable.ic_select_folder);
                    holder.contactMessageSelectIcon.setColorFilter(null);
                } else {
                    holder.contactMessageSelectIcon.setImageResource(R.drawable.ic_unselect_chatroom);
                    holder.contactMessageSelectIcon.setColorFilter(
                            ContextCompat.getColor(holder.ownMessageSelectIcon.getContext(), R.color.grey_600_white_087),
                            PorterDuff.Mode.SRC_IN);
                }
            }

        } else if (positionClicked != INVALID_POSITION && positionClicked == positionInAdapter) {
            listFragment.smoothScrollToPosition(positionClicked);
        }
    }

    private int getAdapterItemPosition(long id) {
        for (int position = 0; position < messages.size(); position++) {
            if (messages.get(position).getMessage().getMsgId() == id) {
                return position + 1;
            }
        }
        return INVALID_POSITION;
    }

    public MegaChatRoom getChatRoom() {
        return chatRoom;
    }

    /**
     * Updates the uploading messages which are on paused state and are visible.
     *
     * @param firstVisibleMessage first visible position on the adapter
     * @param lastVisibleMessage  last visible position on the adapter
     */
    public void updatePausedUploadingMessages(int firstVisibleMessage, int lastVisibleMessage) {
        if (lastVisibleMessage == INVALID_POSITION || lastVisibleMessage == 0) {
            //No visible items, no need to update
            return;
        }

        if (lastVisibleMessage == getItemCount() || lastVisibleMessage >= messages.size()) {
            //Wrong index, replace by the latest message
            lastVisibleMessage = messages.size() - 1;
        }

        if (firstVisibleMessage == INVALID_POSITION) {
            //Wrong index, replace by the first message
            firstVisibleMessage = 0;
        }

        for (int i = firstVisibleMessage; i <= lastVisibleMessage; i++) {
            AndroidMegaChatMessage message = messages.get(i);
            if (message == null) {
                continue;
            }

            if (message.isUploading()) {
                // Update message with uploading state. Increment one position to take into account the header message.l
                notifyItemChanged(i + 1);
            }
        }
    }

    /**
     * Extracts a contact link from a chat messages if exists.
     *
     * @param content Text of the chat message.
     * @return The contact link if exists.
     */
    private String extractContactLink(String content) {
        try {
            if (content != null && !content.trim().isEmpty()) {
                Matcher m = Patterns.WEB_URL.matcher(content);

                while (m.find()) {
                    String url = Util.decodeURL(m.group());

                    if (Util.matchRegexs(url, Constants.CONTACT_LINK_REGEXS)) {
                        return url;
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    /**
     * Gets the user handle of a contact link.
     *
     * @param link Contact link to check.
     * @return The user handle.
     */
    private long getContactLinkHandle(String link) {
        String[] s = link.split("C!");

        return s.length > 0
                ? MegaApiAndroid.base64ToHandle(s[1].trim())
                : INVALID_HANDLE;
    }
}
