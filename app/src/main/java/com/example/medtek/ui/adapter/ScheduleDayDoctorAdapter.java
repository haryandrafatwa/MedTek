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
import com.example.medtek.controller.AppointmentController;
import com.example.medtek.model.AppointmentModel;
import com.example.medtek.model.UserModel;

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

            isLoading(false, holder.pbLoading, holder.btnChatNow);

            if (model.getTglJanji().equals(LocalDate.now().toString())) {
                holder.btnChatNow.setEnabled(true);

                if (model.getIdStatus() == BELUM_DIMULAI) {
                    holder.btnChatNow.setText(holder.itemView.getResources().getString(R.string.add_to_queue));
                } else if (model.getIdStatus() == MENUNGGU_DIANTRIAN) {
                    if ((getTime(startHour).isBefore(LocalTime.now())) &&
                            (getTime(endHour).isAfter(LocalTime.now()))) {
                        holder.btnChatNow.setText(holder.itemView.getResources().getString(R.string.chat_now));
                        boolean isLiveChat = false;
                        for (AppointmentModel appointmentModel : appointmentModels) {
                            if (appointmentModel.getIdStatus() == SEDANG_BERLANSUNG) {
                                isLiveChat = true;
                                break;
                            }
                        }
                        holder.btnChatNow.setEnabled(!isLiveChat);
                    } else {
                        holder.btnChatNow.setText(holder.itemView.getResources().getString(R.string.remove_to_queue));
                    }
                } else if (model.getIdStatus() == SEDANG_BERLANSUNG) {
                    holder.btnChatNow.setText(holder.itemView.getResources().getString(R.string.chat_now));
                }
            } else {
                holder.btnChatNow.setEnabled(false);
            }

        });
    }

    @Override
    public int getItemCount() {
        return appointmentModels.size();
    }

//    private void setBtnState(String id, int position, Button view, View itemView, ProgressBar pb) {
//        controller.getJanjiSingle(id, new BaseCallback<GetJanjiSingleResponse>() {
//            @Override
//            public void onSuccess(GetJanjiSingleResponse result) {
//                App.getInstance().runOnUiThread(() -> {
//                    isLoading(false, pb, view);
//
//                    if (result.getData().getTglJanji().equals(LocalDate.now().toString())) {
//                        view.setEnabled(true);
//
//                        if (result.getData().getIdStatus() == BELUM_DIMULAI) {
//                            view.setText(itemView.getResources().getString(R.string.add_to_queue));
//                        } else if (result.getData().getIdStatus() == MENUNGGU_DIANTRIAN) {
//                            if ((getTime(startHour).isBefore(LocalTime.now())) &&
//                                    (getTime(endHour).isAfter(LocalTime.now()))) {
//                                boolean isLiveChat = false;
//                                for (AppointmentModel model: appointmentModels) {
//                                    if (model.getIdStatus() == SEDANG_BERLANSUNG) {
//                                        isLiveChat = true;
//                                        break;
//                                    }
//                                }
//                                if (!isLiveChat) {
//                                    view.setText(itemView.getResources().getString(R.string.chat_now));
//                                } else {
//                                    view.setText(itemView.getResources().getString(R.string.remove_to_queue));
//                                }
//                            } else {
//                                view.setText(itemView.getResources().getString(R.string.remove_to_queue));
//                            }
//                        } else if (result.getData().getIdStatus() == SEDANG_BERLANSUNG) {
//                            view.setText(itemView.getResources().getString(R.string.chat_now));
//                        }
//                    } else {
//                        view.setEnabled(false);
//                    }
//
//                    view.setOnClickListener(v -> {
//                        if (result.getData().getTglJanji().equals(LocalDate.now().toString())) {
//                            switch (result.getData().getIdStatus()) {
//                                case BELUM_DIMULAI:
//                                    queueJanji(id, position,view, itemView, pb);
//                                    isLoading(true, pb, view);
//                                    break;
//                                case MENUNGGU_DIANTRIAN:
//                                    if ((getTime(startHour).isBefore(LocalTime.now())) &&
//                                            (getTime(endHour).isAfter(LocalTime.now()))) {
//                                        boolean isLiveChat = false;
//                                        for (AppointmentModel model: appointmentModels) {
//                                            if (model.getIdStatus() == SEDANG_BERLANSUNG) {
//                                                isLiveChat = true;
//                                                break;
//                                            }
//                                        }
//                                        if (!isLiveChat) {
//
//                                        } else {
//                                            dequeueJanji(id, position, view, itemView, pb);
//                                            isLoading(true, pb, view);
//                                        }
//                                    } else {
//                                        dequeueJanji(id, position, view, itemView, pb);
//                                        isLoading(true, pb, view);
//                                    }
//                                    break;
//                                case SEDANG_BERLANSUNG:
//                                    break;
//                            }
//                        }
//                    });
//                });
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                Log.d(TAG, "Error");
//                isLoading(false, pb, view);
//            }
//
//            @Override
//            public void onNoConnection() {
//                Log.d(TAG, "No Connection");
//                isLoading(false, pb, view);
//            }
//
//            @Override
//            public void onServerBroken() {
//                Log.d(TAG, "Server Broken");
//                isLoading(false, pb, view);
//            }
//        });
//    }

//    private void queueJanji(String id, int position, Button view, View itemView, ProgressBar pb) {
//        controller.getJanjiQueue(id, new BaseCallback<BaseResponse>() {
//            @Override
//            public void onSuccess(BaseResponse result) {
//                if (result.getSuccess()) {
//                    setBtnState(id, position, view, itemView, pb);
//                }
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                Log.d(TAG, "Error");
//            }
//
//            @Override
//            public void onNoConnection() {
//                Log.d(TAG, "No Connection");
//            }
//
//            @Override
//            public void onServerBroken() {
//                Log.d(TAG, "Server Broken");
//            }
//        });
//    }

//    private void dequeueJanji(String id, int position, Button view, View itemView, ProgressBar pb) {
//        controller.getJanjiDequeue(id, new BaseCallback<BaseResponse>() {
//            @Override
//            public void onSuccess(BaseResponse result) {
//                if (result.getSuccess()) {
//                    setBtnState(id, position, view, itemView, pb);
//                }
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                Log.d(TAG, "Error");
//            }
//
//            @Override
//            public void onNoConnection() {
//                Log.d(TAG, "No Connection");
//            }
//
//            @Override
//            public void onServerBroken() {
//                Log.d(TAG, "Server Broken");
//            }
//        });
//    }

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

