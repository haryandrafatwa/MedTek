package com.example.medtek.Pasien;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.medtek.Dokter.LoginDokterActivity;
import com.example.medtek.Dokter.RegisterDokterActivity;
import com.example.medtek.R;

public class LoginPasienActivity extends AppCompatActivity {

    private EditText email,password;
    private TextView lupa_password, daftar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_pasien);
        initiliaze();

        daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent daftar = new Intent(LoginPasienActivity.this, RegisterPasienActivity.class);
                startActivity(daftar);
            }
        });

    }

    private void initiliaze(){

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        lupa_password = findViewById(R.id.tv_lupa_password);
        daftar = findViewById(R.id.tv_daftar);
        toolbar = findViewById(R.id.toolbar);
        setToolbar();

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
