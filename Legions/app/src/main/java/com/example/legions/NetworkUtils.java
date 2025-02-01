package com.example.legions;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkUtils {

    // Method to fetch the router's MAC address
    public static String getRouterMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null) {
            // Get the current connection information (WifiInfo)
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            // Check if connected to a Wi-Fi network
            if (wifiInfo != null) {
                // Get the DhcpInfo which contains the router's information
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

                if (dhcpInfo != null) {
                    // Convert the router's IP address to MAC address format (if needed)
                    String routerMac = formatMacAddress(dhcpInfo.serverAddress);
                    return routerMac;
                }
            }
        }
        return "Unable to retrieve MAC address";  // Return a default message if not available
    }

    // Convert the server address (integer) to MAC address format (XX:XX:XX:XX:XX:XX)
    private static String formatMacAddress(int serverAddress) {
        StringBuilder macAddress = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int byteValue = (serverAddress >> (i * 8)) & 0xFF;
            macAddress.insert(0, String.format("%02X", byteValue));
            if (i < 5) {
                macAddress.insert(0, ":");
            }
        }
        return macAddress.toString();
    }
}
