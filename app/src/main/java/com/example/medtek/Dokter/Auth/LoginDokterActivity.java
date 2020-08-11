package com.example.medtek.Dokter.Auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.MainActivity;
import com.example.medtek.Pasien.Auth.LoginPasienActivity;
import com.example.medtek.Pasien.Auth.RegisterPasienActivity;
import com.example.medtek.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginDokterActivity extends AppCompatActivity {

    private EditText email,password;
    private TextView lupa_password, daftar, formattidakvalid;
    private Toolbar toolbar;
    private Button btn_masuk;
    private RelativeLayout btn_clone;

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private Boolean status;
    private String str_em, str_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_dokter);
        initiliaze();

        daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent daftar = new Intent(LoginDokterActivity.this, RegisterDokterActivity.class);
                startActivity(daftar);
            }
        });

        btn_masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_clone.setVisibility(View.VISIBLE);
                getValueFromET();

                if(str_em.isEmpty() || str_pw.isEmpty()){
                    Toasty.error(LoginDokterActivity.this,getResources().getString(R.string.isidatadiri),Toasty.LENGTH_LONG).show();
                    btn_clone.setVisibility(View.GONE);
                }else{
                    if(!status){
                        Toasty.error(LoginDokterActivity.this,getResources().getString(R.string.formatemail),Toasty.LENGTH_LONG).show();
                        btn_clone.setVisibility(View.GONE);
                    }else{
                        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().login(str_em,str_pw);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()){
                                    if (response.body()!=null){
                                        try {
                                            btn_clone.setVisibility(View.GONE);
                                            String s = response.body().string();
                                            JSONObject obj = new JSONObject(s);
                                            if (obj.has("message")){
                                                Log.d("MedTekMedTekMedTek",obj.getString("message"));
                                                if (obj.getString("message").equals("User not verified.")){
                                                    Toasty.error(LoginDokterActivity.this,getString(R.string.unverified)).show();
                                                }else if (obj.getString("message").equals("User not found.")){
                                                    Toasty.error(LoginDokterActivity.this,getString(R.string.emailtidakditemukan)).show();
                                                }else{
                                                    Toasty.error(LoginDokterActivity.this,getString(R.string.wrongpassword)).show();
                                                }
                                            }else {
                                                Log.d("MedTekMedTekMedTek", s);
                                                String token = obj.getString("access_token");
                                                String refresh_token = obj.getString("refresh_token");
                                                Toasty.success(LoginDokterActivity.this,getString(R.string.masukberhasil)).show();
                                                saveData(LoginDokterActivity.this,token,refresh_token);
                                                Intent intent = new Intent(LoginDokterActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            }
                                        } catch (JSONException | IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toasty.error(LoginDokterActivity.this,t.getMessage()).show();
                            }
                        });
                    }
                }
            }
        });

    }

    private void initiliaze(){

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        lupa_password = findViewById(R.id.tv_lupa_password);
        formattidakvalid = findViewById(R.id.format_tidakvalid);
        btn_masuk = findViewById(R.id.btn_masuk);
        btn_clone = findViewById(R.id.btn_masuk_clone);
        daftar = findViewById(R.id.tv_daftar);
        toolbar = findViewById(R.id.toolbar);
        setToolbar();

        int tintColorDark = ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray);
        int tintColorDrawable = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);

        Drawable drawableVisibility = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_eye);
        drawableVisibility = DrawableCompat.wrap(drawableVisibility);
        DrawableCompat.setTint(drawableVisibility.mutate(), tintColorDrawable);

        Drawable drawableLock = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_lock);
        drawableLock = DrawableCompat.wrap(drawableLock);
        DrawableCompat.setTint(drawableLock.mutate(), tintColorDrawable);

        drawableVisibility.setBounds(0, 0, drawableVisibility.getIntrinsicWidth(), drawableVisibility.getIntrinsicHeight());
        drawableLock.setBounds(0, 0, drawableLock.getIntrinsicWidth(), drawableLock.getIntrinsicHeight());

        password.setCompoundDrawables(drawableLock, null, drawableVisibility, null);

        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()-40)) {

                        int tintColorDark = ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray);
                        int tintColorDrawable = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);

                        Drawable drawableVisibility = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_eye);
                        drawableVisibility = DrawableCompat.wrap(drawableVisibility);
                        DrawableCompat.setTint(drawableVisibility.mutate(), tintColorDrawable);

                        Drawable drawableVisibilityOff = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_eye_off);
                        drawableVisibilityOff = DrawableCompat.wrap(drawableVisibilityOff);
                        DrawableCompat.setTint(drawableVisibilityOff.mutate(), tintColorDrawable);

                        Drawable drawableLock = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_lock);
                        drawableLock = DrawableCompat.wrap(drawableLock);
                        DrawableCompat.setTint(drawableLock.mutate(), tintColorDrawable);

                        drawableVisibility.setBounds(0, 0, drawableVisibility.getIntrinsicWidth(), drawableVisibility.getIntrinsicHeight());
                        drawableVisibilityOff.setBounds(0, 0, drawableVisibilityOff.getIntrinsicWidth(), drawableVisibilityOff.getIntrinsicHeight());
                        drawableLock.setBounds(0, 0, drawableLock.getIntrinsicWidth(), drawableLock.getIntrinsicHeight());

                        if (password.getTransformationMethod() == HideReturnsTransformationMethod.getInstance()) {
                            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                            DrawableCompat.setTint(drawableVisibility.mutate(), tintColorDark);
                            password.setCompoundDrawables(drawableLock, null, drawableVisibility, null);
                        } else {
                            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            password.setCompoundDrawables(drawableLock, null, drawableVisibilityOff, null);

                        }
                        return true;
                    }
                }
                return false;
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!email.getText().toString().matches(emailPattern)) {
                    formattidakvalid.setVisibility(View.VISIBLE);
                    status = false;
                } else {
                    formattidakvalid.setVisibility(View.GONE);
                    status = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public static void saveData(Context context, String access, String refresh) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", access);
        editor.putString("refresh_token", refresh);
        editor.apply();
    }

    private void getValueFromET(){
        str_em = email.getText().toString().trim();
        str_pw = password.getText().toString();
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
