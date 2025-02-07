package com.example.datn;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Date;

public class MqttService extends Service {
    private static final String CHANNEL_ID = "Thongbao_moi";

    private static final String BROKER_URL = "tcp://broker.emqx.io:1883";
    private static final String CLIENT_ID = "";
    private static final String TOPIC = "DATN180125";

    private MqttHandle mqttHandler;
    private String coordinates ;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        mqttHandler = new MqttHandle();
        mqttHandler.connect(BROKER_URL, CLIENT_ID);

        mqttHandler.setCallbackHandler(new MqttHandle.MqttCallbackHandler() {
            @Override
            public void onMessageReceived(String topic, String message) {
                if (message.equals("canhbaongaxe") || message.equals("canhbaomatxe")) {
                    sendNotification(message.equals("canhbaongaxe") ? "Cảnh báo ngã xe" : "Cảnh báo mất xe");

                    // Gửi dữ liệu sang NotifyActivity
                    sendToNotifyActivity(message);
                } else if (message.contains(",")) {
                    String[] parts = message.split(",");
                    if (parts.length == 2) {
                        String latitude = parts[0].trim();
                        String longitude = parts[1].trim();
                            coordinates = latitude + "," + longitude; // Cập nhật tọa độ
                    }
                }
            }
        });

        mqttHandler.subscribe("DATN180125");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mqttHandler.connect(BROKER_URL, CLIENT_ID);  // Sử dụng mqttHandler đã khởi tạo để kết nối
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thongbao_moi",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

private void sendNotification(String content) {
    String channelId = "Thongbao_moi";

    // Kiểm tra nếu tọa độ đã được cập nhật
    String messageContent = content;
    if (coordinates != null) {
        messageContent += "\nTọa độ: " + coordinates;
    }

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
            .setContentTitle("CẢNH BÁO")
            .setContentText(messageContent)
            .setSmallIcon(R.drawable.ic_warming)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    if (notificationManager != null) {
        notificationManager.notify(getNotificationId(), notificationBuilder.build());
    }
}
    private int getNotificationId() {
        return (int) new Date().getTime();
    }

    private void sendToNotifyActivity(String alertMessage) {
        if (coordinates != null) {
            Intent intent = new Intent("Notify");
            intent.putExtra("alert", alertMessage);
            intent.putExtra("coordinates", coordinates);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

}
