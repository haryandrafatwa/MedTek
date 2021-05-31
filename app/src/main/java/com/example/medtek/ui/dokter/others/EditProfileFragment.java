package com.example.medtek.ui.dokter.others;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.medtek.R;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.utils.FileUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.picasso.Picasso;
import com.tobiasschuerg.prefixsuffix.PrefixSuffixEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import me.abhinay.input.CurrencyEditText;
import me.abhinay.input.CurrencySymbols;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;

public class EditProfileFragment extends Fragment {

    private static final String SUFFIX = " Tahun";
    private ChipNavigationBar bottomNavigationView;
    private Toolbar toolbar;

    private DatePickerDialog datePickerDialog;
    private CircleImageView circleImageView,civ_temp;
    private PrefixSuffixEditText et_pengalaman;
    private CurrencyEditText et_harga;
    private EditText et_nama, et_email, et_tgl, et_noHp, et_noRekening, et_pendidikan;
    private TextView male, female,isverify;
    private Spinner sp_spesialisasi, sp_rs;
    private Button btn_simpan;

    private static final int GALLERY = 22 ;
    private static final String APP_TAG = "MedTek";
    private static final int CAMERAA = 33;
    private final String photoFileName = "avatar.png";
    private Uri filePath;
    private File finalFile;
    private String postPath;
    private RequestBody requestBody;
    private ArrayList rsArray[] = new ArrayList[2];
    private ArrayList specializationArray[] = new ArrayList[2];
    private boolean isLoadedRs = false, isLoadedSpecialist = false;

    private String access, refresh, tglLahir, jenis_kelamin, noHp, name, email, noRekening, pengalaman, pendidikan;
    private int counter=0, idHospital, updateHospital, harga, updateHarga, idSpecialization, updateSpecialization;
    private String updateTgl, updateName, updateNoHp, updateJK, updateNoRekening, updatePengalaman, updatePendidikan;
    private boolean addedSuffix = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile_dokter, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void onBackDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the message show for the Alert time
        builder.setMessage("Apa kamu yakin ingin kembali?");
        builder.setTitle("Perhatian!");
        builder.setCancelable(false);

        builder.setPositiveButton("Iya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActivity().getColor(R.color.colorPrimary));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getActivity().getColor(R.color.textColorGray));
            }
        });
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.changepassword:
                Toast.makeText(getActivity(), "Change Password", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        setHasOptionsMenu(true);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(counter > 0){
                    onBackDialog();
                }else{
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        };
        getActivity().getOnBackPressedDispatcher().addCallback(callback);

        AndroidThreeTen.init(getActivity());
        initialize();

        Call<ResponseBody> getHospital = RetrofitClient.getInstance().getApi().getAllHospital();
        getHospital.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() != null){
                    if (response.isSuccessful()){
                        try {
                            String s = response.body().string();
                            JSONObject obj = new JSONObject(s);
                            JSONArray array = obj.getJSONArray("data");
                            if (array.length() != 0){
                                ArrayList<String> rsList = new ArrayList<>();
                                ArrayList<Integer> idRs = new ArrayList<>();
                                rsList.add("Pilih Rumah Sakit");
                                idRs.add(-1);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject hospital = array.getJSONObject(i);
                                    rsList.add(hospital.getString("name"));
                                    idRs.add(hospital.getInt("id"));
                                }
                                rsArray[0] = rsList;
                                rsArray[1] = idRs;
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,rsArray[0]){
                                    @Override
                                    public boolean isEnabled(int position) {
                                        if (position == 0) {
                                            return false;
                                        } else {
                                            return true;
                                        }
                                    }

                                    @Override
                                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getDropDownView(position, convertView, parent);
                                        TextView textview = (TextView) view;
                                        if (position == 0) {
                                            textview.setTextColor(getActivity().getColor(R.color.textColorGray));
                                        } else {
                                            textview.setTextColor(Color.BLACK);
                                        }
                                        return view;
                                    }
                                };
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                sp_rs.setAdapter(adapter);
                                isLoadedRs = true;
                                checkAllData();
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

        Call<ResponseBody> getSpecialization = RetrofitClient.getInstance().getApi().getSpecializationList("Bearer "+access);
        getSpecialization.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() != null){
                    if (response.isSuccessful()){
                        try {
                            String s = response.body().string();
                            JSONObject obj = new JSONObject(s);
                            JSONArray array = obj.getJSONArray("data");
                            if (array.length()!=0){
                                ArrayList<String> specializationList = new ArrayList<>();
                                ArrayList<Integer> idSpecialization = new ArrayList<>();
                                specializationList.add("Pilih Spesialisasi");
                                idSpecialization.add(-1);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject specialization = array.getJSONObject(i);
                                    specializationList.add(specialization.getString("specialization"));
                                    idSpecialization.add(specialization.getInt("id"));
                                }
                                specializationArray[0] = specializationList;
                                specializationArray[1] = idSpecialization;
                                ArrayAdapter<String> adapterS = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,specializationArray[0]){
                                    @Override
                                    public boolean isEnabled(int position) {
                                        if (position == 0) {
                                            return false;
                                        } else {
                                            return true;
                                        }
                                    }

                                    @Override
                                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getDropDownView(position, convertView, parent);
                                        TextView textview = (TextView) view;
                                        if (position == 0) {
                                            textview.setTextColor(getActivity().getColor(R.color.textColorGray));
                                        } else {
                                            textview.setTextColor(Color.BLACK);
                                        }
                                        return view;
                                    }
                                };
                                adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                sp_spesialisasi.setAdapter(adapterS);
                                isLoadedSpecialist = true;
                                checkAllData();
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

        sp_rs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                ((TextView) parent.getChildAt(0)).setPadding(0,8,0,8);
                if (position == 0){
                    ((TextView) parent.getChildAt(0)).setTextColor(et_nama.getHintTextColors());
                }else{
                    ((TextView) parent.getChildAt(0)).setTextColor(getActivity().getColor(R.color.colorPrimary));
                    if ((position-1) != idHospital){
                        updateHospital = Integer.valueOf(rsArray[1].get(position).toString());
                        counter += 1;
//                        btn_simpan.setEnabled(true);
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                    Log.e(TAG, "onRSSelected: "+position+" >< "+idHospital);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp_spesialisasi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                ((TextView) parent.getChildAt(0)).setPadding(0,8,0,8);
                if (position == 0){
                    ((TextView) parent.getChildAt(0)).setTextColor(et_nama.getHintTextColors());
                }else{
                    ((TextView) parent.getChildAt(0)).setTextColor(getActivity().getColor(R.color.colorPrimary));
                    if ((position-1) != idSpecialization){
                        updateSpecialization = Integer.valueOf(specializationArray[1].get(position).toString());
                        counter += 1;
                        btn_simpan.setEnabled(true);
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                    Log.e(TAG, "ID: "+Integer.valueOf(specializationArray[1].get(position).toString())+" -> "+specializationArray[0].get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        et_nama.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!name.equalsIgnoreCase(s.toString())){
                    counter+=1;
                    btn_simpan.setEnabled(true);
                    updateName = s.toString();
                }else{
                    btn_simpan.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        et_tgl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tglLahir != null) {
                    if (!tglLahir.equalsIgnoreCase(s.toString())){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updateTgl = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }else{
                    if (s.toString().length() > 0){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updateTgl = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        et_noHp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (noHp != null){
                    if (!noHp.equalsIgnoreCase(s.toString())){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updateNoHp = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }else{
                    if (s.toString().length() > 0){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updateNoHp = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        et_noRekening.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (noRekening != null){
                    if (!noRekening.equalsIgnoreCase(s.toString())){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updateNoRekening = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }else{
                    if (s.toString().length() > 0){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updateNoRekening = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        et_harga.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (harga > 0){
                    if (harga != et_harga.getCleanIntValue()) {
                        counter += 1;
                        btn_simpan.setEnabled(true);
                        updateHarga = et_harga.getCleanIntValue();
                    } else {
                        btn_simpan.setEnabled(false);
                    }
                }else{
                    if (s.toString().length() > 0){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updateHarga = et_harga.getCleanIntValue();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }

                if (et_harga.getText().length() == 2){
                    et_harga.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_pendidikan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pendidikan != null){
                    if (!pendidikan.equalsIgnoreCase(s.toString())){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updatePendidikan = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }else{
                    if (s.toString().length() > 0){
                        btn_simpan.setEnabled(true);
                        counter+=1;
                        updatePendidikan = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        et_pengalaman.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pengalaman != null){
                    if (!pengalaman.equalsIgnoreCase(s.toString())){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updatePengalaman = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }else{
                    if (s.toString().length() > 0){
                        counter+=1;
                        btn_simpan.setEnabled(true);
                        updatePengalaman = s.toString();
                    }else{
                        btn_simpan.setEnabled(false);
                    }
                }

                if (s.toString().length() == 0){
                    et_pengalaman.setSuffix("");
                    et_pengalaman.setHintTextColor(et_pendidikan.getHintTextColors());
                }else{
                    et_pengalaman.setSuffix(SUFFIX);
                    et_pengalaman.setHintTextColor(getActivity().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateJK = "pria";
                male.setTextColor(Color.WHITE);
                male.setBackground(getActivity().getDrawable(R.drawable.bg_male));
                female.setTextColor(getActivity().getColor(R.color.colorPrimary));
                female.setBackground(null);
                btn_simpan.setEnabled(!updateJK.equalsIgnoreCase(jenis_kelamin));
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateJK = "wanita";
                female.setTextColor(Color.WHITE);
                female.setBackground(getActivity().getDrawable(R.drawable.bg_female));
                male.setTextColor(getActivity().getColor(R.color.colorPrimary));
                male.setBackground(null);
                btn_simpan.setEnabled(!updateJK.equalsIgnoreCase(jenis_kelamin));
            }
        });

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> jsonParams = new ArrayMap<>();
                if (name == null && updateName ==null){
                    Toasty.warning(getActivity(),"Nama tidak boleh kosong!",Toasty.LENGTH_LONG).show();
                }else{
                    if (noRekening == null && updateNoRekening == null){
                        Toasty.warning(getActivity(),"Nomor rekening tidak boleh kosong!",Toasty.LENGTH_LONG).show();
                    }else{
                        if (harga == 0 && updateHarga == 0){
                        Toasty.warning(getActivity(),"Biaya konsultasi tidak boleh kosong!",Toasty.LENGTH_LONG).show();
                    }else{
                        if (idSpecialization == 0 && updateSpecialization == 0){
                            Toasty.warning(getActivity(),"Spesialisasi tidak boleh kosong!",Toasty.LENGTH_LONG).show();
                        }else{
                            if (idHospital == 0 && updateHospital ==0){
                                Toasty.warning(getActivity(),"Rumah sakit tidak boleh kosong!",Toasty.LENGTH_LONG).show();
                            }else{
                                if (updateName != null){
                                    jsonParams.put("name",updateName);
                                    name = updateName;
                                }else{
                                    jsonParams.put("name",name);
                                }
                                if (updateTgl != null){
                                    jsonParams.put("tglLahir",updateTgl);
                                    LocalDate localDate = LocalDate.parse(updateTgl);
                                    Locale locale = new Locale("in", "ID");
                                    DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy",locale).withLocale(locale);
                                    tglLahir = localDate.format(dayFormat);
                                }
                                if (updateJK != null){
                                    jsonParams.put("jenis_kelamin",updateJK);
                                    jenis_kelamin = updateJK;
                                }
                                if (updateNoHp != null) {
                                    jsonParams.put("notelp", updateNoHp);
                                    noHp = updateNoHp;
                                }
                                if (updateNoRekening != null){
                                    jsonParams.put("nomor_rekening",updateNoRekening);
                                    noRekening = updateNoRekening;
                                }else{
                                    jsonParams.put("nomor_rekening",noRekening);
                                }
                                if (updateHarga > 0){
                                    jsonParams.put("harga",updateHarga);
                                    harga = updateHarga;
                                }else{
                                    jsonParams.put("harga",harga);
                                }
                                if (updateSpecialization > 0){
                                    jsonParams.put("specialization",specializationArray[0].get(updateSpecialization));
                                    idSpecialization = updateSpecialization;
                                }else{
                                    jsonParams.put("specialization",specializationArray[0].get(idSpecialization));
                                }
                                if (updateHospital > 0){
                                    jsonParams.put("hospital_id",updateHospital);
                                    idHospital = updateHospital;
                                }else{
                                    jsonParams.put("hospital_id",idHospital);
                                }
                                if (updatePendidikan != null){
                                    jsonParams.put("lulusan",updatePendidikan);
                                    pendidikan = updatePendidikan;
                                }
                                if (updatePengalaman != null){
                                    jsonParams.put("lama_kerja",updatePengalaman.replace(SUFFIX,""));
                                    pengalaman = updatePengalaman;
                                }
                                jsonParams.put("email",email);

                                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());
                                initializeDialogSuccess(body);
                            }
                        }
                    }
                    }
                }

            }
        });

    }

    private void checkAllData(){
        if (isLoadedRs && isLoadedSpecialist){
            Call<ResponseBody> userProfile = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
            userProfile.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try{
                        if (response.isSuccessful()){
                            if (response.body() != null){
                                String s = response.body().string();
                                JSONObject user = new JSONObject(s);
                                Log.e("TAG", "onResponse: "+s );
                                name = user.getString("name");
                                email = user.getString("email");
                                et_nama.setText(user.getString("name"));
                                et_email.setText(user.getString("email"));
                                if (!user.isNull("email_verified_at")){
                                    isverify.setText(R.string.terverifikasi);
                                    isverify.setBackground(getResources().getDrawable(R.drawable.bg_button_red));
                                    isverify.setBackgroundTintList(getActivity().getColorStateList(R.color.textColorLightRed));
                                    isverify.setTextColor(getResources().getColor(R.color.textColorDarkRed));
                                }else{
                                    isverify.setText(R.string.belumterverifikasi);
                                    isverify.setBackground(getResources().getDrawable(R.drawable.bg_button_red));
                                    isverify.setBackgroundTintList(getActivity().getColorStateList(R.color.textColorLightGray));
                                    isverify.setTextColor(getResources().getColor(R.color.textColorGray));
                                }
                                if (user.getString("tglLahir")!= "null"){
                                    LocalDate localDate = LocalDate.parse(user.getString("tglLahir"));
                                    Locale locale = new Locale("in", "ID");
                                    DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy",locale).withLocale(locale);
                                    tglLahir = localDate.format(dayFormat);
                                    et_tgl.setText(localDate.format(dayFormat));
                                }
                                if (user.getString("notelp") != "null"){
                                    noHp = user.getString("notelp");
                                    et_noHp.setText(user.getString("notelp"));
                                }
                                if (user.getString("nomor_rekening") != "null"){
                                    noRekening = user.getString("nomor_rekening");
                                    et_noRekening.setText(user.getString("nomor_rekening"));
                                }
                                if (!user.isNull("hospital_id")){
                                    idHospital = user.getInt("hospital_id");
                                    sp_rs.setSelection(rsArray[1].indexOf(idHospital));
                                }
                                if (!user.isNull("specialization_id")){
                                    idSpecialization = user.getInt("specialization_id");
                                    sp_spesialisasi.setSelection(specializationArray[1].indexOf(idSpecialization));
                                }
                                if (!user.isNull("harga")){
                                    harga = user.getInt("harga");
                                    et_harga.setText(String.valueOf(harga));
                                    et_harga.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            et_harga.setSelection(et_harga.getText().length());
                                        }
                                    });
                                }
                                if(!user.isNull("lama_kerja")){
                                    pengalaman = user.getString("lama_kerja");
                                    et_pengalaman.setText(String.valueOf(pengalaman));
                                }
                                if (!user.isNull("lulusan")){
                                    pendidikan = user.getString("lulusan");
                                    et_pendidikan.setText(pendidikan);
                                }
                                String path="";
                                JSONArray jsonArrayImage = user.getJSONArray("image");
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
                                Picasso.get().load(path).into(circleImageView);
                                if (user.getString("jenis_kelamin").equalsIgnoreCase("Pria")){
                                    male.setTextColor(Color.WHITE);
                                    male.setBackground(getResources().getDrawable(R.drawable.bg_male));
                                    jenis_kelamin = "pria";
                                }else if(user.getString("jenis_kelamin").equalsIgnoreCase("wanita")){
                                    female.setTextColor(Color.WHITE);
                                    female.setBackground(getResources().getDrawable(R.drawable.bg_female));
                                    jenis_kelamin = "wanita";
                                }else{
                                    jenis_kelamin = "-";
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
    }

    private void initializeDialogSuccess(RequestBody body){
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

        TextView placeholderDetail = dialogView.findViewById(R.id.placeholderDetail);
        placeholderDetail.setVisibility(View.GONE);
        TextView tvharaptunggu = dialogView.findViewById(R.id.tv_harap_tunggu);
        tvharaptunggu.setVisibility(View.GONE);

        TextView tv_pembayaran_berhasil = dialogView.findViewById(R.id.tv_pembayaran_berhasil);
        tv_pembayaran_berhasil.setText("Perubahan telah disimpan!");

        Button button = dialogView.findViewById(R.id.btnNext);
        button.setText("Oke");

        Call<ResponseBody> callUpdate = RetrofitClient.getInstance().getApi().updateProfile("Bearer "+access,body);
        callUpdate.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    if (response.body() != null){
                        try {
                            String s = response.body().string();
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.has("success")){
                                linearLoader.setVisibility(View.GONE);
                                SFL.stopShimmer();
                                relativeContent.setVisibility(View.VISIBLE);
                            }
                        }catch (IOException | JSONException e){
                            Log.e("RESPONSEBODY",e.getMessage());
                            callUpdate.clone().enqueue(this);
                        }
                    }else{
                        try {
                            Log.e("RESPONSEBODY",response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    try {
                        Log.e("RESPONSEBODY",response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("RESPONSEBODY",t.getMessage());
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                btn_simpan.setEnabled(false);
            }
        });

        dialog.show();
    }

    private void initialize(){
        loadData(getActivity());
        bottomNavigationView = getActivity().findViewById(R.id.bottomBar);
        bottomNavigationView.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();
        setStatusBar();

        circleImageView = getActivity().findViewById(R.id.civ_user);
        civ_temp = getActivity().findViewById(R.id.civ_temp);
        et_nama = getActivity().findViewById(R.id.et_nama);
        et_email = getActivity().findViewById(R.id.et_email);
        et_noHp = getActivity().findViewById(R.id.et_no_hp);
        et_tgl = getActivity().findViewById(R.id.et_tgl);
        et_noRekening = getActivity().findViewById(R.id.et_no_rekening);

        et_harga = getActivity().findViewById(R.id.et_harga_konsultasi);
        et_harga.setCurrency(CurrencySymbols.INDONESIA);
        et_harga.setDelimiter(false);
        et_harga.setSpacing(false);
        et_harga.setDecimals(false);
        et_harga.setSeparator(".");

        et_pendidikan = getActivity().findViewById(R.id.et_pendidikan_terakhir);
        et_pengalaman = getActivity().findViewById(R.id.et_pengalaman_kerja);

        sp_spesialisasi = getActivity().findViewById(R.id.sp_spesialisasi);
        sp_rs = getActivity().findViewById(R.id.sp_rs);

        male = getActivity().findViewById(R.id.tv_male);
        female = getActivity().findViewById(R.id.tv_female);
        btn_simpan = getActivity().findViewById(R.id.btn_simpan);
        isverify = getActivity().findViewById(R.id.is_verify);

        et_tgl.setFocusable(false);
        et_tgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.bottom_sheet_take_image,(RelativeLayout) getActivity().findViewById(R.id.bottomSheetContainer));

                bottomSheetView.findViewById(R.id.layout_camera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                        bottomSheetDialog.cancel();
                    }
                });
                bottomSheetView.findViewById(R.id.layout_gallery).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosePhotoFromGallery();
                        bottomSheetDialog.cancel();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
        civ_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleImageView.performClick();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            finalFile = getPhotoFileUri(photoFileName);
            filePath = FileProvider.getUriForFile(getActivity(), "com.example.medtek.fileprovider", finalFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, CAMERAA);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY && resultCode == RESULT_OK) {

            try {
                filePath = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                civ_temp.setImageBitmap(bitmap);
                civ_temp.setVisibility(View.VISIBLE);
                circleImageView.setVisibility(View.GONE);
                postPath = new File(FileUtil.getPath(filePath,getActivity())).getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),new File(FileUtil.getPath(filePath,getActivity())));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERAA && resultCode == RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                civ_temp.setImageBitmap(bitmap);
                civ_temp.setVisibility(View.VISIBLE);
                circleImageView.setVisibility(View.GONE);
                postPath = filePath.getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),getPhotoFileUri(new File(filePath.getPath()).getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (postPath!=null){
            File file = new File(postPath);
            MultipartBody.Part part = MultipartBody.Part.createFormData("profilePic",file.getName(),requestBody);

            Call<ResponseBody> uploadAvatar = RetrofitClient.getInstance().getApi().uploadAvatar("Bearer "+access,part);
            uploadAvatar.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.body() != null){
                        if (response.isSuccessful()){
                            try {
                                String s = response.body().string();
                                JSONObject jsonObject = new JSONObject(s);
                                Log.e("RESPONSEBODY",s);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                Log.e("RESPONSEBODY",response.errorBody().string());
                            } catch (IOException e) {
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

    //method utk melakukan konversi dari abstract path menjadi absolute path
    public File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    //method utk menampilkan galeri foto user
    private void choosePhotoFromGallery() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY);
            }
        }
    }

    private void showDialog(){
        Calendar calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                Calendar datePicker = Calendar.getInstance();
                datePicker.set(Calendar.YEAR,year);
                datePicker.set(Calendar.MONTH,monthOfYear);
                datePicker.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                String[] strDays = new String[] { "", "Senin", "Selasa","Rabu", "Kamis","Jumat", "Sabtu", "Minggu" };
                Locale locale = new Locale("in", "ID");
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM YYYY",locale);
                DateTimeFormatter dbFormat = DateTimeFormatter.ofPattern("YYYY-MM-dd",locale);
                LocalDate localDate = LocalDate.of(year,monthOfYear+1,dayOfMonth);
                et_tgl.setText(localDate.format(dateFormat));
                updateTgl = localDate.format(dbFormat);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setAccentColor(getActivity().getColor(R.color.colorPrimary));
        datePickerDialog.setCancelColor(getActivity().getColor(R.color.textColorGray));
        datePickerDialog.show(getActivity().getSupportFragmentManager(),"DatePickerDialog");
    }

    private void setToolbar() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chevron_left, null);
        drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.textColorDark), PorterDuff.Mode.SRC_IN));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(drawable);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setStatusBar(){
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getActivity().getWindow().setStatusBarColor(Color.WHITE);
    }

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

}
