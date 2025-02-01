package com.example.legions;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class arptable extends AppCompatActivity {

    private TextView devicesListTextView;
    private ExecutorService executorService;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arptable);

        devicesListTextView = findViewById(R.id.devicesListTextView);

        // Initialize ExecutorService for background tasks
        executorService = Executors.newSingleThreadExecutor();
        // Initialize Handler for UI updates
        handler = new Handler(Looper.getMainLooper());

        // Start fetching and displaying the ARP table
        fetchArpTable();
    }

    private void fetchArpTable() {
        executorService.execute(() -> {
            StringBuilder arpTable = new StringBuilder();
            try {
                // Get all network interfaces
                for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    // Get the hardware address (MAC address)
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes != null) {
                        StringBuilder macAddress = new StringBuilder();
                        for (byte b : macBytes) {
                            macAddress.append(String.format("%02X:", b));
                        }
                        if (macAddress.length() > 0) {
                            macAddress.deleteCharAt(macAddress.length() - 1);
                        }
                        String interfaceName = networkInterface.getName();
                        arpTable.append("Interface: ").append(interfaceName)
                                .append(" | MAC: ").append(macAddress.toString()).append("\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                arpTable.append("Error retrieving network interfaces.\n");
            }

            // Update the UI with the ARP table
            String finalArpTable = arpTable.toString();
            handler.post(() -> devicesListTextView.setText(finalArpTable));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the ExecutorService to avoid memory leaks
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}