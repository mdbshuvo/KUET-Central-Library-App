package com.example.kuetcentrallibrary.Receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.example.kuetcentrallibrary.R;

import java.util.Calendar;

import androidx.core.app.NotificationCompat;

import static com.example.kuetcentrallibrary.Activities.SummaryActivity.NOTIFICATION_CHANNEL_ID;
import static com.example.kuetcentrallibrary.Activities.SummaryActivity.default_notification_channel_id;

public class NotifyReciever extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id" ;
    public static String NOTIFICATION = "notification" ;

    @Override
    public void onReceive (Context context , Intent intent) {
        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                // Set the alarm here.
                SharedPreferences sharedPreferences = context.getSharedPreferences("not",Context.MODE_PRIVATE);
//                String[] strings = sharedPreferences.getString("time",null).split("/");

                String time = sharedPreferences.getString("time","0");

                long now = Long.parseLong(time);
                long current = Calendar.getInstance().getTimeInMillis();

                long delay = now - current;

                Intent notificationIntent = new Intent( context, NotifyReciever.class ) ;
                notificationIntent.putExtra(NotifyReciever.NOTIFICATION_ID , 1 ) ;

                NotificationCompat.Builder builder = new NotificationCompat.Builder( context,
                        default_notification_channel_id ) ;
                builder.setContentTitle( "Scheduled Notification" ) ;
                builder.setContentText("Library Book Renewal Required") ;
                builder.setSmallIcon(R.drawable.kuet_logo_ultra_small ) ;
                builder.setAutoCancel( true ) ;
                builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;

                Notification notification = builder.build() ;
                notificationIntent.putExtra(NotifyReciever.NOTIFICATION , notification) ;
                PendingIntent pendingIntent = PendingIntent. getBroadcast ( context, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
                long futureInMillis = SystemClock.elapsedRealtime () + delay ;
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context. ALARM_SERVICE ) ;
                assert alarmManager != null;
                alarmManager.set(AlarmManager. ELAPSED_REALTIME_WAKEUP , futureInMillis , pendingIntent) ;

                return;
            }
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE ) ;
        Notification notification = intent.getParcelableExtra( NOTIFICATION ) ;
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager.IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel) ;
        }
        int id = intent.getIntExtra( NOTIFICATION_ID , 0 ) ;
        assert notificationManager != null;
        notificationManager.notify(id , notification) ;
    }
}
