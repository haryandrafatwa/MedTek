package com.example.medtek.ui.pasien.others;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.codesgood.views.JustifiedTextView;
import com.example.medtek.R;
import com.example.medtek.network.RetrofitClient;
import com.example.medtek.ui.activity.WelcomePageActivity;
import com.example.medtek.ui.dokter.others.JadwalSayaFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.example.medtek.network.RetrofitClient.BASE_SOCKET_URL;
import static com.example.medtek.network.RetrofitClient.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.deleteAllData;
import static com.example.medtek.utils.PropertyUtil.getData;

public class OthersFragment extends Fragment {

    private TextView tv_nama, tv_email,tv_app_version;
    private Button btn_logout;
    private ImageButton ib_edit,ib_cek_verified, ib_wallet, ib_jadwal, ib_riwayat;
    private LinearLayout layout_nilai_kami,layout_invite_friends, ll_wallet, ll_jadwal, ll_riwayat;
    private ChipNavigationBar bottomNavigationView;
    private CircleImageView civ_user;

    private String access, refresh;
    private boolean isVerified;
    private int role_id;

    private RelativeLayout rl_profile;
    private ProgressBar pb_profile;

    private Socket socket;
    private final String SERVER_URL = "http://192.168.137.1:6001";
    private final String CHANNEL_MESSAGES = "messages";
    private final String EVENT_MESSAGE_CREATED = "MessageCreated";
    private final String TAG = "msg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_others, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        loadData(getActivity());
        tv_app_version.setText("v "+getAppVersion());

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<ResponseBody> call = RetrofitClient.getInstance().getApi().logout("Bearer "+access);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                        clearData(getActivity());
                        Intent intent = new Intent(getActivity(), WelcomePageActivity.class);
                        deleteAllData();
                        startActivity(intent);
                        getActivity().finish();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }
        });

        Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
        callUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        pb_profile.setVisibility(View.GONE);
                        rl_profile.setVisibility(View.VISIBLE);
                        String s = response.body().string();
                        JSONObject obj = new JSONObject(s);

                        tv_nama.setText(obj.getString("name"));
                        tv_email.setText(obj.getString("email"));

                        if (obj.isNull("email_verified_at")){
                            ib_cek_verified.setBackground(getActivity().getDrawable(R.drawable.ic_unverified));
                            isVerified = false;
                        }else{
                            ib_cek_verified.setBackground(getActivity().getDrawable(R.drawable.ic_verified));
                            isVerified = true;
                        }

                        role_id = obj.getInt("role_id");

                        if (obj.getInt("role_id") != 1){
                            ll_wallet.setVisibility(View.GONE);
                            ll_jadwal.setVisibility(View.VISIBLE);
                        }

                        ll_wallet.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isVerified){
                                    WalletFragment walletFragment = new WalletFragment();
                                    setFragment(walletFragment,"FragmentWallet");
                                }else{
                                    Toasty.info(getActivity(),getString(R.string.silahkanlakukanverifikasi),Toasty.LENGTH_LONG).show();
                                }
//                                Toasty.info(getActivity(),"Under Maintenance!",Toasty.LENGTH_LONG).show();
                            }
                        });

                        ll_jadwal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JadwalSayaFragment jadwalSayaFragment = new JadwalSayaFragment();
                                setFragment(jadwalSayaFragment,"JadwalSayaFragment");
                            }
                        });

                        ll_riwayat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RiwayatFragment riwayatFragment = new RiwayatFragment();
                                setFragment(riwayatFragment,"RiwayatFragment");
                            }
                        });

                        String path="";
                        JSONArray jsonArrayImage = obj.getJSONArray("image");
                        if (jsonArrayImage.length() !=0){
                            for (int j = 0; j < jsonArrayImage.length(); j++) {
                                JSONObject imageObj = jsonArrayImage.getJSONObject(j);
                                if (imageObj.getInt("type_id") == 1) {
                                    path = BASE_URL + imageObj.getString("path");
                                    Picasso.get().load(path).into(civ_user);
                                    break;
                                }else{
                                    if (obj.getInt("role_id") == 1){
                                        civ_user.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pasien));
                                    }else{
                                        civ_user.setImageDrawable(getActivity().getDrawable(R.drawable.ic_dokter));
                                    }
                                }
                            }
                        }else{
                            if (obj.getInt("role_id") == 1){
                                civ_user.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pasien));
                            }else{
                                civ_user.setImageDrawable(getActivity().getDrawable(R.drawable.ic_dokter));
                            }
                        }

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toasty.info(getActivity(), t.getMessage());
            }
        });

        layout_nilai_kami.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.linkdokter.halodoc.android&hl=in"));
                startActivity(intent);
            }
        });

        layout_invite_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InviteFriendsFragment inviteFriendsFragment = new InviteFriendsFragment();
                setFragment(inviteFriendsFragment,"FragmentAddFriends");
            }
        });

        ib_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (role_id == 1){
                    EditProfileFragment editProfileFragment = new EditProfileFragment();
                    setFragment(editProfileFragment,"FragmentEditProfile");
                }else{
                    com.example.medtek.ui.dokter.others.EditProfileFragment editProfileFragment = new com.example.medtek.ui.dokter.others.EditProfileFragment();
                    setFragment(editProfileFragment,"FragmentEditProfile");
                }
            }
        });

        ib_cek_verified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(),R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.bottom_sheet_check_verified,(RelativeLayout)getActivity().findViewById(R.id.bottomSheetContainer));

                if (!isVerified){
                    bottomSheetView.findViewById(R.id.verify_now).setVisibility(View.VISIBLE);
                    TextView isverify = bottomSheetView.findViewById(R.id.is_verify);
                    JustifiedTextView content = bottomSheetView.findViewById(R.id.content);
                    isverify.setText(R.string.belumterverifikasi);
                    isverify.setBackground(getResources().getDrawable(R.drawable.bg_button_red));
                    isverify.setBackgroundTintList(getActivity().getColorStateList(R.color.textColorLightGray));
                    isverify.setTextColor(getResources().getColor(R.color.textColorGray));
                    content.setText(R.string.emailkamu);
                    bottomSheetView.findViewById(R.id.verify_now).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                startActivity(intent);
                            } catch (android.content.ActivityNotFoundException anfe) {
                                anfe.getMessage();
                            }
                        }
                    });
                }else{
                    bottomSheetView.findViewById(R.id.verify_now).setVisibility(View.GONE);
                    TextView isverify = bottomSheetView.findViewById(R.id.is_verify);
                    JustifiedTextView content = bottomSheetView.findViewById(R.id.content);
                    isverify.setText(R.string.terverifikasi);
                    isverify.setBackground(getResources().getDrawable(R.drawable.bg_button_red));
                    isverify.setBackgroundTintList(getActivity().getColorStateList(R.color.textColorLightRed));
                    isverify.setTextColor(getResources().getColor(R.color.textColorDarkRed));
                    content.setText(R.string.emailanda);
                }
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

        ib_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_wallet.performClick();
            }
        });

        ib_jadwal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_jadwal.performClick();
            }
        });

        ib_riwayat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_riwayat.performClick();
            }
        });

    }

    private void initialize(){
        civ_user = getActivity().findViewById(R.id.civ_user);
        tv_nama = getActivity().findViewById(R.id.tv_nama_user);
        tv_email = getActivity().findViewById(R.id.tv_email_user);
        tv_app_version = getActivity().findViewById(R.id.app_version);
        btn_logout = getActivity().findViewById(R.id.btn_logout);

        ib_edit = getActivity().findViewById(R.id.ib_edit);
        ib_cek_verified = getActivity().findViewById(R.id.btn_cek_verified);
        ib_wallet = getActivity().findViewById(R.id.ib_wallet);
        ib_jadwal = getActivity().findViewById(R.id.ib_jadwal);
        ib_riwayat = getActivity().findViewById(R.id.ib_riwayat);

        bottomNavigationView = getActivity().findViewById(R.id.bottomBar);
        bottomNavigationView.setVisibility(View.VISIBLE);
        setStatusBar();

        layout_nilai_kami = getActivity().findViewById(R.id.layout_nilai_kami);
        layout_invite_friends = getActivity().findViewById(R.id.layout_invite_friends);

        rl_profile = getActivity().findViewById(R.id.rl_profile);
        pb_profile = getActivity().findViewById(R.id.pb_profile);
        ll_wallet = getActivity().findViewById(R.id.layout_wallet);
        ll_jadwal = getActivity().findViewById(R.id.layout_jadwal);
        ll_riwayat = getActivity().findViewById(R.id.layout_riwayat);

        startEcho();

    }

    private static void clearData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void startEcho() {
        Log.e("MedTek", "ECHO START...");

        try {
            socket = IO.socket(BASE_SOCKET_URL);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("SOCKETSOCKETAN", "Connected!");
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("SOCKETSOCKETAN", "Error!");
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("SOCKETSOCKETAN", "Connect Error!");
                }
            });
            socket.connect();

        } catch (URISyntaxException e) {
            Log.e("MedTek", "ECHO ERROR");
            e.printStackTrace();
        }
    }

    private String getAppVersion(){
        String version="";
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    private void setFragment(Fragment fragment, String TAG) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameFragment,fragment,TAG).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    private void setStatusBar(){
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    public void loadData(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        this.access = sharedPreferences.getString("token", "");
//        this.refresh = sharedPreferences.getString("refresh_token", "");
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

}
