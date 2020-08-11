package com.example.medtek.Pasien.Others;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jem.rubberpicker.RubberSeekBar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import me.abhinay.input.CurrencyEditText;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class WalletFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private String access, refresh;

    private TextView tv_now_date, tv_saldo;
    private ImageButton ib_topup;
    private LinearLayout ll_topup;

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

        ib_topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NominalFragment nominalFragment = new NominalFragment();
                setFragment(nominalFragment,"FragmentNominal");

                /*BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.fragment_topup, (RelativeLayout) getActivity().findViewById(R.id.dialogTopup));
                bottomSheetView.findViewById(R.id.nominal).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                CurrencyEditText et_nominal = (CurrencyEditText) bottomSheetView.findViewById(R.id.et_nominal_topup);

                RubberSeekBar rsb_nominal = bottomSheetView.findViewById(R.id.rsb_nominal);
                rsb_nominal.setOnRubberSeekBarChangeListener(new RubberSeekBar.OnRubberSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(@NotNull RubberSeekBar rubberSeekBar, int i, boolean b) {
                        i = ((int)Math.round(i/10000 ))*10000;
//                        et_nominal.setText("Rp "+String.valueOf(i));
                    }

                    @Override
                    public void onStartTrackingTouch(@NotNull RubberSeekBar rubberSeekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(@NotNull RubberSeekBar rubberSeekBar) {

                    }
                });
                bottomSheetDialog.setCancelable(false);
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();*/

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

    }

    private void setFragment(Fragment fragment,String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(TAG);
        fragmentTransaction.commit();
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
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        this.access = sharedPreferences.getString("token", "");
        this.refresh = sharedPreferences.getString("refresh_token", "");
    }

}
