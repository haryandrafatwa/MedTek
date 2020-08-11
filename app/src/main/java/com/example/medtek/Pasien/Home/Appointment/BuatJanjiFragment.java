package com.example.medtek.Pasien.Home.Appointment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuatJanjiFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;

    private RelativeLayout rl_content;
    private LinearLayout ll_loader;
    private ShimmerFrameLayout shimmerFrameLayout;

    private CircleImageView circleImageView;
    private TextView tv_dr_name, tv_dr_specialist, tv_dr_rs, tv_dr_rs_loc;
    private Button btnNext;

    private LinearLayout ll_left, ll_center, ll_right, ll_pick;
    private TextView tv_day_left, tv_date_left, tv_day_center, tv_date_center, tv_day_right, tv_date_right;

    private long left=0, center=0, right=0;

    private String day="",timeString="",date="";
    private DatePickerDialog datePickerDialog;
    private Calendar[] selectableDays;
    private List<int[]> days = new ArrayList<>();

    private boolean morningExpanded=false, noonExpanded=false, afternoonExpanded=false,eveningExpanded=false;
    private LinearLayout layout_morning, layout_noon, layout_afternoon, layout_evening;
    private RelativeLayout header_morning, header_noon, header_afternoon, header_evening;

    private int id,id_dokter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buat_janji, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        AndroidThreeTen.init(getActivity());
        initialize();

        Bundle bundle = getArguments();
        id_dokter = bundle.getInt("id_dokter");

        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().getDokterId(id_dokter);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String s = response.body().string();
                    JSONObject object = new JSONObject(s);
                    JSONObject obj = new JSONObject(object.getString("data"));
                    if (new JSONArray(obj.getString("jadwal")).length() !=0){
                        tv_dr_name.setText(obj.getString("name"));
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

                        JSONArray jsonArray = new JSONArray(obj.getString("jadwal"));
                        String[] strDays = new String[] { "", "Senin", "Selasa","Rabu", "Kamis","Jumat", "Sabtu", "Minggu" };

                        LocalDateTime date = LocalDateTime.now();
                        Locale locale = new Locale("in", "ID");
                        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEEE",locale);
                        DateTimeFormatter halfDateFormat = DateTimeFormatter.ofPattern("dd MMM",locale);

                        if (jsonArray.length() == 1){
                            int dayInt = Integer.parseInt(jsonArray.getJSONObject(0).getString("day_id"));
                            for (int i = 0; i < 3; i++) {
                                if (date.format(dayFormat).equalsIgnoreCase(strDays[dayInt])){
                                    Log.e("GETJADWALDOKTER", date.format(dayFormat)+": "+strDays[dayInt]);
                                    if (tv_day_left.getText().toString().isEmpty()){
                                        tv_day_left.setText("Hari Ini");
                                        tv_date_left.setText(date.format(halfDateFormat));
                                    }else if (tv_day_center.getText().toString().isEmpty()){
                                        tv_day_center.setText(strDays[dayInt]);
                                        tv_date_center.setText(date.plusDays(7).format(halfDateFormat));
                                    }else {
                                        tv_day_right.setText(strDays[dayInt]);
                                        tv_date_right.setText(date.plusDays(14).format(halfDateFormat));
                                    }
                                }else{
                                    for (int j = 0; j < 7; j++) {
                                        if (date.plusDays(j+1).format(dayFormat).equalsIgnoreCase(strDays[dayInt])){
                                            if (tv_day_left.getText().toString().isEmpty()){
                                                if (j==0){
                                                    tv_day_left.setText("Besok");
                                                }else{
                                                    tv_day_left.setText(strDays[dayInt]);
                                                }
                                                tv_date_left.setText(date.plusDays(j+1).format(halfDateFormat));
                                            }else if (tv_day_center.getText().toString().isEmpty()){
                                                tv_day_center.setText(strDays[dayInt]);
                                                tv_date_center.setText(date.plusDays(j+8).format(halfDateFormat));
                                            }else {
                                                tv_day_right.setText(strDays[dayInt]);
                                                tv_date_right.setText(date.plusDays(j+15).format(halfDateFormat));
                                            }
                                        }
                                    }
                                }
                            }
                        }else if(jsonArray.length() == 2){
                            List<Integer> dayId = new ArrayList<>();
                            dayId.add(Integer.parseInt(jsonArray.getJSONObject(0).getString("day_id")));
                            dayId.add(Integer.parseInt(jsonArray.getJSONObject(1).getString("day_id")));
                            Collections.sort(dayId);
                            for (int i = 0; i < 3; i++) {
                                if (i==2){
                                    int dayInt = dayId.get(0);

                                    String dayTemp = tv_day_center.getText().toString().trim();
                                    String dateTemp = tv_date_center.getText().toString().trim();
                                    if (dayTemp.equalsIgnoreCase(strDays[dayInt])){
                                        dayInt = Integer.parseInt(jsonArray.getJSONObject(1).getString("day_id"));
                                    }

                                    if (date.format(dayFormat).equalsIgnoreCase(strDays[dayInt])){
                                        if (tv_day_left.getText().toString().isEmpty()){
                                            tv_day_left.setText("Hari Ini");
                                            tv_date_left.setText(date.format(halfDateFormat));
                                        }else if (tv_day_center.getText().toString().isEmpty()){
                                            tv_day_center.setText(strDays[dayInt]);
                                            tv_date_center.setText(date.plusDays(7).format(halfDateFormat));
                                        }else {
                                            tv_day_right.setText(strDays[dayInt]);
                                            tv_date_right.setText(date.plusDays(7).format(halfDateFormat));
                                        }
                                        Log.e("GETJADWALDOKTER",date.format(halfDateFormat)+"; "+date.plusDays(7).format(halfDateFormat)+"; "+date.plusDays(14).format(halfDateFormat));
                                    }else{
                                        for (int j = 7; j < 14; j++) {
                                            if (date.plusDays(j+1).format(dayFormat).equalsIgnoreCase(strDays[dayInt])){
                                                if (tv_day_center.getText().toString().isEmpty()){
                                                    center = date.plusDays(j+1).toEpochSecond(ZoneOffset.UTC);
                                                    tv_day_center.setText(strDays[dayInt]);
                                                    tv_date_center.setText(date.plusDays(j+1).format(halfDateFormat));
                                                }else {
                                                    right = date.plusDays(j+1).toEpochSecond(ZoneOffset.UTC);
//                                                if (Integer.parseInt(tv_date_center.getText().toString().split(" ")[0]) > Integer.parseInt(date.plusDays(j+1).format(halfDateFormat).split(" ")[0])){
                                                    if (center > right){
                                                        String dayValue = tv_day_center.getText().toString().trim();
                                                        String dateValue = tv_date_center.getText().toString().trim();
                                                        tv_day_center.setText(strDays[dayInt]);
                                                        tv_date_center.setText(date.plusDays(j+1).format(halfDateFormat));
                                                        tv_day_right.setText(dayValue);
                                                        tv_date_right.setText(dateValue);
                                                    }else{
                                                        tv_day_right.setText(strDays[dayInt]);
                                                        tv_date_right.setText(date.plusDays(j+1).format(halfDateFormat));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    int dayInt = dayId.get(i);

                                    if (date.format(dayFormat).equalsIgnoreCase(strDays[dayInt])){
                                        if (tv_day_left.getText().toString().isEmpty()){
                                            tv_day_left.setText("Hari Ini");
                                            tv_date_left.setText(date.format(halfDateFormat));
                                        }else if (tv_day_center.getText().toString().isEmpty()){
                                            String dayValue = tv_day_left.getText().toString().trim();
                                            String dateValue = tv_date_left.getText().toString().trim();
                                            tv_day_left.setText("Hari Ini");
                                            tv_date_left.setText(date.format(halfDateFormat));
                                            tv_day_center.setText(dayValue);
                                            tv_date_center.setText(dateValue);
                                        }else {
                                            tv_day_right.setText(strDays[dayInt]);
                                            tv_date_right.setText(date.plusDays(7).format(halfDateFormat));
                                        }
                                        Log.e("GETJADWALDOKTER",date.format(halfDateFormat)+"; "+date.plusDays(7).format(halfDateFormat)+"; "+date.plusDays(14).format(halfDateFormat));
                                    }else{
                                        for (int j = 0; j < 7; j++) {
                                            if (date.plusDays(j+1).format(dayFormat).equalsIgnoreCase(strDays[dayInt])){
                                                if (tv_day_left.getText().toString().isEmpty()){
                                                    left = date.plusDays(j+1).toEpochSecond(ZoneOffset.UTC);
                                                    if ((j+1) == 1){
                                                        tv_day_left.setText("Besok");
                                                    }else{
                                                        tv_day_left.setText(strDays[dayInt]);
                                                    }
                                                    tv_date_left.setText(date.plusDays(j+1).format(halfDateFormat));
                                                }else if (tv_day_center.getText().toString().isEmpty()){
                                                    center = date.plusDays(j+1).toEpochSecond(ZoneOffset.UTC);

                                                    if (left > center){
                                                        String dayValue = tv_day_left.getText().toString().trim();
                                                        String dateValue = tv_date_left.getText().toString().trim();
//                                                        tv_day_left.setText(strDays[dayInt]);
                                                        tv_day_left.setText("Hari Ini");
                                                        tv_date_left.setText(date.plusDays(j+1).format(halfDateFormat));
                                                        tv_day_center.setText(dayValue);
                                                        tv_date_center.setText(dateValue);
                                                    }else{
                                                        if ((j+1) == 1){
                                                            tv_day_center.setText("Besok");
                                                        }else{
                                                            tv_day_center.setText(strDays[dayInt]);
                                                        }
                                                        tv_date_center.setText(date.plusDays(j+1).format(halfDateFormat));
                                                    }
                                                }else {
                                                    right = date.plusDays(j+1).toEpochSecond(ZoneOffset.UTC);
//                                                if (Integer.parseInt(tv_date_center.getText().toString().split(" ")[0]) > Integer.parseInt(date.plusDays(j+1).format(halfDateFormat).split(" ")[0])){
                                                    if (center > right){
                                                        String dayValue = tv_day_center.getText().toString().trim();
                                                        String dateValue = tv_date_center.getText().toString().trim();
                                                        tv_day_center.setText(strDays[dayInt]);
                                                        tv_date_center.setText(date.plusDays(j+1).format(halfDateFormat));
                                                        tv_day_right.setText(dayValue);
                                                        tv_date_right.setText(dateValue);
                                                    }else{
                                                        tv_day_right.setText(strDays[dayInt]);
                                                        tv_date_right.setText(date.plusDays(j+1).format(halfDateFormat));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }else{
                            for (int i = 0; i < jsonArray.length(); i++) {
                                int dayInt = Integer.parseInt(jsonArray.getJSONObject(i).getString("day_id"));

                                if (date.format(dayFormat).equalsIgnoreCase(strDays[dayInt])){
                                    if (tv_day_left.getText().toString().isEmpty()){
                                        left = date.toEpochSecond(ZoneOffset.UTC);
                                        tv_day_left.setText("Hari Ini");
                                        tv_date_left.setText(date.format(halfDateFormat));
                                    }else if (tv_day_center.getText().toString().isEmpty()){
                                        center = date.toEpochSecond(ZoneOffset.UTC);
                                        if (left > center){
                                            String dayValue = tv_day_left.getText().toString().trim();
                                            String dateValue = tv_date_left.getText().toString().trim();
//                                            tv_day_left.setText(strDays[dayInt]);
                                            tv_day_left.setText("Hari Ini");
                                            tv_date_left.setText(date.format(halfDateFormat));
                                            tv_day_center.setText(dayValue);
                                            tv_date_center.setText(dateValue);
                                            long temp = left;
                                            left = center;
                                            center = temp;
                                        }else{
                                            tv_day_center.setText(strDays[dayInt]);
                                            tv_date_center.setText(date.format(halfDateFormat));
                                        }
                                    }else {
                                        right = date.toEpochSecond(ZoneOffset.UTC);
                                        tv_day_right.setText(strDays[dayInt]);
                                        tv_date_right.setText(date.format(halfDateFormat));
                                        if (center > right){
                                            String dayCenter = tv_day_center.getText().toString().trim();
                                            String dateCenter = tv_date_center.getText().toString().trim();
                                            String dayLeft = tv_day_left.getText().toString().trim();
                                            String dateLeft = tv_date_left.getText().toString().trim();
                                            if (left > right){
                                                tv_day_left.setText("Hari Ini");
                                                tv_date_left.setText(date.format(halfDateFormat));
                                                tv_day_center.setText(dayLeft);
                                                tv_date_center.setText(dateLeft);
                                                tv_day_right.setText(dayCenter);
                                                tv_date_right.setText(dateCenter);
                                            }else{
                                                tv_day_center.setText(strDays[dayInt]);
                                                tv_date_center.setText(date.format(halfDateFormat));
                                                tv_day_right.setText(dayCenter);
                                                tv_date_right.setText(dateCenter);
                                            }

                                        }else{
                                            tv_day_right.setText(strDays[dayInt]);
                                            tv_date_right.setText(date.format(halfDateFormat));
                                        }
                                    }
                                }else{
                                    for (int j = 0; j < 7; j++) {
                                        if (date.plusDays(j+1).format(dayFormat).equalsIgnoreCase(strDays[dayInt])){
                                            if (tv_day_left.getText().toString().isEmpty()){
                                                left = date.plusDays(j+1).toEpochSecond(ZoneOffset.UTC);
                                                tv_day_left.setText(strDays[dayInt]);
                                                tv_date_left.setText(date.plusDays(j+1).format(halfDateFormat));
                                            }else if (tv_day_center.getText().toString().isEmpty()){
                                                center = date.plusDays(j+1).toEpochSecond(ZoneOffset.UTC);
                                                if (left > center){
                                                    String dayValue = tv_day_left.getText().toString().trim();
                                                    String dateValue = tv_date_left.getText().toString().trim();
                                                    tv_day_left.setText(strDays[dayInt]);
                                                    tv_date_left.setText(date.plusDays(j+1).format(halfDateFormat));
                                                    tv_day_center.setText(dayValue);
                                                    tv_date_center.setText(dateValue);
                                                }else{
                                                    tv_day_center.setText(strDays[dayInt]);
                                                    tv_date_center.setText(date.plusDays(j+1).format(halfDateFormat));
                                                }
                                            }else {
                                                right = date.plusDays(j+1).toEpochSecond(ZoneOffset.UTC);
                                                tv_day_right.setText(strDays[dayInt]);
                                                tv_date_right.setText(date.plusDays(j+1).format(halfDateFormat));
                                                Log.e("COMPARINGRANDC",center+" : "+right);
                                                if (center > right){
                                                    String dayCenter = tv_day_center.getText().toString().trim();
                                                    String dateCenter = tv_date_center.getText().toString().trim();
                                                    String dayLeft = tv_day_left.getText().toString().trim();
                                                    String dateLeft = tv_date_left.getText().toString().trim();
                                                    if (left > right){
                                                        tv_day_left.setText("Hari Ini");
                                                        tv_date_left.setText(date.plusDays(j+1).format(halfDateFormat));
                                                        tv_day_center.setText(dayLeft);
                                                        tv_date_center.setText(dateLeft);
                                                        tv_day_right.setText(dayCenter);
                                                        tv_date_right.setText(dateCenter);
                                                    }else{
                                                        tv_day_center.setText(strDays[dayInt]);
                                                        tv_date_center.setText(date.plusDays(j+1).format(halfDateFormat));
                                                        tv_day_right.setText(dayCenter);
                                                        tv_date_right.setText(dateCenter);
                                                    }

                                                }else{
                                                    tv_day_right.setText(strDays[dayInt]);
                                                    tv_date_right.setText(date.plusDays(j+1).format(halfDateFormat));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        days.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            int[] jadwal = new int[3];
                            jadwal[0] = Integer.parseInt(jsonArray.getJSONObject(i).getString("day_id"));
                            String startHour = jsonArray.getJSONObject(i).getString("startHour");
                            int sHour = Integer.parseInt(startHour.split(":")[0]);
                            String endHour = jsonArray.getJSONObject(i).getString("endHour");
                            int eHour = Integer.parseInt(endHour.split(":")[0]);
                            jadwal[1] = sHour;
                            jadwal[2] = eHour;
                            days.add(jadwal);
                        }

                        ll_pick.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialog(days);
                            }
                        });
                        Picasso.get().load(path).into(circleImageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                rl_content.setVisibility(View.VISIBLE);
                                ll_loader.setVisibility(View.GONE);
                                shimmerFrameLayout.startShimmer();
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }else{
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toasty.success(getActivity(),"Tanggal: "+day+", "+date+", Jam: "+timeString,Toasty.LENGTH_LONG).show();
                DetailPasienFragment detailPasienFragment = new DetailPasienFragment();

                Locale locale = new Locale("in", "ID");
                DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy",locale).withLocale(locale);
                DateTimeFormatter fullFormat = DateTimeFormatter.ofPattern("YYYY-MM-dd",locale);
                LocalDate localDate = LocalDate.parse(date+" 2020",dayFormat);
//                Toasty.success(getActivity(),date+", "+timeString).show();
                bundle.putString("date",localDate.format(fullFormat));
                bundle.putString("time",timeString);
                detailPasienFragment.setArguments(bundle);
                setFragment(detailPasienFragment,"DetailPasienFragment");
                day="";
                timeString="";
            }
        });

    }

    private void showDialog(List<int[]> selectable){
        id=0;
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
                DateTimeFormatter halfDateFormat = DateTimeFormatter.ofPattern("dd MMM",locale);
                LocalDate localDate = LocalDate.of(year,monthOfYear+1,dayOfMonth);
                day = strDays[datePicker.get(Calendar.DAY_OF_WEEK)-1];
                date = localDate.format(halfDateFormat);
                btnNext.setEnabled(false);

                for (int i = layout_morning.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_morning.removeView(layout_morning.getChildAt(i));
                    }

                    if (i == 1){
                        layout_morning.getChildAt(i).setVisibility(View.GONE);
                        layout_morning.setPadding(0,0,0,0);
                        header_morning.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        morningExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_morning.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_morning.setLayoutParams(params);
                    }
                }
                for (int i = layout_noon.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_noon.removeView(layout_noon.getChildAt(i));
                    }

                    if (i == 1){
                        layout_noon.getChildAt(i).setVisibility(View.GONE);
                        layout_noon.setPadding(0,0,0,0);
                        header_noon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        noonExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_noon.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_noon.setLayoutParams(params);
                    }
                }
                for (int i = layout_afternoon.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_afternoon.removeView(layout_afternoon.getChildAt(i));
                    }

                    if (i == 1){
                        layout_afternoon.getChildAt(i).setVisibility(View.GONE);
                        layout_afternoon.setPadding(0,0,0,0);
                        header_afternoon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        afternoonExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_afternoon.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_afternoon.setLayoutParams(params);
                    }
                }
                for (int i = layout_evening.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_evening.removeView(layout_evening.getChildAt(i));
                    }

                    if (i == 1){
                        layout_evening.getChildAt(i).setVisibility(View.GONE);
                        layout_evening.setPadding(0,0,0,0);
                        header_evening.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        eveningExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_evening.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_evening.setLayoutParams(params);
                    }
                }

                List<Integer> morningTime = new ArrayList<>();
                List<Integer> noonTime = new ArrayList<>();
                List<Integer> afternoonTime = new ArrayList<>();
                List<Integer> eveningTime = new ArrayList<>();

                for (int i = 0; i < days.size(); i++) {
                    if (strDays[days.get(i)[0]].equalsIgnoreCase(day)){
                        for (int j = days.get(i)[1]; j <= days.get(i)[2]; j++) {
                            if (j>=8 && j <= 11){
                                morningTime.add(j);
                            }else if(j>=12 && j <= 14){
                                noonTime.add(j);
                            }else if (j>=15 && j <= 17){
                                afternoonTime.add(j);
                            }else if(j>=18 && j <= 20){
                                eveningTime.add(j);
                            }
                        }
                    }
                }

                if ( eveningTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_evening.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_evening.setLayoutParams(params);
                }else if (afternoonTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_afternoon.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_afternoon.setLayoutParams(params);
                }else if ( noonTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_noon.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_noon.setLayoutParams(params);
                }else if (morningTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_morning.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_morning.setLayoutParams(params);
                }

                if ( morningTime.size() != 0){
                    layout_morning.setVisibility(View.VISIBLE);
                    if (layout_morning.getChildCount() != 3){
                        if (morningTime.size() < 4){
                            layout_morning.addView(createLayout(morningTime));
                        }else{
                            List<Integer> batch1 = morningTime.subList(0,3);
                            List<Integer> batch2 = morningTime.subList(3,4);
                            layout_morning.addView(createLayout(batch1));
                            layout_morning.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_morning.setVisibility(View.GONE);
                }

                if(noonTime.size() != 0){
                    layout_noon.setVisibility(View.VISIBLE);
                    if (layout_noon.getChildCount() != 3){
                        if (noonTime.size() < 4){
                            layout_noon.addView(createLayout(noonTime));
                        }else{
                            List<Integer> batch1 = noonTime.subList(0,3);
                            List<Integer> batch2 = noonTime.subList(3,4);
                            layout_noon.addView(createLayout(batch1));
                            layout_noon.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_noon.setVisibility(View.GONE);
                }

                if (afternoonTime.size() != 0){
                    layout_afternoon.setVisibility(View.VISIBLE);
                    if (layout_afternoon.getChildCount() != 3){
                        if (afternoonTime.size() < 4){
                            layout_afternoon.addView(createLayout(afternoonTime));
                        }else{
                            List<Integer> batch1 = afternoonTime.subList(0,3);
                            List<Integer> batch2 = afternoonTime.subList(3,4);
                            layout_afternoon.addView(createLayout(batch1));
                            layout_afternoon.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_afternoon.setVisibility(View.GONE);
                }

                if (eveningTime.size() != 0){
                    layout_evening.setVisibility(View.VISIBLE);
                    if (layout_evening.getChildCount() != 3){
                        if (eveningTime.size() < 4){
                            layout_evening.addView(createLayout(eveningTime));
                        }else{
                            List<Integer> batch1 = eveningTime.subList(0,3);
                            List<Integer> batch2 = eveningTime.subList(3,4);
                            layout_evening.addView(createLayout(batch1));
                            layout_evening.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_evening.setVisibility(View.GONE);
                }

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(Calendar.MONTH,calendar.get(Calendar.MONTH)+1);

        datePickerDialog.setMinDate(calendar);
        datePickerDialog.setMaxDate(maxDate);

        for (Calendar loopDate = calendar; calendar.before(maxDate);calendar.add(Calendar.DATE,1),loopDate = calendar){
            int dayOfWeek = loopDate.get(Calendar.DAY_OF_WEEK);
            for (int i = 0; i < selectable.size(); i++) {
                if ((dayOfWeek-1) == selectable.get(i)[0]){
                    selectableDays = new Calendar[1];
                    selectableDays[0] = loopDate;
                    datePickerDialog.setSelectableDays(selectableDays);
                }
            }
        }
        datePickerDialog.setAccentColor(getActivity().getColor(R.color.colorPrimary));
        datePickerDialog.setCancelColor(getActivity().getColor(R.color.textColorGray));
        datePickerDialog.show(getActivity().getSupportFragmentManager(),"DatePickerDialog");
    }

    private void initialize() {
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);

        shimmerFrameLayout = getActivity().findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.startShimmer();

        rl_content = getActivity().findViewById(R.id.layout_visible);
        ll_loader = getActivity().findViewById(R.id.layout_loader);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        circleImageView = getActivity().findViewById(R.id.civ_dokter);
        tv_dr_name = getActivity().findViewById(R.id.tv_dr_name);
        tv_dr_specialist = getActivity().findViewById(R.id.tv_dr_special);
        tv_dr_rs = getActivity().findViewById(R.id.tv_dr_rs);
        tv_dr_rs_loc = getActivity().findViewById(R.id.tv_dr_rs_loc);
        btnNext = getActivity().findViewById(R.id.btnNext);

        ll_left = getActivity().findViewById(R.id.layout_jadwal_left);
        ll_center = getActivity().findViewById(R.id.layout_jadwal_center);
        ll_right = getActivity().findViewById(R.id.layout_jadwal_right);
        ll_pick = getActivity().findViewById(R.id.layout_jadwal_pcik);

        ll_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_left.setBackground(getResources().getDrawable(R.drawable.bg_selected));
                tv_day_left.setTextColor(getActivity().getColor(R.color.colorPrimary));
                tv_date_left.setTextColor(getActivity().getColor(R.color.colorPrimary));

                ll_center.setBackground(getResources().getDrawable(R.drawable.bg_recycler_item));
                tv_day_center.setTextColor(getActivity().getColor(R.color.colorAccent));
                tv_date_center.setTextColor(getActivity().getColor(R.color.colorAccent));

                ll_right.setBackground(getResources().getDrawable(R.drawable.bg_recycler_item));
                tv_day_right.setTextColor(getActivity().getColor(R.color.colorAccent));
                tv_date_right.setTextColor(getActivity().getColor(R.color.colorAccent));
                id=0;
                timeString="";
                btnNext.setEnabled(false);

                day = tv_day_left.getText().toString().trim();
                date = tv_date_left.getText().toString().trim();

                Calendar calendar = Calendar.getInstance();
                String[] strDays = new String[] { "", "Senin", "Selasa","Rabu", "Kamis","Jumat", "Sabtu", "Minggu" };

                if (day.equalsIgnoreCase("Hari Ini")){
                    day = strDays[calendar.get(Calendar.DAY_OF_WEEK)-1];
                }else if(day.equalsIgnoreCase("Besok")){
                    day = strDays[calendar.get(Calendar.DAY_OF_WEEK)];
                }

                for (int i = layout_morning.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_morning.removeView(layout_morning.getChildAt(i));
                    }

                    if (i == 1){
                        layout_morning.getChildAt(i).setVisibility(View.GONE);
                        layout_morning.setPadding(0,0,0,0);
                        header_morning.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        morningExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_morning.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_morning.setLayoutParams(params);
                    }
                }
                for (int i = layout_noon.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_noon.removeView(layout_noon.getChildAt(i));
                    }

                    if (i == 1){
                        layout_noon.getChildAt(i).setVisibility(View.GONE);
                        layout_noon.setPadding(0,0,0,0);
                        header_noon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        noonExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_noon.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_noon.setLayoutParams(params);
                    }
                }
                for (int i = layout_afternoon.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_afternoon.removeView(layout_afternoon.getChildAt(i));
                    }

                    if (i == 1){
                        layout_afternoon.getChildAt(i).setVisibility(View.GONE);
                        layout_afternoon.setPadding(0,0,0,0);
                        header_afternoon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        afternoonExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_afternoon.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_afternoon.setLayoutParams(params);
                    }
                }
                for (int i = layout_evening.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_evening.removeView(layout_evening.getChildAt(i));
                    }

                    if (i == 1){
                        layout_evening.getChildAt(i).setVisibility(View.GONE);
                        layout_evening.setPadding(0,0,0,0);
                        header_evening.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        eveningExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_evening.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_evening.setLayoutParams(params);
                    }
                }

                List<Integer> morningTime = new ArrayList<>();
                List<Integer> noonTime = new ArrayList<>();
                List<Integer> afternoonTime = new ArrayList<>();
                List<Integer> eveningTime = new ArrayList<>();

                for (int i = 0; i < days.size(); i++) {
                    if (strDays[days.get(i)[0]].equalsIgnoreCase(day)){
                        for (int j = days.get(i)[1]; j <= days.get(i)[2]; j++) {
                            if (j>=8 && j <= 11){
                                morningTime.add(j);
                            }else if(j>=12 && j <= 14){
                                noonTime.add(j);
                            }else if (j>=15 && j <= 17){
                                afternoonTime.add(j);
                            }else if(j>=18 && j <= 20){
                                eveningTime.add(j);
                            }
                        }
                    }
                }

                if ( eveningTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_evening.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_evening.setLayoutParams(params);
                }else if (afternoonTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_afternoon.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_afternoon.setLayoutParams(params);
                }else if ( noonTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_noon.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_noon.setLayoutParams(params);
                }else if (morningTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_morning.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_morning.setLayoutParams(params);
                }

                if ( morningTime.size() != 0){
                    layout_morning.setVisibility(View.VISIBLE);
                    if (layout_morning.getChildCount() != 3){
                        if (morningTime.size() < 4){
                            layout_morning.addView(createLayout(morningTime));
                        }else{
                            List<Integer> batch1 = morningTime.subList(0,3);
                            List<Integer> batch2 = morningTime.subList(3,4);
                            layout_morning.addView(createLayout(batch1));
                            layout_morning.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_morning.setVisibility(View.GONE);
                }

                if(noonTime.size() != 0){
                    layout_noon.setVisibility(View.VISIBLE);
                    if (layout_noon.getChildCount() != 3){
                        if (noonTime.size() < 4){
                            layout_noon.addView(createLayout(noonTime));
                        }else{
                            List<Integer> batch1 = noonTime.subList(0,3);
                            List<Integer> batch2 = noonTime.subList(3,4);
                            layout_noon.addView(createLayout(batch1));
                            layout_noon.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_noon.setVisibility(View.GONE);
                }

                if (afternoonTime.size() != 0){
                    layout_afternoon.setVisibility(View.VISIBLE);
                    if (layout_afternoon.getChildCount() != 3){
                        if (afternoonTime.size() < 4){
                            layout_afternoon.addView(createLayout(afternoonTime));
                        }else{
                            List<Integer> batch1 = afternoonTime.subList(0,3);
                            List<Integer> batch2 = afternoonTime.subList(3,4);
                            layout_afternoon.addView(createLayout(batch1));
                            layout_afternoon.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_afternoon.setVisibility(View.GONE);
                }

                if (eveningTime.size() != 0){
                    layout_evening.setVisibility(View.VISIBLE);
                    if (layout_evening.getChildCount() != 3){
                        if (eveningTime.size() < 4){
                            layout_evening.addView(createLayout(eveningTime));
                        }else{
                            List<Integer> batch1 = eveningTime.subList(0,3);
                            List<Integer> batch2 = eveningTime.subList(3,4);
                            layout_evening.addView(createLayout(batch1));
                            layout_evening.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_evening.setVisibility(View.GONE);
                }
            }
        });
        ll_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_center.setBackground(getResources().getDrawable(R.drawable.bg_selected));
                tv_day_center.setTextColor(getActivity().getColor(R.color.colorPrimary));
                tv_date_center.setTextColor(getActivity().getColor(R.color.colorPrimary));

                ll_left.setBackground(getResources().getDrawable(R.drawable.bg_recycler_item));
                tv_day_left.setTextColor(getActivity().getColor(R.color.colorAccent));
                tv_date_left.setTextColor(getActivity().getColor(R.color.colorAccent));

                ll_right.setBackground(getResources().getDrawable(R.drawable.bg_recycler_item));
                tv_day_right.setTextColor(getActivity().getColor(R.color.colorAccent));
                tv_date_right.setTextColor(getActivity().getColor(R.color.colorAccent));
                id=0;
                timeString="";
                btnNext.setEnabled(false);

                for (int i = layout_morning.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_morning.removeView(layout_morning.getChildAt(i));
                    }

                    if (i == 1){
                        layout_morning.getChildAt(i).setVisibility(View.GONE);
                        layout_morning.setPadding(0,0,0,0);
                        header_morning.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        morningExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_morning.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_morning.setLayoutParams(params);
                    }
                }
                for (int i = layout_noon.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_noon.removeView(layout_noon.getChildAt(i));
                    }

                    if (i == 1){
                        layout_noon.getChildAt(i).setVisibility(View.GONE);
                        layout_noon.setPadding(0,0,0,0);
                        header_noon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        noonExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_noon.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_noon.setLayoutParams(params);
                    }
                }
                for (int i = layout_afternoon.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_afternoon.removeView(layout_afternoon.getChildAt(i));
                    }

                    if (i == 1){
                        layout_afternoon.getChildAt(i).setVisibility(View.GONE);
                        layout_afternoon.setPadding(0,0,0,0);
                        header_afternoon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        afternoonExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_afternoon.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_afternoon.setLayoutParams(params);
                    }
                }
                for (int i = layout_evening.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_evening.removeView(layout_evening.getChildAt(i));
                    }

                    if (i == 1){
                        layout_evening.getChildAt(i).setVisibility(View.GONE);
                        layout_evening.setPadding(0,0,0,0);
                        header_evening.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        eveningExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_evening.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_evening.setLayoutParams(params);
                    }
                }

                Log.e("CHECKLAYOUT",layout_morning.getChildCount()+"");

                day = tv_day_center.getText().toString().trim();
                date = tv_date_center.getText().toString().trim();
                Calendar calendar = Calendar.getInstance();
                String[] strDays = new String[] { "", "Senin", "Selasa","Rabu", "Kamis","Jumat", "Sabtu", "Minggu" };

                if(day.equalsIgnoreCase("Besok")){
                    day = strDays[calendar.get(Calendar.DAY_OF_WEEK)];
                }

                List<Integer> morningTime = new ArrayList<>();
                List<Integer> noonTime = new ArrayList<>();
                List<Integer> afternoonTime = new ArrayList<>();
                List<Integer> eveningTime = new ArrayList<>();
                for (int i = 0; i < days.size(); i++) {
                    if (strDays[days.get(i)[0]].equalsIgnoreCase(day)){
                        morningTime.clear();
                        noonTime.clear();
                        afternoonTime.clear();
                        eveningTime.clear();
                        for (int j = days.get(i)[1]; j <= days.get(i)[2]; j++) {
                            if (j>=8 && j <= 11){
                                morningTime.add(j);
                            }else if(j>=12 && j <= 14){
                                noonTime.add(j);
                            }else if (j>=15 && j <= 17){
                                afternoonTime.add(j);
                            }else if(j>=18 && j <= 20){
                                eveningTime.add(j);
                            }
                        }
                    }
                }

                if ( eveningTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_evening.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_evening.setLayoutParams(params);
                }else if (afternoonTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_afternoon.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_afternoon.setLayoutParams(params);
                }else if ( noonTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_noon.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_noon.setLayoutParams(params);
                }else if (morningTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_morning.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_morning.setLayoutParams(params);
                }

                if ( morningTime.size() != 0){
                    layout_morning.setVisibility(View.VISIBLE);
                    if (layout_morning.getChildCount() != 3){
                        if (morningTime.size() < 4){
                            layout_morning.addView(createLayout(morningTime));
                        }else{
                            List<Integer> batch1 = morningTime.subList(0,3);
                            List<Integer> batch2 = morningTime.subList(3,4);
                            layout_morning.addView(createLayout(batch1));
                            layout_morning.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_morning.setVisibility(View.GONE);
                }

                if(noonTime.size() != 0){
                    layout_noon.setVisibility(View.VISIBLE);
                    if (layout_noon.getChildCount() != 3){
                        if (noonTime.size() < 4){
                            layout_noon.addView(createLayout(noonTime));
                        }else{
                            List<Integer> batch1 = noonTime.subList(0,3);
                            List<Integer> batch2 = noonTime.subList(3,4);
                            layout_noon.addView(createLayout(batch1));
                            layout_noon.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_noon.setVisibility(View.GONE);
                }

                if (afternoonTime.size() != 0){
                    layout_afternoon.setVisibility(View.VISIBLE);
                    if (layout_afternoon.getChildCount() != 3){
                        if (afternoonTime.size() < 4){
                            layout_afternoon.addView(createLayout(afternoonTime));
                        }else{
                            List<Integer> batch1 = afternoonTime.subList(0,3);
                            List<Integer> batch2 = afternoonTime.subList(3,4);
                            layout_afternoon.addView(createLayout(batch1));
                            layout_afternoon.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_afternoon.setVisibility(View.GONE);
                }

                if (eveningTime.size() != 0){
                    layout_evening.setVisibility(View.VISIBLE);
                    if (layout_evening.getChildCount() != 3){
                        if (eveningTime.size() < 4){
                            layout_evening.addView(createLayout(eveningTime));
                        }else{
                            List<Integer> batch1 = eveningTime.subList(0,3);
                            List<Integer> batch2 = eveningTime.subList(3,4);
                            layout_evening.addView(createLayout(batch1));
                            layout_evening.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_evening.setVisibility(View.GONE);
                }
            }
        });
        ll_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_right.setBackground(getResources().getDrawable(R.drawable.bg_selected));
                tv_day_right.setTextColor(getActivity().getColor(R.color.colorPrimary));
                tv_date_right.setTextColor(getActivity().getColor(R.color.colorPrimary));

                ll_left.setBackground(getResources().getDrawable(R.drawable.bg_recycler_item));
                tv_day_left.setTextColor(getActivity().getColor(R.color.colorAccent));
                tv_date_left.setTextColor(getActivity().getColor(R.color.colorAccent));

                ll_center.setBackground(getResources().getDrawable(R.drawable.bg_recycler_item));
                tv_day_center.setTextColor(getActivity().getColor(R.color.colorAccent));
                tv_date_center.setTextColor(getActivity().getColor(R.color.colorAccent));
                id=0;
                timeString="";
                btnNext.setEnabled(false);

                day = tv_day_right.getText().toString().trim();
                date = tv_date_right.getText().toString().trim();

                Calendar calendar = Calendar.getInstance();
                String[] strDays = new String[] { "", "Senin", "Selasa","Rabu", "Kamis","Jumat", "Sabtu", "Minggu" };

                if (day.equalsIgnoreCase("Hari Ini")){
                    day = strDays[calendar.get(Calendar.DAY_OF_WEEK)-1];
                }else if(day.equalsIgnoreCase("Besok")){
                    day = strDays[calendar.get(Calendar.DAY_OF_WEEK)];
                }

                for (int i = layout_morning.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_morning.removeView(layout_morning.getChildAt(i));
                    }

                    if (i == 1){
                        layout_morning.getChildAt(i).setVisibility(View.GONE);
                        layout_morning.setPadding(0,0,0,0);
                        header_morning.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        morningExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_morning.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_morning.setLayoutParams(params);
                    }
                }
                for (int i = layout_noon.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_noon.removeView(layout_noon.getChildAt(i));
                    }

                    if (i == 1){
                        layout_noon.getChildAt(i).setVisibility(View.GONE);
                        layout_noon.setPadding(0,0,0,0);
                        header_noon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        noonExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_noon.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_noon.setLayoutParams(params);
                    }
                }
                for (int i = layout_afternoon.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_afternoon.removeView(layout_afternoon.getChildAt(i));
                    }

                    if (i == 1){
                        layout_afternoon.getChildAt(i).setVisibility(View.GONE);
                        layout_afternoon.setPadding(0,0,0,0);
                        header_afternoon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        afternoonExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_afternoon.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_afternoon.setLayoutParams(params);
                    }
                }
                for (int i = layout_evening.getChildCount(); i > 0; i--) {
                    if (i > 1){
                        layout_evening.removeView(layout_evening.getChildAt(i));
                    }

                    if (i == 1){
                        layout_evening.getChildAt(i).setVisibility(View.GONE);
                        layout_evening.setPadding(0,0,0,0);
                        header_evening.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        eveningExpanded=false;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_evening.getLayoutParams();
                        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                        layout_evening.setLayoutParams(params);
                    }
                }

                List<Integer> morningTime = new ArrayList<>();
                List<Integer> noonTime = new ArrayList<>();
                List<Integer> afternoonTime = new ArrayList<>();
                List<Integer> eveningTime = new ArrayList<>();

                for (int i = 0; i < days.size(); i++) {
                    if (strDays[days.get(i)[0]].equalsIgnoreCase(day)){
                        for (int j = days.get(i)[1]; j <= days.get(i)[2]; j++) {
                            if (j>=8 && j <= 11){
                                morningTime.add(j);
                            }else if(j>=12 && j <= 14){
                                noonTime.add(j);
                            }else if (j>=15 && j <= 17){
                                afternoonTime.add(j);
                            }else if(j>=18 && j <= 20){
                                eveningTime.add(j);
                            }
                        }
                    }
                }

                if ( eveningTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_evening.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_evening.setLayoutParams(params);
                }else if (afternoonTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_afternoon.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_afternoon.setLayoutParams(params);
                }else if ( noonTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_noon.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_noon.setLayoutParams(params);
                }else if (morningTime.size() != 0){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_morning.getLayoutParams();
                    params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16),(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                    layout_morning.setLayoutParams(params);
                }

                if ( morningTime.size() != 0){
                    layout_morning.setVisibility(View.VISIBLE);
                    if (layout_morning.getChildCount() != 3){
                        if (morningTime.size() < 4){
                            layout_morning.addView(createLayout(morningTime));
                        }else{
                            List<Integer> batch1 = morningTime.subList(0,3);
                            List<Integer> batch2 = morningTime.subList(3,4);
                            layout_morning.addView(createLayout(batch1));
                            layout_morning.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_morning.setVisibility(View.GONE);
                }

                if(noonTime.size() != 0){
                    layout_noon.setVisibility(View.VISIBLE);
                    if (layout_noon.getChildCount() != 3){
                        if (noonTime.size() < 4){
                            layout_noon.addView(createLayout(noonTime));
                        }else{
                            List<Integer> batch1 = noonTime.subList(0,3);
                            List<Integer> batch2 = noonTime.subList(3,4);
                            layout_noon.addView(createLayout(batch1));
                            layout_noon.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_noon.setVisibility(View.GONE);
                }

                if (afternoonTime.size() != 0){
                    layout_afternoon.setVisibility(View.VISIBLE);
                    if (layout_afternoon.getChildCount() != 3){
                        if (afternoonTime.size() < 4){
                            layout_afternoon.addView(createLayout(afternoonTime));
                        }else{
                            List<Integer> batch1 = afternoonTime.subList(0,3);
                            List<Integer> batch2 = afternoonTime.subList(3,4);
                            layout_afternoon.addView(createLayout(batch1));
                            layout_afternoon.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_afternoon.setVisibility(View.GONE);
                }

                if (eveningTime.size() != 0){
                    layout_evening.setVisibility(View.VISIBLE);
                    if (layout_evening.getChildCount() != 3){
                        if (eveningTime.size() < 4){
                            layout_evening.addView(createLayout(eveningTime));
                        }else{
                            List<Integer> batch1 = eveningTime.subList(0,3);
                            List<Integer> batch2 = eveningTime.subList(3,4);
                            layout_evening.addView(createLayout(batch1));
                            layout_evening.addView(createLayout(batch2));
                        }
                    }
                }else{
                    layout_evening.setVisibility(View.GONE);
                }
            }
        });

        tv_day_left = getActivity().findViewById(R.id.tv_jadwal_day_left);
        tv_date_left = getActivity().findViewById(R.id.tv_jadwal_date_left);
        tv_day_center = getActivity().findViewById(R.id.tv_jadwal_day_center);
        tv_date_center =getActivity().findViewById(R.id.tv_jadwal_date_center);
        tv_day_right = getActivity().findViewById(R.id.tv_jadwal_day_right);
        tv_date_right = getActivity().findViewById(R.id.tv_jadwal_date_right);

        layout_morning = getActivity().findViewById(R.id.layout_Morning);
        header_morning = getActivity().findViewById(R.id.header_Morning);
        layout_noon = getActivity().findViewById(R.id.layout_Noon);
        header_noon = getActivity().findViewById(R.id.header_Noon);
        layout_afternoon = getActivity().findViewById(R.id.layout_Afternoon);
        header_afternoon = getActivity().findViewById(R.id.header_Afternoon);
        layout_evening = getActivity().findViewById(R.id.layout_Evening);
        header_evening = getActivity().findViewById(R.id.header_Evening);

        header_morning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (day.isEmpty()){
                    Toasty.error(getActivity(),"Silahkan pilih tanggal terlebih dahulu.",Toasty.LENGTH_LONG).show();
                }else{
                    if (!morningExpanded){
                        if (layout_morning.getChildCount() == 3){
                            layout_morning.getChildAt(2).setVisibility(View.VISIBLE);
                        }else if (layout_morning.getChildCount() > 3){
                            layout_morning.getChildAt(2).setVisibility(View.VISIBLE);
                            layout_morning.getChildAt(3).setVisibility(View.VISIBLE);
                        }
                        layout_morning.getChildAt(1).setVisibility(View.VISIBLE);
                        layout_morning.setPadding(0,0,0,(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                        header_morning.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_less));
                        morningExpanded = true;
                    }else{
                        if (layout_morning.getChildCount() == 3){
                            layout_morning.getChildAt(2).setVisibility(View.GONE);
                        }else if (layout_morning.getChildCount() > 3){
                            layout_morning.getChildAt(2).setVisibility(View.GONE);
                            layout_morning.getChildAt(3).setVisibility(View.GONE);
                        }
                        layout_morning.getChildAt(1).setVisibility(View.GONE);
                        layout_morning.setPadding(0,0,0,0);
                        header_morning.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        morningExpanded = false;
                    }
                }
            }
        });

        header_noon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (day.isEmpty()){
                    Toasty.error(getActivity(),"Silahkan pilih tanggal terlebih dahulu.",Toasty.LENGTH_LONG).show();
                }else{
                    if (!noonExpanded){
                        if (layout_noon.getChildCount() == 3){
                            layout_noon.getChildAt(2).setVisibility(View.VISIBLE);
                        }else{
                            layout_noon.getChildAt(2).setVisibility(View.VISIBLE);
                            layout_noon.getChildAt(3).setVisibility(View.VISIBLE);
                        }
                        layout_noon.getChildAt(1).setVisibility(View.VISIBLE);
                        layout_noon.setPadding(0,0,0,(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                        header_noon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_less));
                        noonExpanded = true;
                    }else{
                        if (layout_noon.getChildCount() == 3){
                            layout_noon.getChildAt(2).setVisibility(View.GONE);
                        }else{
                            layout_noon.getChildAt(2).setVisibility(View.GONE);
                            layout_noon.getChildAt(3).setVisibility(View.GONE);
                        }
                        layout_noon.getChildAt(1).setVisibility(View.GONE);
                        layout_noon.setPadding(0,0,0,0);
                        header_noon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        noonExpanded = false;
                    }
                }
            }
        });

        header_afternoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (day.isEmpty()){
                    Toasty.error(getActivity(),"Silahkan pilih tanggal terlebih dahulu.",Toasty.LENGTH_LONG).show();
                }else{
                    if (!afternoonExpanded){
                        if (layout_afternoon.getChildCount() == 3){
                            layout_afternoon.getChildAt(2).setVisibility(View.VISIBLE);
                        }else{
                            layout_afternoon.getChildAt(2).setVisibility(View.VISIBLE);
                            layout_afternoon.getChildAt(3).setVisibility(View.VISIBLE);
                        }
                        layout_afternoon.getChildAt(1).setVisibility(View.VISIBLE);
                        layout_afternoon.setPadding(0,0,0,(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                        header_afternoon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_less));
                        afternoonExpanded = true;
                    }else{
                        if (layout_afternoon.getChildCount() == 3){
                            layout_afternoon.getChildAt(2).setVisibility(View.GONE);
                        }else{
                            layout_afternoon.getChildAt(2).setVisibility(View.GONE);
                            layout_afternoon.getChildAt(3).setVisibility(View.GONE);
                        }
                        layout_afternoon.getChildAt(1).setVisibility(View.GONE);
                        layout_afternoon.setPadding(0,0,0,0);
                        header_afternoon.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        afternoonExpanded = false;
                    }
                }
            }
        });

        header_evening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (day.isEmpty()){
                    Toasty.error(getActivity(),"Silahkan pilih tanggal terlebih dahulu.",Toasty.LENGTH_LONG).show();
                }else{
                    if (!eveningExpanded){
                        if (layout_evening.getChildCount() == 3){
                            layout_evening.getChildAt(2).setVisibility(View.VISIBLE);
                        }else{
                            layout_evening.getChildAt(2).setVisibility(View.VISIBLE);
                            layout_evening.getChildAt(3).setVisibility(View.VISIBLE);
                        }
                        layout_evening.getChildAt(1).setVisibility(View.VISIBLE);
                        layout_evening.setPadding(0,0,0,(int) getActivity().getResources().getDimension(R.dimen.margin_16));
                        header_evening.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_less));
                        eveningExpanded = true;
                    }else{
                        if (layout_evening.getChildCount() == 3){
                            layout_evening.getChildAt(2).setVisibility(View.GONE);
                        }else{
                            layout_evening.getChildAt(2).setVisibility(View.GONE);
                            layout_evening.getChildAt(3).setVisibility(View.GONE);
                        }
                        layout_evening.getChildAt(1).setVisibility(View.GONE);
                        layout_evening.setPadding(0,0,0,0);
                        header_evening.getChildAt(2).setBackground(getActivity().getDrawable(R.drawable.ic_expand_more));
                        eveningExpanded = false;
                    }
                }
            }
        });
    }

    private View createLayout(List<Integer> times){

        LinearLayout parent = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_32),0, (int) getActivity().getResources().getDimension(R.dimen.margin_32),0);
        parent.setLayoutParams(params);
        parent.setOrientation(LinearLayout.HORIZONTAL);
        parent.setWeightSum(12);
        params.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_32),(int) getActivity().getResources().getDimension(R.dimen.margin_16), (int) getActivity().getResources().getDimension(R.dimen.margin_32),0);

        if (times.size() == 3){
            for (int i = 0; i < 3 ; i++) {
                DateTimeFormatter minuteFormat = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime localTime = LocalTime.of(times.get(i),00);

                TextView time = new TextView(getActivity());
                LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT,4);
                timeParams.gravity = Gravity.CENTER;
                time.setLayoutParams(timeParams);
                time.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                time.setTextColor(getActivity().getColor(R.color.textColorGray));
                Typeface typeface = ResourcesCompat.getFont(getActivity(),R.font.roboto_medium);
                time.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                time.setTypeface(typeface);
                time.setGravity(Gravity.CENTER);
                time.setPadding((int) getActivity().getResources().getDimension(R.dimen.margin_6),(int) getActivity().getResources().getDimension(R.dimen.margin_6),(int) getActivity().getResources().getDimension(R.dimen.margin_6),(int)getActivity().getResources().getDimension(R.dimen.margin_6));
                time.setText(localTime.format(minuteFormat));
                if (i == 1){
                    timeParams.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),0,(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                }
                int setId = time.generateViewId();
                time.setId(setId);
                time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (id != 0){
                            TextView textView = getActivity().findViewById(id);
                            textView.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        }
                        id = time.getId();
                        time.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                        time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
                        time.setCompoundDrawablePadding(- (int)(getActivity().getResources().getDimension(R.dimen.margin_8)+4));
                        timeString = time.getText().toString().trim();
                        if (day.isEmpty() && timeString.isEmpty()){
                            btnNext.setEnabled(false);
                        }else{
                            btnNext.setEnabled(true);
                        }
                    }
                });

                parent.addView(time);
                parent.setVisibility(View.GONE);
            }
        }else if(times.size() == 2){
            for (int i = 0; i < 3 ; i++) {
                DateTimeFormatter minuteFormat = DateTimeFormatter.ofPattern("HH:mm");

                TextView time = new TextView(getActivity());
                LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT,4);
                time.setLayoutParams(timeParams);
                time.setTextColor(getActivity().getColor(R.color.textColorGray));
                Typeface typeface = ResourcesCompat.getFont(getActivity(),R.font.roboto_medium);
                time.setTypeface(typeface);
                time.setGravity(Gravity.CENTER);
                time.setPadding((int) getActivity().getResources().getDimension(R.dimen.margin_6),(int) getActivity().getResources().getDimension(R.dimen.margin_6),(int) getActivity().getResources().getDimension(R.dimen.margin_6),(int)getActivity().getResources().getDimension(R.dimen.margin_6));
                if (i == 1){
                    timeParams.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),0,(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                }

                if(i != 2 ) {
                    LocalTime localTime = LocalTime.of(times.get(i),00);
                    time.setText(localTime.format(minuteFormat));
                    time.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                    int setId = time.generateViewId();
                    time.setId(setId);
                    time.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (id != 0){
                                TextView textView = getActivity().findViewById(id);
                                textView.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            }
                            id = time.getId();
                            time.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
                            time.setCompoundDrawablePadding(- (int)(getActivity().getResources().getDimension(R.dimen.margin_8)+4));
                            timeString = time.getText().toString().trim();
                            if (day.isEmpty() && timeString.isEmpty()){
                                btnNext.setEnabled(false);
                            }else{
                                btnNext.setEnabled(true);
                            }
                        }
                    });
                }
                parent.addView(time);
                parent.setVisibility(View.GONE);
            }
        }else{
            DateTimeFormatter minuteFormat = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime localTime = LocalTime.of(times.get(0),00);

            for (int i = 0; i < 3; i++) {
                TextView time = new TextView(getActivity());
                LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT,4);
                time.setLayoutParams(timeParams);
                time.setTextColor(getActivity().getColor(R.color.textColorGray));
                Typeface typeface = ResourcesCompat.getFont(getActivity(),R.font.roboto_medium);
                time.setTypeface(typeface);
                time.setGravity(Gravity.CENTER);
                time.setPadding((int) getActivity().getResources().getDimension(R.dimen.margin_6),(int) getActivity().getResources().getDimension(R.dimen.margin_6),(int) getActivity().getResources().getDimension(R.dimen.margin_6),(int)getActivity().getResources().getDimension(R.dimen.margin_6));
                if(i == 0 ) {
                    time.setText(localTime.format(minuteFormat));
                    time.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                    int setId = time.generateViewId();
                    time.setId(setId);
                    time.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (id != 0){
                                TextView textView = getActivity().findViewById(id);
                                textView.setBackground(getActivity().getDrawable(R.drawable.bg_default));
                                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            }
                            id = time.getId();
                            time.setBackground(getActivity().getDrawable(R.drawable.bg_selected));
                            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
                            time.setCompoundDrawablePadding(- (int)(getActivity().getResources().getDimension(R.dimen.margin_8)+4));
                            timeString = time.getText().toString().trim();
                            if (day.isEmpty() && timeString.isEmpty()){
                                btnNext.setEnabled(false);
                            }else{
                                btnNext.setEnabled(true);
                            }
                        }
                    });
                }
                if (i == 1) timeParams.setMargins((int) getActivity().getResources().getDimension(R.dimen.margin_16),0,(int) getActivity().getResources().getDimension(R.dimen.margin_16),0);
                parent.addView(time);
                parent.setVisibility(View.GONE);
            }
        }

        return parent;
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

}
