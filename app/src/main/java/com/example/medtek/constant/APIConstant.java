package com.example.medtek.constant;


/**
 * Url API Medtek
 */

public class APIConstant {

    //GET
    public static final String GET_USER = "/api/user";

    public static final String GET_PASIEN = "/api/get-pasien/{id}";

    public static final String GET_DOKTER = "/api/get-dokter/{id}";

    public static final String GET_JANJI_LIST = "/api/janji";

    public static final String GET_JANJI_SINGLE = "/api/janji/{id}";

    public static final String GET_JANJI_QUEUE = "/api/janji-queue/{id}";

    public static final String GET_JANJI_DEQUEUE = "/api/janji-dequeue/{id}";

    public static final String GET_JANJI_QUEUE_LIST = "/api/janji-queue";

    public static final String GET_JANJI_START = "/api/janji-start/{id}";

    public static final String GET_JANJI_END = "/api/janji-end/{id}";

    public static final String GET_JANJI_DECLINE = "/api/janji-decline/{id}";

    public static final String GET_CONVERSATION = "/api/conversation/{id}";

    public static final String GET_CONVERSATION_LIST = "/api/conversation";

    //POST
    public static final String POST_LOGIN = "/api/login";

    public static final String POST_REFRESH_TOKEN = "/api/refresh-token";

    public static final String POST_CONVERSATION = "/api/conversation/{id}";

    public static final String POST_DOWNLOAD_IMAGE = "/api/download-image";
}

