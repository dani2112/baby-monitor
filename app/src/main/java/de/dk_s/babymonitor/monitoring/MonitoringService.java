package de.dk_s.babymonitor.monitoring;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

public class MonitoringService {

    private static final String TAG = "MonitoringService";

    private AudioRecord audioRecord;

    private short[] recordingBuffer;

    private short processingBuffers[][];


    public void startMonitoring() {
        int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int usedBufferSize = minBufferSize * 100;
        recordingBuffer = new short[usedBufferSize];
        processingBuffers = new short[20][20480];
        Log.e(TAG, String.valueOf(minBufferSize));
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, usedBufferSize);
        audioRecord.startRecording();

        int i = 0;
        while(i<20) {
            audioRecord.read(processingBuffers[i], 0, processingBuffers[i].length);
        i++;
        }

        audioRecord.stop();


        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 20480, AudioTrack.MODE_STREAM);
        audioTrack.play();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int i2 = 0;
                Log.e(TAG, "PLAY");
                while(i2 < 20) {
                    Log.e(TAG, String.valueOf(processingBuffers[i2][0]));
                    audioTrack.write(processingBuffers[i2], 0, processingBuffers[i2].length);
                    i2++;
                }
            }
        });
        t.start();

    }

    public void stopMonitoring() {

    }





}
