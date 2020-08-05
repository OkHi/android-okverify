package io.okhi.android_okverify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.Objects;

import io.okhi.android_background_geofencing.BackgroundGeofencing;
import io.okhi.android_background_geofencing.interfaces.RequestHandler;
import io.okhi.android_background_geofencing.models.BackgroundGeofence;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingException;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingLocationService;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingPermissionService;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingPlayService;
import io.okhi.android_core.OkHiCore;
import io.okhi.android_core.interfaces.OkHiSignInRequestHandler;
import io.okhi.android_core.models.OkHiAuth;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_core.models.OkHiLocation;
import io.okhi.android_core.models.OkHiMode;
import io.okhi.android_core.models.OkHiUser;
import io.okhi.android_okverify.interfaces.OkVerifyAsyncTaskHandler;
import io.okhi.android_okverify.interfaces.OkVerifyCallback;
import io.okhi.android_okverify.interfaces.OkVerifyRequestHandler;
import io.okhi.android_okverify.models.Constant;
import io.okhi.android_okverify.models.OkVerifyGeofence;

public class OkVerify extends OkHiCore {
    private Activity activity;
    private OkHiAuth auth;
    private BackgroundGeofencingPermissionService permissionService;
    private BackgroundGeofencingPlayService playService;
    private BackgroundGeofencingLocationService locationService;
    private String TRANSIT_URL;
    private String TRANSIT_CONFIG_URL;

    private static class BackgroundGeofenceRequestHandler implements RequestHandler {
        private OkVerifyRequestHandler requestHandler;
        private BackgroundGeofenceRequestHandler(@NonNull OkVerifyRequestHandler requestHandler) {
            this.requestHandler = requestHandler;
        }
        @Override
        public void onSuccess() {
            requestHandler.onSuccess();
        }
        @Override
        public void onError(BackgroundGeofencingException exception) {
            requestHandler.onError(new OkHiException(exception.getCode(), Objects.requireNonNull(exception.getMessage())));
        }
    }

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
        private OkHiAuth auth;
        private Activity activity;
        public Builder(@NonNull OkHiAuth auth, Activity activity) {
            this.auth = auth;
            this.activity = activity;
        }
        public OkVerify build() {
            return new OkVerify(this);
        }
    }

    public static boolean isLocationPermissionGranted(@NonNull Context context) {
        return BackgroundGeofencingPermissionService.isLocationPermissionGranted(context);
    }

    public static boolean isLocationServicesEnabled(@NonNull Context context) {
        return BackgroundGeofencingLocationService.isLocationServicesEnabled(context);
    }

    public static boolean isGooglePlayServicesAvailable(@NonNull Context context) {
        return BackgroundGeofencingPlayService.isGooglePlayServicesAvailable(context);
    }

    public static void openLocationServicesSettings(@NonNull Activity activity) {
        BackgroundGeofencingLocationService.openLocationServicesSettings(activity);
    }

    public void requestLocationPermission(@NonNull String rationaleTitle, @NonNull String rationaleMessage, final OkVerifyRequestHandler handler) {
        if (activity != null) {
            permissionService = new BackgroundGeofencingPermissionService(activity);
            permissionService.requestLocationPermission(rationaleTitle, rationaleMessage, new BackgroundGeofenceRequestHandler(handler));
        }
    }

    public void requestEnableGooglePlayServices(@NonNull final OkVerifyRequestHandler handler) {
        if (activity != null) {
            playService = new BackgroundGeofencingPlayService(activity);
            playService.requestEnableGooglePlayServices(new BackgroundGeofenceRequestHandler(handler));
        }
    }

    public void requestEnableLocationServices(@NonNull final OkVerifyRequestHandler handler) {
        if (activity != null) {
            locationService = new BackgroundGeofencingLocationService(activity);
            locationService.requestEnableLocationServices(new BackgroundGeofenceRequestHandler(handler));
        }
    }

    public void onRequestPermissionsResult(@NonNull int requestCode, @NonNull String [] permissions, @NonNull int [] grantResults) {
        if (permissionService != null) {
            permissionService.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onActivityResult(@NonNull int requestCode, @NonNull int resultCode, @NonNull Intent data) {
        if (playService != null) {
            playService.onActivityResult(requestCode, resultCode, data);
        }
        if (locationService != null) {
            locationService.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void start(OkHiUser user, final OkHiLocation location, final OkVerifyCallback<String> handler) {
        anonymousSignWithPhoneNumber(user.getPhone(), Constant.OKVERIFY_SCOPES, new OkHiSignInRequestHandler() {
            @Override
            public void onSuccess(String authorizationToken) {
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
        BackgroundGeofencing.init(context);
    }
}
