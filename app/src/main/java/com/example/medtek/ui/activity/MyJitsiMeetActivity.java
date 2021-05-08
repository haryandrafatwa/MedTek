package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.medtek.App;
import com.example.medtek.network.socket.SocketUtil;
import com.facebook.react.modules.core.PermissionListener;

import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.util.Map;

import static com.example.medtek.constant.APPConstant.EVENT_RESPONSE_ON_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VIDEO_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VOICE_CALL;
import static com.example.medtek.network.socket.SocketUtil.getMessageFromObject;
import static com.example.medtek.utils.Utils.setupVideoJitsi;
import static com.example.medtek.utils.Utils.setupVoiceJitsi;

public class MyJitsiMeetActivity extends FragmentActivity implements JitsiMeetActivityInterface, JitsiMeetViewListener {
    private static final String TAG = MyJitsiMeetActivity.class.getSimpleName();

    private static final String STATE_VIDEO_STATUS = "state_video_status";
    private static final String STATE_AUDIO_STATUS = "state_audio_status";
    private static final String IS_VIDEO_CALL = "is_video_call";
    private static final String ID_JANJI = "id_janji";

    private JitsiMeetView view;
    private JitsiMeetConferenceOptions options;

    private boolean isAudioOn;
    private boolean isVideoOn;
    private boolean isVideoCall;
    private int idJanji;

    public static void navigate(Activity activity, boolean isVideoOn, boolean isAudioOn, int idJanji, boolean isVideoCall) {
        Intent intent = new Intent(activity, MyJitsiMeetActivity.class);
        intent.putExtra(STATE_VIDEO_STATUS, isVideoOn);
        intent.putExtra(STATE_AUDIO_STATUS, isAudioOn);
        intent.putExtra(ID_JANJI, idJanji);
        intent.putExtra(IS_VIDEO_CALL, isVideoCall);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    public static void navigate(Activity activity, int idJanji, boolean isVideoCall) {
        Intent intent = new Intent(activity, MyJitsiMeetActivity.class);
        intent.putExtra(ID_JANJI, idJanji);
        intent.putExtra(IS_VIDEO_CALL, isVideoCall);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JitsiMeetActivityDelegate.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isVideoCall = getIntent().getBooleanExtra(IS_VIDEO_CALL, true);
        idJanji = getIntent().getIntExtra(ID_JANJI, 0);

        SocketUtil.getInstance().setChannelVideoChat(idJanji);

        listenResponse();

        if (isVideoCall) {
            isVideoOn = getIntent().getBooleanExtra(STATE_VIDEO_STATUS, true);
            isAudioOn = getIntent().getBooleanExtra(STATE_AUDIO_STATUS, true);
            options = setupVideoJitsi(isVideoOn, isAudioOn, idJanji);
        } else {
            options = setupVoiceJitsi(idJanji);
        }

        view = new JitsiMeetView(this);
        view.join(options);

        setContentView(view); 
        view.setListener(this);
    }

    private void listenResponse() {
        String eventName = EVENT_RESPONSE_ON_CALL + idJanji;
        Log.d(TAG, "checkListenResponse: " + eventName);
        SocketUtil.getInstance().getChannelVideoChat(idJanji).listenForWhisper(eventName, args -> {
            if (args != null) {
                Log.d(TAG, "getResponse");
            } else {
                Log.d(TAG, "noGetResponse");
            }
            if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VIDEO_CALL) ||
                    getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VOICE_CALL)) {
                onCloseHost();
            }
        });
    }

    private void sendResponseHangup() {
        Log.d(TAG, "checkSendResponse");
        String eventName = EVENT_RESPONSE_ON_CALL + idJanji;
        SocketUtil.getInstance().whisperMessageChannelVideo(eventName,
                (isVideoCall) ? MESSAGE_HANGUP_RESPONSE_VIDEO_CALL : MESSAGE_HANGUP_RESPONSE_VOICE_CALL,
                idJanji);
        onCloseHost();
    }

    private void onCloseHost() {
//        App.getInstance().runOnUiThread(() -> {
//            if (view != null) {
//                view.leave();
//                view.dispose();
//                view = null;
//            }
//            JitsiMeetActivityDelegate.onHostDestroy(this);
//        });
//        this.finish();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.d("SAMPLE", "ONDESTROY");

        if (view != null) {
            view.leave();
            view.dispose();
            view = null;
        }

        super.onDestroy();
        JitsiMeetActivityDelegate.onHostDestroy(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JitsiMeetActivityDelegate.onHostResume(this);
    }

    @Override
    protected void onStop() {
        Log.d("SAMPLE", "ONSTOP");
        super.onStop();
        JitsiMeetActivityDelegate.onHostPause(MyJitsiMeetActivity.this);
    }

    @Override
    public void requestPermissions(String[] strings, int i, PermissionListener permissionListener) {

    }

    @Override
    public void onConferenceJoined(Map<String, Object> map) {

    }

    @Override
    public void onConferenceTerminated(Map<String, Object> map) {
        Log.d("SAMPLE", "TERMINATED");
        sendResponseHangup();
    }

    @Override
    public void onConferenceWillJoin(Map<String, Object> map) {

    }
}

