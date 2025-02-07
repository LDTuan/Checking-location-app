package com.example.datn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChangeDeviceLink extends AppCompatActivity {

    private EditText DeviceId, VehicleType, License;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_device_link);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnSave = findViewById(R.id.btnSave2);
        btnCancel = findViewById(R.id.btnCancel2);
        DeviceId = findViewById(R.id.edDeviceID1);
        VehicleType = findViewById(R.id.edVehicleType1);
        License = findViewById(R.id.edLicence1);

        loadUserInfo();
        //Cancel
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeDeviceLink.this, Home.class);
                // Đảm bảo không lưu lại activity hiện tại trong stack
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Đóng activity hiện tại sau khi chuyển hướng
            }
        });
        //Save
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    updateUserInfo();
                }
            }
        });

    }
    //checkInput
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
    //load device from firestore with deviceID
    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("devices").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Hiển thị thông tin hiện tại lên EditText
                                DeviceId.setText(documentSnapshot.getString("DeviceId"));
                                VehicleType.setText(documentSnapshot.getString("VehicleType"));
                                License.setText(documentSnapshot.getString("License"));
                            } else {
                                Toast.makeText(ChangeDeviceLink.this, "No user information", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChangeDeviceLink.this, "Error getting information: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }
    // update device  with new information
    private void updateUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            Map<String, Object> updatedUser = new HashMap<>();
            updatedUser.put("DeviceId", DeviceId.getText().toString().trim());
            updatedUser.put("VehicleType", VehicleType.getText().toString().trim());
            updatedUser.put("License", License.getText().toString().trim());

            db.collection("devices").document(uid)
                    .update(updatedUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ChangeDeviceLink.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ChangeDeviceLink.this, Home.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChangeDeviceLink.this, "Update information failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}