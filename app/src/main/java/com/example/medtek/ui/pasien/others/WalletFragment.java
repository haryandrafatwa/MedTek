package com.example.medtek.ui.pasien.others;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
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

import com.example.medtek.R;
import com.example.medtek.model.pasien.TransactionModel;
import com.example.medtek.network.RetrofitClient;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
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

import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;

public class WalletFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private String access, refresh, fName, lName, alamat, phone;

    private TextView tv_now_date, tv_saldo;
    private ImageButton ib_topup;
    private LinearLayout ll_topup;

    private TextView today, yesterday;
    private RelativeLayout rl_empty_history,rl_content;
    private final List<TransactionModel> mListToday = new ArrayList<>();
    private final List<TransactionModel> mListYesterday = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    private WalletAdapter mAdapterToday;
    private WalletAdapter mAdapterYesterday;
    private RecyclerView rv_today, rv_yesterday;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        AndroidThreeTen.init(getActivity());

        loadData(getActivity());
        Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
        callUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        String s = response.body().string();
                        JSONObject raw = new JSONObject(s);
                        String name = raw.getString("name");
                        fName = name.split(" ")[0];
                        lName = name.split(" ")[1];
                        phone = raw.getString("notelp");
                        JSONObject alamatObj = raw.getJSONObject("alamat");
                        alamat = alamatObj.getString("jalan")+", No. "+alamatObj.getString("nomor_bangunan")
                                +", RT/RW. "+alamatObj.getString("rtrw")+", "+alamatObj.getString("kelurahan")
                                +", "+alamatObj.getString("kecamatan")+", "+alamatObj.getString("kota");
                        JSONObject wallet = raw.getJSONObject("wallet");
                        String balance = wallet.getString("balance");
                        balance = NumberFormat.getInstance(Locale.ITALIAN).format(Integer.valueOf(balance));

                        Locale locale = new Locale("in","ID");
                        Calendar calendar = Calendar.getInstance();

                        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE",locale);
                        String hari = dayFormat.format(calendar.getTime());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM YYYY",locale);
                        String tanggal = dateFormat.format(calendar.getTime());

                        tv_now_date.setText(getString(R.string.day)+" "+hari+", "+tanggal);
                        tv_saldo.setText("Rp"+balance);

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

        Call<ResponseBody> getTransaction = RetrofitClient.getInstance().getApi().getTransaction("Bearer "+access);
        getTransaction.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        String s = response.body().string();
                        JSONObject raw = new JSONObject(s);
                        if (raw.getJSONArray("data").length() == 0){
                            rl_empty_history.setVisibility(View.VISIBLE);
                            rl_content.setVisibility(View.GONE);
                        }else{
                            mListYesterday.clear();
                            mListToday.clear();
                            for (int i = 0; i < raw.getJSONArray("data").length(); i++) {
                                JSONObject jsonObject = raw.getJSONArray("data").getJSONObject(i);

                                Date date;
                                Locale locale = new Locale("in", "ID");
                                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale);
                                try {
                                    date = format.parse(jsonObject.getString("created_at"));
                                    Calendar calendar = Calendar.getInstance();
                                    Calendar calendarNow = Calendar.getInstance();
                                    calendar.setTime(date);
                                    String month_name = new java.text.SimpleDateFormat("MMMM", locale).format(calendar.getTime());
                                    if(calendar.get(Calendar.DAY_OF_MONTH) == calendarNow.get(Calendar.DAY_OF_MONTH)){
                                        if(calendar.get(Calendar.MONTH) == calendarNow.get(Calendar.MONTH)){
                                            if(calendar.get(Calendar.YEAR) == calendarNow.get(Calendar.YEAR)){
                                                if (mListToday.size() <= 5){
                                                    mListToday.add(new TransactionModel(jsonObject.getInt("id"), jsonObject.getInt("type_id"),jsonObject.getInt("totalHarga"),calendar.get(Calendar.DATE)+" "+month_name+" "+calendar.get(Calendar.YEAR)));
                                                }
                                            }
                                        }
                                    }else if(calendar.get(Calendar.DAY_OF_MONTH) == calendarNow.get(Calendar.DAY_OF_MONTH)-1){
                                        if(calendar.get(Calendar.MONTH) == calendarNow.get(Calendar.MONTH)){
                                            if(calendar.get(Calendar.YEAR) == calendarNow.get(Calendar.YEAR)){
                                                if (mListYesterday.size() <= 5){
                                                    mListYesterday.add(new TransactionModel(jsonObject.getInt("id"), jsonObject.getInt("type_id"),jsonObject.getInt("totalHarga"),calendar.get(Calendar.DATE)+" "+month_name+" "+calendar.get(Calendar.YEAR)));
                                                }
                                            }
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            Collections.reverse(mListToday);
                            initRecyclerViewItem();
                            if (mListToday.size() == 0){
                                rv_today.setVisibility(View.GONE);
                                today.setVisibility(View.GONE);
                            }else{
                                rv_today.setVisibility(View.VISIBLE);
                                today.setVisibility(View.VISIBLE);
                            }

                            if (mListYesterday.size() == 0){
                                rv_yesterday.setVisibility(View.GONE);
                                yesterday.setVisibility(View.GONE);
                            }else{
                                rv_yesterday.setVisibility(View.VISIBLE);
                                yesterday.setVisibility(View.VISIBLE);
                            }
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

        ib_topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*NominalFragment nominalFragment = new NominalFragment();
                Bundle bundle = new Bundle();
                bundle.putString("FirstName",fName);
                bundle.putString("LastName",lName);
                bundle.putString("Alamat",alamat);
                bundle.putString("Phone",phone);
                nominalFragment.setArguments(bundle);
                setFragment(nominalFragment,"FragmentNominal");*/
                Toasty.info(getActivity(),"Under Maintenance!",Toasty.LENGTH_LONG).show();
            }
        });

        ll_topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_topup.performClick();
            }
        });

    }

    private void initialize() {

        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        tv_now_date = getActivity().findViewById(R.id.tv_date);
        tv_saldo = getActivity().findViewById(R.id.tv_saldo);

        ib_topup = getActivity().findViewById(R.id.ib_topup);
        ll_topup = getActivity().findViewById(R.id.layout_topup);

        today = getActivity().findViewById(R.id.today);
        yesterday = getActivity().findViewById(R.id.yesterday);
        rl_empty_history = getActivity().findViewById(R.id.layout_empty_transaction);
        rl_content = getActivity().findViewById(R.id.layout_content);

    }

    private void setFragment(Fragment fragment,String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    private void initRecyclerViewItem(){
        rv_today = getActivity().findViewById(R.id.rv_today_transaction);
        rv_yesterday = getActivity().findViewById(R.id.rv_yesterday_transaction);
        mAdapterToday = new WalletAdapter(mListToday,getActivity().getApplicationContext(),getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        rv_today.setAdapter(mAdapterToday);

        mAdapterYesterday = new WalletAdapter(mListYesterday,getActivity().getApplicationContext(),getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        rv_yesterday.setAdapter(mAdapterYesterday);
    }

    private void setToolbar() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chevron_left, null);
        drawable.setColorFilter(new PorterDuffColorFilter(getActivity().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
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
    }

}
