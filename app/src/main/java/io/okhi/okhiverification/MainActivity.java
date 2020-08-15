package io.okhi.okhiverification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import io.okhi.android_core.models.OkHiAppContext;
import io.okhi.android_core.models.OkHiAuth;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_core.models.OkHiLocation;
import io.okhi.android_core.models.OkHiUser;
import io.okhi.android_okverify.OkVerify;
import io.okhi.android_okverify.interfaces.OkVerifyCallback;
import io.okhi.android_okverify.interfaces.OkVerifyRequestHandler;

public class MainActivity extends AppCompatActivity {

    // Define an OkHiLocation that'll be used for verification
    OkHiLocation workAddress = new OkHiLocation("NmUHW84306", -1.313339237582541, 36.842414181487776);

    // Define your app context: OkHiMode.DEV | OkHiMode.SANDBOX | OkHiMode.PROD - dev will be removed in an update
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

        // Initialise the okverify library onCreate, to enable requesting of permissions and services
        okVerify = new OkVerify.Builder(auth, this).build();

        // Should be invoked one time on app start
        OkVerify.init(getApplicationContext());
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
                }

                @Override
                public void onError(OkHiException e) {
                    showMessage("Something went wrong: " + e.getCode());
                }
            });
        }
    }

    private void stopAddressVerification() {
        OkVerify.stop(getApplicationContext(), workAddress.getId());
    }

    class Handler implements OkVerifyRequestHandler {
        @Override
        public void onSuccess() {
            startAddressVerification();
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
        if (!OkVerify.isLocationServicesEnabled(getApplicationContext())) {
            okVerify.requestEnableLocationServices(requestHandler);
        } else if (!OkVerify.isGooglePlayServicesAvailable(getApplicationContext())) {
            // Check and request user to enable google play services
            okVerify.requestEnableGooglePlayServices(requestHandler);
        } else if (!OkVerify.isLocationPermissionGranted(getApplicationContext())) {
            // Check and request user to grant location permission
            okVerify.requestLocationPermission("Hey we need location permissions", "Pretty please..", requestHandler);
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
        okVerify.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass activity results results to okverify
        okVerify.onActivityResult(requestCode, resultCode, data);
    }

    public void handleButtonTap(View view) {
        startAddressVerification();
    }
}
