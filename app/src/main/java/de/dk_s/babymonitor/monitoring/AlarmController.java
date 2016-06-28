package de.dk_s.babymonitor.monitoring;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Observable;
import java.util.Observer;

import de.dk_s.babymonitor.monitoring.db.DatabaseEventLogger;

public class AlarmController implements Observer {

    private static final String TAG = "AlarmController";

    static final public String EVENT_DB_UPDATED = "de.dk_s.babymonitor.monitoring.AlarmController.EventDbUpdated";

    private BabyVoiceMonitor babyVoiceMonitor;

    private DatabaseEventLogger databaseEventLogger;

    private Context context;

    private LocalBroadcastManager localBroadcastManager = null;

    private boolean isEnabled = false;

    private long lastAlarmEntry = -1;

    public AlarmController(BabyVoiceMonitor babyVoiceMonitor, Context context) {
        this.babyVoiceMonitor = babyVoiceMonitor;
        this.databaseEventLogger = null;
        this.context = context;
    }

    public AlarmController(BabyVoiceMonitor babyVoiceMonitor, DatabaseEventLogger databaseEventLogger, Context context) {
        this.babyVoiceMonitor = babyVoiceMonitor;
        this.databaseEventLogger = databaseEventLogger;
        this.context = context;
    }


    public void enableAlarmController() {
        if(isEnabled) {
            return;
        }
        babyVoiceMonitor.addObserver(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }


    public void disableAlarmController() {
        if(!isEnabled) {
            return;
        }
        babyVoiceMonitor.deleteObserver(this);
        localBroadcastManager = null;
    }

    @Override
    public void update(Observable observable, Object data) {
        BabyVoiceMonitor.AudioEvent audioEvent = (BabyVoiceMonitor.AudioEvent)data;
        if (audioEvent.getEventType() == 1) {
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            lastAlarmEntry = databaseEventLogger.logAlarmEnabled(audioEvent.getTimeStamp());
            broadcastEventHistoryChanged();
        } else if (audioEvent.getEventType() == 3) {
            if(lastAlarmEntry > 0) {
                databaseEventLogger.logAlarmDisabled(audioEvent.getTimeStamp(), lastAlarmEntry);
                broadcastEventHistoryChanged();
            }
        }
    }

    private void broadcastEventHistoryChanged() {
        if(localBroadcastManager != null) {
            Intent intent = new Intent(EVENT_DB_UPDATED);
            localBroadcastManager.sendBroadcast(intent);
        }
    }
}
