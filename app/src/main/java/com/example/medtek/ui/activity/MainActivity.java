package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.medtek.R;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.controller.AppointmentController;
import com.example.medtek.controller.ConversationController;
import com.example.medtek.controller.LoginController;
import com.example.medtek.controller.UserController;
import com.example.medtek.model.AppointmentModel;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.model.ImageModel;
import com.example.medtek.model.ScheduleDoctorModel;
import com.example.medtek.model.UserModel;
import com.example.medtek.model.state.ApplyStateType;
import com.example.medtek.model.state.ChatType;
import com.example.medtek.network.response.AuthTokenResponse;
import com.example.medtek.network.response.GetConversationListResponse;
import com.example.medtek.network.response.GetConversationResponse;
import com.example.medtek.network.response.GetInfoUserResponse;
import com.example.medtek.network.response.GetJanjiListResponse;
import com.example.medtek.network.socket.SocketUtil;
import com.example.medtek.ui.fragment.ChatFragment;
import com.example.medtek.ui.pasien.home.HomeFragment;
import com.example.medtek.ui.pasien.others.OthersFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.example.medtek.constant.APPConstant.ERROR_NULL;
import static com.example.medtek.constant.APPConstant.EVENT_RESPONSE_ON_CALL;
import static com.example.medtek.constant.APPConstant.IMAGE_AVATAR;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VOICE_CALL;
import static com.example.medtek.constant.APPConstant.NO_CONNECTION;
import static com.example.medtek.constant.APPConstant.SERVER_BROKEN;
import static com.example.medtek.network.socket.SocketUtil.getMessageFromObject;
import static com.example.medtek.ui.pasien.home.HomeFragment.REQUEST_CODE_LOC;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.DATA_USER;
import static com.example.medtek.utils.PropertyUtil.EXPIRED_TOKEN;
import static com.example.medtek.utils.PropertyUtil.IS_AFTER_RESET;
import static com.example.medtek.utils.PropertyUtil.LOGIN_STATUS;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getApplicationState;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.PropertyUtil.searchData;
import static com.example.medtek.utils.PropertyUtil.setApplicationState;
import static com.example.medtek.utils.PropertyUtil.setData;
import static com.example.medtek.utils.PropertyUtil.setDataLogin;
import static com.example.medtek.utils.Utils.TAG;
import static com.example.medtek.utils.Utils.getDateTime;
import static com.example.medtek.utils.Utils.getFileExt;
import static com.example.medtek.utils.Utils.getTypeText;
import static com.example.medtek.utils.Utils.isPatient;
import static com.example.medtek.utils.Utils.requestPermissionCompat;
import static com.example.medtek.utils.Utils.setupVoiceJitsi;
import static com.example.medtek.utils.Utils.showToastyError;
import static com.orhanobut.hawk.Hawk.deleteAll;
import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity {

    private int idJanjiForCall = 0;

    public static final String BUNDLE_ALREADY_LOGIN = "bundle_has_login";
    public static final int PERMISSION_STORAGE = 7669;

    private JitsiMeetConferenceOptions options;

    ChipNavigationBar chipNavigationBar;
    FragmentManager fragmentManager;

    private String access, refresh;
    private String TAG;

    LoginController loginController;
    public AppointmentController appointmentController;
    public UserController userController;
    public ConversationController conversationController;
    ChatFragment chatFragment;

    private final ArrayList<ScheduleDoctorModel> scheduleModels = new ArrayList<>();
    private final ArrayList<ChatsModel> chatsModels = new ArrayList<>();

    public static void navigate(Activity activity, boolean clearPrevStack) {
        Intent intent = new Intent(activity, MainActivity.class);
        if (clearPrevStack) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(BUNDLE_ALREADY_LOGIN, true);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getApplicationState() == ApplyStateType.State_Finish_Login) {
            SplashScreen.navigate(MainActivity.this);
        }
        loginController = new LoginController();
        if (searchData(LOGIN_STATUS)  ||searchData(ACCESS_TOKEN)) {
            if ((boolean) getData(LOGIN_STATUS)) {
                if (shouldLogin()) {
                    Log.d(TAG, "Not Login");
                    return;
                }
            } else {
                navigateWelcome();
                finish();
            }
        }
        setContentView(R.layout.activity_main);
        chipNavigationBar = findViewById(R.id.bottomBar);
        loadData(MainActivity.this);
        initBottomNavBar(savedInstanceState);
        init();
    }

    private void init() {
        appointmentController = new AppointmentController();
        userController = new UserController();
        conversationController = new ConversationController();
    }

    private void initBottomNavBar(Bundle savedInstanceState) {
        chatFragment = new ChatFragment();

        if (savedInstanceState==null){
            chipNavigationBar.setItemSelected(R.id.homee,true);
            fragmentManager = getSupportFragmentManager();
            if (isPatient() || !searchData(LOGIN_STATUS) || !((boolean) getData(LOGIN_STATUS)) || !searchData(ACCESS_TOKEN)) {
                HomeFragment homeFragment = new HomeFragment();
                fragmentManager.beginTransaction().replace(R.id.frameFragment,homeFragment).addToBackStack("FragmentHomePasien").commit();
            } else {
                com.example.medtek.ui.dokter.home.HomeFragment homeFragment = new com.example.medtek.ui.dokter.home.HomeFragment();
                fragmentManager.beginTransaction().replace(R.id.frameFragment,homeFragment,"FragmentHomeDokter").addToBackStack("FragmentHomeDokter").commit();
            }

            chipNavigationBar.setOnItemSelectedListener(i -> {

                Fragment fragment = null;
                switch (i){
                    case R.id.chat:
                        if (!searchData(LOGIN_STATUS) || !((boolean) getData(LOGIN_STATUS)) || !searchData(ACCESS_TOKEN)) {
                            navigateWelcome();
                            chipNavigationBar.setItemSelected(R.id.homee, true);
                        } else {
                            fragment = chatFragment;
                            TAG = "FragmentChatDokter";
                        }
                        break;
                    case R.id.homee:
                        if (isPatient() || !searchData(LOGIN_STATUS) || !((boolean) getData(LOGIN_STATUS)) || !searchData(ACCESS_TOKEN)) {
                            fragment = new HomeFragment();
                            TAG = "FragmentHomePasien";
                        } else {
                            fragment = new com.example.medtek.ui.dokter.home.HomeFragment();
                            TAG = "FragmentHomeDokter";
                        }
                        break;
                    case R.id.others:
                        if (!searchData(LOGIN_STATUS) || !((boolean) getData(LOGIN_STATUS)) || !searchData(ACCESS_TOKEN)) {
                            navigateWelcome();
                            chipNavigationBar.setItemSelected(R.id.homee, true);
                        } else {
                            fragment = new OthersFragment();
                            TAG = "FragmentOthersPasien";
                        }
                        break;
                }

                if (searchData(LOGIN_STATUS) || searchData(ACCESS_TOKEN)) {
                    if (fragment != null){
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.frameFragment, fragment).addToBackStack(TAG).commit();
                    }
                }
            });
        }
    }

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");

        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

    private boolean shouldLogin() {
        Log.d(TAG(MainActivity.class), valueOf(getApplicationState()));
        if (!searchData(LOGIN_STATUS) || !((boolean) getData(LOGIN_STATUS)) || !searchData(ACCESS_TOKEN)) {
            navigateWelcome();
            return true;
        } else if (isTokenExpired()) {
            navigateWelcome();
            return true;
        } else if (getApplicationState() != ApplyStateType.State_After_login) {
            if (!searchData(IS_AFTER_RESET)) {
                resetToken();
            }
            setData(IS_AFTER_RESET, true);
            return false;
        } else {
            setApplicationState(ApplyStateType.State_Finish_Login);
            return false;
        }
    }

    private void navigateWelcome() {
        Intent home = new Intent(MainActivity.this, WelcomePageActivity.class);
        startActivity(home);
    }

    public void navigateToSearchActivity() {
        SearchChatsActivity.navigate(this, chatsModels);
    }

    private boolean isTokenExpired() {
        Log.d(TAG(MainActivity.class),"expired: " + (String) getData(EXPIRED_TOKEN));
        DateTime tokenDateExpired = getDateTime((String) getData(EXPIRED_TOKEN));
        if (tokenDateExpired.isAfterNow()) {
            LocalTime currentTime = LocalTime.now();
            if (tokenDateExpired.toLocalDate() == new DateTime().toLocalDate()
                    && currentTime.isAfter(tokenDateExpired.toLocalTime())) {
//                Log.d(TAG, "testTokenExpired");
//                setData(LOGIN_STATUS, false);
                deleteAll();
                return true;
            } else if (tokenDateExpired.toLocalDate() == new DateTime().toLocalDate()
                    && currentTime.isBefore(tokenDateExpired.toLocalTime())) {
                return false;
            } else {
                return false;
            }
        } else {
//            Log.d(TAG, "testTokenExpired");
//            setData(LOGIN_STATUS, false);
            deleteAll();
            return true;
        }
    }

    private void resetToken() {
        loginController.getNewToken(new BaseCallback<AuthTokenResponse>() {
            @Override
            public void onNoConnection() {
                Log.d(TAG(MainActivity.class), "No Connection");
                showToastyError(MainActivity.this, NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(MainActivity.class), "Server Broken");
                showToastyError(MainActivity.this, SERVER_BROKEN);
            }

            @Override
            public void onSuccess(AuthTokenResponse result) {
                Log.d(TAG(MainActivity.class), "Success");
                setDataLogin(result);
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(MainActivity.class), "Error");
                showToastyError(MainActivity.this, ERROR_NULL);
            }
        });
    }

    public void getDataSchedule() {
        if (scheduleModels.size() > 0) {
            scheduleModels.clear();
        }

//        scheduleModels.add(new ScheduleModel(3, "22 Jul 2020", "10:00 - 12:00"));
//        scheduleModels.add(new ScheduleModel(5, "23 Jul 2020", "10:00 - 12:00"));
//        scheduleModels.add(new ScheduleModel(6, "23 Jul 2020", "10:00 - 12:00"));

        appointmentController.getJanjiList(new BaseCallback<GetJanjiListResponse>() {
            @Override
            public void onSuccess(GetJanjiListResponse result) {
                List<AppointmentModel> appointmentModels = new ArrayList<>(result.getData());
                getDataHistoryChats(appointmentModels);
                chatFragment.addDataSchedule(result.getData());
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(MainActivity.class), "Error");
                showToastyError(MainActivity.this, ERROR_NULL);
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(MainActivity.class), "No Connection");
                showToastyError(MainActivity.this, NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(MainActivity.class), "Server Broken");
                showToastyError(MainActivity.this, SERVER_BROKEN);
            }
        });
    }

    public void getDataHistoryChats(List<AppointmentModel> janjiListResponses) {
        if (chatsModels.size() > 0) {
            chatsModels.clear();
        }
        conversationController.getConversationList(new BaseCallback<GetConversationListResponse>() {
            @Override
            public void onSuccess(GetConversationListResponse result) {
                if (result.getData().size() > 0) {
                    int sizeConversationList = result.getData().size();
                    final int[] countGetConversation = {0};
                    for (GetConversationListResponse.ConversationList conversationList : result.getData()) {
                        Log.d(TAG, "conversationList: " + conversationList.getIdConversationList());
                        conversationController.getConversation(conversationList.getIdConversationList(), new BaseCallback<GetConversationResponse>() {
                            @Override
                            public void onSuccess(GetConversationResponse result) {
                                countGetConversation[0]++;
                                ArrayList<ChatsModel.Chat> chats = new ArrayList<>();

                                if (result.getData().getChats().size() > 0) {
                                    int idSender = 0;
                                    String senderAvatar = "";
                                    String senderName = "";

                                    for (AppointmentModel janjiList: janjiListResponses) {
                                        if (janjiList.getIdConversation() == conversationList.getIdConversationList()) {
                                            if (isPatient()) {
                                                idSender = janjiList.getIdDokter();
                                                if (janjiList.getDokter().getImage().size() > 0) {
                                                    for (ImageModel model : janjiList.getDokter().getImage()) {
                                                        if (model.getTypeId() == IMAGE_AVATAR) {
                                                            senderAvatar = model.getPath();
                                                        }
                                                    }
                                                }
                                                senderName = janjiList.getDokter().getName();
                                            } else {
                                                idSender = janjiList.getIdPasien();
                                                if (janjiList.getPasien().getImage().size() > 0) {
                                                    for (ImageModel model : janjiList.getPasien().getImage()) {
                                                        if (model.getTypeId() == IMAGE_AVATAR) {
                                                            senderAvatar = model.getPath();
                                                        }
                                                    }
                                                }
                                                senderName = janjiList.getPasien().getName();
                                            }
                                        }
                                    }
                                    for (GetConversationResponse.Conversation.ChatModel chatModel : result.getData().getChats()) {
                                        chats.add(new ChatsModel.Chat(
                                                chatModel.getIdChat(),
                                                chatModel.getIdSender(),
                                                (chatModel.getIdSender() == ((UserModel) getData(DATA_USER)).getIdUser()) ? idSender : ((UserModel) getData(DATA_USER)).getIdUser(),
                                                chatModel.getCreatedAt(),
                                                chatModel.isRead(),
                                                getTypeText(chatModel.getAttachment(), chatModel.getMessage()),
                                                getMessageText(chatModel.getMessage(), chatModel.getAttachment())
                                        ));
                                    }

                                    ChatsModel chatsModel = new ChatsModel(conversationList.getIdConversationList(), chats);
                                    chatsModel.setIdSender(idSender);
                                    chatsModel.setSenderName(senderName);
                                    chatsModel.setSenderAvatar(senderAvatar);
                                    chatsModel.setFinishedAt(conversationList.getUpdatedAt());

                                    chatsModels.add(chatsModel);
                                }
                                Log.d(TAG(MainActivity.class), "finalI: " + countGetConversation[0]);
                                Log.d(TAG(MainActivity.class), "sizeConversationList: " + sizeConversationList);
                                if (countGetConversation[0] == sizeConversationList) {
                                    chatFragment.addDataHistoryChats(chatsModels);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.d(TAG(MainActivity.class), t.getMessage());
                                showToastyError(MainActivity.this, ERROR_NULL);
                            }


                            @Override
                            public void onNoConnection() {
                                Log.d(TAG(MainActivity.class), "No Connection");
                                showToastyError(MainActivity.this, NO_CONNECTION);
                            }

                            @Override
                            public void onServerBroken() {
                                Log.d(TAG(MainActivity.class), "Server Broken");
                                showToastyError(MainActivity.this, SERVER_BROKEN);
                            }
                        });
                    }
                } else {
                    chatFragment.addDataHistoryChats(chatsModels);
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(MainActivity.class), t.getMessage());
                showToastyError(MainActivity.this, ERROR_NULL);
            }


            @Override
            public void onNoConnection() {
                Log.d(TAG(MainActivity.class), "No Connection");
                showToastyError(MainActivity.this, NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(MainActivity.class), "Server Broken");
                showToastyError(MainActivity.this, SERVER_BROKEN);
            }
        });

//        Cursor cursorConversation = medtekHelper.queryAll(DatabaseContract.ConversationHistoryColumns.TABLE_NAME,
//                DatabaseContract.ConversationHistoryColumns._ID);
//        chatsModels = MappingHelper.mapCursorToListConversation(cursorConversation);
//        if (chatsModels.size() > 0) {
//            for (ChatsModel model : chatsModels) {
//                Cursor chat = medtekHelper.queryByIdConv(valueOf(model.getIdConversation()));
//                model.getChats().addAll(MappingHelper.mapCursorToListChat(chat));
//            }
//        }

//        List<ChatsModel.Chat> chats = new ArrayList<>();
//        chats.add(new ChatsModel.Chat(1, 3, 4, "16:30", true, ChatType.TEXT, "Halo, saya Caleigh Kirlin, ada yg bisa saya bantu?"));
//        chats.add(new ChatsModel.Chat(2, 4, 3, "16:31", false, ChatType.TEXT, "Halo Dok, saya ada keluhan jantung selalu berdegup kencang"));
//        chats.add(new ChatsModel.Chat(3, 3, 4, "16:32", false, ChatType.TEXT, "Setiap kapan yah? dan sejak kapan itu terjadi? Apakah anda juga merasa sakit pada bagian dada?"));
//        chatsModels.add(new ChatsModel(1, chats));

    }

    private String getMessageText(String message, String attachment) {
        if (attachment == null) {
            return message;
        } else {
            return attachment;
        }
    }

    public void getDoctorProfileSchedule(ArrayList<AppointmentModel> models) {
        for (int i = 0; i < models.size(); i++) {
            int position = i;
            userController.getDokter(valueOf(models.get(position).getIdDokter()), new BaseCallback<GetInfoUserResponse>() {
                @Override
                public void onSuccess(GetInfoUserResponse result) {
                    for (UserModel.Jadwal jadwal : result.getData().getJadwal()) {
                        if (jadwal.getIdDay() == models.get(position).getIdDay()) {
                            models.get(position).setJadwal(jadwal);
                        }
                    }
                    chatFragment.setupDataRVSchedulePatient(models);
                }

                @Override
                public void onError(Throwable t) {
                    Log.d(TAG(MainActivity.class), t.getMessage());
                    showToastyError(MainActivity.this, ERROR_NULL);
                }

                @Override
                public void onNoConnection() {
                    Log.d(TAG(MainActivity.class), "No Connection");
                    showToastyError(MainActivity.this, NO_CONNECTION);
                }

                @Override
                public void onServerBroken() {
                    Log.d(TAG(MainActivity.class), "Server Broken");
                    showToastyError(MainActivity.this, SERVER_BROKEN);
                }
            });
        }
    }

    public void getPatientProfileSchedule(ArrayList<AppointmentModel> models) {
        for (int i = 0; i < models.size(); i++) {
            int position = i;
            Log.d(TAG(MainActivity.class), "idPasienSchedule: " + models.get(position).getIdPasien());
            userController.getPasien(valueOf(models.get(position).getIdPasien()), new BaseCallback<GetInfoUserResponse>() {
                @Override
                public void onSuccess(GetInfoUserResponse result) {
                    if (result.getData().getImage().size() > 0) {
                        for (ImageModel model : result.getData().getImage()) {
                            if (model.getTypeId() == IMAGE_AVATAR) {
                                models.get(position).setImageModel(model);
                            }
                        }
                    }
                    chatFragment.setupDataRVScheduleDoctor(models);
                }

                @Override
                public void onError(Throwable t) {
                    Log.d(TAG(MainActivity.class), t.getMessage());
                    showToastyError(MainActivity.this, ERROR_NULL);
                }

                @Override
                public void onNoConnection() {
                    Log.d(TAG(MainActivity.class), "No Connection");
                    showToastyError(MainActivity.this, NO_CONNECTION);
                }

                @Override
                public void onServerBroken() {
                    Log.d(TAG(MainActivity.class), "Server Broken");
                    showToastyError(MainActivity.this, SERVER_BROKEN);
                }
            });
        }
    }

    public void getDoctorProfileChats(ArrayList<ChatsModel> models) {
        for (int i = 0; i < models.size(); i++) {
            int position = i;
            int idSender = 0;
            for (ChatsModel.Chat chat : models.get(position).getChats()) {
                if (chat.getIdSender() != ((UserModel) getData(DATA_USER)).getIdUser()) {
                    idSender = chat.getIdSender();
                }
            }
            userController.getDokter(valueOf(idSender), new BaseCallback<GetInfoUserResponse>() {
                @Override
                public void onSuccess(GetInfoUserResponse result) {
                    if (result.getData().getImage().size() > 0) {
                        for (ImageModel model : result.getData().getImage()) {
                            if (model.getTypeId() == IMAGE_AVATAR) {
                                models.get(position).setSenderAvatar(model.getPath());
                            }
                        }
                    } else {
                        models.get(position).setSenderAvatar("");
                    }
                    models.get(position).setSenderName(result.getData().getName());
                    models.get(position).setIdSender(result.getData().getIdUser());
                    chatFragment.setupDataRVChats(models);
                }

                @Override
                public void onError(Throwable t) {
                    Log.d(TAG(MainActivity.class), t.getMessage());
                    showToastyError(MainActivity.this, ERROR_NULL);
                }

                @Override
                public void onNoConnection() {
                    Log.d(TAG(MainActivity.class), "No Connection");
                    showToastyError(MainActivity.this, NO_CONNECTION);
                }

                @Override
                public void onServerBroken() {
                    Log.d(TAG(MainActivity.class), "Server Broken");
                    showToastyError(MainActivity.this, SERVER_BROKEN);
                }
            });
        }
    }

    public void getPatientProfileChats(ArrayList<ChatsModel> models) {
        for (int i = 0; i < models.size(); i++) {
            int position = i;
            int idSender = 0;
            for (ChatsModel.Chat chat : models.get(position).getChats()) {
                if (chat.getIdSender() != ((UserModel) getData(DATA_USER)).getIdUser()) {
                    idSender = chat.getIdSender();
                }
            }

            Log.d(TAG(MainActivity.class), "idPasienChat: " + idSender);
            userController.getPasien(valueOf(idSender), new BaseCallback<GetInfoUserResponse>() {
                @Override
                public void onSuccess(GetInfoUserResponse result) {
                    if (result.getData().getImage().size() > 0) {
                        for (ImageModel model : result.getData().getImage()) {
                            if (model.getTypeId() == IMAGE_AVATAR) {
                                models.get(position).setSenderAvatar(model.getPath());
                            }
                        }
                    } else {
                        models.get(position).setSenderAvatar("");
                    }
                    models.get(position).setSenderName(result.getData().getName());
                    models.get(position).setIdSender(result.getData().getIdUser());
                    chatFragment.setupDataRVChats(models);
                }

                @Override
                public void onError(Throwable t) {
                    Log.d(TAG(MainActivity.class), t.getMessage());
                    showToastyError(MainActivity.this, ERROR_NULL);
                }

                @Override
                public void onNoConnection() {
                    Log.d(TAG(MainActivity.class), "No Connection");
                    showToastyError(MainActivity.this, NO_CONNECTION);
                }

                @Override
                public void onServerBroken() {
                    Log.d(TAG(MainActivity.class), "Server Broken");
                    showToastyError(MainActivity.this, SERVER_BROKEN);
                }
            });
        }
    }

    public void deleteAllDB() {
//        medtekHelper.deleteAll(DatabaseContract.ConversationHistoryColumns.TABLE_NAME);
//        medtekHelper.deleteAll(DatabaseContract.ChatsHistoryColumns.TABLE_NAME);
//        medtekHelper.deleteAll(DatabaseContract.MediaChatColumns.TABLE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ChatRoomActivity.REQ_ACTIVE_CHAT: {
                    boolean isEndChat = false;
                    if (data != null) {
                        isEndChat = data.getBooleanExtra(ChatRoomActivity.IS_END_CHATS, false);
                    }
                    if (!isEndChat) {
                        chatFragment.setupDataRVActiveChats();
                    } else {
                        chatFragment.doStart();
                    }
                    break;
                }
                case ChatRoomActivity.REQ_VOICE_CALL:
//                    if (data != null) {
//                        idJanjiForCall = data.getIntExtra(ChatRoomActivity.BUNDLE_ID_JANJI, 0);
//                    }
//                    options = setupVoiceJitsi(idJanjiForCall);
//                    registerForBroadcastMessages();
//                    listenResponse();
//                    JitsiMeetActivity.launch(this, options);
//                    break;
            }

        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 1) {
            super.onBackPressed();
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }
}
