package com.example.medtek.ui.dokter.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.dokter.JanjiModel;
import com.example.medtek.network.RetrofitClient;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

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

import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;

public class MenungguKonfirmasiFragment extends Fragment {

    private final List<JanjiModel> mList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private MenungguKonfirmasiAdapter mAdapter;
    private RecyclerView recyclerView;
    private LinearLayout ll_loader;
    private ShimmerFrameLayout shimmerFrameLayout;

    private String access, refresh;
    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private boolean status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menunggu_konfirmasi, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        Call<ResponseBody> listWaiting = RetrofitClient.getInstance().getApi().getUserJanji("Bearer "+access);
        listWaiting.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            String s = response.body().string();
                            JSONObject jsonObject = new JSONObject(s);
                            JSONArray array = jsonObject.getJSONArray("data");
                            mList.clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);

                                int idReport;

                                if (object.isNull("idReport")){
                                    idReport = 0;
                                }else{
                                    idReport = object.getInt("idReport");
                                }

                                JSONArray imageJanji = object.getJSONArray("image");
                                String filePath="";
                                if (imageJanji.length() != 0){
                                    JSONObject imgObj = imageJanji.getJSONObject(0);
                                    filePath = imgObj.getString("path");
                                }

                                if (object.getInt("idStatus") == 1){
                                    JSONArray transaksi = object.getJSONArray("transaksi");
                                    for (int j = 0; j < transaksi.length(); j++) {
                                        JSONObject transObj = transaksi.getJSONObject(j);
                                        if (!transObj.getBoolean("is_paid")){
                                            status = false;
                                        }else{
                                            status = true;
                                        }
                                    }
                                    if (status){
                                        mList.add(new JanjiModel(object.getInt("id"),object.getInt("idPasien"),object.getInt("idDokter"),object.getInt("idConversation"),
                                                idReport,object.getInt("day_id"),object.getInt("idStatus"),object.getString("tglJanji"),object.getString("detailJanji"),
                                                filePath));
                                    }
                                }
                            }
                            initRecyclerViewItem();
                            recyclerView.setVisibility(View.VISIBLE);
                            ll_loader.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                        }
                    }
                } catch (IOException | JSONException e) {
                    listWaiting.clone().enqueue(this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void initialize() {

        setStatusBar();
        loadData(getActivity());

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        recyclerView = getActivity().findViewById(R.id.rv_antrian_konfirmasi);
        shimmerFrameLayout = getActivity().findViewById(R.id.shimmerLayout);
        ll_loader = getActivity().findViewById(R.id.layout_loader);

    }

    private void initRecyclerViewItem(){
        mAdapter = new MenungguKonfirmasiAdapter(mList,getActivity().getApplicationContext(),getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    private void setStatusBar(){
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    public void loadData(Context context) {
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
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
