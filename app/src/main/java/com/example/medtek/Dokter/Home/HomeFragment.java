package com.example.medtek.Dokter.Home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Home.Doctors.DokterAdapterMore;
import com.example.medtek.Pasien.Model.DokterModel;
import com.example.medtek.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private ChipNavigationBar bottomBar;
    private TextView tv_date_today, tv_nama_user, tv_nama_pasien, tv_detail_janji;
    private CircleImageView civ_dokter, civ_pasien;
    private LinearLayout layout_pasien;
    private ImageButton ib_next;

    private String access, refresh,TAG="MedTekMedTek";
    private int idJanji;
    private LocalDate tglJanji;

    private List<JanjiModel> mList = new ArrayList<>();;
    private RecyclerView.LayoutManager mLayoutManager;
    private MenungguKonfirmasiAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_dokter, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        AndroidThreeTen.init(getActivity());
        initialize();

        LocalDate localDate = LocalDate.now();
        Locale locale = new Locale("in", "ID");
        DateTimeFormatter day = DateTimeFormatter.ofPattern("EEEE",locale).withLocale(locale);
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy",locale).withLocale(locale);

        tv_date_today.setText(getResources().getString(R.string.today)+", "+localDate.format(dayFormat));

        Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
        callUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        String s = response.body().string();
                        JSONObject obj = new JSONObject(s);
                        tv_nama_user.setText(obj.getString("name"));
                        JSONArray jsonArray = new JSONArray(obj.getString("image"));
                        if (jsonArray.length() == 0){
                            civ_dokter.setImageDrawable(getActivity().getDrawable(R.drawable.ic_dokter));
                        }else{
                            String path = "http://192.168.1.9:8000"+jsonArray.getJSONObject(0).getString("path");
                            if (jsonArray.getJSONObject(0).getString("path").equals("/storage/Pasien.svg")){
                                civ_dokter.setImageDrawable(getActivity().getDrawable(R.drawable.ic_dokter));
                            }else{
                                Picasso.get().load(path).into(civ_dokter);
                            }
                            Log.d("MedTekMedTekMedTekImage",path);
                        }
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

        Call<ResponseBody> callJanji = RetrofitClient.getInstance().getApi().getUserJanji("Bearer "+access);
        callJanji.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    if (response.isSuccessful()){
                        if (response.body()!=null){
                            String s = response.body().string();
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.getJSONArray("data").length() == 1){
                                JSONObject janji = jsonObject.getJSONArray("data").getJSONObject(0);
                                tglJanji = LocalDate.parse(janji.getString("tglJanji"));
                                Call<ResponseBody> getPasien = RetrofitClient.getInstance().getApi().getPasienId(janji.getInt("idPasien"));
                                getPasien.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            if (response.isSuccessful()){
                                                if (response.body() != null){
                                                    String s = response.body().string();
                                                    JSONObject json = new JSONObject(s);
                                                    JSONArray jsonArray = json.getJSONObject("data").getJSONArray("image");
                                                    String path = "http://192.168.1.9:8000"+jsonArray.getJSONObject(1).getString("path");
                                                    if (jsonArray.getJSONObject(0).getString("path").equals("/storage/Pasien.png")){
                                                        civ_pasien.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pasien));
                                                    }else{
                                                        Picasso.get().load(path).into(civ_pasien);
                                                    }
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

                                tv_nama_pasien.setText(janji.getJSONObject("pasien").getString("name"));
                                if (tglJanji.isEqual(LocalDate.now())){
                                    tv_detail_janji.setText("Hari Ini, "+tglJanji.format(dayFormat));
                                }else if(tglJanji.isEqual(LocalDate.now().plusDays(1))){
                                    tv_detail_janji.setText("Besok, "+tglJanji.format(dayFormat));
                                }else{
                                    tv_detail_janji.setText(tglJanji.format(day)+", "+tglJanji.format(dayFormat));
                                }
                            }else{
                                for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
                                    JSONObject janji = jsonObject.getJSONArray("data").getJSONObject(i);
                                    if (i==0){
                                        tglJanji = LocalDate.parse(janji.getString("tglJanji"));
                                        idJanji = janji.getInt("id");
                                    }else{
                                        LocalDate date = LocalDate.parse(janji.getString("tglJanji"));
                                        if (date.isBefore(tglJanji)){
                                            tglJanji = date;
                                            idJanji = janji.getInt("id");
                                        }
                                    }
                                }
                                Call<ResponseBody> getJanji = RetrofitClient.getInstance().getApi().getJanjiId("Bearer "+access,idJanji);
                                getJanji.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            if (response.isSuccessful()) {
                                                if (response.body() != null) {
                                                    String s = response.body().string();
                                                    JSONObject jsonObject = new JSONObject(s);
                                                    JSONObject janji = jsonObject.getJSONObject("data");
                                                    Call<ResponseBody> getPasien = RetrofitClient.getInstance().getApi().getPasienId(janji.getInt("idPasien"));
                                                    getPasien.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                            try {
                                                                if (response.isSuccessful()){
                                                                    if (response.body() != null){
                                                                        String s = response.body().string();
                                                                        JSONObject json = new JSONObject(s);
                                                                        JSONArray jsonArray = json.getJSONObject("data").getJSONArray("image");
                                                                        if (jsonArray.length() == 0){
                                                                            civ_pasien.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pasien));
                                                                        }else{
                                                                            String path = "http://192.168.1.9:8000"+jsonArray.getJSONObject(0).getString("path");
                                                                            if (jsonArray.getJSONObject(0).getString("path").equals("/storage/Pasien.png")){
                                                                                civ_pasien.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pasien));
                                                                            }else{
                                                                                Picasso.get().load(path).into(civ_pasien);
                                                                            }
                                                                        }
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

                                                    tv_nama_pasien.setText(janji.getJSONObject("pasien").getString("name"));
                                                    if (tglJanji.isEqual(LocalDate.now())){
                                                        tv_detail_janji.setText("Hari Ini, "+tglJanji.format(dayFormat));
                                                    }else if(tglJanji.isEqual(LocalDate.now().plusDays(1))){
                                                        tv_detail_janji.setText("Besok, "+tglJanji.format(dayFormat));
                                                    }else{
                                                        tv_detail_janji.setText(tglJanji.format(day)+", "+tglJanji.format(dayFormat));
                                                    }

                                                }
                                            }
                                        } catch (JSONException | IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                                    }
                                });
                            }
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

                                if (object.getInt("idStatus") == 1){
                                    mList.add(new JanjiModel(object.getInt("id"),object.getInt("idPasien"),object.getInt("idDokter"),object.getInt("idConversation"),
                                            idReport,object.getInt("day_id"),object.getInt("idStatus"),object.getString("tglJanji"),object.getString("detailJanji")));
                                }
                            }
                            initRecyclerViewItem();
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
    }

    private void initialize() {

        setStatusBar();
        loadData(getActivity());

        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.VISIBLE);

        tv_date_today = getActivity().findViewById(R.id.tv_date_today);
        tv_nama_user = getActivity().findViewById(R.id.tv_nama_user);
        tv_nama_pasien = getActivity().findViewById(R.id.tv_nama_pasien);
        tv_detail_janji = getActivity().findViewById(R.id.tv_tgl_janji);
        civ_dokter = getActivity().findViewById(R.id.civ_user);
        civ_pasien = getActivity().findViewById(R.id.civ_pasien);
        layout_pasien = getActivity().findViewById(R.id.layout_pasien);
        ib_next = getActivity().findViewById(R.id.ib_next);

        recyclerView = getActivity().findViewById(R.id.rv_menunggu_konfirmasi);

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
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        this.access = sharedPreferences.getString("token", "");
        this.refresh = sharedPreferences.getString("refresh_token", "");
    }

    private void setFragment(Fragment fragment,String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

}
