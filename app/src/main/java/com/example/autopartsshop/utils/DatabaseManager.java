package com.example.autopartsshop.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private final Context context;
    private final String DATABASE_NAME = "autoparts_db";

    public DatabaseManager(Context context) {
        this.context = context;
    }

    public boolean exportDatabase() {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//com.example.autopartsshop//databases//" + DATABASE_NAME;
                String backupDBPath = "AutoPartsShop_Backup.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(context, "Database exported to Downloads folder", Toast.LENGTH_LONG).show();
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exportDatabase: ", e);
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public boolean importDatabase() {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//com.example.autopartsshop//databases//" + DATABASE_NAME;
                String backupDBPath = "AutoPartsShop_Backup.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (backupDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(context, "Database imported successfully", Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    Toast.makeText(context, "Backup file not found in Downloads folder", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "importDatabase: ", e);
            Toast.makeText(context, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }
} 