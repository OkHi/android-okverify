package io.okhi.android_okverify.models;

import androidx.annotation.NonNull;

import io.okhi.android_background_geofencing.models.BackgroundGeofencingNotification;

public class OkHiNotification extends BackgroundGeofencingNotification {
    public OkHiNotification(@NonNull String title, @NonNull String text, @NonNull String channelId, @NonNull String channelName, @NonNull String channelDescription, int channelImportance, int icon) {
        super(title, text, channelId, channelName, channelDescription, channelImportance, icon);
    }
}
