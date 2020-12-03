package com.example.medtek.Pasien.Others;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.Pasien.Home.Articles.DetailArtikelFragment;
import com.example.medtek.Pasien.Model.ArtikelModel;
import com.example.medtek.Pasien.Model.TransactionModel;
import com.example.medtek.R;
import com.example.medtek.Utils.RoundedCornersTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.abhinay.input.CurrencyEditText;
import me.abhinay.input.CurrencySymbols;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    private List<TransactionModel> mList = new ArrayList<>();
    private Context mContext;
    private FragmentActivity mActivity;

    public WalletAdapter(List<TransactionModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_transaction_history,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TransactionModel model = mList.get(position);

        LinearLayout.LayoutParams lf = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lm = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin_rl = (int) mActivity.getResources().getDimension(R.dimen.margin_16);
        int margin_rl_bottom = (int) (mActivity.getResources().getDimension(R.dimen.margin_16)-mActivity.getResources().getDimension(R.dimen.margin_8));
        lf.setMargins(margin_rl, margin_rl_bottom, margin_rl, 0);
        ll.setMargins(margin_rl, 0, margin_rl, margin_rl_bottom);
        ll.setMargins(margin_rl, margin_rl_bottom, margin_rl, margin_rl_bottom);

        if(position==0){
            holder.linearLayout.setLayoutParams(lf);
        } else if (position==(getItemCount()-1)){
            holder.linearLayout.setLayoutParams(ll);
        } else if (position==(getItemCount()-1) && position == 1){
            holder.linearLayout.setLayoutParams(lm);
        } else{
            holder.linearLayout.setLayoutParams(lf);
        }
        holder.currencyEditText.setCurrency(CurrencySymbols.INDONESIA);
        holder.currencyEditText.setDelimiter(false);
        holder.currencyEditText.setSpacing(false);
        holder.currencyEditText.setDecimals(false);
        holder.currencyEditText.setSeparator(".");
        holder.currencyEditText.setText(model.getTotalHarga()+"");

        if (model.getIdType() == 1){
            holder.riv_artikel.setImageDrawable(mActivity.getDrawable(R.drawable.ic_payment));
            holder.tv_judul.setText("Pembayaran Konsultasi");
            holder.tv_desc.setText("Anda telah melakukan pembayaran konsultasi sebesar "+holder.currencyEditText.getText()+".");
        }else if (model.getIdType() == 2){
            holder.riv_artikel.setImageDrawable(mActivity.getDrawable(R.drawable.ic_receive));
            holder.tv_judul.setText("Menerima Pembayaran");
            holder.tv_desc.setText("Anda telah menerima pembayaran sebesar "+holder.currencyEditText.getText()+".");
        }else if (model.getIdType() == 3){
            holder.riv_artikel.setImageDrawable(mActivity.getDrawable(R.drawable.ic_topup_history));
            holder.tv_judul.setText("Isi Ulang");
            holder.tv_desc.setText("Anda telah melakukan isi ulang sebesar "+holder.currencyEditText.getText()+".");
        }else if (model.getIdType() == 4){
            holder.riv_artikel.setImageDrawable(mActivity.getDrawable(R.drawable.ic_refund));
            holder.tv_judul.setText("Refund");
            holder.tv_desc.setText("Anda telah melakukan refund sebesar "+holder.currencyEditText.getText()+".");
        }

        holder.tv_date.setText(model.getDate());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //class utk menampung objek2 yang ada pada halaman item list recycler view
    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_judul, tv_desc, tv_date;
        private ImageView riv_artikel;
        private LinearLayout linearLayout;
        private CurrencyEditText currencyEditText;

        public ViewHolder(View itemView) {
            super(itemView);
            riv_artikel = itemView.findViewById(R.id.riv_image_history);
            tv_judul = itemView.findViewById(R.id.tv_judul_history);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_desc = itemView.findViewById(R.id.tv_desc);
            linearLayout = itemView.findViewById(R.id.rl_history);
            currencyEditText = itemView.findViewById(R.id.tv_totalharga);
        }
    }
}
