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
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.dokter.JadwalModel;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.ui.dokter.home.MenungguKonfirmasiAdapter;
import com.example.medtek.utils.FileUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.tobiasschuerg.prefixsuffix.PrefixSuffixEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
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

public class JadwalSayaFragment extends Fragment {

    private ChipNavigationBar bottomNavigationView;
    private Toolbar toolbar;
    private int idDokter;
    private String access, refresh, time, mulai, selesai, hari;
    private ArrayList<Integer> idDays = new ArrayList<>();
    private ArrayList<String> startHours = new ArrayList<>();
    private ArrayList<String> endHours = new ArrayList<>();

    private RelativeLayout layout_empty;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private JadwalSayaAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<JadwalModel> mList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jadwal_saya, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        AndroidThreeTen.init(getActivity());
        initialize();

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
                            idDokter = user.getInt("id");
                            mList.clear();
                            if (user.getJSONArray("jadwal").length() > 0){
                                layout_empty.setVisibility(View.GONE);
                                JSONArray jadwal = user.getJSONArray("jadwal");
                                for (int i = 0; i < jadwal.length(); i++) {
                                    JSONObject jadwalObject = jadwal.getJSONObject(i);
                                    idDays.add(jadwalObject.getInt("day_id"));
                                    startHours.add(jadwalObject.getString("startHour"));
                                    endHours.add(jadwalObject.getString("endHour"));
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                    SimpleDateFormat sdfs = new SimpleDateFormat("HH:mm");
                                    String start = sdfs.format(sdf.parse(jadwalObject.getString("startHour")));
                                    String end = sdfs.format(sdf.parse(jadwalObject.getString("endHour")));
                                    ArrayList<String> arrayHari = new ArrayList<String>(Arrays.asList(getActivity().getResources().getStringArray(R.array.hari)));
                                    mList.add(new JadwalModel(jadwalObject.getInt("id"),jadwalObject.getInt("day_id"),jadwalObject.getInt("idDokter"),
                                            start,end,arrayHari.get(jadwalObject.getInt("day_id"))));
                                }
                                Collections.sort(mList, new Comparator<JadwalModel>() {
                                    @Override
                                    public int compare(JadwalModel o1, JadwalModel o2) {
                                        return Integer.valueOf(o1.getIdDay()).compareTo(Integer.valueOf(o2.getIdDay()));
                                    }
                                });
                                initRecyclerViewItem();
                                recyclerView.setVisibility(View.VISIBLE);
                                layout_empty.setVisibility(View.GONE);
                                if (jadwal.length() > 6){
                                    floatingActionButton.setVisibility(View.GONE);
                                }
                            }else{
                                recyclerView.setVisibility(View.GONE);
                                layout_empty.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } catch (IOException | JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDialogForm();
            }
        });

    }

    private void initRecyclerViewItem(){
        mAdapter = new JadwalSayaAdapter(mList,getActivity().getApplicationContext(),getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    private void initializeDialogForm(){
        Dialog dialog = new Dialog(getActivity(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_form_jadwal,null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        EditText et_mulai = dialogView.findViewById(R.id.et_mulai_dari);
        EditText et_selesai = dialogView.findViewById(R.id.et_mulai_selesai);
        Spinner sp_hari = dialogView.findViewById(R.id.sp_hari);
        Button btn_simpan = dialogView.findViewById(R.id.btn_simpan);

        SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Mohon tunggu ...");
        pDialog.setCancelable(false);

        ArrayList<String> arrayHari = new ArrayList<String>(Arrays.asList(getActivity().getResources().getStringArray(R.array.hari)));
        ArrayList<String> newHari = new ArrayList<>();
        if (idDays.size() > 0){
            for (int i = 0; i < arrayHari.size(); i++) {
                if (idDays.contains(i)){
                    continue;
                }else{
                    newHari.add(arrayHari.get(i));
                }
            }
        }else{
            newHari = arrayHari;
        }

        ArrayAdapter<String> adapterS = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,newHari){
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
        sp_hari.setAdapter(adapterS);
        
        et_mulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                        mulai = String.format("%02d",hourOfDay)+":"+String.format("%02d",minute);
                        et_mulai.setText(mulai);
                    }
                },0,0,true);
                timePickerDialog.setAccentColor(getActivity().getColor(R.color.colorPrimary));
                timePickerDialog.setCancelColor(getActivity().getColor(R.color.textColorGray));
                timePickerDialog.show(getActivity().getSupportFragmentManager(),"TimePickerDialog");
            }
        });

        et_selesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mulai == null){
                    Toasty.warning(getActivity(),"Silahkan pilih waktu mulai terlebih dahulu!",Toasty.LENGTH_LONG).show();
                }else{
                    TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                            selesai = String.format("%02d",hourOfDay)+":"+String.format("%02d",minute);
                            et_selesai.setText(selesai);
                        }
                    },0,0,true);
                    int hour = Integer.valueOf(mulai.split(":")[0]);
                    int minutes = Integer.valueOf(mulai.split(":")[1]);
                    timePickerDialog.setMinTime(hour+1,minutes,0);
                    timePickerDialog.setAccentColor(getActivity().getColor(R.color.colorPrimary));
                    timePickerDialog.setCancelColor(getActivity().getColor(R.color.textColorGray));
                    timePickerDialog.show(getActivity().getSupportFragmentManager(),"TimePickerDialog");
                }
            }
        });

        sp_hari.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                ((TextView) parent.getChildAt(0)).setPadding(0,0,0,0);
                if (position == 0){
                    ((TextView) parent.getChildAt(0)).setTextColor(et_mulai.getHintTextColors());
                }else{
                    ((TextView) parent.getChildAt(0)).setTextColor(getActivity().getColor(R.color.colorPrimary));
                    hari = parent.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hari == null){
                    Toasty.warning(getActivity(),"Silahkan pilih hari terlebih dahulu!",Toasty.LENGTH_LONG).show();
                }else{
                    if (mulai == null){
                        Toasty.warning(getActivity(),"Silahkan pilih waktu mulai terlebih dahulu!",Toasty.LENGTH_LONG).show();
                    }else{
                        if (selesai == null){
                            Toasty.warning(getActivity(),"Silahkan pilih waktu selesai terlebih dahulu!",Toasty.LENGTH_LONG).show();
                        }else{
                            Log.e(TAG, "detailJadwal:\nidDokter->"+idDokter+"\nDay->"+hari+"\nstartHour->"+mulai+"\nendHour->"+selesai);
                            Call<ResponseBody> addJadwal = RetrofitClient.getInstance().getApi().addJadwal("Bearer "+access,idDokter,hari.toLowerCase(),mulai,selesai);
                            addJadwal.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.body() != null){
                                        if (response.isSuccessful()){
                                            try {
                                                String s = response.body().string();
                                                JSONObject object = new JSONObject(s);
                                                dialog.cancel();
                                                Toasty.success(getActivity(),"Jadwal Konsultasi Berhasil Ditambahkan!",Toasty.LENGTH_LONG
                                                ).show();
                                                Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentByTag("JadwalSayaFragment");
                                                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                fragmentTransaction.detach(currentFragment);
                                                fragmentTransaction.attach(currentFragment);
                                                fragmentTransaction.commit();
                                                Log.e(TAG, "onResponse: "+s);
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
        });

        dialogView.findViewById(R.id.ib_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
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

        floatingActionButton = getActivity().findViewById(R.id.fab_add_jadwal);
        layout_empty = getActivity().findViewById(R.id.layout_empty_jadwal);
        recyclerView = getActivity().findViewById(R.id.rv_jadwal_dokter);
    }

    private void setToolbar() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chevron_left, null);
        drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN));
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
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        getActivity().getWindow().setStatusBarColor(getActivity().getColor(R.color.colorPrimary));
    }

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

}
