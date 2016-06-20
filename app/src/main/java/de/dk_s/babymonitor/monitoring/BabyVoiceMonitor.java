package de.dk_s.babymonitor.monitoring;


import android.provider.MediaStore;
import android.util.Log;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class BabyVoiceMonitor extends Observable implements Observer {

    /* Load native library */
    static {
        System.loadLibrary("babymonitor");
    }

    public static class AudioEvent {

        private int eventType;

        private long timeStamp;

        private float audioLevel;

        public AudioEvent(int eventType, long timeStamp) {
            this.eventType = eventType;
            this.timeStamp = timeStamp;
        }

        public AudioEvent(int eventType, long timeStamp, float audioLevel) {
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

        public float getAudioLevel() {
            return audioLevel;
        }
    }

    private static final String TAG = "BabyVoiceMonitor";

    private boolean isStarted = false;

    private MicRecorder micRecorder;

    private BlockingQueue<MicRecorder.AudioChunk> audioChunkBlockingQueue;

    private ExecutorService audioChunkExecutorService;

    private Deque<AudioEvent> recentAudioEventList = null;

    private Semaphore recentAudioEventListSemaphore = new Semaphore(1);

    private int recentAudioEventListLimit = 480;

    private float audioLevelMax = 10000;

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
            /* Start list editing and therefore lock list */
            AudioEvent audioEvent = null;
            try {
                recentAudioEventListSemaphore.acquire();
                /* Remove old elements from the queue if necessary */
                if (recentAudioEventList.size() >= recentAudioEventListLimit) {
                    recentAudioEventList.remove();
                }
                /* Add new audio elements to queue */
                if (noiseLevel > 3000) {
                    audioEvent = new AudioEvent(1, audioChunk.getTimeStamp(), noiseLevel / audioLevelMax);
                    recentAudioEventList.add(audioEvent);
                } else {
                    audioEvent = new AudioEvent(0, audioChunk.getTimeStamp(), noiseLevel / audioLevelMax);
                    recentAudioEventList.add(audioEvent);
                }
                recentAudioEventListSemaphore.release();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error: Exception while updating audio event list.");
            }
            setChanged();
            notifyObservers(audioEvent);
        }
    }

    private native float computeNoiseLevel(short[] audioData, int elementCount);


    @Override
    public void update(Observable observable, Object data) {
        MicRecorder.AudioChunk audioChunk = (MicRecorder.AudioChunk) data;
        audioChunkBlockingQueue.add(audioChunk);
    }

    public Deque<AudioEvent> getRecentAudioEventList() {
        Deque<AudioEvent> recentAudioEventListShallowCopy = null;
        try {
            recentAudioEventListSemaphore.acquire();
            recentAudioEventListShallowCopy = new LinkedList<>(recentAudioEventList);
            recentAudioEventListSemaphore.release();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error: Exception while returning recent audio event list shallow copy");
        }
        return recentAudioEventListShallowCopy;
    }

}
