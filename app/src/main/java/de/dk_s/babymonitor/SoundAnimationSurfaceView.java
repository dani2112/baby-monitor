package de.dk_s.babymonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SoundAnimationSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    ExecutorService executorService;

    private SurfaceHolder holder;

    private Paint paint;


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
        setWillNotDraw(false);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
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
        canvas.drawColor(Color.GREEN);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
