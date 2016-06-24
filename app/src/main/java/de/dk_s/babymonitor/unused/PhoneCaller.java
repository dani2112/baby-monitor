package de.dk_s.babymonitor.unused;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;
import de.dk_s.babymonitor.monitoring.MicRecorder;

public class PhoneCaller implements Observer {

    private class EndCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (TelephonyManager.CALL_STATE_RINGING == state) {
                Log.i(TAG, "RINGING, number: " + incomingNumber);
            }
            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                //wait for phone to go offhook (probably set a boolean flag) so you know your app initiated the call.
                Log.i(TAG, "OFFHOOK");
            }
            if (TelephonyManager.CALL_STATE_IDLE == state) {
                //when this state occurs, and your flag is set, restart your app
                Log.e(TAG, "IDLE");
                if (isCalling) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isCalling = false;
                    micRecorder.startRecording();
                }
            }
        }
    }

    private static final String TAG = "PhoneCaller";

    private BabyVoiceMonitor babyVoiceMonitor = null;

    private MicRecorder micRecorder = null;

    private Context context = null;

    private boolean isRunning = false;

    ExecutorService executorService = null;

    private boolean isCalling = false;

    private EndCallListener callListener = null;


    public PhoneCaller(BabyVoiceMonitor babyVoiceMonitor, MicRecorder micRecorder, Context context) {
        this.babyVoiceMonitor = babyVoiceMonitor;
        this.micRecorder = micRecorder;
        this.context = context;
        this.callListener = new EndCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void startListening() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        babyVoiceMonitor.addObserver(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void stopListening() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        babyVoiceMonitor.deleteObserver(this);
        executorService.shutdownNow();
    }


    @Override
    public void update(Observable observable, Object data) {
        BabyVoiceMonitor.AudioEvent audioEvent = (BabyVoiceMonitor.AudioEvent) data;
        if (audioEvent.getEventType() == 1 && isCalling == false) {
            micRecorder.stopRecording();
            isCalling = true;
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "07305179214"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        }
    }
}
