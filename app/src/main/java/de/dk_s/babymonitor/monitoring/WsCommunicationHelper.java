package de.dk_s.babymonitor.monitoring;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Class that sets up websocket connection
 */
public class WsCommunicationHelper {

    private static class HandshakeReturnValue {

        public int returnValue;

        public String key;

    }


    private static final String TAG = "WsCommunicationHelper";

    private final static char[] httpRequestLimiter = new char[]{'\r', '\n', '\r', '\n'};

    /**
     * Handles the necessary handshake for opening a websocket connection
     *
     * @param inputStream  the stream for receiving data
     * @param outputStream the stream for sending data
     * @return true if a websocket connection with a browser was opened, false if it is another connection
     */
    public static boolean handleWsHandshake(InputStream inputStream, OutputStream outputStream) {
        /* Create reader for reading text data */
        InputStreamReader reader = new InputStreamReader(inputStream);
        String request = readHttpRequestResponse(reader);

        HandshakeReturnValue handshakeReturnValue = validateRequest(request);
        if (handshakeReturnValue.returnValue == -1) {
            return false;
        }
        if (handshakeReturnValue.returnValue == 101) {
            String key = computeKeyHash(handshakeReturnValue.key);
            sendWsUpgradeConfirmation(key, outputStream);
        } else {
            Log.e(TAG, "Invalid request");
            sendHttpErrorResponse(handshakeReturnValue.returnValue, outputStream);
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

    private static HandshakeReturnValue validateRequest(String request) {
        HandshakeReturnValue handshakeReturnValue = new HandshakeReturnValue();
        /* Check if request was sent by phone that wants to get audio data */
        if (request.equals("PHONE\r\n\r\n")) {
            handshakeReturnValue.returnValue = -1;
            return handshakeReturnValue;
        }

        Scanner scanner = new Scanner(request);
        String currentToken = "";

        /* Check if method GET is used */
        currentToken = scanner.next();
        if (!currentToken.equals("GET")) {
            scanner.close();
            handshakeReturnValue.returnValue = 405;
            return handshakeReturnValue;
        }
            /* Check if location is valid */
        currentToken = scanner.next();
        if (!currentToken.equals("/audio-stream")) {
            scanner.close();
            handshakeReturnValue.returnValue = 404;
            return handshakeReturnValue;
        }
            /* Check if protocol is valid */
        currentToken = scanner.next();
        if (!currentToken.equals("HTTP/1.1")) {
            scanner.close();
            handshakeReturnValue.returnValue = 400;
            return handshakeReturnValue;
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
            if (currentToken.startsWith("Sec-WebSocket-Key:")) {
                String[] keyHeaderSplit = currentToken.split(" ");
                if (keyHeaderSplit.length != 2) {
                    scanner.close();
                    handshakeReturnValue.returnValue = 400;
                    return handshakeReturnValue;
                }
                handshakeReturnValue.key = keyHeaderSplit[1];
                keyHeaderExists = true;
            }
        }
        scanner.close();
        if (!(keyHeaderExists && upgradeHeaderExists)) {
            scanner.close();
            handshakeReturnValue.returnValue = 400;
            return handshakeReturnValue;
        }
        handshakeReturnValue.returnValue = 101;
        return handshakeReturnValue;
    }

    /**
     * Method that sends HTTP response corresponding to a specific error code.
     *
     * @param errorCode    the error code
     * @param outputStream the stream that is used for sending the response
     */
    private static void sendHttpErrorResponse(int errorCode, OutputStream outputStream) {
        String errorMessage;
        if (errorCode == 400) {
            /* Bad request was sent by client */
            errorMessage = "BAD REQUEST";
        } else {
            /* Some unhandled error happened */
            errorMessage = "UNKNOWN ERROR";
        }
        String responseString = "HTTP/1.1 " + errorCode + " " + errorMessage + "\r\nContent-Length: 0\r\n" + "Connection: close\r\n\r\n";
        try {
            outputStream.write(responseString.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while sending HTTP error message.");
        }
    }

    private static String computeKeyHash(String key) {
        String hashedKey = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] sha1Hash = md.digest(hashedKey.getBytes());
            hashedKey = Base64.encodeToString(sha1Hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error: No SHA-1 algorithm available in java implementation.");
        }
        return hashedKey;
    }

    private static void sendWsUpgradeConfirmation(String hashedKey, OutputStream outputStream) {
        String upgradeAnswer = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + hashedKey + "\r\n" +
                "Sec-WebSocket-Protocol: babymonitor\r\n\r\n";
        try {
            outputStream.write(upgradeAnswer.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error: Exception while sending websocket upgrade confirmation.");
        }
    }

    /* see http://stackoverflow.com/questions/8125507/how-can-i-send-and-receive-websocket-messages-on-the-server-side */
    public static void sendData(int opCode, byte[] data, OutputStream outputStream) {


    }

    /* see http://stackoverflow.com/questions/8125507/how-can-i-send-and-receive-websocket-messages-on-the-server-side */
    public static void receiveData(byte[] data, OutputStream outputStream) {

    }

}
