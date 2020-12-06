package com.example.medtek.ui.pasien.others;

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
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.medtek.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import me.abhinay.input.CurrencyEditText;
import me.abhinay.input.CurrencySymbols;

public class PembayaranFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;

    private CurrencyEditText cet_nominal;
    private EditText et_penerima, et_rekening_penerima, et_pengirim, et_rekening_pengirim;
    private Button btnNext;

    private int nominal;
    private String recipient, recipientNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pembayaran, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        Bundle bundle = getArguments();
        nominal = bundle.getInt("nominal");

        cet_nominal.setEnabled(false);
        cet_nominal.setCurrency(CurrencySymbols.INDONESIA);
        cet_nominal.setDelimiter(false);
        cet_nominal.setSpacing(false);
        cet_nominal.setDecimals(false);
        cet_nominal.setSeparator(".");
        cet_nominal.setText(String.valueOf(nominal));

        et_penerima.setText("PT. Medika Teknologi Sentosa");
        et_rekening_penerima.setText("90013888169");

        et_pengirim.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pengirim = s.toString().trim();
                String rekening = et_rekening_pengirim.getText().toString().trim();
                btnNext.setEnabled(!pengirim.isEmpty() && !rekening.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_rekening_pengirim.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pengirim = et_pengirim.getText().toString().trim();
                String rekening = s.toString().trim();
                btnNext.setEnabled(!pengirim.isEmpty() && !rekening.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pengirim = et_pengirim.getText().toString().trim();
                String rekening = et_rekening_pengirim.getText().toString().trim();
                BuktiTransferFragment buktiTransferFragment = new BuktiTransferFragment();
                buktiTransferFragment.setArguments(bundle);
                setFragment(buktiTransferFragment);

            }
        });

    }

    private void initialize() {
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        btnNext = getActivity().findViewById(R.id.btnNext);
        cet_nominal = getActivity().findViewById(R.id.cet_nominal);
        et_penerima = getActivity().findViewById(R.id.et_penerima);
        et_rekening_penerima = getActivity().findViewById(R.id.et_nomorrekening);
        et_pengirim = getActivity().findViewById(R.id.et_pengirim);
        et_rekening_pengirim = getActivity().findViewById(R.id.et_nomorrekening_sender);
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
