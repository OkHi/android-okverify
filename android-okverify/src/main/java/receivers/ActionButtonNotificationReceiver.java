package receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.okhi.android_core.models.OkHiException;
import io.okhi.android_okverify.OkVerify;
import io.okhi.android_okverify.models.OkVerifyPushNotificationService;

public class ActionButtonNotificationReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    boolean isForegroundServiceRunning = OkVerify.isForegroundServiceRunning(context);
    if (!isForegroundServiceRunning) {
      try {
        OkVerify.startForegroundService(context);
        Toast.makeText(context, "Verification successfully resumed.", Toast.LENGTH_LONG).show();
      } catch (OkHiException e) {
        e.printStackTrace();
      }
    }
    OkVerifyPushNotificationService.clearNotification(context);
  }
}
