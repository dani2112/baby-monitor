package de.dk_s.babymonitor.monitoring;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Deque;

import de.dk_s.babymonitor.monitoring.db.DatabaseEventLogger;

public class MonitoringService extends Service {

    public class MonitoringServiceBinder extends Binder {
        public MonitoringService getService() {
            return MonitoringService.this;
        }
    }

    private static final String TAG = "MonitoringService";

    private final IBinder binder = new MonitoringServiceBinder();

    private boolean isStarted = false;

    private MicRecorder micRecorder = null;

    private BabyVoiceMonitor babyVoiceMonitor = null;

    private AlarmController alarmController = null;

    public MonitoringService() {
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "service created", Toast.LENGTH_SHORT).show();
        isStarted = false;
    }

    public void onDestroy() {
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();
        babyVoiceMonitor.stopMonitoring();
        micRecorder.stopRecording();
        alarmController.disableAlarmController();
        isStarted = false;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        isStarted = true;
        if (micRecorder == null) {
            micRecorder = new MicRecorder();
        }
        if (babyVoiceMonitor == null) {
            babyVoiceMonitor = new BabyVoiceMonitor(micRecorder);
        }
        if(alarmController == null) {
            alarmController = new AlarmController(babyVoiceMonitor, new DatabaseEventLogger(getApplicationContext()), getApplicationContext());
        }
        micRecorder.startRecording();
        babyVoiceMonitor.startMonitoring();
        alarmController.enableAlarmController();
        return START_STICKY;    // restart service if it is killed by system and resources become available
    }

    public Deque<BabyVoiceMonitor.AudioEvent> getRecentAudioEventList() {
        return babyVoiceMonitor == null ? null : babyVoiceMonitor.getRecentAudioEventList();
    }

}
