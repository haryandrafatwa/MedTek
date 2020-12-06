package com.example.medtek.utils;

import com.example.medtek.model.state.ApplyStateType;
import com.example.medtek.network.response.AuthTokenResponse;
import com.orhanobut.hawk.Hawk;

import org.joda.time.DateTime;

import static com.example.medtek.utils.Utils.dateTimeToString;

public class PropertyUtil {
    public static final String APPLICATION_STATE = "prop_app_state";
    public static final String AFTER_SET_VIEWPAGER = "prop_after_viewpager";
    public static final String ID_DOKTER = "prop_id_dokter";
    public static final String ID_PASIEN = "prop_id_pasien";
    // Login Status
    public static final String LOGIN_STATUS = "prop_login_status";
    public static final String USER_TYPE = "prop_user_type";
    public static final String ACCESS_TOKEN = "prop_access_token";
    public static final String REFRESH_TOKEN = "prop_refresh_token";
    public static final String IS_AFTER_RESET = "prop_after_reset";
    public static final String EXPIRED_TOKEN = "prop_expired_token";
    //Profile
    public static final String EMAIL_USER = "prop_email_user";
    public static final String DATA_USER = "prop_data_user";
    //Chats
    public static final String ACTIVE_CHAT = "prop_active_chats";

    private static final String TAG = PropertyUtil.class.getSimpleName();

    public static void setData(String key, Object value) {
        Hawk.put(key, value);
    }

    public static Object getData(String key) {
        return Hawk.get(key);
    }

    public static Boolean searchData(String key) {
        return Hawk.contains(key);
    }

    public static void deleteData(String key) {
        Hawk.delete(key);
    }

    public static void deleteAllData() {
        Hawk.deleteAll();
    }

    public static @ApplyStateType
    int getApplicationState() {
        String s = (String) getData(APPLICATION_STATE);
        try {
            @ApplyStateType int state = Integer.parseInt(s);
            return state;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ApplyStateType.State_None;
    }

    public static void setApplicationState(@ApplyStateType int state) {
        setData(APPLICATION_STATE, String.valueOf(state));
    }

    public static void setDataLogin(AuthTokenResponse value, int typeLogin) {
        setData(LOGIN_STATUS, true);
        setData(ACCESS_TOKEN, value.getAccessToken());
        setData(REFRESH_TOKEN, value.getRefreshToken());
        setData(USER_TYPE, typeLogin);
        setExpiredRefreshToken(value.getExpiresIn());
    }

    public static void setDataLogin(AuthTokenResponse value) {
        setData(LOGIN_STATUS, true);
        setData(ACCESS_TOKEN, value.getAccessToken());
        setData(REFRESH_TOKEN, value.getRefreshToken());
        setExpiredRefreshToken(value.getExpiresIn());
    }

    public static void setExpiredRefreshToken(int expiredToken) {
        DateTime dateNow = new DateTime();
        DateTime dateTokenExpired = dateNow.plusSeconds(expiredToken - 120);
        setData(EXPIRED_TOKEN, dateTimeToString(dateTokenExpired));
    }
}

