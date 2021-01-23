package io.okhi.android_okverify;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import io.okhi.android_background_geofencing.BackgroundGeofencing;
import io.okhi.android_background_geofencing.models.BackgroundGeofence;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingException;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingNotification;
import io.okhi.android_core.OkHiCore;
import io.okhi.android_core.interfaces.OkHiRequestHandler;
import io.okhi.android_core.models.OkHiAuth;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_core.models.OkHiLocation;
import io.okhi.android_core.models.OkHiMode;
import io.okhi.android_core.models.OkHiUser;
import io.okhi.android_okverify.interfaces.OkVerifyAsyncTaskHandler;
import io.okhi.android_okverify.interfaces.OkVerifyCallback;
import io.okhi.android_okverify.models.Constant;
import io.okhi.android_okverify.models.OkHiNotification;
import io.okhi.android_okverify.models.OkVerifyGeofence;
import io.okhi.android_okverify.models.OkVerifyStop;

public class OkVerify extends OkHiCore {
    private final Activity activity;
    private final OkHiAuth auth;
    private final String TRANSIT_URL;
    private final String TRANSIT_CONFIG_URL;

    private OkVerify(@NonNull Builder builder) {
        super(builder.auth);
        this.activity = builder.activity;
        this.auth = builder.auth;
        if (builder.auth.getContext().getMode().equals(Constant.OkHi_DEV_MODE)) {
            TRANSIT_URL = Constant.DEV_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.DEV_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
        } else if (builder.auth.getContext().getMode().equals(OkHiMode.PROD)) {
            TRANSIT_URL = Constant.PROD_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.PROD_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
        } else {
            TRANSIT_URL = Constant.SANDBOX_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.SANDBOX_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
        }
    }

    public static class Builder {
        private final OkHiAuth auth;
        private final Activity activity;

        public Builder(@NonNull OkHiAuth auth, Activity activity) {
            this.auth = auth;
            this.activity = activity;
        }

        public OkVerify build() {
            return new OkVerify(this);
        }
    }

    public void start(OkHiUser user, final OkHiLocation location, final OkVerifyCallback<String> handler) {
        if (location.getId() == null) {
            handler.onError(new OkHiException(OkHiException.NETWORK_ERROR_CODE, "Address failed to be created successfully. Missing location id"));
            return;
        }
        anonymousSignWithPhoneNumber(user.getPhone(), Constant.OKVERIFY_SCOPES, new OkHiRequestHandler<String>() {
            @Override
            public void onResult(String authorizationToken) {
                start(activity.getApplicationContext(), authorizationToken, location, handler);
            }

            @Override
            public void onError(OkHiException exception) {
                handler.onError(exception);
            }
        });
    }

    private void start(final Context context, final String authorizationToken, final OkHiLocation location, final OkVerifyCallback<String> handler) {
        OkVerifyGeofence.getGeofence(context, authorizationToken, auth.getAccessToken(),
                TRANSIT_CONFIG_URL, TRANSIT_URL, new OkVerifyAsyncTaskHandler<OkVerifyGeofence>() {
            @Override
            public void onSuccess(OkVerifyGeofence geofence) {
                start(context, geofence, location, handler);
            }

            @Override
            public void onError(OkHiException exception) {
                handler.onError(exception);
            }
        });
    }

    private void start(Context context, OkVerifyGeofence geofence, OkHiLocation location, final OkVerifyCallback<String> handler) {
        geofence.start(context, location.getId(), location.getLat(), location.getLon(), new OkVerifyAsyncTaskHandler<String>() {
            @Override
            public void onSuccess(String result) {
                handler.onSuccess(result);
            }

            @Override
            public void onError(OkHiException exception) {
                handler.onError(exception);
            }
        });
    }

    public void stop(@NonNull final Context context, @NonNull final OkHiUser user,
                     @NonNull final String locationId, @NonNull final OkVerifyCallback<String> handler)  {

        anonymousSignWithPhoneNumber(user.getPhone(), Constant.OKVERIFY_SCOPES,
                new OkHiRequestHandler<String>() {
            @Override
            public void onResult(String authorizationToken) {
                OkVerifyAsyncTaskHandler okVerifyAsyncTaskHandler = new OkVerifyAsyncTaskHandler() {
                    @Override
                    public void onSuccess(Object result) {
                        BackgroundGeofence.stop(context, locationId);
                        handler.onSuccess((String) result);
                    }

                    @Override
                    public void onError(OkHiException exception) {
                        handler.onError(exception);
                    }
                };
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("state", "stop");
                    OkVerifyStop okVerifyStop = new OkVerifyStop(okVerifyAsyncTaskHandler, payload,
                            auth.getContext().getMode(), locationId, authorizationToken);
                    okVerifyStop.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                catch (JSONException jsonException){
                    handler.onError(new OkHiException("Unknown Error", jsonException.getMessage()));
                }
            }

            @Override
            public void onError(OkHiException exception) {
                handler.onError(exception);
            }
        });

    }

    public static void init(Context context) {
        BackgroundGeofencing.init(context, null);
    }

    public static void init(Context context, OkHiNotification notification) {
        BackgroundGeofencing.init(context, new BackgroundGeofencingNotification(
                notification.getTitle(),
                notification.getText(),
                notification.getChannelId(),
                notification.getChannelName(),
                notification.getChannelDescription(),
                notification.getChannelImportance(),
                notification.getIcon(),
                notification.getNotificationId(),
                notification.getNotificationRequestCode()
        ));
    }

    public static void startForegroundService(Context context) throws OkHiException {
        try {
            BackgroundGeofencing.startForegroundService(context);
        } catch (BackgroundGeofencingException e) {
            throw new OkHiException(e.getCode(), Objects.requireNonNull(e.getMessage()));
        }
    }

    public static void stopForegroundService(Context context) {
        BackgroundGeofencing.stopForegroundService(context);
    }

    public static boolean isForegroundServiceRunning(Context context) {
        return BackgroundGeofencing.isForegroundServiceRunning(context);
    }
}
