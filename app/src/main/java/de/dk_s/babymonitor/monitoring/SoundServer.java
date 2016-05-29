package de.dk_s.babymonitor.monitoring;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that streams sound data over websockets
 */
public class SoundServer {

    private static final String TAG = "SoundServer";

    private boolean isServerStarted = false;

    private ExecutorService connectionHandlingExecutorService;

    private ServerSocket serverSocket;

    private Map<Integer, SoundServerClient> clientList = new HashMap<Integer, SoundServerClient>();


    public void startServer() {
        if (isServerStarted) {
            return;
        }
        isServerStarted = true;
        connectionHandlingExecutorService = Executors.newSingleThreadExecutor();
        connectionHandlingExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                setupServer();
            }
        });
    }

    public void stopServer() {
        if (!isServerStarted) {
            return;
        }
        isServerStarted = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error: Unknown Exception while closing socket.");
        }
        connectionHandlingExecutorService.shutdown();

    }

    private void setupServer() {
        /* Try to create server socket */
        try {
            serverSocket = new ServerSocket(8082);
        } catch (IOException e) {
            Log.e(TAG, "Error: Server Socket could not be opened");
        }
        while (isServerStarted) {
            /* Accept incoming client connection */
            try {
                Socket clientSocket = serverSocket.accept();

                SoundServerClient soundServerClient = new SoundServerClient(clientSocket);

                soundServerClient.startCommunication();
            } catch (IOException e) {
                Log.e(TAG, "Error: Server was interrupted. Maybe stopped from external thread.");
            }
        }
    }


}
