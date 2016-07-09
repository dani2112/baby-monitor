package de.dk_s.babymonitor.client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import de.dk_s.babymonitor.communication.information.InformationServer;
import de.dk_s.babymonitor.monitoring.AlarmController;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;
import de.dk_s.babymonitor.monitoring.MicRecorder;
import de.dk_s.babymonitor.monitoring.MonitoringService;
import de.dk_s.babymonitor.monitoring.db.DatabaseEventLogger;

public class ConnectionService extends Service {

    public class ConnectionServiceBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    private static final String TAG = "ConnectionService";

    private final IBinder binder = new ConnectionServiceBinder();

    private boolean isStarted = false;

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
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "connection service starting", Toast.LENGTH_SHORT).show();
        isStarted = true;

        return START_STICKY;    // restart service if it is killed by system and resources become available
    }
}
