package james.asteroid.data;

import android.graphics.Rect;

public class ParticleData {

    public float x, y, xDiff, yDiff;
    public boolean isAccent;

    public ParticleData() {
        x = (float) Math.random();
        yDiff = (float) (Math.random() * 3) + 1;
    }

    public ParticleData(float x, float y) {
        this.x = x;
        this.y = y;
        xDiff = ((float) (Math.random() + 0.2) - 0.5f) * 0.016f;
        yDiff = ((float) (Math.random() + 0.2) - 0.5f) * 16;
        isAccent = true;
    }

    public Rect next(float speed, int width, int height) {
        if (y >= 0 && y <= height && x >= 0 && x <= width) {
            y += yDiff * speed;
            x += xDiff * speed;
        } else return null;

        float left = x * width;
        return new Rect((int) left, (int) y, (int) left + 1, (int) y + 1);
    }

}
