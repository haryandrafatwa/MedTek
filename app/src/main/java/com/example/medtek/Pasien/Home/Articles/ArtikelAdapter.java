package com.example.medtek.Pasien.Home.Articles;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.Pasien.Home.Doctors.DetailDokterFragment;
import com.example.medtek.Pasien.Model.ArtikelModel;
import com.example.medtek.R;
import com.example.medtek.Utils.RoundedCornersTransformation;
import com.example.medtek.Utils.CustomLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ArtikelAdapter extends RecyclerView.Adapter<ArtikelAdapter.ViewHolder> {

    private List<ArtikelModel> mList = new ArrayList<>();
    private Context mContext;
    private FragmentActivity mActivity;

    public ArtikelAdapter(List<ArtikelModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_artikel_kesehatan,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ArtikelModel model = mList.get(position);

        RelativeLayout.LayoutParams lf = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int margin_rl = (int) mActivity.getResources().getDimension(R.dimen.margin_16);
        int margin_rl_end = (int) (mActivity.getResources().getDimension(R.dimen.margin_16)-mActivity.getResources().getDimension(R.dimen.margin_8));
        lf.setMargins(margin_rl, 0, 0, 0);
        ll.setMargins(0, 0, margin_rl_end, 0);

        if (position==(getItemCount()-1)){
            holder.relativeLayout.setLayoutParams(ll);
        } else if(position==0){
            holder.relativeLayout.setLayoutParams(lf);
        }

        if (model.getImage_url().isEmpty() || model.getImage_url().equals("null")){
            Drawable drawable = mActivity.getDrawable(R.drawable.bg_dialog);
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable,Color.parseColor("#80878787"));
            holder.riv_artikel.setBackground(drawable);
        }else {
            try {
                final int radius = 36;
                final int margin = 0;
                final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                JSONObject obj = new JSONObject(model.getImage_url());
                Log.d("MODELARTIKEL",obj.getString("path"));
                Picasso.get().load("http://192.168.137.1:8000"+obj.getString("path")).transform(transformation).fit().centerCrop().into(holder.riv_artikel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        holder.tv_judul.setText(model.getJudul());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailArtikelFragment detailArtikelFragment = new DetailArtikelFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id_artikel",model.getId_artikel());
                detailArtikelFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frameFragment,detailArtikelFragment).addToBackStack("FragmentDetailArtikel");
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

        private TextView tv_judul;
        private ImageView riv_artikel;
        private RelativeLayout relativeLayout,rl_riv_artikel;

        public ViewHolder(View itemView) {
            super(itemView);
            riv_artikel = itemView.findViewById(R.id.riv_image_artikel);
            tv_judul = itemView.findViewById(R.id.tv_judul_artikel);
            relativeLayout = itemView.findViewById(R.id.rl_artikel);
            rl_riv_artikel = itemView.findViewById(R.id.rl_riv_artikel);
        }
    }
}
