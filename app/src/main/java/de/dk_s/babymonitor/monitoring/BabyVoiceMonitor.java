package de.dk_s.babymonitor.monitoring;


import android.util.Log;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class BabyVoiceMonitor extends Observable implements Observer {

    /* Load native library */
    static {
        System.loadLibrary("babymonitor");
    }

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
            if(noiseLevel > 200) {
                setChanged();
                notifyObservers(new AudioEvent(1, audioChunk.getTimeStamp(), (int)noiseLevel));
            } else {
                setChanged();
                notifyObservers(new AudioEvent(0, audioChunk.getTimeStamp(), (int)noiseLevel));
            }
            Log.d(TAG, String.valueOf(noiseLevel));
        }
    }

    private native float computeNoiseLevel(short[] audioData, int elementCount);


    @Override
    public void update(Observable observable, Object data) {
        MicRecorder.AudioChunk audioChunk = (MicRecorder.AudioChunk)data;
        audioChunkBlockingQueue.add(audioChunk);
    }
}
