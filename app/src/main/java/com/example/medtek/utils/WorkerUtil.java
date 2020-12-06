package com.example.medtek.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.medtek.network.APIInterface;
import com.example.medtek.network.APIService;
import com.example.medtek.network.response.PostConversationResponse;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static java.lang.String.valueOf;

public class WorkerUtil extends Worker {
    public static final String BUNDLE_MESSAGE = "bundle_messaage";
    public static final String BUNDLE_ATTACHMENT = "bundle_attachment";
    public static final String BUNDLE_ID_CONV = "bundle_id_conv";
    private static final String TAG = WorkerUtil.class.getSimpleName();
    private final APIInterface service;
    private final Context context;

    private Result resultStatus;

    public WorkerUtil(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        service = APIService.getClientService().create(APIInterface.class);
    }

    @NonNull
    @Override
    public Result doWork() {
        String message = getInputData().getString(BUNDLE_MESSAGE);
        String attachment = getInputData().getString(BUNDLE_ATTACHMENT);
        int idConversation = getInputData().getInt(BUNDLE_ID_CONV, 0);
        if (attachment.isEmpty()) {
            return postConversation(message, idConversation);
        } else {
            return postConversationFile(message, attachment, idConversation);
        }
    }

    private Result postConversation(String message, int idConversation) {
        HashMap<String, String> body = new HashMap<>();
        body.put("message", message);

        Call<PostConversationResponse> call = service.postConversation(APIService.getAuthHeader(), body, valueOf(idConversation));
        try {
            Response<PostConversationResponse> response = call.execute();
            if (response != null) {
                PostConversationResponse responseAPI = response.body();
                resultStatus = Result.success();
                Log.d(TAG, responseAPI.getData().getMessage());
            } else {
                resultStatus = Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultStatus = Result.failure();
            Log.d(TAG, "onFailure: Gagal....");
        }
        return resultStatus;
    }

    private Result postConversationFile(String message, String path, int idConversation) {
        RequestBody messageSent = RequestBody.create(MultipartBody.FORM, message);

        File fileOriginal = new File(path);

        final String[] uriType = {""};
        MediaScannerConnection.scanFile(context, new String[]{fileOriginal.getAbsolutePath()}, null, ((pathUri, uri) -> {
            Log.i(TAG, uri.toString());
            uriType[0] = context.getContentResolver().getType(uri);
        }));

        RequestBody attachment = RequestBody.create(
                MediaType.parse(uriType[0]),
                fileOriginal);

        MultipartBody.Part attachmentSent = MultipartBody.Part.createFormData("attachment", fileOriginal.getName(), attachment);

        Call<PostConversationResponse> call = service.postConversationFile(APIService.getAuthHeader(), messageSent, attachmentSent, valueOf(idConversation));
        try {
            Response<PostConversationResponse> response = call.execute();
            if (response != null) {
                PostConversationResponse responseAPI = response.body();
                resultStatus = Result.success();
                Log.d(TAG, responseAPI.getData().getMessage());
                Log.d(TAG, responseAPI.getData().getAttachment());
            } else {
                resultStatus = Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultStatus = Result.failure();
            Log.d(TAG, "onFailure: Gagal....");
        }
        return resultStatus;
    }
}
