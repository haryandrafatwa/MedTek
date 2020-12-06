package com.example.medtek.ui.dokter.auth;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyRegisterDokterActivity extends AppCompatActivity {

    private String nama_lengkap,email,password;
    private RelativeLayout upload_image;
    private Button daftar;
    private RoundedImageView roundedImageView;
    private Toolbar toolbar;

    private static final int GALLERY = 22 ;
    private static final String APP_TAG = "MedTek";
    private static final int CAMERAA = 33;
    private final String photoFileName = "ic_user.png";
    private Uri filePath;
    private File finalFile;
    private String postPath;

    private RequestBody requestBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_dokter_verify);

        Bundle bundle = this.getIntent().getBundleExtra("form_data");
        nama_lengkap = bundle.getString("nama_lengkap");
        email = bundle.getString("email");
        password = bundle.getString("password");

        initialize();

        upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(VerifyRegisterDokterActivity.this,R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_take_image,(RelativeLayout)findViewById(R.id.bottomSheetContainer));

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
        roundedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload_image.performClick();
            }
        });

        daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath==null){
                    Toasty.error(VerifyRegisterDokterActivity.this,getResources().getString(R.string.silahkanunggahfoto)).show();
                }else{

                    File file = new File(postPath);

                    MultipartBody.Part part = MultipartBody.Part.createFormData("SIP",file.getName(),requestBody);

                    Call<JSONObject> call = RetrofitClient.getInstance().getApi().register_dokter(nama_lengkap,email,password,password,part);
                    call.enqueue(new Callback<JSONObject>() {
                        @Override
                        public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                            if (response.isSuccessful()){
                                initializeDialogSuccess();
                            }else{
                                try {
                                    String s = response.errorBody().string();
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
        upload_image = findViewById(R.id.upload_image);
        daftar = findViewById(R.id.btn_daftar_verify);
        roundedImageView = findViewById(R.id.riv_upload_image);
        toolbar = findViewById(R.id.toolbar);
        setToolbar();
    }

    private void initializeDialogSuccess(){
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this,R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_daftar_dokter_berhasil,null);
        dialogView.setBackgroundColor(Color.TRANSPARENT);
        dialog.setView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        dialog.setPositiveButton(getResources().getString(R.string.oke), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(VerifyRegisterDokterActivity.this, WelcomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            finalFile = getPhotoFileUri(photoFileName);
            filePath = FileProvider.getUriForFile(this, "com.example.medtek.fileprovider", finalFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
            if (intent.resolveActivity(this.getPackageManager()) != null) {
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                roundedImageView.setImageBitmap(bitmap);
                roundedImageView.setVisibility(View.VISIBLE);
                upload_image.setVisibility(View.GONE);
                postPath = new File(FileUtil.getPath(filePath,VerifyRegisterDokterActivity.this)).getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),new File(FileUtil.getPath(filePath,VerifyRegisterDokterActivity.this)));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERAA && resultCode == RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                roundedImageView.setImageBitmap(bitmap);
                roundedImageView.setVisibility(View.VISIBLE);
                upload_image.setVisibility(View.GONE);
                postPath = filePath.getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),getPhotoFileUri(new File(filePath.getPath()).getName()));
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
    private void choosePhotoFromGallery() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY);
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
