package de.dk_s.babymonitor.client;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import de.dk_s.babymonitor.communication.WsCommunicationHelper;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

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

    /* Flag that indicates if audio event history is requested */
    private boolean isRecentAudioEventHistoryRequested = false;

    /* Semaphore that is used for signaling that new recent audio event data is available */
    private Semaphore isRecentAudioEventHistoryRequestedSemaphore = new Semaphore(0);

    /* Recent audio event history queue that was retreived over network */
    private Deque<BabyVoiceMonitor.AudioEvent> reventAudioEventHistoryDequeue = null;

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

    public Deque<BabyVoiceMonitor.AudioEvent> getRecentAudioEvents() {
        isRecentAudioEventHistoryRequested = true;
        try {
            isRecentAudioEventHistoryRequestedSemaphore.acquire();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error: Interruped while getting recent audio events over remote");
        }
        return reventAudioEventHistoryDequeue;
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
                if(isRecentAudioEventHistoryRequested) {
                    reventAudioEventHistoryDequeue = getReventAudioEventHistoryRemote(clientSocket);
                    isRecentAudioEventHistoryRequestedSemaphore.release();
                }
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

    private Deque<BabyVoiceMonitor.AudioEvent> getReventAudioEventHistoryRemote(Socket socket) {
        Deque<BabyVoiceMonitor.AudioEvent> audioEventDequeue = new LinkedList<>();
        try {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            byte command = 1;
            byte[] sendData = new byte[]{command};
            WsCommunicationHelper.sendDataClient(130, sendData, outputStream);
            byte[] receiveData = WsCommunicationHelper.receiveDataClient(inputStream);
            if (receiveData[0] == 1) {
                /* interpret received data 4 bytes type, 8 bytes timestamp, 4 bytes audio level */
                ByteBuffer byteBuffer = ByteBuffer.wrap(receiveData);
                int historyLength = (receiveData.length - 1) / 16;
                for(int i = 0; i < historyLength; i++) {
                    int eventType = byteBuffer.getInt(i);
                    long timeStamp = byteBuffer.getLong(i + 4);
                    float audioLevel = byteBuffer.getFloat(i + 12);
                    BabyVoiceMonitor.AudioEvent audioEvent = new BabyVoiceMonitor.AudioEvent(eventType, timeStamp, audioLevel);
                    audioEventDequeue.add(audioEvent);
                }
            }
        } catch (IOException e) {

        }
        return audioEventDequeue;
    }
}
