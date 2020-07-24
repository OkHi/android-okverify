package io.okhi.android_okverify.models;

import io.okhi.android_core.models.OkHiAccessScope;

public class Constant {
    private static String API_VERSION = "/v5";
    public static String DEV_BASE_URL = "https://dev-api.okhi.io" + API_VERSION;
    public static String SANDBOX_BASE_URL = "https://sandbox-api.okhi.io" + API_VERSION;
    public static String PROD_BASE_URL = "https://api.okhi.io" + API_VERSION;
    public static String TRANSIT_ENDPOINT = "/users/transits";
    public static String TRANSIT_CONFIG_ENDPOINT = "/verify/config";
    public static String[] OKVERIFY_SCOPES = {OkHiAccessScope.VERIFY};
}
