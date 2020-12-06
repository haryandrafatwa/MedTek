package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.medtek.R;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.controller.AppointmentController;
import com.example.medtek.controller.LoginController;
import com.example.medtek.controller.UserController;
import com.example.medtek.model.AppointmentModel;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.model.ImageModel;
import com.example.medtek.model.ScheduleDoctorModel;
import com.example.medtek.model.UserModel;
import com.example.medtek.model.state.ApplyStateType;
import com.example.medtek.network.response.AuthTokenResponse;
import com.example.medtek.network.response.GetInfoUserResponse;
import com.example.medtek.network.response.GetJanjiListResponse;
import com.example.medtek.ui.fragment.ChatFragment;
import com.example.medtek.ui.pasien.home.HomeFragment;
import com.example.medtek.ui.pasien.others.OthersFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;

import static com.example.medtek.constant.APPConstant.ERROR_NULL;
import static com.example.medtek.constant.APPConstant.IMAGE_AVATAR;
import static com.example.medtek.constant.APPConstant.NO_CONNECTION;
import static com.example.medtek.constant.APPConstant.SERVER_BROKEN;
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
import static com.example.medtek.utils.Utils.isPatient;
import static com.example.medtek.utils.Utils.showToastyError;
import static com.orhanobut.hawk.Hawk.deleteAll;
import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity {

    public static final String BUNDLE_ALREADY_LOGIN = "bundle_has_login";

    ChipNavigationBar chipNavigationBar;
    FragmentManager fragmentManager;

    private String access, refresh;
    private String TAG;

    LoginController loginController;
    public AppointmentController appointmentController;
    public UserController userController;
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
        loginController = new LoginController();
        if (shouldLogin()) {
            Log.d(TAG, "Not Login");
            return;
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
    }

    private void initBottomNavBar(Bundle savedInstanceState) {
        chatFragment = new ChatFragment();

        if (isPatient()) {
            if (savedInstanceState==null){
                chipNavigationBar.setItemSelected(R.id.homee,true);
                fragmentManager = getSupportFragmentManager();
                HomeFragment homeFragment = new HomeFragment();
                fragmentManager.beginTransaction().replace(R.id.frameFragment,homeFragment).addToBackStack("FragmentHomePasien").commit();
            }

            chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
                @Override
                public void onItemSelected(int i) {

                    Fragment fragment = null;
                    switch (i){
                        case R.id.chat:
                            fragment = chatFragment;
                            TAG = "FragmentChatPasien";
                            break;
                        case R.id.homee:
                            fragment = new HomeFragment();
                            TAG = "FragmentHomePasien";
                            break;
                        case R.id.others:
                            fragment = new OthersFragment();
                            TAG = "FragmentOthersPasien";
                            break;
                    }

                    if (fragment!= null){
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.frameFragment,fragment).addToBackStack(TAG).commit();
                    }

                }
            });
        } else {
            if (savedInstanceState==null){
                chipNavigationBar.setItemSelected(R.id.homee,true);
                fragmentManager = getSupportFragmentManager();
                com.example.medtek.ui.dokter.home.HomeFragment homeFragment = new com.example.medtek.ui.dokter.home.HomeFragment();
                fragmentManager.beginTransaction().replace(R.id.frameFragment,homeFragment,"FragmentHomeDokter").addToBackStack("FragmentHomeDokter").commit();
            }

            chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
                @Override
                public void onItemSelected(int i) {

                    Fragment fragment = null;
                    switch (i){
                        case R.id.chat:
                            fragment = chatFragment;
                            TAG = "FragmentChatDokter";
                            break;
                        case R.id.homee:
                            fragment = new com.example.medtek.ui.dokter.home.HomeFragment();
                            TAG = "FragmentHomeDokter";
                            break;
                        case R.id.others:
                            fragment = new OthersFragment();
                            TAG = "FragmentOthersDokter";
                            break;
                    }

                    if (fragment!= null){
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.frameFragment,fragment,TAG).addToBackStack(TAG).commit();
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
        SplashScreen.navigate(MainActivity.this);
    }

    public void navigateToSearchActivity() {
        SearchChatsActivity.navigate(this, chatsModels);
    }

    private boolean isTokenExpired() {
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
                if (result.getData().size() > 0) {
                    chatFragment.addDataSchedule(result.getData());
                }
//                getDataHistoryChats(result.getData());
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

    public void getDataHistoryChats() {
        if (chatsModels.size() > 0) {
            chatsModels.clear();
        }
//        if (janjiListResponses.size() > 0) {
//            for (AppointmentModel model : janjiListResponses) {
//                for (AppointmentModel.Transaksi transaksi : model.getTransaksi()) {
//                    if (transaksi.getIdType() == APPConstant.PAYMENT_JANJI && transaksi.isPaid() && model.getIdStatus() > 3) {
//                        int idSender = isPatient() ? model.getIdDokter() : model.getIdPasien();
//
//                        String imagePath = null;
//                        if (isPatient()) {
//                            for (ImageModel modelImage : model.getDokter().getImage()) {
//                                if (modelImage.getTypeId() == IMAGE_AVATAR) {
//                                    imagePath = modelImage.getPath();
//                                }
//                            }
//                        } else {
//                            for (ImageModel modelImage : model.getPasien().getImage()) {
//                                if (modelImage.getTypeId() == IMAGE_AVATAR) {
//                                    imagePath = modelImage.getPath();
//                                }
//                            }
//                        }
//
//                        chatsModels.add(new ChatsModel(
//                                model.getIdConversation(),
//                                model.getIdJanji(),
//                                isPatient() ? model.getDokter().getName() : model.getPasien().getName(),
//                                idSender,
//                                model.getUpdatedAt(),
//                                imagePath));
//                        break;
//                    }
//                }
//            }
//            for (ChatsModel model: chatsModels) {
//                conversationController.getConversation(model.getIdConversation(), new BaseCallback<GetConversationResponse>() {
//                    @Override
//                    public void onSuccess(GetConversationResponse result) {
//                        for (GetConversationResponse.Conversation.ChatModel chatModel : result.getData().getChats()) {
//                            model.getChats().add(new ChatsModel.Chat(
//                                    chatModel.getIdChat(),
//                                    chatModel.getIdSender(),
//                                    (chatModel.getIdSender() == model.getIdSender()) ? ((UserModel) getData(DATA_USER)).getIdUser() : model.getIdSender(),
//
//                            ))
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//
//                    }
//
//                    @Override
//                    public void onNoConnection() {
//
//                    }
//
//                    @Override
//                    public void onServerBroken() {
//
//                    }
//                });
//            }
//
//        }
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

        chatFragment.addDataHistoryChats(chatsModels);
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
    }

    public void getPatientProfileSchedule(ArrayList<AppointmentModel> models) {
        for (int i = 0; i < models.size(); i++) {
            int position = i;
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
            if (requestCode == ChatRoomActivity.REQ_ACTIVE_CHAT) {
                boolean isEndChat = false;
                if (data != null) {
                    isEndChat = data.getBooleanExtra(ChatRoomActivity.IS_END_CHATS, false);
                }
                if (!isEndChat) {
                    chatFragment.setupDataRVActiveChats();
                } else {
                    chatFragment.doStart();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (!App.isActivityVisible()) {
//            Log.d(TAG, "checkDestroyDB");
//            medtekHelper.close();
//        }
    }
}
