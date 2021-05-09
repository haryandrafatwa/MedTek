package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.databinding.ActivityOutcomingVideochatBinding;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.network.socket.SocketUtil;
import com.example.medtek.ui.helper.SingleActivity;
import com.example.medtek.utils.Utils;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import static com.example.medtek.constant.APPConstant.EVENT_REQUEST_CALL;
import static com.example.medtek.constant.APPConstant.EVENT_RESPONSE_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_ACC_RESPONSE_VIDEO_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_HANGUP_RESPONSE_VIDEO_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_REQUEST_VIDEO_CALL;
import static com.example.medtek.network.socket.SocketUtil.getMessageFromObject;
import static com.example.medtek.ui.activity.ChatRoomActivity.IS_AUDIO_ON;
import static com.example.medtek.ui.activity.ChatRoomActivity.IS_VIDEO_ON;
import static com.example.medtek.ui.activity.IncomingVideoChatActivity.BUNDLE_CHAT_MODEL;
import static com.example.medtek.ui.activity.IncomingVideoChatActivity.PERMISSIONS_REQUEST_CODE_VIDEO_CHAT;
import static com.example.medtek.utils.Utils.getPermissionChatList;
import static com.example.medtek.utils.Utils.isPatient;
import static com.example.medtek.utils.Utils.requestPermissionCompat;
import static com.example.medtek.utils.Utils.setSenderPict;

/**
 * Activity untuk start dan handling response request Video Call
 */

public class OutcomingVideoChatActivity extends SingleActivity {
    private static final String TAG = OutcomingVideoChatActivity.class.getSimpleName();

    private ActivityOutcomingVideochatBinding binding;

    private ChatsModel chatsModel;
    private Preview preview;
    private ProcessCameraProvider cameraProvider;
    private boolean isAudioOn = true;
    private boolean isVideoOn = true;

    public static void navigate(Activity activity, ChatsModel chatsModel, int requestCode) {
        Intent intent = new Intent(activity, OutcomingVideoChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(BUNDLE_CHAT_MODEL, chatsModel);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected View VcontentView() {
        binding = ActivityOutcomingVideochatBinding.inflate(getLayoutInflater());
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE_VIDEO_CHAT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPreview();
            } else {
                this.setResult(Activity.RESULT_CANCELED);
                this.finish();
            }
        } else {
            this.setResult(Activity.RESULT_CANCELED);
            this.finish();
        }
    }

    @Override
    protected void setupView() {
        if (binding != null) {
            if (Utils.checkPermission(this, getPermissionChatList())) {
                startPreview();
            } else {
                requestPermissionCompat(this, getPermissionChatList(), PERMISSIONS_REQUEST_CODE_VIDEO_CHAT);
            }
        }
    }

    private void startPreview() {
        listenResponseVideoChat();
        sendRequestVideoChat();
        setCameraProvider();
        setSenderPict(this, chatsModel.getSenderAvatar(), binding.ivSenderAvatar);
        binding.tvNameUser.setText(chatsModel.getSenderName());

        binding.btnAudioMute.setOnClickListener(v -> {
            if (isAudioOn) {
                binding.btnAudioMute.setBackground(getResources().getDrawable(R.drawable.ic_audio_off));
                isAudioOn = false;
            } else {
                binding.btnAudioMute.setBackground(getResources().getDrawable(R.drawable.ic_audio_on));
                isAudioOn = true;
            }
        });

        binding.btnVideoMute.setOnClickListener(v -> {
            if (isVideoOn) {
                binding.btnVideoMute.setBackground(getResources().getDrawable(R.drawable.ic_video_off));
                isVideoOn = false;
                setVideoPreview(isVideoOn);
            } else {
                binding.btnVideoMute.setBackground(getResources().getDrawable(R.drawable.ic_video_on));
                isVideoOn = true;
                setVideoPreview(isVideoOn);
            }
        });

        binding.btnHangup.setOnClickListener(v -> {
            sendResponseHangupVideoChat();
            unbindPreview();
            this.setResult(Activity.RESULT_CANCELED);
            this.finish();
        });
    }

    private void setVideoPreview(boolean isVideoOn) {
        if (isVideoOn) {
            binding.previewView.setVisibility(View.VISIBLE);
            binding.ivSenderAvatar.setVisibility(View.GONE);
        } else {
            binding.previewView.setVisibility(View.GONE);
            binding.ivSenderAvatar.setVisibility(View.VISIBLE);
        }
    }

    private void listenResponseVideoChat() {
        String eventName = EVENT_RESPONSE_CALL + chatsModel.getIdJanji();
        SocketUtil.getInstance().getChannelVideoChat(chatsModel.getIdJanji()).listen(eventName, args -> {
            if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_ACC_RESPONSE_VIDEO_CALL)) {
                unbindPreview();
                Intent intent = getIntent();
                intent.putExtra(IS_VIDEO_ON, isVideoOn);
                intent.putExtra(IS_AUDIO_ON, isAudioOn);
                this.setResult(Activity.RESULT_OK, intent);
                this.finish();
            } else if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VIDEO_CALL)) {
                unbindPreview();
                this.setResult(Activity.RESULT_CANCELED);
                this.finish();
            }
        });
        SocketUtil.getInstance().getChannelVideoChat(chatsModel.getIdJanji()).listenForWhisper(eventName, args -> {
            if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_ACC_RESPONSE_VIDEO_CALL)) {
                unbindPreview();
                Intent intent = getIntent();
                intent.putExtra(IS_VIDEO_ON, isVideoOn);
                intent.putExtra(IS_AUDIO_ON, isAudioOn);
                this.setResult(Activity.RESULT_OK, intent);
                this.finish();
            } else if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_HANGUP_RESPONSE_VIDEO_CALL)) {
                unbindPreview();
                this.setResult(Activity.RESULT_CANCELED);
                this.finish();
            }
        });
    }

    private void sendResponseHangupVideoChat() {
        String eventName = EVENT_RESPONSE_CALL + chatsModel.getIdJanji();
        SocketUtil.getInstance().whisperMessageChannelVideo(eventName, MESSAGE_HANGUP_RESPONSE_VIDEO_CALL, chatsModel.getIdJanji());
    }


    private void sendRequestVideoChat() {
        if (!isPatient()) {
            String eventName = EVENT_REQUEST_CALL + chatsModel.getIdJanji();
            SocketUtil.getInstance().whisperMessageChannelVideo(eventName, MESSAGE_REQUEST_VIDEO_CALL, chatsModel.getIdJanji());
        }
    }

    private void setCameraProvider() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        binding.previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.previewView.getDisplay().getRotation())
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(binding.previewView.createSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    private void unbindPreview() {
        if (cameraProvider != null) {
            if (preview != null) {
                App.getInstance().runOnUiThread(() -> cameraProvider.unbindAll());
            }
        }
    }
}

