package de.dk_s.babymonitor.communication.information;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import de.dk_s.babymonitor.communication.WsCommunicationHelper;

public class InformationServerClientHandler implements Runnable {

    private static final String TAG = "InformationServerClientHandler";

    private Socket clientSocket;

    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    public InformationServerClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
                String command = new String(cmdData, StandardCharsets.UTF_8);
                Log.e(TAG,command);

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
}
