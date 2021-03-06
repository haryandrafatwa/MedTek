package com.example.medtek.ui.pasien.home.hospitals;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.pasien.HospitalModel;
import com.example.medtek.utils.RoundedCornersTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {

    private List<HospitalModel> mList = new ArrayList<>();
    private Context mContext;
    private final FragmentActivity mActivity;

    public HospitalAdapter(List<HospitalModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_rekomendasi_rs,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final HospitalModel model = mList.get(position);

        RelativeLayout.LayoutParams lf = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int margin_rl = (int) mActivity.getResources().getDimension(R.dimen.margin_16);
        int margin_rl_end = (int) (mActivity.getResources().getDimension(R.dimen.margin_16)-mActivity.getResources().getDimension(R.dimen.margin_8));
        lf.setMargins(margin_rl, 0, 0, 0);
        ll.setMargins(0, 0, margin_rl_end, 0);

        if (position==(getItemCount()-1) && getItemCount()!=1){
            holder.relativeLayout.setLayoutParams(ll);
        } else if(position==0){
            holder.relativeLayout.setLayoutParams(lf);
        }else if (position==0 && getItemCount()==0){
            holder.layout_empty.setVisibility(View.VISIBLE);
            Picasso.get().load(mActivity.getString(R.string.hospitalURL)).fit().centerCrop().into(holder.civ_rs_empty);
        }

        final int radius = 35;
        final int margin = 0;
        final Transformation transformation = new RoundedCornersTransformation(radius, margin);

        Picasso.get().load(model.getImageURL()).fit().centerCrop().into(holder.civ_rs);

        holder.tv_name.setText(model.getName());
        holder.tv_jenis.setText(model.getJenis());
        holder.tv_location.setText(model.getJalan()+", "+model.getKelurahan());

        NumberFormat f = NumberFormat.getInstance(Locale.ITALIAN);
        f.setMaximumFractionDigits(2);
        holder.tv_distance.setText("±"+f.format(model.getDistance())+" km");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailHospitalFragment detailHospitalFragment = new DetailHospitalFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id_hospital",model.getId());
                detailHospitalFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frameFragment,detailHospitalFragment).addToBackStack("FragmentDetailHospital");
                fragmentTransaction.commit();
            }
        });
        holder.btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemView.performClick();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //class utk menampung objek2 yang ada pada halaman item list recycler view
    public class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tv_name;
        private final TextView tv_jenis;
        private final TextView tv_location;
        private final TextView tv_distance;
        private final CircleImageView civ_rs;
        private final CircleImageView civ_rs_empty;
        private final Button btnDetail;
        private final RelativeLayout relativeLayout;
        private final RelativeLayout layout_empty;

        public ViewHolder(View itemView) {
            super(itemView);
            civ_rs = itemView.findViewById(R.id.civ_rs);
            tv_name = itemView.findViewById(R.id.tv_rs_name);
            tv_jenis = itemView.findViewById(R.id.tv_rs_jenis);
            tv_location = itemView.findViewById(R.id.tv_rs_loc);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            relativeLayout = itemView.findViewById(R.id.rl_rekrs);
            civ_rs_empty = itemView.findViewById(R.id.civ_rs_empty);
            layout_empty = itemView.findViewById(R.id.layout_empty_hospital);
            tv_distance = itemView.findViewById(R.id.tv_rs_distance);
        }
    }

}
