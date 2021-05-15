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
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.medtek.BuildConfig;
import com.example.medtek.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jem.rubberpicker.RubberSeekBar;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.PaymentMethod;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.models.BillingAddress;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import me.abhinay.input.CurrencyEditText;
import me.abhinay.input.CurrencySymbols;

public class NominalFragment extends Fragment implements TransactionFinishedCallback {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;

    private RubberSeekBar rubberSeekBar;
    private CurrencyEditText currencyEditText;

    private LinearLayout ll_20rb, ll_50rb, ll_100rb, ll_200rb, ll_500rb, ll_1jt;
    private Button btnBayar;

    private String fName, lName, alamat, notelp;

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
        getArgument();
        initialize();
        initMidTrans();

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
                int nominal;
                if (cleanString.equalsIgnoreCase("")){
                    nominal = 20000;
                }else{
                    nominal = Integer.parseInt(cleanString);
                }
                if (cleanString.isEmpty()){
                    Toasty.error(getActivity(),getResources().getString(R.string.silahkanisinominal)).show();
                }else{
                    if (nominal < getResources().getInteger(R.integer.minSeekBar)){
                        Toasty.info(getActivity(),getResources().getString(R.string.minTopup)).show();
                    }else if (nominal > 10000000){
                        Toasty.info(getActivity(),getResources().getString(R.string.maxTopup)).show();
                    }else{
                        /*PembayaranFragment pembayaranFragment = new PembayaranFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("nominal",nominal);
                        pembayaranFragment.setArguments(bundle);
                        setFragment(pembayaranFragment);*/
                        TransactionRequest request = new TransactionRequest(System.currentTimeMillis()+" ",20000);
                        BillingAddress billingAddress = new BillingAddress();
                        billingAddress.setAddress(alamat);
                        CustomerDetails customerDetails = new CustomerDetails();
                        customerDetails.setFirstName(fName);
                        customerDetails.setLastName(lName);
                        customerDetails.setPhone(notelp);
                        customerDetails.setBillingAddress(billingAddress);
                        ItemDetails itemDetails = new ItemDetails("1",nominal,1,"Topup Saldo Sebesar "+String.valueOf(nominal));
                        ArrayList<com.midtrans.sdk.corekit.models.ItemDetails> details = new ArrayList<>();
                        details.add(itemDetails);
                        request.setCustomerDetails(customerDetails);
                        request.setItemDetails(details);
                        MidtransSDK.getInstance().setTransactionRequest(request);
                        MidtransSDK.getInstance().startPaymentUiFlow(getActivity(), PaymentMethod.BANK_TRANSFER);
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

    private void getArgument(){
        fName = getArguments().getString("FirstName");
        lName = getArguments().getString("LastName");
        alamat = getArguments().getString("Alamat");
        notelp = getArguments().getString("Phone");
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

    private void initMidTrans(){
        SdkUIFlowBuilder.init()
                .setContext(getActivity())
                .setMerchantBaseUrl(BuildConfig.MERCHANT_BASE_URL+"/api/payment/snap/")
                .setClientKey(BuildConfig.MERCHANT_CLIENT_KEY)
                .enableLog(true)
                .setTransactionFinishedCallback(this)
                .buildSDK();
    }

    @Override
    public void onTransactionFinished(TransactionResult transactionResult) {
        if (transactionResult.getResponse()!=null){
            switch (transactionResult.getStatus()){
                case TransactionResult.STATUS_SUCCESS:
                    Toasty.success(getActivity(),"Transaksi Berhasil",Toasty.LENGTH_LONG).show();
                    break;
                case TransactionResult.STATUS_PENDING:
                    Toasty.warning(getActivity(),"Transaksi Pending",Toasty.LENGTH_LONG).show();
                    break;
                case TransactionResult.STATUS_FAILED:
                    Toasty.error(getActivity(),"Transaksi Gagal",Toasty.LENGTH_LONG).show();
                    break;
            }
            transactionResult.getResponse().getValidationMessages();
        }else if(transactionResult.isTransactionCanceled()){
            Toasty.error(getActivity(),"Transaksi Batal",Toasty.LENGTH_LONG).show();
        }else{
            if (transactionResult.getStatus().equalsIgnoreCase(TransactionResult.STATUS_INVALID)){
                Toasty.error(getActivity(),"Transaksi Salah",Toasty.LENGTH_LONG).show();
            }else{
                Toasty.error(getActivity(),"Transaksi Selesai dengan Kesalahan",Toasty.LENGTH_LONG).show();
            }
        }
    }
}
