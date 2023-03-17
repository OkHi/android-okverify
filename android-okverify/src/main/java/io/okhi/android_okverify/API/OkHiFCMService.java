package io.okhi.android_okverify.API;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import io.okhi.android_core.interfaces.OkHiRequestHandler;
import io.okhi.android_core.models.OkHiCoreUtil;
import io.okhi.android_core.models.OkHiException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHiFCMService {

    public static void postFcmToken(String locationId, String token, final OkHiRequestHandler<String> handler) {
        JSONObject payload = new JSONObject();
        try{
            payload.put("locationId", locationId);
            payload.put("token", token);
        }catch (JSONException je){
            je.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient.Builder().build();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), payload.toString());
        Request.Builder requestBuild = new Request.Builder();
        requestBuild.post(requestBody);
        requestBuild.url("https://jsondataserver.okhi.io/data");
        Request request = requestBuild.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                e.printStackTrace();
                OkHiException exception = new OkHiException(OkHiException.UNKNOWN_ERROR_CODE, Objects.requireNonNull(e.getMessage()));
                handler.onError(exception);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    handler.onResult(response.message());
                } else {
                    OkHiException exception = OkHiCoreUtil.generateOkHiException(response);
                    handler.onError(exception);
                }
                response.close();
            }
        });
    }
}
