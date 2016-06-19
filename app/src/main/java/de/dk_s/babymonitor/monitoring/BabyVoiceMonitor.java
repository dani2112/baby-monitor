package de.dk_s.babymonitor.monitoring;


import android.util.Log;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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

    private Deque<AudioEvent> recentAudioEventList;

    private int recentAudioEventListLimit = 480;

    public BabyVoiceMonitor(MicRecorder micRecorder) {
        this.micRecorder = micRecorder;
    }

    public void startMonitoring() {
        if (isStarted) {
            return;
        }
        isStarted = true;
        if (audioChunkBlockingQueue == null) {
            audioChunkBlockingQueue = new LinkedBlockingQueue<>();
        }
        if (recentAudioEventList == null) {
            recentAudioEventList = new LinkedList<>();
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
        if (!isStarted) {
            return;
        }
        isStarted = false;
        micRecorder.deleteObservers();
        audioChunkBlockingQueue.clear();
        audioChunkExecutorService.shutdownNow();
    }

    private void processAudioElements() {
        while (isStarted) {
            /* Calculate noise level of the audio chunk */
            MicRecorder.AudioChunk audioChunk = null;
            try {
                audioChunk = audioChunkBlockingQueue.take();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error: Exception while processing audio elements. Maybe thread was interrupted.");
                continue;
            }
            float noiseLevel = computeNoiseLevel(audioChunk.getChunkData16Bit(), audioChunk.getChunkData16Bit().length);
            /* Remove old elements from the queue if necessary */
            if(recentAudioEventList.size() >= recentAudioEventListLimit) {
                recentAudioEventList.remove();
            }
            /* Add new audio elements to queue */
            if (noiseLevel > 200) {
                recentAudioEventList.add(new AudioEvent(1, audioChunk.getTimeStamp(), (int) noiseLevel));
            } else {
                recentAudioEventList.add(new AudioEvent(0, audioChunk.getTimeStamp(), (int) noiseLevel));
            }
            setChanged();
            notifyObservers(recentAudioEventList);
        }
    }

    private native float computeNoiseLevel(short[] audioData, int elementCount);


    @Override
    public void update(Observable observable, Object data) {
        MicRecorder.AudioChunk audioChunk = (MicRecorder.AudioChunk) data;
        audioChunkBlockingQueue.add(audioChunk);
    }
}
