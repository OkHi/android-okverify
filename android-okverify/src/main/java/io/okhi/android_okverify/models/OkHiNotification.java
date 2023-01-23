package io.okhi.android_okverify.models;

import androidx.annotation.NonNull;

public class OkHiNotification {
  private String title;
  private String text;
  private int channelImportance;
  private String channelId;
  private String channelName;
  private String channelDescription;
  private int notificationId;
  private int notificationRequestCode;

  public OkHiNotification(@NonNull String title, @NonNull String text, @NonNull String channelId, @NonNull String channelName, @NonNull String channelDescription, int channelImportance) {
    this.title = title;
    this.text = text;
    this.channelId = channelId;
    this.channelName = channelName;
    this.channelDescription = channelDescription;
    this.channelImportance = channelImportance;
    this.notificationId = 1;
    this.notificationRequestCode = 2;
  }

  public OkHiNotification(@NonNull String title, @NonNull String text, @NonNull String channelId, @NonNull String channelName, @NonNull String channelDescription, int channelImportance, int notificationId, int notificationRequestCode) {
    this.title = title;
    this.text = text;
    this.channelId = channelId;
    this.channelName = channelName;
    this.channelDescription = channelDescription;
    this.channelImportance = channelImportance;
    this.notificationId = notificationId;
    this.notificationRequestCode = notificationRequestCode;
  }

  public int getChannelImportance() {
    return channelImportance;
  }

  public String getChannelDescription() {
    return channelDescription;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getChannelName() {
    return channelName;
  }

  public String getText() {
    return text;
  }

  public String getTitle() {
    return title;
  }

  public int getNotificationId() {
    return notificationId;
  }

  public int getNotificationRequestCode() {
    return notificationRequestCode;
  }
}
