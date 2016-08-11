package de.dk_s.babymonitor.client;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dk_s.babymonitor.communication.HttpCommunicationHelper;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public class InformationClient {

    /* Tag for debugging outputs */
    private static final String TAG = "InformationClient";

    /* String constant for broadcasting information update */
    static final public String EVENT_INFORMATION_UPDATED = "de.dk_s.babymonitor.client.InformationClient.EventInformationUpdated";

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

    /* Context that is needed for broadcasting */
    private Context context;

    /* Broadcast manager that is needed for broadcasting event list changes */
    private LocalBroadcastManager localBroadcastManager = null;

    /* Last event timestamp */
    private long lastEventTimestamp = -1;

    public InformationClient(String serverAddress, Context context) {
        this.serverAddress = serverAddress;
        this.context = context;
    }

    public void startClient() {
        if (isClientStarted) {
            return;
        }
        isClientStarted = true;

        localBroadcastManager = LocalBroadcastManager.getInstance(context);

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

        localBroadcastManager = null;

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
                broadcastInformationUpdateIfNecessary();
                updateNotificationIfNecessary();
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

    private void updateNotificationIfNecessary() {
        BabyVoiceMonitor.AudioEvent lastEvent = eventHistoryList.get(0);
        if(lastEvent.getEventType() == 1 || lastEvent.getEventType() == 2) {
            if(context instanceof ConnectionService) {
                ConnectionService connectionService = (ConnectionService)context;
                connectionService.updateNotification("Alarm!", "Das Baby schreit!");
            }
        } else {
            if(context instanceof  ConnectionService) {
                ConnectionService connectionService = (ConnectionService)context;
                connectionService.updateNotification("Alles Ruhig...", "Kein Alarm ist aktiv.");
            }
        }
    }

    private void broadcastInformationUpdateIfNecessary() {
         /* Check if event list update has to be broadcastet */
        if(eventHistoryList != null && eventHistoryList.size() > 0) {
            BabyVoiceMonitor.AudioEvent firstElement = eventHistoryList.get(0);
            if(firstElement.getTimeStamp() != lastEventTimestamp) {
                int newElementCount = 0;
                ListIterator<BabyVoiceMonitor.AudioEvent> iterator = eventHistoryList.listIterator(0);
                while (iterator.hasNext()) {
                    BabyVoiceMonitor.AudioEvent currentEvent = iterator.next();
                    if(currentEvent.getTimeStamp() <= lastEventTimestamp) {
                        break;
                    }
                    newElementCount++;
                }
                lastEventTimestamp = firstElement.getTimeStamp();
                Intent intent = new Intent(EVENT_INFORMATION_UPDATED);
                intent.putExtra("NEW_ELEMENT_COUNT", newElementCount);
                localBroadcastManager.sendBroadcast(intent);
            }
        }
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
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while retrieving audio event history.");
        }
        return eventHistoryList;
    }

    private void broadcastEventHistoryChanged() {
        if(localBroadcastManager != null) {
            Intent intent = new Intent(EVENT_INFORMATION_UPDATED);
            localBroadcastManager.sendBroadcast(intent);
        }
    }
}
