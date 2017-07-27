package james.asteroid.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import james.asteroid.R;
import james.asteroid.utils.FontUtils;
import james.asteroid.utils.ImageUtils;
import james.asteroid.views.GameView;

public class MainActivity extends AppCompatActivity implements GameView.GameListener, View.OnClickListener {

    public static final String PREF_SOUND = "sound";

    private TextView titleView;
    private TextView hintView;
    private ImageView soundView;
    private ImageView achievementsView;
    private ImageView rankView;
    private GameView gameView;

    private ValueAnimator animator;
    private String appName, hintStart;

    private SoundPool soundPool;
    private int laserId;
    private int explosionId;
    private int explosion2Id;
    private int buttonId;
    private int hissId;
    private int coinId;

    private SharedPreferences prefs;

    private boolean isSound;
    private Bitmap soundEnabled;
    private Bitmap soundDisabled;

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

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        titleView = findViewById(R.id.title);
        hintView = findViewById(R.id.hint);
        soundView = findViewById(R.id.sound);
        achievementsView = findViewById(R.id.achievements);
        rankView = findViewById(R.id.rank);
        gameView = findViewById(R.id.game);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .build();
        } else soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

        laserId = soundPool.load(this, R.raw.laser, 1);
        explosionId = soundPool.load(this, R.raw.explosion, 1);
        explosion2Id = soundPool.load(this, R.raw.explosion_two, 1);
        buttonId = soundPool.load(this, R.raw.button, 1);
        hissId = soundPool.load(this, R.raw.hiss, 1);
        coinId = soundPool.load(this, R.raw.coin, 1);

        Typeface typeface = FontUtils.getTypeface(this);
        int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        int colorAccent = ContextCompat.getColor(this, R.color.colorAccent);

        titleView.setTypeface(typeface);
        titleView.setPaintFlags(titleView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        titleView.getPaint().setShader(new LinearGradient(
                0, 0, 0,
                titleView.getLineHeight(),
                colorAccent,
                colorPrimary,
                Shader.TileMode.REPEAT
        ));

        hintView.setTypeface(typeface);
        hintView.getPaint().setShader(new LinearGradient(
                0, 0, 0,
                hintView.getLineHeight(),
                colorAccent,
                colorPrimary,
                Shader.TileMode.REPEAT
        ));

        appName = getString(R.string.app_name);
        hintStart = getString(R.string.hint_start);
        animateTitle(true);

        gameView.setListener(this);
        gameView.setOnClickListener(this);

        isSound = prefs.getBoolean(PREF_SOUND, true);
        soundEnabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_sound_enabled), colorAccent, colorPrimary);
        soundDisabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_sound_disabled), colorAccent, colorPrimary);
        soundView.setImageBitmap(isSound ? soundEnabled : soundDisabled);
        soundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSound = !isSound;
                prefs.edit().putBoolean(PREF_SOUND, isSound).apply();
                soundView.setImageBitmap(isSound ? soundEnabled : soundDisabled);
                if (isSound)
                    soundPool.play(buttonId, 1, 1, 0, 0, 1);
            }
        });

        achievementsView.setImageBitmap(ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_achievements), colorAccent, colorPrimary));
        achievementsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSound)
                    soundPool.play(buttonId, 1, 1, 0, 0, 1);
                //TODO: open achievements screen
            }
        });

        rankView.setImageBitmap(ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_rank), colorAccent, colorPrimary));
        rankView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSound)
                    soundPool.play(buttonId, 1, 1, 0, 0, 1);
                //TODO: open rank screen
            }
        });
    }

    private void animateTitle(final boolean isVisible) {
        animator = ValueAnimator.ofFloat(isVisible ? 0 : 1, isVisible ? 1 : 0);
        animator.setDuration(1500);
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
        super.onResume();
        if (gameView != null)
            gameView.onResume();
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
        if (isSound)
            soundPool.play(explosion2Id, 1, 1, 0, 0, 1);
        animateTitle(true);
        gameView.setOnClickListener(this);
    }

    @Override
    public void onAsteroidPassed() {

    }

    @Override
    public void onProjectileFired() {
        if (isSound)
            soundPool.play(laserId, 1, 1, 0, 0, 1);
    }

    @Override
    public void onScoreChanged(int score) {
        titleView.setText(String.valueOf(score));
        if (isSound)
            soundPool.play(coinId, 1, 1, 0, 0, 1);
    }

    @Override
    public void onClick(View view) {
        if (!gameView.isPlaying() && (animator == null || !animator.isStarted())) {
            gameView.setOnClickListener(null);
            gameView.play();
            animateTitle(false);
            if (isSound)
                soundPool.play(hissId, 1, 1, 0, 0, 1);
        }
    }
}
