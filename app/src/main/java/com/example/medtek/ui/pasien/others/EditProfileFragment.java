package com.example.medtek.ui.pasien.others;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;

public class EditProfileFragment extends Fragment {

    private ChipNavigationBar bottomNavigationView;
    private Toolbar toolbar;
    private DatePickerDialog datePickerDialog;

    private CircleImageView circleImageView,civ_temp;
    private EditText et_nama, et_email, et_tgl, et_noHp;
    private TextView male, female,isverify;
    private Button btn_simpan;

    private static final int GALLERY = 22 ;
    private static final String APP_TAG = "MedTek";
    private static final int CAMERAA = 33;
    private final String photoFileName = "avatar.png";
    private Uri filePath;
    private File finalFile;
    private String postPath;
    private RequestBody requestBody;

    private String access, refresh, tglLahir, jenis_kelamin, noHp, name, email;
    private int counter=0;
    private String updateTgl, updateName, updateNoHp, updateJK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(btn_simpan.isEnabled()){
                    onBackDialog();
                }else{
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        };
        getActivity().getOnBackPressedDispatcher().addCallback(callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
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
                            name = user.getString("name");
                            email = user.getString("email");
                            et_nama.setText(user.getString("name"));
                            et_email.setText(user.getString("email"));
                            if (user.getString("email_verified_at") != ""){
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
                            }else{
                                tglLahir = "-";
                                et_tgl.setText("-");
                            }
                            if (user.getString("notelp") != "null"){
                                noHp = user.getString("notelp");
                                et_noHp.setText(user.getString("notelp"));
                            }else{
                                noHp = "-";
                                et_noHp.setText("-");
                            }
                            JSONArray jsonArray = new JSONArray(user.getString("image"));
                            String userImagePath;
                            if (jsonArray.getJSONObject(0).getString("path").equals("/storage/Pasien.png") || jsonArray.length() == 0){
                                userImagePath = "/storage/Pasien.png";
                            }else{
                                userImagePath = jsonArray.getJSONObject(0).getString("path");
                            }
                            Picasso.get().load(BASE_URL+userImagePath).into(circleImageView);
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
                if (!tglLahir.equalsIgnoreCase(s.toString())){
                    counter+=1;
                    btn_simpan.setEnabled(true);
                }else{
                    btn_simpan.setEnabled(false);
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
                if (!noHp.equalsIgnoreCase(s.toString())){
                    counter+=1;
                    btn_simpan.setEnabled(true);
                    updateNoHp = s.toString();
                }else{
                    btn_simpan.setEnabled(false);
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
                if (updateNoHp != null){
                    jsonParams.put("notelp",updateNoHp);
                    noHp = updateNoHp;
                }
                jsonParams.put("email",email);

                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());
                initializeDialogSuccess(body);

            }
        });

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
                    try {
                        String s = response.body().string();
                        JSONObject jsonObject = new JSONObject(s);
                        Log.e("RESPONSEBODY",s);
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
