package de.dk_s.babymonitor.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.util.Deque;
import java.util.List;

import de.dk_s.babymonitor.ParentActivity;
import de.dk_s.babymonitor.R;
import de.dk_s.babymonitor.gui.eventlist.EventHistoryDataProvider;
import de.dk_s.babymonitor.monitoring.AudioEventHistoryDataProvider;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public class ConnectionService extends Service implements AudioEventHistoryDataProvider, EventHistoryDataProvider {

    public class ConnectionServiceBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    private static final String TAG = "ConnectionService";

    private final IBinder binder = new ConnectionServiceBinder();

    private boolean isStarted = false;

    private InformationClient informationClient = null;

    private int notificationID = 1;

    NotificationCompat.Builder notificationBuilder = null;

    public ConnectionService() {
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "connection service created", Toast.LENGTH_SHORT).show();
        isStarted = false;
    }

    public void onDestroy() {
        Toast.makeText(this, "connection service destroyed", Toast.LENGTH_SHORT).show();
        isStarted = false;
        disableServiceNotification();
        informationClient.stopClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "connection service starting", Toast.LENGTH_SHORT).show();
        isStarted = true;
        enableServiceNotification();
        informationClient = new InformationClient("127.0.0.1", this);
        informationClient.startClient();
        return START_STICKY;    // restart service if it is killed by system and resources become available
    }

    @Override
    public Deque<BabyVoiceMonitor.AudioEvent> getRecentAudioEvents() {
        return informationClient != null ? informationClient.getRecentAudioEvents() : null;
    }

    @Override
    public List<BabyVoiceMonitor.AudioEvent> get24HoursAudioEvents() {
        return informationClient != null ? informationClient.getEventHistory() : null;
    }

    @Override
    public BabyVoiceMonitor.AudioEvent getLastAudioEvent() {
        if(informationClient != null) {
            List<BabyVoiceMonitor.AudioEvent> eventHistoryList = informationClient.getEventHistory();
            if(eventHistoryList != null && eventHistoryList.size() > 0) {
                return eventHistoryList.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    private void enableServiceNotification() {
        notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder.setSmallIcon(R.drawable.round_button);
        notificationBuilder.setContentTitle("Babymonitor Status");
        notificationBuilder.setContentText("Babymonitor Status Details");
        notificationBuilder.setOngoing(true); // Make it uncancellable for the user
        Intent notificationIntent = new Intent(this, ParentActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent,
                0);
        notificationBuilder.setContentIntent(contentIntent);
        notificationBuilder.setPriority(Notification.PRIORITY_MAX);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    private void  disableServiceNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
    }

    public void updateNotification(String statusTitle, String statusMessage) {
        if(notificationBuilder != null) {
            notificationBuilder.setContentTitle(statusTitle);
            notificationBuilder.setContentText(statusMessage);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationID, notificationBuilder.build());
        }
    }
}
