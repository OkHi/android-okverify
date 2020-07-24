package io.okhi.okhiverification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import io.okhi.android_core.models.OkHiAuth;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_okverify.OkVerify;
import io.okhi.android_okverify.interfaces.OkVerifyRequestHandler;

public class MainActivity extends AppCompatActivity {

    private static final OkHiAuth auth = new OkHiAuth.Builder("j7yq9XFRWC", "cdbe3121-3a1c-4ecf-9792-6438e76fa740").build();
    private OkVerify okVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        okVerify = new OkVerify.Builder(auth).setActivity(this).build();
        okVerify.requestLocationPermission("We need permissions", "Pretty please", new OkVerifyRequestHandler() {
            @Override
            public void onSuccess() {
                Log.v("App", "we have permissions");
                okVerify.requestEnableLocationServices(new OkVerifyRequestHandler() {
                    @Override
                    public void onSuccess() {
                        Log.v("App", "Service enabled permissions");
                    }

                    @Override
                    public void onError(OkHiException exception) {
                        Log.v("App", "Service disabled: " + exception.getCode());
                    }
                });
            }

            @Override
            public void onError(OkHiException exception) {
                Log.v("App", exception.getCode());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        okVerify.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        okVerify.onActivityResult(requestCode, resultCode, data);
    }
}
