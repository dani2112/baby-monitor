package de.dk_s.babymonitor;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Deque;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;
import de.dk_s.babymonitor.monitoring.MonitoringService;


public class SoundAnimationSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Observer {

    private static final String TAG = "SoundAnimationSurfaceView";

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

    private MonitoringService getMonitoringService() {
        Context context = getContext();
        if(context instanceof ChildActivity) {
            return ((ChildActivity)context).getMonitoringService();
        } else if (context instanceof ParentActivity) {
            return null;
        }
        return null;
    }

    private void init() {
        getMonitoringService();
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);

        setWillNotDraw(false);
        isRunning = true;
        executorService = Executors.newSingleThreadExecutor();
    }

    private void drawAnimation(Canvas canvas, Deque<BabyVoiceMonitor.AudioEvent> recentAudioEventList) {
        int width = canvas.getWidth();
        float stepWidth = (float) width / 60;
        int height = canvas.getHeight();
        canvas.drawColor(Color.WHITE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        executorService.shutdownNow();
    }

    @Override
    public void update(Observable observable, Object data) {
        final Deque<BabyVoiceMonitor.AudioEvent> recentAudioEventList = (Deque<BabyVoiceMonitor.AudioEvent>) data;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = holder.lockCanvas();
                drawAnimation(canvas, recentAudioEventList);
                holder.unlockCanvasAndPost(canvas);
            }
        });
    }
}
