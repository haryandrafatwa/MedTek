package com.example.medtek.Pasien.Home.Appointment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Others.NominalFragment;
import com.example.medtek.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class CheckoutAppointmentFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;

    private CircleImageView circleImageView;
    private TextView tv_edit_jadwal,tv_dr_name, tv_dr_specialist, tv_dr_rs, tv_dr_rs_loc;
    private Button btnNext;
    private ImageButton ib_edit_jadwal;

    private TextView tv_nama_dokter, tv_tgl_konsultasi, tv_jam_konsultasi, tv_harga, tv_total_harga, tv_saldo, tv_total_bayar, tv_sisa_saldo;

    private int saldo,idTransaksi;
    private String access, refresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout_appointment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        Bundle bundle = getArguments();

        Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
        callUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String s = response.body().string();
                    JSONObject object = new JSONObject(s);
                    JSONObject walletObj = object.getJSONObject("wallet");
                    saldo = walletObj.getInt("balance");
                    String balance = NumberFormat.getInstance(Locale.ITALIAN).format(saldo);
                    tv_saldo.setText("Rp"+balance);

                    tv_nama_dokter.setText(bundle.getString("nama_dokter"));

                    Locale locale = new Locale("in", "ID");
                    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM YYYY",locale);
                    DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEEE",locale);
                    LocalDate localDate = LocalDate.parse(bundle.getString("date"));

                    tv_tgl_konsultasi.setText(localDate.format(dateFormat));
                    tv_jam_konsultasi.setText(bundle.getString("time"));
                    String harga = NumberFormat.getInstance(Locale.ITALIAN).format(bundle.getInt("harga"));
                    tv_harga.setText("Rp"+harga);
                    tv_total_harga.setText("Rp"+harga);
                    tv_total_bayar.setText("Rp"+harga);
                    if (saldo < bundle.getInt("harga")){
                        tv_sisa_saldo.setText("Saldo Tidak Mencukupi");
                        tv_sisa_saldo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        btnNext.setText("Isi Ulang Sekarang");
                        btnNext.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                NominalFragment nominalFragment = new NominalFragment();
                                setFragment(nominalFragment,"FragmentNominal");
                            }
                        });
                    }else{
                        String sisaSaldo = NumberFormat.getInstance(Locale.ITALIAN).format(saldo-bundle.getInt("harga"));
                        tv_sisa_saldo.setText("Rp"+sisaSaldo);
                        btnNext.setText("Bayar Sekarang");
                        btnNext.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String detailJanji = localDate.format(dateFormat)+" Pukul "+bundle.getString("time");
                                Call<ResponseBody> buatJanji = RetrofitClient.getInstance().getApi().buatJanji("Bearer "+access,bundle.getInt("id_dokter"),bundle.getString("date"),detailJanji,localDate.format(dayFormat));
                                buatJanji.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            if (response.isSuccessful()){
                                                if (response.body() != null){
                                                    String s = response.body().string();
                                                    JSONObject object = new JSONObject(s);
                                                    if (object.has("success")){

                                                        Call<ResponseBody> getJanji = RetrofitClient.getInstance().getApi().getUserJanji("Bearer "+access);
                                                        getJanji.enqueue(new Callback<ResponseBody>() {
                                                            @Override
                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                try {
                                                                    if (response.isSuccessful()){
                                                                        if (response.body()!=null){
                                                                            String s = response.body().string();
                                                                            JSONObject jsonObject = new JSONObject(s);
                                                                            JSONArray jsonArray = jsonObject.optJSONArray("data");
                                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                                JSONObject object = jsonArray.getJSONObject(i);
                                                                                JSONObject transaksi = object.getJSONObject("transaksi");
                                                                                if (transaksi.getInt("is_paid") == 0){
                                                                                    idTransaksi = transaksi.getInt("id");
                                                                                    Call<ResponseBody> bayar = RetrofitClient.getInstance().getApi().bayar("Bearer "+access,idTransaksi);
                                                                                    bayar.enqueue(new Callback<ResponseBody>() {
                                                                                        @Override
                                                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                            try {
                                                                                                if (response.isSuccessful()) {
                                                                                                    if (response.body() != null) {
                                                                                                        String s = response.body().string();
                                                                                                        Log.e("BUATJANJI","idTransaksi:"+idTransaksi+", "+s);
                                                                                                        JSONObject object = new JSONObject(s);
                                                                                                        if (object.has("success")) {
                                                                                                            initializeDialogSuccess();
                                                                                                        }
                                                                                                    }
                                                                                                }else{
                                                                                                    Log.e("BUATJANJI",response.errorBody().string());
                                                                                                }
                                                                                            } catch (IOException | JSONException e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                                                        }
                                                                                    });
                                                                                    break;
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
                                                    }
                                                    Log.e("BUATJANJI",s);
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
                        });
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    callUser.clone().enqueue(this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

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
                    if (new JSONArray(obj.getString("image")).length() !=0){
                        JSONArray jsonArray = new JSONArray(obj.getString("image"));
                        path = "http://192.168.1.9:8000"+jsonArray.getJSONObject(0).getString("path");
                    }else{
                        path = "http://192.168.1.9:8000/storage/Dokter.png";
                    }
                    Picasso.get().load(path).fit().centerCrop().into(circleImageView);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

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
    }

    private void initializeDialogSuccess(){
        Dialog dialog = new Dialog(getActivity(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_payment_success,null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        dialogView.findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
            }
        });

        dialog.show();
    }

    private void initialize() {
        loadData(getActivity());
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        ib_edit_jadwal = getActivity().findViewById(R.id.ib_editjadwal);
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
        tv_harga = getActivity().findViewById(R.id.tv_biaya_layanan);
        tv_total_harga = getActivity().findViewById(R.id.tv_total_pembayaran);
        tv_saldo = getActivity().findViewById(R.id.tv_saldo);
        tv_total_bayar = getActivity().findViewById(R.id.tv_total_pembayaran_2);
        tv_sisa_saldo = getActivity().findViewById(R.id.tv_sisa_saldo);
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
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        this.access = sharedPreferences.getString("token", "");
        this.refresh = sharedPreferences.getString("refresh_token", "");
    }

}
