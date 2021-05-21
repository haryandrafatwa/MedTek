package com.example.medtek.ui.pasien.home.doctors;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.pasien.FeedbackModel;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.ui.activity.MainActivity;
import com.example.medtek.ui.activity.WelcomePageActivity;
import com.example.medtek.ui.pasien.home.HomeFragment;
import com.example.medtek.ui.pasien.home.appointment.BuatJanjiFragment;
import com.example.medtek.ui.pasien.home.appointment.CheckoutAppointmentFragment;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.LOGIN_STATUS;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.PropertyUtil.searchData;
import static com.example.medtek.utils.Utils.TAG;

public class DetailDokterFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private final List<FeedbackModel> mList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private FeedbackAdapter mAdapter;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private boolean status = false;

    private ChipNavigationBar bottomNavigationView;
    private Toolbar toolbar;

    private TextView tv_dr_name, tv_dr_spec, tv_dr_exp, tv_dr_rs, tv_dr_rs_loc, tv_dr_grd, tv_dr_rat, tv_dr_tot_rev;
    private ImageButton ib_share, ib_rs_loc;
    private Button btnBuatJanji;
    private CircleImageView civ_dokter;
    private RatingBar ratingBar;

    private RelativeLayout rl_content,rl_loader;
    private ShimmerFrameLayout shimmerFrameLayout;
    private String access = "", refresh = "", nama, nama_dokter, detail_janji, date, time, snapToken;
    private int harga, balance, id_doc, idJanji;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_doctor, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        loadData(getActivity());

        Bundle bundle = getArguments();
        int id_dokter = bundle.getInt("id_doc");

        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().getDokterId(id_dokter);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body()!=null){
                        if (response.isSuccessful()){
                            String s = response.body().string();
                            JSONObject object = new JSONObject(s);
                            JSONObject obj = new JSONObject(object.getString("data"));
                            tv_dr_name.setText(obj.getString("name"));
                            JSONObject specObj = new JSONObject(obj.getString("specialization"));
                            String spec = specObj.getString("specialization");
                            tv_dr_spec.setText(spec);
                            tv_dr_exp.setText((obj.isNull("lama_kerja") ? "-" : obj.getString("lama_kerja"))+" "+getActivity().getResources().getString(R.string.tahun)+" "+getActivity().getResources().getString(R.string.exp));
                            JSONObject rsObject = new JSONObject(obj.getString("hospital"));
                            String rs_name = rsObject.getString("name");
                            JSONObject alamatObject = new JSONObject(obj.getString("alamat"));
                            String kelurahan = alamatObject.getString("kelurahan");
                            String kota = alamatObject.getString("kota");
                            String rs_loc = kelurahan+", "+kota;
                            tv_dr_rs.setText(rs_name);
                            tv_dr_rs_loc.setText(rs_loc);
                            tv_dr_grd.setText(obj.getString("lulusan"));
                            String path="";

                            JSONArray jsonArrayImage = new JSONArray(obj.getString("image"));
                            if (new JSONArray(obj.getString("image")).length() !=0){
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

                            initRecyclerViewItem();
                            if (new JSONArray(obj.getString("feedback")).length() !=0){
                                JSONArray jsonArray = new JSONArray(obj.getString("feedback"));
                                tv_dr_tot_rev.setText("("+jsonArray.length()+" "+getActivity().getResources().getString(R.string.review)+")");
                                float rating = 0;
                                mList.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject feedbackObj = jsonArray.getJSONObject(i);
                                    float rat = Float.valueOf(feedbackObj.getString("rating"));
                                    rating+=rat;
                                    int id = feedbackObj.getInt("id");
                                    String message = feedbackObj.getString("message");
                                    String post_date = feedbackObj.getString("created_at");
                                    Call<ResponseBody> callReviewer = RetrofitClient.getInstance().getApi().getFeedbackId(id);
                                    callReviewer.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try {
                                                String s = response.body().string();
                                                JSONObject feedbackObj = new JSONObject(s);
                                                JSONObject dataObj = feedbackObj.getJSONObject("data");
                                                JSONObject pasienObj = dataObj.getJSONObject("pasien");
                                                String name = pasienObj.getString("name");
                                                mList.add(new FeedbackModel(id,name,message,post_date,"",rat));
                                                mAdapter.notifyDataSetChanged();
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            callReviewer.clone().enqueue(this);
                                        }
                                    });
                                }
                                ratingBar.setRating(rating/jsonArray.length());
                                tv_dr_rat.setText(rating/jsonArray.length()+"/5.0");
                            }else{
                                ratingBar.setRating(0);
                                tv_dr_rat.setText(0+"/5.0");
                                tv_dr_tot_rev.setText(getActivity().getResources().getString(R.string.blmadaulasan));
                            }

                            Picasso.get().load(path).into(civ_dokter, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    rl_content.setVisibility(View.VISIBLE);
                                    rl_loader.setVisibility(View.GONE);
                                    shimmerFrameLayout.stopShimmer();
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            });

                            ib_rs_loc.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                                    View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.bottom_sheet_rs_detail, (RelativeLayout) getActivity().findViewById(R.id.bottomSheetRSDetail));
                                    TextView tv_dr_rs = bottomSheetView.findViewById(R.id.tv_dr_rs);
                                    TextView tv_dr_rs_loc = bottomSheetView.findViewById(R.id.tv_dr_rs_loc);
                                    tv_dr_rs.setText(rs_name);
                                    tv_dr_rs_loc.setText(rs_loc);
                                    bottomSheetView.findViewById(R.id.tv_direct_me).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                    Uri.parse("google.navigation:q="+rs_name));
                                            startActivity(intent);
                                        }
                                    });
                                    bottomSheetDialog.setContentView(bottomSheetView);
                                    bottomSheetDialog.show();
                                }
                            });

                            btnBuatJanji.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    progressDialog.show();
                                    if (!access.equals("") && !refresh.equals("")) {
                                        Call<ResponseBody> getUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
                                        getUser.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                try{
                                                    if (response.isSuccessful()){
                                                        if (response.body() != null){
                                                            String s = response.body().string();
                                                            JSONObject user = new JSONObject(s);
                                                            nama = user.getString("name");
                                                            JSONObject walletObj = user.getJSONObject("wallet");
                                                            balance = walletObj.getInt("balance");
                                                            if (!user.isNull("email_verified_at")){
                                                        /*BuatJanjiFragment buatJanjiFragment = new BuatJanjiFragment();
                                                        Bundle bundle = new Bundle();
                                                        bundle.putInt("id_dokter",id_dokter);
                                                        buatJanjiFragment.setArguments(bundle);
                                                        setFragment(buatJanjiFragment,"FragmentBuatJanji");*/
                                                                Call<ResponseBody> getUserJanji = RetrofitClient.getInstance().getApi().getUserJanji("Bearer "+access);
                                                                getUserJanji.enqueue(new Callback<ResponseBody>() {
                                                                    @Override
                                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                        try {
                                                                            if (response.isSuccessful()) {
                                                                                if (response.body() != null) {
                                                                                    String s = response.body().string();
                                                                                    JSONObject object = new JSONObject(s);
                                                                                    JSONArray objArr = object.getJSONArray("data");
                                                                                    if (objArr.length() > 0){
                                                                                        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
                                                                                        for (int i = 0; i < objArr.length(); i++) {
                                                                                            jsonValues.add(objArr.getJSONObject(i));
                                                                                        }
                                                                                        Collections.sort(jsonValues, new Comparator<JSONObject>() {
                                                                                            @Override
                                                                                            public int compare(JSONObject o1, JSONObject o2) {
                                                                                                int idA = 0;
                                                                                                int idB = 0;
                                                                                                try {
                                                                                                    idA = o1.getInt("id");
                                                                                                    idB = o2.getInt("id");
                                                                                                } catch (JSONException e) {
                                                                                                    e.printStackTrace();
                                                                                                }
                                                                                                return -Integer.compare(idA,idB);
                                                                                            }
                                                                                        });
                                                                                        JSONArray sortedJanji = new JSONArray();
                                                                                        for (int i = 0; i < objArr.length(); i++) {
                                                                                            sortedJanji.put(jsonValues.get(i));
                                                                                        }
                                                                                        JSONObject janjiObj = sortedJanji.getJSONObject(0);
                                                                                        JSONObject doctObj = janjiObj.getJSONObject("dokter");
                                                                                        JSONArray transArr = janjiObj.getJSONArray("transaksi");
                                                                                        idJanji = janjiObj.getInt("id");
                                                                                        for (int j = 0; j < transArr.length(); j++) {
                                                                                            JSONObject transObj = transArr.getJSONObject(j);
                                                                                            if (!transObj.getBoolean("is_paid")){
                                                                                                setStatus(false);
                                                                                                nama_dokter = doctObj.getString("name");
                                                                                                id_doc = doctObj.getInt("id");
                                                                                                harga = doctObj.getInt("harga");
                                                                                                detail_janji = janjiObj.getString("detailJanji");
                                                                                                date = janjiObj.getString("tglJanji");
                                                                                                snapToken = transObj.getString("snapToken");
                                                                                            }else{
                                                                                                setStatus(true);
                                                                                            }
                                                                                        }
                                                                                    }else{
                                                                                        setStatus(true);
                                                                                    }
                                                                                    if (!status){
                                                                                        bundle.putString("nama",nama);
                                                                                        bundle.putString("nama_dokter",nama_dokter);
                                                                                        bundle.putInt("harga",harga);
                                                                                        bundle.putInt("balance",balance);
                                                                                        bundle.putInt("id_dokter",id_doc);
                                                                                        bundle.putInt("id_janji",idJanji);
                                                                                        bundle.putString("date",date);
                                                                                        bundle.putString("snapToken",snapToken);
                                                                                        bundle.putString("lastFragment","DetailDokter");

                                                                                        bundle.putString("time",detail_janji.split("Pukul")[1].split("\n")[0]);
                                                                                        bundle.putString("detailJanji",detail_janji);
                                                                                        progressDialog.dismiss();
                                                                                        Log.e(TAG, "onResponse: idJanji ->"+idJanji);
                                                                                        CheckoutAppointmentFragment checkoutAppointmentFragment = new CheckoutAppointmentFragment();
                                                                                        checkoutAppointmentFragment.setArguments(bundle);
                                                                                        setFragment(checkoutAppointmentFragment,"FragmentCheckoutAppointment");
                                                                                    }else{
                                                                                        progressDialog.dismiss();
                                                                                        Fragment buatJanjiFragment = new BuatJanjiFragment();
                                                                                        Bundle bundle = new Bundle();
                                                                                        bundle.putInt("id_dokter",id_dokter);
                                                                                        buatJanjiFragment.setArguments(bundle);
                                                                                        setFragment(buatJanjiFragment,"FragmentBuatJanji");
                                                                                    }
                                                                                }else{
                                                                                    getUserJanji.clone().enqueue(this);
                                                                                }
                                                                            }else{
                                                                                getUserJanji.clone().enqueue(this);
                                                                            }
                                                                        } catch (IOException|JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                        getUserJanji.clone().enqueue(this);
                                                                    }
                                                                });
                                                            }else{
                                                                Toasty.error(getActivity(),"Silahkan verifikasi akun terlebih dahulu!").show();
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
                                    } else {
                                        navigateWelcome();
                                    }
                                }
                            });
                        }else{
                            call.clone().enqueue(this);
                        }
                    }else{
                        call.clone().enqueue(this);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    call.clone().enqueue(this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        ib_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,getActivity().getString(R.string.shareSubject));
                shareIntent.putExtra(Intent.EXTRA_TEXT,getActivity().getString(R.string.shareBody)+"www.medtek.telemedicine.co.id/ProfileDoctor/"+id_dokter);
                startActivity(Intent.createChooser(shareIntent, getActivity().getString(R.string.shareTitle)));
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

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Mohon tunggu ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        tv_dr_name = getActivity().findViewById(R.id.tv_dr_name);
        tv_dr_spec = getActivity().findViewById(R.id.tv_dr_special);
        tv_dr_exp = getActivity().findViewById(R.id.tv_dr_exp);
        tv_dr_rs = getActivity().findViewById(R.id.tv_dr_rs);
        tv_dr_rs_loc = getActivity().findViewById(R.id.tv_dr_loc);
        tv_dr_grd = getActivity().findViewById(R.id.tv_dr_graduate);
        tv_dr_rat = getActivity().findViewById(R.id.tv_dr_rat);
        tv_dr_tot_rev = getActivity().findViewById(R.id.tv_dr_tot_rev);

        ib_share = getActivity().findViewById(R.id.ib_share);
        ib_rs_loc = getActivity().findViewById(R.id.ib_rs_loc);
        btnBuatJanji = getActivity().findViewById(R.id.btnBuatJanji);

        civ_dokter = getActivity().findViewById(R.id.civ_dokter);
        ratingBar = getActivity().findViewById(R.id.rb_dokter);
        rl_content = getActivity().findViewById(R.id.layout_content);
        rl_loader = getActivity().findViewById(R.id.layout_loader);

        recyclerView = getActivity().findViewById(R.id.rv_feedback);
    }

    private void initRecyclerViewItem(){
        mAdapter = new FeedbackAdapter(mList,getActivity().getApplicationContext(),getActivity());
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

    private void setFragment(Fragment fragment,String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment,TAG).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");
        if (searchData(LOGIN_STATUS) || searchData(ACCESS_TOKEN)) {
            if ((boolean) getData(LOGIN_STATUS)) {
                this.access = (String) getData(ACCESS_TOKEN);
                this.refresh = (String) getData(REFRESH_TOKEN);
            }
        }
    }

    private void navigateWelcome() {
        Intent home = new Intent(getContext(), WelcomePageActivity.class);
        startActivity(home);
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
