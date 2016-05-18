package de.dk_s.babymonitor.monitoring;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Class that sets up websocket connection
 */
public class WsSetupHelper {

    private static final String TAG = "WsSetupHelper";

    private final static char[] httpRequestLimiter = new char[]{'\r', '\n', '\r', '\n'};

    public static boolean handleWsHandshake(InputStream inputStream, OutputStream outputStream) {
        /* Create reader for reading text data */
        InputStreamReader reader = new InputStreamReader(inputStream);
        String request = readHttpRequestResponse(reader);
        String key = null;
        int responseCode = validateRequest(request, key);
        if(responseCode == -1) {
            return false;
        }
        if(responseCode == 101) {

        } else {
            Log.e(TAG, "Invalid request");
            sendHttpErrorResponse(responseCode, outputStream);
        }
        return true;
    }

    private static String readHttpRequestResponse(Reader reader) {
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

    private static int validateRequest(String request, String key) {
        /* Check if request was sent by phone that wants to get audio data */
        if(request.equals("PHONE\r\n\r\n")) {
            return -1;
        }

        Scanner scanner = new Scanner(request);
        String currentToken = "";

        /* Check if method GET is used */
        currentToken = scanner.next();
        if (!currentToken.equals("GET")) {
            scanner.close();
            return 405;
        }
            /* Check if location is valid */
        currentToken = scanner.next();
        if (!currentToken.equals("/audio-stream")) {
            scanner.close();
            return 404;
        }
            /* Check if protocol is valid */
        currentToken = scanner.next();
        if (!currentToken.equals("HTTP/1.1")) {
            scanner.close();
            return 400;
        }

            /* Go to next line (Skip \r\n in first line) */
        scanner.nextLine();

            /* Find Upgrade Header */
        boolean upgradeHeaderExists = false;
        boolean keyHeaderExists = false;
        while (scanner.hasNextLine()) {
            currentToken = scanner.nextLine();
            if (currentToken.equals("Upgrade: websocket")) {
                upgradeHeaderExists = true;
            }
            if (currentToken.startsWith("Sec-WebSocket-Accept:")) {
                String[] keyHeaderSplit = currentToken.split(" ");
                if (keyHeaderSplit.length != 2) {
                    scanner.close();
                    return 400;
                }
                key = keyHeaderSplit[1];
                keyHeaderExists = true;
            }
        }
        scanner.close();
        if (!(keyHeaderExists && upgradeHeaderExists)) {
            scanner.close();
            return 400;
        }
        return 101;
    }

    /**
     * Method that sends HTTP response corresponding to a specific error code.
     * @param errorCode the error code
     * @param outputStream the stream that is used for sending the response
     */
    private static void sendHttpErrorResponse(int errorCode, OutputStream outputStream) {
        String errorMessage;
        if(errorCode == 400) {
            /* Bad request was sent by client */
            errorMessage = "BAD REQUEST";
        } else {
            /* Some unhandled error happened */
            errorMessage = "UNKNOWN ERROR";
        }
        String responseString = "HTTP/1.1 " + errorCode + " " + errorMessage +  "\r\nContent-Length: 0\r\n" + "Connection: close\r\n\r\n";
        try {
            outputStream.write(responseString.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while sending HTTP error message.");
        }
    }

}
