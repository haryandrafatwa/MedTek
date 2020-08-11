package com.example.medtek.Pasien.Home.Hospitals;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.codesgood.views.JustifiedTextView;
import com.example.medtek.Pasien.Home.Doctors.DetailDokterFragment;
import com.example.medtek.Pasien.Model.FeedbackModel;
import com.example.medtek.Pasien.Model.ListDokterModel;
import com.example.medtek.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListDokterAdapter extends RecyclerView.Adapter<ListDokterAdapter.ViewHolder> {

    private List<ListDokterModel> mList = new ArrayList<>();
    private Context mContext;
    private FragmentActivity mActivity;

    public ListDokterAdapter(List<ListDokterModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_list_dokter,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ListDokterModel model = mList.get(position);

        holder.tv_name.setText(model.getNama());
        holder.tv_specialist.setText(model.getSpesialisasi());
        Picasso.get().load(model.getImage_url()).fit().into(holder.civ_dokter);
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

    //class utk menampung objek2 yang ada pada halaman item list recycler view
    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_name, tv_specialist;
        private CircleImageView civ_dokter;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_dr_name);
            tv_specialist = itemView.findViewById(R.id.tv_dr_special);
            civ_dokter = itemView.findViewById(R.id.civ_dokter);
        }
    }

}
