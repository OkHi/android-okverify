package io.okhi.android_okverify.models;

import java.util.Collections;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

public class OkVerifyUtil {

    public static OkHttpClient getHttpClient() {
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .supportsTlsExtensions(true)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
                .build();
        return new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(spec))
                .connectTimeout(Constant.TIME_OUT, Constant.TIME_OUT_UNIT)
                .writeTimeout(Constant.TIME_OUT, Constant.TIME_OUT_UNIT)
                .readTimeout(Constant.TIME_OUT, Constant.TIME_OUT_UNIT)
                .build();
    }

    public static Headers getHeaders(String accessToken, String prefix) {
        Headers.Builder builder = new Headers.Builder();
        builder.add("Authorization", prefix == null || prefix.isEmpty() ?  "Bearer " + accessToken : prefix + accessToken);
        return builder.build();
    }

    public static Headers getHeaders(String accessToken) {
        Headers.Builder builder = new Headers.Builder();
        builder.add("Authorization", accessToken);
        return builder.build();
    }
}
