package com.example.medtek.Pasien.Others;

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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;

import com.codesgood.views.JustifiedTextView;
import com.example.medtek.API.RetrofitClient;
import com.example.medtek.R;
import com.example.medtek.WelcomePageActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class OthersFragment extends Fragment {

    private TextView tv_nama, tv_email,tv_app_version;
    private Button btn_logout;
    private ImageButton ib_edit,ib_cek_verified, ib_wallet;
    private LinearLayout layout_nilai_kami,layout_invite_friends, ll_wallet;
    private ChipNavigationBar bottomNavigationView;
    private CircleImageView civ_user;

    private String access, refresh;
    private boolean isVerified;
    private Socket socket;

    private RelativeLayout rl_profile;
    private ProgressBar pb_profile;

    private String SERVER_URL = "http://192.168.1.9:6001";
    private String CHANNEL_MESSAGES = "messages";
    private String EVENT_MESSAGE_CREATED = "MessageCreated";
    private String TAG = "msg";

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
                        clearData(getActivity());
                        Intent intent = new Intent(getActivity(), WelcomePageActivity.class);
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

                        if (obj.getString("email_verified_at").equals("null")){
                            ib_cek_verified.setBackground(getActivity().getDrawable(R.drawable.ic_unverified));
                            isVerified = false;
                        }else{
                            ib_cek_verified.setBackground(getActivity().getDrawable(R.drawable.ic_verified));
                            isVerified = true;
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
                            }
                        });

                        String path = "";
                        if (new JSONArray(obj.getString("image")).length() !=0){
                            JSONArray jsonArray = new JSONArray(obj.getString("image"));
                            path = "http://192.168.1.9:8000"+jsonArray.getJSONObject(0).getString("path");
                        }else{
                            path = "http://192.168.1.9:8000/storage/bg_recycler_default.png";
                        }
                        Picasso.get().load(path).fit().centerCrop().into(civ_user);

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
                EditProfileFragment editProfileFragment = new EditProfileFragment();
                setFragment(editProfileFragment,"FragmentEditProfile");
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

//        startEcho();
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

        bottomNavigationView = getActivity().findViewById(R.id.bottomBar);
        bottomNavigationView.setVisibility(View.VISIBLE);
        setStatusBar();

        layout_nilai_kami = getActivity().findViewById(R.id.layout_nilai_kami);
        layout_invite_friends = getActivity().findViewById(R.id.layout_invite_friends);

        rl_profile = getActivity().findViewById(R.id.rl_profile);
        pb_profile = getActivity().findViewById(R.id.pb_profile);
        ll_wallet = getActivity().findViewById(R.id.layout_wallet);

        startEcho();

        /*PusherOptions options = new PusherOptions();
        options.setCluster("ap1");

        Pusher pusher = new Pusher("2014a5052483f3c31fd5",options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i("Pusher", "State changed from " + change.getPreviousState() + " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i("Pusher", "There was a problem connecting! " +
                        "\ncode: " + code +
                        "\nmessage: " + message +
                        "\nException: " + e
                );
            }
        }, ConnectionState.ALL);

        Channel channel = pusher.subscribe("test");
        channel.bind("App\\Events\\TestEvent", new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                Log.i("Pusher", "Received event with data: " + event.toString());
            }
        });*/

        /*OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://192.168.1.9:6001").build();
        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable okhttp3.Response response) {
                super.onFailure(webSocket, t, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull okhttp3.Response response) {
                super.onOpen(webSocket, response);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.info(getActivity(),"Connection Established!").show();
                    }
                });
            }
        });
        webSocket.send("lol");*/

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
            socket = IO.socket("http://192.168.1.9:6001");
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
            }).on("App\\Events\\TestEvent", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        String messageJson = new String(args[1].toString());
                        Log.e("SOCKETSOCKETAN", messageJson);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            socket.connect();

            JSONObject object = new JSONObject();
            JSONObject auth = new JSONObject();
            JSONObject headers = new JSONObject();

            try {
                object.put("channel", "test");
                object.put("name", "subscribe");

                headers.put("Authorization", "Bearer " + access);
                auth.put("headers", headers);
                object.put("auth", auth);

                Log.d("MedTekMedTek",object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            socket.emit("subscribe", object, new Ack() {
                @Override
                public void call(Object... args) {
                    Log.e("MedTek", "ECHO SUBSCRIBED"); // This event never occurs. I don't know why...
                }
            });
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
        fragmentTransaction.replace(R.id.frameFragment,fragment).addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    private void setStatusBar(){
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    public void loadData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        this.access = sharedPreferences.getString("token", "");
        this.refresh = sharedPreferences.getString("refresh_token", "");
    }

}
