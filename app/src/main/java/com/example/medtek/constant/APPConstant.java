package com.example.medtek.constant;

import com.example.medtek.BuildConfig;

public class APPConstant {
    public static final int API_TIMEOUT = 5; // in minutes

    public static final int LOGIN_DOKTER = 2;
    public static final int LOGIN_PASIEN = 1;

    // image
    public static final int IMAGE_AVATAR = 1;

    //Status Appointment
    public static final int BELUM_DIMULAI = 1;
    public static final int MENUNGGU_DIANTRIAN = 2;
    public static final int SEDANG_BERLANSUNG = 3;
    public static final int SUDAH_SELESAI = 4;
    public static final int PASIEN_TIDAK_DATANG = 5;

    //Error State
    public static final int ERROR_NULL = 1;
    public static final int SERVER_BROKEN = 2;
    public static final int NO_CONNECTION = 3;

    //Chat type
    public static final int TEXT = 1;
    public static final int IMAGE = 2;
    public static final int VIDEO = 3;
    public static final int FILE = 4;

    //Transaction type
    public static final int PAYMENT_JANJI = 1;
    public static final int RECEIVE_PAYMENT = 2;
    public static final int TOP_UP = 3;
    public static final int REFUND = 4;


    public static final int SPLASH_CODE = 100;

    //Pending Intent
    public static final int NOTIFICATION_REMINDER_REQUEST_CODE = 200;
    public static final int NOTIFICATION_MESSAGE_REQUEST_CODE = 201;

    //Socket
    public static final String BASE_SOCKET_URL = BuildConfig.BASE_URL + ":6001";

    //Channel
    public static final String CHANNEL_MESSAGES = "App.User.Conversation.";
    public static final String CHANNEL_VIDEO_CHAT = "App.User.Video.";
    public static final String CHANNEL_JANJI = "App.User.Janji.";

    //Event
    public static final String EVENT_MESSAGE_CREATED = ".new-chat";
    public static final String EVENT_JANJI = ".janji-update";
    public static final String EVENT_REQUEST_CALL = "request-call-";
    public static final String EVENT_RESPONSE_CALL = "response-call-";
    public static final String EVENT_RESPONSE_ON_CALL = "response-on-call-";

    //message
    public static final String MESSAGE_REQUEST_VIDEO_CALL = "requestVideoCall";
    public static final String MESSAGE_REQUEST_VOICE_CALL = "requestVoiceCall";
    public static final String MESSAGE_ACC_RESPONSE_VIDEO_CALL = "startVideoCall";
    public static final String MESSAGE_ACC_RESPONSE_VOICE_CALL = "startVoiceCall";
    public static final String MESSAGE_HANGUP_RESPONSE_VIDEO_CALL = "hangupVideoCall";
    public static final String MESSAGE_HANGUP_RESPONSE_VOICE_CALL = "hangupVoiceCall";

}

