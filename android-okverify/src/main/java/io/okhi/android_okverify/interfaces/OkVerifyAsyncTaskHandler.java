package io.okhi.android_okverify.interfaces;

import io.okhi.android_core.models.OkHiException;

public interface OkVerifyAsyncTaskHandler<T> {
    void onSuccess(T result);
    void onError(OkHiException exception);
}
