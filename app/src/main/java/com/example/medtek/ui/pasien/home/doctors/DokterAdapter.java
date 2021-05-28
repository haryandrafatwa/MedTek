package com.example.medtek.ui.pasien.home.doctors;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.pasien.DokterModel;
import com.example.medtek.utils.RoundedCornersTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DokterAdapter extends RecyclerView.Adapter<DokterAdapter.ViewHolder>{
    private List<DokterModel> mList = new ArrayList<>();
    private Context mContext;
    private final FragmentActivity mActivity;

    public DokterAdapter(List<DokterModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_popular_doctor,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DokterModel model = mList.get(position);

        RelativeLayout.LayoutParams lf = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int margin_rl = (int) mActivity.getResources().getDimension(R.dimen.margin_16);
        int margin_rl_end = (int) (mActivity.getResources().getDimension(R.dimen.margin_16)-mActivity.getResources().getDimension(R.dimen.margin_8));
        lf.setMargins(margin_rl, 0, 0, 0);
        ll.setMargins(0, 0, margin_rl_end, 0);

        if(getItemCount() > 1){
            if (position==(getItemCount()-1)){
                holder.relativeLayout.setLayoutParams(ll);
            } else if(position==0){
                holder.relativeLayout.setLayoutParams(lf);
            }
        }else{
            holder.relativeLayout.setLayoutParams(lf);
        }

        final int radius = 24;
        final int margin = 24;
        final Transformation transformation = new RoundedCornersTransformation(radius, margin);

        if (!model.getImage_url().isEmpty()){
            Picasso.get().load(model.getImage_url()).into(holder.civ_dokter);
        }else{
            holder.civ_dokter.setImageDrawable(mActivity.getDrawable(R.drawable.ic_dokter));
        }

        holder.tv_name.setText(model.getNama());
        holder.tv_specialist.setText(model.getSpesialisasi());
        holder.tv_rs.setText(model.getNama_rs());
        holder.tv_location.setText(model.getAlamat_rs());
        holder.ratingBar.setRating(model.getRating());
        String myPrice = NumberFormat.getInstance(Locale.ITALIAN).format(Integer.valueOf(model.getHarga()));
        holder.tv_harga.setText("Rp"+myPrice);
        holder.tv_rating.setText(model.getRating()+"");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailDokterFragment detailDokterFragment = new DetailDokterFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id_doc",model.getId());
                detailDokterFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frameFragment,detailDokterFragment).addToBackStack("FragmentDetailDokter");
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
        private final TextView tv_specialist;
        private final TextView tv_rs;
        private final TextView tv_location;
        private final TextView tv_rating;
        private final TextView tv_harga;
        private final CircleImageView civ_dokter;
        private final Button btnDetail;
        private final RelativeLayout relativeLayout;
        private final RatingBar ratingBar;

        public ViewHolder(View itemView) {
            super(itemView);
            civ_dokter = itemView.findViewById(R.id.civ_dokter);
            tv_name = itemView.findViewById(R.id.tv_dr_name);
            tv_specialist = itemView.findViewById(R.id.tv_dr_special);
            tv_rs = itemView.findViewById(R.id.tv_dr_rs);
            tv_location = itemView.findViewById(R.id.tv_dr_rs_loc);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            relativeLayout = itemView.findViewById(R.id.rl_artikel);
            ratingBar = itemView.findViewById(R.id.rb_dokter);
            tv_rating = itemView.findViewById(R.id.tv_dr_rat);
            tv_harga = itemView.findViewById(R.id.tv_dr_harga);
        }
    }
}