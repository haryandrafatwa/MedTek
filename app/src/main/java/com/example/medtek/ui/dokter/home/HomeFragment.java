package com.example.medtek.ui.dokter.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.dokter.JanjiModel;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.service.NotificationService;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.medtek.network.RetrofitClient.BASE_SOCKET_URL;
import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;

public class HomeFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private ChipNavigationBar bottomBar;
    private TextView tv_date_today, tv_nama_user, tv_nama_pasien, tv_detail_janji, tv_detail_konfirmasi, tv_info;
    private CircleImageView civ_dokter, civ_pasien;
    private LinearLayout layout_pasien, layout_info, layout_loader;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RelativeLayout layout_visible;
    private ImageButton ib_next;

    private String access;
    private String refresh;
    private final String TAG="MedTekMedTek";
    private int idJanji, idDokter;
    private LocalDate tglJanji;

    private final List<JanjiModel> mList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private MenungguKonfirmasiAdapter mAdapter;
    private RecyclerView recyclerView;
    private RelativeLayout rl_empty_antrian;

    private Socket socket;
    private String CHANNEL_MESSAGES;

    private boolean status;
    private boolean isOnAttach = false;
    private boolean isLoadUser = false;
    private boolean isLoadJanji = false;
    private boolean isLoadQueue = false;

    private DateTimeFormatter day, dayFormat;

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
        day = DateTimeFormatter.ofPattern("EEEE",locale).withLocale(locale);
        dayFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy",locale).withLocale(locale);

        tv_date_today.setText(getResources().getString(R.string.today)+", "+localDate.format(dayFormat));

        Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
        callUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    if (response.body() != null) {
                        try {
                            if (isOnAttach) {
                                String s = response.body().string();
                                JSONObject obj = new JSONObject(s);
                                idDokter = obj.getInt("id");
                                tv_nama_user.setText(obj.getString("name"));
                                String path="";
                                JSONArray jsonArrayImage = obj.getJSONArray("image");
                                if (jsonArrayImage.length() !=0){
                                    for (int j = 0; j < jsonArrayImage.length(); j++) {
                                        JSONObject imageObj = jsonArrayImage.getJSONObject(j);
                                        if (imageObj.getInt("type_id") == 1) {
                                            path = BASE_URL + imageObj.getString("path");
                                            break;
                                        }
                                    }
                                }else{
                                    path = BASE_URL+"/storage/Dokter.png";
                                }
                                Picasso.get().load(path).into(civ_dokter);
                                String noRek = obj.getString("nomor_rekening");
                                String lulusan = obj.getString("lulusan");
                                String lama_kerja = obj.getString("lama_kerja");
                                if (noRek.equalsIgnoreCase("null")){
                                    layout_info.setVisibility(View.VISIBLE);
                                    tv_info.setText(R.string.infonodata);
                                }
                                if (lulusan.equalsIgnoreCase("null")){
                                    layout_info.setVisibility(View.VISIBLE);
                                    tv_info.setText(R.string.infonodata);
                                }
                                if (lama_kerja.equalsIgnoreCase("null")){
                                    layout_info.setVisibility(View.VISIBLE);
                                    tv_info.setText(R.string.infonodata);
                                }
                                if (obj.getJSONArray("jadwal").length() < 1){
                                    layout_info.setVisibility(View.VISIBLE);
                                    tv_info.setText(R.string.infonojadwal);
                                }

                                isLoadUser = true;
                                shimmerSet();
                                startSocket();
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        callUser.clone().enqueue(this);
                    }
                } else {
                    callUser.clone().enqueue(this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toasty.info(getActivity(), t.getMessage());
            }
        });

        Call<ResponseBody> callJanji = RetrofitClient.getInstance().getApi().getAntrianUser("Bearer "+access);
        callJanji.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    if (response.isSuccessful()){
                        if (response.body()!=null){
                            if (isOnAttach) {
                                String s = response.body().string();
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getJSONArray("data").length() == 0){
                                    civ_pasien.setImageDrawable(getActivity().getDrawable(R.drawable.ic_forbidden));
                                    tv_nama_pasien.setText(getString(R.string.tidakadajanji));
                                    tv_detail_janji.setVisibility(View.GONE);
                                    ib_next.setVisibility(View.GONE);
                                }else if (jsonObject.getJSONArray("data").length() == 1){
                                    JSONObject janji = jsonObject.getJSONArray("data").getJSONObject(0);
                                    tglJanji = LocalDate.parse(janji.getString("tglJanji"));
                                    JSONObject pasien = janji.getJSONObject("pasien");
                                    JSONArray jsonArray = new JSONArray(pasien.getString("image"));
                                    String path="";
                                    JSONArray jsonArrayImage = new JSONArray(pasien.getString("image"));
                                    if (new JSONArray(pasien.getString("image")).length() !=0){
                                        for (int j = 0; j < jsonArrayImage.length(); j++) {
                                            JSONObject imageObj = jsonArrayImage.getJSONObject(j);
                                            if (imageObj.getInt("type_id") == 1) {
                                                path = BASE_URL + imageObj.getString("path");
                                                break;
                                            }
                                        }
                                    } else {
                                        path = BASE_URL+"/storage/Dokter.png";
                                    }
                                    Picasso.get().load(path).into(civ_pasien);
                                    tv_nama_pasien.setText(pasien.getString("name"));
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
                                        JSONObject pasien = janji.getJSONObject("pasien");
                                        String path="";
                                        JSONArray jsonArrayImage = new JSONArray(pasien.getString("image"));
                                        if (new JSONArray(pasien.getString("image")).length() !=0){
                                            for (int j = 0; j < jsonArrayImage.length(); j++) {
                                                JSONObject imageObj = jsonArrayImage.getJSONObject(j);
                                                if (imageObj.getInt("type_id") == 1) {
                                                    path = BASE_URL + imageObj.getString("path");
                                                    break;
                                                }
                                            }
                                        } else {
                                            path = BASE_URL+"/storage/Dokter.png";
                                        }
                                        Picasso.get().load(path).into(civ_pasien);
                                    }
                                }
                                isLoadJanji = true;
                                shimmerSet();
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    callJanji.clone().enqueue(this);

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
                            if (array.length() >= 3){
                                tv_detail_konfirmasi.setVisibility(View.VISIBLE);
                            }
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
                                    if (mList.size() < 3){
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
                            }
                            initRecyclerViewItem();
                            if (mList.size() == 0){
                                rl_empty_antrian.setVisibility(View.VISIBLE);
                            }else{
                                rl_empty_antrian.setVisibility(View.GONE);
                            }
                            isLoadQueue = true;
                            shimmerSet();
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

        tv_detail_konfirmasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenungguKonfirmasiFragment menungguKonfirmasiFragment = new MenungguKonfirmasiFragment();
                setFragment(menungguKonfirmasiFragment,"FragmentMenungguKonfirmasi");
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
        tv_info = getActivity().findViewById(R.id.info_text);
        tv_detail_janji = getActivity().findViewById(R.id.tv_tgl_janji);
        civ_dokter = getActivity().findViewById(R.id.civ_user);
        civ_pasien = getActivity().findViewById(R.id.civ_pasien);
        layout_pasien = getActivity().findViewById(R.id.layout_pasien);
        ib_next = getActivity().findViewById(R.id.ib_next);
        tv_detail_konfirmasi = getActivity().findViewById(R.id.tv_seekonfirmasi);
        rl_empty_antrian = getActivity().findViewById(R.id.layout_empty_konfirmasi_queue);
        layout_info = getActivity().findViewById(R.id.info_no_rekening_null);

        layout_visible = getActivity().findViewById(R.id.layout_visible);
        layout_loader = getActivity().findViewById(R.id.layout_loader);
        shimmerFrameLayout = getActivity().findViewById(R.id.shimmerLayout);

        recyclerView = getActivity().findViewById(R.id.rv_menunggu_konfirmasi);

    }

    private void shimmerSet(){
        if (isOnAttach){
            if (isLoadJanji && isLoadQueue && isLoadUser){
                shimmerFrameLayout.stopShimmer();
                layout_visible.setVisibility(View.VISIBLE);
                layout_loader.setVisibility(View.GONE);
            }
        }
    }

    private void startService() throws JSONException {
        Intent serviceIntent = new Intent(getContext(), NotificationService.class);
        serviceIntent.putExtra("data",CHANNEL_MESSAGES);
        getActivity().startService(serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(getContext(), NotificationService.class);
        ContextCompat.startForegroundService(getActivity(),serviceIntent);
    }

    private void startSocket() {
        Log.e("MedTek", "SOCOKET START...");
        try {
            socket = IO.socket(BASE_SOCKET_URL);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("SOCKETSOCKETAN", "Connected!");
                    JSONObject object = new JSONObject();
                    JSONObject auth = new JSONObject();
                    JSONObject headers = new JSONObject();

                    try {
                        object.put("channel", "private-App.User.Notification.Janji."+idDokter);
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
            })
//                    .on(Socket.EVENT_ERROR, new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//                    Log.e("SOCKETSOCKETAN", "Error!");
//                }
//            })
                    .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("SOCKETSOCKETAN", "Connect Error! :"  + args[0].toString());
                }
            }).on("new-janji", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        String s = args[1].toString();
                        JSONObject jsonObject = new JSONObject(s);
                        Call<ResponseBody> getUser = RetrofitClient.getInstance().getApi().
                                getPasienId(jsonObject.getJSONObject("janji").getInt("idPasien"));
                        getUser.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()){
                                    if (response.body() != null){
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    String responses = response.body().string();
                                                    JSONObject jsonObject = new JSONObject(responses);
                                                    CHANNEL_MESSAGES = "Hai dokter, Bpk. "+jsonObject.getJSONObject("data").getString("name")+" sedang menunggu konfirmasi anda. Cek sekarang yuk..";
                                                    startService();
                                                    Fragment frg = null;
                                                    frg = getActivity().getSupportFragmentManager().findFragmentByTag("FragmentHomeDokter");
                                                    final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                    ft.detach(frg);
                                                    ft.attach(frg);
                                                    ft.commit();
                                                }catch (IOException | JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },5000);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                            }
                        });
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

    private void setFragment(Fragment fragment,String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        isOnAttach = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isOnAttach = false;
    }
}
