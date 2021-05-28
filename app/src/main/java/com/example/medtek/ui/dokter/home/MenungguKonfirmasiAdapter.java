package com.example.medtek.ui.dokter.home;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.dokter.JanjiModel;
import com.example.medtek.network.RetrofitClient;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.TAG;

public class MenungguKonfirmasiAdapter extends RecyclerView.Adapter<MenungguKonfirmasiAdapter.ViewHolder> {

    private final List<JanjiModel> mList;
    private Context mContext;
    private final FragmentActivity mActivity;
    private String access, refresh, ktp;
    private Bitmap bmp;
    private boolean status = false;

    public MenungguKonfirmasiAdapter(List<JanjiModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_antrian_konfirmasi,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final JanjiModel model = mList.get(position);
        loadData(mActivity);

        RelativeLayout.LayoutParams lf = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lm = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lt = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lf.setMargins((int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_16),
                (int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4));
        ll.setMargins((int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4),
                (int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_16));
        lt.setMargins((int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4),
                (int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_16));
        lm.setMargins((int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4),
                (int) mActivity.getResources().getDimension(R.dimen.margin_16), (int) mActivity.getResources().getDimension(R.dimen.margin_4));

        if(position==0){
            holder.relativeLayout.setLayoutParams(lf);
        } else if ((getItemCount()-1) == 1){
            holder.relativeLayout.setLayoutParams(lt);
        } else if(position==(getItemCount()-1)){
            holder.relativeLayout.setLayoutParams(ll);
        } else{
            holder.relativeLayout.setLayoutParams(lm);
        }

        Call<ResponseBody> getPasien = RetrofitClient.getInstance().getApi().getPasienId(model.getIdPasien());
        getPasien.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            String s = response.body().string();
                            JSONObject jsonObject = new JSONObject(s);
                            JSONObject data = jsonObject.getJSONObject("data");
                            holder.tv_name.setText(data.getString("name"));
                            JSONArray jsonArray = data.getJSONArray("image");
                            if (jsonArray.length() == 0){
                                holder.civ_pasien.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pasien));
                            }else{
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject imageObj = jsonArray.getJSONObject(i);
                                    if (imageObj.getInt("type_id") == 1){
                                        if (imageObj.getString("path").equalsIgnoreCase("/storage/Pasien.png")){
                                            holder.civ_pasien.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pasien));
                                        }else{
                                            String path = BASE_URL+jsonArray.getJSONObject(0).getString("path");
                                            Picasso.get().load(path).into(holder.civ_pasien);
                                            break;
                                        }
                                    }else{
                                        holder.civ_pasien.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pasien));
                                    }
                                }
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                if (jsonArray.getJSONObject(i).getInt("type_id") == 2){
                                    ktp = BASE_URL+jsonArray.getJSONObject(i).getString("path");
                                }
                            }
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        Log.d(TAG(MenungguKonfirmasiAdapter.class), model.getDetailJanji().split(" Pukul")[0]);
                                        String tgl = "-";
                                        String pukul = "-";
                                        String keluhan = "-";
                                        String[] arrDetailJanji = model.getDetailJanji().split(" Pukul");
                                        if (arrDetailJanji.length > 1) {
                                            String sisa = model.getDetailJanji().split(" Pukul")[1];
                                            tgl = model.getDetailJanji().split(" Pukul")[0];
                                            pukul = sisa.split("\n\nKeluhan")[0];
                                            keluhan = sisa.split("Keluhan:\n")[1];
                                        }

                                        String s = data.getString("tglLahir");
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        Date d = sdf.parse(s);
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(d);
                                        int year = c.get(Calendar.YEAR);
                                        int month = c.get(Calendar.MONTH) + 1;
                                        int date = c.get(Calendar.DATE);
                                        LocalDate l1 = LocalDate.of(year, month, date);
                                        LocalDate now1 = LocalDate.now();
                                        Period diff1 = Period.between(l1, now1);
//                                        Log.e("TAG", "onClick: "+ sisa.split("Keluhan")[0]);
                                        initializeDialogDetail(model.getIdPasien(),model.getId(),data.getString("name"), tgl, pukul,String.valueOf(diff1.getYears()),
                                               String.valueOf( data.getInt("berat_badan")),String.valueOf(data.getInt("tinggi_badan")),String.valueOf(data.getInt("lingkar_tubuh")),
                                                keluhan, ktp, model.getFilePath());
                                    } catch (JSONException | ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
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

        Locale locale = new Locale("in", "ID");
        DateTimeFormatter day = DateTimeFormatter.ofPattern("EEEE",locale).withLocale(locale);
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy",locale).withLocale(locale);
        LocalDate tglJanji = LocalDate.parse(model.getTglJanji());
        if (tglJanji.isEqual(LocalDate.now())){
            holder.tv_detail.setText("Hari Ini, "+tglJanji.format(dayFormat));
        }else if(tglJanji.isEqual(LocalDate.now().plusDays(1))){
            holder.tv_detail.setText("Besok, "+tglJanji.format(dayFormat));
        }else{
            holder.tv_detail.setText(tglJanji.format(day)+", "+tglJanji.format(dayFormat));
        }
        holder.ib_terima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDialogSuccess(model);
            }
        });
        holder.tv_terima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.ib_terima.performClick();
            }
        });
        holder.ib_tolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDialogTolak(model);
            }
        });
        holder.tv_tolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.ib_tolak.performClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tv_name;
        private final TextView tv_detail;
        private final TextView tv_tolak;
        private final TextView tv_terima;
        private final CircleImageView civ_pasien;
        private final ImageButton ib_tolak;
        private final ImageButton ib_terima;
        private final RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.rl_recycler);
            civ_pasien = itemView.findViewById(R.id.civ_pasien);
            tv_name = itemView.findViewById(R.id.tv_nama_pasien);
            tv_detail = itemView.findViewById(R.id.tv_detail_janji);
            tv_tolak = itemView.findViewById(R.id.tv_tolak);
            tv_terima = itemView.findViewById(R.id.tv_terima);
            ib_tolak = itemView.findViewById(R.id.ib_tolak);
            ib_terima = itemView.findViewById(R.id.ib_terima);
        }
    }

    private void initializeDialogSuccess(JanjiModel model){
        Dialog dialog = new Dialog(mActivity,R.style.CustomAlertDialog);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_konfirmasi_antrian,null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        TextView textView = dialogView.findViewById(R.id.terms_and_condition);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        dialogView.findViewById(R.id.btnTerima).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Call<ResponseBody> postJanji = RetrofitClient.getInstance().getApi().getAntrianId("Bearer "+access, model.getId());
                postJanji.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful()){
                                if (response.body() != null){
                                    String s = response.body().string();
                                    Log.e("TAG", "onResponse: "+s );
                                    JSONObject jsonObject = new JSONObject(s);
                                    if (jsonObject.has("success")){
                                        Toasty.success(mActivity,"Janji berhasil diterima!",Toasty.LENGTH_LONG).show();
                                        dialog.cancel();

                                        Fragment currentFragment = mActivity.getSupportFragmentManager().findFragmentByTag("FragmentHomeDokter");
                                        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                                        fragmentTransaction.detach(currentFragment);
                                        fragmentTransaction.attach(currentFragment);
                                        fragmentTransaction.commit();
                                    }
                                }else{
                                    Log.e("TAG", "onResponse: "+response.errorBody().string());
                                }
                            }else{
                                Log.e("TAG", "onResponse: "+response.errorBody().string());
                            }
                        } catch (IOException | JSONException e) {
                            Log.e("TAG", "onResponse: "+e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("TAG", "onResponse: "+t.getMessage());
                    }
                });
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void initializeDialogTolak(JanjiModel model){
        Dialog dialog = new Dialog(mActivity,R.style.CustomAlertDialog);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_konfirmasi_antrian,null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        TextView textView = dialogView.findViewById(R.id.terms_and_condition);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setVisibility(View.GONE);

        TextView tv_andayakin = dialogView.findViewById(R.id.apakahkamuyakin);
        tv_andayakin.setText(R.string.apakahkamuyakintolak);

        Button btnTolak = dialogView.findViewById(R.id.btnTerima);
        btnTolak.setText(R.string.tolak);

        btnTolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<ResponseBody> declineJanji = RetrofitClient.getInstance().getApi().declineJanji("Bearer "+access,model.getId());
                declineJanji.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful()){
                                if (response.body() != null){
                                    String s = response.body().string();
                                    JSONObject jsonObject = new JSONObject(s);
                                    if (jsonObject.has("success")){
                                        Toasty.success(mActivity,"Janji berhasil ditolak!",Toasty.LENGTH_LONG).show();
                                        dialog.cancel();

                                        Fragment currentFragment = mActivity.getSupportFragmentManager().findFragmentByTag("FragmentHomeDokter");
                                        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                                        fragmentTransaction.detach(currentFragment);
                                        fragmentTransaction.attach(currentFragment);
                                        fragmentTransaction.commit();
                                    }else{
                                        Toasty.success(mActivity,jsonObject.getString("message"),Toasty.LENGTH_LONG).show();
                                    }
                                }else{
                                    Log.e("Response Null", "onResponse: "+response.errorBody().string());
                                }
                            }else{
                                Log.e("Response Isn't Success", "onResponse: "+response.errorBody().string());
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Log.e("Exception", "onResponse: "+e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("on Failure", "onResponse: "+t.getMessage());
                    }
                });
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void initializeDialogDetail(int idPasien, int idJanji, String namaPasien, String tgl, String pukul, String umur, String berat, String tinggi, String lingkar, String keluhan, String ktp, String filePath){
        Dialog dialog = new Dialog(mActivity,R.style.CustomAlertDialog);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_detail_konfirmasi,null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);
        RoundedImageView roundedImageView = dialogView.findViewById(R.id.riv_ktp);

        SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Mohon tunggu ...");
        pDialog.setCancelable(false);

        TextView tv_ktp_pasien = dialogView.findViewById(R.id.tv_ktp_pasien);
        tv_ktp_pasien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                File ktpFile = new File(Environment.getExternalStorageDirectory(), "/Download/MedTek/janji/"+idJanji+"/"+ktp);
                if (!ktpFile.exists()){
                    Call<ResponseBody> callKTP = RetrofitClient.getInstance().getApi().getPasienKTP("Bearer "+access,idPasien);
                    callKTP.enqueue(new Callback<ResponseBody>(){
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    // display the image data in a ImageView or save it
                                    try {
                                        byte[] bytes = response.body().bytes();
                                        ktpFile.getParentFile().mkdirs();
                                        InputStream input = new ByteArrayInputStream(bytes);
                                        OutputStream output = new FileOutputStream(ktpFile);
                                        try {
                                            try {
                                                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                                int read;

                                                while ((read = input.read(buffer)) != -1) {
                                                    output.write(buffer, 0, read);
                                                }
                                                output.flush();
                                            } finally {
                                                output.close();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace(); // handle exception, define IOException and others
                                        }
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
                }
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setDataAndType(FileProvider.getUriForFile(mActivity,mActivity.getApplicationContext().getPackageName()+".fileprovider",ktpFile),"image/*");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent intent = Intent.createChooser(target, "Open File");
                try {
                    mActivity.startActivity(intent);
                    pDialog.dismiss();
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                }
            }
        });

        TextView tv_file_pasien = dialogView.findViewById(R.id.tv_file_pasien);
        tv_file_pasien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout content = dialogView.findViewById(R.id.layout_content);
                ProgressBar progressBar = dialogView.findViewById(R.id.progressbar);
                content.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                if(filePath.length() >= 4){
                    Log.e("TAG", "FilePath: "+filePath );
                }else{
                    Log.e("TAG", "FilePath: kosong" );
                }
                String filename = filePath.split("/")[4];
                File file = new File(Environment.getExternalStorageDirectory(), "/Download/MedTek/janji/"+idJanji+"/"+filename);

                if (!file.exists()){
                    Call<ResponseBody> getJanji = RetrofitClient.getInstance().getApi().getJanjiId("Bearer "+access,idJanji);
                    getJanji.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.isSuccessful()){
                                    if (response.body() != null){
                                        String s = response.body().string();
                                        JSONObject rawObject = new JSONObject(s);
                                        JSONObject data = rawObject.getJSONObject("data");
                                        if (data.getJSONArray("image").length() != 0){}{
                                            Call<ResponseBody> getImgFile = RetrofitClient.getInstance().getApi().getFile("Bearer "+access,data.getJSONArray("image").getJSONObject(0).getInt("id"));
                                            getImgFile.enqueue(new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    if (response.isSuccessful()){
                                                        if (response.body()!=null){
                                                            try {
                                                                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                                                } else {
                                                                    if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                                                                    } else {
                                                                        byte[] bytes = response.body().bytes();
                                                                        file.getParentFile().mkdirs();
                                                                        InputStream input = new ByteArrayInputStream(bytes);
                                                                        OutputStream output = new FileOutputStream(file);

                                                                        try {
                                                                            try {
                                                                                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                                                                int read;

                                                                                while ((read = input.read(buffer)) != -1) {
                                                                                    output.write(buffer, 0, read);
                                                                                }
                                                                                output.flush();
                                                                            } finally {
                                                                                output.close();
                                                                            }
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace(); // handle exception, define IOException and others
                                                                        }
                                                                    }
                                                                }
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else {
                                                            Log.e("RESPONSEBODY","ISNULL!");
                                                            getImgFile.clone().enqueue(this);
                                                        }
                                                    }else{
                                                        try {
                                                            Log.e("RESPONSEBODY","ERROR: "+response.errorBody().string());
                                                            getImgFile.clone().enqueue(this);
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                    Log.e("RESPONSEBODY","ERROR: "+t.getMessage());
                                                    getImgFile.clone().enqueue(this);
                                                }
                                            });
                                        }
                                    }
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                getJanji.clone().enqueue(this);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            getJanji.clone().enqueue(this);
                        }
                    });
                }
                Intent target = new Intent(Intent.ACTION_VIEW);
                if(filename.contains("png") || filename.contains("jpg") || filename.contains("jpeg")){
                    target.setDataAndType(FileProvider.getUriForFile(mActivity,mActivity.getApplicationContext().getPackageName()+".fileprovider",file),"image/*");
                }else if (filename.contains("pdf")){
                    target.setDataAndType(FileProvider.getUriForFile(mActivity,mActivity.getApplicationContext().getPackageName()+".fileprovider",file),"application/pdf");
                }else if (filename.contains("docx") || filename.contains("doc")){
                    target.setDataAndType(FileProvider.getUriForFile(mActivity,mActivity.getApplicationContext().getPackageName()+".fileprovider",file),"application/msword");
                }else if (filename.contains("mp4")){
                    target.setDataAndType(FileProvider.getUriForFile(mActivity,mActivity.getApplicationContext().getPackageName()+".fileprovider",file),"video/*");
                }
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent intent = Intent.createChooser(target, "Open File");
                try {
                    mActivity.startActivity(intent);
                    content.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                }
            }
        });

        TextView tv_nama = dialogView.findViewById(R.id.tv_nama_pasien);
        tv_nama.setText(namaPasien);

        TextView tv_tgl = dialogView.findViewById(R.id.tv_tgl_janji);
        tv_tgl.setText(tgl);

        TextView tv_pukul = dialogView.findViewById(R.id.tv_pukul_janji);
        tv_pukul.setText(pukul);

        TextView tv_umur = dialogView.findViewById(R.id.tv_umur_pasien);
        tv_umur.setText(umur+" Tahun");

        TextView tv_berat = dialogView.findViewById(R.id.tv_berat_pasien);
        tv_berat.setText(berat+"kg");

        TextView tv_tinggi = dialogView.findViewById(R.id.tv_tinggi_pasien);
        tv_tinggi.setText(tinggi+"cm");

        TextView tv_lingkar = dialogView.findViewById(R.id.tv_lingkar_pasien);
        tv_lingkar.setText(lingkar+"cm");

        TextView tv_keluhan = dialogView.findViewById(R.id.tv_keluhan);
        tv_keluhan.setText(keluhan);

        dialogView.findViewById(R.id.ib_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void loadData(Context context) {
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

    private static void downloadFile(String url, File outputFile) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch(FileNotFoundException e) {
            return; // swallow a 404
        } catch (IOException e) {
            return; // swallow a 404
        }
    }

}
