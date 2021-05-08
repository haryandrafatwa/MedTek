package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.medtek.databinding.ActivityIncomingVoicechatBinding;
import com.example.medtek.model.CallModel;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.network.socket.SocketUtil;
import com.example.medtek.ui.helper.SingleActivity;

import static com.example.medtek.constant.APPConstant.EVENT_RESPONSE_CALL;
import static com.example.medtek.constant.APPConstant.EVENT_RESPONSE_ON_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_ACC_RESPONSE_VOICE_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VIDEO_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VOICE_CALL;
import static com.example.medtek.network.socket.SocketUtil.getMessageFromObject;
import static com.example.medtek.utils.Utils.isPatient;
import static com.example.medtek.utils.Utils.setSenderPict;
import static com.example.medtek.utils.Utils.setupJitsi;
import static com.example.medtek.utils.Utils.setupVoiceJitsi;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.eventbus.EventBus;
import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import timber.log.Timber;


public class IncomingVoiceChatActivity extends SingleActivity {
    private static final String TAG = IncomingVoiceChatActivity.class.getSimpleName();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    private JitsiMeetConferenceOptions options;

    public static final String BUNDLE_CHAT_MODEL = "bundle_chat_model";

    private ActivityIncomingVoicechatBinding binding;

    private ChatsModel chatsModel;

    private boolean isTerminatedByResponse = false;
    private boolean isLaunch = false;


    public static void navigate(Activity activity, ChatsModel chatsModel, int requestCode) {
        Intent intent = new Intent(activity, IncomingVoiceChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(BUNDLE_CHAT_MODEL, chatsModel);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected View VcontentView() {
        binding = ActivityIncomingVoicechatBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected int IcontentView() {
        return 0;
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
        chatsModel = getIntent().getParcelableExtra(BUNDLE_CHAT_MODEL);
    }

    @Override
    protected void setupView() {
        if (binding != null) {
//            setupJitsi();
            listenResponseVoiceChat();
//            registerForBroadcastMessages();
//            options = setupVoiceJitsi(chatsModel.getIdJanji());
            setSenderPict(this, chatsModel.getSenderAvatar(), binding.ivSenderAvatar);
            binding.tvNameUser.setText(chatsModel.getSenderName());

            binding.btnCall.setOnClickListener(v -> {
                sendResponseVoiceChat(true);

//                MyJitsiMeetActivity.navigate(this, chatsModel.getIdJanji(), false);

//                VoiceChatActivity.navigate(this, chatsModel);
//                JitsiMeetActivity.launch(this, options);
//                isLaunch = true;
//                Intent intent = new Intent();
//                intent.putExtra(START_VOICE_CALL, true);
                setResult(Activity.RESULT_OK);
                finish();
            });

            binding.btnHangup.setOnClickListener(v -> {
                sendResponseVoiceChat(false);
//                Intent intent = getIntent();
//                intent.putExtra(START_VOICE_CALL, false);
                setResult(Activity.RESULT_CANCELED);
                finish();
            });
        }
    }

    private void listenResponseVoiceChat() {
        String eventName = EVENT_RESPONSE_CALL + chatsModel.getIdJanji();
        SocketUtil.getInstance().getChannelVideoChat(chatsModel.getIdJanji()).listenForWhisper(eventName, args -> {
            if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VOICE_CALL)) {
//                if (isLaunch) {
//                    isTerminatedByResponse = true;
//                    ChatRoomActivity.count++;
//                    hangUp();
//                }
//                Intent intent = getIntent();
//                intent.putExtra(START_VOICE_CALL, false);
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    private void sendResponseVoiceChat(boolean isAcc) {
        String eventName = EVENT_RESPONSE_CALL + chatsModel.getIdJanji();
        if (isAcc) {
            SocketUtil.getInstance().whisperMessageChannelVideo(eventName, MESSAGE_ACC_RESPONSE_VOICE_CALL, chatsModel.getIdJanji());
        } else {
            SocketUtil.getInstance().whisperMessageChannelVideo(eventName, MESSAGE_HANGUP_RESPONSE_VOICE_CALL, chatsModel.getIdJanji());
        }
    }

//    @Override
//    protected void onDestroy() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
//
//        super.onDestroy();
//    }

    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                ... other events
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Timber.i("Conference Joined with url%s", event.getData().get("url"));
                    break;
                case PARTICIPANT_JOINED:
                    Timber.i("Participant joined%s", event.getData().get("name"));
                    break;
                case CONFERENCE_TERMINATED:
                    Log.d(TAG, "CONFERENCE_TERMINATED");
                    Timber.i("Error: %s", event.getData().get("error"));
                    if (!isTerminatedByResponse) {
                        sendResponseHangup();
                        ChatRoomActivity.count++;
                        finish();
                    }
                    break;
                case PARTICIPANT_LEFT:
                    Timber.i("Participant left%s", event.getData().get("name"));
                    break;
            }
        }
    }

    private void sendResponseHangup() {
        Log.d(TAG, "checkSendResponse");
        String eventName = EVENT_RESPONSE_CALL + chatsModel.getIdJanji();
        SocketUtil.getInstance().whisperMessageChannelVideo(eventName,
                MESSAGE_HANGUP_RESPONSE_VOICE_CALL, chatsModel.getIdJanji());
    }

    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }
}

