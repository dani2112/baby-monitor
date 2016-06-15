package de.dk_s.babymonitor.monitoring;

import android.app.Service;
import android.content.Intent;
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

    private static final String TAG = "MonitoringService";

    public MonitoringService() {
    }

    @Override
    public void onCreate () {
        Toast.makeText(this, "service created", Toast.LENGTH_SHORT).show();
    }

    public void onDestroy() {
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return START_STICKY;    // restart service if it is killed by system and resources become available
    }
}
