package com.example.medtek.Pasien.Home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.Pasien.Model.DokterModel;
import com.example.medtek.Pasien.Model.SearchModel;
import com.example.medtek.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements Filterable {
    private List<SearchModel> mList;
    private List<SearchModel> mListFull;
    private Context mContext;
    private FragmentActivity mActivity;

    public SearchAdapter(List<SearchModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mListFull = new ArrayList<>(mList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_search,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SearchModel model = mList.get(position);
        holder.tv_name.setText(model.getNama());
        holder.tv_detail.setText(model.getDetail());
        if (model.getImage_url().equalsIgnoreCase("http://192.168.1.9:8000/storage/Dokter.png")){
            holder.circleImageView.setImageDrawable(mActivity.getDrawable(R.drawable.ic_dokter));
        }else{
            Picasso.get().load(R.drawable.ic_dokter).fit().into(holder.circleImageView);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public Filter getFilter() {
        return searchFilters;
    }

    private Filter searchFilters = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SearchModel> filteredSearch = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                filteredSearch.addAll(mListFull);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                String[] parts = filterPattern.split(", ");
                String city = "";
                String query = "";
                if (parts.length == 2){
                    city = parts[0];
                    query = parts[1];
                }else{
                    Log.e("TAG", "performFiltering: TEST ->"+parts[0]);
                    city = parts[0].split(",")[0];
                    query = "kosong";
                }
                Log.e("TAG", "performFiltering:"+city+" TEST ->"+query);
                if (city.equalsIgnoreCase("Semua")){
                    if (query.equalsIgnoreCase("Dokter") || query.equalsIgnoreCase("rs") || query.equalsIgnoreCase("doctor")
                            || query.equalsIgnoreCase("rumah sakit") || query.equalsIgnoreCase("hospital") || query.equalsIgnoreCase("kosong")){
                        filteredSearch.addAll(mList);
                        Log.e("TAG", "performFiltering: Semua All");
                    }else{
                        for (SearchModel itemSearch:mList){
                            if(itemSearch.getNama().toLowerCase().contains(query) || itemSearch.getDetail().toLowerCase().contains(query) ){
                                filteredSearch.add(itemSearch);
                            }
                        }
                        Log.e("TAG", "performFiltering: Query");
                    }
                }else{
                    for (SearchModel itemSearch:mList){
                        Log.e("TAG", "Item Location: "+itemSearch.getCity());
                        if (itemSearch.getCity().equalsIgnoreCase(city)){
                            if (query.equalsIgnoreCase("kosong") || query.equalsIgnoreCase("Dokter") || query.equalsIgnoreCase("rs") || query.equalsIgnoreCase("doctor")
                                    || query.equalsIgnoreCase("rumah sakit") || query.equalsIgnoreCase("hospital")){
                                filteredSearch.add(itemSearch);
                                Log.e("TAG", "performFiltering: Location All");
                            }else{
                                if(itemSearch.getNama().toLowerCase().contains(query) || itemSearch.getDetail().toLowerCase().contains(query) ){
                                    filteredSearch.add(itemSearch);
                                    Log.e("TAG", "performFiltering: Location Query");
                                }
                            }
                        }
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredSearch;
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

        private TextView tv_name, tv_detail;
        private CircleImageView circleImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_detail = itemView.findViewById(R.id.tv_detail);
            circleImageView = itemView.findViewById(R.id.circleImageView);
        }
    }

}
