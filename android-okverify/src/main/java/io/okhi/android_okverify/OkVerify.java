package io.okhi.android_okverify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.Objects;

import io.okhi.android_background_geofencing.interfaces.RequestHandler;
import io.okhi.android_background_geofencing.models.BackgroundGeofence;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingException;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingLocationService;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingPermissionService;
import io.okhi.android_background_geofencing.models.BackgroundGeofencingPlayService;
import io.okhi.android_core.OkHiCore;
import io.okhi.android_core.models.OkHiAuth;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_okverify.interfaces.OkVerifyRequestHandler;

public class OkVerify extends OkHiCore {
    private Activity activity;
    private BackgroundGeofencingPermissionService permissionService;
    private BackgroundGeofencingPlayService playService;
    private BackgroundGeofencingLocationService locationService;

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
    }

    public static class Builder {
        private OkHiAuth auth;
        private Activity activity;
        public Builder(@NonNull OkHiAuth auth) {
            this.auth = auth;
        }
        public Builder setActivity(@NonNull Activity activity) {
            this.activity = activity;
            return this;
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
}
