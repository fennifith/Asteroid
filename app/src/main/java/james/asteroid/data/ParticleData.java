package james.asteroid.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ParticleData extends DrawerData {

    public float x, y, xDiff, yDiff;
    public float size, sizeDiff, sizeDiffDiff;
    public float rotate, rotateDiff;

    public ParticleData(Paint paint) {
        super(paint);
        x = (float) Math.random();
        yDiff = (float) (Math.random() * 3) + 1;
    }

    public ParticleData(Paint paint, float x, float y) {
        super(paint);
        this.x = x;
        this.y = y;
        xDiff = ((float) (Math.random() + 0.2) - 0.5f) * 0.016f;
        yDiff = ((float) (Math.random() + 0.2) - 0.5f) * 16;
        size = 1;
        sizeDiff = 4;
        sizeDiffDiff = -0.2f;
        rotate = (float) Math.random() * 360;
        rotateDiff = (float) Math.random() * 10;
    }

    public Rect next(float speed, int width, int height) {
        if (y >= 0 && y <= height && x >= 0 && x <= width) {
            y += yDiff * speed;
            x += xDiff * speed;
        } else return null;

        size += sizeDiff;
        sizeDiff += sizeDiffDiff;
        if (size < 2)
            size = 2;

        rotate += rotateDiff;

        float left = x * width;
        return new Rect((int) left - (int) (size / 2), (int) y - (int) (size / 2), (int) left + (int) (size / 2), (int) y + (int) (size / 2));
    }

    @Override
    public boolean draw(Canvas canvas, float speed) {
        Rect rect = next(speed, canvas.getWidth(), canvas.getHeight());
        if (rect != null) {
            canvas.save();
            canvas.rotate(rotate, rect.centerX(), rect.centerY());
            canvas.drawRect(rect, paint(0));
            canvas.restore();
            return true;
        } else return false;
    }
}
