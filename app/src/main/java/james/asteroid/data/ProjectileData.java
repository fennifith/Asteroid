package james.asteroid.data;

import android.graphics.Rect;

public class ProjectileData {

    public float x, y, xDiff, yDiff = 4;

    public ProjectileData(float x, float y, float xDiff, float yDiff) {
        this.x = x;
        this.y = y;
        this.xDiff = xDiff;
        this.yDiff = yDiff;
    }

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
