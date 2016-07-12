package de.dk_s.babymonitor.client;


import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InformationClient {

    /* Tag for debugging outputs */
    private static final String TAG = "InformationClient";

    /* Flag that indicates if client is started */
    private boolean isClientStarted = false;

    /* ExecutorService that runs communication thread */
    private ExecutorService clientExecutorService;

    /* Socket for connecting to the Server */
    private Socket clientSocket;

    /* IP-Address that should be used for the connection */
    private String serverAddress;

    public InformationClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void startClient() {
        if(isClientStarted) {
            return;
        }
        isClientStarted = true;

        clientExecutorService = Executors.newSingleThreadExecutor();

        clientExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                handleClientConnection();
            }
        });

    }

    public void stopClient() {
        if(!isClientStarted) {
            return;
        }
        isClientStarted = false;
        try {
            clientSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while closing information server connection");
        }
        clientExecutorService.shutdownNow();
    }

    private void handleClientConnection() {
        try {
            clientSocket = new Socket(serverAddress, 8083);
            while(isClientStarted) {

            }
        } catch (IOException e) {
            Log.e(TAG, "Error: Could not connect to information server.");
        }
    }

}
