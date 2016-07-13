package de.dk_s.babymonitor.client;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dk_s.babymonitor.communication.WsCommunicationHelper;

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

    /* Flag that indicates if information client is connected */
    private boolean isClientConnected = false;

    public InformationClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void startClient() {
        if (isClientStarted) {
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
        if (!isClientStarted) {
            return;
        }
        isClientStarted = false;
        try {
            if(clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while closing information server connection");
        }
        clientExecutorService.shutdownNow();
    }

    private void handleClientConnection() {
        clientSocket = tryConnect(serverAddress, 8083);
        /* Connection not successful if clientSocket null */
        if(clientSocket == null) {
            return;
        }
        while (isClientStarted) {
            try {
                Log.e(TAG, "Connection successful");
            } catch (Exception e) {
                Log.e(TAG, "Error: Exception in information server communication.");
            }
        }
    }

    private Socket tryConnect(String ipAddress, int port) {
        boolean connectionSucessful = false;
        Socket socket = null;
        try {
            socket = new Socket(ipAddress, port);
            WsCommunicationHelper.handleHandshakeClient(socket.getOutputStream());
            connectionSucessful = performPing(socket);
        } catch (IOException e) {
            connectionSucessful = false;
        }
        return connectionSucessful ? socket : null;
    }

    private boolean performPing(Socket socket) {
        boolean commandSucessful = false;
        try {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            byte command = 0;
            byte[] sendData = new byte[]{command};
            WsCommunicationHelper.sendDataClient(130, sendData, outputStream);
            byte[] receiveData = WsCommunicationHelper.receiveDataClient(inputStream);
            if (receiveData[0] == 0) {
                commandSucessful = true;
            }
        } catch (IOException e) {
            commandSucessful = false;
        }
        return commandSucessful;
    }
}
