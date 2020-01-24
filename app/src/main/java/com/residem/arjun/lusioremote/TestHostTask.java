package com.residem.arjun.lusioremote;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestHostTask implements Runnable {
    private MainActivity activity;

    private final AtomicInteger taskCounter;
    private String testHost;
    private static List<String> availableHosts = Collections.synchronizedList(new ArrayList<String>());
    private final static int timeout = 3000;

    public TestHostTask(MainActivity activity, AtomicInteger taskCounter, String host) {
        this.activity = activity;
        this.taskCounter = taskCounter;
        this.testHost = host;
    }

    @Override
    public void run() {
        try {
            if (InetAddress.getByName(this.testHost).isReachable(timeout)) {
                System.out.println(this.testHost + " is reachable");
                availableHosts.add(this.testHost);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        int tasksLeft = this.taskCounter.decrementAndGet();

        if (tasksLeft == 0) {
            activity.attemptConnection(availableHosts);
        }
    }

    public static void cleanHostsList() {
        availableHosts.clear();
    }
}
