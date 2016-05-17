package de.dk_s.babymonitor.monitoring;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that streams sound data over websockets
 */
public class WsSoundServer {

    ServerSocket serverSocket;

    private class ConnectionHandlingRunnable implements Runnable {

        private final char[] httpRequestLimiter = new char[] { '\r', '\n', '\r', '\n' };

        @Override
        public void run() {
            /* Try to create server socket */
            try {
                serverSocket = new ServerSocket(8082);
            } catch (IOException e) {
                Log.e(TAG, "Error: Server Socket could not be opened");
            }
            while (isServerStarted) {
                /* Accept incoming client connection */
                try {
                    Socket clientSocket = serverSocket.accept();
                    InputStream inputStream = clientSocket.getInputStream();
                    OutputStream outputStream = clientSocket.getOutputStream();
                    handleWsHandshake(inputStream, outputStream);
                } catch (IOException e) {
                    Log.e(TAG, "Error: Server was interrupted. Maybe stopped from external thread.");
                }
            }
        }

        private void handleWsHandshake(InputStream inputStream, OutputStream outputStream) {
            /* Create reader */
            InputStreamReader reader = new InputStreamReader(inputStream);
            /* Read whole request */
            boolean isRequestRead = false;
            Queue<Character> characterQueue = new LinkedList<Character>();
            Character[] lastFourCharactersArray = new Character[4];
            StringBuilder request = new StringBuilder();
            while (isRequestRead == false) {
                /* Try reading character from stream */
                try {
                    char currentCharacter = (char)reader.read();
                    request.append(currentCharacter);
                    characterQueue.add(currentCharacter);
                    if(characterQueue.size() > 4) {
                        characterQueue.remove();
                    }
                    characterQueue.toArray(lastFourCharactersArray);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static final String TAG = "WsSoundServer";

    private boolean isServerStarted = false;

    private ExecutorService connectionHandlingExecutorService;

    public void startServer() {
        if (isServerStarted) {
            return;
        }
        isServerStarted = true;
        connectionHandlingExecutorService = Executors.newSingleThreadExecutor();
        connectionHandlingExecutorService.submit(new ConnectionHandlingRunnable());
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
        connectionHandlingExecutorService.shutdown();

    }


}
