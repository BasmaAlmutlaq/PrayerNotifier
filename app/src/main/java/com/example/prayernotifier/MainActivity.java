package com.example.prayernotifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity{

    private TextView sunriseTimeTextView;
    private TextView sunsetTimeTextView;
    private TextView fajrTimeTextView;
    private TextView dhuhrTimeTextView;
    private TextView asrTimeTextView;
    private TextView maghribTimeTextView;
    private TextView ishaTimeTextView;
    private TextView nextPrayerTextView;
    private TextView timeRemainingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sunriseTimeTextView = findViewById(R.id.sunrise);
        sunsetTimeTextView = findViewById(R.id.sunset);

        fajrTimeTextView = findViewById(R.id.fajer);
        dhuhrTimeTextView = findViewById(R.id.dhuhr);
        asrTimeTextView = findViewById(R.id.asr);
        maghribTimeTextView = findViewById(R.id.magreb);
        ishaTimeTextView = findViewById(R.id.isha);

        nextPrayerTextView = findViewById(R.id.nextPrayer);
        timeRemainingTextView = findViewById(R.id.timeRemaining);

        displayPrayerTimes();
        displayNextPray();

        ColorStateList colorStateList = getResources().getColorStateList(R.color.color);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                bottomNavigationView.setItemIconTintList(colorStateList);
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.navigation_settings) {
                bottomNavigationView.setItemIconTintList(colorStateList);
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            } else if (itemId == R.id.navigation_compass) {
                bottomNavigationView.setItemIconTintList(colorStateList);
                startActivity(new Intent(MainActivity.this, CompassActivity.class));
                return true;
            } else {
                return false;
            }
        });

    }
    private void displayNextPray(){
        PrayTime prayTime = new PrayTime();

        // Replace these values with actual latitude, longitude, and time zone
        double latitude = 24.7136; // Example: Riyadh latitude
        double longitude = 46.6753; // Example: Riyadh longitude
        double timeZone = 3;

        // Get current time
        Calendar now = Calendar.getInstance();

        // Calculate prayer times for today
        ArrayList<String> prayerTimes = prayTime.getPrayerTimes(now, latitude, longitude, timeZone);

        // Determine the next prayer
        int nextPrayerIndex = getNextPrayerIndex(prayerTimes, now);
        String nextPrayer = prayerTimes.get(nextPrayerIndex);

        // Display next prayer
        nextPrayerTextView.setText("Next Prayer: " + getNextPrayerName(nextPrayerIndex));

        // Calculate and display time remaining for the next prayer
        Calendar nextPrayerTime = (Calendar) now.clone();
        String[] nextPrayerParts = nextPrayer.split(":");
        nextPrayerTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(nextPrayerParts[0]));
        nextPrayerTime.set(Calendar.MINUTE, Integer.parseInt(nextPrayerParts[1]));

        long timeRemainingMillis = nextPrayerTime.getTimeInMillis() - now.getTimeInMillis();
        long minutesRemaining = timeRemainingMillis / (1000 * 60);

        timeRemainingTextView.setText("After: " + minutesRemaining + " minutes");
    }

    private int getNextPrayerIndex(ArrayList<String> prayerTimes, Calendar now) {
        // Get the current time in milliseconds since epoch
        long currentTimeMillis = now.getTimeInMillis();

        // Iterate through the list of prayer times
        for (int i = 0; i < prayerTimes.size(); i++) {
            // Parse the current prayer time into Calendar object for comparison
            String prayerTime = prayerTimes.get(i);
            String[] timeParts = prayerTime.split(":");
            int prayerHour = Integer.parseInt(timeParts[0]);
            int prayerMinute = Integer.parseInt(timeParts[1]);

            // Set the prayer time in a Calendar object
            Calendar prayerTimeCal = (Calendar) now.clone();
            prayerTimeCal.set(Calendar.HOUR_OF_DAY, prayerHour);
            prayerTimeCal.set(Calendar.MINUTE, prayerMinute);

            // Get the time in milliseconds for this prayer time
            long prayerTimeMillis = prayerTimeCal.getTimeInMillis();

            // Compare current time with prayer time to find the next upcoming prayer
            if (prayerTimeMillis > currentTimeMillis) {
                // Found the next upcoming prayer time, return its index
                return i;
            }
        }

        return 0; // Default to the first prayer time (Fajr) if no upcoming time is found
    }


    private String getNextPrayerName(int index) {
        // Map index to prayer name
        String[] prayerNames = {"Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};
        return prayerNames[index];
    }
    private void displayPrayerTimes() {
        // Create an instance of the PrayerTimes class
        PrayTime prayerTimes = new PrayTime ();

        // Set the city to Riyadh
        //prayerTimes.setCity("Riyadh");

        // Get the current date
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        double latitude = 24.7136;  // Riyadh latitude
        double longitude = 46.6753; // Riyadh longitude
        double timezone = 3;

        // Calculate the prayer times for today
        ArrayList<String> times = prayerTimes.getPrayerTimes(cal, latitude, longitude, timezone);


        // Set TextViews with prayer times
        fajrTimeTextView.setText("Fajr \n" + times.get(0));
        sunriseTimeTextView.setText("Sunrise \n" + times.get(1));
        dhuhrTimeTextView.setText("Dhuhr \n" + times.get(2));
        asrTimeTextView.setText("Asr \n" + times.get(3));
        sunsetTimeTextView.setText("Sunset \n" + times.get(4));
        maghribTimeTextView.setText("Magheib \n" + times.get(5));
        ishaTimeTextView.setText("Isha \n" + times.get(6));


        // Set TextViews for Dhuhr, Asr, Maghrib, Isha similarly
    }
}