package de.dk_s.babymonitor.monitoring;


import android.util.Log;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class BabyVoiceMonitor implements Observer {

    /* Load native library */
    static {
        System.loadLibrary("babymonitor");
    }

    private static final String TAG = "BabyVoiceMonitor";

    private boolean isStarted = false;

    private MicRecorder micRecorder;

    private BlockingQueue<MicRecorder.AudioChunk> audioChunkBlockingQueue;

    private ExecutorService audioChunkExecutorService;

    public BabyVoiceMonitor(MicRecorder micRecorder) {
        this.micRecorder = micRecorder;
    }

    public void startMonitoring() {
        if(isStarted) {
            return;
        }
        isStarted = true;
        if(audioChunkBlockingQueue == null) {
            audioChunkBlockingQueue = new LinkedBlockingQueue<>();
        }
        micRecorder.addObserver(this);
        audioChunkExecutorService = Executors.newSingleThreadExecutor();
        audioChunkExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                processAudioElements();
            }
        });
    }

    public void stopMonitoring() {
        if(!isStarted) {
            return;
        }
        isStarted = false;
        micRecorder.deleteObservers();
        audioChunkBlockingQueue.clear();
        audioChunkExecutorService.shutdownNow();
    }

    private void processAudioElements() {
        while(isStarted) {
            MicRecorder.AudioChunk audioChunk = null;
            try {
                audioChunk = audioChunkBlockingQueue.take();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error: Exception while processing audio elements. Maybe thread was interrupted.");
                continue;
            }
            float noiseLevel = computeNoiseLevel(audioChunk.getChunkData16Bit(), audioChunk.getChunkData16Bit().length);
            Log.e(TAG, String.valueOf(noiseLevel));
        }
    }

    private native float computeNoiseLevel(short[] audioData, int elementCount);


    @Override
    public void update(Observable observable, Object data) {
        MicRecorder.AudioChunk audioChunk = (MicRecorder.AudioChunk)data;
        audioChunkBlockingQueue.add(audioChunk);
    }
}
