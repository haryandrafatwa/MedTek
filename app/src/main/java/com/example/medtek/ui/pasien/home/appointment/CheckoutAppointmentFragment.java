package com.example.medtek.ui.pasien.home.appointment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.medtek.BuildConfig;
import com.example.medtek.R;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.network.request.JanjiRequest;
import com.example.medtek.ui.pasien.home.HomeFragment;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.midtrans.sdk.corekit.callback.GetTransactionStatusCallback;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.UIKitCustomSetting;
import com.midtrans.sdk.corekit.models.BillingAddress;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.snap.Authentication;
import com.midtrans.sdk.corekit.models.snap.CreditCard;
import com.midtrans.sdk.corekit.models.snap.Gopay;
import com.midtrans.sdk.corekit.models.snap.Shopeepay;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.corekit.models.snap.TransactionStatusResponse;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.socket.client.Socket;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;

public class CheckoutAppointmentFragment extends Fragment implements TransactionFinishedCallback{

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;

    private CircleImageView circleImageView;
    private TextView tv_edit_jadwal,tv_dr_name, tv_dr_specialist, tv_dr_rs, tv_dr_rs_loc;
    private Button btnNext;
    private ImageButton ib_edit_jadwal, ib_chevron;
    private Bundle bundle;
    private ProgressDialog progressDialog;

    private RelativeLayout rl_content,relativeContent;
    private LinearLayout ll_loader, linearLoader, ll_payment_method, ll_item_payment_method, ll_choose_wallet, ll_choose_others, ll_wallet_choosed, ll_other_choosed, ll_dompet_visible;
    private ShimmerFrameLayout shimmerFrameLayout, SFL;

    private TextView tv_choose_payment, tv_nama_dokter, tv_tgl_konsultasi, tv_jam_konsultasi, tv_harga, tv_total_harga, tv_saldo, tv_total_bayar, tv_sisa_saldo, tv_layanan, tv_kode_unik;

    private int saldo,idJanji, totalBayar,layananInt;
    private String access, refresh, fName, lName, alamat, phone, email, snapToken, payMethod, detailJanji, day,metode;
    private SdkUIFlowBuilder sdkUIFlowBuilder;

    private Socket socket;
    private final String SERVER_URL = "http://192.168.137.1:6001";
    private final String CHANNEL_MESSAGES = "messages";
    private final String EVENT_MESSAGE_CREATED = "MessageCreated";
    private final String TAG = "msg";
    private boolean status;

    private Uri docUri;
    private String filename;

    private boolean isChoosed = false, isShowed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout_appointment, container, false);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        initMidTrans("MERCHANT_BASE_URL");
        bundle = getArguments();
        snapToken = bundle.getString("snapToken");

        ib_edit_jadwal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getActivity().onBackPressed();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack("FragmentBuatJanji",0);
            }
        });
        tv_edit_jadwal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_edit_jadwal.performClick();
            }
        });

        ll_payment_method.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowed){
                    ib_chevron.setBackground(getActivity().getDrawable(R.drawable.ic_chevron_up));
                    ll_payment_method.setBackground(getActivity().getDrawable(R.drawable.bg_form_input_rounded_top));
                    ll_item_payment_method.setVisibility(View.VISIBLE);
                    isShowed = true;
                }else{
                    ib_chevron.setBackground(getActivity().getDrawable(R.drawable.ic_chevron_down));
                    ll_payment_method.setBackground(getActivity().getDrawable(R.drawable.bg_form_input));
                    ll_item_payment_method.setVisibility(View.GONE);
                    isShowed = false;
                }
            }
        });

        ll_choose_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_choose_payment.setVisibility(View.GONE);
                ll_wallet_choosed.setVisibility(View.VISIBLE);
                ll_other_choosed.setVisibility(View.GONE);
                btnNext.setVisibility(View.VISIBLE);
                isChoosed = true;
                isShowed = false;
                payMethod = "Wallet";
                ib_chevron.setBackground(getActivity().getDrawable(R.drawable.ic_chevron_down));
                ll_payment_method.setBackground(getActivity().getDrawable(R.drawable.bg_form_input));
                ll_item_payment_method.setVisibility(View.GONE);
                ll_dompet_visible.setVisibility(View.VISIBLE);
                int sisa = saldo-totalBayar;
                String sisaSaldo = NumberFormat.getInstance(Locale.ITALIAN).format(sisa);
                tv_sisa_saldo.setText("Rp"+sisaSaldo);
                btnNext.setText("Bayar Sekarang");
                if (saldo < totalBayar){
                    tv_sisa_saldo.setTextColor(getActivity().getColor(R.color.colorPrimary));
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressDialog.show();
                            if (bundle.getString("lastFragment").equalsIgnoreCase("DetailDokter")){
                                setIdJanji(bundle.getInt("id_janji"));
                                Log.e(TAG, "onClick: DetailDokter ->"+snapToken );
                                if (!snapToken.equalsIgnoreCase("null")){
                                    ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                    ArrayList<ItemDetails> details = new ArrayList<>();
                                    details.add(itemDetails);
                                    TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                    request.setItemDetails(details);
                                    progressDialog.dismiss();
                                    MidtransSDK.getInstance().setTransactionRequest(request);
                                    MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
                                }else{
                                    Call<ResponseBody> payment = RetrofitClient.getInstance().getApi().payment("Bearer "+access,idJanji);
                                    payment.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.body() != null){
                                                try {
                                                    JSONObject responseObj = new JSONObject(response.body().string());
                                                    Log.e(TAG, "onResponse: "+response.body().string());
                                                    snapToken = responseObj.getString("token");
                                                    ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                                    ArrayList<ItemDetails> details = new ArrayList<>();
                                                    details.add(itemDetails);
                                                    TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                                    request.setItemDetails(details);
                                                    progressDialog.dismiss();
                                                    MidtransSDK.getInstance().setTransactionRequest(request);
                                                    MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
                                                } catch (IOException | JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            payment.clone().enqueue(this);
                                        }
                                    });
                                }
                            }else{
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
                                                                snapToken = transObj.getString("snapToken");
                                                                metode = transObj.getString("method");
                                                            }else{
                                                                setStatus(true);
                                                            }
                                                        }
                                                    }else{
                                                        setStatus(true);
                                                    }
                                                    if (status){
                                                        JanjiRequest request = new JanjiRequest(bundle.getInt("id_dokter"), bundle.getString("date"), detailJanji, day);
                                                        Call<ResponseBody> buatJanji = RetrofitClient.getInstance().getApi().buatJanji("Bearer "+access, request);
                                                        buatJanji.enqueue(new Callback<ResponseBody>() {
                                                            @Override
                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                try {
                                                                    if (response.isSuccessful()){
                                                                        if (response.body() != null){
                                                                            String s = response.body().string();
                                                                            JSONObject object = new JSONObject(s);
                                                                            if (object.has("success")){
                                                                                getView().setOnKeyListener(new View.OnKeyListener() {
                                                                                    @Override
                                                                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                                                                        if (event.getAction() ==KeyEvent.ACTION_DOWN){
                                                                                            if( keyCode == KeyEvent.KEYCODE_BACK) {
                                                                                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                                                manager.popBackStack();
                                                                                                manager.popBackStack();
                                                                                                manager.popBackStack();
                                                                                                return true;
                                                                                            }
                                                                                        }
                                                                                        return false;
                                                                                    }
                                                                                });
                                                                                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View v) {
                                                                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                                        manager.popBackStack();
                                                                                        manager.popBackStack();
                                                                                        manager.popBackStack();
                                                                                    }
                                                                                });
                                                                                JSONObject janjiSuccess = object.getJSONObject("data");
                                                                                setIdJanji(janjiSuccess.getInt("id"));
                                                                                if (bundle.getString("uri") != null) {
                                                                                    docUri = Uri.parse(bundle.getString("uri"));
                                                                                    String mimeType = getActivity().getContentResolver().getType(docUri);
                                                                                    try {
                                                                                        String fileName = getFileName(docUri);
                                                                                        File outputDir = getActivity().getCacheDir(); // context being the Activity pointer
                                                                                        File outputFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1], outputDir);

                                                                                        File fileCopy = copyToTempFile(docUri, outputFile);
                                                                                        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), fileCopy);
                                                                                        MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);
                                                                                        Call<ResponseBody> uploadFile = RetrofitClient.getInstance().getApi().uploadFile("Bearer " + access, idJanji, part);
                                                                                        uploadFile.enqueue(new Callback<ResponseBody>() {
                                                                                            @Override
                                                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                                try {
                                                                                                    if (response.isSuccessful()) {
                                                                                                        if (response.body() != null) {
                                                                                                            String respons = response.body().string();
                                                                                                            JSONObject uploadObj = new JSONObject(respons);
                                                                                                            Log.e("TAG", "onActivityResult: " + respons);
                                                                                                            if (uploadObj.has("success")) {
                                                                                                                Map<String, Object> jsonParams = new ArrayMap<>();
                                                                                                                jsonParams.put("idJanji",idJanji);
                                                                                                                jsonParams.put("wallet",true);
                                                                                                                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());
                                                                                                                Call<ResponseBody> payment = RetrofitClient.getInstance().getApi().paymentWallet("Bearer "+access,body);
                                                                                                                payment.enqueue(new Callback<ResponseBody>() {
                                                                                                                    @Override
                                                                                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                                                        if (response.body() != null){
                                                                                                                            try {
                                                                                                                                Log.e(TAG, "onResponse: "+response.body().string());
                                                                                                                                JSONObject responseObj = new JSONObject(response.body().string());
                                                                                                                                snapToken = responseObj.getString("token");
                                                                                                                                ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                                                                                                                ArrayList<ItemDetails> details = new ArrayList<>();
                                                                                                                                details.add(itemDetails);
                                                                                                                                TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                                                                                                                request.setItemDetails(details);
                                                                                                                                progressDialog.dismiss();
                                                                                                                                MidtransSDK.getInstance().setTransactionRequest(request);
                                                                                                                                MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
                                                                                                                            } catch (IOException | JSONException e) {
                                                                                                                                e.printStackTrace();
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }

                                                                                                                    @Override
                                                                                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                                                        payment.clone().enqueue(this);
                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        } else {
                                                                                                            String respons = response.errorBody().string();
                                                                                                            Log.e("TAG", "onActivityResult: " + respons);
                                                                                                        }
                                                                                                    } else {
                                                                                                        String respons = response.errorBody().string();
                                                                                                        Log.e("TAG", "onActivityResult: " + respons);
                                                                                                    }
                                                                                                } catch (IOException | JSONException e) {
                                                                                                    e.printStackTrace();
                                                                                                    Log.e("TAG", "onActivityResult: " + e.getMessage());
                                                                                                    uploadFile.clone().enqueue(this);
                                                                                                }
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                                Log.e("TAG", "Failure: " + t.getMessage());
                                                                                            }
                                                                                        });
                                                                                    } catch (Exception e) {
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                }else{
                                                                                    Map<String, Object> jsonParams = new ArrayMap<>();
                                                                                    jsonParams.put("idJanji",idJanji);
                                                                                    jsonParams.put("wallet",true);
                                                                                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());
                                                                                    Call<ResponseBody> payment = RetrofitClient.getInstance().getApi().paymentWallet("Bearer "+access,body);
                                                                                    payment.enqueue(new Callback<ResponseBody>() {
                                                                                        @Override
                                                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                            if (response.body() != null){
                                                                                                try {
                                                                                                    JSONObject responseObj = new JSONObject(response.body().string());
                                                                                                    Log.e(TAG, "onResponse: "+response.body().string());
                                                                                                    snapToken = responseObj.getString("token");
                                                                                                    ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                                                                                    ArrayList<ItemDetails> details = new ArrayList<>();
                                                                                                    details.add(itemDetails);
                                                                                                    TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                                                                                    request.setItemDetails(details);
                                                                                                    progressDialog.dismiss();
                                                                                                    MidtransSDK.getInstance().setTransactionRequest(request);
                                                                                                    MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
                                                                                                } catch (IOException | JSONException e) {
                                                                                                    e.printStackTrace();
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                            payment.clone().enqueue(this);
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }catch (IOException | JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                            }
                                                        });
                                                    }else{
                                                        setIdJanji(bundle.getInt("id_janji"));
                                                        ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                                        ArrayList<ItemDetails> details = new ArrayList<>();
                                                        details.add(itemDetails);
                                                        TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                                        request.setItemDetails(details);
                                                        progressDialog.dismiss();
                                                        MidtransSDK.getInstance().setTransactionRequest(request);
                                                        MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
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
                            }
                        }
                    });
                }else{
                    tv_sisa_saldo.setTextColor(getActivity().getColor(R.color.colorAccent));
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressDialog.show();
                            if (bundle.getString("lastFragment").equalsIgnoreCase("DetailDokter")){
                                setIdJanji(bundle.getInt("id_janji"));
                                Call<ResponseBody> bayarWallet = RetrofitClient.getInstance().getApi().bayar("Bearer "+access,idJanji);
                                bayarWallet.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.body() != null){
                                            if (response.isSuccessful()){
                                                try {
                                                    String s = response.body().string();
                                                    JSONObject bayarObj = new JSONObject(s);
                                                    if (bayarObj.has("success")){
                                                        initializeDialogSuccess();
                                                    }
                                                } catch (IOException | JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                                    }
                                });
                            }else{
                                JanjiRequest request = new JanjiRequest(bundle.getInt("id_dokter"), bundle.getString("date"), detailJanji, day);
                                Call<ResponseBody> buatJanji = RetrofitClient.getInstance().getApi().buatJanji("Bearer "+access, request);
                                buatJanji.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            if (response.isSuccessful()){
                                                if (response.body() != null){
                                                    String s = response.body().string();
                                                    JSONObject object = new JSONObject(s);
                                                    if (object.has("success")){
                                                        getView().setOnKeyListener(new View.OnKeyListener() {
                                                            @Override
                                                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                                                if (event.getAction() ==KeyEvent.ACTION_DOWN){
                                                                    if( keyCode == KeyEvent.KEYCODE_BACK) {
                                                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                        manager.popBackStack();
                                                                        manager.popBackStack();
                                                                        manager.popBackStack();
                                                                        return true;
                                                                    }
                                                                }
                                                                return false;
                                                            }
                                                        });
                                                        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                manager.popBackStack();
                                                                manager.popBackStack();
                                                                manager.popBackStack();
                                                            }
                                                        });
                                                        JSONObject janjiSuccess = object.getJSONObject("data");
                                                        setIdJanji(janjiSuccess.getInt("id"));
                                                        if (bundle.getString("uri") != null){
                                                            docUri = Uri.parse(bundle.getString("uri"));
                                                            String mimeType = getActivity().getContentResolver().getType(docUri);
                                                            try {
                                                                String fileName = getFileName(docUri);
                                                                File outputDir = getActivity().getCacheDir(); // context being the Activity pointer
                                                                File outputFile = File.createTempFile(fileName.split("\\.")[0], "."+fileName.split("\\.")[1], outputDir);

                                                                File fileCopy = copyToTempFile(docUri, outputFile);
                                                                RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType),fileCopy);
                                                                MultipartBody.Part part = MultipartBody.Part.createFormData("file",fileName,requestBody);
                                                                Call<ResponseBody> uploadFile = RetrofitClient.getInstance().getApi().uploadFile("Bearer "+access, idJanji, part);
                                                                uploadFile.enqueue(new Callback<ResponseBody>() {
                                                                    @Override
                                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                        try {
                                                                            if (response.isSuccessful()){
                                                                                if (response.body() != null){
                                                                                    String respons = response.body().string();
                                                                                    JSONObject uploadObj = new JSONObject(respons);
                                                                                    Log.e("TAG", "onActivityResult: "+respons);
                                                                                    if (uploadObj.has("success")){
                                                                                        Call<ResponseBody> bayarWallet = RetrofitClient.getInstance().getApi().bayar("Bearer "+access,idJanji);
                                                                                        bayarWallet.enqueue(new Callback<ResponseBody>() {
                                                                                            @Override
                                                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                                if (response.body() != null){
                                                                                                    if (response.isSuccessful()){
                                                                                                        try {
                                                                                                            String s = response.body().string();
                                                                                                            JSONObject bayarObj = new JSONObject(s);
                                                                                                            if (bayarObj.has("success")){
                                                                                                                initializeDialogSuccess();
                                                                                                            }
                                                                                                        } catch (IOException | JSONException e) {
                                                                                                            e.printStackTrace();
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }else{
                                                                                    String respons = response.errorBody().string();
                                                                                    Log.e("TAG", "onActivityResult: "+respons);
                                                                                }
                                                                            }else{
                                                                                String respons = response.errorBody().string();
                                                                                Log.e("TAG", "onActivityResult: "+respons);
                                                                            }
                                                                        } catch (IOException | JSONException e) {
                                                                            e.printStackTrace();
                                                                            Log.e("TAG", "onActivityResult: "+e.getMessage());
                                                                            uploadFile.clone().enqueue(this);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                        Log.e("TAG", "Failure: "+t.getMessage());
                                                                    }
                                                                });
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }else{
                                                            Call<ResponseBody> bayarWallet = RetrofitClient.getInstance().getApi().bayar("Bearer "+access,idJanji);
                                                            bayarWallet.enqueue(new Callback<ResponseBody>() {
                                                                @Override
                                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                    if (response.body() != null){
                                                                        if (response.isSuccessful()){
                                                                            try {
                                                                                String s = response.body().string();
                                                                                JSONObject bayarObj = new JSONObject(s);
                                                                                if (bayarObj.has("success")){
                                                                                    progressDialog.dismiss();
                                                                                    initializeDialogSuccess();
                                                                                }
                                                                            } catch (IOException | JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        }catch (IOException | JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        ll_choose_others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_choose_payment.setVisibility(View.GONE);
                ll_wallet_choosed.setVisibility(View.GONE);
                ll_other_choosed.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                isChoosed = true;
                isShowed = false;
                payMethod = "Others";
                ib_chevron.setBackground(getActivity().getDrawable(R.drawable.ic_chevron_down));
                ll_payment_method.setBackground(getActivity().getDrawable(R.drawable.bg_form_input));
                ll_item_payment_method.setVisibility(View.GONE);
                ll_dompet_visible.setVisibility(View.GONE);
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog.show();
                        if (bundle.getString("lastFragment").equalsIgnoreCase("DetailDokter")){
                            setIdJanji(bundle.getInt("id_janji"));
                            if (!snapToken.equalsIgnoreCase("null")){
                                ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                ArrayList<ItemDetails> details = new ArrayList<>();
                                details.add(itemDetails);
                                TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                request.setItemDetails(details);
                                progressDialog.dismiss();
                                MidtransSDK.getInstance().setTransactionRequest(request);
                                MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
                            }else{
                                Call<ResponseBody> payment = RetrofitClient.getInstance().getApi().payment("Bearer "+access,idJanji);
                                payment.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.body() != null){
                                            try {
                                                JSONObject responseObj = new JSONObject(response.body().string());
                                                Log.e(TAG, "onResponse: "+response.body().string());
                                                snapToken = responseObj.getString("token");
                                                ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                                ArrayList<ItemDetails> details = new ArrayList<>();
                                                details.add(itemDetails);
                                                TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                                request.setItemDetails(details);
                                                progressDialog.dismiss();
                                                MidtransSDK.getInstance().setTransactionRequest(request);
                                                MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        payment.clone().enqueue(this);
                                    }
                                });
                            }
                        }else{
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
                                                            snapToken = transObj.getString("snapToken");
                                                            metode = transObj.getString("method");
                                                        }else{
                                                            setStatus(true);
                                                        }
                                                    }
                                                }else{
                                                    setStatus(true);
                                                }
                                                if (status){
                                                    JanjiRequest request = new JanjiRequest(bundle.getInt("id_dokter"), bundle.getString("date"), detailJanji, day);
                                                    Call<ResponseBody> buatJanji = RetrofitClient.getInstance().getApi().buatJanji("Bearer "+access, request);
                                                    buatJanji.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                            try {
                                                                if (response.isSuccessful()){
                                                                    if (response.body() != null){
                                                                        String s = response.body().string();
                                                                        JSONObject object = new JSONObject(s);
                                                                        if (object.has("success")){
                                                                            getView().setOnKeyListener(new View.OnKeyListener() {
                                                                                @Override
                                                                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                                                                    if (event.getAction() ==KeyEvent.ACTION_DOWN){
                                                                                        if( keyCode == KeyEvent.KEYCODE_BACK) {
                                                                                            FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                                            manager.popBackStack();
                                                                                            manager.popBackStack();
                                                                                            manager.popBackStack();
                                                                                            return true;
                                                                                        }
                                                                                    }
                                                                                    return false;
                                                                                }
                                                                            });
                                                                            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(View v) {
                                                                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                                    manager.popBackStack();
                                                                                    manager.popBackStack();
                                                                                    manager.popBackStack();
                                                                                }
                                                                            });
                                                                            JSONObject janjiSuccess = object.getJSONObject("data");
                                                                            setIdJanji(janjiSuccess.getInt("id"));
                                                                            if (bundle.getString("uri") != null) {
                                                                                docUri = Uri.parse(bundle.getString("uri"));
                                                                                String mimeType = getActivity().getContentResolver().getType(docUri);
                                                                                try {
                                                                                    String fileName = getFileName(docUri);
                                                                                    File outputDir = getActivity().getCacheDir(); // context being the Activity pointer
                                                                                    File outputFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1], outputDir);

                                                                                    File fileCopy = copyToTempFile(docUri, outputFile);
                                                                                    RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), fileCopy);
                                                                                    MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);
                                                                                    Call<ResponseBody> uploadFile = RetrofitClient.getInstance().getApi().uploadFile("Bearer " + access, idJanji, part);
                                                                                    uploadFile.enqueue(new Callback<ResponseBody>() {
                                                                                        @Override
                                                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                            try {
                                                                                                if (response.isSuccessful()) {
                                                                                                    if (response.body() != null) {
                                                                                                        String respons = response.body().string();
                                                                                                        JSONObject uploadObj = new JSONObject(respons);
                                                                                                        Log.e("TAG", "onActivityResult: " + respons);
                                                                                                        if (uploadObj.has("success")) {
                                                                                                            Call<ResponseBody> payment = RetrofitClient.getInstance().getApi().payment("Bearer "+access,idJanji);
                                                                                                            payment.enqueue(new Callback<ResponseBody>() {
                                                                                                                @Override
                                                                                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                                                    if (response.body() != null){
                                                                                                                        try {
                                                                                                                            Log.e(TAG, "onResponse: "+response.body().string());
                                                                                                                            JSONObject responseObj = new JSONObject(response.body().string());
                                                                                                                            snapToken = responseObj.getString("token");
                                                                                                                            ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                                                                                                            ArrayList<ItemDetails> details = new ArrayList<>();
                                                                                                                            details.add(itemDetails);
                                                                                                                            TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                                                                                                            request.setItemDetails(details);
                                                                                                                            progressDialog.dismiss();
                                                                                                                            MidtransSDK.getInstance().setTransactionRequest(request);
                                                                                                                            MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
                                                                                                                        } catch (IOException | JSONException e) {
                                                                                                                            e.printStackTrace();
                                                                                                                        }
                                                                                                                    }
                                                                                                                }

                                                                                                                @Override
                                                                                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                                                    payment.clone().enqueue(this);
                                                                                                                }
                                                                                                            });
                                                                                                        }
                                                                                                    } else {
                                                                                                        String respons = response.errorBody().string();
                                                                                                        Log.e("TAG", "onActivityResult: " + respons);
                                                                                                    }
                                                                                                } else {
                                                                                                    String respons = response.errorBody().string();
                                                                                                    Log.e("TAG", "onActivityResult: " + respons);
                                                                                                }
                                                                                            } catch (IOException | JSONException e) {
                                                                                                e.printStackTrace();
                                                                                                Log.e("TAG", "onActivityResult: " + e.getMessage());
                                                                                                uploadFile.clone().enqueue(this);
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                            Log.e("TAG", "Failure: " + t.getMessage());
                                                                                        }
                                                                                    });
                                                                                } catch (Exception e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            }else{
                                                                                Call<ResponseBody> payment = RetrofitClient.getInstance().getApi().payment("Bearer "+access,idJanji);
                                                                                payment.enqueue(new Callback<ResponseBody>() {
                                                                                    @Override
                                                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                        if (response.body() != null){
                                                                                            try {
                                                                                                JSONObject responseObj = new JSONObject(response.body().string());
                                                                                                Log.e(TAG, "onResponse: "+response.body().string());
                                                                                                snapToken = responseObj.getString("token");
                                                                                                ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                                                                                ArrayList<ItemDetails> details = new ArrayList<>();
                                                                                                details.add(itemDetails);
                                                                                                TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                                                                                request.setItemDetails(details);
                                                                                                progressDialog.dismiss();
                                                                                                MidtransSDK.getInstance().setTransactionRequest(request);
                                                                                                MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
                                                                                            } catch (IOException | JSONException e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                        payment.clone().enqueue(this);
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }catch (IOException | JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                        }
                                                    });
                                                }else{
                                                    setIdJanji(bundle.getInt("id_janji"));
                                                    ItemDetails itemDetails = new ItemDetails("1",totalBayar,1,"Konsultasi dengan "+tv_dr_name.getText().toString());
                                                    ArrayList<ItemDetails> details = new ArrayList<>();
                                                    details.add(itemDetails);
                                                    TransactionRequest request = initTransactionRequest(totalBayar,phone,fName,lName,email,alamat);
                                                    request.setItemDetails(details);
                                                    progressDialog.dismiss();
                                                    MidtransSDK.getInstance().setTransactionRequest(request);
                                                    MidtransSDK.getInstance().startPaymentUiFlow(getActivity(),snapToken);
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
                        }
                    }
                });
            }
        });

        Call<ResponseBody> callDokter = RetrofitClient.getInstance().getApi().getDokterId(bundle.getInt("id_dokter"));
        callDokter.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String s = response.body().string();
                    JSONObject object = new JSONObject(s);
                    JSONObject obj = new JSONObject(object.getString("data"));
                    tv_dr_name.setText(obj.getString("name"));
                    JSONObject specObj = new JSONObject(obj.getString("specialization"));
                    String spec = specObj.getString("specialization");
                    tv_dr_specialist.setText(spec);
                    JSONObject rsObject = new JSONObject(obj.getString("hospital"));
                    String rs_name = rsObject.getString("name");
                    JSONObject alamatObject = new JSONObject(obj.getString("alamat"));
                    String kelurahan = alamatObject.getString("kelurahan");
                    String kecamatan = alamatObject.getString("kecamatan");
                    String kota = alamatObject.getString("kota");
                    String rs_loc = kecamatan+", "+kota;
                    tv_dr_rs.setText(rs_name);
                    tv_dr_rs_loc.setText(rs_loc);
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
                    }else{
                        path = BASE_URL+"/storage/Dokter.png";
                    }
                    Picasso.get().load(path).into(circleImageView);
                    Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
                    callUser.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String s = response.body().string();
                                JSONObject object = new JSONObject(s);
                                String userName = obj.getString("name");
                                String[] userNameArr = userName.split(" ");
                                fName = userNameArr[0];
                                lName = "";
                                if (userNameArr.length > 1) {
                                    lName = userName.split(" ")[1];
                                }
                                phone = object.getString("notelp");
                                email = object.getString("email");
                                JSONObject alamatObj = object.getJSONObject("alamat");
                                alamat = alamatObj.getString("jalan")+", No. "+alamatObj.getString("nomor_bangunan")
                                        +", RT/RW. "+alamatObj.getString("rtrw")+", "+alamatObj.getString("kelurahan")
                                        +", "+alamatObj.getString("kecamatan")+", "+alamatObj.getString("kota");
                                JSONObject walletObj = object.getJSONObject("wallet");
                                saldo = walletObj.getInt("balance");
                                String balance = NumberFormat.getInstance(Locale.ITALIAN).format(saldo);
                                tv_saldo.setText("Rp"+balance);

                                tv_nama_dokter.setText(bundle.getString("nama_dokter"));

                                Locale locale = new Locale("in", "ID");
                                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM YYYY",locale);
                                LocalDate localDate = LocalDate.parse(bundle.getString("date"));

                                tv_tgl_konsultasi.setText(localDate.format(dateFormat));
                                tv_jam_konsultasi.setText(bundle.getString("time"));
                                String harga = NumberFormat.getInstance(Locale.ITALIAN).format(bundle.getInt("harga"));
                                String uniqueCode = NumberFormat.getInstance(Locale.ITALIAN).format(object.getInt("id"));

                                DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEEE",locale);

                                Call<ResponseBody> getServiceFee = RetrofitClient.getInstance().getApi().getServiceFee();
                                getServiceFee.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            if (response.isSuccessful()){
                                                if (response.body() != null){
                                                    String s = response.body().string();
                                                    JSONObject jsonObject = new JSONObject(s);
                                                    layananInt = jsonObject.getJSONObject("data").getInt("servicefee");
                                                    totalBayar = object.getInt("id")+layananInt+bundle.getInt("harga");
                                                    String layanan = NumberFormat.getInstance(Locale.ITALIAN).format(jsonObject.getJSONObject("data").getInt("servicefee"));
                                                    tv_layanan.setText("Rp"+layanan);
                                                    String total = NumberFormat.getInstance(Locale.ITALIAN).format(totalBayar);
                                                    tv_total_harga.setText("Rp"+total);
                                                    tv_total_bayar.setText("Rp"+total);
                                                    tv_harga.setText("Rp"+harga);
                                                    tv_kode_unik.setText("Rp"+uniqueCode);
                                                    setDay(localDate.format(dayFormat).toLowerCase());
                                                    setDetailJanji(localDate.format(dateFormat)+" Pukul "+bundle.getString("time")+"\n\nKeluhan:\n"+bundle.getString("detailJanji"));

                                                    if (bundle.containsKey("method")){
                                                        metode = bundle.getString("method");
                                                        Log.e(TAG, "onStart: "+metode );
                                                        if (metode.equalsIgnoreCase("hybrid")){
                                                            ll_choose_wallet.performClick();
                                                            ll_payment_method.setClickable(false);
                                                        }else if(metode.equalsIgnoreCase("snap")){
                                                            ll_choose_others.performClick();
                                                            ll_payment_method.setClickable(false);
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

                                ll_loader.setVisibility(View.GONE);
                                shimmerFrameLayout.stopShimmer();
                                rl_content.setVisibility(View.VISIBLE);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                callUser.clone().enqueue(this);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void setIdJanji(int idJanji) {
        this.idJanji = idJanji;
    }

    private void initMidTrans(String event){
        if (event.equalsIgnoreCase("OUTSIDE")){
            sdkUIFlowBuilder.init()
                    .setContext(getActivity())
                    .setMerchantBaseUrl(BuildConfig.MERCHANT_BASE_URL_OUTSIDE)
                    .setClientKey(BuildConfig.MERCHANT_CLIENT_KEY)
                    .enableLog(true)
                    .setTransactionFinishedCallback(this)
                    .setUIkitCustomSetting(uiKitCustomSetting())
                    .buildSDK();
        }else{
            sdkUIFlowBuilder.init()
                    .setContext(getActivity())
                    .setMerchantBaseUrl(BuildConfig.MERCHANT_BASE_URL)
                    .setClientKey(BuildConfig.MERCHANT_CLIENT_KEY)
                    .enableLog(true)
                    .setTransactionFinishedCallback(this)
                    .setUIkitCustomSetting(uiKitCustomSetting())
                    .buildSDK();
        }
    }

    private void initializeDialogSuccess(){
        progressDialog.dismiss();
        Dialog dialog = new Dialog(getActivity(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_payment_success,null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        RelativeLayout relativeContent = dialogView.findViewById(R.id.layout_visible);
        ShimmerFrameLayout SFL = dialogView.findViewById(R.id.shimmerLayout);
        SFL.startShimmer();
        LinearLayout linearLoader = dialogView.findViewById(R.id.layout_loader);

        SFL.stopShimmer();
        relativeContent.setVisibility(View.VISIBLE);
        linearLoader.setVisibility(View.GONE);

        dialogView.findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                for(int i = 0; i < fm.getBackStackEntryCount()-1; ++i) {
                    fm.popBackStack();
                }
            }
        });

        dialog.show();
    }

    /**
     * Obtains the file name for a URI using content resolvers. Taken from the following link
     * https://developer.android.com/training/secure-file-sharing/retrieve-info.html#RetrieveFileInfo
     *
     * @param uri a uri to query
     * @return the file name with no path
     * @throws IllegalArgumentException if the query is null, empty, or the column doesn't exist
     */
    private String getFileName(Uri uri) throws IllegalArgumentException {
        // Obtain a cursor with information regarding this uri
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
        }

        cursor.moveToFirst();

        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

        cursor.close();

        return fileName;
    }

    /**
     * Copies a uri reference to a temporary file
     *
     * @param uri      the uri used as the input stream
     * @param tempFile the file used as an output stream
     * @return the input tempFile for convenience
     * @throws IOException if an error occurs
     */
    private File copyToTempFile(Uri uri, File tempFile) throws IOException {
        // Obtain an input stream from the uri
        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new IOException("Unable to obtain input stream from URI");
        }

        // Copy the stream to the temp file
        FileUtils.copyInputStreamToFile(inputStream, tempFile);

        return tempFile;
    }

    private void initialize() {
        loadData(getActivity());
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        shimmerFrameLayout = getActivity().findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.startShimmer();

        rl_content = getActivity().findViewById(R.id.layout_visible);
        ll_loader = getActivity().findViewById(R.id.layout_loader);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        ll_payment_method = getActivity().findViewById(R.id.layout_metode_pembayaran);
        ll_item_payment_method = getActivity().findViewById(R.id.layout_item_metode_pembayaran);
        ll_choose_wallet =getActivity().findViewById(R.id.layout_choose_wallet);
        ll_choose_others = getActivity().findViewById(R.id.layout_choose_other);
        ll_wallet_choosed = getActivity().findViewById(R.id.layout_wallet_choosed);
        ll_other_choosed = getActivity().findViewById(R.id.layout_other_choosed);
        ll_dompet_visible = getActivity().findViewById(R.id.dompet_visible);

        ib_edit_jadwal = getActivity().findViewById(R.id.ib_editjadwal);
        ib_chevron = getActivity().findViewById(R.id.ib_chevron);
        tv_edit_jadwal = getActivity().findViewById(R.id.tv_edit_jadwal);
        circleImageView = getActivity().findViewById(R.id.civ_dokter);
        tv_dr_name = getActivity().findViewById(R.id.tv_dr_name);
        tv_dr_specialist = getActivity().findViewById(R.id.tv_dr_special);
        tv_dr_rs = getActivity().findViewById(R.id.tv_dr_rs);
        tv_dr_rs_loc = getActivity().findViewById(R.id.tv_dr_rs_loc);
        btnNext = getActivity().findViewById(R.id.btnNext);

        tv_nama_dokter = getActivity().findViewById(R.id.tv_nama_dokter);
        tv_tgl_konsultasi = getActivity().findViewById(R.id.tv_tanggal_buatjanji);
        tv_jam_konsultasi = getActivity().findViewById(R.id.tv_jam_buatjanji);
        tv_harga = getActivity().findViewById(R.id.tv_biaya_konsultasi);
        tv_total_harga = getActivity().findViewById(R.id.tv_total_pembayaran);
        tv_saldo = getActivity().findViewById(R.id.tv_saldo);
        tv_total_bayar = getActivity().findViewById(R.id.tv_total_pembayaran_2);
        tv_sisa_saldo = getActivity().findViewById(R.id.tv_sisa_saldo);
        tv_layanan = getActivity().findViewById(R.id.tv_biaya_layanan);
        tv_kode_unik = getActivity().findViewById(R.id.tv_unique_code);
        tv_choose_payment = getActivity().findViewById(R.id.tv_choose_payment);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Mohon tunggu ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    private void setFragment(Fragment fragment,String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    public void setDetailJanji(String detailJanji) {
        this.detailJanji = detailJanji;
    }

    public void setDay(String day) {
        this.day = day;
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

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

    @Override
    public void onTransactionFinished(TransactionResult transactionResult) {
        if (transactionResult.getResponse()!=null){
            switch (transactionResult.getStatus()){
                case TransactionResult.STATUS_SUCCESS:
                    initializeDialogSuccess();
                    break;
                case TransactionResult.STATUS_PENDING:
                    MidtransSDK.getInstance().getTransactionStatus(snapToken, new GetTransactionStatusCallback() {
                        @Override
                        public void onSuccess(TransactionStatusResponse response) {
                            // do action for response
                            initializeDialogSuccess();
                        }

                        @Override
                        public void onFailure(TransactionStatusResponse response, String reason) {
                            // do nothing
                            Toasty.warning(getActivity(),"Transaksi "+response.getTransactionStatus(),Toasty.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(Throwable error) {
                            // do action if error
                            Toasty.error(getActivity(),"Transaksi Error: "+error.getMessage(),Toasty.LENGTH_LONG).show();
                        }
                    });
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

    private UIKitCustomSetting uiKitCustomSetting(){
        UIKitCustomSetting uIKitCustomSetting = new UIKitCustomSetting();
        uIKitCustomSetting.setSkipCustomerDetailsPages(true);
        uIKitCustomSetting.setShowPaymentStatus(true);
        return uIKitCustomSetting;
    }

    private TransactionRequest initTransactionRequest(double bayar, String phone, String fName, String lName, String email, String alamat) {
        // Create new Transaction Request
        TransactionRequest transactionRequestNew = new
                TransactionRequest(System.currentTimeMillis() + "", bayar);
        transactionRequestNew.setCustomerDetails(initCustomerDetails(phone, fName, lName, email, alamat));

        CreditCard creditCard = new CreditCard();
        creditCard.setSaveCard(false);
        creditCard.setAuthentication(Authentication.AUTH_3DS);

        transactionRequestNew.setCreditCard(creditCard);
        transactionRequestNew.setGopay(new Gopay("demo:://midtrans"));
        transactionRequestNew.setShopeepay(new Shopeepay("demo:://midtrans"));

        return transactionRequestNew;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    private CustomerDetails initCustomerDetails(String phone, String fName, String lName, String email, String alamat) {
        //define customer detail (mandatory for coreflow)
        CustomerDetails mCustomerDetails = new CustomerDetails();
        mCustomerDetails.setPhone(phone);
        mCustomerDetails.setFirstName(fName);
        mCustomerDetails.setLastName(lName);
        mCustomerDetails.setEmail(email);
        mCustomerDetails.setCustomerIdentifier(email);

        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setAddress(alamat);
        billingAddress.setFirstName(fName);
        billingAddress.setLastName(lName);
        billingAddress.setPhone(phone);
        mCustomerDetails.setBillingAddress(billingAddress);
        return mCustomerDetails;
    }
}
