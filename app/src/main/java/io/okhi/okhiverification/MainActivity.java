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

    // 1. define your app context: OkHiMode.DEV | OkHiMode.SANDBOX | OkHiMode.PROD - dev will be removed in an update
    private static final OkHiAppContext context = new OkHiAppContext.Builder(Secret.OKHI_DEV_MODE).build();

    // 2. initialise OkHiAuth with your branchId, clientKey and your apps context
    private static final OkHiAuth auth = new OkHiAuth.Builder(Secret.OKHI_BRANCH_ID, Secret.OKHI_CLIENT_KEY)
            .withContext(context)
            .build();

    // 3. Create the OkVerify object variable
    private OkVerify okVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 4. initialise the okverify library onCreate, to enable requesting of permissions and services
        okVerify = new OkVerify.Builder(auth, this).build();

        // 7. should be invoked one time on app start
        OkVerify.init(getApplicationContext());
    }


    // 8. define a method you'll use to start okverify
    public void startOkVerify() {
        boolean preFlightCheck = preFlightCheck();

        // 17. if all the checks pass attempt to start okverify
        if (preFlightCheck) {
           // 18. create a okhi location
            OkHiLocation location = new OkHiLocation("myLocationId", -1.313275, 36.842388);
            // 19. create an okhi user
            OkHiUser user = new OkHiUser.Builder(Secret.OKHI_TEST_PHONE_NUMBER)
                    .withFirstName("Julius")
                    .withLastName("Kiano")
                    .build();

            // 20. start verification
            okVerify.start(user, location, new OkVerifyCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    showMessage("Successfully started verification for: " + result);
                }
                @Override
                public void onError(OkHiException e) {
                    showMessage("Something went wrong: " + e.getCode());
                }
            });

            // 21. to stop
            // OkVerify.stop(getApplicationContext(), location.getId());
        }
    }

    // 9. define a method you'll use to check if conditions are met to start okverify - this method will be added in the lib on the next update
    private boolean preFlightCheck() {
        // 10. check and request user to enable location services
        if (!OkVerify.isLocationServicesEnabled(getApplicationContext())) {
            okVerify.requestEnableLocationServices(new OkVerifyRequestHandler() {
                @Override
                public void onSuccess() {
                    // 11. user has enabled location services
                    startOkVerify();
                }
                @Override
                public void onError(OkHiException exception) {
                    // 12. location services aren't enabled - handle error
                    showMessage("Location services are required to start verification");
                }
            });
        } else if (!OkVerify.isGooglePlayServicesAvailable(getApplicationContext())) {
            // 13. check and request user to enable google play services
            okVerify.requestEnableGooglePlayServices(new OkVerifyRequestHandler() {
                @Override
                public void onSuccess() {
                    // 14. user has enabled google play services
                    startOkVerify();
                }
                @Override
                public void onError(OkHiException exception) {
                    // 15. location services aren't enabled - handle error
                    showMessage("Google play services are required to start verification");
                }
            });
        } else if (!OkVerify.isLocationPermissionGranted(getApplicationContext())) {
            // 16. check and request user to grant location permission
            okVerify.requestLocationPermission("Hey we need location permissions", "Pretty please..", new OkVerifyRequestHandler() {
                @Override
                public void onSuccess() {
                    startOkVerify();
                }
                @Override
                public void onError(OkHiException exception) {
                    showMessage("Location permissions are required to start verification");
                }
            });
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

        // 5. pass permission results to okverify
        okVerify.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 6. pass activity results results to okverify
        okVerify.onActivityResult(requestCode, resultCode, data);
    }

    public void handleButtonTap(View view) {
        startOkVerify();
    }
}
