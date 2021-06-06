package com.example.medtek.ui.pasien.others;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.pasien.RiwayatModel;
import com.example.medtek.model.pasien.TransactionModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.abhinay.input.CurrencyEditText;
import me.abhinay.input.CurrencySymbols;

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.ViewHolder> {

    private List<RiwayatModel> mList = new ArrayList<>();
    private Context mContext;
    private final FragmentActivity mActivity;

    public RiwayatAdapter(List<RiwayatModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_riwayat,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RiwayatModel model = mList.get(position);

        RelativeLayout.LayoutParams lf = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lm = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lt = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lf.setMargins((int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_16),
                (int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4));
        ll.setMargins((int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4),
                (int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_16));
        lt.setMargins((int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4),
                (int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_16));
        lm.setMargins((int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4),
                (int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4));

        if(position==0){
            holder.relativeLayout.setLayoutParams(lf);
        } else if ((getItemCount()-1) == 1){
            holder.relativeLayout.setLayoutParams(lt);
        } else if(position==(getItemCount()-1)){
            holder.relativeLayout.setLayoutParams(ll);
        } else{
            holder.relativeLayout.setLayoutParams(lm);
        }

        if (model.getIdType() == 1){
            holder.tv_status.setText("Belum Dimulai");
            holder.tv_status.setTextColor(mActivity.getColor(R.color.textColorGray));
            holder.tv_status.setBackgroundTintList(mActivity.getColorStateList(R.color.textColorLightGray));
        }else if (model.getIdType() == 2){
            holder.tv_status.setText("Menunggu Antrian");
            holder.tv_status.setTextColor(mActivity.getColor(R.color.colorCoffee));
            holder.tv_status.setBackgroundTintList(mActivity.getColorStateList(R.color.colorLatte));
        }else if (model.getIdType() == 3){
            holder.tv_status.setText("Sedang Berlangsung");
            holder.tv_status.setTextColor(mActivity.getColor(R.color.colorCarrot));
            holder.tv_status.setBackgroundTintList(mActivity.getColorStateList(R.color.colorTangerine));
        }else if (model.getIdType() == 4){
            holder.tv_status.setText("Sudah Selesai");
            holder.tv_status.setTextColor(mActivity.getColor(R.color.successColor));
            holder.tv_status.setBackgroundTintList(mActivity.getColorStateList(R.color.success_stroke_color));
        }else if (model.getIdType() == 5){
            holder.tv_status.setText("Pasien Tidak Hadir");
            holder.tv_status.setTextColor(mActivity.getColor(R.color.textColorDarkRed));
            holder.tv_status.setBackgroundTintList(mActivity.getColorStateList(R.color.textColorLightRed));
        }else if (model.getIdType() == 6){
            holder.tv_status.setText("Refund");
            holder.tv_status.setTextColor(mActivity.getColor(R.color.colorOcean));
            holder.tv_status.setBackgroundTintList(mActivity.getColorStateList(R.color.colorWave));
        }

        if (model.getImagePath().equalsIgnoreCase("pasien")){
            holder.civ_dokter.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pasien));
        }else if (model.getImagePath().equalsIgnoreCase("dokter")){
            holder.civ_dokter.setImageDrawable(mActivity.getDrawable(R.drawable.ic_dokter));
        }else{
            Picasso.get().load(model.getImagePath()).into(holder.civ_dokter);
        }

        holder.tv_nama.setText(model.getNama());
        holder.tv_spesialis.setText(model.getSpesialis());
        String detailJanji;
        if (model.getDetailJanji().contains("Keluhan")){
            detailJanji = model.getDetailJanji().split("Keluhan:\n")[1];
        }else{
            detailJanji = model.getDetailJanji();
        }
        holder.tv_detail.setText(detailJanji);
        holder.tv_date.setText(model.getDate());

        holder.currencyEditText.setCurrency(CurrencySymbols.INDONESIA);
        holder.currencyEditText.setDelimiter(false);
        holder.currencyEditText.setSpacing(false);
        holder.currencyEditText.setDecimals(false);
        holder.currencyEditText.setSeparator(".");
        holder.currencyEditText.setText(model.getHarga()+"");
        holder.tv_harga.setText(holder.currencyEditText.getText());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //class utk menampung objek2 yang ada pada halaman item list recycler view
    public class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tv_nama;
        private final TextView tv_spesialis;
        private final TextView tv_date;
        private final TextView tv_harga;
        private final TextView tv_detail;
        private final TextView tv_status;
        private final CircleImageView civ_dokter;
        private final RelativeLayout relativeLayout;
        private final CurrencyEditText currencyEditText;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_nama = itemView.findViewById(R.id.tv_dr_name);
            tv_spesialis = itemView.findViewById(R.id.tv_dr_special);
            tv_status = itemView.findViewById(R.id.tv_status);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            tv_detail = itemView.findViewById(R.id.tv_detail_janji);
            relativeLayout = itemView.findViewById(R.id.rl_recycler);
            civ_dokter = itemView.findViewById(R.id.civ_dokter);
            currencyEditText = itemView.findViewById(R.id.tv_totalharga);
        }
    }
}
