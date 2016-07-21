package de.dk_s.babymonitor.communication.information;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Deque;

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
            boolean result = WsCommunicationHelper.handleWsHandshakeServer(inputStream, outputStream);
            if(!result) {
                throw new Exception("Handshake failed on connection opening.");
            }
            boolean connectionActive = true;
            while(connectionActive) {
                byte[] cmdData = WsCommunicationHelper.receiveDataServer(inputStream);
                Log.e(TAG, "RECEIVE");
                int commandValue = cmdData[0];
                /* Respond on ping command */
                if(commandValue == 0) {
                    Log.e(TAG, "Ping command");
                    sendPingResponse(clientSocket);
                } else if (commandValue == 1) {
                    Log.e(TAG, "Audio event history command");
                    sendAudioEventHistoryResponse(clientSocket);
                }

            }

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

    private void sendPingResponse(Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            byte command = 0;
            byte[] sendData = new byte[] { command };
            WsCommunicationHelper.sendDataServer(130, sendData, outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while sending ping response in server.");
        }
    }

    private void sendAudioEventHistoryResponse(Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            byte command = 1;
            Deque<BabyVoiceMonitor.AudioEvent> recentAudioEventList = babyVoiceMonitor.getRecentAudioEventList();
            int audioEventLength = recentAudioEventList.size();
            byte[] sendData = new byte[1 + audioEventLength * 16];
            ByteBuffer sendDataBuffer = ByteBuffer.wrap(sendData);
            sendData[0] = command;
            int index = 1;
            for(BabyVoiceMonitor.AudioEvent audioEvent : recentAudioEventList) {
                sendDataBuffer.putInt(index, audioEvent.getEventType());
                sendDataBuffer.putLong(index + 4, audioEvent.getTimeStamp());
                sendDataBuffer.putFloat(index + 12, audioEvent.getAudioLevel());
                index++;
            }
            WsCommunicationHelper.sendDataServer(130, sendDataBuffer.array(), outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while sending audio event history response in server.");
        }
    }

}
