package de.dk_s.babymonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.MediaStore;

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

    public MonitoringService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
