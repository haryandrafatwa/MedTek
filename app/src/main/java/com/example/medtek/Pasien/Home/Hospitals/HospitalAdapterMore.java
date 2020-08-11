package com.example.medtek.Pasien.Home.Hospitals;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.NumberFormat;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.Pasien.Home.Doctors.DokterAdapterMore;
import com.example.medtek.Pasien.Model.DokterModel;
import com.example.medtek.Pasien.Model.HospitalModel;
import com.example.medtek.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class HospitalAdapterMore extends RecyclerView.Adapter<HospitalAdapterMore.ViewHolder> implements Filterable {

    private List<HospitalModel> mList;
    private List<HospitalModel> mListFull;
    private Context mContext;
    private FragmentActivity mActivity;

    public HospitalAdapterMore(List<HospitalModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mListFull = new ArrayList<>(mList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_rekomendasi_rs_all,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final HospitalModel model = mList.get(position);

        RelativeLayout.LayoutParams lf = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lf.setMargins(0, 32, 0, 0);
        ll.setMargins(0, 0, 0, 32);

        if(position==0){
            holder.relativeLayout.setLayoutParams(lf);
        } else if (position==(getItemCount()-1)){
            holder.relativeLayout.setLayoutParams(ll);
        }

        holder.tv_name.setText(model.getName());
        holder.tv_jenis.setText(model.getJenis());
        holder.tv_location.setText(model.getKelurahan()+", "+model.getKota());
        Picasso.get().load(model.getImageURL()).fit().centerCrop().into(holder.civ_rs);
        NumberFormat f = NumberFormat.getInstance(Locale.ITALIAN);
        f.setMaximumFractionDigits(2);
        holder.tv_distance.setText("±"+f.format(model.getDistance())+" km");
        holder.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + model.getNo_telp()));//change the number
                    mActivity.startActivity(callIntent);
                }else{
                    ActivityCompat.requestPermissions(mActivity, new String[]{
                            Manifest.permission.CALL_PHONE
                    },28);
                }
            }
        });
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
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public Filter getFilter() {
        return hospitalsFilter;
    }

    private Filter hospitalsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<HospitalModel> filteredHospitals = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                filteredHospitals.addAll(mListFull);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (HospitalModel itemHospitals:mListFull){
                    if(itemHospitals.getName().toLowerCase().contains(filterPattern)){
                        filteredHospitals.add(itemHospitals);
                    }
                }
            }


            FilterResults results = new FilterResults();
            results.values = filteredHospitals;
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

        private TextView tv_name, tv_jenis, tv_location,tv_distance;
        private CircleImageView civ_rs;
        private Button btnCall;
        private RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            civ_rs = itemView.findViewById(R.id.civ_rs);
            tv_name = itemView.findViewById(R.id.tv_rs_name);
            tv_jenis = itemView.findViewById(R.id.tv_rs_jenis);
            tv_location = itemView.findViewById(R.id.tv_rs_loc);
            btnCall = itemView.findViewById(R.id.btnCall);
            relativeLayout = itemView.findViewById(R.id.rl_rekrs);
            tv_distance = itemView.findViewById(R.id.tv_rs_distance);
        }
    }

}
