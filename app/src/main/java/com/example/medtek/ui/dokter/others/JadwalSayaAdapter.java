package com.example.medtek.ui.dokter.others;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.R;
import com.example.medtek.model.UserModel;
import com.example.medtek.model.dokter.JadwalModel;
import com.example.medtek.model.dokter.JanjiModel;
import com.example.medtek.network.RetrofitClient;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.TAG;

public class JadwalSayaAdapter extends RecyclerView.Adapter<JadwalSayaAdapter.ViewHolder> {

    private final List<JadwalModel> mList;
    private Context mContext;
    private final FragmentActivity mActivity;
    private String access, refresh;

    private ArrayList<Integer> idDays = new ArrayList<>();
    private ArrayList<String> newHari = new ArrayList<>();

    public JadwalSayaAdapter(List<JadwalModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_jadwal_saya,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final JadwalModel model = mList.get(position);
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

//        holder.tv_id.setText(String.valueOf(model.getIdDay()));
        holder.tv_id.setText(String.valueOf(position+1));
        holder.tv_name.setText(model.getDay());
        holder.tv_detail.setText(model.getStartHour()+" - "+model.getEndHour());
        holder.ib_ubah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDialogForm(model);
            }
        });
        holder.tv_ubah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.ib_ubah.performClick();
            }
        });
        holder.ib_hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDialogHapus(model);
            }
        });

        holder.tv_hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.ib_hapus.performClick();
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
        private final TextView tv_hapus;
        private final TextView tv_ubah;
        private final TextView tv_id;
        private final ImageButton ib_hapus;
        private final ImageButton ib_ubah;
        private final RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.rl_recycler);
            tv_id = itemView.findViewById(R.id.tv_id_day);
            tv_name = itemView.findViewById(R.id.tv_nama_day);
            tv_detail = itemView.findViewById(R.id.tv_detail_jadwal);
            tv_hapus = itemView.findViewById(R.id.tv_hapus);
            tv_ubah = itemView.findViewById(R.id.tv_edit_jadwal);
            ib_hapus = itemView.findViewById(R.id.ib_hapus);
            ib_ubah = itemView.findViewById(R.id.ib_edit);
        }
    }


    private void initializeDialogForm(JadwalModel model){
        final String[] mulai = {model.getStartHour()};
        final String[] selesai = {model.getEndHour()};
        final String[] hari = {model.getDay()};
        int idDokter = model.getIdDokter();
        Dialog dialog = new Dialog(mActivity,R.style.CustomAlertDialog);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_form_jadwal,null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        EditText et_mulai = dialogView.findViewById(R.id.et_mulai_dari);
        EditText et_selesai = dialogView.findViewById(R.id.et_mulai_selesai);
        Spinner sp_hari = dialogView.findViewById(R.id.sp_hari);
        Button btn_simpan = dialogView.findViewById(R.id.btn_simpan);

        SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Mohon tunggu ...");
        pDialog.setCancelable(false);

        ArrayList<String> arrayHari = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.hari)));

        ArrayAdapter<String> adapterS = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item,arrayHari){
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
                    textview.setTextColor(mActivity.getColor(R.color.textColorGray));
                } else {
                    textview.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_hari.setAdapter(adapterS);

        sp_hari.setSelection(arrayHari.indexOf(arrayHari.get(model.getIdDay())));
        et_mulai.setText(mulai[0]);
        et_selesai.setText(selesai[0]);
        sp_hari.setClickable(false);
        sp_hari.setEnabled(false);

        et_mulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                        mulai[0] = String.format("%02d",hourOfDay)+":"+String.format("%02d",minute);
                        et_mulai.setText(mulai[0]);
                    }
                },Integer.valueOf(mulai[0].split(":")[0]),Integer.valueOf(mulai[0].split(":")[1]),true);
                timePickerDialog.setAccentColor(mActivity.getColor(R.color.colorPrimary));
                timePickerDialog.setCancelColor(mActivity.getColor(R.color.textColorGray));
                timePickerDialog.show(mActivity.getSupportFragmentManager(),"TimePickerDialog");
            }
        });

        et_selesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mulai[0] == null){
                    Toasty.warning(mActivity,"Silahkan pilih waktu mulai terlebih dahulu!",Toasty.LENGTH_LONG).show();
                }else{
                    TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                            selesai[0] = String.format("%02d",hourOfDay)+":"+String.format("%02d",minute);
                            et_selesai.setText(selesai[0]);
                        }
                    },Integer.valueOf(selesai[0].split(":")[0]),Integer.valueOf(selesai[0].split(":")[1]),true);
                    int hour = Integer.valueOf(mulai[0].split(":")[0]);
                    int minutes = Integer.valueOf(mulai[0].split(":")[1]);
                    timePickerDialog.setMinTime(hour+1,minutes,0);
                    timePickerDialog.setAccentColor(mActivity.getColor(R.color.colorPrimary));
                    timePickerDialog.setCancelColor(mActivity.getColor(R.color.textColorGray));
                    timePickerDialog.show(mActivity.getSupportFragmentManager(),"TimePickerDialog");
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
                    ((TextView) parent.getChildAt(0)).setTextColor(mActivity.getColor(R.color.colorPrimary));
                    hari[0] = parent.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hari[0] == null){
                    Toasty.warning(mActivity,"Silahkan pilih hari terlebih dahulu!",Toasty.LENGTH_LONG).show();
                }else{
                    if (mulai[0] == null){
                        Toasty.warning(mActivity,"Silahkan pilih waktu mulai terlebih dahulu!",Toasty.LENGTH_LONG).show();
                    }else{
                        if (selesai[0] == null){
                            Toasty.warning(mActivity,"Silahkan pilih waktu selesai terlebih dahulu!",Toasty.LENGTH_LONG).show();
                        }else{
                            Log.e(ContentValues.TAG, "detailJadwal:\nidDokter->"+idDokter+"\nDay->"+ hari[0] +"\nstartHour->"+ mulai[0] +"\nendHour->"+ selesai[0]);
                            Call<ResponseBody> addJadwal = RetrofitClient.getInstance().getApi().addJadwal("Bearer "+access,idDokter, hari[0].toLowerCase(), mulai[0], selesai[0]);
                            addJadwal.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.body() != null){
                                        if (response.isSuccessful()){
                                            try {
                                                String s = response.body().string();
                                                JSONObject object = new JSONObject(s);
                                                dialog.cancel();
                                                Fragment currentFragment = mActivity.getSupportFragmentManager().findFragmentByTag("JadwalSayaFragment");
                                                FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                                                fragmentTransaction.detach(currentFragment);
                                                fragmentTransaction.attach(currentFragment);
                                                fragmentTransaction.commit();
                                                Log.e(ContentValues.TAG, "onResponse: "+s);
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

    private void initializeDialogHapus(JadwalModel model){
        Dialog dialog = new Dialog(mActivity,R.style.CustomAlertDialog);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_konfirmasi_antrian,null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        TextView tv_title = dialogView.findViewById(R.id.tv_berhasil);
        tv_title.setText(R.string.konf_hapus_jadwal);

        TextView textView = dialogView.findViewById(R.id.terms_and_condition);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setVisibility(View.GONE);

        TextView tv_andayakin = dialogView.findViewById(R.id.apakahkamuyakin);
        tv_andayakin.setText(R.string.apakahkamuyakinhapusjadwal);

        Button btnHapus = dialogView.findViewById(R.id.btnTerima);
        btnHapus.setText(R.string.hapus);

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> jsonParams = new ArrayMap<>();
                jsonParams.put("idDokter",model.getIdDokter());
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());
                Call<ResponseBody> deleteJadwal = RetrofitClient.getInstance().getApi().deleteJadwal("Bearer "+access,model.getId(),body);
                deleteJadwal.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful()){
                                if (response.body() != null){
                                    String s = response.body().string();
                                    JSONObject jsonObject = new JSONObject(s);
                                    if (jsonObject.has("success")){
                                        Toasty.success(mActivity,"Jadwal Berhasil Dihapus!",Toasty.LENGTH_LONG).show();
                                        dialog.cancel();
                                        Fragment currentFragment = mActivity.getSupportFragmentManager().findFragmentByTag("JadwalSayaFragment");
                                        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                                        fragmentTransaction.detach(currentFragment);
                                        fragmentTransaction.attach(currentFragment);
                                        fragmentTransaction.commit();
                                    }else{
                                        dialog.cancel();
                                        Toasty.warning(mActivity,jsonObject.getString("message"),Toasty.LENGTH_LONG).show();Fragment currentFragment = mActivity.getSupportFragmentManager().findFragmentByTag("JadwalSayaFragment");
                                        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                                        fragmentTransaction.detach(currentFragment);
                                        fragmentTransaction.attach(currentFragment);
                                        fragmentTransaction.commit();
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

    public void loadData(Context context) {
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }
}
