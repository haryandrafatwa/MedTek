package com.example.medtek.Pasien.Others;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.medtek.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jem.rubberpicker.RubberSeekBar;

import org.jetbrains.annotations.NotNull;

import es.dmoral.toasty.Toasty;
import me.abhinay.input.CurrencyEditText;
import me.abhinay.input.CurrencySymbols;

public class NominalFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;

    private RubberSeekBar rubberSeekBar;
    private CurrencyEditText currencyEditText;

    private LinearLayout ll_20rb, ll_50rb, ll_100rb, ll_200rb, ll_500rb, ll_1jt;
    private Button btnBayar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_topup, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        currencyEditText.setCurrency(CurrencySymbols.INDONESIA);
        currencyEditText.setDelimiter(false);
        currencyEditText.setSpacing(false);
        currencyEditText.setDecimals(false);
        currencyEditText.setSeparator(".");

        rubberSeekBar.setOnRubberSeekBarChangeListener(new RubberSeekBar.OnRubberSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@NotNull RubberSeekBar rubberSeekBar, int i, boolean b) {
                i = ((int)Math.round(i/10000 ))*10000;
                currencyEditText.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(@NotNull RubberSeekBar rubberSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(@NotNull RubberSeekBar rubberSeekBar) {

            }
        });
        currencyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("Rp")){
                    String cleanString = s.toString().replaceAll("[Rp,.]", "");
                    switch (cleanString){
                        case "20000" :
                            ll_20rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                            ll_50rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_100rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_200rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_500rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_1jt.setBackground(getActivity().getDrawable(R.drawable.bg_default));

                            break;

                        case "50000" :
                            ll_20rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_50rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                            ll_100rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_200rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_500rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_1jt.setBackground(getActivity().getDrawable(R.drawable.bg_default));

                            break;

                        case "100000" :
                            ll_20rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_50rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_100rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                            ll_200rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_500rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_1jt.setBackground(getActivity().getDrawable(R.drawable.bg_default));

                            break;

                        case "200000" :
                            ll_20rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_50rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_100rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_200rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                            ll_500rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_1jt.setBackground(getActivity().getDrawable(R.drawable.bg_default));

                            break;

                        case "500000" :
                            ll_20rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_50rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_100rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_200rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_500rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                            ll_1jt.setBackground(getActivity().getDrawable(R.drawable.bg_default));

                            break;

                        case "1000000" :
                            ll_20rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_50rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_100rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_200rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_500rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_1jt.setBackground(getActivity().getDrawable(R.drawable.bg_selected));

                            break;

                        default:
                            ll_20rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_50rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_100rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_200rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_500rb.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            ll_1jt.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            break;
                    }
                }else{
                    currencyEditText.setText("Rp");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ll_20rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_20rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                currencyEditText.setText("20000");
            }
        });

        ll_50rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_50rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                currencyEditText.setText("50000");
            }
        });

        ll_100rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_100rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                currencyEditText.setText("100000");
            }
        });

        ll_200rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_200rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                currencyEditText.setText("200000");
            }
        });

        ll_500rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_500rb.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                currencyEditText.setText("500000");
            }
        });

        ll_1jt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_1jt.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                currencyEditText.setText("1000000");
            }
        });

        btnBayar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cleanString = currencyEditText.getText().toString().trim().replaceAll("[Rp,.]", "");
                int nominal = Integer.parseInt(cleanString);
                if (cleanString.isEmpty()){
                    Toasty.error(getActivity(),getResources().getString(R.string.silahkanisinominal)).show();
                }else{
                    if (nominal < getResources().getInteger(R.integer.minSeekBar)){
                        Toasty.info(getActivity(),getResources().getString(R.string.minTopup)).show();
                    }else if (nominal > 10000000){
                        Toasty.info(getActivity(),getResources().getString(R.string.maxTopup)).show();
                    }else{
                        PembayaranFragment pembayaranFragment = new PembayaranFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("nominal",nominal);
                        pembayaranFragment.setArguments(bundle);
                        setFragment(pembayaranFragment);
                    }
                }
            }
        });

    }

    private void initialize() {

        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);
        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        rubberSeekBar = getActivity().findViewById(R.id.rsb_nominal);
        currencyEditText = getActivity().findViewById(R.id.et_nominal_topup);

        ll_1jt = getActivity().findViewById(R.id.ll_1000rb);
        ll_500rb = getActivity().findViewById(R.id.ll_500rb);
        ll_200rb = getActivity().findViewById(R.id.ll_200rb);
        ll_100rb = getActivity().findViewById(R.id.ll_100rb);
        ll_50rb = getActivity().findViewById(R.id.ll_50rb);
        ll_20rb = getActivity().findViewById(R.id.ll_20rb);

        btnBayar = getActivity().findViewById(R.id.btn_bayar);

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

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(null);
        fragmentTransaction.commit();
    }

}
