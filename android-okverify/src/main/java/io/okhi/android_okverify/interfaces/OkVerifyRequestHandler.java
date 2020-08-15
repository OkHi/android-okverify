package io.okhi.android_okverify.interfaces;

import io.okhi.android_core.models.OkHiException;

public interface OkVerifyRequestHandler {
    void onSuccess();

    void onError(OkHiException exception);
}
