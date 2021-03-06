package com.example.medtek.ui.dialog.bottomsheetdialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.controller.AppointmentController;
import com.example.medtek.controller.UserController;
import com.example.medtek.databinding.BsdDetailDoctorScheduleBinding;
import com.example.medtek.model.AppointmentModel;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.model.ScheduleDoctorModel;
import com.example.medtek.model.UserModel;
import com.example.medtek.network.base.BaseResponse;
import com.example.medtek.network.response.GetInfoUserResponse;
import com.example.medtek.network.socket.SocketUtil;
import com.example.medtek.ui.activity.ChatRoomActivity;
import com.example.medtek.ui.adapter.ScheduleDayDoctorAdapter;
import com.example.medtek.ui.dialog.BaseBottomSheetDialog;
import com.example.medtek.ui.fragment.ChatFragment;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;

import static com.example.medtek.constant.APPConstant.BELUM_DIMULAI;
import static com.example.medtek.constant.APPConstant.MENUNGGU_DIANTRIAN;
import static com.example.medtek.constant.APPConstant.NO_CONNECTION;
import static com.example.medtek.constant.APPConstant.SEDANG_BERLANSUNG;
import static com.example.medtek.constant.APPConstant.SERVER_BROKEN;
import static com.example.medtek.utils.PropertyUtil.ACTIVE_CHAT;
import static com.example.medtek.utils.PropertyUtil.DATA_USER;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.PropertyUtil.searchData;
import static com.example.medtek.utils.RecyclerViewUtil.recyclerLinear;
import static com.example.medtek.utils.Utils.TAG;
import static com.example.medtek.utils.Utils.changeDatePattern;
import static com.example.medtek.utils.Utils.dateTimeToString;
import static com.example.medtek.utils.Utils.getTime;
import static com.example.medtek.utils.Utils.showToastyError;
import static com.example.medtek.utils.Utils.timeToHour;
import static java.lang.String.valueOf;

public class BSDScheduleDoctorDetail extends BaseBottomSheetDialog {
    private static final String TAG = BSDScheduleDoctorDetail.class.getSimpleName();

    private BsdDetailDoctorScheduleBinding binding;
    private final ScheduleDoctorModel scheduleDoctorModel;
    private ScheduleDayDoctorAdapter scheduleDayDoctorAdapter;
    private AppointmentController controller;
    private UserController userController;

    private String startHour;
    private String endHour;
    private int sizeAppointment;
    private int sizeAppointmentNow;

    public BSDScheduleDoctorDetail(ScheduleDoctorModel scheduleDoctorModel) {
        this.scheduleDoctorModel = scheduleDoctorModel;
    }

    @Override
    protected View setContentView(LayoutInflater inflater, ViewGroup container) {
        binding = BsdDetailDoctorScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
        controller = new AppointmentController();
        userController = new UserController();
        sizeAppointment = 0;
        sizeAppointmentNow = 0;
        getJadwalDoctor();
    }

    @Override
    protected void setupView() {
        scheduleDayDoctorAdapter = new ScheduleDayDoctorAdapter(App.getContext());
        if (binding != null) {
            searchJadwal();
            binding.tvDateSchedule.setText(changeDatePattern(scheduleDoctorModel.getTglJanji()));
            binding.tvAmountPatient.setText(getResources().getString(R.string.amount_patient)
                    .replace("__amount__", valueOf(scheduleDoctorModel.getAppointmentModelList().size())));
            ArrayList<AppointmentModel> models = new ArrayList<>(scheduleDoctorModel.getAppointmentModelList());
            scheduleDayDoctorAdapter.setSchedules(models);
            recyclerLinear(binding.rvSchedulePasien, LinearLayoutManager.HORIZONTAL, scheduleDayDoctorAdapter);
            binding.btnClose.setOnClickListener(v -> {
                dismiss();
            });
            onClickBtnSchedule();
        }
    }

    @SuppressLint("SetTextI18n")
    private void searchJadwal() {
        for (UserModel.Jadwal jadwal : ((UserModel) getData(DATA_USER)).getJadwal()) {
            if (jadwal.getIdDay() == scheduleDoctorModel.getAppointmentModelList().get(0).getIdDay()) {
                startHour = jadwal.getStartHour();
                endHour = jadwal.getEndHour();
                if (binding != null) {
                    App.getInstance().runOnUiThread(() ->  binding.tvTimeSchedule.setText(timeToHour(startHour) + " - " + timeToHour(endHour)));
                }
                break;
            }
        }
    }

    private void onClickBtnSchedule() {
        scheduleDayDoctorAdapter.setOnItemClickCallback((model, pb, btn, position) -> {
            switch (model.getIdStatus()) {
                case BELUM_DIMULAI:
                    queueJanji(valueOf(model.getIdJanji()), position);
                    scheduleDayDoctorAdapter.isLoading(true, pb, btn);
                    break;
                case MENUNGGU_DIANTRIAN:
                    if (model.getTglJanji().equals(LocalDate.now().toString())) {
                        if ((getTime(startHour).isBefore(LocalTime.now())) &&
                                (getTime(endHour).isAfter(LocalTime.now()))) {
                            SocketUtil.getInstance().listenForEventChat(model.getIdConversation(), model.getIdJanji());
                            ChatsModel chatsModel = new ChatsModel(
                                    model.getIdConversation(),
                                    model.getIdJanji(),
                                    model.getPasien().getName(),
                                    model.getIdPasien(),
                                    dateTimeToString(new DateTime()),
                                    (model.getImageModel() == null) ? "" : model.getImageModel().getPath());
                            if (searchData(ACTIVE_CHAT)) {
                                ChatsModel tempChatsModel = (ChatsModel) getData(ACTIVE_CHAT);
                                if (tempChatsModel.getIdJanji() == model.getIdJanji()) {
                                    chatsModel = tempChatsModel;
                                }
                            }
                            ((ChatFragment) getParentFragment()).navigateToChatRoomWithResult(chatsModel, ChatRoomActivity.REQ_ACTIVE_CHAT);
                            dismiss();
                        }
                    } else {
                        dequeueJanji(valueOf(model.getIdJanji()), position);
                        scheduleDayDoctorAdapter.isLoading(true, pb, btn);
                    }
                    break;
                case SEDANG_BERLANSUNG:
                    SocketUtil.getInstance().listenForEventChat(model.getIdConversation(), model.getIdJanji());
                    ChatsModel chatsModel = new ChatsModel(
                            model.getIdConversation(),
                            model.getIdJanji(),
                            model.getPasien().getName(),
                            model.getIdPasien(),
                            dateTimeToString(new DateTime()),
                            (model.getImageModel() == null) ? "" : model.getImageModel().getPath());

                    if (searchData(ACTIVE_CHAT)) {
                        ChatsModel tempChatsModel = (ChatsModel) getData(ACTIVE_CHAT);
                        if (tempChatsModel.getIdJanji() == model.getIdJanji()) {
                            chatsModel = tempChatsModel;
                        }
                    }

                    ((ChatFragment) getParentFragment()).navigateToChatRoomWithResult(chatsModel, ChatRoomActivity.REQ_ACTIVE_CHAT);
                    dismiss();
                    break;
            }
        });
    }

    private void getJadwalDoctor() {
        String idDokter = valueOf(((UserModel) getData(DATA_USER)).getIdUser());
        userController.getDokter(idDokter, new BaseCallback<GetInfoUserResponse>() {
            @Override
            public void onSuccess(GetInfoUserResponse result) {
                ((UserModel) getData(DATA_USER)).setJadwal(result.getData().getJadwal());
                searchJadwal();
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "Error");
                showToastyError(getActivity(), t.getMessage());
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "No Connection");
                showToastyError(getActivity(), NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "Server Broken");
                showToastyError(getActivity(), SERVER_BROKEN);
            }
        });
    }

    private void queueJanji(String id, int position) {
        controller.getJanjiQueue(id, new BaseCallback<BaseResponse>() {
            @Override
            public void onSuccess(BaseResponse result) {
                scheduleDayDoctorAdapter.notifyItemChanged(position);
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "Error");
                showToastyError(getActivity(), t.getMessage());
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "No Connection");
                showToastyError(getActivity(), NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "Server Broken");
                showToastyError(getActivity(), SERVER_BROKEN);
            }
        });
    }

    private void dequeueJanji(String id, int position) {
        controller.getJanjiDequeue(id, new BaseCallback<BaseResponse>() {
            @Override
            public void onSuccess(BaseResponse result) {
                scheduleDayDoctorAdapter.removeItem(position);
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "Error");
                showToastyError(getActivity(), t.getMessage());
            }

            @Override
            public void onNoConnection() {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "No Connection");
                showToastyError(getActivity(), NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                Log.d(TAG(BSDScheduleDoctorDetail.class), "Server Broken");
                showToastyError(getActivity(), SERVER_BROKEN);
            }
        });
    }

    @Override
    protected void destroyView() {
        binding = null;
    }

}

