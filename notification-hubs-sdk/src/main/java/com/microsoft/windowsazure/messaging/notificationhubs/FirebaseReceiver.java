package com.microsoft.windowsazure.messaging.notificationhubs;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Facilitates processing messages that are delivered to the device via Firebase Cloud Messaging
 * (FCM).
 *
 * This class must be public to be invoked by Android. However, there should be no need for
 * consumers to interact directly with this class.
 */
public final class FirebaseReceiver extends FirebaseMessagingService {

    private final NotificationHub mHub;

    /**
     * Creates a new instance that will inform the static-global-instance of {@link NotificationHub}
     * when a new message is received.
     */
    public FirebaseReceiver() {
        this(NotificationHub.getInstance());
    }

    /**
     * Creates a new instance that will inform the given {@link NotificationHub} instance when a
     * message is received.
     * @param hub The hub that should be informed when a new notification arrives.
     */
    public FirebaseReceiver(NotificationHub hub) {
        mHub = hub;
    }

    /**
     * Loads all resources that the service will need to execute.
     */
    @Override
    public void onCreate() {
        super.onCreate();


        if (mHub.getPushChannel() == null) {
            FirebaseInstanceId.getInstance()
                    .getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()){
                            Log.e("ANH", "unable to fetch FirebaseInstanceId");
                            return;
                        }
                        mHub.setPushChannel(task.getResult().getToken());
                    });
        }
    }

    /**
     * Callback for the system when a new {@link RemoteMessage} is received.
     * @param remoteMessage the newly acquired message.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        mHub.relayMessage(getNotificationMessage(remoteMessage));
    }

    /**
     * Callback for when Firebase assigns a new unique identifier for pushing notifications to this
     * application on this device.
     * @param s The new unique identifier for this application.
     */
    @Override
    public void onNewToken(@NonNull String s) {
        mHub.setPushChannel(s);
    }

    /**
     * Converts from a {@link RemoteMessage} to a {@link BasicNotificationMessage}.
     * @param remoteMessage The message intended for this device, as delivered by Firebase.
     * @return A fully instantiated {@link BasicNotificationMessage}.
     */
    static BasicNotificationMessage getNotificationMessage(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String title = null;
        String body = null;
        if (notification != null) {
            title = notification.getTitle();
            body = notification.getBody();
        }
        return new BasicNotificationMessage(
                title,
                body,
                remoteMessage.getData());
    }
}
