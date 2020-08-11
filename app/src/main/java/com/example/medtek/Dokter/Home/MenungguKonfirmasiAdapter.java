package com.example.medtek.Dokter.Home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Home.Doctors.DokterAdapterMore;
import com.example.medtek.Pasien.Model.DokterModel;
import com.example.medtek.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenungguKonfirmasiAdapter extends RecyclerView.Adapter<MenungguKonfirmasiAdapter.ViewHolder> {

    private List<JanjiModel> mList;
    private Context mContext;
    private FragmentActivity mActivity;

    public MenungguKonfirmasiAdapter(List<JanjiModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_antrian_konfirmasi,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final JanjiModel model = mList.get(position);

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

        Call<ResponseBody> getPasien = RetrofitClient.getInstance().getApi().getPasienId(model.getIdPasien());
        getPasien.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            String s = response.body().string();
                            JSONObject jsonObject = new JSONObject(s);
                            JSONObject data = jsonObject.getJSONObject("data");
                            holder.tv_name.setText(data.getString("name"));
                            JSONArray jsonArray = data.getJSONArray("image");
                            if (jsonArray.length() == 0){
                                holder.civ_pasien.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pasien));
                            }else{
                                String path = "http://192.168.1.9:8000"+jsonArray.getJSONObject(0).getString("path");
                                if (jsonArray.getJSONObject(0).getString("path").equals("/storage/Pasien.png")){
                                    holder.civ_pasien.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pasien));
                                }else{
                                    Picasso.get().load(path).into(holder.civ_pasien);
                                }
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        holder.tv_detail.setText(model.getDetailJanji());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_name, tv_detail, tv_tolak, tv_terima;
        private CircleImageView civ_pasien;
        private ImageButton ib_tolak, ib_terima;
        private RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.rl_recycler);
            civ_pasien = itemView.findViewById(R.id.civ_pasien);
            tv_name = itemView.findViewById(R.id.tv_nama_pasien);
            tv_detail = itemView.findViewById(R.id.tv_detail_janji);
            tv_tolak = itemView.findViewById(R.id.tv_tolak);
            tv_terima = itemView.findViewById(R.id.tv_terima);
            ib_tolak = itemView.findViewById(R.id.ib_tolak);
            ib_terima = itemView.findViewById(R.id.ib_terima);
        }
    }

}
