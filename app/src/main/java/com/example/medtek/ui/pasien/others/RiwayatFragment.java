package com.example.medtek.ui.pasien.others;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.BuildConfig;
import com.example.medtek.R;
import com.example.medtek.model.pasien.RiwayatModel;
import com.example.medtek.model.pasien.TransactionModel;
import com.example.medtek.network.RetrofitClient;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.USER_TYPE;
import static com.example.medtek.utils.PropertyUtil.getData;

public class RiwayatFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private String access, refresh;
    private int role;

    private RelativeLayout rl_empty_history;
    private final List<RiwayatModel> mList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private RiwayatAdapter mAdapter;
    private RecyclerView rv_riwayat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_riwayat, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        AndroidThreeTen.init(getActivity());

        loadData(getActivity());

        Call<ResponseBody> getJanji = RetrofitClient.getInstance().getApi().getUserJanji("Bearer "+access);
        getJanji.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() != null){
                    if (response.isSuccessful()){
                        try {
                            String s = response.body().string();
                            JSONObject obj = new JSONObject(s);
                            JSONArray jsonArray = obj.getJSONArray("data");

                            if (jsonArray.length() > 0){
                                mList.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    int id = jsonObject.getInt("id");
                                    String tgl = jsonObject.getString("tglJanji");
                                    Date date;
                                    Locale locale = new Locale("in", "ID");
                                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", locale);
                                    try {
                                        date = format.parse(tgl);
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(date);
                                        String month_name = new SimpleDateFormat("MMMM", locale).format(calendar.getTime());
                                        tgl = String.format("%02d",calendar.get(Calendar.DATE))+" "+month_name+" "+calendar.get(Calendar.YEAR);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    int idStatus = jsonObject.getInt("idStatus");
                                    String detailJanji = jsonObject.getString("detailJanji");
                                    String nama = "";
                                    String data = "";
                                    String path = "";
                                    if (role == 2){
                                        JSONObject pasien = jsonObject.getJSONObject("pasien");
                                        nama = pasien.getString("name");
                                        data = pasien.getString("email");
                                        path="";
                                        JSONArray jsonArrayImage = pasien.getJSONArray("image");
                                        if (jsonArrayImage.length() !=0){
                                            for (int j = 0; j < jsonArrayImage.length(); j++) {
                                                JSONObject imageObj = jsonArrayImage.getJSONObject(j);
                                                if (imageObj.getInt("type_id") == 1) {
                                                    path = BASE_URL + imageObj.getString("path");
                                                    break;
                                                }else{
                                                    path = "pasien";
                                                }
                                            }
                                        }else{
                                            path = "pasien";
                                        }
                                    }else{
                                        JSONObject dokter = jsonObject.getJSONObject("dokter");
                                        nama = dokter.getString("name");
                                        data = dokter.getJSONObject("specialization").getString("specialization");
                                        path="";
                                        JSONArray jsonArrayImage = dokter.getJSONArray("image");
                                        if (jsonArrayImage.length() !=0){
                                            for (int j = 0; j < jsonArrayImage.length(); j++) {
                                                JSONObject imageObj = jsonArrayImage.getJSONObject(j);
                                                if (imageObj.getInt("type_id") == 1) {
                                                    path = BASE_URL + imageObj.getString("path");
                                                    break;
                                                }else{
                                                    path = "dokter";
                                                }
                                            }
                                        }else{
                                            path = "dokter";
                                        }
                                    }
                                    int harga = jsonObject.getJSONArray("transaksi").getJSONObject(0).getInt("totalHarga");
                                    mList.add(new RiwayatModel(id,idStatus,harga,tgl,nama,data,detailJanji,path));
                                }
                                initRecyclerViewItem();
                                rl_empty_history.setVisibility(View.GONE);
                                rv_riwayat.setVisibility(View.VISIBLE);
                            }else{
                                rl_empty_history.setVisibility(View.VISIBLE);
                                rv_riwayat.setVisibility(View.GONE);
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void initialize() {

        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        rl_empty_history = getActivity().findViewById(R.id.layout_empty);

    }

    private void setFragment(Fragment fragment,String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    private void initRecyclerViewItem(){
        rv_riwayat = getActivity().findViewById(R.id.rv_riwayat);
        mAdapter = new RiwayatAdapter(mList,getActivity().getApplicationContext(),getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        rv_riwayat.setAdapter(mAdapter);
    }

    private void setToolbar() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chevron_left, null);
        drawable.setColorFilter(new PorterDuffColorFilter(getActivity().getColor(R.color.white), PorterDuff.Mode.SRC_IN));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(drawable);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
        this.role = (Integer) getData(USER_TYPE);
    }

}
