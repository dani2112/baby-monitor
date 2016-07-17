package de.dk_s.babymonitor.client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Deque;
import java.util.List;

import de.dk_s.babymonitor.communication.information.InformationServer;
import de.dk_s.babymonitor.gui.eventlist.EventHistoryDataProvider;
import de.dk_s.babymonitor.monitoring.AlarmController;
import de.dk_s.babymonitor.monitoring.AudioEventHistoryDataProvider;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;
import de.dk_s.babymonitor.monitoring.MicRecorder;
import de.dk_s.babymonitor.monitoring.MonitoringService;
import de.dk_s.babymonitor.monitoring.db.DatabaseEventLogger;

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
        informationClient = new InformationClient("127.0.0.1");
        informationClient.startClient();
        return START_STICKY;    // restart service if it is killed by system and resources become available
    }

    @Override
    public Deque<BabyVoiceMonitor.AudioEvent> getRecentAudioEvents() {
        return informationClient != null ? informationClient.getRecentAudioEvents() : null;
    }

    @Override
    public List<BabyVoiceMonitor.AudioEvent> get24HoursAudioEvents() {
        return null;
    }

    @Override
    public BabyVoiceMonitor.AudioEvent getLastAudioEvent() {
        return null;
    }
}
