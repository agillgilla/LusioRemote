package com.residem.arjun.lusioremote;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScanHostsTask implements Runnable {

    private CommandSender commandSender;
    private String host;
    private boolean isConnected;

    private final static int timeout = 3000;

    public ScanHostsTask(CommandSender commandSender, String host) {
        this.commandSender = commandSender;
        this.host = host;
        this.isConnected = false;
    }

    @Override
    public void run() {
        this.isConnected = commandSender.scanHostsAndConnect(Arrays.asList(host));
    }

    public boolean isConnected() {
        return this.isConnected;
    }
}
