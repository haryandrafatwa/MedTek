package com.example.medtek.ui.pasien.others;

import android.Manifest;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.medtek.R;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.utils.FileUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;

public class BuktiTransferFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private String access,refresh;
    private int nominal;
    private String penerima, rekPenerima, pengirim, rekPengirim;

    private RelativeLayout upload_image;
    private Button kirim;
    private RoundedImageView roundedImageView;

    private static final int GALLERY = 22 ;
    private static final String APP_TAG = "MedTek";
    private static final int CAMERAA = 33;
    private final String photoFileName = "ic_user.png";
    private Uri filePath;
    private File finalFile;
    private String postPath;
    private RequestBody requestBody;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bukti_transfer, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        loadData(getActivity());
        Bundle bundle = getArguments();
        nominal = bundle.getInt("nominal");
        penerima = bundle.getString("penerima");
        rekPenerima = bundle.getString("rekPenerima");
        pengirim = bundle.getString("pengirim");
        rekPengirim = bundle.getString("rekPengirim");

        upload_image.setOnClickListener(new View.OnClickListener() {
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
        roundedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload_image.performClick();
            }
        });
        kirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDialogSuccess();
            }
        });

    }

    private void initializeDialogSuccess(){
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getActivity(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_daftar_dokter_berhasil,null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        dialog.setPositiveButton(getResources().getString(R.string.oke), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                File file = new File(postPath);

                MultipartBody.Part part = MultipartBody.Part.createFormData("bukti", file.getName(),requestBody);

                Call<ResponseBody> call = RetrofitClient.getInstance().getApi().postTopup("Bearer "+access, nominal, penerima, rekPenerima, pengirim, rekPengirim, part);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String s = response.body().string();
                            JSONObject object = new JSONObject(s);
                            if (object.has("success")){
                                closeFragment();
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

        dialog.show();
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
                roundedImageView.setImageBitmap(bitmap);
                roundedImageView.setVisibility(View.VISIBLE);
                upload_image.setVisibility(View.GONE);
                postPath = new File(FileUtil.getPath(filePath,getActivity())).getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),new File(FileUtil.getPath(filePath,getActivity())));
                if (filePath!=null){
                    kirim.setEnabled(true);
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
                roundedImageView.setImageBitmap(bitmap);
                roundedImageView.setVisibility(View.VISIBLE);
                upload_image.setVisibility(View.GONE);
                postPath = filePath.getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),getPhotoFileUri(new File(filePath.getPath()).getName()));
                if (filePath!=null){
                    kirim.setEnabled(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private void initialize() {
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();


        upload_image = getActivity().findViewById(R.id.upload_image);
        kirim = getActivity().findViewById(R.id.btn_kirim);
        roundedImageView = getActivity().findViewById(R.id.riv_upload_image);
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

//    private void setFragment(Fragment fragment) {
    private void closeFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack("FragmentNominal",0);
        fm.popBackStack();
    }

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

}
