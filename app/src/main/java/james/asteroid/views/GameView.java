package james.asteroid.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

import james.asteroid.R;

public class GameView extends SurfaceView implements Runnable, View.OnTouchListener {

    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private boolean isRunning;
    private Thread thread;

    private Bitmap shipBitmap;
    private float shipPositionX = 0.5f;
    private float shipPositionY = -1f;
    private float shipPositionStartX;
    private float shipRotation;

    private Bitmap asteroidBitmap;
    private Bitmap asteroidBitmap2;
    private List<Asteroid> asteroids;
    private long asteroidTime, asteroidLength;

    private List<Projectile> projectiles;
    private long projectileTime;

    private List<Particle> particles;

    private ValueAnimator animator;
    private GameListener listener;
    private boolean isPlaying;
    private int score;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        surfaceHolder = getHolder();

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        particles = new ArrayList<>();
        particles.add(new Particle());

        projectiles = new ArrayList<>();

        shipBitmap = tintBitmap(getBitmap(R.drawable.ic_ship));

        asteroidBitmap = tintBitmap(getBitmap(R.drawable.ic_asteroid));
        asteroidBitmap2 = tintBitmap(getBitmap(R.drawable.ic_asteroid_two));
        asteroids = new ArrayList<>();
    }

    private Bitmap getBitmap(@DrawableRes int id) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), id);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            drawable = (DrawableCompat.wrap(drawable)).mutate();

        Bitmap result = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return result;
    }

    private Bitmap tintBitmap(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0,0,0, height, ContextCompat.getColor(getContext(), R.color.colorAccent), ContextCompat.getColor(getContext(), R.color.colorPrimary), Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(0,0, width, height, paint);

        return result;
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        while (isRunning) {
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas == null)
                    return;

                canvas.drawColor(Color.BLACK);

                for (Particle particle : new ArrayList<>(particles)) {
                    Rect rect = particle.next(canvas.getWidth(), canvas.getHeight());
                    if (rect != null)
                        canvas.drawRect(rect, paint);
                    else particles.remove(particle);
                }

                particles.add(new Particle());

                for (Asteroid asteroid : new ArrayList<>(asteroids)) {
                    Matrix matrix = asteroid.next(canvas.getWidth(), canvas.getHeight());
                    if (matrix != null) {
                        canvas.drawBitmap(asteroid.asteroidBitmap, matrix, paint);
                    } else {
                        asteroids.remove(asteroid);
                        asteroidLength -= (asteroidLength * 0.1);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null)
                                    listener.onAsteroidPassed();
                            }
                        });
                    }
                }

                if (isPlaying() && System.currentTimeMillis() - asteroidTime > asteroidLength) {
                    asteroidTime = System.currentTimeMillis();
                    asteroids.add(new Asteroid(Math.round(Math.random()) == 0 ? asteroidBitmap : asteroidBitmap2));
                }

                float left = canvas.getWidth() * shipPositionX;
                float top = canvas.getHeight() - (shipBitmap.getHeight() * shipPositionY);

                Matrix matrix = new Matrix();
                matrix.postTranslate(-shipBitmap.getWidth() / 2, -shipBitmap.getHeight() / 2);
                matrix.postRotate(shipRotation);
                matrix.postTranslate(left, top);
                canvas.drawBitmap(shipBitmap, matrix, paint);

                Rect position = new Rect(
                        (int) left - (shipBitmap.getWidth() / 2),
                        (int) top - (shipBitmap.getWidth() / 2),
                        (int) left + (shipBitmap.getWidth() / 2),
                        (int) top + (shipBitmap.getWidth() / 2)
                );

                for (Projectile projectile : new ArrayList<>(projectiles)) {
                    Rect rect = projectile.next(canvas.getWidth(), canvas.getHeight());
                    if (rect != null) {
                        for (Asteroid asteroid : new ArrayList<>(asteroids)) {
                            if (isPlaying && asteroid.position != null && rect.intersect(asteroid.position)) {
                                projectiles.remove(projectile);
                                asteroids.remove(asteroid);

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (listener != null)
                                            listener.onScoreChanged(++score);
                                    }
                                });
                                break;
                            }
                        }

                        canvas.drawRect(rect, paint);
                    } else projectiles.remove(projectile);
                }

                if (isPlaying) {
                    for (Asteroid asteroid : asteroids) {
                        if (asteroid.position != null && position.intersect(asteroid.position)) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null)
                                        listener.onCollision();

                                    stop();
                                }
                            });
                        }
                    }
                }

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void play() {
        isPlaying = true;
        score = 0;
        shipPositionX = 0.5f;
        shipRotation = 0;
        asteroidLength = 3000;
        asteroids.clear();
        projectiles.clear();

        if (animator != null && animator.isStarted())
            animator.cancel();

        ValueAnimator animator = ValueAnimator.ofFloat(shipPositionY, 1f);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                shipPositionY = (float) valueAnimator.getAnimatedValue();
            }
        });
        animator.start();

        setOnTouchListener(this);
    }

    public void stop() {
        isPlaying = false;
        setOnTouchListener(null);

        if (animator != null && animator.isStarted())
            animator.cancel();

        ValueAnimator animator = ValueAnimator.ofFloat(shipPositionY, -1f);
        animator.setDuration(2000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                shipPositionY = (float) valueAnimator.getAnimatedValue();
                shipRotation = 720 * valueAnimator.getAnimatedFraction();
            }
        });
        animator.start();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void onPause() {
        if (thread == null)
            return;

        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
        thread = null;
    }

    public void onResume() {
        if (thread != null)
            onPause();

        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (System.currentTimeMillis() - projectileTime < 350) {
                    projectiles.add(new Projectile(shipPositionX, shipBitmap.getHeight() * shipPositionY * 1.5f));
                    return false;
                } else projectileTime = System.currentTimeMillis();

                if (animator != null && animator.isStarted())
                    animator.cancel();

                if (event.getX() > getWidth() / 2) {
                    if (shipPositionX < 1) {
                        animator = ValueAnimator.ofFloat(shipPositionX, shipPositionX + 1);
                        animator.setDuration(2000);
                    } else return false;
                } else if (shipPositionX > 0) {
                    animator = ValueAnimator.ofFloat(shipPositionX, shipPositionX - 1);
                    animator.setDuration(2000);
                }

                animator.setInterpolator(new AccelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float newX = (float) valueAnimator.getAnimatedValue();
                        if (newX <= 0)
                            shipPositionX = 0;
                        else if (newX >= 1)
                            shipPositionX = 1;
                        else shipPositionX = newX;
                    }
                });

                animator.start();
                shipPositionStartX = shipPositionX;
                break;
            case MotionEvent.ACTION_UP:
                if (animator != null && animator.isStarted())
                    animator.cancel();

                float newX = shipPositionX + ((shipPositionX - shipPositionStartX) / 1.5f);
                if (newX <= 0)
                    newX = 0;
                else if (newX >= 1)
                    newX = 1;

                animator = ValueAnimator.ofFloat(shipPositionX, newX);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.setDuration(1000);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float newX = (float) valueAnimator.getAnimatedValue();
                        if (newX <= 0)
                            shipPositionX = 0;
                        else if (newX >= 1)
                            shipPositionX = 1;
                        else shipPositionX = newX;
                    }
                });

                animator.start();
                break;
        }
        return true;
    }

    public interface GameListener {
        void onCollision();
        void onAsteroidPassed();

        void onScoreChanged(int score);
    }

    private static class Particle {

        float x, y, yDiff;

        private Particle() {
            x = (float) Math.random();
            yDiff = (float) (Math.random() * 3) + 1;
        }

        private Rect next(int width, int height) {
            if (y < height)
                y += yDiff;
            else return null;

            float left = x * width;
            return new Rect((int) left, (int) y, (int) left + 1, (int) y + 1);
        }
    }

    private static class Projectile {

        float x, y, yDiff = 4, explosion;
        boolean isExploding;

        private Projectile(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private Rect next(int width, int height) {
            if (y < height)
                y += yDiff;
            else return null;

            float left = x * width;
            float top = height - y;
            return new Rect((int) left - 2, (int) top - 2, (int) left + 2, (int) top + 2);
        }
    }

    private static class Asteroid {

        Bitmap asteroidBitmap;
        float x, xDiff, y, yDiff, rotation, rotationDiff;
        Rect position;

        private Asteroid(Bitmap asteroidBitmap) {
            this.asteroidBitmap = asteroidBitmap;
            x = (float) Math.random();
            y = -asteroidBitmap.getHeight();
            rotationDiff = (float) Math.random() - 0.5f;
            xDiff = (float) (Math.random() - 0.5) * 0.002f;
            yDiff = (float) (Math.random() * 6) + 1;
        }

        private Matrix next(int width, int height) {
            if ((y - asteroidBitmap.getHeight()) < height) {
                y += yDiff;
                rotation += rotationDiff;
                x += xDiff;
            } else return null;

            float left = x * width, top = y;
            position = new Rect(
                    (int) left - (asteroidBitmap.getWidth() / 2),
                    (int) top - (asteroidBitmap.getHeight() / 2),
                    (int) left + (asteroidBitmap.getWidth() / 2),
                    (int) top + (asteroidBitmap.getHeight() / 2)
            );

            Matrix matrix = new Matrix();
            matrix.postTranslate(-asteroidBitmap.getWidth() / 2, -asteroidBitmap.getHeight() / 2);
            matrix.postRotate(rotation);
            matrix.postTranslate(left, top);

            return matrix;
        }
    }
}
