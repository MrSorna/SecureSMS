package ap.com.securesms.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.provider.Settings;

public class NotificationHelper {


    public static void create(Context context, int id, String title, String message, PendingIntent resultPendingIntent, int icon) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String CHANNEL_ID = "" + id;
            CharSequence name = "NOTIFICATION_CHANNEL_NAME";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setShowBadge(true);
//            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(channel);
            notification = new Notification.Builder(context).setSmallIcon(icon)
                    .setLargeIcon(((BitmapDrawable) context.getDrawable(icon)).getBitmap())
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(false)
                    .setChannelId(CHANNEL_ID).build();
        } else {
            notification = new Notification.Builder(context).setSmallIcon(icon)
                    .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(icon)).getBitmap())
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(false)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI).build();
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(id, notification);

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(icon)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setContentIntent(resultPendingIntent)
//                .setAutoCancel(false)
//                .setShowWhen(true);
//        mNotificationManager.notify(id, mBuilder.build());
    }
}