package com.example.medtek.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.model.UserModel;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

import static com.example.medtek.BuildConfig.BASE_URL;
import static com.example.medtek.constant.APPConstant.CHANNEL_VIDEO_CHAT;
import static com.example.medtek.constant.APPConstant.ERROR_NULL;
import static com.example.medtek.constant.APPConstant.LOGIN_DOKTER;
import static com.example.medtek.constant.APPConstant.LOGIN_PASIEN;
import static com.example.medtek.constant.APPConstant.NO_CONNECTION;
import static com.example.medtek.constant.APPConstant.SERVER_BROKEN;
import static com.example.medtek.utils.PropertyUtil.DATA_USER;
import static com.example.medtek.utils.PropertyUtil.USER_TYPE;
import static com.example.medtek.utils.PropertyUtil.getData;

public class Utils {
    public static boolean isPatient() {
        return (int) getData(USER_TYPE) == LOGIN_PASIEN;
    }

    public static boolean isDoctor() {
        return (int) getData(USER_TYPE) == LOGIN_DOKTER;
    }

    public static String[] getPermissionStorageList() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
    }

    public static String[] getPermisionCameraList() {
        return new String[]{
                Manifest.permission.CAMERA,
        };
    }

    public static String[] getPermissionChatList() {
        return new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
        };
    }

    public static boolean checkPermission(Activity activity, String[] permission) {
        for (String perm : permission) {
            if (ActivityCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissionCompat(Activity activity, String[] permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, permission, requestCode);
    }

    public static boolean isEmailValid(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern) && email.length() > 0;
    }

    public static String dateTimeToStringHour(DateTime date) {
        return DateTimeFormat.forPattern("HH:mm").print(date);
    }

    public static String dateTimeToStringDate(DateTime date) {
        return DateTimeFormat.forPattern("yyy-MM-dd").print(date);
    }

    public static String dateTimeToString(DateTime date) {
        return DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").print(date);
    }

    public static DateTime getDateTime(String date, DateTimeZone timeZone) {
        return DateTimeFormat
                .forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                .withZone(timeZone)
                .parseDateTime(date);
    }

    public static DateTime getDateTime(String date) {
        return DateTimeFormat
                .forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                .parseDateTime(date);
    }

    public static LocalTime getTime(String date) {
        return DateTimeFormat
                .forPattern("HH:mm:ss")
                .parseDateTime(date).toLocalTime();
    }

    public static DateTime getDate(String date) {
        return DateTimeFormat
                .forPattern("yyyy-MM-dd")
                .parseDateTime(date);
    }

    public static String timeToHour(String time) {
        return DateTimeFormat.forPattern("HH:mm").print(
                DateTimeFormat.forPattern("HH:mm:ss").parseDateTime(time)
        );
    }

    public static String changeDatePattern(String date) {
        return DateTimeFormat.forPattern("dd MMM yyyy").print(
                DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(date)
        );
    }

    public static String setNewFileName(Context context) {
        return context.getResources().getString(R.string.app_name) +
                "_" +
                DateTimeFormat.forPattern("yyyy").print(new DateTime()) +
                DateTimeFormat.forPattern("MM").print(new DateTime()) +
                DateTimeFormat.forPattern("dd").print(new DateTime()) +
                DateTimeFormat.forPattern("HH").print(new DateTime()) +
                DateTimeFormat.forPattern("mm").print(new DateTime());
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String getFileExt(String path) {
        return path.substring(path.lastIndexOf("."));
    }

    public static String getFileSize(long size) {
        String hrSize = "";
        double m = size/1024.0;
        double n = m/1024.0;
        DecimalFormat dec = new DecimalFormat("0.00");

        if (n > 1) {
            hrSize = dec.format(n).concat(" MB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" KB");
        } else {
            hrSize = dec.format(size).concat(" B");
        }
        return hrSize;
    }

    public static String getFileMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = App.getContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static String getFileMimeTypeWithoutDot(String mimeType) {
        String[] arrFileInfo = mimeType.split("/");
        return arrFileInfo[1];
    }

    public static String getImagePath() {
        return Environment.getExternalStorageDirectory() +
                File.separator +
                "Medtek" +
                File.separator +
                "Images" +
                File.separator;
    }

    public static String getVideoPath() {
        return Environment.getExternalStorageDirectory() +
                File.separator +
                "Medtek" +
                File.separator +
                "Videos" +
                File.separator;
    }

    public static String getDocumentPath() {
        return Environment.getExternalStorageDirectory() +
                File.separator +
                "Medtek" +
                File.separator +
                "Document" +
                File.separator;
    }

    @SuppressLint("DefaultLocale")
    public static String getDurationVideo(String path) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(path);
        String duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(duration);

        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec))
        );
    }

    public static String getThousandFormat(int value) {
        String tempVal = "";
        Locale local = new Locale("id", "id");
        DecimalFormatSymbols decimalSymbol = new DecimalFormatSymbols(local);
        DecimalFormat formatter = new DecimalFormat("#,###", decimalSymbol);
        if (value > 0) {
            tempVal = formatter.format(value);
        } else {
            tempVal = formatter.format(0);
        }

        return tempVal;
    }

    public static void copyFileDst(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void setSenderPict(Activity activity, String senderAvatar, ImageView ivSenderAvatar) {
        if (senderAvatar.equals("")) {
            if ((int) getData(USER_TYPE) == LOGIN_PASIEN) {
                ivSenderAvatar.setImageResource(R.drawable.ic_dokter_square);
            } else {
                ivSenderAvatar.setImageResource(R.drawable.ic_pasien_square);
            }
        } else {
            Glide.with(activity)
                    .load(BASE_URL + senderAvatar)
                    .into(ivSenderAvatar);
        }
    }

    public static void setupJitsi() {
        URL serverURL;

        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }

        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setEmail(((UserModel) getData(DATA_USER)).getEmail());
        userInfo.setDisplayName(((UserModel) getData(DATA_USER)).getName());

        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // When using JaaS, set the obtained JWT here
                //.setToken("MyJWT")
                .setUserInfo(userInfo)
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);
    }

    public static JitsiMeetConferenceOptions setupVideoJitsi(boolean isVideoOn, boolean isAudioOn, int idJanji) {
        JitsiMeetConferenceOptions optionsVideo = new JitsiMeetConferenceOptions.Builder()
                .setRoom(CHANNEL_VIDEO_CHAT + idJanji)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("meeting-name.enabled", false)
                .setVideoMuted(!isVideoOn)
                .setAudioMuted(!isAudioOn)
                .build();

        return optionsVideo;
    }

    public static JitsiMeetConferenceOptions setupVoiceJitsi(int idJanji) {
        JitsiMeetConferenceOptions optionsVideo = new JitsiMeetConferenceOptions.Builder()
                .setRoom(CHANNEL_VIDEO_CHAT + idJanji)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("meeting-name.enabled", false)
                .setAudioOnly(true)
                .build();

        return optionsVideo;
    }

    public static void isLoading(Boolean status, View view) {
        App.getInstance().runOnUiThread(() -> {
            if (status) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        });
    }

    public static String TAG(Class aClass) {
        return aClass.getSimpleName();
    }

    public static void showToastyError(Context context, int type) {
        switch(type){
            case ERROR_NULL:
                App.getInstance().runOnUiThread(() -> {
                    Toasty.error(context, context.getString(R.string.error_null_desc)).show();
                });
                break;
            case SERVER_BROKEN:
                App.getInstance().runOnUiThread(() -> {
                    Toasty.error(context, context.getString(R.string.error_busy_desc)).show();
                });
                break;
            case NO_CONNECTION:
                App.getInstance().runOnUiThread(() -> {
                    Toasty.error(context, context.getString(R.string.error_no_conn_desc)).show();
                });
                break;
        }
    }
}

