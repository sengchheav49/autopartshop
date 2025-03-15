package com.example.autopartsshop.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.autopartsshop.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Enhanced utility class for sharing the SQLite database between devices.
 * This class provides methods to:
 * 1. Export/import the full database
 * 2. Track changes to the database
 * 3. Sync specific changes over the network to other devices
 * 4. Discover other devices on the same network
 */
public class DatabaseSyncUtils {
    private static final String TAG = "DatabaseSyncUtils";
    private static final String DB_NAME = "autoparts_db";
    private static final String EXPORT_DIR = "AutoPartsShop/Database";
    private static final String SYNC_WORK_NAME = "database_sync_work";
    
    // Network discovery settings
    private static final int PORT = 8888;
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    private static final String DEVICE_DISCOVERY_MESSAGE = "AUTO_PARTS_DEVICE";
    
    // Network communication
    private static OkHttpClient httpClient;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    // In-memory cache of discovered devices
    private static Map<String, DeviceInfo> discoveredDevices = new HashMap<>();
    
    // Executor for background tasks
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    
    // Change tracking
    private static final Map<String, Long> lastSyncTimes = new HashMap<>();

    /**
     * Represents a discovered device on the network
     */
    public static class DeviceInfo {
        public String ipAddress;
        public int port;
        public String deviceName;
        public long lastSeen;
        
        public DeviceInfo(String ipAddress, int port, String deviceName) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.deviceName = deviceName;
            this.lastSeen = System.currentTimeMillis();
        }
    }

    /**
     * Initializes the sync utilities
     */
    public static void init(Context context) {
        // Initialize HTTP client for data transfer
        httpClient = new OkHttpClient.Builder().build();
        
        // Start device discovery in background
        startDeviceDiscovery(context);
    }

    /**
     * Exports the current database to external storage.
     * @param context Application context
     * @return Uri of the exported file or null if export failed
     */
    public static Uri exportDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath(DB_NAME);
            if (!dbFile.exists()) {
                Log.e(TAG, "Database file not found");
                return null;
            }

            File exportDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), EXPORT_DIR);
            if (!exportDir.exists()) {
                if (!exportDir.mkdirs()) {
                    Log.e(TAG, "Failed to create export directory");
                    return null;
                }
            }

            // Create a timestamped filename for the export
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File exportFile = new File(exportDir, DB_NAME + "_" + timestamp + ".db");

            // Copy the database file
            copyFile(dbFile, exportFile);

            // Return the URI to the exported file
            return Uri.fromFile(exportFile);
        } catch (IOException e) {
            Log.e(TAG, "Error exporting database: " + e.getMessage());
            return null;
        }
    }

    /**
     * Imports a database file from the given Uri.
     * @param context Application context
     * @param sourceUri Uri of the database file to import
     * @return true if import was successful, false otherwise
     */
    public static boolean importDatabase(Context context, Uri sourceUri) {
        try {
            File dbFile = context.getDatabasePath(DB_NAME);

            // Close any open database connections
            context.deleteDatabase(DB_NAME);

            // Copy the database file from the Uri
            File tempFile = new File(context.getCacheDir(), "temp_import.db");
            copyFromUri(context, sourceUri, tempFile);
            
            // Copy the file to the database location
            copyFile(tempFile, dbFile);
            
            // Delete the temporary file
            if (!tempFile.delete()) {
                Log.w(TAG, "Failed to delete temporary file");
            }
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error importing database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Start listening for other devices on the same network
     */
    private static void startDeviceDiscovery(Context context) {
        executor.execute(() -> {
            try {
                // Start multicasting to announce our presence
                announcePresence();
                
                // Listen for other devices
                listenForDevices();
            } catch (Exception e) {
                Log.e(TAG, "Error in device discovery: " + e.getMessage());
            }
        });
    }
    
    /**
     * Announce this device's presence on the network
     */
    private static void announcePresence() {
        executor.execute(() -> {
            try {
                // Create a JSON message with device info
                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("type", "ANNOUNCE");
                deviceInfo.put("deviceName", android.os.Build.MODEL);
                deviceInfo.put("port", PORT);
                
                byte[] sendData = deviceInfo.toString().getBytes();
                
                DatagramSocket socket = new DatagramSocket();
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(
                        sendData, sendData.length, group, PORT);
                
                // Send announcement periodically
                while (!Thread.currentThread().isInterrupted()) {
                    socket.send(packet);
                    Thread.sleep(30000); // Announce every 30 seconds
                }
            } catch (Exception e) {
                Log.e(TAG, "Error announcing presence: " + e.getMessage());
            }
        });
    }
    
    /**
     * Listen for other devices on the network
     */
    private static void listenForDevices() {
        executor.execute(() -> {
            try {
                MulticastSocket socket = new MulticastSocket(PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);
                
                byte[] buffer = new byte[1024];
                
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    String message = new String(packet.getData(), 0, packet.getLength());
                    processDiscoveryMessage(message, packet.getAddress().getHostAddress());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error listening for devices: " + e.getMessage());
            }
        });
    }
    
    /**
     * Process a discovery message from another device
     */
    private static void processDiscoveryMessage(String message, String ipAddress) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");
            
            if ("ANNOUNCE".equals(type)) {
                String deviceName = json.getString("deviceName");
                int port = json.getInt("port");
                
                // Store or update the device info
                DeviceInfo deviceInfo = new DeviceInfo(ipAddress, port, deviceName);
                discoveredDevices.put(ipAddress, deviceInfo);
                
                Log.d(TAG, "Discovered device: " + deviceName + " at " + ipAddress);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing discovery message: " + e.getMessage());
        }
    }
    
    /**
     * Get list of discovered devices
     */
    public static List<DeviceInfo> getDiscoveredDevices() {
        // Clean up old devices (not seen in last 2 minutes)
        long now = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, DeviceInfo> entry : discoveredDevices.entrySet()) {
            if (now - entry.getValue().lastSeen > 120000) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (String key : toRemove) {
            discoveredDevices.remove(key);
        }
        
        // Return the current list
        return new ArrayList<>(discoveredDevices.values());
    }

    /**
     * Schedule synchronization of database changes
     */
    public static void scheduleDatabaseSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(DatabaseSyncWorker.class)
                .setConstraints(constraints)
                .build();
        
        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        SYNC_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        syncWorkRequest);
    }
    
    /**
     * Worker class for database synchronization
     */
    public static class DatabaseSyncWorker extends Worker {
        public DatabaseSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }
        
        @NonNull
        @Override
        public Result doWork() {
            // Get changes since last sync
            JSONObject changes = getRecentChanges(getApplicationContext());
            
            // Send changes to all discovered devices
            for (DeviceInfo device : getDiscoveredDevices()) {
                sendChangesToDevice(device, changes);
            }
            
            return Result.success();
        }
        
        /**
         * Get recent changes from the database
         */
        private JSONObject getRecentChanges(Context context) {
            JSONObject changes = new JSONObject();
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            try {
                // Add changes from each table
                changes.put("products", getTableChanges(db, DatabaseHelper.TABLE_PRODUCTS));
                changes.put("brands", getTableChanges(db, DatabaseHelper.TABLE_BRANDS));
                // Add other tables as needed
                
            } catch (JSONException e) {
                Log.e(TAG, "Error creating changes JSON: " + e.getMessage());
            } finally {
                db.close();
            }
            
            return changes;
        }
        
        /**
         * Get changes from a specific table
         */
        private JSONArray getTableChanges(SQLiteDatabase db, String tableName) {
            JSONArray tableChanges = new JSONArray();
            long lastSyncTime = lastSyncTimes.getOrDefault(tableName, 0L);
            
            String query = "SELECT * FROM " + tableName + 
                    " WHERE datetime(" + DatabaseHelper.KEY_CREATED_AT + ") > datetime(?)";
            
            // Convert timestamp to SQLite datetime format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String lastSyncTimeStr = sdf.format(new Date(lastSyncTime));
            
            try (Cursor cursor = db.rawQuery(query, new String[]{lastSyncTimeStr})) {
                int colCount = cursor.getColumnCount();
                String[] colNames = cursor.getColumnNames();
                
                while (cursor.moveToNext()) {
                    JSONObject row = new JSONObject();
                    
                    for (int i = 0; i < colCount; i++) {
                        String colName = colNames[i];
                        
                        switch (cursor.getType(i)) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                row.put(colName, cursor.getLong(i));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                row.put(colName, cursor.getDouble(i));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                row.put(colName, cursor.getString(i));
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                row.put(colName, ""); // Skip BLOBs for now, or encode as needed
                                break;
                            case Cursor.FIELD_TYPE_NULL:
                                row.put(colName, JSONObject.NULL);
                                break;
                        }
                    }
                    
                    tableChanges.put(row);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting changes for table " + tableName + ": " + e.getMessage());
            }
            
            // Update last sync time for this table
            lastSyncTimes.put(tableName, System.currentTimeMillis());
            
            return tableChanges;
        }
        
        /**
         * Send changes to a specific device
         */
        private void sendChangesToDevice(DeviceInfo device, JSONObject changes) {
            try {
                String url = "http://" + device.ipAddress + ":" + device.port + "/sync";
                
                RequestBody body = RequestBody.create(changes.toString(), JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Failed to send changes to " + device.deviceName + 
                                ": " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending changes to " + device.deviceName + 
                        ": " + e.getMessage());
            }
        }
    }

    /**
     * Apply changes received from another device
     */
    public static boolean applyReceivedChanges(Context context, JSONObject changes) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            // Apply changes to each table
            if (changes.has("products")) {
                applyTableChanges(db, DatabaseHelper.TABLE_PRODUCTS, changes.getJSONArray("products"));
            }
            
            if (changes.has("brands")) {
                applyTableChanges(db, DatabaseHelper.TABLE_BRANDS, changes.getJSONArray("brands"));
            }
            
            // Add other tables as needed
            
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error applying changes: " + e.getMessage());
            return false;
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
            db.close();
        }
    }
    
    /**
     * Apply changes to a specific table
     */
    private static void applyTableChanges(SQLiteDatabase db, String tableName, JSONArray changes) 
            throws JSONException {
        for (int i = 0; i < changes.length(); i++) {
            JSONObject row = changes.getJSONObject(i);
            ContentValues values = new ContentValues();
            
            // Extract values from JSON
            for (int j = 0; j < row.names().length(); j++) {
                String name = row.names().getString(j);
                
                if (!row.isNull(name)) {
                    Object value = row.get(name);
                    
                    if (value instanceof String) {
                        values.put(name, (String) value);
                    } else if (value instanceof Integer) {
                        values.put(name, (Integer) value);
                    } else if (value instanceof Long) {
                        values.put(name, (Long) value);
                    } else if (value instanceof Double) {
                        values.put(name, (Double) value);
                    } else if (value instanceof Boolean) {
                        values.put(name, (Boolean) value ? 1 : 0);
                    }
                }
            }
            
            // Check if record exists
            if (row.has(DatabaseHelper.KEY_ID)) {
                long id = row.getLong(DatabaseHelper.KEY_ID);
                String whereClause = DatabaseHelper.KEY_ID + " = ?";
                String[] whereArgs = new String[]{String.valueOf(id)};
                
                // Update if exists, insert if not
                int affected = db.update(tableName, values, whereClause, whereArgs);
                if (affected == 0) {
                    db.insert(tableName, null, values);
                }
            } else {
                db.insert(tableName, null, values);
            }
        }
    }

    /**
     * Copies a file from a Uri to a destination file.
     */
    private static void copyFromUri(Context context, Uri sourceUri, File destFile) throws IOException {
        try (FileOutputStream out = new FileOutputStream(destFile)) {
            // Open an input stream from the Uri
            try (FileInputStream in = (FileInputStream) context.getContentResolver().openInputStream(sourceUri)) {
                if (in == null) {
                    throw new IOException("Cannot open input stream from Uri");
                }
                
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                out.flush();
            }
        }
    }

    /**
     * Copies a file from source to destination.
     */
    private static void copyFile(File src, File dst) throws IOException {
        try (FileChannel inChannel = new FileInputStream(src).getChannel();
             FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }
} 