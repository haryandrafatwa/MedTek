package com.example.medtek.Pasien.Home.Appointment;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import com.example.medtek.Utils.FileUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private Bundle bundle;

    private RelativeLayout rl_content;
    private LinearLayout ll_loader;
    private ShimmerFrameLayout shimmerFrameLayout;

    private TextView tv_nama_dokter, tv_tgl_konsultasi, tv_jam_konsultasi, tv_harga, tv_total_harga, tv_saldo, tv_total_bayar, tv_sisa_saldo, tv_layanan, tv_kode_unik;

    private int saldo,idJanji,idTransaksi, totalBayar;
    private String access, refresh;

    private Socket socket;
    private String SERVER_URL = "http://192.168.137.1:6001";
    private String CHANNEL_MESSAGES = "messages";
    private String EVENT_MESSAGE_CREATED = "MessageCreated";
    private String TAG = "msg";

    private Uri docUri;
    private String filename;

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
        bundle = getArguments();

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
                        path = "http://192.168.137.1:8000"+jsonArray.getJSONObject(0).getString("path");
                    }else{
                        path = "http://192.168.137.1:8000/storage/Dokter.png";
                    }
                    Picasso.get().load(path).into(circleImageView);
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
                                LocalDate localDate = LocalDate.parse(bundle.getString("date"));

                                tv_tgl_konsultasi.setText(localDate.format(dateFormat));
                                tv_jam_konsultasi.setText(bundle.getString("time"));
                                String harga = NumberFormat.getInstance(Locale.ITALIAN).format(bundle.getInt("harga"));
                                String uniqueCode = NumberFormat.getInstance(Locale.ITALIAN).format(object.getInt("id"));
                                totalBayar = object.getInt("id")+11000+bundle.getInt("harga");
                                String total = NumberFormat.getInstance(Locale.ITALIAN).format(totalBayar);

                                Call<ResponseBody> getServiceFee = RetrofitClient.getInstance().getApi().getServiceFee();
                                getServiceFee.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            if (response.isSuccessful()){
                                                if (response.body() != null){
                                                    String s = response.body().string();
                                                    JSONObject jsonObject = new JSONObject(s);
                                                    String layanan = NumberFormat.getInstance(Locale.ITALIAN).format(jsonObject.getJSONObject("data").getInt("servicefee"));
                                                    tv_layanan.setText("Rp"+layanan);
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

                                tv_harga.setText("Rp"+harga);
                                tv_kode_unik.setText("Rp"+uniqueCode);
                                tv_total_harga.setText("Rp"+total);
                                tv_total_bayar.setText("Rp"+total);
                                if (saldo < totalBayar){
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
                                    int sisa = saldo-totalBayar;
                                    String sisaSaldo = NumberFormat.getInstance(Locale.ITALIAN).format(sisa);
                                    Log.e(TAG, "onResponse: "+sisaSaldo+"; "+sisa );
                                    tv_sisa_saldo.setText("Rp"+sisaSaldo);
                                    btnNext.setText("Bayar Sekarang");
                                    btnNext.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String detailJanji = localDate.format(dateFormat)+" Pukul "+bundle.getString("time")+"\n\nKeluhan:\n"+bundle.getString("detailJanji");
                                            initializeDialogSuccess(detailJanji,localDate);
                                        }
                                    });
                                }
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

    private void initializeDialogSuccess(String detailJanji, LocalDate localDate){
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

        Locale locale = new Locale("in", "ID");
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEEE",locale);
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
                                                        idJanji = object.getInt("id");
                                                        JSONArray transaksiArr = object.getJSONArray("transaksi");
                                                        for (int j = 0; j < transaksiArr.length(); j++) {
                                                            JSONObject transaksi = transaksiArr.getJSONObject(j);
                                                            if (transaksi.getInt("is_paid") == 0){
                                                                idTransaksi = transaksi.getInt("id");
                                                                Call<ResponseBody> bayar = RetrofitClient.getInstance().getApi().bayar("Bearer "+access,idTransaksi,idJanji);
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
                                                                                        Call<ResponseBody> getJanjiAgain = RetrofitClient.getInstance().getApi().getUserJanji("Bearer "+access);
                                                                                        getJanjiAgain.enqueue(new Callback<ResponseBody>() {
                                                                                            @Override
                                                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                                try {
                                                                                                    if (response.isSuccessful()){
                                                                                                        if (response.body() != null){
                                                                                                            String s = response.body().string();
                                                                                                            JSONObject janjiObject = new JSONObject(s);
                                                                                                            JSONArray janjiArr = janjiObject.getJSONArray("data");
                                                                                                            for (int i = 0; i < janjiArr.length(); i++) {
                                                                                                                JSONObject janjiObj = janjiArr.getJSONObject(i);
                                                                                                                if (janjiObj.getInt("idStatus") == 1){
                                                                                                                    docUri = Uri.parse(bundle.getString("uri"));
                                                                                                                    String mimeType = getActivity().getContentResolver().getType(docUri);

                                                                                                                    try {
                                                                                                                        String fileName = getFileName(docUri);

                                                                                                                        // The temp file could be whatever you want
                                                                                                                        File outputDir = getActivity().getCacheDir(); // context being the Activity pointer
                                                                                                                        File outputFile = File.createTempFile(fileName.split("\\.")[0], "."+fileName.split("\\.")[1], outputDir);

                                                                                                                        File fileCopy = copyToTempFile(docUri, outputFile);
                                                                                                                        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType),fileCopy);
                                                                                                                        MultipartBody.Part part = MultipartBody.Part.createFormData("file",fileName,requestBody);
                                                                                                                        Call<ResponseBody> uploadFile = RetrofitClient.getInstance().getApi().uploadFile("Bearer "+access,janjiObj.getInt("id"),part);
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
                                                                                                                                                SFL.stopShimmer();
                                                                                                                                                relativeContent.setVisibility(View.VISIBLE);
                                                                                                                                                linearLoader.setVisibility(View.GONE);
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
                                                                                                                        break;
                                                                                                                    } catch (Exception e) {
                                                                                                                        e.printStackTrace();
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                } catch (IOException | JSONException e) {
                                                                                                    e.printStackTrace();
                                                                                                    getJanjiAgain.clone().enqueue(this);
                                                                                                }
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            }else{
                                                                                Log.e("BUATJANJI",response.errorBody().string());
                                                                            }
                                                                        } catch (IOException | JSONException e) {
                                                                            e.printStackTrace();
                                                                            bayar.clone().enqueue(this);
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
                                            }
                                        } catch (IOException | JSONException e) {
                                            getJanji.clone().enqueue(this);
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

        dialogView.findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
//                socket.emit("App.User.Notification.Janji."+bundle.getInt("id_dokter"),bundle.getString("name"));
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
        tv_harga = getActivity().findViewById(R.id.tv_biaya_konsultasi);
        tv_total_harga = getActivity().findViewById(R.id.tv_total_pembayaran);
        tv_saldo = getActivity().findViewById(R.id.tv_saldo);
        tv_total_bayar = getActivity().findViewById(R.id.tv_total_pembayaran_2);
        tv_sisa_saldo = getActivity().findViewById(R.id.tv_sisa_saldo);
        tv_layanan = getActivity().findViewById(R.id.tv_biaya_layanan);
        tv_kode_unik = getActivity().findViewById(R.id.tv_unique_code);
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
