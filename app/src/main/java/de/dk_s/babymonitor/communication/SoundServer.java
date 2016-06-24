package de.dk_s.babymonitor.communication;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dk_s.babymonitor.monitoring.MicRecorder;

/**
 * Class that streams sound data over websockets
 */
public class SoundServer {

    /* Tag for debugging outputs */
    private static final String TAG = "SoundServer";

    /* Flag that indicates if server is started */
    private boolean isServerStarted = false;

    /* ExecutorService that runs thread for accepting connections */
    private ExecutorService serverExecutorService;

    /* Server socket that listens for incoming connections */
    private ServerSocket serverSocket;

    /* Integer variable that acts as client id */
    private Integer currentClientId = 1;

    /* Map to keep track of connected clients */
    private Map<Integer, SoundServerClient> clientConnectionMap;

    /* ExecutorService that runs the client connections */
    private ExecutorService clientConnectionExecutorService;

    /* Timer that stops timed out client connections */
    private Timer clientConnectionTimer;

    /* Object that provides sound data */
    private MicRecorder micRecorder;

    public SoundServer(MicRecorder micRecorder) {
        this.micRecorder = micRecorder;
    }

    public void startServer() {
        if (isServerStarted) {
            return;
        }
        isServerStarted = true;
        /* Set up client handling and tracking */
        clientConnectionMap = new HashMap<Integer, SoundServerClient>();
        clientConnectionExecutorService = Executors.newCachedThreadPool();

        /* Set up timer that looks for inactive connections */
        clientConnectionTimer = new Timer();
        clientConnectionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Iterator<Map.Entry<Integer, SoundServerClient>> iterator = clientConnectionMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, SoundServerClient> entry = iterator.next();
                    SoundServerClient currentClient = entry.getValue();
                    Log.e(TAG, String.valueOf(currentClient.isConnected()));
                    if(!currentClient.isConnected()){
                        currentClient.stopCommunication();
                        micRecorder.deleteObserver(currentClient);
                        iterator.remove();
                    }
                }
            }
        }, 0, 5000);

         /* Try to create server socket */
        try {
            serverSocket = new ServerSocket(8082);
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
        micRecorder.deleteObservers();
        clientConnectionTimer.cancel();
        clientConnectionMap.clear();
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
                SoundServerClient soundServerClient = new SoundServerClient(clientSocket);

                /* Register client object as observer */
                micRecorder.addObserver(soundServerClient);

                /* Add it to map that is used to keep track of current connections and increase id counter */
                clientConnectionMap.put(currentClientId, soundServerClient);
                currentClientId++;
                /* Run client connection  */
                clientConnectionExecutorService.submit(soundServerClient);
            } catch (IOException e) {
                Log.e(TAG, "Error: Server was interrupted. Maybe stopped from external thread.");
            }
        }
    }


}
