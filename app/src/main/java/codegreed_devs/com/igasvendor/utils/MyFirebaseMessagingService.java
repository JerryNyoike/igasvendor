package codegreed_devs.com.igasvendor.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.ui.ViewOrder;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    public MyFirebaseMessagingService() {

    }

    //get new fcm token
    @Override
    public void onNewToken(String s) {

        saveToken(s);

        super.onNewToken(s);
    }

    //save token to shared preference
    //and to database if user is logged in
    private void saveToken(String token) {

        Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_FCM_TOKEN, token);

        if (!Utils.isFirstLogin(getApplicationContext()))
            FirebaseDatabase
                    .getInstance()
                    .getReference("vendors")
                    .child(Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_ID))
                    .child("fcm_token")
                    .setValue(token)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful() && task.getException() != null)
                                Log.e("DATABASE ERROR", task.getException().getMessage());
                        }
                    });

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //check if message notification has body
        if (remoteMessage.getNotification() != null && remoteMessage.getData() != null)
            showNotification(remoteMessage.getNotification(), remoteMessage.getData());

        super.onMessageReceived(remoteMessage);


    }

    //show pop up notification with passed body
    private void showNotification(RemoteMessage.Notification notification, @NonNull Map<String, String> messageData) {

        String clientId = messageData.get("client_id");
        String orderId = messageData.get("order_id");

        Log.e("NOTIFICATION","Notification: " + notification.toString() + ", Data : " + messageData.toString());

        Intent intent = new Intent(this, ViewOrder.class);
        intent.putExtra("client_id", clientId);
        intent.putExtra("order_id", orderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        int id = (int) System.currentTimeMillis();

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(notification.getTitle() + "\n" + notification.getBody())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, notificationBuilder.build());

    }
}
