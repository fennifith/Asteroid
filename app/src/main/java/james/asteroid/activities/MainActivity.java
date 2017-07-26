package james.asteroid.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import james.asteroid.utils.FontUtils;
import james.asteroid.views.GameView;
import james.asteroid.R;

public class MainActivity extends AppCompatActivity implements GameView.GameListener, View.OnClickListener {

    private TextView titleView;
    private TextView hintView;
    private GameView gameView;

    private ValueAnimator animator;
    private String appName, hintStart;

    private Handler handler = new Handler();
    private Runnable hintRunnable = new Runnable() {
        @Override
        public void run() {
            if (!hintView.getText().toString().contains("."))
                hintView.setText(String.format(".%s.", hintStart));
            else if (hintView.getText().toString().contains("..."))
                hintView.setText(hintStart);
            else if (hintView.getText().toString().contains(".."))
                hintView.setText(String.format("...%s...", hintStart));
            else if (hintView.getText().toString().contains("."))
                hintView.setText(String.format("..%s..", hintStart));

            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = findViewById(R.id.title);
        hintView = findViewById(R.id.hint);
        gameView = findViewById(R.id.game);

        Typeface typeface = FontUtils.getTypeface(this);

        titleView.setTypeface(typeface);
        titleView.setPaintFlags(titleView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        titleView.getPaint().setShader(new LinearGradient(
                0, 0, 0,
                titleView.getLineHeight(),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                Shader.TileMode.REPEAT
        ));

        hintView.setTypeface(typeface);
        hintView.getPaint().setShader(new LinearGradient(
                0, 0, 0,
                hintView.getLineHeight(),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                Shader.TileMode.REPEAT
        ));

        appName = getString(R.string.app_name);
        hintStart = getString(R.string.hint_start);
        animateTitle(true);

        gameView.setListener(this);
        gameView.setOnClickListener(this);
    }

    private void animateTitle(final boolean isVisible) {
        if (isVisible == titleView.getText().length() > 0)
            return;

        animator = ValueAnimator.ofFloat(isVisible ? 0 : 1, isVisible ? 1 : 0);
        animator.setDuration(2000);
        animator.setStartDelay(500);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                titleView.setText(appName.substring(0, (int) ((float) valueAnimator.getAnimatedValue() * appName.length())));
                hintView.setText(hintStart.substring(0, (int) ((float) valueAnimator.getAnimatedValue() * hintStart.length())));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isVisible)
                    handler.post(hintRunnable);
            }
        });

        animator.start();

        if (!isVisible)
            handler.removeCallbacks(hintRunnable);
    }

    @Override
    public void onPause() {
        if (gameView != null)
            gameView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (gameView != null)
            gameView.onResume();
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                findViewById(android.R.id.content).setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                findViewById(android.R.id.content).setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }
    }

    @Override
    public void onCollision() {
        animateTitle(true);
        gameView.setOnClickListener(this);
    }

    @Override
    public void onAsteroidPassed() {

    }

    @Override
    public void onDistanceChanged(int distance) {

    }

    @Override
    public void onClick(View view) {
        if (!gameView.isPlaying() && (animator == null || !animator.isStarted())) {
            gameView.setOnClickListener(null);
            gameView.play();
            animateTitle(false);
        }
    }
}
