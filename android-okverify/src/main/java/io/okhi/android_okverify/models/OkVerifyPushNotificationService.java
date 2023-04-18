package io.okhi.android_okverify.models;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.okhi.android_background_geofencing.BackgroundGeofencing;
import io.okhi.android_background_geofencing.database.BackgroundGeofencingDB;
import io.okhi.android_background_geofencing.models.BackgroundGeofence;
import io.okhi.android_core.models.OkHiAppContext;
import io.okhi.android_core.models.OkHiException;
import io.okhi.android_core.models.OkPreference;
import io.okhi.android_okverify.OkVerify;
import io.okhi.android_okverify.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import receivers.ActionButtonNotificationReceiver;

public class OkVerifyPushNotificationService {
  public static String TAG = "OkHiPushNotificationService";
  private static String NOTIFICATION_CHANNEL_ID = "okhi_verification";
  private static String NOTIFICATION_CHANNEL_NAME = "OkHi Verification";
  private static String NOTIFICATION_CHANNEL_DESC = "Address verification alerts";
  private static int NOTIFICATION_REQUEST_CODE = 25;
  private static int NOTIFICATION_ID = 25;

  public static void saveFCMToken(String baseUrl, String authToken, String fcmToken, String locationId) {
    if (baseUrl == null || authToken == null || fcmToken == null || locationId == null) {
      return;
    }
    JSONObject payload = new JSONObject();
    String url = baseUrl+ "/" + locationId + "/verifications/start";
    try {
      payload.put("push_notification_token", fcmToken);
    } catch (Exception e) {
      e.printStackTrace();
    }
    OkHttpClient client = OkVerifyUtil.getHttpClient();
    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), payload.toString());
    Headers headers = OkVerifyUtil.getHeaders("Bearer " + authToken);
    Request.Builder requestBuild = new Request.Builder();
    requestBuild.post(requestBody);
    requestBuild.url(url);
    requestBuild.headers(headers);
    Request request = requestBuild.build();
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        e.printStackTrace();
      }
      @Override
      public void onResponse(Call call, Response response) throws IOException {
        Log.v(TAG, "Sync result: " + response.isSuccessful());
        response.close();
      }
    });
  }

  public static void onNewToken(String token, Context context) {
    String env = OkHiAppContext.getEnv(context);
    if (env == null) return;
    String baseUrl = env.equals("prod") ? Constant.PROD_BASE_URL : env.equals("dev") ? Constant.DEV_BASE_URL : Constant.SANDBOX_BASE_URL;
    String url = baseUrl + Constant.PUSH_NOTIFICATION_UPDATE_ENDPOINT;
    if (token == null) return;
    try {
      JSONObject payload = new JSONObject();
      String authToken = OkPreference.getItem("okhi:recent:token", context);
      if (authToken == null) return;
      Headers headers = OkVerifyUtil.getHeaders("Bearer " + authToken);
      ArrayList<BackgroundGeofence> geofences =  BackgroundGeofencingDB.getAllGeofences(context);
      if (geofences.size() < 1) return;
      JSONArray ids = new JSONArray();
      for(BackgroundGeofence geofence: geofences) {
        ids.put(geofence.getId());
      }
      payload.put("location_ids", ids);
      payload.put("push_notification_token", token);
      OkHttpClient client = new OkHttpClient.Builder().build();
      RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), payload.toString());
      Request.Builder requestBuild = new Request.Builder();
      requestBuild.patch(requestBody);
      requestBuild.url(url);
      requestBuild.headers(headers);
      Request request = requestBuild.build();
      client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          e.printStackTrace();
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
          Log.v(TAG, "FCM: " + response);
          response.close();
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void onMessageReceived(Context context) {
    BackgroundGeofencing.triggerGeofenceEvents(context);
    restartForegroundService(context);
  }

  private static void restartForegroundService(Context context) {
    boolean isForegroundServiceRunning = OkVerify.isForegroundServiceRunning(context);
    if (isForegroundServiceRunning) {
      Log.d(TAG, "Foreground service running, no need for restart");
      return;
    }
    Log.d(TAG, "Foreground service not running, attempting restart..");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      try {
        OkVerify.startForegroundService(context);
        Log.d(TAG, "Foreground service restarted successfully.");
      } catch (OkHiException e) {
        Log.d(TAG, "Foreground service restart failed. Unknown error");
        e.printStackTrace();
      } catch (Exception exception) {
        Log.d(TAG, "Foreground service restart failed. User interaction required.");
        showNotification(context);
      }
    } else {
      try {
        OkVerify.startForegroundService(context);
        Log.d(TAG, "Foreground service restarted successfully.");
      } catch (OkHiException e) {
        e.printStackTrace();
      }
    }
  }

  private static void createNotificationChannel(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
      channel.setDescription(NOTIFICATION_CHANNEL_DESC);
      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  public static String getApplicationName(Context context) {
    ApplicationInfo applicationInfo = context.getApplicationInfo();
    int stringId = applicationInfo.labelRes;
    return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private static void showNotification(Context context) {
    createNotificationChannel(context);
    Intent intent = new Intent(context, ActionButtonNotificationReceiver.class);
    PendingIntent pendingIntent = null;
    pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_stat_name)
      .setContentTitle(getApplicationName(context) + " address verification stopped")
      .setContentText("Tap \"Continue\" to resume verification now.")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setAutoCancel(false)
      .setOngoing(true)
      .addAction(0, "Continue", pendingIntent);
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    notificationManager.notify(NOTIFICATION_ID, builder.build());
  }

  public static void clearNotification(Context context) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    notificationManager.cancel(NOTIFICATION_ID);
  }

}
