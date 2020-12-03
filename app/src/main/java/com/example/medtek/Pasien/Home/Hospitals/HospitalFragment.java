package com.example.medtek.Pasien.Home.Hospitals;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Home.Doctors.DokterAdapterMore;
import com.example.medtek.Pasien.Model.DokterModel;
import com.example.medtek.Pasien.Model.HospitalModel;
import com.example.medtek.R;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private EditText searchText;

    private LinearLayout ll_rekrs_loader;
    private RelativeLayout rl_empty_rekrs;
    private CircleImageView civ_rs;
    private TextView tv_empty;

    private List<HospitalModel> mList = new ArrayList<>();
    private List<DokterModel> mListDokter = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private HospitalAdapterMore mAdapter;
    private RecyclerView recyclerView;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private int REQUEST_CODE = 46;
    private Location location;
    private double sLat, sLong;
    private String kelurahan="", kecamatan="", kota="", provinsi="", jalan="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hospital_recomendations, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
            @Override
            public void onLayoutReady() {
                ll_rekrs_loader.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        };

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
                        mList.clear();
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
                                hospitalImage = "http://192.168.137.1:8000"+obj.getString("image");
                            }else{
                                hospitalImage = "http://192.168.137.1:8000/storage/Hospital.png";
                            }
                            mListDokter.clear();
                            for (int j = 0; j < arrDokter.length(); j++) {
                                JSONObject jo = arrDokter.getJSONObject(i);
                                String nameDokter = jo.getString("name");
                                String email = jo.getString("email");
                                int isVerified;
                                if (!jo.getString("email_verified_at").equals(null)){
                                    isVerified = 1;
                                }else{
                                    isVerified = 0;
                                }
                                String path = "";
                                if (jo.getJSONArray("image").length()!=0){
                                    JSONArray jsonArray = new JSONArray(jo.getString("image"));
                                    path = "http://192.168.137.1:8000"+jsonArray.getJSONObject(0).getString("path");
                                }else{
                                    path = "http://192.168.137.1:8000/storage/Hospital.png";
                                }
                                int harga = jo.getInt("harga");
                                int idDokter = jo.getInt("id");
                                int lamakerja = Integer.valueOf(jo.getInt("lama_kerja"));
                                float rating = Float.valueOf(jo.getString("rating"));
                                String spec = "Interventional Cardiology";
                                if (isVerified == 1) {
                                    mListDokter.add(new DokterModel(idDokter, nameDokter, email, spec, name, location, path, harga, rating, isVerified, lamakerja));
                                }
                            }
                            if (!kota.isEmpty() && mList.size() < 5){
                                double distance;
                                Locale locale = new Locale("in", "ID");
                                Geocoder geocoder = new Geocoder(getActivity(), locale);

                                List<Address> addressesD = geocoder.getFromLocationName(name,1);
                                double dLat = addressesD.get(0).getLatitude();
                                double dLong = addressesD.get(0).getLongitude();
                                Log.e("SOURCELONGANDLAT","sLat: "+sLat+", sLong: "+sLong);
                                double longDiff = sLong - dLong;
                                distance = Math.sin(sLat*Math.PI/180.0)*Math.sin(dLat*Math.PI/180.0) + Math.cos(sLat*Math.PI/180.0)*Math.cos(dLat*Math.PI/180.0)*Math.cos(longDiff*Math.PI/180.0);
                                distance = Math.acos(distance);
                                distance = distance*180.0/Math.PI;
                                distance = distance*60*1.1515;
                                distance = distance*1.609344;
                                String jenis="";
                                if (obj.getString("jenis").equalsIgnoreCase("umum")){
                                    jenis = getActivity().getString(R.string.rsumum);
                                }else if (obj.getString("jenis").equalsIgnoreCase("spesialisasi")){
                                    jenis = getActivity().getString(R.string.rsspesial);
                                }
                                if (distance < 20){
                                    mList.add(new HospitalModel(name, no_telp, jalanRs, no_bangunanRs, rtrwRs, kelurahanRs, kecamatanRs, kotaRs, "Provinsi", infoRs,jenis,hospitalImage,id,distance, mListDokter));
                                }
                            }
                        }
                        Collections.sort(mList, Comparator.comparing(HospitalModel::getDistance));
                        if(location==null){
                            Picasso.get().load(getActivity().getString(R.string.hospitalURL)).fit().centerCrop().into(civ_rs);
                            rl_empty_rekrs.setVisibility(View.VISIBLE);
                            tv_empty.setText(getActivity().getString(R.string.locationNotFound));
                        }else if (mList.isEmpty()){
                            Picasso.get().load(getActivity().getString(R.string.hospitalURL)).fit().centerCrop().into(civ_rs);
                            rl_empty_rekrs.setVisibility(View.VISIBLE);
                            tv_empty.setText(getActivity().getString(R.string.emptyHospital));
                        }
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_rekrs_loader.setVisibility(View.GONE);
                        initRecyclerViewItem();
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
            },REQUEST_CODE);
        }

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
                        kecamatan = addresses.get(0).getLocality().split(" ",2)[1];
                        kelurahan = addresses.get(0).getSubLocality();
                        kota = addresses.get(0).getSubAdminArea().split(" ",2)[1];
                        provinsi = addresses.get(0).getAdminArea();
                        jalan = addresses.get(0).getThoroughfare().split(" ",2)[1];
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

    private void initialize(){
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        ll_rekrs_loader = getActivity().findViewById(R.id.ll_rekrs_loader);
        rl_empty_rekrs = getActivity().findViewById(R.id.layout_empty_hospital);
        civ_rs = getActivity().findViewById(R.id.civ_rs_empty);
        tv_empty = getActivity().findViewById(R.id.tv_hospital_empty);
        recyclerView = getActivity().findViewById(R.id.rv_rekrs);
        searchText = getActivity().findViewById(R.id.et_search_hospital);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    private void initRecyclerViewItem(){
        mAdapter = new HospitalAdapterMore(mList,getActivity().getApplicationContext(),getActivity());
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
