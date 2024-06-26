package io.okhi.android_okverify.models;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.okhi.android_background_geofencing.models.BackgroundGeofence;
import io.okhi.android_core.models.OkHiAccessScope;

public class Constant {
    public static final String OkHi_DEV_MODE = "dev";

    private static final String API_VERSION = "/v5";
    public static final String DEV_BASE_URL = "https://dev-api.okhi.io" + API_VERSION;
    public static final String SANDBOX_BASE_URL = "https://sandbox-api.okhi.io" + API_VERSION;
    public static final String PROD_BASE_URL = "https://api.okhi.io" + API_VERSION;

    public static final String START_VERIFICATION_ENDPOINT = "/locations";

    public static final String PUSH_NOTIFICATION_UPDATE_ENDPOINT = "/users/push-notification-token";
    public static final String TRANSIT_ENDPOINT = "/users/transits";
    public static final String TRANSIT_CONFIG_ENDPOINT = "/verify/config";
    public static final String DEVICE_PING_ENDPOINT = "/devices/ping";
    public static final String STOP_ENDPOINT_PREFIX = "/locations";
    public static final String STOP_ENDPOINT_SURFIX = "/verifications";

    public static final String[] OKVERIFY_SCOPES = {OkHiAccessScope.VERIFY};
    static final long TIME_OUT = 30;
    static final TimeUnit TIME_OUT_UNIT = TimeUnit.SECONDS;

    // defaults
    static final int DEFAULT_GEOFENCE_RADIUS = 500;
    static final int DEFAULT_GEOFENCE_EXPIRATION = -1;
    static final int DEFAULT_GEOFENCE_NOTIFICATION_RESPONSIVENESS = 900000;
    static final int DEFAULT_GEOFENCE_LOITERING_DELAY = 1800000;
    static final boolean DEFAULT_GEOFENCE_REGISTER_ON_DEVICE_RESTART = true;
    static final int DEFAULT_INITIAL_TRIGGER_TRANSITION_TYPES = BackgroundGeofence.INITIAL_TRIGGER_ENTER | BackgroundGeofence.INITIAL_TRIGGER_EXIT | BackgroundGeofence.INITIAL_TRIGGER_DWELL;
    static final int DEFAULT_TRANSITION_TYPES = BackgroundGeofence.TRANSITION_ENTER | BackgroundGeofence.INITIAL_TRIGGER_EXIT | BackgroundGeofence.TRANSITION_DWELL;
    static final int DEFAULT_TRANSIT_TIMEOUT = 3000;

    public static final String OKPR_REGISTERED_GEOFENCES = "registered_geofences";

    static JSONObject getLibraryMeta() {
        JSONObject meta = new JSONObject();
        JSONObject lib = new JSONObject();
        try {
            // library info
            String LIBRARY_NAME = "okverifyMobileAndroid";
            String LIBRARY_VERSION = "1.9.44";
            lib.put("name", LIBRARY_NAME);
            lib.put("version", LIBRARY_VERSION);
            meta.put("lib", lib);
            return meta;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return meta;
    }
}
