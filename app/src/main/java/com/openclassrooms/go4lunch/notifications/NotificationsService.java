package com.openclassrooms.go4lunch.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.EmployeeHelper;
import com.openclassrooms.go4lunch.controller.activity.PrincipalActivity;
import com.openclassrooms.go4lunch.controller.activity.RestaurantActivity;
import com.openclassrooms.go4lunch.model.Employee;

public class NotificationsService extends FirebaseMessagingService {
    private final int NOTIFICATION_ID = 001;
    private final String NOTIFICATION_TAG = "FIREBASE_NOTIF";
    final String PLACE_ID = "placeId";

    String mEmployeeId = PrincipalActivity.mCurrentUser.getUid();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
          if (mEmployeeId != null) prepareNotification(mEmployeeId);
        }
    }

    private void prepareNotification(String id) {
        if (id != null){
            EmployeeHelper.getEmployee(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Employee employee = documentSnapshot.toObject(Employee.class);
                    String message = (employee.getLunchPlace() == null) ? getApplicationContext().getResources().getString(R.string.you_did_not_decide_yet): getApplicationContext().getResources().getString(R.string.you_eat_at) + " " + employee.getLunchPlace();
                    String placeId = employee.getLunchPlaceId();
                    boolean notif = employee.getNotif();

                    if (notif) sendVisualNotification(message, placeId);
                }
            });
        }
    }

    private void sendVisualNotification(String messageBody, String placeId) {
        PendingIntent pendingIntent = createIntent(placeId);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.addLine(messageBody);

        String channelId = getString(R.string.default_notification_channel_id);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_title))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Firebase message";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

    private PendingIntent createIntent(String placeId) {
        PendingIntent pendingIntent;
        if (placeId == null) {
            Intent intent = new Intent(this, PrincipalActivity.class);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        } else {
            Intent intent = new Intent(this, RestaurantActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(PLACE_ID,placeId);
            intent.putExtras(bundle);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }
        return pendingIntent;
    }
}
