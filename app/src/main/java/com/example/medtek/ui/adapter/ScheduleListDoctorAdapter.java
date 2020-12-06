package com.example.medtek.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.model.ScheduleDoctorModel;

import java.util.ArrayList;

import static com.example.medtek.utils.Utils.changeDatePattern;
import static java.lang.String.valueOf;

public class ScheduleListDoctorAdapter extends RecyclerView.Adapter<ScheduleListDoctorAdapter.ViewHolder> {
    private final Context mContext;
    private final ArrayList<ScheduleDoctorModel> scheduleDoctorModels;
    private OnItemClickCallback onItemClickCallback;

    public ScheduleListDoctorAdapter(Context mContext) {
        this.mContext = mContext;
        scheduleDoctorModels = new ArrayList<>();
    }

    public void setSchedules(ArrayList<ScheduleDoctorModel> scheduleDoctorModels) {
        App.getInstance().runOnUiThread(() -> {
            if (scheduleDoctorModels.size() > 0) {
                this.scheduleDoctorModels.clear();
            }
            this.scheduleDoctorModels.addAll(scheduleDoctorModels);
            notifyDataSetChanged();
        });
    }

    public void addItem(ScheduleDoctorModel item) {
        this.scheduleDoctorModels.add(item);
        notifyItemInserted(this.scheduleDoctorModels.size() - 1);
    }

    public void removeItem(int position) {
        this.scheduleDoctorModels.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.scheduleDoctorModels.size());
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
        ScheduleDoctorModel model = scheduleDoctorModels.get(position);
        {
            holder.llScheduleDoctor.setOnClickListener(v -> {
                if (holder.getAdapterPosition() < scheduleDoctorModels.size()) {
                    onItemClickCallback.onItemClick(model, holder.getAdapterPosition());
                }
            });
        }
        App.getInstance().runOnUiThread(() -> {
            holder.llSchedulePatient.setVisibility(View.GONE);
            holder.llScheduleDoctor.setVisibility(View.VISIBLE);
            holder.tvDateScheduleDoctor.setText(changeDatePattern(model.getTglJanji()));
            holder.tvAmountPatient.setText(holder.itemView.getResources().getString(R.string.amount_patient)
                    .replace("__amount__", valueOf(model.getAppointmentModelList().size())));
        });
    }

    @Override
    public int getItemCount() {
        return scheduleDoctorModels.size();
    }

    public interface OnItemClickCallback {
        void onItemClick(ScheduleDoctorModel model, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llSchedulePatient, llScheduleDoctor;
        TextView tvDateScheduleDoctor, tvAmountPatient;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            llSchedulePatient = itemView.findViewById(R.id.ll_schedule_patient);
            llScheduleDoctor = itemView.findViewById(R.id.ll_schedule_doctor);
            tvDateScheduleDoctor = itemView.findViewById(R.id.tv_date_schedule_doctor);
            tvAmountPatient = itemView.findViewById(R.id.tv_amount_patient);
        }
    }
}

