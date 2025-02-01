package com.example.legions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.widget.TextView;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;

public class securedpage extends AppCompatActivity {
    private TextView dhcpLeaseTime, wifiIpAddress, mac;
    private CountDownTimer leaseTimer;
    private long remainingLeaseTimeMillis; // Store remaining lease time in milliseconds
    private final Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_securedpage);

        // Initialize views
        dhcpLeaseTime = findViewById(R.id.dhcpLeaseTime);
        wifiIpAddress = findViewById(R.id.wifiIpAddress);
        mac = findViewById(R.id.routerMacAddress);

        // Get and display IP address
        wifiIpAddress.setText("IP Address: " + getDeviceIpAddress());

        // Get and display router MAC address
        mac.setText("Router MAC Address: " + NetworkUtils.getRouterMacAddress(this));

        // Start DHCP lease countdown
        startDhcpLeaseCountdown();
    }

    private String getDeviceIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (dhcpInfo != null) {
                return Formatter.formatIpAddress(dhcpInfo.ipAddress);
            }
        }
        return "Unknown";
    }

    private void startDhcpLeaseCountdown() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (dhcpInfo != null && dhcpInfo.leaseDuration > 0) {
                remainingLeaseTimeMillis = dhcpInfo.leaseDuration * 1000L; // Convert seconds to milliseconds
                startCountdownTimer();
            } else {
                dhcpLeaseTime.setText("DHCP Lease Time: Unknown");
            }
        }
    }

    private void startCountdownTimer() {
        if (leaseTimer != null) {
            leaseTimer.cancel(); // Stop existing timer before starting a new one
        }

        leaseTimer = new CountDownTimer(remainingLeaseTimeMillis, 1000) { // Update every second
            @Override
            public void onTick(long millisUntilFinished) {
                remainingLeaseTimeMillis = millisUntilFinished;
                int minutes = (int) (millisUntilFinished / 60000);
                int seconds = (int) ((millisUntilFinished / 1000) % 60);
                dhcpLeaseTime.setText("DHCP Lease Time: " + minutes + " min " + seconds + " sec");
            }

            @Override
            public void onFinish() {
                dhcpLeaseTime.setText("DHCP Lease Expired");
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDhcpLeaseCountdown(); // Restart countdown when activity resumes
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (leaseTimer != null) {
            leaseTimer.cancel(); // Stop countdown when activity is paused
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (leaseTimer != null) {
            leaseTimer.cancel(); // Ensure countdown stops when activity is destroyed
        }
    }
}
