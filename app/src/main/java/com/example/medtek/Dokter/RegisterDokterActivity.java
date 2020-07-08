package com.example.medtek.Dokter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.android.volley.RequestQueue;
import com.example.medtek.R;

import es.dmoral.toasty.Toasty;

public class RegisterDokterActivity extends AppCompatActivity {

    private EditText nama_lengkap,email,password,repassword;
    private String str_nl, str_em, str_pw, str_rp;
    private TextView lupa_password, masuk,formattidakvalid;
    private Toolbar toolbar;
    private Button btn_daftar;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private Boolean status;

    private static String APIUrl = "192.168.1.5/api/register";

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_dokter);
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

                getValueFromET();

                if(str_nl.isEmpty() || str_em.isEmpty() || str_pw.isEmpty() || str_rp.isEmpty()){
                    Toasty.error(RegisterDokterActivity.this,getResources().getString(R.string.isidatadiri),Toasty.LENGTH_LONG).show();
                }else{
                    if(!status){
                        Toasty.error(RegisterDokterActivity.this,getResources().getString(R.string.formatemail),Toasty.LENGTH_LONG).show();
                    }else{
                        if (str_pw.length() < 8 || str_rp.length() < 8){
                            Toasty.error(RegisterDokterActivity.this,getResources().getString(R.string.passwordmin),Toasty.LENGTH_LONG).show();
                        }else{
                            if (!str_pw.equals(str_rp)){
                                Toasty.error(RegisterDokterActivity.this,getResources().getString(R.string.passwordsama),Toasty.LENGTH_LONG).show();
                            }else{
                                Bundle bundle = new Bundle();
                                bundle.putString("nama_lengkap",str_nl);
                                bundle.putString("email",str_em);
                                bundle.putString("password",str_pw);
                                Intent verify = new Intent(RegisterDokterActivity.this,VerifyRegisterDokterActivity.class);
                                verify.putExtra("form_data",bundle);
                                startActivity(verify);
                            }
                        }
                    }
                }
            }
        });
    }

    private void initiliaze(){

        nama_lengkap = findViewById(R.id.et_nama_lengkap_daftar);
        email = findViewById(R.id.et_email_daftar);
        password = findViewById(R.id.et_password_daftar);
        repassword = findViewById(R.id.et_repassword_daftar);
        lupa_password = findViewById(R.id.tv_lupa_password);
        formattidakvalid = findViewById(R.id.format_tidakvalid);
        masuk = findViewById(R.id.tv_masuk);
        toolbar = findViewById(R.id.toolbar);
        setToolbar();

        btn_daftar = findViewById(R.id.btn_daftar);

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