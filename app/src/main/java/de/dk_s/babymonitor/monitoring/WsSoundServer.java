package de.dk_s.babymonitor.monitoring;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that streams sound data over websockets
 */
public class WsSoundServer {

    private static final String TAG = "WsSoundServer";

    private boolean isServerStarted = false;

    private ExecutorService connectionHandlingExecutorService;

    private ServerSocket serverSocket;

    private final char[] httpRequestLimiter = new char[]{'\r', '\n', '\r', '\n'};


    public void startServer() {
        if (isServerStarted) {
            return;
        }
        isServerStarted = true;
        connectionHandlingExecutorService = Executors.newSingleThreadExecutor();
        connectionHandlingExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                setupServer();
            }
        });
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

    private void setupServer() {
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
            /* Create reader for reading text data */
        InputStreamReader reader = new InputStreamReader(inputStream);
        String request = readHttpRequestResponse(reader);
        String key = null;
        int responseCode = validateRequest(request, key);
        Log.e(TAG, request.substring(0, 20));
    }

    private String readHttpRequestResponse(Reader reader) {
            /* Read whole request */
        boolean isRequestRead = false;
        StringBuilder request = new StringBuilder();
        char[] lastFourCharacters = new char[4];
        while (isRequestRead == false) {
                /* Try reading character from stream */
            try {
                int currentByte = reader.read();
                char currentCharacter = (char) currentByte;
                request.append(currentCharacter);
                if (request.length() >= 4) {
                    request.getChars(request.length() - 4, request.length(), lastFourCharacters, 0);
                    if (Arrays.equals(lastFourCharacters, httpRequestLimiter)) {
                        isRequestRead = true;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error: Parser error while parsing request.");
            }
        }
        return request.toString();
    }

    private int validateRequest(String request, String key) {
        Scanner scanner = new Scanner(request);
        String currentToken = "";

            /* Check if method GET is used */
        currentToken = scanner.next();
        if(!currentToken.equals("GET")) {
            scanner.close();
            return 405;
        }
            /* Check if location is valid */
        currentToken = scanner.next();
        if(!currentToken.equals("/audio-stream")) {
            scanner.close();
            return 404;
        }
            /* Check if protocol is valid */
        currentToken = scanner.next();
        if(!currentToken.equals("HTTP/1.1")) {
            scanner.close();
            return 400;
        }

            /* Go to next line (Skip \r\n in first line) */
        scanner.nextLine();

            /* Find Upgrade Header */
        boolean upgradeHeaderExists = false;
        boolean keyHeaderExists = false;
        while(scanner.hasNextLine()) {
            currentToken = scanner.nextLine();
            if(currentToken.equals("Upgrade: websocket")) {
                upgradeHeaderExists = true;
            }
            if(currentToken.startsWith("Sec-WebSocket-Accept:")) {
                String[] keyHeaderSplit = currentToken.split(" ");
                if(keyHeaderSplit.length != 2) {
                    scanner.close();
                    return 400;
                }
                key = keyHeaderSplit[1];
                keyHeaderExists = true;
            }
        }
        scanner.close();
        if(!(keyHeaderExists && upgradeHeaderExists)) {
            scanner.close();
            return 400;
        }
        return 101;
    }


}
