package io.okhi.android_okverify.models;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import io.okhi.android_core.models.OkHiException;
import io.okhi.android_core.models.OkHiMode;
import io.okhi.android_okverify.interfaces.OkVerifyAsyncTaskHandler;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkVerifyStop extends AsyncTask<Void, Void, String> {
    private String environment, locationId, token;
    private int responseCode;
    private JSONObject payload;
    private OkVerifyAsyncTaskHandler okVerifyAsyncTaskHandler;

    public OkVerifyStop(OkVerifyAsyncTaskHandler okVerifyAsyncTaskHandler, JSONObject payload,
                        String environment, String locationId, String token) {
        this.okVerifyAsyncTaskHandler = okVerifyAsyncTaskHandler;
        this.environment = environment;
        this.locationId = locationId;
        this.token = token;
        this.payload = payload;
    }

    @Override
    protected String doInBackground(Void... params) {
        String results = null;
        try {
            String urlString;
            if (environment.equalsIgnoreCase(OkHiMode.PROD)) {
                urlString = "https://api.okhi.io/v5/locations/"+locationId+"/verifications";
            } else if (environment.equalsIgnoreCase(OkHiMode.SANDBOX)) {
                urlString = "https://sandbox-api.okhi.io/v5/locations/"+locationId+"/verifications";
            } else {
                urlString = "https://api.okhi.io/v5/locations/"+locationId+"/verifications";
            }

            OkHttpClient.Builder b = new OkHttpClient.Builder();
            b.connectTimeout(10, TimeUnit.SECONDS);
            b.readTimeout(10, TimeUnit.SECONDS);
            b.writeTimeout(10, TimeUnit.SECONDS);
            OkHttpClient client = b.build();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, payload.toString());
            Request request = new Request.Builder()
                    .url(urlString)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            results = responseBody.string();
            responseCode = response.code();

        } catch (UnsupportedEncodingException e) {
            displayLog("unsupported encoding exception " + e.toString());
        } catch (IOException io) {
            displayLog("io exception " + io.toString());
        } catch (IllegalArgumentException iae) {
            displayLog("illegal argument exception " + iae.toString());
        }
        return results;
    }

    @Override
    protected void onPostExecute(String result) {
        if ((200 <= responseCode) && (responseCode < 300)) {
            okVerifyAsyncTaskHandler.onSuccess(result);
        } else {
            okVerifyAsyncTaskHandler.onError(new OkHiException(""+responseCode, result));
        }
    }

    private void displayLog(String log) {
        Log.i("OkVerifyStop", log);
    }
}
