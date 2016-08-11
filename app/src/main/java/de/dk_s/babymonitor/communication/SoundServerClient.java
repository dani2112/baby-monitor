package de.dk_s.babymonitor.communication;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.dk_s.babymonitor.monitoring.MicRecorder;

public class SoundServerClient implements Runnable, Observer {

    private static final String TAG = "SoundServerClient";

    private Socket clientSocket;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    private BlockingQueue<MicRecorder.AudioChunk> audioChunksQueue = new LinkedBlockingQueue<MicRecorder.AudioChunk>();

    private int maxQueueSize = 5;

    private boolean isRunning = true;

    private long lastAck;

    public SoundServerClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.lastAck = System.currentTimeMillis();
    }

    public void stopCommunication() {
        try {
            isRunning = false;
            inputStream.close();
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAck > 200000) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void run() {
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isWsConnection = WsCommunicationHelper.handleWsHandshakeServer(inputStream, outputStream);

        Log.e(TAG, "Running");
        while (isRunning) {
            try {
                MicRecorder.AudioChunk currentChunk = audioChunksQueue.take();
                WsCommunicationHelper.sendDataServer(130, currentChunk.getChunkData8Bit(), outputStream);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error: Exception while sending audio data. Maybe threadwas shutdown.");
            }
        }
        Log.e(TAG, "QUIT");
    }

    @Override
    public void update(Observable observable, Object data) {
        MicRecorder.AudioChunk audioChunk = (MicRecorder.AudioChunk) data;
        if (audioChunksQueue.size() < maxQueueSize) {
            audioChunksQueue.add(audioChunk);
        } else {
            audioChunksQueue.poll(); // remove with poll otherwise exception is thrown if element was processed in the meanwhile
            audioChunksQueue.add(audioChunk);
        }
    }
}
