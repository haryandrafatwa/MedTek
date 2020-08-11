package com.example.medtek.Pasien.Auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
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

import com.example.medtek.R;
import com.example.medtek.API.RetrofitClient;
import com.example.medtek.WelcomePageActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterPasienActivity extends AppCompatActivity {

    private EditText nama_lengkap,email,password,repassword;
    private String str_nl, str_em, str_pw, str_rp;

    private TextView masuk,formattidakvalid;
    private Toolbar toolbar;
    private Button btn_daftar;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private Boolean status;
    private RelativeLayout btn_clone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_pasien);
        initiliaze();

        masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_clone.setVisibility(View.VISIBLE);
                getValueFromET();

                if(str_nl.isEmpty() || str_em.isEmpty() || str_pw.isEmpty() || str_rp.isEmpty()){
                    Toasty.error(RegisterPasienActivity.this,getResources().getString(R.string.isidatadiri),Toasty.LENGTH_LONG).show();
                    btn_clone.setVisibility(View.GONE);
                }else{
                    if(!status){
                        Toasty.error(RegisterPasienActivity.this,getResources().getString(R.string.formatemail),Toasty.LENGTH_LONG).show();
                        btn_clone.setVisibility(View.GONE);
                    }else{
                        if (str_pw.length() < 8 || str_rp.length() < 8){
                            Toasty.error(RegisterPasienActivity.this,getResources().getString(R.string.passwordmin),Toasty.LENGTH_LONG).show();
                            btn_clone.setVisibility(View.GONE);
                        }else{
                            if (!str_pw.equals(str_rp)){
                                Toasty.error(RegisterPasienActivity.this,getResources().getString(R.string.passwordsama),Toasty.LENGTH_LONG).show();
                                btn_clone.setVisibility(View.GONE);
                            }else{
                                Call<ResponseBody> call = RetrofitClient.getInstance().getApi().register_pasien(str_nl,str_em,str_pw,str_rp);
                                call.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            btn_clone.setVisibility(View.GONE);
                                            String s = response.errorBody().string();
                                            JSONObject obj = new JSONObject(s);
                                            if (obj.has("errors")){
                                                JSONObject newObj = obj.getJSONObject("errors");
                                                if (newObj.getJSONArray("email").getString(0).equals("Email has already been taken")) {
                                                    Toasty.error(RegisterPasienActivity.this, getResources().getString(R.string.emailsudahdigunakan)).show();
                                                }
                                            }else{
                                                initializeDialogSuccess();
                                            }

                                        } catch (IOException | JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Toasty.error(RegisterPasienActivity.this,t.getMessage()).show();
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }

    private void initiliaze(){

        nama_lengkap = findViewById(R.id.et_nama_lengkap_daftar_pasien);
        email = findViewById(R.id.et_email_daftar_pasien);
        password = findViewById(R.id.et_password_daftar_pasien);
        repassword = findViewById(R.id.et_repassword_daftar_pasien);
        formattidakvalid = findViewById(R.id.format_tidakvalid);
        masuk = findViewById(R.id.tv_masuk);
        toolbar = findViewById(R.id.toolbar);
        setToolbar();

        btn_daftar = findViewById(R.id.btn_daftar_daftar_pasien);
        btn_clone = findViewById(R.id.btn_daftar_clone);

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
        repassword.setCompoundDrawables(drawableLock, null, drawableVisibility, null);

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

        repassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (repassword.getRight() - repassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()-40)) {

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

                        if (repassword.getTransformationMethod() == HideReturnsTransformationMethod.getInstance()) {
                            repassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                            DrawableCompat.setTint(drawableVisibility.mutate(), tintColorDark);
                            repassword.setCompoundDrawables(drawableLock, null, drawableVisibility, null);
                        } else {
                            repassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            repassword.setCompoundDrawables(drawableLock, null, drawableVisibilityOff, null);

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

    private void initializeDialogSuccess(){
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this,R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_daftar_pasien_berhasil,null);
        dialogView.setBackgroundColor(Color.TRANSPARENT);
        dialog.setView(dialogView);
        dialog.setCancelable(false);
        dialog.setTitle(null);

        dialog.setPositiveButton(getResources().getString(R.string.oke), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(RegisterPasienActivity.this, WelcomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    private void getValueFromET(){
        str_nl = nama_lengkap.getText().toString().trim();
        str_em = email.getText().toString().trim();
        str_pw = password.getText().toString();
        str_rp = repassword.getText().toString();
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
