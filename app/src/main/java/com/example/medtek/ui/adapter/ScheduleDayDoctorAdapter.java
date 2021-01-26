package com.example.medtek.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.controller.AppointmentController;
import com.example.medtek.model.AppointmentModel;
import com.example.medtek.model.UserModel;
import com.example.medtek.network.response.GetJanjiSingleResponse;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.medtek.BuildConfig.BASE_URL;
import static com.example.medtek.constant.APPConstant.BELUM_DIMULAI;
import static com.example.medtek.constant.APPConstant.MENUNGGU_DIANTRIAN;
import static com.example.medtek.constant.APPConstant.SEDANG_BERLANSUNG;
import static com.example.medtek.utils.PropertyUtil.DATA_USER;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.getTime;
import static java.lang.String.valueOf;

public class ScheduleDayDoctorAdapter extends RecyclerView.Adapter<ScheduleDayDoctorAdapter.ViewHolder> {
    private static final String TAG = ScheduleDayDoctorAdapter.class.getSimpleName();

    private final Context mContext;
    private final ArrayList<AppointmentModel> appointmentModels;
    private final AppointmentController controller;
    private OnItemClickCallback onItemClickCallback;

    private String startHour;
    private String endHour;

    public ScheduleDayDoctorAdapter(Context mContext) {
        this.mContext = mContext;
        appointmentModels = new ArrayList<>();
        controller = new AppointmentController();
    }

    public void setSchedules(ArrayList<AppointmentModel> appointmentModels) {
        if (appointmentModels.size() > 0) {
            this.appointmentModels.clear();
        }
        this.appointmentModels.addAll(appointmentModels);
        notifyDataSetChanged();
    }

    public void addItem(AppointmentModel item) {
        this.appointmentModels.add(item);
        notifyItemInserted(this.appointmentModels.size() - 1);
    }

    public void updateItem(AppointmentModel item, int position) {
        this.appointmentModels.set(position, item);
        notifyItemChanged(position, item);
    }

    public void removeItem(int position) {
        this.appointmentModels.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.appointmentModels.size());
    }

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue_schedule, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel model = appointmentModels.get(position);
        {
            holder.btnChatNow.setOnClickListener(v -> {
                if (holder.getAdapterPosition() < appointmentModels.size()) {
                    onItemClickCallback.onItemClick(model, holder.pbLoading, holder.btnChatNow, holder.getAdapterPosition());
                }
            });
        }
        App.getInstance().runOnUiThread(() -> {
            searchJadwal();
            isLoading(true, holder.pbLoading, holder.btnChatNow);
            if (model.getImageModel() != null) {
                Glide.with(holder.itemView.getContext())
                        .load(BASE_URL + model.getImageModel().getPath())
                        .into(holder.ivPatientPicture);
            } else {
                holder.ivPatientPicture.setImageResource(R.drawable.ic_pasien_square);
            }
            holder.tvPatientName.setText(model.getPasien().getName());
            holder.tvDuration.setText("30 Minutes");

            isLoading(true, holder.pbLoading, holder.btnChatNow);

            controller.getJanjiSingle(valueOf(model.getIdJanji()), new BaseCallback<GetJanjiSingleResponse>() {
                @Override
                public void onSuccess(GetJanjiSingleResponse result) {
                    int idStatus = result.getData().getIdStatus();

                    App.getInstance().runOnUiThread(() -> {
                        isLoading(false, holder.pbLoading, holder.btnChatNow);

                        boolean isEnabled = true;
                        String btnText = "";

                        switch (idStatus) {
                            case BELUM_DIMULAI:
                                isEnabled = model.getTglJanji().equals(LocalDate.now().toString());
                                btnText = holder.itemView.getResources().getString(R.string.add_to_queue);
                                break;
                            case MENUNGGU_DIANTRIAN:
                                if (model.getTglJanji().equals(LocalDate.now().toString())) {
                                    if ((getTime(startHour).isBefore(LocalTime.now())) &&
                                            (getTime(endHour).isAfter(LocalTime.now()))) {
                                        btnText = holder.itemView.getResources().getString(R.string.chat_now);
                                        boolean isLiveChat = false;
                                        for (AppointmentModel appointmentModel : appointmentModels) {
                                            if (appointmentModel.getIdStatus() == SEDANG_BERLANSUNG) {
                                                isLiveChat = true;
                                                break;
                                            }
                                        }
                                        isEnabled = !isLiveChat;
                                    } else {
                                        btnText = holder.itemView.getResources().getString(R.string.waiting_for_queue);
                                    }
                                } else {
//                                    btnText = holder.itemView.getResources().getString(R.string.remove_to_queue);
                                    btnText = holder.itemView.getResources().getString(R.string.chat_now);
                                    isEnabled = false;
                                }
                                break;
                            case SEDANG_BERLANSUNG:
                                if (model.getTglJanji().equals(LocalDate.now().toString())) {
                                    isEnabled = (getTime(startHour).isBefore(LocalTime.now())) &&
                                            (getTime(endHour).isAfter(LocalTime.now()));
                                } else {
                                    isEnabled = false;
                                }

                                btnText = holder.itemView.getResources().getString(R.string.chat_now);
                                break;
                        }
                        holder.btnChatNow.setEnabled(isEnabled);
                        holder.btnChatNow.setText(btnText);
                    });

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onNoConnection() {

                }

                @Override
                public void onServerBroken() {

                }
            });

        });
    }

    @Override
    public int getItemCount() {
        return appointmentModels.size();
    }

    private void searchJadwal() {
        for (UserModel.Jadwal jadwal : ((UserModel) getData(DATA_USER)).getJadwal()) {
            if (jadwal.getIdDay() == appointmentModels.get(0).getIdDay()) {
                startHour = jadwal.getStartHour();
                endHour = jadwal.getEndHour();
            }
        }
    }

    public void isLoading(boolean status, ProgressBar pb, Button btn) {
        if (status) {
            pb.setVisibility(View.VISIBLE);
            btn.setVisibility(View.GONE);
        } else {
            pb.setVisibility(View.GONE);
            btn.setVisibility(View.VISIBLE);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView ivPatientPicture;
        TextView tvPatientName, tvDuration;
        ProgressBar pbLoading;
        Button btnChatNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPatientPicture = itemView.findViewById(R.id.iv_patient_picture);
            tvPatientName = itemView.findViewById(R.id.tv_name_patient);
            tvDuration = itemView.findViewById(R.id.tv_duration_schedule);
            pbLoading = itemView.findViewById(R.id.loading);
            btnChatNow = itemView.findViewById(R.id.btn_chat_now);
        }
    }

    public interface OnItemClickCallback {
        void onItemClick(AppointmentModel model, ProgressBar pb, Button btn, int position);
    }


}

