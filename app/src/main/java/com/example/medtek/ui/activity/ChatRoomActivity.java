package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.controller.AppointmentController;
import com.example.medtek.controller.ConversationController;
import com.example.medtek.databinding.ActivityChatRoomBinding;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.model.JanjiModel;
import com.example.medtek.model.MediaModel;
import com.example.medtek.model.MessageModel;
import com.example.medtek.model.UserModel;
import com.example.medtek.model.state.ChatType;
import com.example.medtek.network.base.BaseResponse;
import com.example.medtek.network.socket.SocketUtil;
import com.example.medtek.ui.adapter.ChatAdapter;
import com.example.medtek.ui.dialog.bottomsheetdialog.BSDEndSession;
import com.example.medtek.ui.dialog.bottomsheetdialog.BSDMediaPicker;
import com.example.medtek.ui.helper.SingleActivity;
import com.example.medtek.utils.Utils;
import com.example.medtek.utils.WorkerUtil;
import com.kbeanie.multipicker.api.FilePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.FilePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenFile;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

import static com.example.medtek.BuildConfig.BASE_URL;
import static com.example.medtek.constant.APPConstant.ERROR_NULL;
import static com.example.medtek.constant.APPConstant.LOGIN_PASIEN;
import static com.example.medtek.constant.APPConstant.MESSAGE_REQUEST_VIDEO_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_REQUEST_VOICE_CALL;
import static com.example.medtek.constant.APPConstant.NO_CONNECTION;
import static com.example.medtek.constant.APPConstant.SERVER_BROKEN;
import static com.example.medtek.constant.APPConstant.SUDAH_SELESAI;
import static com.example.medtek.utils.PropertyUtil.ACTIVE_CHAT;
import static com.example.medtek.utils.PropertyUtil.DATA_USER;
import static com.example.medtek.utils.PropertyUtil.USER_TYPE;
import static com.example.medtek.utils.PropertyUtil.deleteData;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.PropertyUtil.searchData;
import static com.example.medtek.utils.PropertyUtil.setData;
import static com.example.medtek.utils.RecyclerViewUtil.recyclerLinear;
import static com.example.medtek.utils.Utils.TAG;
import static com.example.medtek.utils.Utils.copyFileDst;
import static com.example.medtek.utils.Utils.dateTimeToStringHour;
import static com.example.medtek.utils.Utils.getDateTime;
import static com.example.medtek.utils.Utils.getDocumentPath;
import static com.example.medtek.utils.Utils.getFileExt;
import static com.example.medtek.utils.Utils.getFileInfo;
import static com.example.medtek.utils.Utils.getFileMimeType;
import static com.example.medtek.utils.Utils.getFileMimeTypeWithoutDot;
import static com.example.medtek.utils.Utils.getFileName;
import static com.example.medtek.utils.Utils.getImagePath;
import static com.example.medtek.utils.Utils.getPath;
import static com.example.medtek.utils.Utils.getPermisionCameraList;
import static com.example.medtek.utils.Utils.getVideoPath;
import static com.example.medtek.utils.Utils.hideKeyboard;
import static com.example.medtek.utils.Utils.isPatient;
import static com.example.medtek.utils.Utils.requestPermissionCompat;
import static com.example.medtek.utils.Utils.setNewFileName;
import static com.example.medtek.utils.Utils.showToastyError;
import static java.lang.String.valueOf;

public class ChatRoomActivity extends SingleActivity implements View.OnClickListener, FilePickerCallback, ChatAdapter.OnItemClickCallback {
    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    public static final int REQ_ACTIVE_CHAT = 6661;

    private static final int CHOOSER_PERMISSIONS_REQUEST_CODE_IMAGE = 7459;
    private static final int CHOOSER_PERMISSIONS_REQUEST_CODE_VIDEO = 7460;
    private static final int CHOOSER_PERMISSIONS_REQUEST_CODE_FILE = 7461;
    public static final String BUNDLE_CHATS = "bundle_chats";
    public static final String IS_ACTIVE_CHATS = "is_active_chats";
    public static final String IS_END_CHATS = "is_end_chats";

    private ConversationController conversationController;
    private AppointmentController appointmentController;
    private ActivityChatRoomBinding binding;
    private ChatsModel chatsModel;
    private ArrayList<MediaModel> mediaModels;

    private ChatAdapter chatAdapter;
    public Runnable moveToBottomRv = new Runnable() {
        @Override
        public void run() {
            if (binding.rvChat.getAdapter().getItemCount() > 1) {
                binding.rvChat.smoothScrollToPosition(binding.rvChat.getAdapter().getItemCount() - 1);
            }
        }
    };

    private EasyImage easyImage;
    private FilePicker filePicker;
    private List<String> imagesPath;
    private List<String> videosPath;
    private List<ChosenFile> filesPath;
    private int imagesCount;
    private boolean isActiveChats;
    private boolean isEndChats;
    private int endChat = 0;

    private final ArrayList<Integer> tempIdChat = new ArrayList<>();

    public static void navigate(Activity activity) {
        Intent intent = new Intent(activity, ChatRoomActivity.class);
        activity.startActivity(intent);
    }

    public static void navigate(Activity activity, ChatsModel model) {
        Intent intent = new Intent(activity, ChatRoomActivity.class);
        intent.putExtra(BUNDLE_CHATS, model);
        intent.putExtra(IS_ACTIVE_CHATS, false);
        activity.startActivity(intent);
    }

    public static void navigate(Activity activity, ChatsModel model, int reqCode) {
        Intent intent = new Intent(activity, ChatRoomActivity.class);
        intent.putExtra(BUNDLE_CHATS, model);
        intent.putExtra(IS_ACTIVE_CHATS, true);
        activity.startActivityForResult(intent, reqCode);
    }

    @Override
    protected View VcontentView() {
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected int IcontentView() {
        return 0;
    }

    private int imagesCountNow;

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
        if (getIntent() != null) {
            chatsModel = getIntent().getParcelableExtra(BUNDLE_CHATS);
            isActiveChats = getIntent().getBooleanExtra(IS_ACTIVE_CHATS, false);
        }
//        if (isActiveChats) {
//            setData(ACTIVE_CHAT, chatsModel);
//        }
        conversationController = new ConversationController();
        appointmentController = new AppointmentController();
        easyImage = initImagePickerGallery();
        mediaModels = new ArrayList<>();
        imagesCount = 0;
        imagesCountNow = 0;
        isEndChats = false;

        SocketUtil.getInstance().setChannelVideoChat(chatsModel.getIdJanji());
    }

//    private void connectSocket() {
//        EchoOptions echoOptions = new EchoOptions();
//        echoOptions.host = BASE_SOCKET_URL;
//
//        echoOptions.headers.put("Authorization", "Bearer " + getData(ACCESS_TOKEN));
//
//        Echo echo = new Echo(echoOptions);
//        echo.connect(args -> {
//            Log.d(TAG, "successConnectSocket");
//            listenForEvents(echo);
//        }, args -> Log.e(TAG, "errConnectSocket"));
//    }
//
//    private void listenForEvents(Echo echo) {
//        SocketIOPrivateChannel ioPrivateChannel = echo.privateChannel(CHANNEL_MESSAGES + "1");
//        ioPrivateChannel.listen(EVENT_MESSAGE_CREATED, args -> {
//            Log.d(TAG, "TEST");
//            MessageCreated NewChat = MessageCreated.Companion.parseFrom(args);
//            Log.d(TAG, NewChat.getChat().getMessage());
//            try {
//                App.getInstance().runOnUiThread(() -> {
//                    ChatType type;
//                    if (NewChat.getChat().getAttachment() == null) {
//                        type = ChatType.TEXT;
//                    } else {
//                        type = ChatType.IMAGE;
//                    }
//                    if (NewChat.getChat().getIdSender() != ((UserModel) getData(DATA_USER)).getIdUser()) {
//                        ChatsModel.Chat chat = initDataReceiverMessage(NewChat.getChat().getMessage(), type);
//                        chatsModel.getChats().add(chat);
//                        chatAdapter.addItem(chat);
//                        moveToBottomRV(moveToBottomRv, 0);
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Log.d(TAG, "isRegister");
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "isUnregister");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatEvent(Object newEvent) {
        try {
            App.getInstance().runOnUiThread(() -> {
                if (newEvent instanceof MessageModel) {
                    MessageModel newChat = (MessageModel) newEvent;
                    Log.d(TAG, "fromSocket: " + newChat.getChat().getMessage());
                    if (tempIdChat.isEmpty() || newChat.getChat().getIdChat() != tempIdChat.get(tempIdChat.size() - 1)) {
                        if (newChat.getChat().getIdSender() != ((UserModel) getData(DATA_USER)).getIdUser()) {
                            tempIdChat.add(newChat.getChat().getIdChat());
                            if (newChat.getChat().getAttachment() == null) {

                                ChatsModel.Chat chat = initDataReceiverMessage(newChat.getChat().getMessage(), ChatType.TEXT);
                                chatsModel.getChats().add(chat);
                                chatAdapter.addItem(chat);

                            } else {
                                if (newChat.getChat().getMessage().equals(ChatType.IMAGE.canonicalForm())) {
                                    Log.d(TAG, newChat.getChat().getAttachment());

                                    getImageAttachment(newChat.getChat().getAttachment(), true, -1);

                                } else if (newChat.getChat().getMessage().equals(ChatType.VIDEO.canonicalForm())) {
                                    ChatType type = ChatType.VIDEO;

                                    getVideoAttachment(newChat.getChat().getAttachment(), true, -1);
                                } else {
                                    ChatType type = ChatType.FILE;

                                    getFileAttachment(newChat.getChat().getAttachment(), true, -1);
                                }
                            }
                            moveToBottomRV(moveToBottomRv, 0);
                        }
                    }

                } else if (newEvent instanceof JanjiModel) {
                    JanjiModel janjiUpdate = (JanjiModel) newEvent;
                    Log.d(TAG, "idStatus: " + janjiUpdate.getJanji().getIdStatus());
                    if (janjiUpdate.getJanji().getIdStatus() == SUDAH_SELESAI) {
                        endSession();
                    }
                } else if (newEvent instanceof String) {
                    String newMessage = (String) newEvent;
                    if (newMessage.equals(MESSAGE_REQUEST_VIDEO_CALL)) {
                        IncomingVideoChatActivity.navigate(this, chatsModel);
                    } else if (newMessage.equals(MESSAGE_REQUEST_VOICE_CALL)) {
                        IncomingVoiceChatActivity.navigate(this, chatsModel);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setupView() {
        setupToolbar();
        chatAdapter = new ChatAdapter(App.getContext(), this, isActiveChats);
        setSenderPict(chatsModel.getSenderAvatar());
        binding.tvSenderName.setText(chatsModel.getSenderName());
        if (chatsModel.getChats().size() > 0) {
            addMediaToDB(chatsModel);
        }
        setupDataRVChat(chatsModel);
        if (!isActiveChats) {
            binding.rlFieldChat.setVisibility(View.GONE);
            binding.rlBtnChat.setVisibility(View.GONE);
            binding.rlEndChat.setVisibility(View.GONE);
        }

        chatAdapter.setOnItemClickCallback(this);

        binding.btnVidvoiceCall.setOnClickListener(this);
        binding.btnAttachFile.setOnClickListener(this);
        binding.vLayoutAttachment.setOnClickListener(this);
        binding.vLayoutAccess.setOnClickListener(this);
        binding.optImageAttach.setOnClickListener(this);
        binding.optVideoAttach.setOnClickListener(this);
        binding.optDocsAttach.setOnClickListener(this);
        binding.optVideoCall.setOnClickListener(this);
        binding.optVoiceCall.setOnClickListener(this);
        binding.rlEndChat.setOnClickListener(this);

        if (isPatient()) {
            binding.rlVidvoiceCall.setVisibility(View.GONE);
        }

        binding.etFieldChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isChatEmpty(s.length() == 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.rvChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                moveToBottomRV(moveToBottomRv, 0);
            }
        });

    }

    private void addMediaToDB(ChatsModel chatsModel) {
        int idMedia = 0;
        for (ChatsModel.Chat chat : chatsModel.getChats()) {
            Log.d(TAG, "type: " + chat.getType().canonicalForm());
            Log.d(TAG, "message: " + chat.getMessage());

            if (chat.getType() != ChatType.TEXT && chat.getType() != ChatType.END) {
                String path = "";
                File checkFile = new File(chat.getMessage());

                Log.d(TAG, "checkFileExist: " + checkFile.isFile());
                if (checkFile.isFile()) {
                    path = chat.getMessage();
                } else {
                    String checkPath = getPath(chat.getType()) +
                            getFileName(chat.getMessage());

                    Log.d(TAG, "checkFileExist: " + checkPath);

                    File checkFileOnMediaFolder = new File(checkPath);

                    Log.d(TAG, "checkFileExist: " + checkFileOnMediaFolder.isFile());
                    if (checkFileOnMediaFolder.isFile()) {
                        path = checkPath;
                    }
                }

                if (!path.isEmpty()) {
                    MediaModel media = new MediaModel(
                            ++idMedia,
                            chat.getIdChat(),
                            getFileName(path),
                            path,
                            getFileExt(path),
                            chat.getType()
                    );
                    mediaModels.add(media);
                }
            }
        }
    }

    private void setSenderPict(String senderAvatar) {
        if (senderAvatar.equals("")) {
            if ((int) getData(USER_TYPE) == LOGIN_PASIEN) {
                binding.ivSenderPict.setImageResource(R.drawable.ic_dokter_square);
            } else {
                binding.ivSenderPict.setImageResource(R.drawable.ic_pasien_square);
            }
        } else {
            Glide.with(this)
                    .load(BASE_URL + senderAvatar)
                    .into(binding.ivSenderPict);
        }
    }

    private String getSenderName(ChatsModel.Chat chat) {
        String senderName = chatsModel.getSenderName();
        if (chat.getIdSender() == ((UserModel) getData(DATA_USER)).getIdUser()) {
            senderName = ((UserModel) getData(DATA_USER)).getName();
        }
        return senderName;
    }

    public void setupDataRVChat(ChatsModel chatsModels) {
        ArrayList<ChatsModel.Chat> chats = new ArrayList<>(chatsModels.getChats());
        chatAdapter.setChatList(chats);
        recyclerLinear(binding.rvChat, LinearLayoutManager.VERTICAL, chatAdapter);
    }

    private void isChatEmpty(boolean status) {
        if (status) {
            binding.btnSendChat.setBackground(getResources().getDrawable(R.drawable.ic_send_chat_disable));
            binding.btnSendChat.setOnClickListener(null);
            if (!isPatient()) {
                binding.rlVidvoiceCall.setVisibility(View.VISIBLE);
            }
        } else {
            binding.btnSendChat.setBackground(getResources().getDrawable(R.drawable.ic_send_chat_enable));
            binding.btnSendChat.setOnClickListener(this);
            if (!isPatient()) {
                binding.rlVidvoiceCall.setVisibility(View.GONE);
            }
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_back_primary);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.chat_menu, menu);
//        return true;
//    }

    private void fadeTransitionAttachment() {
        Transition transition = new Fade();
        transition.setDuration(300);
        transition.addTarget(R.id.rl_attachment);
        TransitionManager.beginDelayedTransition(binding.rlAttachment, transition);
    }

    private void fadeTransitionAccess() {
        Transition transition = new Fade();
        transition.setDuration(300);
        transition.addTarget(R.id.rl_access_video_voice);
        TransitionManager.beginDelayedTransition(binding.rlAccessVideoVoice, transition);
    }

    private ChatsModel.Chat initDataSenderMessage(String message, ChatType type) {
        int idNow = 1;
        if (chatsModel.getChats().size() > 1) {
            idNow = chatsModel.getChats().get(chatsModel.getChats().size() - 1).getIdChat();
        }
        int idSender = ((UserModel) getData(DATA_USER)).getIdUser();
        int idReceiver = chatsModel.getIdSender();

        String time = dateTimeToStringHour(new DateTime());
        if (type != ChatType.TEXT && type != ChatType.END) {
            MediaModel media = new MediaModel(
                    (mediaModels.size() > 2) ? mediaModels.size() - 1 : 1,
                    idNow + 1,
                    getFileName(message),
                    message,
                    getFileExt(message),
                    type
            );
            mediaModels.add(media);
        }

        ChatsModel.Chat chat =  new ChatsModel.Chat(idNow + 1, idSender, idReceiver, time, false, type, message);

        if (searchData(ACTIVE_CHAT)) {
            ChatsModel activeChatsModel = ((ChatsModel) getData(ACTIVE_CHAT));
            activeChatsModel.getChats().add(chat);
            setData(ACTIVE_CHAT, activeChatsModel);
        }

        return chat;
    }

    private ChatsModel.Chat initDataReceiverMessage(String message, ChatType type) {
        int idNow = 1;
        if (chatsModel.getChats().size() > 1) {
            idNow = chatsModel.getChats().get(chatsModel.getChats().size() - 1).getIdChat();
        }
        int idReceiver = ((UserModel) getData(DATA_USER)).getIdUser();
        int idSender = chatsModel.getIdSender();

        String time = dateTimeToStringHour(new DateTime());
        Log.d(TAG, message);
        if (type != ChatType.TEXT && type != ChatType.END) {
            MediaModel media = new MediaModel(
                    mediaModels.size() - 1,
                    idNow + 1,
                    getFileName(message),
                    message,
                    getFileExt(message),
                    type
            );
            mediaModels.add(media);
        }

        ChatsModel.Chat chat = new ChatsModel.Chat(idNow + 1, idSender, idReceiver, time, false, type, message);

        if (searchData(ACTIVE_CHAT)) {
            ChatsModel activeChatsModel = ((ChatsModel) getData(ACTIVE_CHAT));
            activeChatsModel.getChats().add(chat);
            setData(ACTIVE_CHAT, activeChatsModel);
        }

        return chat;
    }

    public void moveToBottomRV(Runnable runnable, int timeDelay) {
        binding.rvChat.postDelayed(runnable, timeDelay);
    }

    public void moveToBottomImage(Runnable runnable, int timeDelay) {
        imagesCountNow++;
        Log.d(TAG, "imagesCountNow: " + imagesCountNow);
        if (imagesCountNow < imagesCount) {
            binding.rvChat.postDelayed(runnable, timeDelay);
        }
    }

    public Runnable getRunnable() {
        return moveToBottomRv;
    }

    private void initImagePickerCamera() {
        new ImagePicker.Builder(ChatRoomActivity.this)
                .mode(ImagePicker.Mode.CAMERA)
                .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
                .directory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                        File.separator +
                        "Medtek" +
                        File.separator +
                        "Images" +
                        File.separator)
                .enableDebuggingMode(true)
                .build();
    }

    private void initVideoPickerCamera() {
        new VideoPicker.Builder(ChatRoomActivity.this)
                .mode(VideoPicker.Mode.CAMERA)
                .directory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                        File.separator +
                        "Medtek" +
                        File.separator +
                        "Videos" +
                        File.separator)
                .enableDebuggingMode(true)
                .build();
    }

    private EasyImage initImagePickerGallery() {
        return new EasyImage.Builder(ChatRoomActivity.this)
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .allowMultiple(true)
                .build();
    }

    private void initVideoPickerGallery() {
        new VideoPicker.Builder(ChatRoomActivity.this)
                .mode(VideoPicker.Mode.GALLERY)
                .enableDebuggingMode(true)
                .build();
    }

    private void postConversation(String message, String attachment, int idConversation) {
        Data data = new Data.Builder()
                .putString(WorkerUtil.BUNDLE_MESSAGE, message)
                .putString(WorkerUtil.BUNDLE_ATTACHMENT, attachment)
                .putInt(WorkerUtil.BUNDLE_ID_CONV, idConversation)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(WorkerUtil.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);
        WorkManager.getInstance().getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(this, workInfo -> {
                    Log.d(TAG, "status: " + workInfo.getState().name());
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_chat:
                String message = binding.etFieldChat.getText().toString();
                if (message.length() > 0) {
                    ChatsModel.Chat chat = initDataSenderMessage(message, ChatType.TEXT);
                    chatsModel.getChats().add(chat);
                    chatAdapter.addItem(chat);
                    moveToBottomRV(moveToBottomRv, 0);
                    postConversation(message, "", chatsModel.getIdConversation());
                }
                binding.etFieldChat.setText("");
                break;
            case R.id.btn_attach_file:
                if (binding.rlAttachment.getVisibility() == View.GONE || binding.rlAttachment.getVisibility() == View.INVISIBLE) {
                    fadeTransitionAttachment();
                    binding.rlAttachment.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_vidvoice_call:
                if (binding.rlAccessVideoVoice.getVisibility() == View.GONE || binding.rlAccessVideoVoice.getVisibility() == View.INVISIBLE) {
                    fadeTransitionAccess();
                    binding.rlAccessVideoVoice.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.v_layout_attachment:
                if (binding.rlAttachment.getVisibility() == View.VISIBLE) {
                    fadeTransitionAttachment();
                    binding.rlAttachment.setVisibility(View.GONE);
                }
                break;
            case R.id.v_layout_access:
                if (binding.rlAccessVideoVoice.getVisibility() == View.VISIBLE) {
                    fadeTransitionAccess();
                    binding.rlAccessVideoVoice.setVisibility(View.GONE);
                }
                break;
            case R.id.opt_image_attach:
                if (Utils.checkPermission(this, getPermisionCameraList())) {
                    BSDMediaPicker fragment = new BSDMediaPicker(BSDMediaPicker.STATE_IMAGE);
                    fragment.show(getSupportFragmentManager(), fragment.getTag());
                    if (binding.rlAttachment.getVisibility() == View.VISIBLE) {
                        binding.rlAttachment.setVisibility(View.GONE);
                    }
                } else {
                    requestPermissionCompat(this, getPermisionCameraList(), CHOOSER_PERMISSIONS_REQUEST_CODE_IMAGE);
                }
                break;
            case R.id.opt_video_attach:
                if (Utils.checkPermission(this, getPermisionCameraList())) {
                    BSDMediaPicker fragment = new BSDMediaPicker(BSDMediaPicker.STATE_VIDEO);
                    fragment.show(getSupportFragmentManager(), fragment.getTag());
                    if (binding.rlAttachment.getVisibility() == View.VISIBLE) {
                        binding.rlAttachment.setVisibility(View.GONE);
                    }
                } else {
                    requestPermissionCompat(this, getPermisionCameraList(), CHOOSER_PERMISSIONS_REQUEST_CODE_VIDEO);
                }
                break;
            case R.id.opt_docs_attach:
                if (Utils.checkPermission(this, getPermisionCameraList())) {
                    getFileFromDir();
                    if (binding.rlAttachment.getVisibility() == View.VISIBLE) {
                        binding.rlAttachment.setVisibility(View.GONE);
                    }
                } else {
                    requestPermissionCompat(this, getPermisionCameraList(), CHOOSER_PERMISSIONS_REQUEST_CODE_FILE);
                }
                break;
            case R.id.opt_voice_call:
                if (binding.rlAccessVideoVoice.getVisibility() == View.VISIBLE) {
                    binding.rlAccessVideoVoice.setVisibility(View.GONE);
                }
                OutcomingVoiceChatActivity.navigate(this, chatsModel);
//                VoiceChatActivity.navigate(this, chatsModel);
                break;
            case R.id.opt_video_call:
                if (binding.rlAccessVideoVoice.getVisibility() == View.VISIBLE) {
                    binding.rlAccessVideoVoice.setVisibility(View.GONE);
                }
                OutcomingVideoChatActivity.navigate(this, chatsModel);
//                VideoChatActivity.navigate(this, chatsModel);
                break;
            case R.id.rl_end_chat:
                BSDEndSession fragment = new BSDEndSession();
                fragment.show(getSupportFragmentManager(), fragment.getTag());
                break;
        }
    }

    public void getImageFromCamera() {
        initImagePickerCamera();
    }

    public void getImageFromGallery() {
        easyImage.openGallery(ChatRoomActivity.this);
    }

    public void getVideoFromCamera() {
        initVideoPickerCamera();
    }

    public void getVideoFromGallery() {
        initVideoPickerGallery();
    }

    public void getFileFromDir() {
        filePicker = new FilePicker(this);
        filePicker.setFilePickerCallback(this);
        filePicker.allowMultiple();
        filePicker.pickFile();
    }

    private void loadImagePreview() {
        if (imagesPath != null && imagesPath.size() > 0) {
            imagesCountNow = 0;
            imagesCount = imagesPath.size() + 1;
            Log.d(TAG, "imagesCount: " + imagesCount);
            for (String imagePath : imagesPath) {
                if (imagePath.length() > 0) {
                    ChatsModel.Chat chat = initDataSenderMessage(imagePath, ChatType.IMAGE);
                    chatsModel.getChats().add(chat);
                    chatAdapter.addItem(chat);
                    postConversation(ChatType.IMAGE.canonicalForm(), imagePath, chatsModel.getIdConversation());
                }
                binding.etFieldChat.setText("");
            }
        }
    }

    private void loadVideoPreview() {
        if (videosPath != null && videosPath.size() > 0) {
            imagesCountNow = 0;
            imagesCount = videosPath.size() + 1;
            for (String videoPath : videosPath) {
                if (videoPath.length() > 0) {
                    ChatsModel.Chat chat = initDataSenderMessage(videoPath, ChatType.VIDEO);
                    chatsModel.getChats().add(chat);
                    chatAdapter.addItem(chat);
                    postConversation(ChatType.VIDEO.canonicalForm(), videoPath, chatsModel.getIdConversation());
                }
                binding.etFieldChat.setText("");
                moveToBottomImage(moveToBottomRv, 500);
            }
        }
    }

    private void loadFilePreview() {
        if (filesPath != null && filesPath.size() > 0) {
            for (ChosenFile file : filesPath) {
                File getFile = new File(file.getOriginalPath());
                String fileInfo = file.getDisplayName() + ","
                        + getFileMimeTypeWithoutDot(getFileMimeType(Uri.fromFile(getFile))) + ","
                        + file.getHumanReadableSize(false) + ","
                        + file.getOriginalPath() + ","
                        + getFileMimeType(Uri.fromFile(getFile));
                if (fileInfo.length() > 0) {
                    ChatsModel.Chat chat = initDataSenderMessage(fileInfo, ChatType.FILE);
                    chatsModel.getChats().add(chat);
                    chatAdapter.addItem(chat);
                    postConversation(ChatType.FILE.canonicalForm(), file.getOriginalPath(), chatsModel.getIdConversation());
                }
                binding.etFieldChat.setText("");
                moveToBottomRV(moveToBottomRv, 200);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CHOOSER_PERMISSIONS_REQUEST_CODE_IMAGE) {
                BSDMediaPicker fragment = new BSDMediaPicker(BSDMediaPicker.STATE_IMAGE);
                fragment.show(getSupportFragmentManager(), fragment.getTag());
                if (binding.rlAttachment.getVisibility() == View.VISIBLE) {
                    binding.rlAttachment.setVisibility(View.GONE);
                }
            } else if (requestCode == CHOOSER_PERMISSIONS_REQUEST_CODE_VIDEO) {
                BSDMediaPicker fragment = new BSDMediaPicker(BSDMediaPicker.STATE_VIDEO);
                fragment.show(getSupportFragmentManager(), fragment.getTag());
                if (binding.rlAttachment.getVisibility() == View.VISIBLE) {
                    binding.rlAttachment.setVisibility(View.GONE);
                }
            } else {
                getFileFromDir();
                if (binding.rlAttachment.getVisibility() == View.VISIBLE) {
                    binding.rlAttachment.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImagePicker.IMAGE_PICKER_REQUEST_CODE:
                    if (imagesPath != null && imagesPath.size() > 0) imagesPath.clear();
                    imagesPath = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
                    loadImagePreview();
                    break;
                case VideoPicker.VIDEO_PICKER_REQUEST_CODE:
                    if (videosPath != null && videosPath.size() > 0) videosPath.clear();
                    videosPath = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
                    loadVideoPreview();
                case Picker.PICK_FILE:
                    if (filePicker == null) {
                        filePicker = new FilePicker(this);
                        filePicker.setFilePickerCallback(this);
                    }
                    filePicker.submit(data);
                    break;
            }
        }

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(@NotNull MediaFile[] mediaFiles, @NotNull MediaSource mediaSource) {
                if (imagesPath != null && imagesPath.size() > 0) imagesPath.clear();
                imagesPath = new ArrayList<>();
                //create new dir
                File fileDst = new File(getImagePath());
                if (!fileDst.exists() || !fileDst.isDirectory()) {
                    fileDst.mkdirs();
                }

                for (MediaFile mediaFile : mediaFiles) {
                    // copy file from cache
                    File newFileDst = new File(fileDst.getPath() + File.separator + setNewFileName(ChatRoomActivity.this) + getFileExt(mediaFile.getFile().getPath()));
                    try {
                        copyFileDst(mediaFile.getFile(), newFileDst);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imagesPath.add(newFileDst.getPath());
                }
                loadImagePreview();
            }

            @Override
            public void onImagePickerError(@NotNull Throwable error, @NotNull MediaSource source) {
                error.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isActiveChats) {
            Intent resultIntent = new Intent();
            if (!isEndChats) {
                if (!searchData(ACTIVE_CHAT)) {
                    setData(ACTIVE_CHAT, chatsModel);
                }
            }
            resultIntent.putExtra(IS_END_CHATS, isEndChats);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFilesChosen(List<ChosenFile> list) {
        filesPath = list;
        loadFilePreview();
    }

    @Override
    public void onError(String s) {

    }

    public void endSession() {
        if (endChat < 1) {
            hideKeyboard(this);
            binding.rlFieldChat.setVisibility(View.GONE);
            binding.rlBtnChat.setVisibility(View.GONE);
            binding.rlEndChat.setVisibility(View.GONE);
            SocketUtil.getInstance().leaveChannelMessage(chatsModel.getIdConversation());
            SocketUtil.getInstance().leaveChannelJanji(chatsModel.getIdJanji());
            SocketUtil.getInstance().leaveChannelVideo(chatsModel.getIdJanji());
            ChatsModel.Chat chat = initDataSenderMessage("", ChatType.END);
            Log.d(TAG, "chatTime: " + chat.getTime());
            chatAdapter.addItem(chat);
            chatsModel.setActive(false);
            moveToBottomRV(moveToBottomRv, 0);

            deleteData(ACTIVE_CHAT);
            isEndChats = true;
            endChat += 1;
        }
    }

    public void sendEndSession() {
        appointmentController.getJanjiEnd(valueOf(chatsModel.getIdJanji()), new BaseCallback<BaseResponse>() {
            @Override
            public void onSuccess(BaseResponse result) {
                Log.d(TAG, "Chat End");
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(ChatRoomActivity.class), "Error");
                showToastyError(ChatRoomActivity.this, ERROR_NULL);
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(ChatRoomActivity.class), "No Connection");
                showToastyError(ChatRoomActivity.this, NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(ChatRoomActivity.class), "Server Broken");
                showToastyError(ChatRoomActivity.this, SERVER_BROKEN);
            }
        });
    }

    private void getImageAttachment(String attachment, boolean isNewMessage, int position) {
        conversationController.getImageMessage(attachment, new BaseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                try {
                    Bitmap bmp = BitmapFactory.decodeStream(result.byteStream());

                    File fileDst = new File(getImagePath());
                    Log.d(TAG, "fileDstExists: " + fileDst.exists());
                    Log.d(TAG, "fileDstDir: " + fileDst.isDirectory());
                    if (!fileDst.exists()) {
//                                                boolean mkdirCheck = fileDst.mkdir();
                        boolean mkdirsCheck = fileDst.mkdirs();

                        Log.d(TAG, "mkdirsCheck: " + mkdirsCheck);
                    }

                    Log.d(TAG, "new Image path: " + fileDst.getPath());

                    File newFileDst = new File(fileDst.getPath() +
                            File.separator +
                            getFileName(attachment));
                    //                                        byte[] fileData = result.bytes();
//
//                                        newFileDst.createNewFile();
//
                    FileOutputStream outputStream = new FileOutputStream(newFileDst);

                    bmp.compress((getFileExt(attachment) == ".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                            100, outputStream);

                    outputStream.flush();

//                                        outputStream.write(fileData);
                    outputStream.close();
//
//                                        Log.d(TAG, "new image path: " + fileDst.getPath());
                    if (isNewMessage) {
                        ChatsModel.Chat chat = initDataReceiverMessage(newFileDst.getPath(), ChatType.IMAGE);
                        chatsModel.getChats().add(chat);
                        chatAdapter.addItem(chat);
                    } else {
                        if (position != -1) {
                            int idChat = chatsModel.getChats().get(position).getIdChat();
                            int idSender = chatsModel.getChats().get(position).getIdSender();
                            int idReceiver = chatsModel.getChats().get(position).getIdReceiver();
                            String time = chatsModel.getChats().get(position).getTime();
                            boolean isRead = chatsModel.getChats().get(position).isRead();
                            ChatType type = chatsModel.getChats().get(position).getType();

                            MediaModel media = new MediaModel(
                                    (mediaModels.size() > 2) ? mediaModels.size() - 1 : 1,
                                    idChat,
                                    getFileName(newFileDst.getPath()),
                                    newFileDst.getPath(),
                                    getFileExt(newFileDst.getPath()),
                                    type
                            );
                            mediaModels.add(media);

                            ChatsModel.Chat chat = new ChatsModel.Chat(idChat, idSender, idReceiver, time, isRead, type, newFileDst.getPath());
                            chatsModel.getChats().set(position, chat);
                            chatAdapter.updateItem(position, chat);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(ChatRoomActivity.class), "Error");
                showToastyError(ChatRoomActivity.this, ERROR_NULL);
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(ChatRoomActivity.class), "No Connection");
//                showToastyError(ChatRoomActivity.this, NO_CONNECTION);
                getImageAttachment(attachment, isNewMessage, position);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(ChatRoomActivity.class), "Server Broken");
                showToastyError(ChatRoomActivity.this, SERVER_BROKEN);
            }
        });
    }

    private void getVideoAttachment(String attachment, boolean isNewMessage, int position) {
        conversationController.getImageMessage(attachment, new BaseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                try {
                    File fileDst = new File(getVideoPath());
                    Log.d(TAG, "fileDstExists: " + fileDst.exists());
                    Log.d(TAG, "fileDstDir: " + fileDst.isDirectory());
                    if (!fileDst.exists()) {
                        fileDst.mkdirs();

//                        Log.d(TAG, "mkdirsCheck: " + mkdirsCheck);
                    }

                    Log.d(TAG, "new Video path: " + fileDst.getPath());

                    File newFileDst = new File(fileDst.getPath() +
                            File.separator +
                            getFileName(attachment));

                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        byte[] fileReader = new byte[4096];

                        long fileSize = result.contentLength();
                        long fileSizeDownloaded = 0;

                        inputStream = result.byteStream();
                        outputStream = new FileOutputStream(newFileDst);

                        while (true) {
                            int read = inputStream.read(fileReader);

                            if (read == -1) break;

                            outputStream.write(fileReader, 0, read);
                            fileSizeDownloaded += read;
                            Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                        }
                        outputStream.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }

                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }

                    if (isNewMessage) {
                        ChatsModel.Chat chat = initDataReceiverMessage(newFileDst.getPath(), ChatType.VIDEO);
                        chatsModel.getChats().add(chat);
                        chatAdapter.addItem(chat);
                    } else {
                        if (position != -1) {
                            int idChat = chatsModel.getChats().get(position).getIdChat();
                            int idSender = chatsModel.getChats().get(position).getIdSender();
                            int idReceiver = chatsModel.getChats().get(position).getIdReceiver();
                            String time = chatsModel.getChats().get(position).getTime();
                            boolean isRead = chatsModel.getChats().get(position).isRead();
                            ChatType type = chatsModel.getChats().get(position).getType();

                            MediaModel media = new MediaModel(
                                    (mediaModels.size() > 2) ? mediaModels.size() - 1 : 1,
                                    idChat,
                                    getFileName(newFileDst.getPath()),
                                    newFileDst.getPath(),
                                    getFileExt(newFileDst.getPath()),
                                    type
                            );
                            mediaModels.add(media);

                            ChatsModel.Chat chat = new ChatsModel.Chat(idChat, idSender, idReceiver, time, isRead, type, newFileDst.getPath());
                            chatsModel.getChats().set(position, chat);
                            chatAdapter.updateItem(position, chat);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(ChatRoomActivity.class), "Error");
                showToastyError(ChatRoomActivity.this, ERROR_NULL);
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(ChatRoomActivity.class), "No Connection");
//                showToastyError(ChatRoomActivity.this, NO_CONNECTION);
                getVideoAttachment(attachment, isNewMessage, position);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(ChatRoomActivity.class), "Server Broken");
                showToastyError(ChatRoomActivity.this, SERVER_BROKEN);
            }
        });
    }

    private void getFileAttachment(String attachment, boolean isNewMessage, int position) {
        conversationController.getImageMessage(attachment, new BaseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                try {
                    File fileDst = new File(getDocumentPath());
                    Log.d(TAG, "fileDstExists: " + fileDst.exists());
                    Log.d(TAG, "fileDstDir: " + fileDst.isDirectory());
                    if (!fileDst.exists()) {
                        fileDst.mkdirs();

//                        Log.d(TAG, "mkdirsCheck: " + mkdirsCheck);
                    }

                    Log.d(TAG, "new File path: " + fileDst.getPath());

                    File newFileDst = new File(fileDst.getPath() +
                            File.separator +
                            getFileName(attachment));

                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        byte[] fileReader = new byte[4096];

                        long fileSize = result.contentLength();
                        long fileSizeDownloaded = 0;

                        inputStream = result.byteStream();
                        outputStream = new FileOutputStream(newFileDst);

                        while (true) {
                            int read = inputStream.read(fileReader);

                            if (read == -1) break;

                            outputStream.write(fileReader, 0, read);
                            fileSizeDownloaded += read;
                            Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                        }
                        outputStream.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }

                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }

                    String fileInfo = getFileInfo(newFileDst);

                    Log.d(TAG, "fileInfo: " + fileInfo);

                    if (isNewMessage) {
                        ChatsModel.Chat chat = initDataReceiverMessage(fileInfo, ChatType.FILE);
                        chatsModel.getChats().add(chat);
                        chatAdapter.addItem(chat);
                    } else {
                        if (position != -1) {
                            int idChat = chatsModel.getChats().get(position).getIdChat();
                            int idSender = chatsModel.getChats().get(position).getIdSender();
                            int idReceiver = chatsModel.getChats().get(position).getIdReceiver();
                            String time = chatsModel.getChats().get(position).getTime();
                            boolean isRead = chatsModel.getChats().get(position).isRead();
                            ChatType type = chatsModel.getChats().get(position).getType();

                            ChatsModel.Chat chat = new ChatsModel.Chat(idChat, idSender, idReceiver, time, isRead, type, newFileDst.getPath());
                            chatsModel.getChats().set(position, chat);
                            chatAdapter.updateItem(position, chat);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(ChatRoomActivity.class), "Error");
                showToastyError(ChatRoomActivity.this, ERROR_NULL);
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(ChatRoomActivity.class), "No Connection");
//                showToastyError(ChatRoomActivity.this, NO_CONNECTION);
                getFileAttachment(attachment, isNewMessage, position);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(ChatRoomActivity.class), "Server Broken");
                showToastyError(ChatRoomActivity.this, SERVER_BROKEN);
            }
        });
    }

    @Override
    public void onItemTextClick(ChatsModel.Chat model, View view, int position, boolean isFileExist) {

    }

    @Override
    public void onItemImageClick(ChatsModel.Chat model, View view, ProgressBar loading, LinearLayout llDownloadImage, int position, boolean isFileExist) {
        String time = (isActiveChats) ? model.getTime() : dateTimeToStringHour(getDateTime(model.getTime(), DateTimeZone.UTC));
        if (isFileExist || isActiveChats) {
            MediaViewerActivity.navigate(ChatRoomActivity.this, mediaModels, getSenderName(model), time, model.getIdChat());
        } else {
            chatAdapter.setImageLoading(loading, llDownloadImage);
            getImageAttachment(model.getMessage(), false, position);
        }
    }

    @Override
    public void onItemVideoClick(ChatsModel.Chat model, View view, ProgressBar loading, LinearLayout llDownloadVideo, LinearLayout llOpenVid, int position, boolean isFileExist) {
        String time = (isActiveChats) ? model.getTime() : dateTimeToStringHour(getDateTime(model.getTime(), DateTimeZone.UTC));

        if (isFileExist || isActiveChats) {
            MediaViewerActivity.navigate(ChatRoomActivity.this, mediaModels, getSenderName(model), time, model.getIdChat());
        } else {
            chatAdapter.setVideoLoading(loading, llDownloadVideo, llOpenVid);
            getVideoAttachment(model.getMessage(), false, position);
        }
    }

    @Override
    public void onItemFileClick(ChatsModel.Chat model, View view, ProgressBar loading, ImageView ivDownloadFile, TextView tvFileExt, int position, boolean isFileExist) {
        if (isFileExist || isActiveChats) {
            File file = new File(getPath(model.getType()) +
                    getFileName(model.getMessage()));
            String[] arrFileInfo = getFileInfo(file).split(",");
            Intent intent = new Intent(Intent.ACTION_VIEW);

//            String fileExt = getFileExt(arrFileInfo[3]);
//            Log.d(TAG, "fileExt: " + fileExt);
//            String mimeType = "*/*";
//
//            if (fileExt.contains("jpg") || fileExt.contains("png") || fileExt.contains("gif")) {
//                mimeType = "image/*";
//            } else if (fileExt.contains("mp4") || fileExt.contains("3gp")) {
//                mimeType = "video/*";
//            } else if (fileExt.contains("pdf")) {
//                mimeType = "application/pdf";
//            } else if (fileExt.contains("doc") || fileExt.contains("docx")) {
//                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
//            } else if (fileExt.contains("ppt") || fileExt.contains("pptx")) {
//                mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
//            } else if (fileExt.contains("xls") || fileExt.contains("xslx")) {
//                mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
//            }

//            MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, ((path, uri) -> {
//                intent.setDataAndType(uri, finalMimeType);
//            }));

//            Uri uri = Uri.fromFile(file);
            Uri uri = FileProvider.getUriForFile(this,getApplicationContext().getPackageName()+".fileprovider", file);
            intent.setDataAndType(uri, arrFileInfo[4]);

            Log.d(TAG, "uri: " +uri.toString());
            Log.d(TAG, "mimeType: " + arrFileInfo[4]);

            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            Log.d(TAG, uri.toString());

//            intent.putExtra(Intent.EXTRA_STREAM, uri);
//            intent.setType(arrFileInfo[4]);

            Intent chooser = Intent.createChooser(intent, "Choose to Open File");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        } else {
            chatAdapter.setFileLoading(loading, ivDownloadFile, tvFileExt);
            getFileAttachment(model.getMessage(), false, position);
        }
    }

}

