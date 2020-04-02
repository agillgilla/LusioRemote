package com.residem.arjun.lusioremote;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class CommandSender implements Runnable {
    private MainActivity activity;

    String connectedHost;

    public static final int PORT = 65432;
    private DataOutputStream socketStream;
    private Socket socket;

    private BlockingQueue<String> commandQueue;
    private static final String DELIMITER = "\n";

    public CommandSender(MainActivity activity, BlockingQueue<String> commandQueue) {
        this.activity = activity;
        this.commandQueue = commandQueue;
    }

    public boolean scanHostsAndConnect(List<String> possibleHosts, Optional<Long> timeout) {
        long socketTimeout = 500;
        if (timeout.isPresent()) {
            socketTimeout = timeout.get();
        }

        for (String possibleHost : possibleHosts) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(possibleHost, PORT), 500);
                socketStream = new DataOutputStream(socket.getOutputStream());

                this.connectedHost = possibleHost;

                System.out.println("=====================================");
                System.out.println("Connected to server at " + this.connectedHost + ":" + PORT);
                System.out.println("=====================================");

                return true;
            } catch (ConnectException ce) {
                System.out.println("Host at " + possibleHost + " refused connection to port: " + PORT);
            } catch (SocketTimeoutException ste) {
                System.out.println("Host at " + possibleHost + " took too long to connect.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //System.out.println("Blocking on taking from command queue...");
                String command = this.commandQueue.take();
                sendCommand(command);
            } catch (InterruptedException e) {
                System.err.println("Error occurred:" + e);
            }
        }
    }

    private void sendCommand(String command) {
        try {
            //System.out.println("Sending command: " + command);
            socketStream.write((command + DELIMITER).getBytes());
        } catch (SocketException se) {
            //activity.toast("Connection lost.");

            activity.snackbar("Connection lost.", "RECONNECT");

            activity.disableButtons();
            activity.enableConnectButton();
        } catch (IOException ioe) {
            System.out.println("Error sending command:");
            ioe.printStackTrace();
        }
    }

    public String getConnectedHost() {
        return this.connectedHost;
    }
}
