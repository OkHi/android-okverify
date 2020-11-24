package io.okhi.android_okverify;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import io.okhi.android_background_geofencing.BackgroundGeofencing;
import io.okhi.android_background_geofencing.models.BackgroundGeofence;
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
        OkVerifyGeofence.getGeofence(context, authorizationToken, auth.getAccessToken(), TRANSIT_CONFIG_URL, TRANSIT_URL, new OkVerifyAsyncTaskHandler<OkVerifyGeofence>() {
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

    public static void stop(Context context, String locationId) {
        BackgroundGeofence.stop(context, locationId);
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
}
