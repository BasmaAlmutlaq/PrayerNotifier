package com.example.prayernotifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView imageView;
    private TextView textView;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];
    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    private boolean locationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        imageView = findViewById(R.id.compress);
        textView = findViewById(R.id.textView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationPermissionGranted = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationPermissionGranted) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            } else {
                Toast.makeText(this, "Location permission is required to determine Qibla direction.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            floatGravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            floatGeoMagnetic = event.values;
        }

        if (floatGravity != null && floatGeoMagnetic != null) {
            boolean successfullyRead = SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
            if (successfullyRead) {
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
                float azimuthAngle = floatOrientation[0];
                double qiblaAngle = calculateQiblaDirection();

                // Rotate compass image to point towards Qibla
                float rotationAngle = (float) Math.toDegrees(azimuthAngle - qiblaAngle);
                imageView.setRotation(rotationAngle);

                // Display Qibla direction
                textView.setText("Qibla Direction: " + Math.round(qiblaAngle) + "° from North");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing for now
    }

    private double calculateQiblaDirection() {
        // Your existing code to retrieve location and calculate Qibla direction
        if (locationPermissionGranted) {
            // Check if fine location permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                double kaabaLatitude = Math.toRadians(21.4225); // Latitude of Kaaba in radians
                                double kaabaLongitude = Math.toRadians(39.8262); // Longitude of Kaaba in radians

                                double longitudeDifference = kaabaLongitude - longitude;

                                double qiblaAngle = Math.atan2(
                                        Math.sin(longitudeDifference),
                                        Math.cos(latitude) * Math.tan(kaabaLatitude) - Math.sin(latitude) * Math.cos(longitudeDifference)
                                );

                                qiblaAngle = Math.toDegrees(qiblaAngle);
                                if (qiblaAngle < 0) {
                                    qiblaAngle += 360; // Convert negative angle to positive
                                }

                                // Update compass ImageView rotation and TextView with the Qibla angle
                                imageView.setRotation((float) -qiblaAngle); // Adjust for clockwise rotation
                                textView.setText("Qibla Direction: " + Math.round(qiblaAngle) + "° from North");
                            } else {
                                Toast.makeText(this, "Unable to retrieve location. Please make sure location services are enabled.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(this, e -> {
                            Log.e("CompassActivity", "Error getting location", e);
                            Toast.makeText(this, "Error getting location. Please try again later.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Location permission not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, "Location permission is required to determine Qibla direction.", Toast.LENGTH_SHORT).show();
        }

        return 0.0; // Default to 0.0 if calculation fails
    }


    private void updateCompassDisplay(double qiblaAngle) {
        // Update the compass ImageView rotation and TextView with the Qibla angle
        imageView.setRotation((float) -qiblaAngle); // Adjust for clockwise rotation
        textView.setText("Qibla Direction: " + Math.round(qiblaAngle) + "° from North");
    }


}
