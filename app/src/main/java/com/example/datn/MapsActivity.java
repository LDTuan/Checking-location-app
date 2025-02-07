package com.example.datn;

import androidx.fragment.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.datn.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private TextView Lat, Long;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ImageView Back, LockDevice, GetLocation, ChangeMap;
    private static final String BROKER_URL = "tcp://broker.emqx.io:1883";
    private static final String CLIENT_ID = "";
    private MqttHandle mqttHandler;
    private String receivedMsg;
    private Marker currentMarker;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String deviceID;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LockDevice = findViewById(R.id.imglock);
        GetLocation = findViewById(R.id.imgGetLocation);
        ChangeMap = findViewById(R.id.imgMapsChange);
        Lat = findViewById(R.id.txtLat);
        Long = findViewById(R.id.txtLong);
        Back = findViewById(R.id.imgBack);

        mqttHandler = new MqttHandle();
        mqttHandler.connect(BROKER_URL, CLIENT_ID);

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        mqttHandler.setCallbackHandler(new MqttHandle.MqttCallbackHandler() {
            @Override
            public void onMessageReceived(String topic, String message) {
                receivedMsg = message;

                updateMapWithReceivedMessage();
            }
        });

        loadDeviceAndSubscribe();
        ChangeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOpenLockConfirmationDialog();
            }
        });

        LockDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLockConfirmationDialog();
            }
        });

        GetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deviceID != null) {
                    publishMessage(deviceID, "getlocation");
                } else {
                    showToast("Device ID is not available!");
                }
            }
        });
    }

    private void showLockConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm vehicle lock")
                .setMessage("Are you sure you want to lock your vehical?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (deviceID != null) {
                            publishMessage(deviceID, "lock");
                        } else {
                            showToast("Device ID is not available");
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showOpenLockConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm vehicle unlock")
                .setMessage("Are you sure you want to unlock your vehical?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (deviceID != null) {
                            publishMessage(deviceID, "unlock");
                        } else {
                            showToast("Device ID is not available");
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        if (receivedMsg != null && !receivedMsg.isEmpty()) {
            addMarkerToMap(receivedMsg);
        }
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
    }

    private void publishMessage(String topic, String message) {
        mqttHandler.publish(topic, message);
    }

    private void subscribeToTopic(String topic) {
        mqttHandler.subscribe(topic);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show());
    }

    private void updateMapWithReceivedMessage() {
        runOnUiThread(() -> {
            if (mMap != null && receivedMsg != null && !receivedMsg.isEmpty()) {
                addMarkerToMap(receivedMsg);
            }
        });
    }

    private void addMarkerToMap(String message) {
        try {

            if (!isValidCoordinates(message)) {
            }
            String[] coordinates = message.split(",");
            double latitude = Double.parseDouble(coordinates[0].trim());
            double longitude = Double.parseDouble(coordinates[1].trim());

            Lat.setText(String.valueOf(latitude));
            Long.setText(String.valueOf(longitude));

            LatLng receivedLatLng = new LatLng(latitude, longitude);


            if (currentMarker != null) {
                currentMarker.remove();
            }

            currentMarker = mMap.addMarker(new MarkerOptions().position(receivedLatLng).title("Received Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(receivedLatLng, 18));

        } catch (NumberFormatException e) {

        } catch (IllegalArgumentException e) {
        }
    }

    private boolean isValidCoordinates(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        String[] parts = message.split(",");
        if (parts.length != 2) {
            return false;
        }
        return true;
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
                                    subscribeToTopic(deviceID);
                                    mqttHandler.publish(deviceID, "getlocation");
                                } else {
                                    showToast("DeviceID not found!");
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToast("Error while getting DeviceID" + e.getMessage());
                    });
        }
    }
}
