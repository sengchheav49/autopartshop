package com.example.autopartsshop.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service that handles HTTP communication for database synchronization.
 * This service starts an HTTP server to listen for sync requests from other devices.
 */
public class DatabaseSyncService extends Service {
    private static final String TAG = "DatabaseSyncService";
    private static final int SERVER_PORT = 8888; // Same as in DatabaseSyncUtils
    
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private ExecutorService threadPool;

    @Override
    public void onCreate() {
        super.onCreate();
        threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * Start the HTTP server
     */
    private void startServer() {
        isRunning = true;
        threadPool.execute(() -> {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                Log.d(TAG, "Server started on port: " + SERVER_PORT);
                
                while (isRunning) {
                    final Socket clientSocket = serverSocket.accept();
                    threadPool.execute(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error starting server: " + e.getMessage());
            }
        });
    }
    
    /**
     * Stop the HTTP server
     */
    private void stopServer() {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server: " + e.getMessage());
            }
        }
        threadPool.shutdown();
    }
    
    /**
     * Handle a client connection
     */
    private void handleClient(Socket clientSocket) {
        try {
            // Read the request
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            
            // Parse HTTP headers
            String line;
            StringBuilder requestBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }
            
            // Read the request body (JSON data)
            int contentLength = 0;
            String[] requestLines = requestBuilder.toString().split("\r\n");
            for (String header : requestLines) {
                if (header.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(header.substring(16).trim());
                    break;
                }
            }
            
            char[] body = new char[contentLength];
            if (contentLength > 0) {
                reader.read(body, 0, contentLength);
            }
            
            String requestBody = new String(body);
            
            // Process the request based on the path
            boolean success = false;
            String firstLine = requestLines[0];
            
            if (firstLine.contains("/sync") && firstLine.startsWith("POST")) {
                // Handle sync request
                try {
                    JSONObject changes = new JSONObject(requestBody);
                    success = DatabaseSyncUtils.applyReceivedChanges(getApplicationContext(), changes);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing sync request: " + e.getMessage());
                }
            }
            
            // Send response
            String response;
            if (success) {
                response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: 15\r\n" +
                        "\r\n" +
                        "{\"status\":\"ok\"}";
            } else {
                response = "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: 19\r\n" +
                        "\r\n" +
                        "{\"status\":\"error\"}";
            }
            
            OutputStream output = clientSocket.getOutputStream();
            output.write(response.getBytes(StandardCharsets.UTF_8));
            output.flush();
            
            // Close the connection
            clientSocket.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling client: " + e.getMessage());
        }
    }
} 