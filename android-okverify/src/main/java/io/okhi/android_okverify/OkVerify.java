package io.okhi.android_okverify;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;

import io.okhi.android_background_geofencing.BackgroundGeofencing;
import io.okhi.android_background_geofencing.interfaces.ResultHandler;
import io.okhi.android_background_geofencing.models.BackgroundGeofence;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingException;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingNotification;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingWebHook;
import io.okhi.android_background_geofencing.models.WebHookRequest;
import io.okhi.android_background_geofencing.models.WebHookType;
import io.okhi.android_core.OkHiCore;
import io.okhi.android_core.interfaces.OkHiRequestHandler;
import io.okhi.android_core.models.OkHiAuth;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_core.models.OkHiLocation;
import io.okhi.android_core.models.OkHiMode;
import io.okhi.android_core.models.OkHiUser;
import io.okhi.android_core.models.OkPreference;
import io.okhi.android_okverify.interfaces.OkVerifyAsyncTaskHandler;
import io.okhi.android_okverify.interfaces.OkVerifyCallback;
import io.okhi.android_okverify.models.Constant;
import io.okhi.android_okverify.models.OkHiNotification;
import io.okhi.android_okverify.models.OkVerifyPushNotificationService;
import io.okhi.android_okverify.models.OkVerifyGeofence;

public class OkVerify extends OkHiCore {
    private final Activity activity;
    private final String TRANSIT_URL;
    private final String TRANSIT_CONFIG_URL;
    private final String START_VERIFICATION_URL;
    private boolean withForeground = true;

    private String bearerToken;
    private OkHiUser okHiUser;

    private OkVerify(Builder builder) throws OkHiException {
        super(builder.activity);
        this.activity = builder.activity;
        if (auth.getContext().getMode().equals(Constant.OkHi_DEV_MODE)) {
            TRANSIT_URL = Constant.DEV_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.DEV_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
            START_VERIFICATION_URL = Constant.DEV_BASE_URL + Constant.START_VERIFICATION_ENDPOINT;
        } else if (auth.getContext().getMode().equals(OkHiMode.PROD)) {
            TRANSIT_URL = Constant.PROD_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.PROD_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
            START_VERIFICATION_URL = Constant.PROD_BASE_URL + Constant.START_VERIFICATION_ENDPOINT;
        } else {
            TRANSIT_URL = Constant.SANDBOX_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.SANDBOX_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
            START_VERIFICATION_URL = Constant.SANDBOX_BASE_URL + Constant.START_VERIFICATION_ENDPOINT;
        }
    }

    private OkVerify(Activity activity, OkHiAuth auth) throws OkHiException {
        super(auth);
        this.activity = activity;
        if (auth.getContext().getMode().equals(Constant.OkHi_DEV_MODE)) {
            TRANSIT_URL = Constant.DEV_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.DEV_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
            START_VERIFICATION_URL = Constant.DEV_BASE_URL + Constant.START_VERIFICATION_ENDPOINT;
        } else if (auth.getContext().getMode().equals(OkHiMode.PROD)) {
            TRANSIT_URL = Constant.PROD_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.PROD_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
            START_VERIFICATION_URL = Constant.PROD_BASE_URL + Constant.START_VERIFICATION_ENDPOINT;
        } else {
            TRANSIT_URL = Constant.SANDBOX_BASE_URL + Constant.TRANSIT_ENDPOINT;
            TRANSIT_CONFIG_URL = Constant.SANDBOX_BASE_URL + Constant.TRANSIT_CONFIG_ENDPOINT;
            START_VERIFICATION_URL = Constant.SANDBOX_BASE_URL + Constant.START_VERIFICATION_ENDPOINT;
        }
    }

    public static class Builder {
        private final Activity activity;
        private final OkHiAuth auth;

        public Builder(@NonNull Activity activity) {
            this.activity = activity;
            this.auth = null;
        }

        public Builder(@NonNull Activity activity, OkHiAuth auth) {
            this.activity = activity;
            this.auth = auth;
        }

        public OkVerify build() throws OkHiException {
            if (this.auth == null) {
                return new OkVerify(this);
            }
            return new OkVerify(this.activity, this.auth);
        }
    }

    public void start(OkHiUser user, final OkHiLocation location, final OkVerifyCallback<String> handler) {
        start(user, location, true, handler);
    }

    public void start(OkHiUser user, final OkHiLocation location, boolean withForeground, final OkVerifyCallback<String> handler) {
        ArrayList<BackgroundGeofence> existingGeofences = BackgroundGeofence.getAllGeofences(activity.getApplicationContext());
        Boolean isExistingGeofence = false;
        for(BackgroundGeofence geofence: existingGeofences) {
            if (geofence.getId().equals(location.getId())) {
                isExistingGeofence = true;
                break;
            }
        }
        if (isExistingGeofence) {
            handler.onError(new OkHiException("already_exists", "Verification already started"));
            return;
        }
        this.withForeground = withForeground;
        if (location.getId() == null) {
            handler.onError(new OkHiException(OkHiException.NETWORK_ERROR_CODE, "Address failed to be created successfully. Missing location id"));
            return;
        }
        anonymousSignWithPhoneNumber(user.getPhone(), Constant.OKVERIFY_SCOPES, new OkHiRequestHandler<String>() {
            @Override
            public void onResult(String authorizationToken) {
                bearerToken = authorizationToken;
                okHiUser = user;
                start(activity.getApplicationContext(), authorizationToken, location, handler);
            }

            @Override
            public void onError(OkHiException exception) {
                handler.onError(exception);
            }
        });
    }

    private void start(final Context context, final String authorizationToken, final OkHiLocation location, final OkVerifyCallback<String> handler) {
        OkVerifyGeofence.getGeofence(context, authorizationToken, auth, new OkVerifyAsyncTaskHandler<OkVerifyGeofence>() {
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

    private void start(final Context context, OkVerifyGeofence geofence, OkHiLocation location, final OkVerifyCallback<String> handler) {
        geofence.start(context, location.getId(), location.getLat(), location.getLon(), new OkVerifyAsyncTaskHandler<String>() {
            @Override
            public void onSuccess(String result) {
                if (withForeground) {
                    try {
                        startForegroundService(context);
                    } catch (Exception e) {

                    }
                }
                onVerificationStart(result);
                handler.onSuccess(result);
            }

            @Override
            public void onError(OkHiException exception) {
                handler.onError(exception);
            }
        });
    }

    private void onVerificationStart(String locationId) {
        if (okHiUser != null && bearerToken != null) {
            String token = okHiUser.getFcmPushNotificationToken();
            if (token != null) {
                OkVerifyPushNotificationService.saveFCMToken(START_VERIFICATION_URL, bearerToken, token, locationId);
            }
        }
    }

    public static void stop(Context context, String locationId) {
        initStopWebHook(context, locationId);
        BackgroundGeofence.stop(context, locationId, new ResultHandler<String>() {
            @Override
            public void onSuccess(String result) {
                // no callback implemented
            }
            @Override
            public void onError(BackgroundGeofencingException exception) {
                // no callback implemented
            }
        });
    }

    public static void stop(Context context, String locationId, final OkVerifyCallback<String> callback) {
        initStopWebHook(context, locationId);
        BackgroundGeofence.stop(context, locationId, new ResultHandler<String>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
            }
            @Override
            public void onError(BackgroundGeofencingException exception) {
                callback.onError(new OkHiException(exception.getCode(), exception.getMessage()));
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

    private static void initStopWebHook (Context context, String locationId) {
        BackgroundGeofencingWebHook geofenceWebHook = BackgroundGeofencingWebHook.getWebHook(context, WebHookType.GEOFENCE);
        if (geofenceWebHook == null) return;
        String geofenceUrl = geofenceWebHook.getUrl();
        String stopUrl;
        if (geofenceUrl.contains(Constant.DEV_BASE_URL)) {
            stopUrl = Constant.DEV_BASE_URL + Constant.STOP_ENDPOINT_PREFIX + "/" + locationId +  Constant.STOP_ENDPOINT_SURFIX;
        } else if (geofenceUrl.contains(Constant.SANDBOX_BASE_URL)) {
            stopUrl = Constant.SANDBOX_BASE_URL + Constant.STOP_ENDPOINT_PREFIX + "/" + locationId +  Constant.STOP_ENDPOINT_SURFIX;
        } else {
            stopUrl = Constant.PROD_BASE_URL + Constant.STOP_ENDPOINT_PREFIX + "/" + locationId +  Constant.STOP_ENDPOINT_SURFIX;
        }
        BackgroundGeofencingWebHook stopVerificationWebHook = new BackgroundGeofencingWebHook(
            stopUrl,
            10000,
            geofenceWebHook.getHeadersHashMap(),
            null,
            WebHookType.STOP,
            WebHookRequest.PATCH
        );
        stopVerificationWebHook.save(context);
    }

    public static String fetchRegisteredGeofences(Context context) {
        try {
            return OkPreference.getItem(Constant.OKPR_REGISTERED_GEOFENCES, context);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
