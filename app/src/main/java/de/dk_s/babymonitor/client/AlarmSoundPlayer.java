package de.dk_s.babymonitor.client;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmSoundPlayer {

    private static ExecutorService executorService = null;

    private static boolean isAlarmPlaying = false;

    public static void playAlarm(final Context context) {
        if(isAlarmPlaying) {
            return;
        }
        isAlarmPlaying = true;
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while(isAlarmPlaying) {
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(context, notification);
                        r.play();
                        while(r.isPlaying()) {
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void stopAlarm() {
        if(!isAlarmPlaying) {
            return;
        }
        isAlarmPlaying = false;
        executorService.shutdownNow();
    }


}
