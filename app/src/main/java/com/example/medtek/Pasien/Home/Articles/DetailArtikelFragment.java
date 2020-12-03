package com.example.medtek.Pasien.Home.Articles;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.R;
import com.example.medtek.Utils.RoundedCornersTransformation;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailArtikelFragment extends Fragment {

    private ChipNavigationBar bottomBar;
    private Toolbar toolbar;
    private ImageView iv_artikel;
    private CircleImageView civ_author;
    private TextView tv_author_name, tv_judul_artikel, tv_date_artikel, tv_content_artikel;
    private ImageButton ib_share;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artikel_detail, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        Bundle bundle = getArguments();
        int id = bundle.getInt("id_artikel");
        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().getArtikelId(id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String s = response.body().string();
                    JSONObject object = new JSONObject(s);
                    JSONObject jsonObject = object.getJSONObject("data");
                    String path;
                    Log.e("ArtikelDetailId",s);
                    if (jsonObject.getString("image")!="null"){final int radius = 0;
                        final int margin = 0;
                        final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                        path = "http://192.168.137.1:8000"+jsonObject.getJSONObject("image").getString("path");
                        Picasso.get().load(path).fit().centerCrop().transform(transformation).into(iv_artikel);
                    }else{
                        Drawable drawable = getActivity().getDrawable(R.drawable.bg_default_artikel);
                        drawable = DrawableCompat.wrap(drawable);
                        DrawableCompat.setTint(drawable, Color.parseColor("#80878787"));
                        iv_artikel.setBackground(drawable);
                    }
                    String name = jsonObject.getJSONObject("author").getString("name");
                    tv_author_name.setText(name);
                    tv_judul_artikel.setText(jsonObject.getString("judul"));
                    tv_content_artikel.setText(jsonObject.getString("isi"));
                    JSONArray jsonArray = new JSONArray(jsonObject.getJSONObject("author").getString("image"));
                    String userImagePath;
                    if (jsonArray.length() == 0){
                        userImagePath = "/storage/Pasien.png";
                    }else{
                        userImagePath = jsonArray.getJSONObject(0).getString("path");
                    }
                    Picasso.get().load("http://192.168.137.1:8000"+userImagePath).into(civ_author);
                    Date date;
                    Locale locale = new Locale("in", "ID");
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale);
                    try {
                        date = format.parse(jsonObject.getString("created_at"));
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        String month_name = new SimpleDateFormat("MMMM", locale).format(calendar.getTime());
                        tv_date_artikel.setText(calendar.get(Calendar.DATE)+" "+month_name+" "+calendar.get(Calendar.YEAR)+" • "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }




                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                call.clone().enqueue(this);
            }
        });
    }

    private void initialize(){
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setVisibility(View.GONE);
        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();
        setStatusBar();

        iv_artikel = getActivity().findViewById(R.id.iv_artikel);
        civ_author = getActivity().findViewById(R.id.civ_author);
        tv_author_name = getActivity().findViewById(R.id.tv_author_name);
        tv_date_artikel = getActivity().findViewById(R.id.tv_date_artikel);
        tv_judul_artikel = getActivity().findViewById(R.id.tv_judul_artikel);
        tv_content_artikel = getActivity().findViewById(R.id.tv_content_artikel);
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

    private void setStatusBar(){
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getActivity().getWindow().setStatusBarColor(Color.WHITE);;
    }

}
