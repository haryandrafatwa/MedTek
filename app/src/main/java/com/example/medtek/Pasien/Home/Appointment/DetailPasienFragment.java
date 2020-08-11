package com.example.medtek.Pasien.Home.Appointment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.R;
import com.example.medtek.Utils.FileUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttp;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class DetailPasienFragment extends Fragment {

    private String access, refresh;
    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private DatePickerDialog datePickerDialog;

    private CircleImageView circleImageView;
    private TextView tv_edit_jadwal,tv_dr_name, tv_dr_specialist, tv_dr_rs, tv_dr_rs_loc;
    private EditText et_fullname, et_dob;
    private Button btnNext;
    private ImageButton ib_edit_jadwal;

    private RelativeLayout rl_id_card;
    private RoundedImageView riv_id_card;
    private static final int GALLERY = 22 ;
    private static final String APP_TAG = "MedTek";
    private static final int CAMERAA = 33;
    private String photoFileName = "ktp.png";
    private Uri filePath;
    private File finalFile;
    private String postPath;
    private RequestBody requestBody;

    private String nama,email,tglLahir,nama_dokter,tgl_konsul,jam_konsul;
    private int harga,balance;
    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_pasien, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        AndroidThreeTen.init(getActivity());
        initialize();

        bundle = getArguments();
        int id_dokter = bundle.getInt("id_dokter");
        jam_konsul = bundle.getString("time");
        tgl_konsul = bundle.getString("date");

        Call<ResponseBody> callKTP = RetrofitClient.getInstance().getApi().getKTP("Bearer "+access);
        callKTP.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // display the image data in a ImageView or save it
                        try {
                            byte[] bytes = response.body().bytes();
//                            Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            if (bmp != null){
                                riv_id_card.setImageBitmap(bmp);
                                riv_id_card.setVisibility(View.VISIBLE);
                                rl_id_card.setVisibility(View.GONE);
                                btnNext.setEnabled(true);
                            }else{
                                Log.e("RESPONSEBODY","NOKTP!");
                                btnNext.setEnabled(false);
                            }

                            Call<ResponseBody> callDokter = RetrofitClient.getInstance().getApi().getDokterId(id_dokter);
                            callDokter.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        String s = response.body().string();
                                        JSONObject object = new JSONObject(s);
                                        JSONObject obj = new JSONObject(object.getString("data"));
                                        tv_dr_name.setText(obj.getString("name"));
                                        nama_dokter = obj.getString("name");
                                        harga = obj.getInt("harga");
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

                            Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
                            callUser.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        String s = response.body().string();
                                        JSONObject object = new JSONObject(s);
                                        et_fullname.setText(object.getString("name"));
                                        nama = object.getString("name");
                                        email = object.getString("email");
                                        JSONObject walletObj = object.getJSONObject("wallet");
                                        balance = walletObj.getInt("balance");
                                        if (!object.getString("tglLahir").equalsIgnoreCase("null")){
                                            Locale locale = new Locale("in", "ID");
                                            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM YYYY",locale);
                                            LocalDate localDate = LocalDate.parse(object.getString("tglLahir"));
                                            et_dob.setText(localDate.format(dateFormat));
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

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("RESPONSEBODY","ISNULL!");
                        callKTP.clone().enqueue(this);
                    }
                }else{
                    try {
                        Log.e("RESPONSEBODY","ERROR: "+response.errorBody().string());
                        callKTP.clone().enqueue(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("RESPONSEBODY","ERROR: "+t.getMessage());
                callKTP.clone().enqueue(this);
            }
        });

        rl_id_card.setOnClickListener(new View.OnClickListener() {
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
        riv_id_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_id_card.performClick();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> jsonParams = new ArrayMap<>();
                jsonParams.put("name",nama);
                jsonParams.put("email",email);
                jsonParams.put("tglLahir",tglLahir);

                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());

                Call<ResponseBody> callUpdate = RetrofitClient.getInstance().getApi().updateProfileTglLahir("Bearer "+access,body);
                callUpdate.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){
                            if (response.body() != null){
                                try {
                                    String s = response.body().string();
                                    JSONObject jsonObject = new JSONObject(s);
                                    if (jsonObject.has("success")){
                                        bundle.putString("nama_dokter",nama_dokter);
                                        bundle.putInt("harga",harga);
                                        bundle.putInt("balance",balance);
                                        CheckoutAppointmentFragment checkoutAppointmentFragment = new CheckoutAppointmentFragment();
                                        checkoutAppointmentFragment.setArguments(bundle);
                                        setFragment(checkoutAppointmentFragment,"FragmentCheckoutAppointment");
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
                riv_id_card.setImageBitmap(bitmap);
                riv_id_card.setVisibility(View.VISIBLE);
                rl_id_card.setVisibility(View.GONE);
                postPath = new File(FileUtil.getPath(filePath,getActivity())).getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),new File(FileUtil.getPath(filePath,getActivity())));
                if (filePath!=null){
                    btnNext.setEnabled(true);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERAA && resultCode == RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                riv_id_card.setImageBitmap(bitmap);
                riv_id_card.setVisibility(View.VISIBLE);
                rl_id_card.setVisibility(View.GONE);
                postPath = filePath.getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),getPhotoFileUri(new File(filePath.getPath()).getName()));
                if (filePath!=null){
                    btnNext.setEnabled(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File file = new File(postPath);
        MultipartBody.Part part = MultipartBody.Part.createFormData("ktp",file.getName(),requestBody);

        Call<ResponseBody> callKTP = RetrofitClient.getInstance().getApi().uploadKTP("Bearer "+access,part);
        callKTP.enqueue(new Callback<ResponseBody>() {
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
                et_dob.setText(localDate.format(dateFormat));
                tglLahir = localDate.format(dbFormat);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setAccentColor(getActivity().getColor(R.color.colorPrimary));
        datePickerDialog.setCancelColor(getActivity().getColor(R.color.textColorGray));
        datePickerDialog.show(getActivity().getSupportFragmentManager(),"DatePickerDialog");
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
        et_fullname = getActivity().findViewById(R.id.et_fullname);
        et_dob = getActivity().findViewById(R.id.et_dob);
        btnNext = getActivity().findViewById(R.id.btnNext);

        rl_id_card = getActivity().findViewById(R.id.upload_image);
        riv_id_card = getActivity().findViewById(R.id.riv_upload_image);

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
        et_dob.setFocusable(false);
        et_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

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
