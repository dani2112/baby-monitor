package de.dk_s.babymonitor.monitoring;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that provides microphone input data in PCM format...
 */
public class MicRecorder extends Observable {

    public static class AudioChunk {

        private long timeStamp;

        private byte[] chunkData8Bit;

        private short[] chunkData16Bit;

        public AudioChunk(long timeStamp, byte[] chunkData) {
            this.timeStamp = timeStamp;
            this.chunkData8Bit = chunkData;
        }

        public AudioChunk(long timeStamp, short[] chunkData) {
            this.timeStamp = timeStamp;
            this.chunkData16Bit = chunkData;
        }
    }

    private class RecordingRunnable implements Runnable {

        private int chunkSize;

        private int recordingBufferSize;

        public RecordingRunnable(int chunkSize, int recordingBufferSize) {
            this.chunkSize = chunkSize;
            this.recordingBufferSize = recordingBufferSize;
        }

        @Override
        public void run() {
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recordingBufferSize);
            audioRecord.startRecording();

            while(isRecordingRunning) {
                short[] audioData = new short[chunkSize];
                long timeStamp = System.currentTimeMillis();
                audioRecord.read(audioData, 0, audioData.length);
                setChanged();
                notifyObservers(new AudioChunk(timeStamp, audioData));
            }

            audioRecord.stop();
        }
    }

    private static final String TAG = "MicRecorder";

    /* Flag that indicates if recording is running */
    private boolean isRecordingRunning = false;

    /* ExecutorService used for executing the recording in own thread */
    ExecutorService recordingExecutorService;

    /* The duration of one audio chunk that is accessible through MicRecorder */
    private final float chunkSizeInSeconds = 0.5f;

    /* The time of audio that should be buffered by AudioRecord object (audio has to be polled at least every x seconds) */
    private final float recordingBufferSizeInSeconds = 2.0f;

    /* The sampling rate used for recording */
    private final int samplingRate = 44100; // Only sampling rate that is supported on all phones


    public void startRecording() {
        if(isRecordingRunning) {
            return;
        }
        int minBufferSize = AudioRecord.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        int chunkSize = (int)(samplingRate / (1 / chunkSizeInSeconds));

        int recordingBufferSize = (int)((samplingRate * recordingBufferSizeInSeconds) / minBufferSize) * minBufferSize;

        isRecordingRunning = true;

        recordingExecutorService = Executors.newSingleThreadExecutor();
        recordingExecutorService.submit(new RecordingRunnable(chunkSize, recordingBufferSize));
    }

    public void stopRecording() {
        if(!isRecordingRunning) {
            return;
        }
        isRecordingRunning = false;
        recordingExecutorService.shutdown();
    }

    public native void testNdk();


}
