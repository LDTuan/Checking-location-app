package com.example.datn;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class Home extends AppCompatActivity {

    private Button Mode;
    private ImageView User1, Home1, Notifi1, imvInformation, imvDevice;
    private ImageButton Logout;
    private FirebaseAuth mAuth;
    private TextView ID, FullName, PhoneNumber, Address, CitizenNumber;
    private FirebaseFirestore db;
    private Button linkdevice;
    private TextView DeviceID, Licence, VehicleType;

    // MQTT
    private static final String BROKER_URL = "tcp://broker.emqx.io:1883";
    private static final String CLIENT_ID = "";

    private MqttHandle mqttHandler;

    private boolean isMode1 = true; // Default mode is Mode1

    private static final String SHARED_PREFS_NAME = "ModePrefs";
    private static final String MODE_KEY = "current_mode";

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        Intent serviceIntent = new Intent(this, MqttService.class);
        startService(serviceIntent);

        Mode = findViewById(R.id.btnMode);
        Home1 = findViewById(R.id.imvHome1);
        Notifi1 = findViewById(R.id.imvNotifi1);
        Logout = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        imvDevice = findViewById(R.id.imageView3);
        imvInformation = findViewById(R.id.imageView2);
        ID = findViewById(R.id.txtID);
        FullName = findViewById(R.id.txtFullName);
        PhoneNumber = findViewById(R.id.txtPhoneNumber);
        Address = findViewById(R.id.txtAddress);
        CitizenNumber = findViewById(R.id.txtCitizenNumber);
        linkdevice = findViewById(R.id.btnAddDevice);
        DeviceID = findViewById(R.id.txtDeviceID);
        Licence = findViewById(R.id.txtLicense);
        VehicleType = findViewById(R.id.txtType);

        mqttHandler = new MqttHandle();
        mqttHandler.connect(BROKER_URL, CLIENT_ID);

        loadUserInfo();
        loadDevice();

        mqttHandler.setCallbackHandler(new MqttHandle.MqttCallbackHandler() {
            @Override
            public void onMessageReceived(String topic, String message) {

                if (message.equals("Mode1")) {
                    Mode.setBackgroundColor(getResources().getColor(R.color.mode1_color));
                } else if (message.equals("Mode2")) {
                    Mode.setBackgroundColor(getResources().getColor(R.color.mode2_color));
                }
            }
        });
        subscribeToTopic(DeviceID.getText().toString());

        isMode1 = loadModeState();
        if (isMode1) {
            Mode.setBackgroundColor(getResources().getColor(R.color.mode1_color));
        } else {
            Mode.setBackgroundColor(getResources().getColor(R.color.mode2_color));
        }


        Mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMode1) {
                    publishMessage(DeviceID.getText().toString(), "Mode2");
                    Mode.setBackgroundColor(getResources().getColor(R.color.mode2_color));
                } else {
                    publishMessage(DeviceID.getText().toString(), "Mode1");
                    Mode.setBackgroundColor(getResources().getColor(R.color.mode1_color));
                }
                isMode1 = !isMode1;

                saveModeState(isMode1);
            }
        });

        Notifi1.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Notifi.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        linkdevice.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, LinkDevice.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        imvDevice.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(Home.this)
                        .setTitle("Confirm")
                        .setMessage("Do you want to edit the information?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Home.this, ChangeDeviceLink.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                return true;
            }
        });

        imvInformation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(Home.this)
                        .setTitle("Confirm")
                        .setMessage("Do you want to edit the information?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Home.this, ChangeInformation.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                return true;
            }
        });

        Home1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(Home.this)
                        .setTitle("Confirm logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mAuth.signOut();
                                Intent intent = new Intent(Home.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void publishMessage(String topic, String message) {
        mqttHandler.publish(topic, message);
    }

    private void subscribeToTopic(String topic) {
        mqttHandler.subscribe(topic);
    }

    private void saveModeState(boolean isMode1) {
        getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(MODE_KEY, isMode1)
                .apply();
    }

    private boolean loadModeState() {
        return getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
                .getBoolean(MODE_KEY, true); // Default is Mode1
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> user = documentSnapshot.getData();
                            if (user != null) {
                                ID.setText(uid);
                                FullName.setText(user.get("FullName").toString());
                                PhoneNumber.setText(user.get("PhoneNumber").toString());
                                Address.setText(user.get("Address").toString());
                                CitizenNumber.setText(user.get("CitizenNumber").toString());
                            }
                        } else {
                            Toast.makeText(Home.this, "User information not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Home.this, "Error getting user information" + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDevice() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("devices").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> device = documentSnapshot.getData();
                            if (device != null) {
                                DeviceID.setText(device.get("DeviceId") != null ? device.get("DeviceId").toString() : "Not linked");
                                Licence.setText(device.get("License") != null ? device.get("License").toString() : "Not linked");
                                VehicleType.setText(device.get("VehicleType") != null ? device.get("VehicleType").toString() : "Not linked");
                                if (device.get("DeviceId") != null) {
                                    linkdevice.setEnabled(false);
                                    linkdevice.setBackgroundColor(ContextCompat.getColor(this, R.color.mode2_color));
                                }
                            }
                        } else {
                            Toast.makeText(Home.this, "Device not linked!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting device data", e);
                        Toast.makeText(Home.this, "Error getting device information" + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
