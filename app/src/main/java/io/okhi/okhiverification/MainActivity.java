package io.okhi.okhiverification;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.okhi.android_core.OkHi;
import io.okhi.android_core.interfaces.OkHiRequestHandler;
import io.okhi.android_core.models.OkHiAppContext;
import io.okhi.android_core.models.OkHiAuth;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_core.models.OkHiLocation;
import io.okhi.android_core.models.OkHiUser;
import io.okhi.android_okverify.OkVerify;
import io.okhi.android_okverify.interfaces.OkVerifyCallback;
import io.okhi.android_okverify.models.OkHiNotification;

public class MainActivity extends AppCompatActivity {

    // Define an OkHi class variable to be used for managing permissions and various other services
    OkHi okhi;

    // Define an OkHiLocation that'll be used for verification
    final OkHiLocation workAddress = new OkHiLocation("NmUHW84306", -1.313339237582541, 36.842414181487776);

    // Define your app context: OkHiMode.SANDBOX | OkHiMode.PROD - dev will be removed in an update
    private static final OkHiAppContext context = new OkHiAppContext.Builder(Secret.OKHI_DEV_MODE).build();

    // Initialise OkHiAuth with your branchId, clientKey and your apps context
    private static final OkHiAuth auth = new OkHiAuth.Builder(Secret.OKHI_BRANCH_ID, Secret.OKHI_CLIENT_KEY)
            .withContext(context)
            .build();

    // Create the OkVerify object variable
    private OkVerify okVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialise OkHi to enable requesting of permissions and services. Must be done onCreate
        okhi = new OkHi(this);

        // Initialise the okverify library. Must be done onCreate
        okVerify = new OkVerify.Builder(auth, this).build();

        // Should be invoked one time on app start.
        // (optional) OkHiNotification, use to start a foreground service to transmit verification signals to OkHi servers
        int importance = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? NotificationManager.IMPORTANCE_DEFAULT : 3;
        OkVerify.init(getApplicationContext(), new OkHiNotification(
                "Verifying your address",
                "We're currently verifying your address. This won't take long",
                "OkHi",
                "OkHi Address Verification",
                "Alerts related to any address verification updates",
                importance,
                R.mipmap.ic_launcher,
                1, // notificationId
                2 // notification request code
        ));
    }


    // Define a method you'll use to start okverify
    private void startAddressVerification() {
        boolean canStartAddressVerification = canStartAddressVerification();

        // If all the checks pass attempt to start okverify
        if (canStartAddressVerification) {
            // Create an okhi user
            OkHiUser user = new OkHiUser.Builder(Secret.OKHI_TEST_PHONE_NUMBER)
                    .withFirstName("Julius")
                    .withLastName("Kiano")
                    .build();

            // Start verification
            okVerify.start(user, workAddress, new OkVerifyCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    showMessage("Successfully started verification for: " + result);
                    startForegroundVerification();
                }

                @Override
                public void onError(OkHiException e) {
                    showMessage("Something went wrong: " + e.getCode());
                }
            });
        }
    }

    class Handler implements OkHiRequestHandler<Boolean> {
        @Override
        public void onResult(Boolean result) {
            if (result) startAddressVerification();
        }
        @Override
        public void onError(OkHiException exception) {
            showMessage(exception.getMessage());
        }
    }

    // Define a method you'll use to check if conditions are met to start okverify - this method will be added in the lib on the next update
    private boolean canStartAddressVerification() {
        Handler requestHandler = new Handler();
        // Check and request user to enable location services
        if (!OkHi.isLocationServicesEnabled(getApplicationContext())) {
            okhi.requestEnableLocationServices(requestHandler);
        } else if (!OkHi.isGooglePlayServicesAvailable(getApplicationContext())) {
            // Check and request user to enable google play services
            okhi.requestEnableGooglePlayServices(requestHandler);
        } else if (!OkHi.isBackgroundLocationPermissionGranted(getApplicationContext())) {
            // Check and request user to grant location permission
            okhi.requestBackgroundLocationPermission("Hey we need  background location permissions", "Pretty please..", requestHandler);
        } else {
            return true;
        }
        return false;
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Pass permission results to okverify
        okhi.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass activity results results to okverify
        okhi.onActivityResult(requestCode, resultCode, data);
    }

    public void handleButtonTap(View view) {
        startAddressVerification();
    }

    public void stopAddressVerification(View view) {
        OkVerify.stop(getApplicationContext(), workAddress.getId());
    }

    private void startForegroundVerification() {
        try {
            // start a foreground service that'll improve the stability and reliability of verification signals
            OkVerify.startForegroundService(getApplicationContext());
        } catch (OkHiException e) {
            e.printStackTrace();
        }
    }

    private void stopForegroundVerification() {
        // stops the running foreground service
        OkVerify.stopForegroundService(getApplicationContext());
    }

    private boolean checkForegroundService() {
        // checks if the foreground service is running
        return OkVerify.isForegroundServiceRunning(getApplicationContext());
    }
}
