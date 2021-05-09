package com.example.medtek.ui.dialog.bottomsheetdialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.controller.AppointmentController;
import com.example.medtek.databinding.BsdDetailPatientScheduleBinding;
import com.example.medtek.model.AppointmentModel;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.model.ImageModel;
import com.example.medtek.network.response.GetJanjiSingleResponse;
import com.example.medtek.ui.activity.ChatRoomActivity;
import com.example.medtek.ui.dialog.BaseBottomSheetDialog;
import com.example.medtek.ui.fragment.ChatFragment;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import static com.example.medtek.BuildConfig.BASE_URL;
import static com.example.medtek.constant.APPConstant.BELUM_DIMULAI;
import static com.example.medtek.constant.APPConstant.ERROR_NULL;
import static com.example.medtek.constant.APPConstant.IMAGE_AVATAR;
import static com.example.medtek.constant.APPConstant.MENUNGGU_DIANTRIAN;
import static com.example.medtek.constant.APPConstant.NO_CONNECTION;
import static com.example.medtek.constant.APPConstant.PASIEN_TIDAK_DATANG;
import static com.example.medtek.constant.APPConstant.PAYMENT_JANJI;
import static com.example.medtek.constant.APPConstant.SEDANG_BERLANSUNG;
import static com.example.medtek.constant.APPConstant.SERVER_BROKEN;
import static com.example.medtek.constant.APPConstant.SUDAH_SELESAI;
import static com.example.medtek.utils.PropertyUtil.ACTIVE_CHAT;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.PropertyUtil.searchData;
import static com.example.medtek.utils.Utils.TAG;
import static com.example.medtek.utils.Utils.changeDatePattern;
import static com.example.medtek.utils.Utils.dateTimeToString;
import static com.example.medtek.utils.Utils.dateTimeToStringDate;
import static com.example.medtek.utils.Utils.getDate;
import static com.example.medtek.utils.Utils.getThousandFormat;
import static com.example.medtek.utils.Utils.getTime;
import static com.example.medtek.utils.Utils.showToastyError;
import static com.example.medtek.utils.Utils.timeToHour;
import static java.lang.String.valueOf;

public class BSDSchedulePatientDetail extends BaseBottomSheetDialog {
    private static final String TAG = BSDSchedulePatientDetail.class.getSimpleName();

    private BsdDetailPatientScheduleBinding binding;
    private final AppointmentModel appointmentModel;
    private AppointmentController controller;

    public BSDSchedulePatientDetail(AppointmentModel appointmentModel) {
        this.appointmentModel = appointmentModel;
    }

    @Override
    protected View setContentView(LayoutInflater inflater, ViewGroup container) {
        binding = BsdDetailPatientScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
        controller = new AppointmentController();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void setupView() {
        if (binding != null) {
            isLoading(true);
            String pathAvatar = "";
            for (ImageModel modelImage : appointmentModel.getDokter().getImage()) {
                if (modelImage.getTypeId() == IMAGE_AVATAR) {
                    pathAvatar = modelImage.getPath();
                }
            }
            if (pathAvatar.equals("")) {
                Glide.with(App.getContext())
                        .load(R.drawable.ic_dokter_square)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                        .into(binding.ivDoctorPicture);
            } else {
                Glide.with(App.getContext())
                        .load(BASE_URL + pathAvatar)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                        .into(binding.ivDoctorPicture);
            }

            if (dateTimeToStringDate(new DateTime()).equals(appointmentModel.getTglJanji()) &&
                    LocalTime.now().isAfter(getTime(appointmentModel.getJadwal().getStartHour())) &&
                    LocalTime.now().isBefore(getTime(appointmentModel.getJadwal().getEndHour()))) {
                binding.llJadwal.setBackground(getResources().getDrawable(R.drawable.bg_schedule_online));
            } else {
                binding.llJadwal.setBackground(getResources().getDrawable(R.drawable.bg_schedule_offline));
            }

            binding.tvDoctorName.setText(appointmentModel.getDokter().getName());
            binding.tvDoctorSpecialist.setText(appointmentModel.getDokter().getSpecialization().getSpecialization());
            binding.tvDateSchedule.setText(changeDatePattern(appointmentModel.getTglJanji()));

            String[] startDateHour = appointmentModel.getStartHour().split("\\s+");
            String[] endDateHour = appointmentModel.getEndHour().split("\\s+");
            binding.tvTimeSchedule.setText(timeToHour(startDateHour[1]) + " - " + timeToHour(endDateHour[1]));

            for (AppointmentModel.Transaksi transaksi : appointmentModel.getTransaksi()) {
                if (transaksi.getIdType() == PAYMENT_JANJI) {
                    binding.tvCharge.setText(getResources().getString(R.string.charge_amount)
                            .replace("__amount__", getThousandFormat(transaksi.getTotalHarga())));
                    break;
                }
            }
            binding.tvDuration.setText(appointmentModel.getDuration() + " Menit");
            setBtnChatNow();
            String finalPathAvatar = pathAvatar;
            binding.btnChatNow.setOnClickListener(v -> {
                ChatsModel chatsModel = new ChatsModel(
                        appointmentModel.getIdConversation(),
                        appointmentModel.getIdJanji(),
                        appointmentModel.getDokter().getName(),
                        appointmentModel.getIdDokter(),
                        dateTimeToString(new DateTime()),
                        finalPathAvatar);

                if (searchData(ACTIVE_CHAT)) {
                    ChatsModel tempChatsModel =  (ChatsModel) getData(ACTIVE_CHAT);
                    if (tempChatsModel.getIdJanji() == appointmentModel.getIdJanji()) {
                        chatsModel = tempChatsModel;
                    }
                }

                ((ChatFragment) getParentFragment()).navigateToChatRoomWithResult(chatsModel, ChatRoomActivity.REQ_ACTIVE_CHAT);
                dismiss();
            });
        }
    }

    private void setBtnChatNow() {
        controller.getJanjiSingle(valueOf(appointmentModel.getIdJanji()), new BaseCallback<GetJanjiSingleResponse>() {
            @Override
            public void onSuccess(GetJanjiSingleResponse result) {
                isLoading(false);
                App.getInstance().runOnUiThread(() -> {

                    switch (result.getData().getIdStatus()) {
                        case SUDAH_SELESAI:
                        case PASIEN_TIDAK_DATANG:
                        case BELUM_DIMULAI:
                            binding.btnChatNow.setEnabled(false);
                            binding.btnChatNow.setText(getResources().getString(R.string.waiting_for_confirm));
                            break;
                        case MENUNGGU_DIANTRIAN:
                            if ((getDate(result.getData().getTglJanji())).toLocalDate().equals(new DateTime().toLocalDate())) {
                                binding.btnChatNow.setText(getResources().getString(R.string.waiting_for_queue));
                            } else {
                                binding.btnChatNow.setText(getResources().getString(R.string.waiting_for_schedule));
                            }
                            binding.btnChatNow.setEnabled(false);
                            break;
                        case SEDANG_BERLANSUNG:
                            binding.btnChatNow.setEnabled(true);
                            binding.btnChatNow.setText(getResources().getString(R.string.chat_now));
                            break;
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                isLoading(false);
                Log.d(TAG(BSDSchedulePatientDetail.class), "Error");
                showToastyError(getActivity(), ERROR_NULL);
            }

            @Override
            public void onNoConnection() {
                isLoading(false);
                Log.d(TAG(BSDSchedulePatientDetail.class), "No Connection");
                showToastyError(getActivity(), NO_CONNECTION);
            }

            @Override
            public void onServerBroken() {
                isLoading(false);
                Log.d(TAG(BSDSchedulePatientDetail.class), "Server Broken");
                showToastyError(getActivity(), SERVER_BROKEN);
            }
        });
    }

    @Override
    protected void destroyView() {
        binding = null;
    }

    private void isLoading(boolean status) {
        App.getInstance().runOnUiThread(() -> {
            if (status) {
                binding.loading.setVisibility(View.VISIBLE);
                binding.btnChatNow.setVisibility(View.GONE);
            } else {
                binding.loading.setVisibility(View.GONE);
                binding.btnChatNow.setVisibility(View.VISIBLE);
            }
        });
    }
}
