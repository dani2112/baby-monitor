package de.dk_s.babymonitor.client;


import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dk_s.babymonitor.communication.HttpCommunicationHelper;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public class InformationClient {

    /* Tag for debugging outputs */
    private static final String TAG = "InformationClient";

    /* Flag that indicates if client is started */
    private boolean isClientStarted = false;

    /* ExecutorService that runs communication thread */
    private ExecutorService clientExecutorService;

    /* IP-Address that should be used for the connection */
    private String serverAddress;

    /* Recent audio event history queue that was retreived over network */
    private Deque<BabyVoiceMonitor.AudioEvent> recentAudioEventHistoryDequeue = null;

    /* Event list that is retrieved from the database of remote device */
    private List<BabyVoiceMonitor.AudioEvent> eventHistoryList = null;

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
        clientExecutorService.shutdownNow();
    }

    public Deque<BabyVoiceMonitor.AudioEvent> getRecentAudioEvents() {
        return recentAudioEventHistoryDequeue;
    }

    public List<BabyVoiceMonitor.AudioEvent> getEventHistory() {
        return eventHistoryList;
    }

    private void handleClientConnection() {
        while (isClientStarted) {
            try {
                Thread.sleep(1000);
                recentAudioEventHistoryDequeue = getRecentAudioEventHistoryRemote();
                eventHistoryList = getEventHistoryRemote();
            } catch (Exception e) {
                Log.e(TAG, "Error: Exception while receiving information.");
            }
        }
    }

    private Socket tryConnect(String ipAddress, int port) {
        boolean connectionSucessful;
        Socket socket = null;
        try {
            socket = new Socket(ipAddress, port);
            connectionSucessful = true;
        } catch (IOException e) {
            connectionSucessful = false;
        }
        return connectionSucessful ? socket : null;
    }

    private Deque<BabyVoiceMonitor.AudioEvent> getRecentAudioEventHistoryRemote() {
        Socket clientSocket = tryConnect(serverAddress, 8083);
        if(clientSocket == null) {
            return null;
        }
        Deque<BabyVoiceMonitor.AudioEvent> audioEventDequeue = new LinkedList<>();
        try {
            HttpCommunicationHelper.sendHttpGetRequest("/history", clientSocket.getInetAddress().getHostName(), clientSocket);
            String response = HttpCommunicationHelper.readHttpReponseBodyAsString(clientSocket);
            clientSocket.close();
            String[] reponseSplit = response.split(";");
            for(String audioEventValues : reponseSplit) {
                String[] valuesSplit = audioEventValues.split(",");
                int eventType = Integer.parseInt(valuesSplit[0]);
                long timeStamp = Long.parseLong(valuesSplit[1]);
                float audioLevel = Float.parseFloat(valuesSplit[2]);
                audioEventDequeue.add(new BabyVoiceMonitor.AudioEvent(eventType, timeStamp, audioLevel));
            }
            recentAudioEventHistoryDequeue = audioEventDequeue;
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while retrieving audio event history.");
        }
        return audioEventDequeue;
    }

    private List<BabyVoiceMonitor.AudioEvent> getEventHistoryRemote() {
        Socket clientSocket = tryConnect(serverAddress, 8083);
        if(clientSocket == null) {
            return null;
        }
        List<BabyVoiceMonitor.AudioEvent> eventHistoryList = new LinkedList<>();
        try {
            HttpCommunicationHelper.sendHttpGetRequest("/events", clientSocket.getInetAddress().getHostName(), clientSocket);
            String response = HttpCommunicationHelper.readHttpReponseBodyAsString(clientSocket);
            clientSocket.close();
            String[] reponseSplit = response.split(";");
            for(String audioEventValues : reponseSplit) {
                String[] valuesSplit = audioEventValues.split(",");
                int eventType = Integer.parseInt(valuesSplit[0]);
                long timeStamp = Long.parseLong(valuesSplit[1]);
                eventHistoryList.add(new BabyVoiceMonitor.AudioEvent(eventType, timeStamp));
            }
            this.eventHistoryList = eventHistoryList;
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while retrieving audio event history.");
        }
        return null;
    }
}
