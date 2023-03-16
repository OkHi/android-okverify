package io.okhi.android_okverify.receivers;

import static io.okhi.android_okverify.models.Constant.PUSH_NOTIFICATION_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import io.okhi.android_core.models.OkHiException;
import io.okhi.android_okverify.OkVerify;

public class PushButtonNotificationReceiver extends BroadcastReceiver {
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
    clearNotification(context);
  }

  public static void clearNotification(Context context) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    notificationManager.cancel(PUSH_NOTIFICATION_ID);
  }
}
