package james.asteroid.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class CloudData extends DrawerData {

    private int y = -10;
    private float start, end;

    public CloudData(Paint paint, float start, float end) {
        super(paint);
        this.start = start;
        this.end = end;
    }

    /**
     * Get the position of start of the cloud, from 0-1, as a
     * scale of the width of the canvas.
     *
     * @return The position of the start of the cloud, from 0-1,
     *         to be scaled by the width of the canvas.
     */
    public float getStart() {
        return start;
    }

    /**
     * Get the position of the end of the cloud, from 0-1, as a
     * scale of the width of the canvas.
     *
     * @return The position of the end of the cloud, from 0-1,
     *         to be scaled by the width of the canvas.
     */
    public float getEnd() {
        return end;
    }

    /**
     * Calculates the next Rect area to draw the cloud in on the
     * canvas.
     *
     * @param speed         The speed of the cloud.
     * @param width         The width of the drawing canvas.
     * @param height        The height of the drawing canvas.
     * @return              The Rect area to draw the cloud in
     *                      for the next frame. Returns "null" if
     *                      the cloud can no longer be drawn within
     *                      the given width/height.
     */
    public Rect next(float speed, int width, int height) {
        if (y <= height) {
            y++;
        } else return null;

        return new Rect((int) (start * width), y, (int) (end * width), y + 10);
    }

    /**
     * Draws the cloud on the given canvas.
     *
     * @param canvas        The canvas to draw the cloud on.
     * @param speed         The speed the cloud should travel at.
     * @return              Whether the cloud was drawn on the canvas;
     *                      false if the cloud can no longer be drawn
     *                      within the visible area.
     */
    @Override
    public boolean draw(Canvas canvas, float speed) {
        Rect rect = next(speed, canvas.getWidth(), canvas.getHeight());
        if (rect != null) {
            canvas.drawRect(rect, paint(0));
            return true;
        } else return false;
    }
}
