package de.dk_s.babymonitor.communication.information;

import java.net.Socket;

public class InformationServerClientHandler implements Runnable {

    private Socket clientSocket;

    public InformationServerClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {

    }
}
