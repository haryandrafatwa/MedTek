package com.example.medtek.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.model.AppointmentModel;
import com.example.medtek.model.ImageModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.medtek.BuildConfig.BASE_URL;
import static com.example.medtek.constant.APPConstant.IMAGE_AVATAR;
import static com.example.medtek.utils.Utils.TAG;
import static com.example.medtek.utils.Utils.changeDatePattern;
import static com.example.medtek.utils.Utils.timeToHour;

public class ScheduleListPatientAdapter extends RecyclerView.Adapter<ScheduleListPatientAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<AppointmentModel> mAppointments;
    private OnItemClickCallback onItemClickCallback;

    public ScheduleListPatientAdapter(Context mContext) {
        this.mContext = mContext;
        mAppointments = new ArrayList<>();
    }

    public void setSchedules(ArrayList<AppointmentModel> mAppointments) {
        App.getInstance().runOnUiThread(() -> {
            if (mAppointments.size() > 0) {
                this.mAppointments.clear();
            }
            this.mAppointments.addAll(mAppointments);
            notifyDataSetChanged();
        });
    }

    public void addItem(AppointmentModel item) {
        this.mAppointments.add(item);
        notifyItemInserted(this.mAppointments.size() - 1);
    }

    public void removeItem(int position) {
        this.mAppointments.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.mAppointments.size());
    }

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consultation_schedule, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel model = mAppointments.get(position);
        {
            holder.llSchedulePatient.setOnClickListener(v -> {
                if (holder.getAdapterPosition() < mAppointments.size()) {
                    onItemClickCallback.onItemClick(model, holder.getAdapterPosition());
                }
            });
        }
        App.getInstance().runOnUiThread(() -> {
            holder.llSchedulePatient.setVisibility(View.VISIBLE);
            holder.llScheduleDoctor.setVisibility(View.GONE);

            String pathAvatar = "";

            for (ImageModel modelImage : model.getDokter().getImage()) {
                if (modelImage.getTypeId() == IMAGE_AVATAR) {
                    pathAvatar = modelImage.getPath();
                }
            }
            if (pathAvatar.equals("")) {
                holder.ivDoctorPicture.setImageResource(R.drawable.ic_dokter_square);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(BASE_URL + pathAvatar)
                        .into(holder.ivDoctorPicture);
            }
            holder.tvDoctorName.setText(model.getDokter().getName());
            holder.tvDoctorSpecialist.setText(model.getDokter().getSpecialization().getSpecialization());
            holder.tvDateSchedule.setText(changeDatePattern(model.getTglJanji()));
            Log.d(TAG(ScheduleListPatientAdapter.class), "starthour : " + model.getJadwal().getStartHour());
            holder.tvTimeSchedule.setText(timeToHour(model.getJadwal().getStartHour()) + " - " + timeToHour(model.getJadwal().getEndHour()));
        });
    }

    @Override
    public int getItemCount() {
        return mAppointments.size();
    }

    public interface OnItemClickCallback {
        void onItemClick(AppointmentModel model, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llSchedulePatient, llScheduleDoctor;
        CircleImageView ivDoctorPicture;
        TextView tvDoctorName, tvDoctorSpecialist, tvDateSchedule, tvTimeSchedule;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            llSchedulePatient = itemView.findViewById(R.id.ll_schedule_patient);
            llScheduleDoctor = itemView.findViewById(R.id.ll_schedule_doctor);
            ivDoctorPicture = itemView.findViewById(R.id.iv_doctor_picture);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvDoctorSpecialist = itemView.findViewById(R.id.tv_doctor_specialist);
            tvDateSchedule = itemView.findViewById(R.id.tv_date_schedule);
            tvTimeSchedule = itemView.findViewById(R.id.tv_time_schedule);
        }
    }
}

