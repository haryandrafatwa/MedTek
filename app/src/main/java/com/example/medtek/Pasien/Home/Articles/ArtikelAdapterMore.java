package com.example.medtek.Pasien.Home.Articles;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Model.ArtikelModel;
import com.example.medtek.R;
import com.example.medtek.Utils.RoundedCornersTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtikelAdapterMore extends RecyclerView.Adapter<ArtikelAdapterMore.ViewHolder> implements Filterable {

    private List<ArtikelModel> mList;
    private List<ArtikelModel> mListFull;
    private Context mContext;
    private FragmentActivity mActivity;
    private Date date;

    private int isNotFound;

    public ArtikelAdapterMore(List<ArtikelModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mListFull = new ArrayList<>(mList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_artikel,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ArtikelModel model = mList.get(position);

        if (mList!=null){
            holder.relativeLayout.setVisibility(View.VISIBLE);
            holder.rl_not_found.setVisibility(View.GONE);
            RelativeLayout.LayoutParams lf = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            int margin_rl = (int) mActivity.getResources().getDimension(R.dimen.margin_16);
            int margin_rl_bottom = (int) (mActivity.getResources().getDimension(R.dimen.margin_16)-mActivity.getResources().getDimension(R.dimen.margin_8));
            lf.setMargins(0, margin_rl, 0, 0);
            ll.setMargins(0, 0, 0, margin_rl_bottom);

            if(position==0){
                holder.relativeLayout.setLayoutParams(lf);
            } else if (position==(getItemCount()-1)){
                holder.relativeLayout.setLayoutParams(ll);
            }

            if (model.getImage_url().isEmpty() || model.getImage_url().equals("null")){
                Drawable drawable = mActivity.getDrawable(R.drawable.bg_dialog);
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, Color.parseColor("#80878787"));
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
            Locale locale = new Locale("in", "ID");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale);
            try {
                date = format.parse(model.getUpload_date());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String month_name = new SimpleDateFormat("MMMM", locale).format(calendar.getTime());
                holder.tv_desc.setText(model.getAuthor()+" • "+calendar.get(Calendar.DATE)+" "+month_name+" "+calendar.get(Calendar.YEAR));
            } catch (ParseException e) {
                e.printStackTrace();
            }
//        Log.d("ARTIKELADAPTERMORE","mList: "+mList.size()+"   |   mListFull: "+mListFull.size());

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
        }else{
            holder.rl_artikel_item.setVisibility(View.GONE);
            holder.rl_not_found.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public Filter getFilter() {
        return articlesFilter;
    }

    private Filter articlesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ArtikelModel> filteredArticles = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                filteredArticles.addAll(mListFull);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ArtikelModel itemArticle:mListFull){
                    if(itemArticle.getJudul().toLowerCase().contains(filterPattern)){
                        filteredArticles.add(itemArticle);
                    }
                }
            }


            FilterResults results = new FilterResults();
            results.values = filteredArticles;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList.clear();
            mList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //class utk menampung objek2 yang ada pada halaman item list recycler view
    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_judul,tv_desc;
        private ImageView riv_artikel;
        private RelativeLayout relativeLayout,rl_not_found,rl_artikel_item;

        public ViewHolder(View itemView) {
            super(itemView);
            riv_artikel = itemView.findViewById(R.id.riv_image_artikel);
            tv_judul = itemView.findViewById(R.id.tv_judul_artikel);
            tv_desc = itemView.findViewById(R.id.tv_desc);
            relativeLayout = itemView.findViewById(R.id.rl_artikel);
            rl_not_found = itemView.findViewById(R.id.rl_not_found);
            rl_artikel_item = itemView.findViewById(R.id.rl_item_artikel);
        }
    }

    public String getMonth(int month, Locale locale) {
        return new DateFormatSymbols(locale).getMonths()[month-1];
    }
}
