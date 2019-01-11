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

        //save token to shared preference
        Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_FCM_TOKEN, s);

        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //check if message notification has body
        if (remoteMessage.getNotification() != null && remoteMessage.getData() != null)
        {

            //get message data
            Map<String, String> messageData = remoteMessage.getData();

            //get notification body
            String notificationBody = remoteMessage.getNotification().getBody();

            showNotification(notificationBody, messageData);

        }

        super.onMessageReceived(remoteMessage);


    }

    //show pop up notification with passed body
    private void showNotification(String notificationBody, @NonNull Map<String, String> messageData) {

        String clientId = messageData.get("client_id");
        String orderId = messageData.get("order_id");

        Log.e("NOTIFICATION","Message : " + notificationBody + ", Body : {" + clientId + "," + orderId + "}");

        Intent intent = new Intent(this, ViewOrder.class);
        intent.putExtra("client_id", clientId);
        intent.putExtra("order_id", orderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        int id = (int) System.currentTimeMillis();

        Notification.Builder notification = new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(notificationBody)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, notification.build());

    }
}
