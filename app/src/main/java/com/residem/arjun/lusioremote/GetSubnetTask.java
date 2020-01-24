package com.residem.arjun.lusioremote;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class GetSubnetTask extends AsyncTask<Void, Void, String> {
    @Override
    protected String doInBackground(Void... voids) {
        String localIP = null;

        try {
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            while (n.hasMoreElements()) {
                NetworkInterface e = n.nextElement();
                //System.out.println("Interface: " + e.getName());
                if (e.getName().equals("wlan0")) {
                    Enumeration<InetAddress> a = e.getInetAddresses();
                    while (a.hasMoreElements()) {
                        InetAddress addr = a.nextElement();
                        System.out.println("  " + addr.getHostAddress());
                        localIP = addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException se) {
            se.printStackTrace();
        }

        int lastPeriod = localIP.lastIndexOf(".");
        String subnet = localIP.substring(0, lastPeriod);

        return subnet;
    }
}
