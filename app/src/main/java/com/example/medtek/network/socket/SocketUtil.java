package com.example.medtek.network.socket;

import android.util.Log;

import com.example.medtek.model.JanjiModel;
import com.example.medtek.model.MessageModel;

import net.mrbin99.laravelechoandroid.Echo;
import net.mrbin99.laravelechoandroid.EchoOptions;
import net.mrbin99.laravelechoandroid.channel.SocketIOPrivateChannel;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.medtek.constant.APPConstant.BASE_SOCKET_URL;
import static com.example.medtek.constant.APPConstant.CHANNEL_JANJI;
import static com.example.medtek.constant.APPConstant.CHANNEL_MESSAGES;
import static com.example.medtek.constant.APPConstant.CHANNEL_VIDEO_CHAT;
import static com.example.medtek.constant.APPConstant.EVENT_JANJI;
import static com.example.medtek.constant.APPConstant.EVENT_MESSAGE_CREATED;
import static com.example.medtek.constant.APPConstant.EVENT_REQUEST_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_REQUEST_VIDEO_CALL;
import static com.example.medtek.constant.APPConstant.MESSAGE_REQUEST_VOICE_CALL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.isPatient;

public class SocketUtil {
    private static final String TAG = SocketUtil.class.getSimpleName();
    public static Echo echo;
    private static SocketUtil instance;
    private static SocketIOPrivateChannel channelVideoChat;
    private SocketIOPrivateChannel channelMessage;
    private SocketIOPrivateChannel channelJanji;

    public static SocketUtil getInstance() {
        if (instance == null) {
            instance = new SocketUtil();
        }
        if (echo == null || !echo.isConnected()) {
            connectSocket();
        }
        return instance;
    }

    private static void connectSocket() {
        EchoOptions echoOptions = new EchoOptions();
        echoOptions.host = BASE_SOCKET_URL;

        echoOptions.headers.put("Authorization", "Bearer " + getData(ACCESS_TOKEN));

        echo = new Echo(echoOptions);
        echo.connect(args -> {
            Log.d(TAG, "successConnectSocket");
        }, args -> Log.e(TAG, "errConnectSocket"));
    }

    public static String getMessageFromObject(Object... args) {
        Log.d(TAG, "getMessageFromObject() called");
        String message = "";
        try {
            JSONObject object = (JSONObject) args[1];
            message = object.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    public SocketIOPrivateChannel getChannelVideoChat(int idJanji) {
        if (channelVideoChat == null) {
            setChannelVideoChat(idJanji);
        }
        return channelVideoChat;
    }

    private void setChannelMessage(int idConversation) {
        channelMessage = echo.privateChannel(CHANNEL_MESSAGES + idConversation);
    }

    private void setChannelJanji(int idJanji) {
        channelJanji = echo.privateChannel(CHANNEL_JANJI + idJanji);
    }

    public void listenForEventChat(int idConversation, int idJanji) {
        if (channelMessage == null || channelJanji == null) {
            setChannelMessage(idConversation);
            setChannelJanji(idJanji);
        }
        Log.d(TAG, "channel: " + CHANNEL_MESSAGES + idConversation);
        channelMessage.listen(EVENT_MESSAGE_CREATED, args -> {
            MessageModel NewChat = MessageModel.Companion.parseFrom(args);
            Log.d(TAG, NewChat.getChat().getMessage());
            EventBus.getDefault().post(NewChat);
        });
        channelJanji.listen(EVENT_JANJI, args -> {
            JanjiModel janjiUpdate = JanjiModel.Companion.parseFrom(args);
            Log.d(TAG, "idStatus : " + janjiUpdate.getJanji().getIdStatus());
            EventBus.getDefault().post(janjiUpdate);
        });
    }

    public void leaveChannelMessage(int idConversation) {
        if (channelMessage != null) {
            echo.leave(CHANNEL_MESSAGES + idConversation);
            channelMessage = null;
        }
    }

    public void leaveChannelJanji(int idJanji) {
        if (channelJanji != null) {
            echo.leave(CHANNEL_JANJI + idJanji);
            channelJanji = null;
        }
    }

    public void leaveChannelVideo(int idJanji) {
        if (channelVideoChat != null) {
            echo.leave(CHANNEL_VIDEO_CHAT + idJanji);
            channelVideoChat = null;
        }
    }

    public void disconnectSocket() {
        echo.disconnect();
    }

    public void setChannelVideoChat(int idJanji) {
        channelVideoChat = echo.privateChannel(CHANNEL_VIDEO_CHAT + idJanji);
        if (isPatient()) {
            Log.d(TAG, "listenForStart: " + EVENT_REQUEST_CALL + idJanji);
            channelVideoChat.listenForWhisper(EVENT_REQUEST_CALL + idJanji, args -> {
                Log.d(TAG, "receiveRequest");
                if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_REQUEST_VIDEO_CALL)) {
                    Log.d(TAG, "messageReceived: " + MESSAGE_REQUEST_VIDEO_CALL);
                    EventBus.getDefault().post(MESSAGE_REQUEST_VIDEO_CALL);
                } else if (getMessageFromObject(args).equalsIgnoreCase(MESSAGE_REQUEST_VOICE_CALL)) {
                    Log.d(TAG, "messageReceived: " + MESSAGE_REQUEST_VOICE_CALL);
                    EventBus.getDefault().post(MESSAGE_REQUEST_VOICE_CALL);
                }
            });
        }
    }

    public void whisperMessageChannelVideo(String eventName, String message, int idJanji) {
        try {
            Log.d(TAG, "whisperMessage() called with: message = [" + message + "]");
            Log.d(TAG, "eventName = [" + eventName + "]");
            JSONObject data = new JSONObject();
            data.put("message", message);
            SocketUtil.getInstance().getChannelVideoChat(idJanji).whisper(eventName,
                    data, args -> {

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

