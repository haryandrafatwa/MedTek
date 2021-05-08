package com.example.medtek.ui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.constant.APPConstant;
import com.example.medtek.databinding.FragmentChatBinding;
import com.example.medtek.model.AppointmentModel;
import com.example.medtek.model.CallModel;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.model.ImageModel;
import com.example.medtek.model.MessageModel;
import com.example.medtek.model.ScheduleDoctorModel;
import com.example.medtek.model.UserModel;
import com.example.medtek.model.state.ChatType;
import com.example.medtek.network.base.BaseResponse;
import com.example.medtek.network.response.GetInfoUserResponse;
import com.example.medtek.network.socket.SocketUtil;
import com.example.medtek.ui.activity.ChatRoomActivity;
import com.example.medtek.ui.activity.IncomingVideoChatActivity;
import com.example.medtek.ui.activity.IncomingVoiceChatActivity;
import com.example.medtek.ui.activity.MainActivity;
import com.example.medtek.ui.adapter.ChatsListAdapter;
import com.example.medtek.ui.adapter.ScheduleListDoctorAdapter;
import com.example.medtek.ui.adapter.ScheduleListPatientAdapter;
import com.example.medtek.ui.dialog.bottomsheetdialog.BSDScheduleDoctorDetail;
import com.example.medtek.ui.dialog.bottomsheetdialog.BSDSchedulePatientDetail;
import com.example.medtek.ui.helper.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.example.medtek.App.ID_CHANNEL_MESSAGE;
import static com.example.medtek.constant.APPConstant.ERROR_NULL;
import static com.example.medtek.constant.APPConstant.IMAGE_AVATAR;
import static com.example.medtek.constant.APPConstant.MESSAGE_REQUEST_VIDEO_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_REQUEST_VOICE_CALL;
import static com.example.medtek.constant.APPConstant.NO_CONNECTION;
import static com.example.medtek.constant.APPConstant.SERVER_BROKEN;
import static com.example.medtek.utils.PropertyUtil.ACTIVE_CHAT;
import static com.example.medtek.utils.PropertyUtil.DATA_USER;
import static com.example.medtek.utils.PropertyUtil.USER_TYPE;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.PropertyUtil.searchData;
import static com.example.medtek.utils.PropertyUtil.setData;
import static com.example.medtek.utils.RecyclerViewUtil.recyclerLinear;
import static com.example.medtek.utils.Utils.TAG;
import static com.example.medtek.utils.Utils.dateTimeToStringHour;
import static com.example.medtek.utils.Utils.getDate;
import static com.example.medtek.utils.Utils.getDateTime;
import static com.example.medtek.utils.Utils.getTypeText;
import static com.example.medtek.utils.Utils.isPatient;
import static com.example.medtek.utils.Utils.setupVoiceJitsi;
import static com.example.medtek.utils.Utils.showToastyError;
import static com.example.medtek.utils.WidgetUtil.showNotification;
import static java.lang.String.valueOf;

public class ChatFragment extends BaseFragment {
    private static final String TAG = ChatFragment.class.getSimpleName();

    private FragmentChatBinding binding;

    private ScheduleListPatientAdapter scheduleListPatientAdapter;
    private ScheduleListDoctorAdapter scheduleListDoctorAdapter;
    private ChatsListAdapter chatsHistoryAdapter, chatsActiveAdapter;

    private ArrayList<AppointmentModel> appointmentModels = new ArrayList<>();
    private final ArrayList<ScheduleDoctorModel> scheduleDoctorModels = new ArrayList<>();
    private ArrayList<ChatsModel> chatsModels = new ArrayList<>();

    private int sizeOfAppointment;
    private int sizeOfChats;
    private int sizeOfAppointmentNow;
    private int sizeOfChatsNow;
    private boolean isAppointmentDone;
    private boolean isChatsDone;

    @Override
    protected View setContentView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
        // FOR TEMP
        if (isPatient()) {
            ArrayList<ChatsModel.Chat> chats = new ArrayList<>();
            ChatsModel newActiveChatsModel = new ChatsModel(468, chats);
            newActiveChatsModel.setIdSender(92);
            newActiveChatsModel.setSenderName("gabs");
            newActiveChatsModel.setSenderAvatar("/storage/Dokter.png");
            newActiveChatsModel.setIdJanji(466);
            setData(ACTIVE_CHAT, newActiveChatsModel);
        } else {
            ArrayList<ChatsModel.Chat> chats = new ArrayList<>();
            ChatsModel newActiveChatsModel = new ChatsModel(468, chats);
            newActiveChatsModel.setIdSender(91);
            newActiveChatsModel.setSenderName("Gabriel");
            newActiveChatsModel.setSenderAvatar("/storage/avatar/91/1618050914.jpeg");
            newActiveChatsModel.setIdJanji(466);
            setData(ACTIVE_CHAT, newActiveChatsModel);
        }

    }

    public void initState() {
        sizeOfAppointment = 0;
        sizeOfChats = 0;
        sizeOfAppointmentNow = 0;
        sizeOfChatsNow = 0;
        // FOR TEMP
        isAppointmentDone = true /* false */;
        isChatsDone = true /* false */;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void setupView() {
        scheduleListPatientAdapter = new ScheduleListPatientAdapter(getContext());
        scheduleListDoctorAdapter = new ScheduleListDoctorAdapter(getContext());
        chatsHistoryAdapter = new ChatsListAdapter(getContext(), false);
        chatsActiveAdapter = new ChatsListAdapter(getContext(), true);
        doStart();
        setMakeSchedule();
        binding.rlSearch.setOnClickListener(v -> {
            ((MainActivity) getActivity()).navigateToSearchActivity();
        });
        binding.swipeRefresh.setOnRefreshListener(this::doStart);
    }

    public void setupDataRVChats(ArrayList<ChatsModel> chatsModels) {
//        sizeOfChatsNow++;
//        if (sizeOfChatsNow >= sizeOfChats) {
//
//        }
        Log.d(TAG, "setupDataRVChats");
        this.chatsModels = chatsModels;
        if (binding != null) {
            App.getInstance().runOnUiThread(() -> {
                if (searchData(ACTIVE_CHAT)) {
//                    FOR TEMP
//                    ChatsModel activeChatsModel = (ChatsModel) getData(ACTIVE_CHAT);
//                    for (ChatsModel model: chatsModels) {
//                        if (model.getIdConversation() == activeChatsModel.getIdConversation()) {
//                            chatsModels.remove(model);
//                            break;
//                        }
//                    }
                }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        chatsModels.sort((o1, o2) -> getDateTime(o2.getFinishedAt(), DateTimeZone.UTC).compareTo(getDateTime(o1.getFinishedAt(), DateTimeZone.UTC)));
                    } else {
                        Collections.sort(chatsModels, (o1, o2) -> getDateTime(o2.getFinishedAt(), DateTimeZone.UTC).compareTo(getDateTime(o1.getFinishedAt(), DateTimeZone.UTC)));
                    }

                chatsHistoryAdapter.setChatsList(chatsModels);
                if (chatsModels.size() > 0) {
                    recyclerLinear(binding.rvChatHistory, LinearLayoutManager.VERTICAL, chatsHistoryAdapter);
                    chatsHistoryAdapter.setOnItemClickCallback((model, position) -> {
                        navigateToChatRoom(model);
                    });
                    isNoChats(false);
                } else {
                    isNoChats(true);
                }
                isChatsDone = true;
                isLoading();
            });
        }
    }

    public void setupDataRVActiveChats() {
        ChatsModel activeChatsModel = (ChatsModel) getData(ACTIVE_CHAT);
        ArrayList<ChatsModel> chatsModels = new ArrayList<>();
        if (activeChatsModel != null) {
            chatsModels.add(activeChatsModel);
        }
        if (binding != null) {
            Log.d(TAG, "chatsModels.size(): " + chatsModels.size());
            Log.d(TAG, "chatsModels.size(): " + chatsModels.get(0).getSenderName());
            if (chatsModels.size() > 0) {
                if (binding.llActiveChat.getVisibility() != View.VISIBLE) {
                    binding.llActiveChat.setVisibility(View.VISIBLE);
                }
                chatsActiveAdapter.setChatsList(chatsModels);
                recyclerLinear(binding.rvActiveChats, LinearLayoutManager.VERTICAL, chatsActiveAdapter);
                chatsActiveAdapter.setOnItemClickCallback((model, position) -> {
//                    SocketUtil.getInstance().listenForEventChat(model.getIdConversation(), model.getIdJanji());
                    navigateToChatRoomWithResult(model, ChatRoomActivity.REQ_ACTIVE_CHAT);
                });
            }
        }
    }

    public void setupDataRVSchedulePatient(ArrayList<AppointmentModel> appointmentModels) {
        sizeOfAppointmentNow++;
        Log.d(TAG, "sizeOfAppointment: " + sizeOfAppointment);
        Log.d(TAG, "sizeOfAppointmentNow: " + sizeOfAppointmentNow);
        if (sizeOfAppointmentNow >= sizeOfAppointment) {
            this.appointmentModels = appointmentModels;
            if (binding != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    appointmentModels.sort((o1, o2) -> getDate(o1.getTglJanji()).compareTo(getDate(o2.getTglJanji())));
                } else {
                    Collections.sort(appointmentModels, (o1, o2) -> getDate(o1.getTglJanji()).compareTo(getDate(o2.getTglJanji())));
                }
                scheduleListPatientAdapter.setSchedules(appointmentModels);
                recyclerLinear(binding.rvSchedule, LinearLayoutManager.HORIZONTAL, scheduleListPatientAdapter);
                scheduleListPatientAdapter.setOnItemClickCallback((model, position) -> {
                    BSDSchedulePatientDetail fragment = new BSDSchedulePatientDetail(model);
                    fragment.show(getChildFragmentManager(), fragment.getTag());
                });
                isNoSchedule(false, (int) getData(USER_TYPE));
                isAppointmentDone = true;
                isLoading();
            }
        }
    }

    public void setupDataRVScheduleDoctor(ArrayList<AppointmentModel> appointmentModels) {
        sizeOfAppointmentNow++;
        if (sizeOfAppointmentNow >= sizeOfAppointment) {
            if (scheduleDoctorModels.size() > 0) {
                scheduleDoctorModels.clear();
            }
            for (AppointmentModel model : appointmentModels) {
                for (AppointmentModel.Transaksi transaksi : model.getTransaksi()) {
                    if (transaksi.getIdType() == APPConstant.PAYMENT_JANJI && transaksi.isPaid() && model.getIdStatus() <= 3) {
                        if (scheduleDoctorModels.size() == 0) {
                            scheduleDoctorModels.add(new ScheduleDoctorModel(model.getTglJanji()));
                            scheduleDoctorModels.get(0).getAppointmentModelList().add(model);
                        } else {
                            boolean isDateAvailable = false;
                            for (ScheduleDoctorModel scheduleDoctorModel : scheduleDoctorModels) {
                                if (scheduleDoctorModel.getTglJanji().equals(model.getTglJanji())) {
                                    isDateAvailable = true;
                                    scheduleDoctorModel.getAppointmentModelList().add(model);
                                    break;
                                }
                            }
                            if (!isDateAvailable) {
                                scheduleDoctorModels.add(new ScheduleDoctorModel(model.getTglJanji()));
                                scheduleDoctorModels.get(scheduleDoctorModels.size() - 1).getAppointmentModelList().add(model);
                            }
                        }
                        break;
                    }
                }
            }
            Log.d(TAG, "scheduleDoctorModels.size() " + scheduleDoctorModels.size());
            if (binding != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    scheduleDoctorModels.sort((o1, o2) -> getDate(o1.getTglJanji()).compareTo(getDate(o2.getTglJanji())));
                } else {
                    Collections.sort(scheduleDoctorModels, (o1, o2) -> getDate(o1.getTglJanji()).compareTo(getDate(o2.getTglJanji())));
                }
                for (ScheduleDoctorModel model : scheduleDoctorModels) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        model.getAppointmentModelList().sort(Comparator.comparing(AppointmentModel::getIdConversation));
                    } else {
                        Collections.sort(model.getAppointmentModelList(), (o1, o2) -> valueOf(o1.getIdConversation()).compareTo(valueOf(o2.getIdConversation())));
                    }
                }
                scheduleListDoctorAdapter.setSchedules(scheduleDoctorModels);
                recyclerLinear(binding.rvSchedule, LinearLayoutManager.HORIZONTAL, scheduleListDoctorAdapter);
                scheduleListDoctorAdapter.setOnItemClickCallback((model, position) -> {
                    BSDScheduleDoctorDetail fragment = new BSDScheduleDoctorDetail(model);
                    fragment.show(getChildFragmentManager(), fragment.getTag());
                });
                isNoSchedule(false, (int) getData(USER_TYPE));
                isAppointmentDone = true;
                isLoading();
            }
        }
    }

    public void addDataSchedule(List<AppointmentModel> janjiListResponses) {
        if (janjiListResponses.size() > 0) {
            for (Iterator<AppointmentModel> iterator = janjiListResponses.iterator(); iterator.hasNext(); ) {
                AppointmentModel value = iterator.next();
                Log.d(TAG, "value.getTglJanji(): " + value.getTglJanji());
                if (getDate(value.getTglJanji()).isBeforeNow()) {
                    if (!getDate(value.getTglJanji()).toLocalDate().isEqual(LocalDate.now())) {
                        iterator.remove();
                    }
                }
            }
            if (janjiListResponses.size() > 0) {
                boolean isNoEmpty = false;
                for (AppointmentModel model : janjiListResponses) {
                    for (AppointmentModel.Transaksi transaksi : model.getTransaksi()) {
                        if (transaksi.getIdType() == APPConstant.PAYMENT_JANJI && transaksi.isPaid()) {
                            if (model.getIdStatus() <= 3) {
                                if (isPatient()) {
                                    isNoEmpty = true;
                                    break;
                                } else {
                                    if (model.getIdStatus() >  1) {
                                        isNoEmpty = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (isNoEmpty) {
                    ArrayList<AppointmentModel> appointmentModels = new ArrayList<>();
                    for (AppointmentModel model : janjiListResponses) {
                        for (AppointmentModel.Transaksi transaksi : model.getTransaksi()) {
                            if (transaksi.getIdType() == APPConstant.PAYMENT_JANJI && transaksi.isPaid() && model.getIdStatus() <= 3) {
                                if (isPatient()) {
                                    appointmentModels.add(model);
                                    if (getDate(model.getTglJanji()).toLocalDate().isEqual(LocalDate.now())) {
                                        Log.d(TAG, "testSocket");
                                        SocketUtil.getInstance().listenForEventChat(model.getIdConversation(), model.getIdJanji());
                                        SocketUtil.getInstance().setChannelVideoChat(model.getIdJanji());
                                    }
                                } else {
                                    if (model.getIdStatus() > 1) {
                                        appointmentModels.add(model);
                                        if (model.getIdStatus() == 3) {
                                            SocketUtil.getInstance().listenForEventChat(model.getIdConversation(), model.getIdJanji());
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    sizeOfAppointment = appointmentModels.size();
                    Log.d(TAG, "sizeOfAppointment: " + sizeOfAppointment);
                    if (isPatient()) {
                        getDoctorProfileSchedule(appointmentModels);
                    } else {
                        getPatientProfileSchedule(appointmentModels);
                    }
                } else {
                    isNoSchedule(true, (int) getData(USER_TYPE));
                    isAppointmentDone = true;
                    isLoading();
                }
            } else {
                isNoSchedule(true, (int) getData(USER_TYPE));
                isAppointmentDone = true;
                isLoading();
            }
        } else {
            isNoSchedule(true, (int) getData(USER_TYPE));
            isAppointmentDone = true;
            isLoading();
        }
    }

    public void addDataHistoryChats(ArrayList<ChatsModel> chatsModels) {
        sizeOfChats = chatsModels.size();
        Log.d(TAG, "sizeOfChats: " + sizeOfChats);
        if (sizeOfChats > 0) {
            Log.d(TAG, "sizeOfChats: " + sizeOfChats);
            setupDataRVChats(chatsModels);
        } else {
            isNoChats(true);
            isChatsDone = true;
            isLoading();
        }
    }

    private void getDataSchedule() {
        ((MainActivity) getActivity()).getDataSchedule();
    }

    private void getDoctorProfileSchedule(ArrayList<AppointmentModel> model) {
        ((MainActivity) getActivity()).getDoctorProfileSchedule(model);
    }

    private void getPatientProfileSchedule(ArrayList<AppointmentModel> model) {
        ((MainActivity) getActivity()).getPatientProfileSchedule(model);
    }

    public void navigateToChatRoom(ChatsModel model) {
        ChatRoomActivity.navigate(getActivity(), model);
    }

    public void navigateToChatRoomWithResult(ChatsModel model, int reqCode) {
        ChatRoomActivity.navigate(getActivity(), model, reqCode);
    }

    public void startChat(int idJanji) {
        ((MainActivity) getActivity()).appointmentController.getJanjiStart(valueOf(idJanji), new BaseCallback<BaseResponse>() {
            @Override
            public void onSuccess(BaseResponse result) {
                Log.d(TAG, "Chat Start");
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(ChatFragment.class), "Error");
                showToastyError(getActivity(), ERROR_NULL);
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(ChatFragment.class), "No Connection");
                showToastyError(getActivity(), NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(ChatFragment.class), "Server Broken");
                showToastyError(getActivity(), SERVER_BROKEN);
            }
        });
    }

    @Override
    protected void destroyView() {
        binding = null;
    }

    public void isNoSchedule(boolean status, int type) {
        if (binding != null) {
            if (status) {
                binding.rvSchedule.setVisibility(View.GONE);
                binding.llNoSchedule.setVisibility(View.VISIBLE);
            } else {
                binding.rvSchedule.setVisibility(View.VISIBLE);
                binding.llNoSchedule.setVisibility(View.GONE);
            }
        }
    }

    private void isNoChats(boolean status) {
        if (binding != null) {
            if (status) {
                binding.rvChatHistory.setVisibility(View.GONE);
                binding.llNoChatsHistory.setVisibility(View.VISIBLE);
            } else {
                binding.rvChatHistory.setVisibility(View.VISIBLE);
                binding.llNoChatsHistory.setVisibility(View.GONE);
            }
        }
    }

    private void setMakeSchedule() {
        try {
            Spannable clickhere = new SpannableString(" " + getString(R.string.klik_disini));
            ClickableSpan cs = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {

                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(false);
                }
            };
            clickhere.setSpan(cs, 1, clickhere.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int color = 0;
            if (getContext() != null) {
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            } else {
                color = Color.RED;
            }
            clickhere.setSpan(
                    new ForegroundColorSpan(color),
                    0,
                    clickhere.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            clickhere.setSpan(boldSpan, 1, clickhere.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvNoScheduleDesc.append(clickhere);
            binding.tvNoScheduleDesc.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void isLoading() {
        if (binding != null) {
            App.getInstance().runOnUiThread(() -> {
                if (binding != null) {
                    if (isAppointmentDone && isChatsDone) {
                        binding.sflConsulSchedule.setVisibility(View.GONE);
                        binding.sflChatHistory.setVisibility(View.GONE);
                        binding.llConsulSchedule.setVisibility(View.VISIBLE);
                        binding.llRecentChat.setVisibility(View.VISIBLE);
                        binding.sflSearchChats.setVisibility(View.GONE);
                        binding.optSearchChats.setVisibility(View.VISIBLE);
                        if (searchData(ACTIVE_CHAT)) {
                            binding.llActiveChat.setVisibility(View.VISIBLE);
                        }
                    } else {
                        binding.sflConsulSchedule.setVisibility(View.VISIBLE);
                        binding.sflChatHistory.setVisibility(View.VISIBLE);
                        binding.llConsulSchedule.setVisibility(View.GONE);
                        binding.llActiveChat.setVisibility(View.GONE);
                        binding.llRecentChat.setVisibility(View.GONE);
                        binding.sflSearchChats.setVisibility(View.VISIBLE);
                        binding.optSearchChats.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public void removeActiveChatLayout() {
        binding.llActiveChat.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Log.d(TAG, "isRegister");
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "isUnregister");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatEvent(Object newEvent) {
        ChatsModel activeChatsModel = ((ChatsModel) getData(ACTIVE_CHAT));
        if (newEvent instanceof String) {
            String newMessage = (String) newEvent;
            if (newMessage.equals(MESSAGE_REQUEST_VIDEO_CALL)) {
                IncomingVideoChatActivity.navigate(getActivity(), activeChatsModel, ChatRoomActivity.REQ_VIDEO_CALL);
            } else if (newMessage.equals(MESSAGE_REQUEST_VOICE_CALL)) {
                IncomingVoiceChatActivity.navigate(getActivity(), activeChatsModel, ChatRoomActivity.REQ_VOICE_CALL);
            }
        } else if (newEvent instanceof MessageModel) {
            MessageModel newChat = (MessageModel) newEvent;
            if (searchData(ACTIVE_CHAT)) {
                Log.d(TAG, "IS_ACTIVE_CHAT: YES");
                if (newChat.getChat().getIdSender() != ((UserModel) getData(DATA_USER)).getIdUser()) {
                    Log.d(TAG, "before: " + ((ChatsModel) getData(ACTIVE_CHAT)).getChats().size());
                    ChatType type = getTypeText(newChat.getChat().getAttachment(), newChat.getChat().getMessage());
                    String time = dateTimeToStringHour(new DateTime());
                    activeChatsModel.getChats().add(new ChatsModel.Chat(
                            newChat.getChat().getIdChat(),
                            newChat.getChat().getIdSender(),
                            ((UserModel) getData(DATA_USER)).getIdUser(),
                            time,
                            false,
                            type,
                            newChat.getChat().getMessage()
                    ));
                    setData(ACTIVE_CHAT, activeChatsModel);
                    Log.d(TAG, "after: " + ((ChatsModel) getData(ACTIVE_CHAT)).getChats().size());
                    showNotification(getContext(), ID_CHANNEL_MESSAGE, activeChatsModel.getSenderName(), newChat.getChat().getMessage(), newChat.getChat().getIdChat(), false, null);
                    chatsActiveAdapter.updateItem(((ChatsModel) getData(ACTIVE_CHAT)), 0);
                }
            } else {
                Log.d(TAG, "IS_ACTIVE_CHAT: NO");
                ArrayList<ChatsModel.Chat> chats = new ArrayList<>();
                ChatsModel newActiveChatsModel = new ChatsModel(newChat.getChat().getIdConversation(), chats);
                ((MainActivity) getActivity()).userController.getDokter(valueOf(newChat.getChat().getIdSender()), new BaseCallback<GetInfoUserResponse>() {
                    @Override
                    public void onSuccess(GetInfoUserResponse result) {
                        App.getInstance().runOnUiThread(() -> {
                            newActiveChatsModel.setIdSender(result.getData().getIdUser());
                            newActiveChatsModel.setSenderName(result.getData().getName());
                            for (ImageModel model : result.getData().getImage()) {
                                if (model.getTypeId() == IMAGE_AVATAR) {
                                    newActiveChatsModel.setSenderAvatar(model.getPath());
                                }
                            }
                            ChatType type = getTypeText(newChat.getChat().getAttachment(), newChat.getChat().getMessage());
                            String time = dateTimeToStringHour(new DateTime());
                            newActiveChatsModel.getChats().add(new ChatsModel.Chat(
                                    newChat.getChat().getIdChat(),
                                    newChat.getChat().getIdSender(),
                                    ((UserModel) getData(DATA_USER)).getIdUser(),
                                    time,
                                    false,
                                    type,
                                    newChat.getChat().getMessage()
                            ));
                            for (AppointmentModel model : appointmentModels) {
                                if (newChat.getChat().getIdConversation() == model.getIdConversation()) {
                                    newActiveChatsModel.setIdJanji(model.getIdJanji());
                                }
                            }
                            setData(ACTIVE_CHAT, newActiveChatsModel);
                            showNotification(getContext(), ID_CHANNEL_MESSAGE, newActiveChatsModel.getSenderName(), newChat.getChat().getMessage(), newChat.getChat().getIdChat(), false, null);
                            setupDataRVActiveChats();
                        });
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG(ChatFragment.class), "Error");
                        showToastyError(getActivity(), ERROR_NULL);
                    }

                    @Override
                    public void onNoConnection() {
                        Log.d(TAG(ChatFragment.class), "No Connection");
                        showToastyError(getActivity(), NO_CONNECTION);
                    }

                    @Override
                    public void onServerBroken() {
                        Log.d(TAG(ChatFragment.class), "Server Broken");
                        showToastyError(getActivity(), SERVER_BROKEN);
                    }
                });
            }
        }
    }

    public void doStart() {
        initState();
        isLoading();
        binding.swipeRefresh.setRefreshing(false);
//        FOR TEMP
//        getDataSchedule();
        if (searchData(ACTIVE_CHAT)) {
            setupDataRVActiveChats();
        } else {
            removeActiveChatLayout();
        }
    }
}

