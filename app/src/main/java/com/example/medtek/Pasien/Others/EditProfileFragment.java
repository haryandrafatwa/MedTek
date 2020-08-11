package com.example.medtek.Pasien.Others;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.codesgood.views.JustifiedTextView;
import com.example.medtek.Pasien.Model.UserModel;
import com.example.medtek.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    private ChipNavigationBar bottomNavigationView;
    private Toolbar toolbar;

    private CircleImageView circleImageView;
    private EditText et_nama, et_email, et_tgl, et_noHp;
    private TextView male, female,isverify;
    private Button btn_simpan;

    private UserModel userModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.changepassword:
                Toast.makeText(getActivity(), "Change Password", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        Bundle bundle = getArguments();
        if (bundle!=null){
            userModel = bundle.getParcelable("userModel");
            et_nama.setText(userModel.getName());
            et_email.setText(userModel.getEmail());
            if (userModel.getIsVerified() == 1){
                isverify.setText(R.string.terverifikasi);
                isverify.setBackground(getResources().getDrawable(R.drawable.bg_button_red));
                isverify.setBackgroundTintList(getActivity().getColorStateList(R.color.textColorLightRed));
                isverify.setTextColor(getResources().getColor(R.color.textColorDarkRed));
            }else{
                isverify.setText(R.string.belumterverifikasi);
                isverify.setBackground(getResources().getDrawable(R.drawable.bg_button_red));
                isverify.setBackgroundTintList(getActivity().getColorStateList(R.color.textColorLightGray));
                isverify.setTextColor(getResources().getColor(R.color.textColorGray));
            }
        }

    }

    private void initialize(){
        bottomNavigationView = getActivity().findViewById(R.id.bottomBar);
        bottomNavigationView.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();
        setStatusBar();

        circleImageView = getActivity().findViewById(R.id.civ_user);
        et_nama = getActivity().findViewById(R.id.et_nama);
        et_email = getActivity().findViewById(R.id.et_email);
        et_noHp = getActivity().findViewById(R.id.et_no_hp);
        et_tgl = getActivity().findViewById(R.id.et_tgl);
        male = getActivity().findViewById(R.id.tv_male);
        female = getActivity().findViewById(R.id.tv_female);
        btn_simpan = getActivity().findViewById(R.id.btn_simpan);
        isverify = getActivity().findViewById(R.id.is_verify);
    }

    private void setToolbar() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chevron_left, null);
        drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.textColorDark), PorterDuff.Mode.SRC_IN));
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
