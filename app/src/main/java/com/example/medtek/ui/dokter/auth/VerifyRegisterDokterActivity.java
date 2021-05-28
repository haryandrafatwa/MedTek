package com.example.medtek.ui.dokter.auth;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.medtek.R;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.ui.activity.WelcomePageActivity;
import com.example.medtek.utils.FileUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyRegisterDokterActivity extends AppCompatActivity {

    private String nama_lengkap,email,password;
    private RelativeLayout upload_image_sip, upload_image_ktp;
    private Button daftar;
    private RoundedImageView riv_sip, riv_ktp;
    private Toolbar toolbar;
    private SweetAlertDialog pDialog;

    private static final int GALLERY_SIP = 20 ;
    private static final int GALLERY_KTP = 21 ;
    private static final int CAMERA_SIP = 30;
    private static final int CAMERA_KTP = 31;
    private static final String APP_TAG = "MedTek";
    private final String photoFileName = DateFormat.format("MM-dd-yy hh-mm-ss", new Date()).toString();
    private Uri filePath,ktpPath;
    private File finalFile, ktpFile;
    private String postPath, ktpPostPath;

    private RequestBody requestBodySIP, requestBodyKTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_dokter_verify);

        Bundle bundle = this.getIntent().getBundleExtra("form_data");
        nama_lengkap = bundle.getString("nama_lengkap");
        email = bundle.getString("email");
        password = bundle.getString("password");

        initialize();

        upload_image_sip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(VerifyRegisterDokterActivity.this,R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_take_image,(RelativeLayout)findViewById(R.id.bottomSheetContainer));

                bottomSheetView.findViewById(R.id.layout_camera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent("SIP");
                        bottomSheetDialog.cancel();
                    }
                });
                bottomSheetView.findViewById(R.id.layout_gallery).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosePhotoFromGallery("SIP");
                        bottomSheetDialog.cancel();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
        riv_sip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload_image_sip.performClick();
            }
        });

        upload_image_ktp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(VerifyRegisterDokterActivity.this,R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_take_image,(RelativeLayout)findViewById(R.id.bottomSheetContainer));

                bottomSheetView.findViewById(R.id.layout_camera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent("KTP");
                        bottomSheetDialog.cancel();
                    }
                });
                bottomSheetView.findViewById(R.id.layout_gallery).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosePhotoFromGallery("KTP");
                        bottomSheetDialog.cancel();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
        riv_ktp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload_image_ktp.performClick();
            }
        });

        daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                if (postPath==null || ktpPostPath == null){
                    pDialog.dismiss();
                    Toasty.error(VerifyRegisterDokterActivity.this,getResources().getString(R.string.silahkanunggahfoto)).show();
                }else{

                    File file = new File(postPath);
                    File ktpFile = new File (ktpPostPath);

                    MultipartBody.Part part = MultipartBody.Part.createFormData("SIP",file.getName(),requestBodySIP);
                    MultipartBody.Part ktpPart = MultipartBody.Part.createFormData("ktp",ktpFile.getName(),requestBodyKTP);

                    Call<JSONObject> call = RetrofitClient.getInstance().getApi().register_dokter(nama_lengkap,email,password,password,part,ktpPart);
                    call.enqueue(new Callback<JSONObject>() {
                        @Override
                        public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                            if (response.isSuccessful()){
                                pDialog.dismiss();
                                initializeDialogSuccess();
                            }else{
                                Log.d("errorRegist", response.message());
                                try {
                                    String s = response.errorBody().string();
                                    Log.d("errorRegist", s);
                                    JSONObject obj = new JSONObject(s);
                                    JSONObject jsonObject = obj.getJSONObject("errors");
                                    if (jsonObject.getJSONArray("email").getString(0).equals("The email has already been taken.")){
                                        Toasty.error(VerifyRegisterDokterActivity.this,getResources().getString(R.string.emailsudahdigunakan)).show();
                                        onBackPressed();
                                    }
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JSONObject> call, Throwable t) {
                            Toast.makeText(VerifyRegisterDokterActivity.this, "Error: "+t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void initialize(){
        upload_image_sip = findViewById(R.id.upload_image_sip);
        upload_image_ktp = findViewById(R.id.upload_image_ktp);
        daftar = findViewById(R.id.btn_daftar_verify);
        riv_sip = findViewById(R.id.riv_upload_image_sip);
        riv_ktp = findViewById(R.id.riv_upload_image_ktp);
        toolbar = findViewById(R.id.toolbar);
        setToolbar();
        setSweetAlert();
    }

    private void setSweetAlert(){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Mohon tunggu ...");
        pDialog.setCancelable(false);
    }

    private void initializeDialogSuccess(){
        Dialog dialog = new Dialog(this,R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_daftar_dokter_berhasil,null);
        dialogView.setBackground(getDrawable(R.drawable.dialog_background));
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        dialogView.findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(VerifyRegisterDokterActivity.this, WelcomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    private void dispatchTakePictureIntent(String event) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            if(event.equalsIgnoreCase("SIP")){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                finalFile = getPhotoFileUri(photoFileName);
                filePath = FileProvider.getUriForFile(this, "com.example.medtek.fileprovider", finalFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                if (intent.resolveActivity(this.getPackageManager()) != null) {
                    startActivityForResult(intent, CAMERA_SIP);
                }
            }else{

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ktpFile = getPhotoFileUri(photoFileName);
                ktpPath = FileProvider.getUriForFile(this, "com.example.medtek.fileprovider", ktpFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, ktpPath);
                if (intent.resolveActivity(this.getPackageManager()) != null) {
                    startActivityForResult(intent, CAMERA_KTP);
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_SIP && resultCode == RESULT_OK) {

            try {
                filePath = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                riv_sip.setImageBitmap(bitmap);
                riv_sip.setVisibility(View.VISIBLE);
                upload_image_sip.setVisibility(View.GONE);
                postPath = new File(FileUtil.getPath(filePath,VerifyRegisterDokterActivity.this)).getPath();
                requestBodySIP = RequestBody.create(MediaType.parse("image/*"),new File(FileUtil.getPath(filePath,VerifyRegisterDokterActivity.this)));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }else if (requestCode == CAMERA_SIP && resultCode == RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                riv_sip.setImageBitmap(bitmap);
                riv_sip.setVisibility(View.VISIBLE);
                upload_image_sip  .setVisibility(View.GONE);
                postPath = filePath.getPath();
                requestBodySIP = RequestBody.create(MediaType.parse("image/*"),getPhotoFileUri(new File(filePath.getPath()).getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (requestCode == GALLERY_KTP && resultCode == RESULT_OK) {

            try {
                ktpPath = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ktpPath);
                riv_ktp.setImageBitmap(bitmap);
                riv_ktp.setVisibility(View.VISIBLE);
                upload_image_ktp.setVisibility(View.GONE);
                ktpPostPath = new File(FileUtil.getPath(ktpPath,VerifyRegisterDokterActivity.this)).getPath();
                requestBodyKTP = RequestBody.create(MediaType.parse("image/*"),new File(FileUtil.getPath(ktpPath,VerifyRegisterDokterActivity.this)));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }else if (requestCode == CAMERA_KTP && resultCode == RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ktpPath);
                riv_ktp.setImageBitmap(bitmap);
                riv_ktp.setVisibility(View.VISIBLE);
                upload_image_ktp  .setVisibility(View.GONE);
                ktpPostPath = ktpPath.getPath();
                requestBodyKTP = RequestBody.create(MediaType.parse("image/*"),getPhotoFileUri(new File(ktpPath.getPath()).getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //method utk melakukan konversi dari abstract path menjadi absolute path
    public File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    //method utk menampilkan galeri foto user
    private void choosePhotoFromGallery(String event) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (event.equalsIgnoreCase("SIP")){
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_SIP);
                }else{
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_KTP);
                }
            }
        }
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
