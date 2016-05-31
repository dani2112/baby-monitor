package de.dk_s.babymonitor.monitoring;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SoundServerClient implements Runnable {

    private static final String TAG = "SoundServerClient";

    private Socket clientSocket;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    private boolean isRunning = true;

    private long lastAck;

    public SoundServerClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.lastAck = System.currentTimeMillis();
    }

    public void stopCommunication() {
        try {
            isRunning = false;
            inputStream.close();
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAck > 200000) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void run() {
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isWsConnection = WsCommunicationHelper.handleWsHandshake(inputStream, outputStream);

        Log.e(TAG, "Running");
        while(isRunning) {

        }
        Log.e(TAG, "QUIT");
    }
}
