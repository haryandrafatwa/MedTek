package com.example.medtek.ui.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import com.example.medtek.network.socket.SocketUtil;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.R.id;

import java.util.HashMap;

import static com.example.medtek.constant.APPConstant.EVENT_RESPONSE_ON_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VIDEO_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VOICE_CALL;
import static com.example.medtek.network.socket.SocketUtil.getMessageFromObject;

public class NewJitsiMeetActivity extends JitsiMeetActivity {

    private int idJanji;
    private boolean isVideoCall = false;

    public NewJitsiMeetActivity(int idJanji) {
        this.idJanji = idJanji;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onConferenceTerminated(HashMap<String, Object> extraData) {
        Log.i(TAG, "TEST TERMINATED");
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(manager.findFragmentById(id.jitsiFragment));
        transaction.commit();
        sendResponseHangup();
        super.onConferenceTerminated(extraData);
        this.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        JitsiMeetActivityDelegate.onHostPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JitsiMeetActivityDelegate.onHostResume(this);
    }

    private void listenResponse() {
        String eventName = EVENT_RESPONSE_ON_CALL + idJanji;
        Log.d(TAG, "checkListenResponse: " + eventName);
        SocketUtil.getInstance().getChannelVideoChat(idJanji).listenForWhisper(eventName, args -> {
            if (args != null) {
                Log.d(TAG, "getResponse: " + getMessageFromObject(args));
            } else {
                Log.d(TAG, "noGetResponse");
            }
            if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VIDEO_CALL) ||
                    getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VOICE_CALL)) {
                this.onDestroy();
            }
        });
    }

    private void sendResponseHangup() {
        Log.d(TAG, "checkSendResponse");
        String eventName = EVENT_RESPONSE_ON_CALL + idJanji;
        SocketUtil.getInstance().whisperMessageChannelVideo(eventName,
                (isVideoCall) ? MESSAGE_HANGUP_RESPONSE_VIDEO_CALL : MESSAGE_HANGUP_RESPONSE_VOICE_CALL,
                idJanji);
    }

}
