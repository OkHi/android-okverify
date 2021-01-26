package io.okhi.android_okverify.models;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.okhi.android_core.models.OkHiException;
import io.okhi.android_core.models.OkHiMode;
import io.okhi.android_okverify.interfaces.OkVerifyAsyncTaskHandler;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static io.okhi.android_okverify.models.Constant.DEV_BASE_URL;
import static io.okhi.android_okverify.models.Constant.PROD_BASE_URL;
import static io.okhi.android_okverify.models.Constant.SANDBOX_BASE_URL;
import static io.okhi.android_okverify.models.Constant.STOP_ENDPOINT_LOCATIONS;
import static io.okhi.android_okverify.models.Constant.STOP_ENDPOINT_VERIFICATIONS;
import static io.okhi.android_okverify.models.OkVerifyUtil.getHeaders;
import static io.okhi.android_okverify.models.OkVerifyUtil.getHttpClient;

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
                urlString = PROD_BASE_URL+STOP_ENDPOINT_LOCATIONS+locationId+STOP_ENDPOINT_VERIFICATIONS;
            } else if (environment.equalsIgnoreCase(OkHiMode.SANDBOX)) {
                urlString = SANDBOX_BASE_URL+STOP_ENDPOINT_LOCATIONS+locationId+STOP_ENDPOINT_VERIFICATIONS;
            } else {
                urlString = DEV_BASE_URL+STOP_ENDPOINT_LOCATIONS+locationId+STOP_ENDPOINT_VERIFICATIONS;
            }

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, payload.toString());
            Request request = new Request.Builder()
                    .url(urlString)
                    .patch(body)
                    .headers(getHeaders(token, "Bearer "))
                    .build();
            Response response = getHttpClient().newCall(request).execute();
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
        } else if (responseCode == 404) {
            okVerifyAsyncTaskHandler.onError(new OkHiException("BAD_REQUEST", "Invalid location ID"));
        } else {
            okVerifyAsyncTaskHandler.onError(new OkHiException(OkHiException.UNKNOWN_ERROR_CODE, result));
        }
    }

    private void displayLog(String log) {
        Log.i("OkVerifyStop", log);
    }
}
