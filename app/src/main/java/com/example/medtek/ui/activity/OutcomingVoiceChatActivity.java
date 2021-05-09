package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.medtek.databinding.ActivityOutcomingVoicechatBinding;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.network.socket.SocketUtil;
import com.example.medtek.ui.helper.SingleActivity;

import static com.example.medtek.constant.APPConstant.EVENT_REQUEST_CALL;
import static com.example.medtek.constant.APPConstant.EVENT_RESPONSE_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_ACC_RESPONSE_VOICE_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VOICE_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_REQUEST_VOICE_CALL;
import static com.example.medtek.network.socket.SocketUtil.getMessageFromObject;
import static com.example.medtek.ui.activity.IncomingVoiceChatActivity.BUNDLE_CHAT_MODEL;
import static com.example.medtek.utils.Utils.isPatient;
import static com.example.medtek.utils.Utils.setSenderPict;

/**
 * Activity untuk start dan handling response request Voice Call
 */

public class OutcomingVoiceChatActivity extends SingleActivity {
    private static final String TAG = OutcomingVoiceChatActivity.class.getSimpleName();

    private ActivityOutcomingVoicechatBinding binding;

    private ChatsModel chatsModel;

    public static void navigate(Activity activity, ChatsModel chatsModel, int requestCode) {
        Intent intent = new Intent(activity, OutcomingVoiceChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(BUNDLE_CHAT_MODEL, chatsModel);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected View VcontentView() {
        binding = ActivityOutcomingVoicechatBinding.inflate(getLayoutInflater());
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
        listenResponseVoiceChat();
        sendRequestVoiceChat();
        setSenderPict(this, chatsModel.getSenderAvatar(), binding.ivSenderAvatar);
        binding.tvNameUser.setText(chatsModel.getSenderName());

        binding.btnHangup.setOnClickListener(v -> {
            sendResponseHangupVideoChat();
            this.setResult(Activity.RESULT_CANCELED);
            this.finish();
        });
    }

    private void sendRequestVoiceChat() {
        if (!isPatient()) {
            String eventName = EVENT_REQUEST_CALL + chatsModel.getIdJanji();
            SocketUtil.getInstance().whisperMessageChannelVideo(eventName, MESSAGE_REQUEST_VOICE_CALL, chatsModel.getIdJanji());
        }
    }

    private void sendResponseHangupVideoChat() {
        String eventName = EVENT_RESPONSE_CALL + chatsModel.getIdJanji();
        SocketUtil.getInstance().whisperMessageChannelVideo(eventName, MESSAGE_HANGUP_RESPONSE_VOICE_CALL, chatsModel.getIdJanji());
    }

    private void listenResponseVoiceChat() {
        String eventName = EVENT_RESPONSE_CALL + chatsModel.getIdJanji();
        SocketUtil.getInstance().getChannelVideoChat(chatsModel.getIdJanji()).listen(eventName, args -> {
            if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_ACC_RESPONSE_VOICE_CALL)) {
                this.setResult(Activity.RESULT_OK);
                this.finish();
            } else if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VOICE_CALL)) {
                this.setResult(Activity.RESULT_CANCELED);
                this.finish();
            }
        });
        SocketUtil.getInstance().getChannelVideoChat(chatsModel.getIdJanji()).listenForWhisper(eventName, args -> {
            if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_ACC_RESPONSE_VOICE_CALL)) {
                setResult(Activity.RESULT_OK);
                finish();
            } else if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VOICE_CALL)) {
                this.setResult(Activity.RESULT_CANCELED);
                this.finish();
            }
        });
    }

}

