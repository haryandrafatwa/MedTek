package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.medtek.databinding.ActivityIncomingVoicechatBinding;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.network.socket.SocketUtil;
import com.example.medtek.ui.helper.SingleActivity;

import static com.example.medtek.constant.APPConstant.EVENT_RESPONSE_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_ACC_RESPONSE_VOICE_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VOICE_CALL;
import static com.example.medtek.network.socket.SocketUtil.getMessageFromObject;
import static com.example.medtek.utils.Utils.setSenderPict;
import static com.example.medtek.utils.Utils.setupJitsi;

public class IncomingVoiceChatActivity extends SingleActivity {
    public static final String BUNDLE_CHAT_MODEL = "bundle_chat_model";

    private ActivityIncomingVoicechatBinding binding;

    private ChatsModel chatsModel;

    public static void navigate(Activity activity, ChatsModel chatsModel) {
        Intent intent = new Intent(activity, IncomingVoiceChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(BUNDLE_CHAT_MODEL, chatsModel);
        activity.startActivity(intent);
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
            listenResponseVoiceChat();
            setupJitsi();
            setSenderPict(this, chatsModel.getSenderAvatar(), binding.ivSenderAvatar);
            binding.tvNameUser.setText(chatsModel.getSenderName());

            binding.btnCall.setOnClickListener(v -> {
                sendResponseVoiceChat(true);
                MyJitsiMeetActivity.navigate(this, chatsModel.getIdJanji(), false);
//                VoiceChatActivity.navigate(this, chatsModel);
                finish();
            });

            binding.btnHangup.setOnClickListener(v -> {
                sendResponseVoiceChat(false);
                finish();
            });
        }
    }

    private void listenResponseVoiceChat() {
        String eventName = EVENT_RESPONSE_CALL + chatsModel.getIdJanji();
        SocketUtil.getInstance().getChannelVideoChat(chatsModel.getIdJanji()).listenForWhisper(eventName, args -> {
            if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VOICE_CALL)) {
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
}

