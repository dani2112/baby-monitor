package de.dk_s.babymonitor.monitoring;


import android.media.AudioManager;
import android.media.ToneGenerator;
import android.provider.MediaStore;

import java.util.Observable;
import java.util.Observer;

public class AlarmController implements Observer {

    private BabyVoiceMonitor babyVoiceMonitor;

    private DatabaseEventLogger databaseEventLogger;

    private boolean isEnabled = false;

    public AlarmController(BabyVoiceMonitor babyVoiceMonitor) {
        this.babyVoiceMonitor = babyVoiceMonitor;
        this.databaseEventLogger = null;
    }

    public AlarmController(BabyVoiceMonitor babyVoiceMonitor, DatabaseEventLogger databaseEventLogger) {
        this.babyVoiceMonitor = babyVoiceMonitor;
        this.databaseEventLogger = databaseEventLogger;
    }


    public void enableAlarmController() {
        if(isEnabled) {
            return;
        }
        babyVoiceMonitor.addObserver(this);
    }


    public void disableAlarmController() {
        if(!isEnabled) {
            return;
        }
        babyVoiceMonitor.deleteObserver(this);
    }

    @Override
    public void update(Observable observable, Object data) {
        BabyVoiceMonitor.AudioEvent audioEvent = (BabyVoiceMonitor.AudioEvent)data;
        if (audioEvent.getEventType() == 1) {
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        }
    }
}
