package james.asteroid.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
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
import james.asteroid.utils.ImageUtils;

public class GameView extends SurfaceView implements Runnable, View.OnTouchListener {

    private Paint paint;
    private Paint accentPaint;
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
    private float speed = 1;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        surfaceHolder = getHolder();

        int colorPrimaryLight = ContextCompat.getColor(getContext(), R.color.colorPrimaryLight);
        int colorPrimary = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        int colorAccent = ContextCompat.getColor(getContext(), R.color.colorAccent);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        accentPaint = new Paint();
        accentPaint.setColor(colorPrimaryLight);
        accentPaint.setStyle(Paint.Style.FILL);
        accentPaint.setAntiAlias(true);

        particles = new ArrayList<>();
        particles.add(new Particle());

        projectiles = new ArrayList<>();

        shipBitmap = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(getContext(), R.drawable.ic_ship), colorAccent, colorPrimary);

        asteroidBitmap = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(getContext(), R.drawable.ic_asteroid), colorAccent, colorPrimary);
        asteroidBitmap2 = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(getContext(), R.drawable.ic_asteroid_two), colorAccent, colorPrimary);
        asteroids = new ArrayList<>();
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
                        canvas.drawRect(rect, particle.isAccent ? accentPaint : paint);
                    else particles.remove(particle);
                }

                particles.add(new Particle());

                for (Asteroid asteroid : new ArrayList<>(asteroids)) {
                    Matrix matrix = asteroid.next(canvas.getWidth(), canvas.getHeight());
                    if (matrix != null) {
                        canvas.drawBitmap(asteroid.asteroidBitmap, matrix, paint);
                    } else {
                        asteroids.remove(asteroid);
                        if (asteroidLength > 750)
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

                                for (int i = 0; i < 50; i++) {
                                    particles.add(new Particle((float) rect.left / canvas.getWidth(), rect.top));
                                }

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (listener != null)
                                            listener.onScoreChanged(++score);
                                    }
                                });

                                speed += (speed - 0.9) * 0.1;
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
        speed = 1;
        shipPositionX = 0.5f;
        shipRotation = 0;
        asteroidLength = 3000;
        asteroids.clear();
        projectiles.clear();

        if (animator != null && animator.isStarted())
            animator.cancel();

        ValueAnimator animator = ValueAnimator.ofFloat(shipPositionY, 1);
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

        ValueAnimator animator1 = ValueAnimator.ofFloat(speed, 1);
        animator1.setInterpolator(new DecelerateInterpolator());
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                speed = (float) valueAnimator.getAnimatedValue();
            }
        });
        animator1.start();
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
                    if (listener != null)
                        listener.onProjectileFired();
                    return false;
                } else projectileTime = System.currentTimeMillis();

                if (animator != null && animator.isStarted())
                    animator.cancel();

                if (event.getX() > getWidth() / 2) {
                    if (shipPositionX < 1)
                        animator = ValueAnimator.ofFloat(shipPositionX, shipPositionX + 1);
                    else return false;
                } else if (shipPositionX > 0)
                    animator = ValueAnimator.ofFloat(shipPositionX, shipPositionX - 1);

                animator.setDuration((long) (2000 / speed));
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
                animator.setDuration((long) (1000 / speed));
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

        void onProjectileFired();
        void onScoreChanged(int score);
    }

    private class Particle {

        float x, y, xDiff, yDiff;
        boolean isAccent;

        private Particle() {
            x = (float) Math.random();
            yDiff = (float) (Math.random() * 3) + 1;
        }

        private Particle(float x, float y) {
            this.x = x;
            this.y = y;
            xDiff = ((float) (Math.random() + 0.2) - 0.5f) * 0.016f;
            yDiff = ((float) (Math.random() + 0.2) - 0.5f) * 16;
            isAccent = true;
        }

        private Rect next(int width, int height) {
            if (y >= 0 && y <= height && x >= 0 && x <= width) {
                y += yDiff * speed;
                x += xDiff * speed;
            } else return null;

            float left = x * width;
            return new Rect((int) left, (int) y, (int) left + 1, (int) y + 1);
        }
    }

    private class Projectile {

        float x, y, yDiff = 4;

        private Projectile(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private Rect next(int width, int height) {
            if (y < height)
                y += yDiff * speed;
            else return null;

            float left = x * width;
            float top = height - y;
            return new Rect((int) left - 2, (int) top - 2, (int) left + 2, (int) top + 2);
        }
    }

    private class Asteroid {

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
                y += yDiff * speed;
                rotation += rotationDiff;
                x += xDiff * speed;
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
