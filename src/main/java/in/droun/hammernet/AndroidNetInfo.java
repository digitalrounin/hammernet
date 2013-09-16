/*
 * Copyright (c) 2013, Pedro F. Hernandez (Digital Rounin)
 *
 * All rights reserved.
 *
 * See the separate "LICENSE.md" file for the distribution license (Modified BSD licence)
 */
package in.droun.hammernet;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.math.BigInteger;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is mostly here to allow for dependency injection and to simplify unit testing. Since
 * we have no way of inheriting from {@link  java.net.NetworkInterface} or
 * {@link  android.content.Context} This class provides a nice wrapper around both when it comes to
 * networking functionality.
 *
 * @author Pedro F. Hernandez (Digital Rounin)
 */
public class AndroidNetInfo {

    protected static final Logger LOG = LoggerFactory.getLogger("qsAndrWifiInfo");

    /**
     * Android context to use for networking calls.
     */
    private final transient Context mAndroidContext;

    private final transient NetworkInterfaceInfo mInterfaceInfo;

    /**
     * Initialize with an Android Context.
     *
     * @param context
     * @param interfaceInfo
     */
    public AndroidNetInfo(final Context context, final NetworkInterfaceInfo interfaceInfo) {
        mAndroidContext = context;
        mInterfaceInfo = interfaceInfo;
    }

    public AndroidNetInfo(final Context context) {
        this(context, new NetworkInterfaceInfo());
    }


    /**
     * Returns the Wi-Fi interface name on an Android device.
     *
     * @return The interface name of the Wi-Fi adapter. If there is no Wi-Fi adapter, null is
     *         returned. This is typical the case for Android Virtual Machines.
     *
     * @throws SocketException
     *
     * <!-- CHECKSTYLE.OFF: LineLength - URLs, cannot shorten -->
     * @see <a
     * href="http://stackoverflow.com/questions/4677684/wifi-network-interface-name/18657669#18657669">
     * Stackoverflow: wifi network interface name</a> - for more information.
     * <!-- CHECKSTYLE.ON: LineLength -->
     *
     *
     */
    public String wifiInterfaceName() throws SocketException {

        // Fetch WiFi MAC address to search on
        final BigInteger wifiMac = wifiMacAddress();
        final String result;
        if (wifiMac != null) {
            result = mInterfaceInfo.getNameByMacAddress(wifiMac);
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Finds the devices WiFi interface and returns its MAC address.
     *
     * @return The WiFi device's MAC address, null if there is no no WiFi interface.
     */
    public BigInteger wifiMacAddress() {

        // Get WiFi interface's MAC address as a BigInteger.
        final WifiManager wifiManager
                = (WifiManager) mAndroidContext.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final String wifiMacString = wifiInfo.getMacAddress();

        BigInteger result;
        if (wifiMacString != null) {
            try {
                result = NetworkInterfaceInfo.macAddressToBigInteger(wifiMacString);
            } catch (IllegalArgumentException ex) {
                // TODO: log!!!
                result = null;
            }
        } else {
            result = null;
        }

        return result;
    }

    public String getIp4Address() {
        return getIp4Address(null);
    }

    public String getIp4Address(final String defaultInterface) {
        String ipAddress = null;
        try {
            String interfaceName = wifiInterfaceName();
            if (interfaceName == null || interfaceName.isEmpty()) {
                LOG.debug("Using defaultInterfaceName '{}'.", defaultInterface);
                interfaceName = defaultInterface;
            }
            if (interfaceName != null && !interfaceName.isEmpty()) {
                ipAddress = mInterfaceInfo.getIp4HostAddressByName(interfaceName);
            }
        } catch (SocketException ex) {
            LOG.error("Failed to get WiFi interface name.", ex);
        }
        return ipAddress != null && !ipAddress.isEmpty() ? ipAddress : null;
    }
}