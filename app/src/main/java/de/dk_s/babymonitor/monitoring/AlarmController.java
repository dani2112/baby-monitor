package de.dk_s.babymonitor.monitoring;


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

    }


    public void disableAlarmController() {
        if(!isEnabled) {
            return;
        }

    }

    @Override
    public void update(Observable observable, Object data) {

    }
}
