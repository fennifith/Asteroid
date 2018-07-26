package james.asteroid.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class CloudData extends DrawerData {

    private int y;
    private float start, end;

    public CloudData(Paint paint, float start, float end) {
        super(paint);
        this.start = start;
        this.end = end;
    }

    public float getStart() {
        return start;
    }

    public float getEnd() {
        return end;
    }

    public Rect next(float speed, int width, int height) {
        if (y >= 0 && y <= height) {
            y++;
        } else return null;

        return new Rect((int) (start * width), y - 10, (int) (end * width), y);
    }

    @Override
    public boolean draw(Canvas canvas, float speed) {
        Rect rect = next(speed, canvas.getWidth(), canvas.getHeight());
        if (rect != null) {
            canvas.drawRect(rect, paint(0));
            return true;
        } else return false;
    }
}
