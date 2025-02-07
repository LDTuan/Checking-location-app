package com.example.datn;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Notifi extends AppCompatActivity {

    private ListView ThongBao;
    private ImageView Back;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MqttHandle mqttHandler;
    private NotifiAdapter adapter;
    private List<NotificationItem> notificationList;
    private static final String BROKER_URL = "tcp://broker.emqx.io:1883";
    private static final String CLIENT_ID = "";
    private String deviceID;
    private static final String TAG = "Notifi";
    private String coordinates = "10.762622, 106.660172";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifi);

        ThongBao = findViewById(R.id.lstThongBao);
        Back = findViewById(R.id.imgBack1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mqttHandler = new MqttHandle();
        mqttHandler.connect(BROKER_URL, CLIENT_ID);

        notificationList = new ArrayList<>();
        adapter = new NotifiAdapter(this, notificationList);
        ThongBao.setAdapter((ListAdapter) adapter);

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Notifi.this, Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        registerBroadcastReceiver();
        mqttHandler.setCallbackHandler(new MqttHandle.MqttCallbackHandler() {
            @Override
            public void onMessageReceived(String topic, String message) {
                // Kiểm tra tin nhắn MQTT
                Log.d(TAG, "Received MQTT message: " + message);
            }

        });

        loadDeviceAndSubscribe();
    }

    private void loadDeviceAndSubscribe() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("devices").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                deviceID = documentSnapshot.getString("DeviceId");
                                if (deviceID != null) {
                                    // Subscribe MQTT với DeviceID làm topic
                                    subscribeToTopic(deviceID);
                                    Log.d(TAG, "DeviceID: " + deviceID);  // Log để kiểm tra
                                    // Load notifications from Firestore
                                    loadNotificationsFromFirestore(deviceID);
                                } else {
                                    showToast("DeviceID not found");
                                }
                            } else {
                                showToast("Document does not exist");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToast("Error retrieving DeviceID" + e.getMessage());
                    });
        }
    }

    private void subscribeToTopic(String topic) {
        mqttHandler.subscribe(topic);
    }
    private void registerBroadcastReceiver() {
        // Tạo một BroadcastReceiver
        BroadcastReceiver mqttReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String alert = intent.getStringExtra("alert");
                String coordinates = intent.getStringExtra("coordinates");

                if (alert != null && coordinates != null) {
                    addNotificationToList(alert, coordinates);
                    saveWarningToFirestore(alert, coordinates);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mqttReceiver, new IntentFilter("Notify"));
    }

    private void addNotificationToList(String message, String coordinates) {
        long timestamp = System.currentTimeMillis();
        String formattedTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(new Date(timestamp));

        NotificationItem newItem = new NotificationItem(message, formattedTime, coordinates);
        notificationList.add(newItem);

        Collections.sort(notificationList, new Comparator<NotificationItem>() {
            @Override
            public int compare(NotificationItem o1, NotificationItem o2) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
                try {
                    Date date1 = sdf.parse(o1.getTimestamp());
                    Date date2 = sdf.parse(o2.getTimestamp());

                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        adapter.notifyDataSetChanged();
    }


    private void saveWarningToFirestore(String message, String coordinates) {

        long timestamp = System.currentTimeMillis();
        String formattedTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(new Date(timestamp));

        Map<String, Object> warning = new HashMap<>();
        warning.put("DeviceID", deviceID);
        warning.put("AlertType", message);
        warning.put("TimeReceived", formattedTime);
        warning.put("Coordinates", coordinates);

        db.collection("Notify").add(warning)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Warning successfully saved with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error saving warning", e);
                    }
                });
    }

    private void loadNotificationsFromFirestore(String deviceID) {
        db.collection("Notify")
                .whereEqualTo("DeviceID", deviceID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showToast("No notifications");
                    } else {

                        notificationList.clear();

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String alertType = document.getString("AlertType");
                            String timeReceived = document.getString("TimeReceived");
                            String coordinates = document.getString("Coordinates");

                            if (alertType != null && timeReceived != null && coordinates != null) {
                                notificationList.add(new NotificationItem(alertType, timeReceived, coordinates));
                            }
                        }


                        Collections.sort(notificationList, new Comparator<NotificationItem>() {
                            @Override
                            public int compare(NotificationItem o1, NotificationItem o2) {
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
                                try {
                                    Date date1 = sdf.parse(o1.getTimestamp());
                                    Date date2 = sdf.parse(o2.getTimestamp());

                                    if (date1 != null && date2 != null) {
                                        return date2.compareTo(date1);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return 0;
                            }
                        });

                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                });
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastReceiver mqttReceiver = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mqttReceiver);
    }

}

