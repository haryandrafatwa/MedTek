package com.example.medtek.Pasien.Home.Doctors;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.Pasien.Model.DokterModel;
import com.example.medtek.R;
import com.example.medtek.Utils.RoundedCornersTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DokterAdapterMore extends RecyclerView.Adapter<DokterAdapterMore.ViewHolder> implements Filterable {
    private List<DokterModel> mList;
    private List<DokterModel> mListFull;
    private Context mContext;
    private FragmentActivity mActivity;

    public DokterAdapterMore(List<DokterModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mListFull = new ArrayList<>(mList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_popular_doctor_all,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DokterModel model = mList.get(position);

        RelativeLayout.LayoutParams lf = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lf.setMargins(0, 32, 0, 0);
        ll.setMargins(0, 0, 0, 32);

        if(position==0){
            holder.relativeLayout.setLayoutParams(lf);
        } else if (position==(getItemCount()-1)){
            holder.relativeLayout.setLayoutParams(ll);
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
        holder.tv_rating.setText(model.getRating()+"");
        String myPrice = NumberFormat.getInstance(Locale.ITALIAN).format(Integer.valueOf(model.getHarga()));
        holder.tv_harga.setText("Rp"+myPrice);
        holder.tv_lamakerja.setText(model.getLamakerja()+" "+mActivity.getResources().getString(R.string.tahun));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailDokterFragment detailDokterFragment = new DetailDokterFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id_dokter",model.getId());
                detailDokterFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frameFragment,detailDokterFragment).addToBackStack("FragmentDetailDokter");
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public Filter getFilter() {
        return doctorsFilter;
    }

    private Filter doctorsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<DokterModel> filteredDoctors = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                filteredDoctors.addAll(mListFull);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (DokterModel itemArticle:mListFull){
                    if(itemArticle.getNama().toLowerCase().contains(filterPattern)){
                        filteredDoctors.add(itemArticle);
                    }
                }
            }


            FilterResults results = new FilterResults();
            results.values = filteredDoctors;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList.clear();
            mList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    //class utk menampung objek2 yang ada pada halaman item list recycler view
    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_name, tv_specialist, tv_rs, tv_location,tv_rating,tv_lamakerja,tv_harga;
        private CircleImageView civ_dokter;
        private Button btnDetail;
        private RelativeLayout relativeLayout;
        private RatingBar ratingBar;

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
            tv_lamakerja = itemView.findViewById(R.id.tv_dr_exp);
        }
    }
}
