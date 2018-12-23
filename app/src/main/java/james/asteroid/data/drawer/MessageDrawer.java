package james.asteroid.data.drawer;

import android.graphics.Canvas;
import android.graphics.Paint;

import james.asteroid.data.DrawerData;
import james.asteroid.utils.ConversionUtils;

public class MessageDrawer extends DrawerData {

    private static final long MESSAGE_DELAY = 3000;

    private String message;
    private long messageTime;

    private Paint paint;

    public MessageDrawer(Paint paint) {
        this.paint = paint;
    }

    /**
     * Display a new message! Shows it on the screen for an amount
     * of time, then fades out.
     *
     * @param message The message string to display.
     */
    public void drawMessage(String message) {
        this.message = message;
        messageTime = System.currentTimeMillis();
    }

    @Override
    public boolean draw(Canvas canvas, float speed) {
        long diff = Math.abs(System.currentTimeMillis() - messageTime);

        if (diff < MESSAGE_DELAY) {
            if (diff < 500) {
                paint.setAlpha((int) (255 * ((float) diff / 500)));
            } else if (diff > MESSAGE_DELAY - 500) {
                paint.setAlpha((int) (255 * ((float) (MESSAGE_DELAY - diff) / 500)));
            } else paint.setAlpha(1);

            canvas.drawText(message, canvas.getWidth() / 2, canvas.getHeight() - ConversionUtils.getPixelsFromDp(64), paint);
        }

        return true;
    }
}
