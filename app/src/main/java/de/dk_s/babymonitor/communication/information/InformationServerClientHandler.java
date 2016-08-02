package de.dk_s.babymonitor.communication.information;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Deque;

import de.dk_s.babymonitor.communication.HttpCommunicationHelper;
import de.dk_s.babymonitor.communication.WsCommunicationHelper;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public class InformationServerClientHandler implements Runnable {

    private static final String TAG = "InformationServerClientHandler";

    private Socket clientSocket;

    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    private BabyVoiceMonitor babyVoiceMonitor = null;

    public InformationServerClientHandler(Socket clientSocket, BabyVoiceMonitor babyVoiceMonitor) {
        this.clientSocket = clientSocket;
        this.babyVoiceMonitor = babyVoiceMonitor;
    }


    @Override
    public void run() {
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();

            String httpRequest = HttpCommunicationHelper.readHttpRequestResponse(new InputStreamReader(inputStream));
            String firstLine = httpRequest.split("\r\n")[0];
            String[] firstLineSplit = firstLine.split(" ");
            String url = null;
            if(firstLineSplit.length >= 3) {
                url = firstLineSplit[1];
            } else {
                throw new Exception("Error: No valid request");
            }
            if(url.equals("/history")) {
                String response = generateAudioEventHistoryResponse();
                HttpCommunicationHelper.sendHttpResponse(200, "OK", response, clientSocket);
            } else {
                throw new Exception("Error: No valid URL.");
            }
            inputStream.close();
            outputStream.close();
            clientSocket.close();
        } catch (Exception e) {
            Log.e(TAG, "Error: Exception while handling information retreiving client.");
            try {
                inputStream.close();
                outputStream.close();
                clientSocket.close();
            } catch (IOException e1) {
                Log.e(TAG, "Error: Exception while closing sockets.");
            }
        }
    }


    private String generateAudioEventHistoryResponse() {
        StringBuilder response = new StringBuilder();
        Deque<BabyVoiceMonitor.AudioEvent> recentAudioEventList = babyVoiceMonitor.getRecentAudioEventList();
        for(BabyVoiceMonitor.AudioEvent audioEvent : recentAudioEventList) {
            response.append(audioEvent.getEventType());
            response.append(",");
            response.append(audioEvent.getTimeStamp());
            response.append(",");
            response.append(audioEvent.getAudioLevel());
            response.append(";");
        }
        return response.toString();
    }

}
