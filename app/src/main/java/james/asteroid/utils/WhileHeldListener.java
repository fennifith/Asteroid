package james.asteroid.utils;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public abstract class WhileHeldListener implements View.OnTouchListener, Runnable {

    private Handler handler;
    private int interval;
    private MotionEvent event;

    public WhileHeldListener() {
        interval = 1;
    }

    public WhileHeldListener(int interval) {
        this.interval = interval;
    }

    public abstract void onHeld(MotionEvent event);

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (handler != null)
                    return true;
                handler = new Handler();
                handler.postDelayed(this, interval);
                this.event = event;
                break;
            case MotionEvent.ACTION_UP:
                if (handler == null)
                    return true;
                handler.removeCallbacks(this);
                handler = null;
                break;
        }
        return true;
    }

    public void clear() {
        if (handler != null)
            handler.removeCallbacks(this);
    }

    @Override
    public void run() {
        onHeld(event);
        handler.postDelayed(this, interval);
    }
}
