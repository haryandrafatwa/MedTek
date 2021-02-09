package com.example.medtek.ui.pasien.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.pasien.ArtikelModel;
import com.example.medtek.model.pasien.DokterModel;
import com.example.medtek.model.pasien.HospitalModel;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.service.NotificationService;
import com.example.medtek.ui.pasien.home.articles.ArtikelAdapter;
import com.example.medtek.ui.pasien.home.articles.ArtikelFragment;
import com.example.medtek.ui.pasien.home.doctors.DokterAdapter;
import com.example.medtek.ui.pasien.home.doctors.DokterFragment;
import com.example.medtek.ui.pasien.home.hospitals.HospitalAdapter;
import com.example.medtek.ui.pasien.home.hospitals.HospitalFragment;
import com.example.medtek.ui.pasien.others.NominalFragment;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.medtek.network.RetrofitClient.BASE_SOCKET_URL;
import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.TAG;
import static java.lang.String.valueOf;

public class HomeFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private final ArrayList<ArtikelModel> mListArtikel = new ArrayList<>();
    private final ArrayList<ArtikelModel> mListArtikelReverse = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManagerArtikel;
    private RecyclerView.Adapter mAdapterArtikel;
    private RecyclerView rv_artikel;

    private final ArrayList<DokterModel> mListDokter = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManagerDokter;
    private RecyclerView.Adapter mAdapterADokter;
    private RecyclerView rv_dokter;

    private final ArrayList<HospitalModel> mListHospital = new ArrayList<>();
    private final ArrayList<DokterModel> mListDokterHospital = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManagerHospital;
    private RecyclerView.Adapter mAdapterHospital;
    private RecyclerView rv_hospital;

    private ChipNavigationBar bottomNavigationView;
    private ShimmerFrameLayout shimmerFrameLayout;
    private String access, refresh,  kelurahan="", kecamatan="", kota="", provinsi="", jalan="";
    private RelativeLayout rl_content;
    private LinearLayout ll_loader;

    private FusedLocationProviderClient fusedLocationProviderClient;
    public static int REQUEST_CODE_LOC = 46;
    private Location location;
    private double sLat, sLong;

    private RelativeLayout rl_header;
    private LinearLayout ll_search;
    private String userName, userImagePath;
    private CircleImageView circleImageView;
    private TextView tv_name, tv_welcome, placeholderSearch;
    private ImageView iv_search;

    private LinearLayout ll_wallet, ll_wallet_title;
    private String mySaldo;
    private TextView tv_saldo, tv_saldo_user, tv_topup, tv_withdraw, tv_history;
    private ImageButton ib_topup, ib_withdraw, ib_history;

    private TextView tv_rs_rekomendasi, tv_seers;
    private LinearLayout ll_rekrs;
    private TextView tv_rs_empty;
    private RelativeLayout rl_hospital_empty;
    private CircleImageView civ_rs_empty;

    private TextView tv_dr_rekomendasi, tv_seedr;
    private LinearLayout ll_popdok;

    private TextView tv_artikel, tv_seeartikel;
    private LinearLayout ll_artikel;

    private Socket socket;
    private final String SERVER_URL = "http://192.168.137.1:6001";
    private String CHANNEL_MESSAGES="";
    private int idDokter=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
//        skeletonScreen = Skeleton.bind(this.getView()).load(R.layout.fragment_home).show();
        initialize();

        initRecyclerViewItem();
        recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
            @Override
            public void onLayoutReady() {
            }
        };

        loadData(getActivity());
        Call<ResponseBody> getJanji = RetrofitClient.getInstance().getApi().getUserJanji("Bearer "+access);
        getJanji.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            String s = response.body().string();
                            JSONObject janjiObject = new JSONObject(s);
                            if(!janjiObject.has("error")){
                                JSONArray janjiArr = janjiObject.getJSONArray("data");
                                for (int i = 0; i < janjiArr.length(); i++) {
                                    JSONObject janjiObj = janjiArr.getJSONObject(i);
                                    if (janjiObj.getInt("idStatus") == 1){
                                        idDokter = janjiObj.getInt("id");
                                        Log.e("TAG", "onResponse: "+idDokter);
                                        startSocket();
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    getJanji.clone().enqueue(this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        Call<ResponseBody> callArticles = RetrofitClient.getInstance().getApi().getAllArticles();
        callArticles.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    String s = response.body().string().trim();
                    JSONObject object = new JSONObject(s);
                    JSONArray array = new JSONArray(object.getString("data"));
                    mListArtikel.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jo = array.getJSONObject(i);
                        String author = jo.getString("author");
                        JSONObject authorObject = new JSONObject(author);
                        String authorName = authorObject.getString("name");
                        String topic = jo.getString("topic");
                        JSONObject topicObject = new JSONObject(topic);
                        String topicName = topicObject.getString("topic");
                        int id_artikel = jo.getInt("id");
                        String judul = jo.getString("judul");
                        String isi = jo.getString("isi");
                        String slug = jo.getString("slug");
                        String image_url = jo.getString("image");
                        String upload_date = jo.getString("created_at");
                        boolean isVerified = jo.getBoolean("is_verified");
                        if (isVerified){
                            mListArtikel.add(new ArtikelModel(id_artikel,judul,isi,slug,image_url,upload_date,authorName,topicName));
                            mAdapterArtikel.notifyDataSetChanged();
                        }
                        rv_artikel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                if(recyclerViewReadyCallback != null){
                                    recyclerViewReadyCallback.onLayoutReady();
                                }
                                rv_artikel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        });
                    }
                    Collections.reverse(mListArtikel);
                    mListArtikelReverse.clear();
                    for (int i = 0; i < 5; i++) {
                        mListArtikelReverse.add(mListArtikel.get(i));
                    }

                    Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
                    callUser.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()){
                                try {
                                    String s = response.body().string();
                                    JSONObject obj = new JSONObject(s);
                                    userName = obj.getString("name");
                                    JSONArray jsonArray = new JSONArray(obj.getString("image"));
                                    if (jsonArray.length() == 0){
                                        userImagePath = "/storage/Pasien.png";
                                    }else{
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject imageObj = jsonArray.getJSONObject(i);
                                            if (imageObj.getInt("type_id") == 1){
                                                if (imageObj.getString("path").equalsIgnoreCase("/storage/Pasien.png")){
                                                    userImagePath = "/storage/Pasien.png";
                                                }else{
                                                    userImagePath = jsonArray.getJSONObject(0).getString("path");
                                                    break;
                                                }
                                            }else{
                                                userImagePath = "/storage/Pasien.png";
                                            }
                                        }
                                    }

                                    Call<ResponseBody> callWallet = RetrofitClient.getInstance().getApi().getUserWallet("Bearer "+access);
                                    callWallet.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.isSuccessful()){
                                                try {
                                                    String s = response.body().string();
                                                    JSONObject obj = new JSONObject(s);
                                                    if (!obj.has("message")){
                                                        JSONObject jsonObject = new JSONObject(obj.getString("data"));
                                                        mySaldo = NumberFormat.getInstance(Locale.ITALIAN).format(Integer.valueOf(jsonObject.getString("balance")));
                                                    }else{
                                                        mySaldo = "-";
                                                    }

                                                    Call<ResponseBody> callDokter = RetrofitClient.getInstance().getApi().getDokterRated();
                                                    callDokter.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                            try {
                                                                String dokterResult = response.body().string();
                                                                JSONObject object = new JSONObject(dokterResult);
                                                                JSONArray array = new JSONArray(object.getString("data"));
                                                                mListDokter.clear();
                                                                for (int i = 0; i < array.length(); i++) {
                                                                    JSONObject jo = array.getJSONObject(i);
                                                                    String name = jo.getString("name");
                                                                    String email = jo.getString("email");
                                                                    int isVerified;
                                                                    if (!jo.isNull("email_verified_at")){
                                                                        isVerified = 1;
                                                                    }else{
                                                                        isVerified = 0;
                                                                    }
                                                                    String path = "";
                                                                    if (new JSONArray(jo.getString("image")).length() !=0){
                                                                        JSONArray jsonArray = new JSONArray(jo.getString("image"));
                                                                        path = BASE_URL+jsonArray.getJSONObject(0).getString("path");
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
                                                                    if (isVerified == 1){
                                                                        mListDokter.add(new DokterModel(id,name,email,spec,rs_name,rs_loc,path,harga,rating,isVerified,lamakerja));
                                                                        mAdapterADokter.notifyDataSetChanged();
                                                                    }
                                                                    rv_dokter.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                                        @Override
                                                                        public void onGlobalLayout() {
                                                                            if(recyclerViewReadyCallback != null){
                                                                                recyclerViewReadyCallback.onLayoutReady();
                                                                            }
                                                                            rv_dokter.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                                                        }
                                                                    });
                                                                }

                                                                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                                                                    getLocation();
                                                                    Call<ResponseBody> callHospital = RetrofitClient.getInstance().getApi().getAllHospital();
                                                                    callHospital.enqueue(new Callback<ResponseBody>() {
                                                                        @Override
                                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                            try {
                                                                                String s = response.body().string();
                                                                                JSONObject object = new JSONObject(s);
                                                                                JSONArray array = new JSONArray(object.getString("data"));
                                                                                mListHospital.clear();
                                                                                for (int i = 0; i < array.length(); i++) {
                                                                                    JSONObject obj = array.getJSONObject(i);
                                                                                    int id = obj.getInt("id");
                                                                                    String name = obj.getString("name");
                                                                                    String no_telp = obj.getString("notelp");
                                                                                    JSONObject alamat = obj.getJSONObject("alamat");
                                                                                    String jalanRs = alamat.getString("jalan");
                                                                                    String no_bangunanRs = alamat.getString("nomor_bangunan");
                                                                                    String rtrwRs = alamat.getString("rtrw");
                                                                                    String kelurahanRs = alamat.getString("kelurahan");
                                                                                    String kecamatanRs = alamat.getString("kecamatan");
                                                                                    String kotaRs = alamat.getString("kota");
                                                                                    String infoRs = obj.getString("info");
                                                                                    JSONArray arrDokter = obj.getJSONArray("dokter");
                                                                                    String location = "Jl. "+jalanRs+", No. "+no_bangunanRs+", Rt/Rw. "+rtrwRs+", Kelurahan "+kelurahanRs+", Kecamatan "+kecamatanRs+", Kota "+kotaRs;
                                                                                    String hospitalImage="";
                                                                                    if (!obj.getString("image").equals("null")){
                                                                                        hospitalImage = BASE_URL+obj.getString("image");
                                                                                    }else{
                                                                                        hospitalImage = BASE_URL+"/storage/Hospital.png";
                                                                                    }
                                                                                    mListDokterHospital.clear();
                                                                                    for (int j = 0; j < arrDokter.length(); j++) {
                                                                                        JSONObject jo = arrDokter.getJSONObject(j);
                                                                                        String nameDokter = jo.getString("name");
                                                                                        String email = jo.getString("email");
                                                                                        int isVerified;
                                                                                        if (!jo.getString("email_verified_at").equals(null)){
                                                                                            isVerified = 1;
                                                                                        }else{
                                                                                            isVerified = 0;
                                                                                        }
                                                                                        String path = "";
                                                                                        if (jo.has("image")){
                                                                                            path = BASE_URL+jo.getString("image");
                                                                                        }else{
                                                                                            path = BASE_URL+"/storage/Hospital.png";
                                                                                        }
                                                                                        int harga = jo.getInt("harga");
                                                                                        int idDokter = jo.getInt("id");
                                                                                        int lamakerja = Integer.valueOf(jo.getInt("lama_kerja"));
                                                                                        float rating = Float.valueOf(jo.getString("rating"));
                                                                                        String spec = "Interventional Cardiology";
                                                                                        if (isVerified == 1) {
                                                                                            mListDokterHospital.add(new DokterModel(idDokter, nameDokter, email, spec, name, location, path, harga, rating, isVerified, lamakerja));
                                                                                        }
                                                                                    }
                                                                                    if (!kota.isEmpty() && mListHospital.size() < 5){
                                                                                        double distance = -1.0;
                                                                                        Locale locale = new Locale("in", "ID");
                                                                                        Geocoder geocoder = new Geocoder(getActivity(), locale);

                                                                                        List<Address> addressesD = geocoder.getFromLocationName(name,1);
                                                                                        if (addressesD.size() > 0) {
                                                                                            double dLat = addressesD.get(0).getLatitude();
                                                                                            double dLong = addressesD.get(0).getLongitude();
                                                                                            double longDiff = sLong - dLong;
                                                                                            distance = Math.sin(sLat*Math.PI/180.0)*Math.sin(dLat*Math.PI/180.0) + Math.cos(sLat*Math.PI/180.0)*Math.cos(dLat*Math.PI/180.0)*Math.cos(longDiff*Math.PI/180.0);
                                                                                            distance = Math.acos(distance);
                                                                                            distance = distance*180.0/Math.PI;
                                                                                            distance = distance*60*1.1515;
                                                                                            distance = distance*1.609344;
                                                                                        }
                                                                                        String jenis="";
                                                                                        if (obj.getString("jenis").equalsIgnoreCase("umum")){
                                                                                            jenis = getActivity().getString(R.string.rsumum);
                                                                                        }else if (obj.getString("jenis").equalsIgnoreCase("spesialisasi")){
                                                                                            jenis = getActivity().getString(R.string.rsspesial);
                                                                                        }
                                                                                        if (distance < 20 && distance >= 0){
                                                                                            mListHospital.add(new HospitalModel(name, no_telp, jalanRs, no_bangunanRs, rtrwRs, kelurahanRs, kecamatanRs, kotaRs, "Provinsi", infoRs,jenis,hospitalImage,id,distance, mListDokterHospital));
                                                                                        }
                                                                                    }
                                                                                }
                                                                                Collections.sort(mListHospital, Comparator.comparing(HospitalModel::getDistance));
                                                                                if(location==null){
                                                                                    rl_hospital_empty.setVisibility(View.VISIBLE);
                                                                                    tv_rs_empty.setText(getActivity().getString(R.string.locationNotFound));
                                                                                }else if (mListHospital.isEmpty()){
                                                                                    Picasso.get().load(getActivity().getString(R.string.hospitalURL)).fit().centerCrop().into(civ_rs_empty);
                                                                                    rl_hospital_empty.setVisibility(View.VISIBLE);
                                                                                    tv_rs_empty.setText(getActivity().getString(R.string.emptyHospital));
                                                                                }
                                                                                if (mListDokter.size() >= 5){
                                                                                    tv_seedr.setVisibility(View.VISIBLE);
                                                                                }
                                                                                if (mListArtikelReverse.size() >= 5){
                                                                                    tv_seeartikel.setVisibility(View.VISIBLE);
                                                                                }
                                                                                if (mListHospital.size() >= 3){
                                                                                    tv_seers.setVisibility(View.VISIBLE);
                                                                                }
                                                                                rl_content.setVisibility(View.VISIBLE);
                                                                                ll_loader.setVisibility(View.GONE);

                                                                                rv_dokter.setVisibility(View.VISIBLE);
                                                                                rv_artikel.setVisibility(View.VISIBLE);
                                                                                rv_hospital.setVisibility(View.VISIBLE);

                                                                                rl_header.setBackground(getActivity().getDrawable(R.drawable.bg_home_bottom_rounded));
                                                                                tv_welcome.setBackground(null);
                                                                                tv_welcome.setText(getString(R.string.welcomeback));
                                                                                tv_name.setBackground(null);
                                                                                tv_name.setText(userName);
                                                                                circleImageView.setBackground(null);
                                                                                Picasso.get().load(BASE_URL+userImagePath).into(circleImageView);
                                                                                ll_search.setBackground(getActivity().getDrawable(R.drawable.bg_search));
                                                                                placeholderSearch.setVisibility(View.VISIBLE);
                                                                                iv_search.setVisibility(View.VISIBLE);

                                                                                ll_wallet.setBackground(getActivity().getDrawable(R.drawable.bg_home_all_rounded));
                                                                                ll_wallet_title.setBackground(getActivity().getDrawable(R.drawable.bg_home_top_rounded));
                                                                                tv_saldo.setBackground(null);
                                                                                tv_saldo_user.setBackground(null);
                                                                                tv_saldo.setText(getString(R.string.saldo));
                                                                                tv_saldo_user.setText("Rp"+mySaldo);
                                                                                ib_topup.setBackground(getActivity().getDrawable(R.drawable.ic_top_up));
                                                                                tv_topup.setBackground(null);
                                                                                tv_topup.setText(getString(R.string.topup));
                                                                                ib_withdraw.setBackground(getActivity().getDrawable(R.drawable.ic_withdraw));
                                                                                tv_withdraw.setBackground(null);
                                                                                tv_withdraw.setText(getString(R.string.withdraw));
                                                                                ib_history.setBackground(getActivity().getDrawable(R.drawable.ic_history_wallet));
                                                                                tv_history.setBackground(null);
                                                                                tv_history.setText(getString(R.string.history));

                                                                                tv_rs_rekomendasi.setBackground(null);
                                                                                tv_rs_rekomendasi.setText(R.string.rsrekomendasi);
                                                                                ll_rekrs.setVisibility(View.GONE);

                                                                                tv_dr_rekomendasi.setBackground(null);
                                                                                tv_dr_rekomendasi.setText(R.string.drpopuler);
                                                                                ll_popdok.setVisibility(View.GONE);

                                                                                tv_artikel.setBackground(null);
                                                                                tv_artikel.setText(R.string.artikelkesehatan);
                                                                                ll_artikel.setVisibility(View.GONE);

                                                                                shimmerFrameLayout.stopShimmer();
                                                                            } catch (IOException | JSONException e) {
                                                                                e.printStackTrace();
                                                                                callHospital.clone().enqueue(this);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                                        }
                                                                    });
                                                                }else{
                                                                    ActivityCompat.requestPermissions(getActivity(), new String[]{
                                                                            Manifest.permission.ACCESS_FINE_LOCATION
                                                                    }, REQUEST_CODE_LOC);
                                                                }
                                                            } catch (IOException | JSONException e) {
                                                                e.printStackTrace();
                                                                callDokter.clone().enqueue(this);
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                        }
                                                    });
                                                } catch (JSONException | IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Toasty.info(getActivity(), t.getMessage());
                                        }
                                    });
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toasty.info(getActivity(), t.getMessage());
                        }
                    });

                } catch (IOException | JSONException e) {
                    callArticles.clone().enqueue(this);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        tv_seeartikel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArtikelFragment artikelFragment = new ArtikelFragment();
                setFragment(artikelFragment,"FragmentArtikel");
            }
        });

        tv_seedr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DokterFragment dokterFragment = new DokterFragment();
                setFragment(dokterFragment,"FragmentPopulerDokter");
            }
        });

        tv_seers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HospitalFragment hospitalFragment = new HospitalFragment();
                setFragment(hospitalFragment,"FragmentRekomendasiRS");
            }
        });

        ib_topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NominalFragment nominalFragment = new NominalFragment();
                setFragment(nominalFragment, "FragmentNominal");
            }
        });

        tv_topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_topup.performClick();
            }
        });

    }

    private void startSocket() {
        Log.e("MedTek", "SOCOKET START...");
        try {
//            IO.Options opts = new IO.Options();
//            opts.transports = new String[] { WebSocket.NAME };
            socket = IO.socket(BASE_SOCKET_URL);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("SOCKETSOCKETAN", "Connected!");
                    JSONObject object = new JSONObject();
                    JSONObject auth = new JSONObject();
                    JSONObject headers = new JSONObject();

                    try {
                        object.put("channel", "private-App.User.Janji."+idDokter);
                        object.put("name", "subscribe");

                        headers.put("Authorization", "Bearer " + access);
                        auth.put("headers", headers);
                        object.put("auth", auth);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    socket.emit("subscribe", object, new Ack() {
                        @Override
                        public void call(Object... args) {
                            String messageJson = args[1].toString();
                            Log.e("MedTek", "onResponse: "+messageJson);
                        }
                    });
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("SOCKETSOCKETAN", "Error!");
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("SOCKETSOCKETAN", "Connect Error!" + args[0].toString());
                }
            }).on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Transport transport = (Transport) args[0];
                    // Adding headers when EVENT_REQUEST_HEADERS is called
                    transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.v(TAG(HomeFragment.class), "Caught EVENT_REQUEST_HEADERS after EVENT_TRANSPORT, adding headers");
                            Map<String, List<String>> mHeaders = (Map<String, List<String>>)args[0];
                            mHeaders.put("Authorization", Arrays.asList("Bearer " + access));
                        }
                    });
                }
            }).on("janji-update", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        String s = args[1].toString();
                        JSONObject jsonObject = new JSONObject(s);
                        JSONObject janjiObj = jsonObject.getJSONObject("janji");
                        Call<ResponseBody> getDokter = RetrofitClient.getInstance().getApi().getDokterId(janjiObj.getInt("idDokter"));
                        getDokter.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    if (response.isSuccessful()){
                                        if (response.body() != null){
                                            String s = response.body().string();
                                            JSONObject dataDokter = new JSONObject(s);
                                            String drName = dataDokter.getJSONObject("data").getString("name");

                                            if (jsonObject.getString("message").equalsIgnoreCase("janji declined")){
                                                CHANNEL_MESSAGES = "Yahh, sepertinya Dr. "+drName+" sedang sibuk. Janji kamu dibatalkan.";
                                                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Janji Ditolak!")
                                                        .setContentText("Dokter sedang sibuk, sehingga janji anda dibatalkan. Silahkan cek saldo anda anda pada halaman wallet.")
                                                        .setConfirmButtonBackgroundColor(Color.parseColor("#2196F3"))
                                                        .show();
                                            }else if (jsonObject.getString("message").equalsIgnoreCase("Janji Queued")){
                                                CHANNEL_MESSAGES = drName+" telah mengkonfirmasi janji anda, mohon tunggu.";
                                                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Janji Diterima!")
                                                        .setContentText("Janji anda telah diterima, silahkan menunggu antrian pada halaman chat")
                                                        .setConfirmButtonBackgroundColor(Color.parseColor("#2196F3"))
                                                        .show();
                                            }
                                            startService();
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
                        Log.e("TAG", "call: "+s );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            socket.connect();

        } catch (URISyntaxException e) {
            Log.e("MedTek", "ECHO ERROR");
            e.printStackTrace();
        }
    }

    private void startService() throws JSONException {
        Intent serviceIntent = new Intent(getActivity(), NotificationService.class);
        serviceIntent.putExtra("data",CHANNEL_MESSAGES);
        getActivity().startService(serviceIntent);
    }

    private void initialize(){

        bottomNavigationView = getActivity().findViewById(R.id.bottomBar);
        bottomNavigationView.setVisibility(View.VISIBLE);
        setStatusBar();

        shimmerFrameLayout = getActivity().findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.startShimmer();

        rl_content = getActivity().findViewById(R.id.layout_visible);
        ll_loader = getActivity().findViewById(R.id.layout_loader);

        rl_header = getActivity().findViewById(R.id.header);
        ll_search = getActivity().findViewById(R.id.layout_search);
        circleImageView = getActivity().findViewById(R.id.civ_user);
        iv_search = getActivity().findViewById(R.id.iv_search);
        tv_welcome = getActivity().findViewById(R.id.tv_welcome);
        tv_name = getActivity().findViewById(R.id.tv_nama_user);
        placeholderSearch = getActivity().findViewById(R.id.placeholderSearch);

        ll_wallet = getActivity().findViewById(R.id.wallet);
        ll_wallet_title = getActivity().findViewById(R.id.ll_wallet_title);
        tv_saldo = getActivity().findViewById(R.id.tv_saldo);
        tv_saldo_user = getActivity().findViewById(R.id.saldo_user);
        ib_topup = getActivity().findViewById(R.id.ib_topup);
        tv_topup = getActivity().findViewById(R.id.tv_topup);
        ib_withdraw = getActivity().findViewById(R.id.ib_withdraw);
        tv_withdraw = getActivity().findViewById(R.id.tv_withdraw);
        ib_history = getActivity().findViewById(R.id.ib_history);
        tv_history = getActivity().findViewById(R.id.tv_history);

        tv_rs_rekomendasi = getActivity().findViewById(R.id.tv_rekrs);
        tv_seers = getActivity().findViewById(R.id.tv_seerekrs);
        ll_rekrs = getActivity().findViewById(R.id.ll_rekrsk_loader);
        tv_rs_empty = getActivity().findViewById(R.id.tv_hospital_empty);
        rl_hospital_empty = getActivity().findViewById(R.id.layout_empty_hospital);
        civ_rs_empty = getActivity().findViewById(R.id.civ_rs_empty);

        tv_dr_rekomendasi = getActivity().findViewById(R.id.tv_rekdr);
        tv_seedr = getActivity().findViewById(R.id.tv_seerekdr);
        ll_popdok = getActivity().findViewById(R.id.ll_popdok_loader);

        tv_artikel = getActivity().findViewById(R.id.tv_artikel);
        tv_seeartikel = getActivity().findViewById(R.id.tv_seeartikel);
        ll_artikel = getActivity().findViewById(R.id.ll_artikel_loader);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        ll_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment searchFragment = new SearchFragment();
                setFragment(searchFragment,"FragmentSearch");
            }
        });
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_search.performClick();
            }
        });
        placeholderSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_search.performClick();
            }
        });
    }

    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                location = task.getResult();
                if (location!=null){
                    try {
                        Locale locale = new Locale("in", "ID");
                        Geocoder geocoder = new Geocoder(getActivity(), locale);
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        Log.d("errorLoc", valueOf(addresses.size()));
//                        Log.d("errorLoc", addresses.get(0).getLocality());
//                        Log.d("errorLoc", addresses.get(0).getSubLocality());
//                        Log.d("errorLoc", addresses.get(0).getSubAdminArea());
//                        Log.d("errorLoc", addresses.get(0).getAdminArea());
                        Log.d("errorLoc", valueOf(addresses.get(0).getThoroughfare() == null));
//                        Log.d("errorLoc", valueOf(addresses.get(0).getLatitude()));
//                        Log.d("errorLoc", valueOf(addresses.get(0).getLongitude()));
//                        kecamatan = addresses.get(0).getLocality().split(" ",2)[1];
                        kecamatan = addresses.get(0).getLocality();
                        kelurahan = addresses.get(0).getSubLocality();
                        kota = addresses.get(0).getSubAdminArea().split(" ",2)[1];
                        provinsi = addresses.get(0).getAdminArea();
                        jalan = (addresses.get(0).getThoroughfare() == null) ? "" : addresses.get(0).getThoroughfare().split(" ",2)[1];
                        sLat = addresses.get(0).getLatitude();
                        sLong = addresses.get(0).getLongitude();
//                        Toasty.info(getActivity(),"Kecamatan: "+kecamatan+", Kelurahan: "+kelurahan).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        fusedLocationProviderClient.getLastLocation();
                    }
                }
            }
        });
    }

    private void initRecyclerViewItem(){
        rv_artikel = getActivity().findViewById(R.id.rv_artikel);
        mAdapterArtikel = new ArtikelAdapter(mListArtikelReverse,getActivity().getApplicationContext(),getActivity());
        mLayoutManagerArtikel = new LinearLayoutManager(getActivity(),RecyclerView.HORIZONTAL,false);
        rv_artikel.setLayoutManager(mLayoutManagerArtikel);
        rv_artikel.setAdapter(mAdapterArtikel);

        rv_dokter = getActivity().findViewById(R.id.rv_popdr);
        mAdapterADokter = new DokterAdapter(mListDokter,getActivity().getApplicationContext(),getActivity());
        mLayoutManagerDokter = new LinearLayoutManager(getActivity(),RecyclerView.HORIZONTAL,false);
        rv_dokter.setLayoutManager(mLayoutManagerDokter);
        rv_dokter.setAdapter(mAdapterADokter);

        rv_hospital = getActivity().findViewById(R.id.rv_rekrs);
        mAdapterHospital = new HospitalAdapter(mListHospital,getActivity().getApplicationContext(),getActivity());
        mLayoutManagerHospital = new LinearLayoutManager(getActivity(),RecyclerView.HORIZONTAL,false);
        rv_hospital.setLayoutManager(mLayoutManagerHospital);
        rv_hospital.setAdapter(mAdapterHospital);
    }

    private void setStatusBar(){
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

    private void setFragment(Fragment fragment,String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOC) {

        }
    }
}
