package com.example.medtek.ui.pasien.others;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.pasien.TransactionModel;

import java.util.ArrayList;
import java.util.List;

import me.abhinay.input.CurrencyEditText;
import me.abhinay.input.CurrencySymbols;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    private List<TransactionModel> mList = new ArrayList<>();
    private Context mContext;
    private final FragmentActivity mActivity;

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
        holder.currencyEditTextHarga.setCurrency(CurrencySymbols.INDONESIA);
        holder.currencyEditTextHarga.setDelimiter(false);
        holder.currencyEditTextHarga.setSpacing(false);
        holder.currencyEditTextHarga.setDecimals(false);
        holder.currencyEditTextHarga.setSeparator(".");
        holder.currencyEditTextHarga.setText(model.getHarga()+"");

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
            holder.tv_desc.setText("Anda telah melakukan refund sebesar "+holder.currencyEditTextHarga.getText()+".");
        }

        holder.tv_date.setText(model.getDate());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //class utk menampung objek2 yang ada pada halaman item list recycler view
    public class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tv_judul;
        private final TextView tv_desc;
        private final TextView tv_date;
        private final ImageView riv_artikel;
        private final LinearLayout linearLayout;
        private final CurrencyEditText currencyEditText;
        private final CurrencyEditText currencyEditTextHarga;

        public ViewHolder(View itemView) {
            super(itemView);
            riv_artikel = itemView.findViewById(R.id.riv_image_history);
            tv_judul = itemView.findViewById(R.id.tv_judul_history);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_desc = itemView.findViewById(R.id.tv_desc);
            linearLayout = itemView.findViewById(R.id.rl_history);
            currencyEditText = itemView.findViewById(R.id.tv_totalharga);
            currencyEditTextHarga = itemView.findViewById(R.id.tv_harga);
        }
    }
}
