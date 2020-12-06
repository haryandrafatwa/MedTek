package com.example.medtek.ui.pasien.home.hospitals;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.pasien.ListDokterModel;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.utils.RoundedCornersTransformation;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailHospitalFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private final List<ListDokterModel> mList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private ListDokterAdapter mAdapter;
    private RecyclerView recyclerView;

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;

    private ImageButton ib_share;
    private ImageView iv_rs;
    private TextView tv_rs_name, tv_rs_jenis, tv_rs_info, tv_rs_alamat;
    private Button btn_direct, btn_call;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_hospital, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        Bundle bundle = getArguments();
        int id_hospital = bundle.getInt("id_hospital");

        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().getHospitalId(id_hospital);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String s = response.body().string();
                    JSONObject raw = new JSONObject(s);
                    JSONObject data = raw.getJSONObject("data");
                    String rsName = data.getString("name");
                    String rsInfo = data.getString("info");
                    String notelp = data.getString("notelp");
                    String path=data.getString("image");
                    if (path!= "null"){
                        if (path.toLowerCase().equalsIgnoreCase(getString(R.string.hospitalURL))){
                            Picasso.get().load(path).fit().centerCrop().into(iv_rs);
                        }else if (data.getString("jenis").toLowerCase().equalsIgnoreCase("spesialisasi")){
                            final int radius = 16;
                            final int margin = 0;
                            final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                            Picasso.get().load(path).transform(transformation).fit().centerCrop().into(iv_rs);
                        }
                    }else{
                        Picasso.get().load(getString(R.string.hospitalURL)).fit().centerCrop().into(iv_rs);
                    }

                    String jenis="";
                    if (data.getString("jenis").toLowerCase().equalsIgnoreCase("umum")){
                        jenis = getActivity().getString(R.string.rsumum);
                    }else if (data.getString("jenis").toLowerCase().equalsIgnoreCase("spesialisasi")){
                        jenis = getActivity().getString(R.string.rsspesial);
                    }

                    JSONObject alamat = data.getJSONObject("alamat");
                    String jalanRs = alamat.getString("jalan");
                    String noRs = alamat.getString("nomor_bangunan");
                    String rtrwRs = alamat.getString("rtrw");
                    String kelurahanRs = alamat.getString("kelurahan");
                    String kecamatanRs = alamat.getString("kecamatan");
                    String kotaRs = alamat.getString("kota");
                    String fullAddress = "Jalan "+jalanRs+", No. "+noRs+", Rt/Rw. "+rtrwRs+", Kelurahan "+kelurahanRs+", Kecamatan "+kecamatanRs+", Kota "+kotaRs;

                    JSONArray dokterList = data.getJSONArray("dokter");
                    mList.clear();
                    for (int i = 0; i < dokterList.length(); i++) {
                        JSONObject dataDokter = dokterList.getJSONObject(i);
                        int id = dataDokter.getInt("id");
                        String drName = dataDokter.getString("name");
                        JSONObject specialization = dataDokter.getJSONObject("specialization");
                        String spesialisasi = specialization.getString("specialization");
                        JSONArray image = dataDokter.getJSONArray("image");
                        String dokterImage="";
                        if (image.length()!=0){
                            dokterImage = image.getJSONObject(0).getString("path");
                        }else{
                            dokterImage = getString(R.string.dokterURL);
                        }
                        mList.add(new ListDokterModel(id, drName, spesialisasi, dokterImage));
                        mAdapter.notifyDataSetChanged();
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
                    Log.e("HOSPITALGETID",mList.get(0).getSpesialisasi());

                    tv_rs_name.setText(rsName);
                    tv_rs_jenis.setText(jenis);
                    tv_rs_alamat.setText(fullAddress);
                    tv_rs_info.setText(rsInfo);

                    btn_direct.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("google.navigation:q="+rsName));
                            startActivity(intent);
                        }
                    });

                    btn_call.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                callIntent.setData(Uri.parse("tel:" + notelp));//change the number
                                startActivity(callIntent);
                            }else{
                                ActivityCompat.requestPermissions(getActivity(), new String[]{
                                        Manifest.permission.CALL_PHONE
                                },28);
                            }
                        }
                    });

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    call.clone().enqueue(this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        initRecyclerViewItem();

    }

    private void initialize() {

        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        ib_share = getActivity().findViewById(R.id.ib_share);
        iv_rs = getActivity().findViewById(R.id.iv_rs);
        tv_rs_name = getActivity().findViewById(R.id.tv_rs_name);
        tv_rs_jenis = getActivity().findViewById(R.id.tv_rs_jenis);
        tv_rs_info = getActivity().findViewById(R.id.tv_rs_info);
        tv_rs_alamat = getActivity().findViewById(R.id.tv_rs_add);
        btn_direct = getActivity().findViewById(R.id.btn_direct);
        btn_call = getActivity().findViewById(R.id.btn_call);

        recyclerView = getActivity().findViewById(R.id.rv_listdokter);
        recyclerView.setVisibility(View.VISIBLE);

    }

    private void initRecyclerViewItem(){
        mAdapter = new ListDokterAdapter(mList,getActivity().getApplicationContext(),getActivity());
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
