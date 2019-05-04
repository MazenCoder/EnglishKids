package com.mobidroid.englishkids.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mobidroid.englishkids.MainActivity;
import com.mobidroid.englishkids.R;

public class FCMMsgService extends FirebaseMessagingService {

    private static final String TAG = "FCMMsgService";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d(TAG, "Message received from: " + message.getFrom());

        if (message.getData().size() > 0) {
            Log.d(TAG, "Message size: " + message.getData().size());
            for (String key : message.getData().keySet()) {
                Log.d(TAG, "key: " + key + "; data: " + message.getData().get(key));
//                sendNotification(message.getData().get(key));
            }
        }

        try {
            String title = message.getData().get(getString(R.string.data_title));
            String msg = message.getData().get(getString(R.string.data_message));
            sendChatmessageNotification(title, msg);
        }catch (NullPointerException e) {
            Log.e(TAG, "onMessageReceived: "+e.getMessage());
        }
    }

    @Override
    public void onDeletedMessages() {
        // if this method is called it's because you haven't connected to FCM
        // for quite some time (more than a month) or there are too many
        // pending messages to send. Your app should perform a full
        // sync with your server to get up to date
    }

    private void sendChatmessageNotification(String title, String message){
        Log.d(TAG, "sendChatmessageNotification: building a chatmessage notification");

        //get the notification id

        try {
            // Instantiate a Builder object.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                    getString(R.string.default_notification_channel_id));

            // Creates an Intent for the Activity
            Intent pendingIntent = new Intent(this, MainActivity.class);

            // Sets the Activity to start in a new, empty task
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        pendingIntent.putExtra(getString(R.string.intent_chatroom), chatroom);
            // Creates the PendingIntent
            PendingIntent notifyPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            pendingIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            //add properties to the builder
            builder.setSmallIcon(R.drawable.ic_notification_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.ic_notification_logo))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentTitle(title)
                    .setContentText(message)
//                .setColor(getColor(R.color.blue4))
                    .setAutoCancel(true);
//                .setSubText(message)
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .bigText("New messages in " + chatroom.getChatroom_name()).setSummaryText(message))
//                .setNumber(mNumPendingMessages)
//                .setOnlyAlertOnce(true);

            builder.setContentIntent(notifyPendingIntent);
            /**
             NotificationManager mNotificationManager =
             (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

             mNotificationManager.notify(0, builder.build());
             */
//        mNotificationManager.notify(0 /* ID of notification */, mNotificationManager.build());



            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id),
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
                builder.setColor(getColor(R.color.blue4));
            }

            notificationManager.notify(0 /* ID of notification */, builder.build());
        }catch (Exception e) {
            Log.e(TAG, "sendChatmessageNotification: "+e.getMessage());
        }
    }
}
