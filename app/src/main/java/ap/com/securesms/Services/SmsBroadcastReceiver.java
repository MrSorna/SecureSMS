package ap.com.securesms.Services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import java.util.ArrayList;
import ap.com.securesms.Activity.ConversationActivity;
import ap.com.securesms.Activity.HomeActivity;
import ap.com.securesms.Activity.LoginActivity;
import ap.com.securesms.Util.Constants;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.Model.MessageItem;
import ap.com.securesms.R;
import ap.com.securesms.Util.NotificationHelper;
import ap.com.securesms.Util.Settings;
import ap.com.securesms.Util.Utils;
import ap.com.securesms.Database.DatabaseHandler;


public class SmsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Utils.OnReceiveSMS(context, intent, new Utils.OnReceiveSMS() {
            @Override
            public void OnReceive(String address, String body) {
                DatabaseHandler db = new DatabaseHandler(context);
                ArrayList<Contact> contacts = db.getContacts();
                for (Contact contact : contacts) {
                    if (address.contains(contact.getPhone())) {
                        if (Utils.isEnc(body)) {
                            final Uri uri = new Settings(context).getSoundUri();
                            if (uri != null)
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            MediaPlayer.create(context, uri).start();
                                        } catch (Exception e) {
                                        }
                                    }
                                }, 2000);
                            if (HomeActivity.resume | ConversationActivity.resume) {
                                HomeActivity.refresh(new MessagesParser.OnFinished() {
                                    @Override
                                    public void finished(ArrayList<MessageItem> items) {
                                    }
                                }, 3000);
                            } else {
                                createNotification(context, contact);
                            }
                            db.close();
                            abortBroadcast();
                            return;
                        }
                    }
                }
                db.close();

            }
        });
    }


    private void createNotification(Context context, Contact contact) {
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int id = 1;
        try {
            id = Integer.parseInt(contact.getPhone());
        } catch (Exception e) {
        }
        Intent resultIntent = new Intent(context, LoginActivity.class);
        resultIntent.putExtra(Constants.KEY_NUMBER, contact.getPhone());
        resultIntent.putExtra(Constants.FROM_SMS_RECEIVER, true);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationHelper.create(context, id, contact.getName(), contact.getPhone(), resultPendingIntent, R.drawable.logo);
//
//        NotificationChannel channel = notificationManager.getNotificationChannel(mNotificationId);
//        if (channel != null) {
//            channel.
//        } else {
//
//        }
//        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
//                R.drawable.logo);
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(context)
//                        .setLargeIcon(icon)
//                        .setSmallIcon(R.drawable.logo)
//                        .setContentTitle(senderNo)
//                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
//                        .setAutoCancel(true)
//                        .setContentText(message);
//        mBuilder.setContentIntent(resultPendingIntent);
//
//        notificationManager.notify(id, mBuilder.build());
//        try {
//            Badges.setBadge(context, notificationManager.getActiveNotifications().length);
//        } catch (BadgesNotSupportedException e) {
//            e.printStackTrace();
//        }
    }
}

