package james.asteroid.services;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

import james.asteroid.R;
import james.asteroid.data.AsteroidData;
import james.asteroid.data.ParticleData;
import james.asteroid.utils.ImageUtils;

public class BackgroundService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new BackgroundEngine();
    }

    private class BackgroundEngine extends Engine {

        private Paint paint;
        private Paint accentPaint;

        private Bitmap asteroidBitmap;
        private Bitmap asteroidBitmap2;
        private List<AsteroidData> asteroids;
        private List<ParticleData> particles;
        private long asteroidTime, asteroidLength = 5000;

        private boolean isVisible;
        private DrawingThread thread;

        public BackgroundEngine() {
            thread = new DrawingThread(this);
            thread.start();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            if (!thread.isAlive()) {
                thread = new DrawingThread(this);
                thread.start();
            }

            int colorPrimaryLight = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight);
            int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
            int colorAccent = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);

            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);

            accentPaint = new Paint();
            accentPaint.setColor(colorPrimaryLight);
            accentPaint.setStyle(Paint.Style.FILL);
            accentPaint.setAntiAlias(true);

            particles = new ArrayList<>();
            particles.add(new ParticleData());

            asteroidBitmap = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(getApplicationContext(), R.drawable.ic_asteroid), colorAccent, colorPrimary);
            asteroidBitmap2 = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(getApplicationContext(), R.drawable.ic_asteroid_two), colorAccent, colorPrimary);
            asteroids = new ArrayList<>();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            isVisible = visible;
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            isVisible = true;
            draw(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            isVisible = false;
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            draw(holder);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        private void draw(SurfaceHolder holder) {
            if (holder == null || !holder.getSurface().isValid())
                return;

            Canvas canvas;
            try {
                canvas = holder.lockCanvas();
                if (canvas == null) return;
            } catch (Exception e) {
                return;
            }

            canvas.drawColor(Color.BLACK);

            for (ParticleData particle : new ArrayList<>(particles)) {
                Rect rect = particle.next(1, canvas.getWidth(), canvas.getHeight());
                if (rect != null)
                    canvas.drawRect(rect, particle.isAccent ? accentPaint : paint);
                else particles.remove(particle);
            }

            particles.add(new ParticleData());

            for (AsteroidData asteroid : new ArrayList<>(asteroids)) {
                Matrix matrix = asteroid.next(1, canvas.getWidth(), canvas.getHeight());
                if (matrix != null) {
                    canvas.drawBitmap(asteroid.asteroidBitmap, matrix, paint);
                } else asteroids.remove(asteroid);
            }

            if (System.currentTimeMillis() - asteroidTime > asteroidLength) {
                asteroidTime = System.currentTimeMillis();
                asteroids.add(new AsteroidData(Math.round(Math.random()) == 0 ? asteroidBitmap : asteroidBitmap2));
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private static class DrawingThread extends Thread {

        private BackgroundEngine engine;

        private DrawingThread(BackgroundEngine engine) {
            this.engine = engine;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                }

                if (engine.isVisible) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            engine.draw(engine.getSurfaceHolder());
                        }
                    });
                }
            }
        }
    }
}