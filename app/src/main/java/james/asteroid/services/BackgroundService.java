package james.asteroid.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;

import james.asteroid.R;
import james.asteroid.data.drawer.AsteroidDrawer;
import james.asteroid.data.drawer.BackgroundDrawer;

public class BackgroundService extends WallpaperService {

    private static final String PREF_SPEED = "wallpaperSpeed";
    private static final String PREF_ASTEROIDS = "wallpaperAsteroids";
    private static final String PREF_ASTEROID_SPEED = "wallpaperAsteroidSpeed";
    private static final String PREF_ASTEROID_INTERVAL = "wallpaperAsteroidInterval";

    @Override
    public Engine onCreateEngine() {
        return new BackgroundEngine();
    }

    public static void setSpeed(Context context, int speed) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREF_SPEED, speed).apply();
    }

    public static void setAsteroids(Context context, boolean isAsteroids) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_ASTEROIDS, isAsteroids).apply();
    }

    public static void setAsteroidSpeed(Context context, int speed) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREF_ASTEROID_SPEED, speed).apply();
    }

    public static void setAsteroidInterval(Context context, int interval) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREF_ASTEROID_INTERVAL, interval).apply();
    }

    public static int getSpeed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_SPEED, 1);
    }

    public static boolean isAsteroids(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_ASTEROIDS, true);
    }

    public static int getAsteroidSpeed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_ASTEROID_SPEED, 1);
    }

    public static int getAsteroidInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_ASTEROID_INTERVAL, 5000);
    }

    private class BackgroundEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private Paint paint;
        private Paint accentPaint;

        private BackgroundDrawer background;
        private AsteroidDrawer asteroids;
        private int particleSpeed;
        private boolean isAsteroids;
        private int asteroidSpeed;
        private long asteroidTime;
        private long asteroidInterval;

        private SharedPreferences prefs;
        private Handler handler = new Handler();
        private Runnable runnable = () -> draw(getSurfaceHolder());

        public BackgroundEngine() {
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            updateSettings();
        }

        private void updateSettings() {
            particleSpeed = getSpeed(getApplicationContext());
            isAsteroids = isAsteroids(getApplicationContext());
            asteroidSpeed = getAsteroidSpeed(getApplicationContext());
            asteroidInterval = getAsteroidInterval(getApplicationContext());
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
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

            background = new BackgroundDrawer(paint, accentPaint);

            updateSettings();
            prefs.registerOnSharedPreferenceChangeListener(this);
            draw(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            prefs.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            draw(holder);
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            draw(holder);
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

            background.draw(canvas, particleSpeed);

            holder.unlockCanvasAndPost(canvas);

            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 10);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            updateSettings();
        }
    }
}