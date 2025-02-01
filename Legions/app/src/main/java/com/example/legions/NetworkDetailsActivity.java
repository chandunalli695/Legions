package com.example.legions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class NetworkDetailsActivity extends AppCompatActivity {

    private Handler vpnHandler = new Handler();
    private LottieAnimationView vpnAnimation;
    private Runnable vpnUpdater;

    private TextView networkName, securityStatus, attackPercentage;
    private TextView encryptionType, signalStrength, frequencyBand, networkType, ipAddress,connectionStatus;
    private TextView dhcpLeaseTime;
    private Handler handler = new Handler();
    private Button securebtn;
    private Runnable dhcpUpdater;
    private BroadcastReceiver vpnConnectionReceiver;
    LottieAnimationView l1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_details);

        // Initialize views
        networkName = findViewById(R.id.networkName);
        securityStatus = findViewById(R.id.securityStatus);
        attackPercentage = findViewById(R.id.attackPercentage);
        encryptionType = findViewById(R.id.encryptionType);
        signalStrength = findViewById(R.id.signalStrength);
        frequencyBand = findViewById(R.id.frequencyBand);
        networkType = findViewById(R.id.networkType);
        ipAddress = findViewById(R.id.ipAddress);
        connectionStatus = findViewById(R.id.connectionStatus);
        dhcpLeaseTime = findViewById(R.id.dhcpLeaseTime);
        securebtn=findViewById(R.id.securebtn);
        vpnAnimation=findViewById(R.id.lottieAnimationView);
        securebtn.setVisibility(View.GONE);
        updateVpnStatus();
        startVpnStatusUpdater();
        dhcpLeaseTime.setVisibility(View.GONE);
//        vpnAnimation = findViewById(R.id.lottieAnimationView);

        vpnConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                // Check if the connection is a VPN and if it's connected
                if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_VPN) {
                    if (networkInfo.isConnected()) {
                        // VPN is connected, redirect to the target page
                        redirectToPage();
                    }
                }
            }
        };



        securebtn.setOnClickListener(v -> {

            try {

                String buttonText = securebtn.getText().toString();
                if(buttonText.equals("VPN Connected")){
                    startActivity(new Intent(NetworkDetailsActivity.this, securedpage.class) );
                }else{

                Intent intent = new Intent(Settings.ACTION_VPN_SETTINGS);
                startActivity(intent);
                                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });



        // Start periodic update of DHCP lease time
        startDhcpLeaseTimeUpdater();


        // Get data from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String ssid = extras.getString("SSID");
            boolean isSecure = extras.getBoolean("isSecure");
            int attackRisk = extras.getInt("attackRisk");
            String encryption = extras.getString("encryption");
            int level = extras.getInt("signalStrength");
            int frequency = extras.getInt("frequency");
            String type = extras.getString("networkType");

            // Display the data
            networkName.setText(ssid);
            securityStatus.setText("Security: " + (isSecure ? "Secure" : "Insecure"));
            attackPercentage.setText("Attack Risk: " + attackRisk + "%");
            encryptionType.setText("Encryption: " + encryption);
            signalStrength.setText("Signal Strength: " + level + " dBm");
            frequencyBand.setText("Frequency Band: " + (frequency >= 2400 && frequency <= 2500 ? "2.4 GHz" : "5 GHz"));
            networkType.setText("Network Type: " + type);
            String ip = getDeviceIpAddress();
            if(ip.equals("0.0.0.0")){
                ipAddress.setText("connect to a wifi network");

            } else{
                ipAddress.setText("My IP Address: " + ip);
                System.out.println(ip);
            }

             checkConnectionStatus(ssid);


            // Simulate packet transfer graph (placeholder)

        }

    }

    private void redirectToPage() {

        Intent intent = new Intent(NetworkDetailsActivity.this, securedpage.class); // Replace with your target activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Important for services to launch activities
        startActivity(intent);
    }

    private void checkConnectionStatus(String selectedSsid) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String connectedSsid = wifiInfo.getSSID().replace("\"", ""); // Remove quotes from SSID

                if (connectedSsid.equals(selectedSsid)) {
                    connectionStatus.setText("Connection Status: Connected");
                    securebtn.setVisibility(View.VISIBLE);

                } else {
                    connectionStatus.setText("Connection Status: Not Connected");
                }
            }
        }
    }

    private String getDeviceIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (dhcpInfo != null) {
                // Convert the IP address from integer to human-readable format
                return Formatter.formatIpAddress(dhcpInfo.ipAddress);
            }
        }
        return "Unknown";
    }

    private void startDhcpLeaseTimeUpdater() {
        dhcpUpdater = new Runnable() {
            @Override
            public void run() {
                updateDhcpLeaseTime();
                handler.postDelayed(this, 1000); // Update every 5 seconds
            }
        };
        handler.post(dhcpUpdater);
    }
    private void updateDhcpLeaseTime() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (dhcpInfo != null) {
                int leaseTimeSeconds = dhcpInfo.leaseDuration;
                if (leaseTimeSeconds > 0) {
                    int minutes = leaseTimeSeconds / 60;
                    int seconds = leaseTimeSeconds % 60;
                    dhcpLeaseTime.setText("DHCP Lease Time: " + minutes + " min " + seconds + " sec");
                } else {
                    dhcpLeaseTime.setText("DHCP Lease Time: Unknown");
                }
            }
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(dhcpUpdater); // Stop updates when activity is destroyed
    }

    private void launchInstalledVPN() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        List<String> vpnApps = new ArrayList<>();

        for (ApplicationInfo appInfo : installedApps) {
            // Check if the app is categorized as a VPN (Android 10+)
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null &&
                    packageManager.checkPermission(android.Manifest.permission.BIND_VPN_SERVICE, appInfo.packageName) == PackageManager.PERMISSION_GRANTED) {
                vpnApps.add(appInfo.packageName);
            }
        }

        if (!vpnApps.isEmpty()) {
            String vpnPackage = vpnApps.get(0);
            Intent launchIntent = packageManager.getLaunchIntentForPackage(vpnPackage);
            if (launchIntent != null) {
                startActivity(launchIntent);
                Toast.makeText(this, "Launching VPN...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to launch VPN.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No VPN app  found raa babu . Please install one.", Toast.LENGTH_LONG).show();
        }
    }




    private void updateVpnStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            // Get the active network
            Network activeNetwork = connectivityManager.getActiveNetwork();

            // Check if there is an active network and if it's a VPN connection
            if (activeNetwork != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    // VPN is connected
                    securebtn.setText("VPN Connected");
                    securebtn.setBackgroundColor(Color.GREEN);
                    vpnAnimation.setAnimation(R.raw.vpnanimation);
                    vpnAnimation.playAnimation();


                } else  {
                    // VPN is not connected
                    securebtn.setText("Secure Connection");
                    securebtn.setBackgroundColor(Color.LTGRAY);


                }
            } else {
                // No active network
                securebtn.setText("Secure Connection");
                securebtn.setBackgroundColor(Color.LTGRAY);
            }
        }
    }
    private void startVpnStatusUpdater() {
        vpnUpdater = new Runnable() {
            @Override
            public void run() {
                updateVpnStatus();
                vpnHandler.postDelayed(this, 1000); // Run every 1 second
            }
        };
        vpnHandler.post(vpnUpdater);
    }


}


    // Simulate packet transfer graph (placeholder)

