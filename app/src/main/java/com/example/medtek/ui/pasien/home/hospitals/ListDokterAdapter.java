package com.example.medtek.ui.pasien.home.hospitals;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.pasien.ListDokterModel;
import com.example.medtek.ui.pasien.home.doctors.DetailDokterFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListDokterAdapter extends RecyclerView.Adapter<ListDokterAdapter.ViewHolder> {

    private List<ListDokterModel> mList = new ArrayList<>();
    private Context mContext;
    private final FragmentActivity mActivity;

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

        private final TextView tv_name;
        private final TextView tv_specialist;
        private final CircleImageView civ_dokter;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_dr_name);
            tv_specialist = itemView.findViewById(R.id.tv_dr_special);
            civ_dokter = itemView.findViewById(R.id.civ_dokter);
        }
    }

}
