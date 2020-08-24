package io.okhi.android_okverify.models;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import io.okhi.android_background_geofencing.interfaces.RequestHandler;
import io.okhi.android_background_geofencing.models.BackgroundGeofence;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingException;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingWebHook;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_okverify.interfaces.OkVerifyAsyncTaskHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkVerifyGeofence {
    private float radius = Constant.DEFAULT_GEOFENCE_RADIUS;
    private long expiration = Constant.DEFAULT_GEOFENCE_EXPIRATION;
    private int initialTriggerTransitionTypes = Constant.DEFAULT_INITIAL_TRIGGER_TRANSITION_TYPES;
    private int loiteringDelay = Constant.DEFAULT_GEOFENCE_LOITERING_DELAY;
    private int notificationResponsiveness = Constant.DEFAULT_GEOFENCE_NOTIFICATION_RESPONSIVENESS;
    private int transitionTypes = Constant.DEFAULT_TRANSITION_TYPES;
    private boolean registerOnDeviceRestart = Constant.DEFAULT_GEOFENCE_REGISTER_ON_DEVICE_RESTART;

    private OkVerifyGeofence() {
    }

    private OkVerifyGeofence(Context context, ResponseBody responseBody, String transitUrl, String authorizationToken) {
        try {
            JSONObject configuration = responseBody != null ? new JSONObject(responseBody.string()) : new JSONObject();
            radius = configuration.has("radius") ? configuration.getInt("radius") : Constant.DEFAULT_GEOFENCE_RADIUS;
            expiration = configuration.has("expiration") ? configuration.getInt("expiration") : Constant.DEFAULT_GEOFENCE_EXPIRATION;
            notificationResponsiveness = configuration.has("notification_responsiveness") ? configuration.getInt("notification_responsiveness") : Constant.DEFAULT_GEOFENCE_NOTIFICATION_RESPONSIVENESS;
            loiteringDelay = configuration.has("loitering_delay") ? configuration.getInt("loitering_delay") : Constant.DEFAULT_GEOFENCE_LOITERING_DELAY;
            transitionTypes = configuration.has("set_dwell_transition_type") ? Constant.DEFAULT_TRANSITION_TYPES : BackgroundGeofence.TRANSITION_ENTER | BackgroundGeofence.TRANSITION_EXIT;
            registerOnDeviceRestart = configuration.has("register_on_device_restart") ? configuration.getBoolean("register_on_device_restart") : Constant.DEFAULT_GEOFENCE_REGISTER_ON_DEVICE_RESTART;
            initialTriggerTransitionTypes = configuration.has("set_initial_triggers") ? Constant.DEFAULT_INITIAL_TRIGGER_TRANSITION_TYPES : 0;
        } catch (Exception e) {
            // do nothing, if we have an error use defaults
        } finally {
            String authorization = "Bearer " + authorizationToken;
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", authorization);
            BackgroundGeofencingWebHook webHook = new BackgroundGeofencingWebHook(transitUrl, Constant.DEFAULT_TRANSIT_TIMEOUT, headers, Constant.getLibraryMeta());
            webHook.save(context);
        }
    }

    public float getRadius() {
        return radius;
    }

    public long getExpiration() {
        return expiration;
    }

    public int getLoiteringDelay() {
        return loiteringDelay;
    }

    public int getInitialTriggerTransitionTypes() {
        return initialTriggerTransitionTypes;
    }

    public int getNotificationResponsiveness() {
        return notificationResponsiveness;
    }

    public int getTransitionTypes() {
        return transitionTypes;
    }

    public boolean getRegisterOnDeviceRestart() {
        return registerOnDeviceRestart;
    }

    public static void getGeofence(final Context context, final String authorizationToken, String accessToken, String configurationUrl, final String transitUrl, final OkVerifyAsyncTaskHandler<OkVerifyGeofence> handler) {
        getGeofenceConfiguration(configurationUrl, getHeaders(accessToken), new OkVerifyAsyncTaskHandler<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                handler.onSuccess(new OkVerifyGeofence(context, responseBody, transitUrl, authorizationToken));
            }

            @Override
            public void onError(OkHiException exception) {
                handler.onSuccess(new OkVerifyGeofence(context, null, transitUrl, authorizationToken));
            }
        });
    }

    public void start(Context context, final String id, double lat, double lon, final OkVerifyAsyncTaskHandler<String> handler) {
        BackgroundGeofence backgroundGeofence = new BackgroundGeofence.BackgroundGeofenceBuilder(id, lat, lon)
                .setConfiguration(registerOnDeviceRestart)
                .setExpiration(expiration)
                .setInitialTriggerTransitionTypes(initialTriggerTransitionTypes)
                .setLoiteringDelay(loiteringDelay)
                .setNotificationResponsiveness(notificationResponsiveness)
                .setRadius(radius)
                .setTransitionTypes(transitionTypes)
                .build();
        backgroundGeofence.start(context, new RequestHandler() {
            @Override
            public void onSuccess() {
                handler.onSuccess(id);
            }

            @Override
            public void onError(BackgroundGeofencingException exception) {
                handler.onError(new OkHiException(exception.getCode(), Objects.requireNonNull(exception.getMessage())));
            }
        });
    }

    private static void getGeofenceConfiguration(String configurationUrl, Headers headers, final OkVerifyAsyncTaskHandler<ResponseBody> handler) {
        Request request = new Request.Builder().url(configurationUrl).headers(headers).build();
        getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handler.onError(new OkHiException(OkHiException.NETWORK_ERROR_CODE, OkHiException.NETWORK_ERROR_MESSAGE));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    handler.onSuccess(response.body());
                } else {
                    // TODO: create static method that handle response codes in core library
                    handler.onError(new OkHiException(OkHiException.UNKNOWN_ERROR_CODE, OkHiException.UNKNOWN_ERROR_MESSAGE));
                }
            }
        });
    }

    private static OkHttpClient getHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Constant.TIME_OUT, Constant.TIME_OUT_UNIT)
                .writeTimeout(Constant.TIME_OUT, Constant.TIME_OUT_UNIT)
                .readTimeout(Constant.TIME_OUT, Constant.TIME_OUT_UNIT)
                .build();
    }

    private static Headers getHeaders(String accessToken) {
        Headers.Builder builder = new Headers.Builder();
        builder.add("Authorization", accessToken);
        return builder.build();
    }
}
