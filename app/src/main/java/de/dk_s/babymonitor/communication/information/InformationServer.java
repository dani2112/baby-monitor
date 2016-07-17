package de.dk_s.babymonitor.communication.information;


import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dk_s.babymonitor.client.InformationClient;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public class InformationServer {

    /* Tag for debugging outputs */
    private static final String TAG = "InformationServer";

    /* Flag that indicates if server is started */
    private boolean isServerStarted = false;

    /* ExecutorService that runs thread for accepting connections */
    private ExecutorService serverExecutorService;

    /* Server socket that listens for incoming connections */
    private ServerSocket serverSocket;

    /* ExecutorService that runs the client connections */
    private ExecutorService clientConnectionExecutorService;

    /* BabyVoiceMonitor object that contains necessary information */
    private BabyVoiceMonitor babyVoiceMonitor;

    public InformationServer(BabyVoiceMonitor babyVoiceMonitor) {
        this.babyVoiceMonitor = babyVoiceMonitor;
    }


    public void startServer() {
        if (isServerStarted) {
            return;
        }
        isServerStarted = true;

          /* Set up client handling */
        clientConnectionExecutorService = Executors.newCachedThreadPool();

 /* Try to create server socket */
        try {
            serverSocket = new ServerSocket(8083);
            serverExecutorService = Executors.newSingleThreadExecutor();
            serverExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    acceptConnections();
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error: Server Socket could not be opened");
        }
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
        serverExecutorService.shutdownNow();
        clientConnectionExecutorService.shutdownNow();
    }


    private void acceptConnections() {
        while (isServerStarted) {
            /* Accept incoming client connection */
            try {
                /* Accept client connection */
                Socket clientSocket = serverSocket.accept();

                /* Create new client object */
                InformationServerClientHandler informationServerClientHandler = new InformationServerClientHandler(clientSocket, babyVoiceMonitor);

                /* Run client connection  */
                clientConnectionExecutorService.submit(informationServerClientHandler);
            } catch (IOException e) {
                Log.e(TAG, "Error: Server was interrupted. Maybe stopped from external thread.");
            }
        }
    }


}
