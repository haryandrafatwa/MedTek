package com.example.medtek.ui.pasien.home.appointment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.example.medtek.R;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.utils.FileUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
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
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
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

public class DetailPasienFragment extends Fragment {

    private String access, refresh;
    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private DatePickerDialog datePickerDialog;
    private RelativeLayout rl_content;
    private LinearLayout ll_loader;
    private ShimmerFrameLayout shimmerFrameLayout;

    private CircleImageView circleImageView;
    private TextView tv_edit_jadwal,tv_dr_name, tv_dr_specialist, tv_dr_rs, tv_dr_rs_loc;
    private EditText et_fullname, et_dob, et_bb, et_tb, et_lp, et_keluhan;
    private Button btnNext;
    private ImageButton ib_edit_jadwal;

    private RelativeLayout rl_id_card, rl_file;
    private TextView tv_upload_file;
    private ImageView iv_attach, iv_clear;
    private RoundedImageView riv_id_card;
    private static final int GALLERY = 22 ;
    private static final String APP_TAG = "MedTek";
    private static final int CAMERAA = 33;
    private static final int FILE = 44 ;
    private final String photoFileName = "ktp.png";
    private String displayName, size;
    private Uri filePath,docUri;
    private File finalFile;
    private String postPath;
    private RequestBody requestBody;

    private String nama,email,tglLahir,nama_dokter,  jam_konsul, tgl_konsul;
    private int harga,balance, bb, tb, lp;
    private Bitmap bmp;
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
        if (displayName != null && docUri != null){
            tv_upload_file.setText(displayName);
            tv_upload_file.setTextColor(getActivity().getColor(R.color.colorAccent));
            iv_clear.setVisibility(View.VISIBLE);
        }
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
                            bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                            if (bmp != null){
                                riv_id_card.setImageBitmap(bmp);
                                riv_id_card.setVisibility(View.VISIBLE);
                                rl_id_card.setVisibility(View.GONE);
                            }else{
                                Log.e("RESPONSEBODY","NOKTP!");
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
                                            path = BASE_URL+jsonArray.getJSONObject(0).getString("path");
                                        }else{
                                            path = BASE_URL+"/storage/Dokter.png";
                                        }
                                        Picasso.get().load(path).fit().centerCrop().into(circleImageView);
                                        rl_content.setVisibility(View.VISIBLE);
                                        ll_loader.setVisibility(View.GONE);
                                        shimmerFrameLayout.stopShimmer();
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
                                        if (!object.isNull("berat_badan")){
                                            bb = object.getInt("berat_badan");
                                            et_bb.setText(bb+"");
                                        }
                                        if (!object.isNull("tinggi_badan")){
                                            tb = object.getInt("tinggi_badan");
                                            et_tb.setText(tb+"");
                                        }
                                        if (!object.isNull("lingkar_tubuh")){
                                            lp = object.getInt("lingkar_tubuh");
                                            et_lp.setText(lp+"");
                                        }
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

        /*Call<ResponseBody> getUserDetail = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
        getUserDetail.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            String s = response.body().string();
                            JSONObject object = new JSONObject(s);
                            et_fullname.setText(object.getString("name"));
                            nama = object.getString("name");
                            email = object.getString("email");
                            if (!object.isNull("berat_badan")){
                                bb = object.getInt("berat_badan");
                                et_bb.setText(bb+"");
                            }
                            if (!object.isNull("tinggi_badan")){
                                tb = object.getInt("tinggi_badan");
                                et_tb.setText(tb+"");
                            }
                            if (!object.isNull("lingkar_tubuh")){
                                lp = object.getInt("lingkar_tubuh");
                                et_lp.setText(lp+"");
                            }
                            JSONObject walletObj = object.getJSONObject("wallet");
                            balance = walletObj.getInt("balance");
                            if (!object.getString("tglLahir").equalsIgnoreCase("null")){
                                Locale locale = new Locale("in", "ID");
                                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM YYYY",locale);
                                LocalDate localDate = LocalDate.parse(object.getString("tglLahir"));
                                et_dob.setText(localDate.format(dateFormat));
                            }
                            JSONArray imageArray = object.getJSONArray("image");
                            if (imageArray.length() != 0 ){
                                for (int i = 0; i < imageArray.length(); i++) {
                                    JSONObject imageObj = imageArray.getJSONObject(i);
                                    if (imageObj.getInt("type_id") == 2){
                                        Picasso.get().load(BASE_URL+imageObj.getString("path")).into(riv_id_card);
                                        Log.e("TAG", "onResponse: "+ BASE_URL+imageObj.getString("path"));
                                    }
                                }
                            }
                            rl_content.setVisibility(View.VISIBLE);
                            ll_loader.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });*/

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

        rl_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        Intent intent = new Intent();
                        intent.setType("application/pdf");
                        intent.setType("application/msword");
                        intent.setType("*/*");
                        String[] mimetypes = {"image/*","application/pdf","application/msword","video/*"};
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                        startActivityForResult(Intent.createChooser(intent, "Pilih file"), FILE);
                    }
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bb = Integer.parseInt(et_bb.getText().toString().trim());
                tb = Integer.parseInt(et_tb.getText().toString().trim());
                lp = Integer.parseInt(et_lp.getText().toString().trim());

                Map<String, Object> jsonParams = new ArrayMap<>();
                jsonParams.put("name",nama);
                jsonParams.put("email",email);
                jsonParams.put("tglLahir",tglLahir);
                jsonParams.put("berat_badan",bb);
                jsonParams.put("tinggi_badan",tb);
                jsonParams.put("lingkar_tubuh",lp);

                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());

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
                                        bundle.putString("nama",nama);
                                        bundle.putString("nama_dokter",nama_dokter);
                                        bundle.putInt("harga",harga);
                                        bundle.putInt("balance",balance);
                                        bundle.putString("detailJanji",et_keluhan.getText().toString().trim());

                                        if (docUri != null){
                                            bundle.putString("uri",docUri.toString());
                                        }

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
        et_fullname = getActivity().findViewById(R.id.et_fullname);
        et_dob = getActivity().findViewById(R.id.et_dob);
        et_bb = getActivity().findViewById(R.id.et_beratbadan);
        et_tb = getActivity().findViewById(R.id.et_tinggibadan);
        et_lp = getActivity().findViewById(R.id.et_lingkarperut);
        et_keluhan = getActivity().findViewById(R.id.et_detail_janji);
        btnNext = getActivity().findViewById(R.id.btnNext);

        rl_id_card = getActivity().findViewById(R.id.upload_image);
        riv_id_card = getActivity().findViewById(R.id.riv_upload_image);
        rl_file = getActivity().findViewById(R.id.upload_file);
        tv_upload_file = getActivity().findViewById(R.id.tv_upload_file);
        iv_attach = getActivity().findViewById(R.id.image_attach);
        iv_clear = getActivity().findViewById(R.id.iv_clear);

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

        tv_upload_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_file.performClick();
            }
        });
        iv_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_file.performClick();
            }
        });
        iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                docUri = null;
                tv_upload_file.setText(R.string.uploadfiles);
                tv_upload_file.setTextColor(Color.parseColor("#60000000"));
                iv_clear.setVisibility(View.GONE);
            }
        });

        et_keluhan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnNext.setEnabled(!et_fullname.getText().toString().trim().isEmpty() && !et_dob.getText().toString().trim().isEmpty() && !et_bb.getText().toString().trim().isEmpty() &&
                        !et_tb.getText().toString().trim().isEmpty() && !et_lp.getText().toString().trim().isEmpty() && !et_keluhan.getText().toString().trim().isEmpty() && (filePath == null || bmp == null));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_lp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnNext.setEnabled(!et_fullname.getText().toString().trim().isEmpty() && !et_dob.getText().toString().trim().isEmpty() && !et_bb.getText().toString().trim().isEmpty() &&
                        !et_tb.getText().toString().trim().isEmpty() && !et_lp.getText().toString().trim().isEmpty() && !et_keluhan.getText().toString().trim().isEmpty() && (filePath == null || bmp == null));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_tb.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnNext.setEnabled(!et_fullname.getText().toString().trim().isEmpty() && !et_dob.getText().toString().trim().isEmpty() && !et_bb.getText().toString().trim().isEmpty() &&
                        !et_tb.getText().toString().trim().isEmpty() && !et_lp.getText().toString().trim().isEmpty() && !et_keluhan.getText().toString().trim().isEmpty() && (filePath == null || bmp == null));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_bb.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnNext.setEnabled(!et_fullname.getText().toString().trim().isEmpty() && !et_dob.getText().toString().trim().isEmpty() && !et_bb.getText().toString().trim().isEmpty() &&
                        !et_tb.getText().toString().trim().isEmpty() && !et_lp.getText().toString().trim().isEmpty() && !et_keluhan.getText().toString().trim().isEmpty() && (filePath == null || bmp == null));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_dob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnNext.setEnabled(!et_fullname.getText().toString().trim().isEmpty() && !et_dob.getText().toString().trim().isEmpty() && !et_bb.getText().toString().trim().isEmpty() &&
                        !et_tb.getText().toString().trim().isEmpty() && !et_lp.getText().toString().trim().isEmpty() && !et_keluhan.getText().toString().trim().isEmpty() && (filePath == null || bmp == null));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_fullname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnNext.setEnabled(!et_fullname.getText().toString().trim().isEmpty() && !et_dob.getText().toString().trim().isEmpty() && !et_bb.getText().toString().trim().isEmpty() &&
                        !et_tb.getText().toString().trim().isEmpty() && !et_lp.getText().toString().trim().isEmpty() && !et_keluhan.getText().toString().trim().isEmpty() && (filePath == null || bmp == null));
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                File file = new File(postPath);
                MultipartBody.Part part = MultipartBody.Part.createFormData("ktp",file.getName(),requestBody);
                rl_content.setVisibility(View.GONE);
                ll_loader.setVisibility(View.VISIBLE);
                shimmerFrameLayout.startShimmer();

                Call<ResponseBody> callKTP = RetrofitClient.getInstance().getApi().uploadKTP("Bearer "+access,part);
                callKTP.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String s = response.body().string();
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.has("success")){
                                rl_content.setVisibility(View.VISIBLE);
                                ll_loader.setVisibility(View.GONE);
                                shimmerFrameLayout.stopShimmer();
                            }
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
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }else if (requestCode == CAMERAA && resultCode == RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                riv_id_card.setImageBitmap(bitmap);
                riv_id_card.setVisibility(View.VISIBLE);
                rl_id_card.setVisibility(View.GONE);
                postPath = filePath.getPath();
                requestBody = RequestBody.create(MediaType.parse("image/*"),getPhotoFileUri(new File(filePath.getPath()).getName()));
                File file = new File(postPath);
                MultipartBody.Part part = MultipartBody.Part.createFormData("ktp",file.getName(),requestBody);
                rl_content.setVisibility(View.GONE);
                ll_loader.setVisibility(View.VISIBLE);
                shimmerFrameLayout.startShimmer();

                Call<ResponseBody> callKTP = RetrofitClient.getInstance().getApi().uploadKTP("Bearer "+access,part);
                callKTP.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String s = response.body().string();
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.has("success")){
                                rl_content.setVisibility(View.VISIBLE);
                                ll_loader.setVisibility(View.GONE);
                                shimmerFrameLayout.stopShimmer();
                            }else{
                                callKTP.clone().enqueue(this);
                            }
                            Log.e("RESPONSEBODY",s);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            callKTP.clone().enqueue(this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        callKTP.clone().enqueue(this);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == FILE && resultCode == RESULT_OK) {
            docUri = data.getData();
            dumpImageMetaData(docUri);
            if (Integer.parseInt(size) > 10000000){
                Toasty.error(getActivity(), "Ukuran file melebihi batas! Silahkan pilih file dengan ukuran maksimal 10MB.",Toasty.LENGTH_LONG).show();
                docUri = null;
                tv_upload_file.setText(R.string.uploadfiles);
                tv_upload_file.setTextColor(Color.parseColor("#60000000"));
                iv_clear.setVisibility(View.GONE);
            }else{
                tv_upload_file.setText(displayName);
                tv_upload_file.setTextColor(getActivity().getColor(R.color.colorAccent));
                iv_clear.setVisibility(View.VISIBLE);
            }
        }

    }

    public void dumpImageMetaData(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("TAG", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i("TAG", "Size: " + size);
            }
        } finally {
            cursor.close();
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
                intent.setAction(Intent.ACTION_PICK);
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
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

}
