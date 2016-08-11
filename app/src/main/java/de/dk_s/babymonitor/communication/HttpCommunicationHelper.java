package de.dk_s.babymonitor.communication;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.Arrays;

public class HttpCommunicationHelper {

    private static final String TAG = "HttpCommunicationHelper";

    private final static char[] httpRequestLimiter = new char[]{'\r', '\n', '\r', '\n'};

    /* ------------------------------ Server ------------------------------ */
    public static String readHttpRequestResponseHeader(Reader reader) {
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

    /* ------------------------------ Client ------------------------------ */
    public static void sendHttpGetRequest(String url, String host, Socket socket) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("GET ");
        request.append(url);
        request.append(" HTTP/1.1\r\nHost: ");
        request.append(host);
        request.append("\r\n\r\n");
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(request.toString().getBytes());
        outputStream.flush();
    }

    public static String readHttpReponseBodyAsString(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        Reader inputStreamReader = new InputStreamReader(inputStream);
        String header = readHttpRequestResponseHeader(inputStreamReader);
        String[] lineSplit = header.split("\r\n");
        int contentLength = 0;
        for (String line : lineSplit) {
            String[] keyValuePair = line.split(":");
            if(keyValuePair[0].equals("Content-Length")) {
                contentLength = Integer.parseInt(keyValuePair[1].trim());
            }
        }
        StringBuilder body = new StringBuilder();
        for(int i = 0; i < contentLength; i++) {
            int currentByte = inputStreamReader.read();
            char currentCharacter = (char) currentByte;
            body.append(currentCharacter);
        }
        return body.toString();
    }

}
