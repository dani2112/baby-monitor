package de.dk_s.babymonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import de.dk_s.babymonitor.monitoring.MicRecorder;


public class SoundAnimationSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Observer {

    private static final String TAG = "SoundAnimationSurfaceView";

    private BlockingQueue<MonitoringService.AudioEvent> audioEventBlockingQueue;

    ExecutorService executorService;

    private SurfaceHolder holder;

    private Paint paint;

    private boolean isRunning = false;

    public SoundAnimationSurfaceView(Context context) {
        super(context);
        init();
    }

    public SoundAnimationSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);

        setWillNotDraw(false);
        isRunning = true;
        audioEventBlockingQueue = new LinkedBlockingQueue<>();
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while(isRunning) {
                        Thread.sleep(1000);
                        Canvas canvas = holder.lockCanvas();
                        drawAnimation(canvas);
                        holder.unlockCanvasAndPost(canvas);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void drawAnimation(Canvas canvas) {
        audioEventBlockingQueue.add(new MonitoringService.AudioEvent(0, System.currentTimeMillis()));
        int audioChunkLength = audioEventBlockingQueue.size();
        int width = canvas.getWidth();
        float stepWidth = (float)width / 60;
        int height = canvas.getHeight();
        for(int i = 0; i < audioChunkLength; i++) {
            MonitoringService.AudioEvent audioEvent = audioEventBlockingQueue.poll();
            if(audioEvent == null) {
                continue;
            }
            long timeStamp = audioEvent.getTimeStamp();
            int position = (int)(((timeStamp / 1000l) % 60) * stepWidth);
            canvas.drawColor(Color.WHITE);
            canvas.drawLine(position, 0, position, height - 1, paint);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(!isRunning) {
            return;
        }
        isRunning = false;
        executorService.shutdownNow();
    }

    @Override
    public void update(Observable observable, Object data) {
        MonitoringService.AudioEvent audioEvent = (MonitoringService.AudioEvent)data;
        audioEventBlockingQueue.add(audioEvent);
    }
}
