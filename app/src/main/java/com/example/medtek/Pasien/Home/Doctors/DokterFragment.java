package com.example.medtek.Pasien.Home.Doctors;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Home.Doctors.DokterAdapterMore;
import com.example.medtek.Pasien.Model.DokterModel;
import com.example.medtek.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DokterFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private ChipNavigationBar bottomNavigationView;
    private Toolbar toolbar;
    private EditText searchText;
    private ShimmerFrameLayout shimmerFrameLayout;

    private LinearLayout ll_popdok_loader;

    private List<DokterModel> mList = new ArrayList<>();;
    private RecyclerView.LayoutManager mLayoutManager;
    private DokterAdapterMore mAdapter;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_popular_doctors, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
            @Override
            public void onLayoutReady() {
                ll_popdok_loader.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                recyclerView.setVisibility(View.VISIBLE);
            }
        };

        Call<ResponseBody> callDokter = RetrofitClient.getInstance().getApi().getAllDokter();
        callDokter.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String dokterResult = response.body().string();
                    Log.e("MedTekMedTekMedTek",dokterResult);
                    JSONObject object = new JSONObject(dokterResult);
                    JSONArray array = new JSONArray(object.getString("data"));
                    mList.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jo = array.getJSONObject(i);
                        String name = jo.getString("name");
                        String email = jo.getString("email");
                        int isVerified;
                        if (!jo.getString("email_verified_at").equals(null)){
                            isVerified = 1;
                        }else{
                            isVerified = 0;
                        }
                        String path = "";
                        if (new JSONArray(jo.getString("image")).length() !=0){
                            JSONArray jsonArray = new JSONArray(jo.getString("image"));
                            path = "http://192.168.1.2:8000"+jsonArray.getJSONObject(0).getString("path");
                        }
                        JSONObject rsObject = new JSONObject(jo.getString("hospital"));
                        String rs_name = rsObject.getString("name");
                        JSONObject alamatObject = new JSONObject(jo.getString("alamat"));
                        String kelurahan = alamatObject.getString("kelurahan");
                        String kota = alamatObject.getString("kota");
                        String rs_loc = kelurahan+", "+kota;

                        JSONObject specObj = new JSONObject(jo.getString("specialization"));
                        String spec = specObj.getString("specialization");
                        int harga = jo.getInt("harga");
                        int id = jo.getInt("id");
                        int lamakerja = Integer.valueOf(jo.getInt("lama_kerja"));
                        float rating = Float.valueOf(jo.getString("rating"));
                        Log.e("MedTekMedTek",rating+"");
                        if (isVerified == 1){
                            mList.add(new DokterModel(id,name,email,spec,rs_name,rs_loc,path,harga,rating,isVerified,lamakerja));
//                            mAdapter.notifyDataSetChanged();
                        }
                        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                if(recyclerViewReadyCallback != null){
                                    recyclerViewReadyCallback.onLayoutReady();
                                }
                                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        });
                    }
                    Collections.reverse(mList);
                    Collections.sort(mList, Comparator.comparing(DokterModel::getRating).reversed().thenComparing(DokterModel::getHarga));
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_popdok_loader.setVisibility(View.GONE);
                    initRecyclerViewItem();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    callDokter.clone().enqueue(this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mAdapter != null){
                    mAdapter.getFilter().filter(s.toString().trim());
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null){
                    mAdapter.getFilter().filter(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mAdapter != null){
                    mAdapter.getFilter().filter(s.toString().trim());
                }
            }
        });
    }

    private void initialize(){
        shimmerFrameLayout = getActivity().findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.startShimmer();
        bottomNavigationView = getActivity().findViewById(R.id.bottomBar);
        bottomNavigationView.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        ll_popdok_loader = getActivity().findViewById(R.id.ll_popdok_loader);
        recyclerView = getActivity().findViewById(R.id.rv_popdr);
        searchText = getActivity().findViewById(R.id.et_search_dokter);
    }

    private void initRecyclerViewItem(){
        mAdapter = new DokterAdapterMore(mList,getActivity().getApplicationContext(),getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    private void setToolbar() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chevron_left, null);
        drawable.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(drawable);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

}
