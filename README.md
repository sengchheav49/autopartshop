# Auto Parts Shop App

## 🚀 Technologies and Tools
<div align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" height="30" alt="java logo"  />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/androidstudio/androidstudio-original.svg" height="30" alt="jetpackcompose logo"  />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/sqlite/sqlite-original.svg" height="30" alt="sqlite logo"  />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/figma/figma-original.svg" height="30" alt="figma logo"  />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg" height="30" alt="git logo"  />
  <img width="12" />
  <img src="https://skillicons.dev/icons?i=github" height="30" alt="github logo"  />
  <img width="12" />
</div>

## Database Sharing Features

This app implements database sharing to ensure that product edits, images, and other data are preserved when sharing the app with other devices.

### How Database Sharing Works

1. **Automatic Sharing on App Start:**
   - The app automatically saves a copy of your database in the app's external storage directory.
   - This copy is included when you zip and share the project with others.
   - When someone else installs the app from your project, they will automatically get your database with all your product edits and images.

2. **Manual Export/Import:**
   - From the main menu, select "Export Database" to save the current database to your Downloads folder.
   - On another device, select "Import Database" to load a database from the Downloads folder.

### Instructions for Sharing the App with Another Device

1. **Method 1: Share the Project Files**
   - Zip the entire project folder.
   - Send the zip file to another device.
   - The other device will automatically get your database when they open the app.

2. **Method 2: Share Only the Database File**
   - Use "Export Database" from the menu.
   - Send the generated database file (autoparts_db.db) from your Downloads folder to the other device.
   - On the other device, place the file in the Downloads folder.
   - Use "Import Database" from the menu to load your database.

### Troubleshooting

- If the other device doesn't see your edits, make sure they've restarted the app after importing.
- Check that the exported database file was successfully sent to the other device.
- Ensure the database file is properly placed in the Downloads folder.

# Auto Parts Shop - SQLite Database Sync

This Android application allows sharing SQLite database between multiple devices without using Firebase or any cloud service. The sync mechanism works over local WiFi networks.

## Features

- Export/import full database files
- Automatic device discovery on the same network
- Real-time synchronization of database changes
- Support for selective table synchronization
- Background sync service

## How to Use Database Sync

### Setup

1. Make sure all devices are connected to the same WiFi network
2. Grant the necessary permissions when prompted:
   - Storage permissions (for database export/import)
   - Network permissions (for device discovery and sync)

### Accessing the Sync Feature

1. Open the app and log in
2. Tap the menu icon (three dots) in the top-right corner
3. Select "Database Sync" from the menu

### Syncing Databases

#### Method 1: Full Database Export/Import

1. On the source device:
   - Tap "Export" to create a database backup file
   - The file will be saved to your Downloads folder
   - Share this file with the target device via email, messaging, etc.

2. On the target device:
   - Tap "Import"
   - Select the database file you received
   - Confirm the import when prompted

#### Method 2: Automatic Network Sync

1. Make sure both devices are on the same WiFi network
2. On both devices, open the Database Sync screen
3. Pull down to refresh the device list
4. On one device, select the other device from the list
5. Tap "Sync" to start the synchronization process
6. Changes will be automatically transferred between devices

### Troubleshooting

- If devices don't appear in the list, make sure:
  - Both devices are on the same WiFi network
  - Network permissions are granted
  - Try refreshing the list by pulling down
- If sync fails:
  - Check your network connection
  - Try exporting/importing the database manually

## Technical Details

The sync mechanism works by:
1. Discovering devices on the same network using UDP multicast
2. Establishing a direct HTTP connection between devices
3. Tracking changes to the database using timestamps
4. Sending only the changed records to other devices
5. Applying changes to the target database with conflict resolution

## Permissions Required

- `INTERNET`: For network communication
- `ACCESS_NETWORK_STATE`: To check network connectivity
- `ACCESS_WIFI_STATE`: To get WiFi information
- `CHANGE_WIFI_MULTICAST_STATE`: For device discovery
- `READ_EXTERNAL_STORAGE`: For importing database files
- `WRITE_EXTERNAL_STORAGE`: For exporting database files
- `MANAGE_EXTERNAL_STORAGE`: For Android 11+ to access files #   s t u d e n t 
 
 
