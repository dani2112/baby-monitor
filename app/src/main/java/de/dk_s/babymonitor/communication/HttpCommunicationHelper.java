package de.dk_s.babymonitor.communication;


import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.Arrays;

public class HttpCommunicationHelper {

    private static final String TAG = "HttpCommunicationHelper";

    private final static char[] httpRequestLimiter = new char[]{'\r', '\n', '\r', '\n'};

    public static String readHttpRequestResponse(Reader reader) {
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

    public static void sendHttpResponse(int statusCode, String statusMessage, String body, Socket socket) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ");
        response.append(statusCode);
        response.append(" ");
        response.append(statusMessage);
        response.append("\r\n");
        response.append("Content-Length: ");
        response.append(body.length());
        response.append("\r\n");
        response.append("Content-Type: text/html\r\n\r\n");
        response.append(body);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(response.toString().getBytes());
        outputStream.flush();
    }


}
