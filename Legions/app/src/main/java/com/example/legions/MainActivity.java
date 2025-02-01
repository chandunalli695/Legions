package com.example.legions;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Button rbt;

    private ListView networkListView;
    private ArrayAdapter<String> adapter;
    private List<String> networkList;
    private List<ScanResult> scanResults;
    private WifiManager wifiManager;
    private Button sort2_4GHzButton, sort5GHzButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        networkListView = findViewById(R.id.networkListView);
        networkList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, networkList);
        networkListView.setAdapter(adapter);
        rbt=findViewById(R.id.Refreshbtn);

        // Initialize Buttons
        sort2_4GHzButton = findViewById(R.id.sort_2_4ghz);
        sort5GHzButton = findViewById(R.id.sort_5ghz);

        rbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  refreshNetworks();


            }
        });

        // Inside the ListView item click listener
        networkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected network
                ScanResult selectedNetwork = scanResults.get(position);

                // Determine if the network is secure
                boolean isSecure = isNetworkSecure(selectedNetwork);

                // Calculate attack risk
                int attackRisk = calculateAttackRisk(selectedNetwork);

                // Determine encryption type
                String encryption = getEncryptionType(selectedNetwork.capabilities);

                // Get signal strength
                int level = selectedNetwork.level;

                // Get frequency band
                int frequency = selectedNetwork.frequency;

                // Determine network type (public/private)
                String type = isPublicNetwork(selectedNetwork.SSID) ? "Public" : "Private";

                // Open NetworkDetailsActivity and pass the data
                Intent intent = new Intent(MainActivity.this, NetworkDetailsActivity.class);
                intent.putExtra("SSID", selectedNetwork.SSID);
                intent.putExtra("isSecure", isSecure);
                intent.putExtra("attackRisk", attackRisk);
                intent.putExtra("encryption", encryption);
                intent.putExtra("signalStrength", level);
                intent.putExtra("frequency", frequency);
                intent.putExtra("networkType", type);
                startActivity(intent);
            }
        });

// Helper method to get encryption type


// Helper method to determine if the network is public



        // Set button click listeners
        sort2_4GHzButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterNetworksByBand(2400, 2500);
            }
        });

        sort5GHzButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterNetworksByBand(4900, 5900);
            }
        });


        

        // Initialize WifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, start Wi-Fi scanning
            scanWiFiNetworks();
        }
    }
    private boolean isNetworkSecure(ScanResult network) {
        String capabilities = network.capabilities;
        return !capabilities.contains("WEP") && !capabilities.contains("ESS");
    }
    private String getEncryptionType(String capabilities) {
        if (capabilities.contains("WEP")) {
            return "WEP";
        } else if (capabilities.contains("WPA2")) {
            return "WPA2";
        } else if (capabilities.contains("WPA3")) {
            return "WPA3";
        } else {
            return "Open";
        }
    }

    private int calculateAttackRisk(ScanResult selectedNetwork) {
        int risk = 0;

        // Factor 1: Encryption Type
        String capabilities = selectedNetwork.capabilities;
        if (capabilities.contains("WEP")) {
            risk += 40; // WEP is highly vulnerable
        } else if (capabilities.contains("WPA") || capabilities.contains("WPA2")) {
            risk += 20; // WPA/WPA2 is more secure but not immune
        } else if (capabilities.contains("WPA3")) {
            risk += 10; // WPA3 is the most secure
        } else {
            risk += 70; // Open network (no encryption)
        }

        // Factor 2: Frequency Band
        int frequency = selectedNetwork.frequency;
        if (frequency >= 2400 && frequency <= 2500) {
            risk += 20; // 2.4 GHz networks are more susceptible to interference
        } else if (frequency >= 4900 && frequency <= 5900) {
            risk += 10; // 5 GHz networks are less susceptible
        }

        // Factor 3: Signal Strength
        int level = selectedNetwork.level; // Signal strength in dBm
        if (level >= -50) {
            risk += 10; // Strong signal (low risk)
        } else if (level >= -70) {
            risk += 20; // Moderate signal
        } else {
            risk += 30; // Weak signal (high risk)
        }

        // Factor 4: Network Type (Public/Private)
        // This is a placeholder. You can add logic to detect public/private networks.
        risk += 10; // Assume it's a public network

        // Ensure the risk is within 0-100%
        return Math.min(risk, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start Wi-Fi scanning
                scanWiFiNetworks();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to scan Wi-Fi networks.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isPublicNetwork(String ssid) {
        // Placeholder logic: Assume networks with "Guest" or "Public" in the name are public
        return ssid.toLowerCase().contains("guest") || ssid.toLowerCase().contains("public")||ssid.toLowerCase().contains("wsu_ez_connect (5 ghz)") || ssid.toLowerCase().contains("wsu_ez_connect ");
    }
    private void scanWiFiNetworks() {
        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled()) {
                // Enable Wi-Fi if it's disabled
                wifiManager.setWifiEnabled(true);
                Toast.makeText(this, "Wi-Fi is enabled.", Toast.LENGTH_SHORT).show();
            }

            // Start Wi-Fi scanning
            wifiManager.startScan();
            scanResults = wifiManager.getScanResults();
            displayNetworks(scanResults);
        }
    }

    private void displayNetworks(List<ScanResult> scanResults) {
        networkList.clear();
        for (ScanResult result : scanResults) {
            String ssid = result.SSID;
            String band = getBand(result.frequency);
            networkList.add(ssid + " (" + band + ")");
        }
        adapter.notifyDataSetChanged();
    }

    private String getBand(int frequency) {
        if (frequency >= 2400 && frequency <= 2500) {
            return "2.4 GHz";
        } else if (frequency >= 4900 && frequency <= 5900) {
            return "5 GHz";
        } else {
            return "Unknown";
        }
    }
    private void refreshNetworks() {
        if (wifiManager != null) {
            // Start a new Wi-Fi scan
            boolean scanStarted = wifiManager.startScan();

            if (scanStarted) {
                // Clear the current list of networks
                networkList.clear();
                adapter.notifyDataSetChanged();

                // Get the updated scan results
                scanResults = wifiManager.getScanResults();

                // Display the updated networks
                displayNetworks(scanResults);

                Toast.makeText(this, "Scanning for networks...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to start Wi-Fi scan.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void filterNetworksByBand(int minFrequency, int maxFrequency) {
        List<String> filteredNetworks = new ArrayList<>();
        for (ScanResult result : scanResults) {
            if (result.frequency >= minFrequency && result.frequency <= maxFrequency) {
                String ssid = result.SSID;
                String band = getBand(result.frequency);
                filteredNetworks.add(ssid + " (" + band + ")");
            }
        }
        networkList.clear();
        networkList.addAll(filteredNetworks);
        adapter.notifyDataSetChanged();
    }
}