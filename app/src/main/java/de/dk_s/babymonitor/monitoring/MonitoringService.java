package de.dk_s.babymonitor.monitoring;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

public class MonitoringService extends Service {

    public static class AudioEvent {

        private int eventType;

        private long timeStamp;

        private int audioLevel;

        public AudioEvent(int eventType, long timeStamp) {
            this.eventType = eventType;
            this.timeStamp = timeStamp;
        }

        public AudioEvent(int eventType, long timeStamp, int audioLevel) {
            this.eventType = eventType;
            this.timeStamp = timeStamp;
            this.audioLevel = audioLevel;
        }

        public int getEventType() {
            return eventType;
        }


        public long getTimeStamp() {
            return timeStamp;
        }

        public int getAudioLevel() {
            return audioLevel;
        }
    }

    public class MonitoringServiceBinder extends Binder {
        public MonitoringService getService() {
            return MonitoringService.this;
        }
    }

    private static final String TAG = "MonitoringService";

    private final IBinder binder = new MonitoringServiceBinder();


    private boolean isStarted = false;

    public MonitoringService() {
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void onCreate () {
        Toast.makeText(this, "service created", Toast.LENGTH_SHORT).show();
        isStarted = false;
    }

    public void onDestroy() {
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();
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
        return START_STICKY;    // restart service if it is killed by system and resources become available
    }

}
