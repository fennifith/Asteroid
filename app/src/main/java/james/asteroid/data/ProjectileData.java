package james.asteroid.data;

import android.graphics.Rect;

public class ProjectileData {

    public float x, y, xDiff, yDiff;

    public ProjectileData(float x, float y, float xDiff, float yDiff) {
        this.x = x;
        this.y = y;
        this.xDiff = xDiff;
        this.yDiff = yDiff;
    }

    /**
     * Calculates the Rect to draw the projectile in for the next frame.
     *
     * @param speed         The speed of the projectile.
     * @param width         The width of the drawing canvas.
     * @param height        The height of the drawing canvas.
     * @return              The Rect to draw the projectile in for the
     *                      next frame - equals null if it can no longer
     *                      be drawn within the given width/height.
     */
    public Rect next(float speed, int width, int height) {
        if (x >= 0 && x <= 1 && y >= 0 && y <= height) {
            x += xDiff * speed;
            y += yDiff * speed;
        } else return null;

        float left = x * width;
        float top = height - y;
        return new Rect((int) left - 2, (int) top - 2, (int) left + 2, (int) top + 2);
    }

}
