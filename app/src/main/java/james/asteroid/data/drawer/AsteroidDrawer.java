package james.asteroid.data.drawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import james.asteroid.R;
import james.asteroid.data.AsteroidData;
import james.asteroid.data.DrawerData;
import james.asteroid.data.ParticleData;
import james.asteroid.utils.ImageUtils;

public class AsteroidDrawer extends DrawerData {

    private List<AsteroidData> asteroids;
    List<ParticleData> particles;
    private Bitmap asteroidBitmap;
    private Bitmap asteroidBitmap2;

    private long asteroidTime;
    private float asteroidLength;

    private boolean shouldMakeAsteroids;

    public AsteroidDrawer(Context context, int colorAccent, int colorPrimary, Paint asteroidPaint, Paint particlePaint) {
        super(asteroidPaint, particlePaint);
        asteroids = new ArrayList<>();
        particles = new ArrayList<>();
        asteroidBitmap = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(context, R.drawable.ic_asteroid), colorAccent, colorPrimary);
        asteroidBitmap2 = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(context, R.drawable.ic_asteroid_two), colorAccent, colorPrimary);
    }

    /**
     * Set whether the drawer should generate its own asteroids at
     * a set interval.
     *
     * @param shouldMakeAsteroids Whether the drawer should generate
     *                            its own asteroids.
     */
    public void setMakeAsteroids(boolean shouldMakeAsteroids) {
        this.shouldMakeAsteroids = shouldMakeAsteroids;
        asteroidLength = 3000;
        if (shouldMakeAsteroids)
            asteroids.clear();
    }

    /**
     * Make a new asteroid. Like magic.
     */
    public void makeNew() {
        asteroidTime = System.currentTimeMillis();
        asteroids.add(new AsteroidData(Math.round(Math.random()) == 0 ? asteroidBitmap : asteroidBitmap2));
    }

    /**
     * @return The amount of asteroids currently visible on the screen.
     */
    public int size() {
        return asteroids.size();
    }

    /**
     * Determine if there is an asteroid intersecting the given position
     * on the canvas; if so, return it.
     *
     * @param position      The position Rect to check if an asteroid intersects.
     * @return              The AsteroidData if it intersects the given position;
     *                      null if there is nothing there.
     */
    public AsteroidData asteroidAt(Rect position) {
        for (AsteroidData asteroid : asteroids) {
            if (asteroid.position != null && position.intersect(asteroid.position))
                return asteroid;
        }

        return null;
    }

    /**
     * Destroy a given asteroid; generate an explosion of particles in its place.
     *
     * @param asteroid      The asteroid to obliterate. Kaboom! Kablowie! Kapow!
     *                      Badabadoosh!
     */
    public void destroy(AsteroidData asteroid) {
        asteroids.remove(asteroid);

        for (int i = 0; i < 50; i++) {
            particles.add(new ParticleData(paint(1), asteroid.x, asteroid.y));
        }
    }

    @Override
    public boolean draw(Canvas canvas, float speed) {
        boolean isPassed = false;

        for (AsteroidData asteroid : new ArrayList<>(asteroids)) {
            Matrix matrix = asteroid.next(speed, canvas.getWidth(), canvas.getHeight());
            if (matrix != null) {
                canvas.drawBitmap(asteroid.asteroidBitmap, matrix, paint(0));
            } else {
                isPassed = true;
                asteroids.remove(asteroid);
                if (asteroidLength > 750)
                    asteroidLength -= (asteroidLength * 0.1);
            }
        }

        for (ParticleData particle : new ArrayList<>(particles)) {
            if (!particle.draw(canvas, 1))
                particles.remove(particle);
        }

        if (shouldMakeAsteroids && System.currentTimeMillis() - asteroidTime > asteroidLength)
            makeNew();

        return isPassed;
    }
}
