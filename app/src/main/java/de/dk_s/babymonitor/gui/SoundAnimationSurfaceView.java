package de.dk_s.babymonitor.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dk_s.babymonitor.ChildActivity;
import de.dk_s.babymonitor.ParentActivity;
import de.dk_s.babymonitor.monitoring.AudioEventHistoryDataProvider;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;


public class SoundAnimationSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "SoundAnimationSurfaceView";

    ExecutorService executorService;

    private SurfaceHolder holder;

    private Paint paint;

    private Paint alternativePaint;

    private boolean isRunning = false;

    public SoundAnimationSurfaceView(Context context) {
        super(context);
        init();
    }

    public SoundAnimationSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private AudioEventHistoryDataProvider getAudioEventHistoryDataProvider() {
        Context context = getContext();
        if (context instanceof ChildActivity) {
            return ((ChildActivity) context).getMonitoringService();
        } else if (context instanceof ParentActivity) {
            return ((ParentActivity) context).getConnectionService();
        }
        return null;
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);

        alternativePaint = new Paint();
        alternativePaint.setColor(Color.RED);
        alternativePaint.setStyle(Paint.Style.FILL);
        alternativePaint.setStrokeWidth(5);

        setWillNotDraw(false);

    }

    private void drawAnimation(Canvas canvas, Deque<BabyVoiceMonitor.AudioEvent> recentAudioEventList) {
        long currentTimeStamp = System.currentTimeMillis();
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        float stepWidth = (float) width / 120;
        canvas.drawColor(Color.WHITE);
        if (recentAudioEventList != null) {
            Iterator<BabyVoiceMonitor.AudioEvent> iterator = recentAudioEventList.descendingIterator();
            while (iterator.hasNext()) {
                BabyVoiceMonitor.AudioEvent currentEvent = iterator.next();
                float timeDifferenceInSeconds = (float) (currentTimeStamp - currentEvent.getTimeStamp()) / 1000;
                int position = (int) (timeDifferenceInSeconds * stepWidth);
                if (currentEvent.getEventType() == 0) {
                    canvas.drawLine(width - position, height, width - position, height - currentEvent.getAudioLevel() * height, paint);
                } else if (currentEvent.getEventType() == 1 || currentEvent.getEventType() == 2) {
                    canvas.drawLine(width - position, height, width - position, height - currentEvent.getAudioLevel() * height, alternativePaint);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        isRunning = true;
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    Log.e(TAG, "Request started");
                    AudioEventHistoryDataProvider audioEventHistoryDataProvider = getAudioEventHistoryDataProvider();
                    Deque<BabyVoiceMonitor.AudioEvent> recentAudioEventList = audioEventHistoryDataProvider == null ? null : audioEventHistoryDataProvider.getRecentAudioEvents();
                    Log.e(TAG, "Received List");
                    Canvas canvas = holder.lockCanvas();
                    if (recentAudioEventList != null) {
                        drawAnimation(canvas, recentAudioEventList);
                    }
                    holder.unlockCanvasAndPost(canvas);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
}
