package com.example.autogarbagesortapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if the message contains a data payload.
        if (remoteMessage.getData().isEmpty()) {
            Log.w(TAG, "Received message with no data payload");
            return;
        }

        // Parse the bin data from the FCM data payload.
        String binType = remoteMessage.getData().get("binType");
        String binLevelStr = remoteMessage.getData().get("binLevel");

        if (binType == null || binLevelStr == null) {
            Log.w(TAG, "Missing binType or binLevel in FCM payload");
            return;
        }

        try {
            int binLevel = Integer.parseInt(binLevelStr);

            // Use NotificationHelper to display the notification
            // This shows the notification even when the app is closed or in the background
            NotificationHelper.showNotification(
                    getApplicationContext(),
                    "Bin Overflow Alert",
                    binType + " bin is " + binLevel + "% full.",
                    binType.hashCode()
            );

        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid binLevel: " + binLevelStr, e);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed: " + token);
        // Optionally send the token to your server here
    }
}
