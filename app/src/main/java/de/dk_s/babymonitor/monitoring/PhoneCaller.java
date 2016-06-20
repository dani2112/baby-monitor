package de.dk_s.babymonitor.monitoring;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhoneCaller implements Observer {

    private BabyVoiceMonitor babyVoiceMonitor = null;

    private Context context = null;

    private boolean isRunning = false;

    ExecutorService executorService = null;

    private boolean isCalling = false;


    public PhoneCaller(BabyVoiceMonitor babyVoiceMonitor, Context context) {
        this.babyVoiceMonitor = babyVoiceMonitor;
        this.context = context;
    }

    public void startListening() {
        if(isRunning) {
            return;
        }
        isRunning = true;
        babyVoiceMonitor.addObserver(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void stopListening() {
        if(!isRunning) {
            return;
        }
        isRunning = false;
        babyVoiceMonitor.deleteObserver(this);
        executorService.shutdownNow();
    }


    @Override
    public void update(Observable observable, Object data) {
        BabyVoiceMonitor.AudioEvent audioEvent = (BabyVoiceMonitor.AudioEvent)data;
        System.out.println(audioEvent.getEventType());
        if(audioEvent.getEventType() == 1 && isCalling == false) {
            isCalling = true;
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "07305179214"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        }
    }
}
