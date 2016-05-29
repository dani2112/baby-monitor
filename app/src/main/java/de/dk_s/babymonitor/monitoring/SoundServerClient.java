package de.dk_s.babymonitor.monitoring;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SoundServerClient {

    private Socket clientSocket;

    public SoundServerClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void startCommunication() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isWsConnection = WsCommunicationHelper.handleWsHandshake(inputStream, outputStream);
    }

    public void stopCommunication() {

    }

}
