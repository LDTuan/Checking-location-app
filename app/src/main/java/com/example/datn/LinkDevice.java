package com.example.datn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class LinkDevice extends AppCompatActivity {

    private EditText DeviceId, VehicleType, License;
    private Button btnSave, btnCancle;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_link_device);


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        btnSave = findViewById(R.id.btnSave1);
        btnCancle = findViewById(R.id.btnCancle1);
        DeviceId = findViewById(R.id.edDeviceID);
        VehicleType = findViewById(R.id.edVehicleType);
        License = findViewById(R.id.edLicence);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deviceId = DeviceId.getText().toString().trim();
                String vehicleType = VehicleType.getText().toString().trim();
                String license = License.getText().toString().trim();

                if (isValidInput()) {

                    Map<String, Object> deviceData = new HashMap<>();
                    deviceData.put("DeviceId", deviceId);
                    deviceData.put("VehicleType", vehicleType);
                    deviceData.put("License", license);


                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String uid = currentUser.getUid();


                        db.collection("devices").document(uid)
                                .set(deviceData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(LinkDevice.this, "Device information saved", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(LinkDevice.this, Home.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LinkDevice.this, "Error saving information" + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(LinkDevice.this, "User not logged in", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LinkDevice.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });


        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LinkDevice.this, Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean isValidInput() {
        if (DeviceId.getText().toString().trim().isEmpty()) {
            DeviceId.setError("Device ID cannot be empty");
            DeviceId.requestFocus();
            return false;
        }

        if (VehicleType.getText().toString().trim().isEmpty()) {
            VehicleType.setError("Vehicle Type cannot be empty");
            VehicleType.requestFocus();
            return false;
        }

        if (License.getText().toString().trim().isEmpty()) {
            License.setError("License cannot be empty");
            License.requestFocus();
            return false;
        }

        return true;
    }
}
