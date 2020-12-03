package com.example.medtek.Pasien.Home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.API.Api;
import com.example.medtek.API.RajaOngkirRetrofitClient;
import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Home.Doctors.DokterFragment;
import com.example.medtek.Pasien.Home.Hospitals.ListDokterAdapter;
import com.example.medtek.Pasien.Model.DokterModel;
import com.example.medtek.Pasien.Model.HospitalModel;
import com.example.medtek.Pasien.Model.ListDokterModel;
import com.example.medtek.Pasien.Model.SearchModel;
import com.example.medtek.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;
    private ChipNavigationBar bottomNavigationView;
    private Toolbar toolbar;

    private TextView tv_lokasi;
    private EditText et_search;

    private List<String> cityList = new ArrayList<>();
    private TextView tv_kardiologiintervensi, tv_aritmiadanelektrofisiologi, tv_kardiologipediatri, tv_pencitraankardiovaskuler, tv_kardiologiklinik, tv_rehabilitasidanprevensi,
    tv_vaskuler, tv_perawatanjantungakut, tv_ahlijantungumum;

    private String city="", selected="";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private int REQUEST_CODE = 46;
    private Location location;

    private List<SearchModel> listDokter = new ArrayList<>();
    private List<SearchModel> mListDokter = new ArrayList<>();
    private List<SearchModel> mListRs, listRs = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private SearchAdapter mAdapterDokter, mAdapterRs;
    private RecyclerView rv_dokter, rv_rs;

    private LinearLayout layout_spesialisasi, layout_rs, layout_dokter;
    private TextView tv_seeall_dokter, tv_seeall_rs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
            @Override
            public void onLayoutReady() {
                if (mAdapterDokter.getItemCount() >= 4){
                    tv_seeall_dokter.setVisibility(View.VISIBLE);
                }else{
                    tv_seeall_dokter.setVisibility(View.GONE);
                }
            }
        };
        Call<ResponseBody> getCity = RetrofitClient .getInstance().getApi().getCity();
        getCity.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            String s = response.body().string();
                            Log.e("TAG", "onResponse: "+s );
                            JSONObject object = new JSONObject(s);
                            JSONArray cityArr = object.getJSONArray("data");
                            for (int i = 0; i < cityArr.length(); i++) {
                                JSONObject cityObject = cityArr.getJSONObject(i);
                                if (!cityObject.getString("kota").equalsIgnoreCase("null")){
                                    cityList.add(cityObject.getString("kota"));
                                }
                            }
                            tv_lokasi.setEnabled(true);
                        }else{
                            Log.e("TAG", "onResponse: "+response.errorBody().string() );
                        }
                    }else{
                        Log.e("TAG", "onResponse: "+response.errorBody().string() );
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Log.e("TAG", "onResponse: "+e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        Call<ResponseBody> callDokter = RetrofitClient.getInstance().getApi().getAllDokter();
        callDokter.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String dokterResult = response.body().string();
                    JSONObject object = new JSONObject(dokterResult);
                    JSONArray array = new JSONArray(object.getString("data"));
                    listDokter.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jo = array.getJSONObject(i);
                        String name = jo.getString("name");
                        int id = jo.getInt("id");
                        int isVerified;
                        if (!jo.getString("email_verified_at").equals(null)){
                            isVerified = 1;
                        }else{
                            isVerified = 0;
                        }
                        String path = "";
                        if (new JSONArray(jo.getString("image")).length() !=0){
                            JSONArray jsonArray = new JSONArray(jo.getString("image"));
                            path = "http://192.168.137.1:8000"+jsonArray.getJSONObject(0).getString("path");
                        }else{
                            path = "http://192.168.137.1:8000/storage/Dokter.png";
                        }
                        JSONObject alamatObject = new JSONObject(jo.getString("alamat"));
                        String kota = alamatObject.getString("kota");

                        JSONObject specObj = new JSONObject(jo.getString("specialization"));
                        String spec = specObj.getString("specialization");
                        if (spec.equalsIgnoreCase("acute cardiac care")){
                            spec = "Perawatan jantung akut";
                        }
                        if (isVerified == 1){
                            listDokter.add(new SearchModel(id,name,spec,path,kota));
                        }
                    }
                    Collections.reverse(listDokter);
                    Collections.sort(listDokter, Comparator.comparing(SearchModel::getNama));
                    if (mAdapterDokter.getItemCount() >= 4){
                        tv_seeall_dokter.setVisibility(View.VISIBLE);
                    }else{
                        tv_seeall_dokter.setVisibility(View.GONE);
                    }
                    mListDokter.clear();
                    for (int i = 0; i < 4; i++) {
                        mListDokter.add(listDokter.get(i));
                    }
                    mAdapterDokter.notifyDataSetChanged();
                    rv_dokter.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if(recyclerViewReadyCallback != null){
                                recyclerViewReadyCallback.onLayoutReady();
                            }
                            rv_dokter.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    callDokter.clone().enqueue(this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        tv_lokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.bottom_sheet_location, getActivity().findViewById(R.id.bottomSheetRSDetail));
                LinearLayout linearLayout = bottomSheetView.findViewById(R.id.layout_content);
                TextView userLoc = bottomSheetView.findViewById(R.id.tv_selectfromhere);
                TextView allLoc = bottomSheetView.findViewById(R.id.tv_all_location);
                for (int i = 0; i < cityList.size(); i++) {
                    LinearLayout.LayoutParams lf = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lf.setMargins(0,16,0,0);

                    TextView textView = new TextView(getActivity());
                    textView.setText(cityList.get(i));
                    textView.setTextSize(16);
                    textView.setTextColor(Color.BLACK);
                    textView.setLayoutParams(lf);
                    linearLayout.addView(textView);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            city = textView.getText().toString().trim();
                            tv_lokasi.setText(textView.getText().toString().trim());
                            tv_lokasi.setCompoundDrawables(null,null,null,null);
                            Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_regular);
                            tv_lokasi.setTypeface(typeface);
                            tv_lokasi.setTextColor(getActivity().getColor(R.color.textColorLightGray));

                            mListDokter.clear();
                            for (int i = 0; i < 4; i++) {
                                mListDokter.add(listDokter.get(i));
                            }
                            mAdapterDokter.notifyDataSetChanged();
                            et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                            bottomSheetDialog.dismiss();
                        }
                    });
                }
                userLoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                            getLocation();
                        }else{
                            ActivityCompat.requestPermissions(getActivity(), new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },REQUEST_CODE);
                        }

                        tv_lokasi.setText("Lokasi Saya");
                        tv_lokasi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_my_location),null,null,null);
                        for (Drawable drawable : tv_lokasi.getCompoundDrawables()) {
                            if (drawable != null) {
                                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(tv_lokasi.getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN));
                            }
                        }
                        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_medium);
                        tv_lokasi.setTypeface(typeface);
                        tv_lokasi.setTextColor(getActivity().getColor(R.color.colorAccent));
                        bottomSheetDialog.dismiss();
                    }
                });
                allLoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tv_lokasi.setText("Semua Lokasi");
                        city = "";
                        tv_lokasi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_my_location),null,null,null);
                        for (Drawable drawable : tv_lokasi.getCompoundDrawables()) {
                            if (drawable != null) {
                                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(tv_lokasi.getContext(), R.color.textColorLightGray), PorterDuff.Mode.SRC_IN));
                            }
                        }
                        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_medium);
                        tv_lokasi.setTypeface(typeface);
                        tv_lokasi.setTextColor(getActivity().getColor(R.color.textColorLightGray));
                        mListDokter.clear();
                        for (int i = 0; i < 4; i++) {
                            mListDokter.add(listDokter.get(i));
                        }
                        mAdapterDokter.notifyDataSetChanged();
                        et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    if (city == ""){
                        city = "Semua";
                    }
                    mAdapterDokter.getFilter().filter(city+", "+et_search.getText().toString().trim());
                    if (mAdapterDokter.getItemCount() != 0){
                        if (mAdapterDokter.getItemCount() < 4){
                            tv_seeall_dokter.setVisibility(View.GONE);
                        }
                        layout_dokter.setVisibility(View.VISIBLE);
                    }else{
                        layout_dokter.setVisibility(View.GONE);
                    }
                    return true;
                }
                return false;
            }
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0){
                    et_search.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_search),null,null,null);
                    for (Drawable drawable : et_search.getCompoundDrawables()) {
                        if (drawable != null) {
                            drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(et_search.getContext(), R.color.textColorLightGray), PorterDuff.Mode.SRC_IN));
                        }
                    }
                    setSpesialisasiNull();
                    layout_spesialisasi.setVisibility(View.VISIBLE);
                    layout_dokter.setVisibility(View.VISIBLE);
                    layout_rs.setVisibility(View.VISIBLE);
                    if (listDokter.size() > 4){
                        tv_seeall_dokter.setVisibility(View.VISIBLE);
                    }else{
                        tv_seeall_dokter.setVisibility(View.GONE);
                    }
                    mListDokter.clear();
                    for (int i = 0; i < 4; i++) {
                        mListDokter.add(listDokter.get(i));
                    }
                    mAdapterDokter.notifyDataSetChanged();
                }else{
                    et_search.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_search),null,getActivity().getDrawable(R.drawable.ic_clear),null);
                    for (Drawable drawable : et_search.getCompoundDrawables()) {
                        if (drawable != null) {
                            drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(et_search.getContext(), R.color.textColorLightGray), PorterDuff.Mode.SRC_IN));
                        }
                    }
                    mListDokter.clear();
                    for (int i = 0; i < listDokter.size(); i++) {
                        mListDokter.add(listDokter.get(i));
                    }
                    mAdapterDokter.notifyDataSetChanged();
                    if(s.toString().equalsIgnoreCase("dokter") || s.toString().equalsIgnoreCase("doctor")){
                        layout_spesialisasi.setVisibility(View.GONE);
                        layout_rs.setVisibility(View.GONE);
                        layout_dokter.setVisibility(View.VISIBLE);
                    }else if(s.toString().contains("rumah sakit") || s.toString().contains("hospital") || s.toString().contains("rs")){
                        layout_spesialisasi.setVisibility(View.GONE);
                        layout_dokter.setVisibility(View.GONE);
                        layout_rs.setVisibility(View.VISIBLE);
                    }else{
                        layout_rs.setVisibility(View.GONE);
                        layout_dokter.setVisibility(View.GONE);
                        tv_seeall_dokter.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (city == ""){
                    city = "Semua";
                }
                mAdapterDokter.getFilter().filter(city+", "+et_search.getText().toString().trim());
                if (mAdapterDokter.getItemCount() != 0){
                    if (mAdapterDokter.getItemCount() < 4){
                        tv_seeall_dokter.setVisibility(View.GONE);
                    }
                    layout_dokter.setVisibility(View.VISIBLE);
                }else{
                    layout_dokter.setVisibility(View.GONE);
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
        });
        et_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (et_search.getCompoundDrawables()[DRAWABLE_RIGHT] != null){
                        if (event.getRawX() >= (et_search.getRight() - et_search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()-40)) {
                            et_search.setText("");
                            selected="";
                        }
                    }
                }
                return false;
            }
        });
        tv_seeall_dokter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListDokter.clear();
                tv_seeall_dokter.setVisibility(View.GONE);
                et_search.setText("Dokter");
                et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
            }
        });
    }

    private void initialize(){
        bottomNavigationView = getActivity().findViewById(R.id.bottomBar);
        bottomNavigationView.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        tv_lokasi = getActivity().findViewById(R.id.tv_lokasi);
        et_search = getActivity().findViewById(R.id.et_search);

        rv_dokter = getActivity().findViewById(R.id.rv_dokter);
        rv_rs = getActivity().findViewById(R.id.rv_rs);

        tv_seeall_dokter = getActivity().findViewById(R.id.tv_dokter_seeall);
        tv_seeall_rs = getActivity().findViewById(R.id.tv_rs_seeall);

        layout_spesialisasi = getActivity().findViewById(R.id.layout_spesialisasi);
        layout_rs = getActivity().findViewById(R.id.layout_rs);
        layout_dokter = getActivity().findViewById(R.id.layout_dokter);

        tv_kardiologiintervensi = getActivity().findViewById(R.id.tv_kardiologiintervensi);
        tv_aritmiadanelektrofisiologi = getActivity().findViewById(R.id.tv_aritmiadanelektrofisiologi);
        tv_kardiologipediatri = getActivity().findViewById(R.id.tv_kardiologipediatri);
        tv_pencitraankardiovaskuler = getActivity().findViewById(R.id.tv_pencitraankardiovaskuler);
        tv_kardiologiklinik = getActivity().findViewById(R.id.tv_kardiologiklinik);
        tv_rehabilitasidanprevensi = getActivity().findViewById(R.id.tv_rehabilitasidanprevensi);
        tv_vaskuler = getActivity().findViewById(R.id.tv_vaskuler);
        tv_perawatanjantungakut = getActivity().findViewById(R.id.tv_perawatanjantungakut);
        tv_ahlijantungumum = getActivity().findViewById(R.id.tv_ahlijantungumum);

        tv_kardiologiintervensi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected.equalsIgnoreCase(tv_kardiologiintervensi.getText().toString().trim())){
                    setSpesialisasiNull();
                    selected="";
                    et_search.setText("");
                    et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                }else{
                    selected = tv_kardiologiintervensi.getText().toString().trim();
                    et_search.setText(tv_kardiologiintervensi.getText().toString().trim());
                    et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                    tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                    tv_kardiologiintervensi.setBackgroundTintList(null);
                    tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                    setTextViewDrawableColor(tv_kardiologiintervensi,R.color.colorPrimary);
                    tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.colorPrimary));

                    tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
                    tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
                    tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
                    tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
                    tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
                    tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
                    tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
                    tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
                    tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));
                }
            }
        });
        tv_aritmiadanelektrofisiologi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected.equalsIgnoreCase(tv_aritmiadanelektrofisiologi.getText().toString().trim())){
                    setSpesialisasiNull();
                    selected="";
                    et_search.setText("");
                    et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                }else{
                    selected = tv_aritmiadanelektrofisiologi.getText().toString().trim();
                    et_search.setText(tv_aritmiadanelektrofisiologi.getText().toString().trim());
                    et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                    tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                    tv_aritmiadanelektrofisiologi.setBackgroundTintList(null);
                    tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                    setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.colorPrimary);
                    tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.colorPrimary));

                    tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
                    tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
                    tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
                    tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
                    tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
                    tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
                    tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
                    tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

                    tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                    tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                    tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                    setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
                    tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));
                }
            }
        });
        tv_kardiologipediatri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setText(tv_kardiologipediatri.getText().toString().trim());
                et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                tv_kardiologipediatri.setBackgroundTintList(null);
                tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                setTextViewDrawableColor(tv_kardiologipediatri,R.color.colorPrimary);
                tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.colorPrimary));

                tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
                tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
                tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
                tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
                tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
                tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
                tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
                tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
                tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));
            }
        });
        tv_pencitraankardiovaskuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setText(tv_pencitraankardiovaskuler.getText().toString().trim());
                et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                tv_pencitraankardiovaskuler.setBackgroundTintList(null);
                tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.colorPrimary);
                tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.colorPrimary));

                tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
                tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
                tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
                tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
                tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
                tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
                tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
                tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
                tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));
            }
        });
        tv_kardiologiklinik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setText(tv_kardiologiklinik.getText().toString().trim());
                et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                tv_kardiologiklinik.setBackgroundTintList(null);
                tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                setTextViewDrawableColor(tv_kardiologiklinik,R.color.colorPrimary);
                tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.colorPrimary));

                tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
                tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
                tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
                tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
                tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
                tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
                tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
                tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
                tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));
            }
        });
        tv_rehabilitasidanprevensi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setText(tv_rehabilitasidanprevensi.getText().toString().trim());
                et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                tv_rehabilitasidanprevensi.setBackgroundTintList(null);
                tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.colorPrimary);
                tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.colorPrimary));

                tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
                tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
                tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
                tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
                tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
                tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
                tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
                tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
                tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));
            }
        });
        tv_vaskuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListDokter.clear();
                for (int i = 0; i < listDokter.size(); i++) {
                    mListDokter.add(listDokter.get(i));
                }
                mAdapterDokter.notifyDataSetChanged();
                et_search.setText(tv_vaskuler.getText().toString().trim());
                et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                tv_vaskuler.setBackgroundTintList(null);
                tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                setTextViewDrawableColor(tv_vaskuler,R.color.colorPrimary);
                tv_vaskuler.setTextColor(getActivity().getColor(R.color.colorPrimary));

                tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
                tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
                tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
                tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
                tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
                tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
                tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
                tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
                tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));
            }
        });
        tv_perawatanjantungakut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListDokter.clear();
                for (int i = 0; i < listDokter.size(); i++) {
                    mListDokter.add(listDokter.get(i));
                }
                mAdapterDokter.notifyDataSetChanged();
                et_search.setText(tv_perawatanjantungakut.getText().toString().trim());
                tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                tv_perawatanjantungakut.setBackgroundTintList(null);
                tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                setTextViewDrawableColor(tv_perawatanjantungakut,R.color.colorPrimary);
                tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.colorPrimary));

                tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
                tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
                tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
                tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
                tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
                tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
                tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
                tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
                tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));
            }
        });
        tv_ahlijantungumum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setText(tv_ahlijantungumum.getText().toString().trim());
                et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                tv_ahlijantungumum.setBackgroundTintList(null);
                tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check_circle),null,null,null);
                setTextViewDrawableColor(tv_ahlijantungumum,R.color.colorPrimary);
                tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.colorPrimary));

                tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
                tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
                tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
                tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
                tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
                tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
                tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
                tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

                tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
                tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
                tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
                setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
                tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        initRecyclerViewItem();
        setSpesialisasiNull();
    }

    private void initRecyclerViewItem(){
        mAdapterDokter = new SearchAdapter(mListDokter,getActivity().getApplicationContext(),getActivity());
//        mAdapterRs = new SearchAdapter(mListRs,getActivity().getApplicationContext(),getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        rv_dokter.setAdapter(mAdapterDokter);
//        rv_rs.setAdapter(mAdapterRs);
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), color), PorterDuff.Mode.SRC_IN));
            }
        }
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
                        String kecamatan = addresses.get(0).getLocality().split(" ",2)[1];
                        String kelurahan = addresses.get(0).getSubLocality();
                        city = addresses.get(0).getSubAdminArea().split(" ",2)[1];
                        String provinsi = addresses.get(0).getAdminArea();
                        String jalan = addresses.get(0).getThoroughfare().split(" ",2)[1];
                        double sLat = addresses.get(0).getLatitude();
                        double sLong = addresses.get(0).getLongitude();
//                        Toasty.info(getActivity(),"Kecamatan: "+kecamatan+", Kelurahan: "+kelurahan).show();
                        mListDokter.clear();
                        for (int i = 0; i < listDokter.size(); i++) {
                            mListDokter.add(listDokter.get(i));
                        }
                        mAdapterDokter.notifyDataSetChanged();
                        et_search.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0));
                    } catch (IOException e) {
                        e.printStackTrace();
                        fusedLocationProviderClient.getLastLocation();
                    }
                }
            }
        });
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

    private void setSpesialisasiNull(){
        tv_ahlijantungumum.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_ahlijantungumum.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_ahlijantungumum.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_ahlijantungumum,R.color.textColorGray);
        tv_ahlijantungumum.setTextColor(getActivity().getColor(R.color.textColorGray));

        tv_aritmiadanelektrofisiologi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_aritmiadanelektrofisiologi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_aritmiadanelektrofisiologi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_aritmiadanelektrofisiologi,R.color.textColorGray);
        tv_aritmiadanelektrofisiologi.setTextColor(getActivity().getColor(R.color.textColorGray));

        tv_kardiologipediatri.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_kardiologipediatri.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_kardiologipediatri.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_kardiologipediatri,R.color.textColorGray);
        tv_kardiologipediatri.setTextColor(getActivity().getColor(R.color.textColorGray));

        tv_pencitraankardiovaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_pencitraankardiovaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_pencitraankardiovaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_pencitraankardiovaskuler,R.color.textColorGray);
        tv_pencitraankardiovaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

        tv_kardiologiklinik.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_kardiologiklinik.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_kardiologiklinik.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_kardiologiklinik,R.color.textColorGray);
        tv_kardiologiklinik.setTextColor(getActivity().getColor(R.color.textColorGray));

        tv_rehabilitasidanprevensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_rehabilitasidanprevensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_rehabilitasidanprevensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_rehabilitasidanprevensi,R.color.textColorGray);
        tv_rehabilitasidanprevensi.setTextColor(getActivity().getColor(R.color.textColorGray));

        tv_vaskuler.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_vaskuler.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_vaskuler.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_vaskuler,R.color.textColorGray);
        tv_vaskuler.setTextColor(getActivity().getColor(R.color.textColorGray));

        tv_perawatanjantungakut.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_perawatanjantungakut.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_perawatanjantungakut.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_perawatanjantungakut,R.color.textColorGray);
        tv_perawatanjantungakut.setTextColor(getActivity().getColor(R.color.textColorGray));

        tv_kardiologiintervensi.setBackground(getActivity().getDrawable(R.drawable.bg_button_stroke_red));
        tv_kardiologiintervensi.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.textColorGray)));
        tv_kardiologiintervensi.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.ic_check),null,null,null);
        setTextViewDrawableColor(tv_kardiologiintervensi,R.color.textColorGray);
        tv_kardiologiintervensi.setTextColor(getActivity().getColor(R.color.textColorGray));
    }

}
