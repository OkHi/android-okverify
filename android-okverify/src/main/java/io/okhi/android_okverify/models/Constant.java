package io.okhi.android_okverify.models;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.okhi.android_background_geofencing.models.BackgroundGeofence;
import io.okhi.android_core.models.OkHiAccessScope;

public class Constant {
    public static String OkHi_DEV_MODE = "dev";
    // library info
    private static String LIBRARY_NAME = "okverifyMobileAndroid";
    private static String LIBRARY_VERSION = "v1.0.0";

    private static String API_VERSION = "/v5";
    public static String DEV_BASE_URL = "https://dev-api.okhi.io" + API_VERSION;
    public static String SANDBOX_BASE_URL = "https://sandbox-api.okhi.io" + API_VERSION;
    public static String PROD_BASE_URL = "https://api.okhi.io" + API_VERSION;
    public static String TRANSIT_ENDPOINT = "/users/transits";
    public static String TRANSIT_CONFIG_ENDPOINT = "/verify/config";
    public static String[] OKVERIFY_SCOPES = {OkHiAccessScope.VERIFY};
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
    static final int DEFAULT_TRANSIT_TIMEOUT = 30000;

    static JSONObject getLibraryMeta() {
        JSONObject meta = new JSONObject();
        JSONObject lib = new JSONObject();
        try {
            lib.put("name", LIBRARY_NAME);
            lib.put("version", LIBRARY_VERSION);
            meta.put("lib", lib);
            return meta;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return meta;
        }
    };
}
