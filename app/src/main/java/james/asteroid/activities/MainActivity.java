package james.asteroid.activities;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import james.asteroid.R;
import james.asteroid.data.WeaponData;
import james.asteroid.utils.FontUtils;
import james.asteroid.utils.ImageUtils;
import james.asteroid.utils.PreferenceUtils;
import james.asteroid.views.GameView;

public class MainActivity extends AppCompatActivity implements GameView.GameListener, View.OnClickListener {

    private TextView titleView;
    private TextView highScoreView;
    private TextView hintView;
    private ImageView musicView;
    private ImageView soundView;
    private ImageView achievementsView;
    private ImageView rankView;
    private LinearLayout buttonLayout;
    private ImageView pauseView;
    private ImageView stopView;
    private GameView gameView;

    private ValueAnimator animator;
    private String appName, hintStart, weapon;

    private SoundPool soundPool;
    private int explosionId;
    private int explosion2Id;
    private int buttonId;
    private int hissId;
    private int coinId;
    private int upgradeId;
    private int replenishId;
    private int errorId;

    private SharedPreferences prefs;

    private boolean isSound;
    private Bitmap soundEnabled;
    private Bitmap soundDisabled;

    private MediaPlayer player;
    private boolean isMusic;
    private Bitmap musicEnabled;
    private Bitmap musicDisabled;

    private boolean isPaused;
    private Bitmap play;
    private Bitmap pause;
    private Bitmap stop;

    private Handler handler = new Handler();
    private Runnable hintRunnable = new Runnable() {
        @Override
        public void run() {
            if (!gameView.isPlaying() && (animator == null || !animator.isStarted())) {
                if (!hintView.getText().toString().contains("."))
                    hintView.setText(String.format(".%s.", hintStart));
                else if (hintView.getText().toString().contains("..."))
                    hintView.setText(hintStart);
                else if (hintView.getText().toString().contains(".."))
                    hintView.setText(String.format("...%s...", hintStart));
                else if (hintView.getText().toString().contains("."))
                    hintView.setText(String.format("..%s..", hintStart));

                if (highScoreView.getVisibility() == View.VISIBLE)
                    highScoreView.setVisibility(View.GONE);
                else highScoreView.setVisibility(View.VISIBLE);
            }

            if (isPaused) {
                if (pauseView.getAlpha() == 1)
                    pauseView.setAlpha(0.5f);
                else pauseView.setAlpha(1f);
            }

            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        titleView = findViewById(R.id.title);
        highScoreView = findViewById(R.id.highScore);
        hintView = findViewById(R.id.hint);
        buttonLayout = findViewById(R.id.buttonLayout);
        musicView = findViewById(R.id.music);
        soundView = findViewById(R.id.sound);
        achievementsView = findViewById(R.id.achievements);
        rankView = findViewById(R.id.rank);
        pauseView = findViewById(R.id.pause);
        stopView = findViewById(R.id.stop);
        gameView = findViewById(R.id.game);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .build();
        } else soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

        explosionId = soundPool.load(this, R.raw.explosion, 1);
        explosion2Id = soundPool.load(this, R.raw.explosion_two, 1);
        buttonId = soundPool.load(this, R.raw.button, 1);
        hissId = soundPool.load(this, R.raw.hiss, 1);
        coinId = soundPool.load(this, R.raw.coin, 1);
        replenishId = soundPool.load(this, R.raw.replenish, 1);
        upgradeId = soundPool.load(this, R.raw.upgrade, 1);
        errorId = soundPool.load(this, R.raw.error, 1);
        WeaponData.loadSounds(this, soundPool);

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

        highScoreView.setTypeface(typeface);
        highScoreView.getPaint().setShader(new LinearGradient(
                0, 0, 0,
                hintView.getLineHeight(),
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

        isMusic = prefs.getBoolean(PreferenceUtils.PREF_MUSIC, true);
        musicEnabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_music_enabled), colorAccent, colorPrimary);
        musicDisabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_music_disabled), colorAccent, colorPrimary);
        musicView.setImageBitmap(isMusic ? musicEnabled : musicDisabled);
        musicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMusic = !isMusic;
                prefs.edit().putBoolean(PreferenceUtils.PREF_MUSIC, isMusic).apply();
                musicView.setImageBitmap(isMusic ? musicEnabled : musicDisabled);

                if (isMusic) player.start();
                else player.pause();

                if (isSound)
                    soundPool.play(buttonId, 1, 1, 0, 0, 1);
            }
        });

        isSound = prefs.getBoolean(PreferenceUtils.PREF_SOUND, true);
        soundEnabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_sound_enabled), colorAccent, colorPrimary);
        soundDisabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_sound_disabled), colorAccent, colorPrimary);
        soundView.setImageBitmap(isSound ? soundEnabled : soundDisabled);
        soundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSound = !isSound;
                prefs.edit().putBoolean(PreferenceUtils.PREF_SOUND, isSound).apply();
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

        play = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_play), colorAccent, colorPrimary);
        pause = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_pause), colorAccent, colorPrimary);
        stop = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_stop), colorAccent, colorPrimary);

        pauseView.setImageBitmap(pause);
        pauseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPaused = !isPaused;
                if (isPaused) {
                    pauseView.setImageBitmap(play);
                    stopView.setVisibility(View.VISIBLE);
                    gameView.onPause();
                } else {
                    pauseView.setImageBitmap(pause);
                    pauseView.setAlpha(1f);
                    stopView.setVisibility(View.GONE);
                    gameView.onResume();
                }
            }
        });

        stopView.setImageBitmap(stop);
        stopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPaused) {
                    pauseView.setImageBitmap(pause);
                    pauseView.setAlpha(1f);
                    gameView.onResume();
                    isPaused = false;
                }

                gameView.stop();
                onStop(0);
            }
        });

        int highScore = prefs.getInt(PreferenceUtils.PREF_HIGH_SCORE, 0);
        if (highScore > 0)
            highScoreView.setText(String.format(getString(R.string.score_high), highScore));

        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true);
        if (isMusic)
            player.start();

        handler.postDelayed(hintRunnable, 1000);
    }

    private void animateTitle(final boolean isVisible) {
        highScoreView.setVisibility(View.GONE);

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
        animator.start();

        if (isVisible) {
            buttonLayout.setVisibility(View.VISIBLE);
            pauseView.setVisibility(View.GONE);
            stopView.setVisibility(View.GONE);
        } else {
            buttonLayout.setVisibility(View.GONE);
            pauseView.setVisibility(View.VISIBLE);
            stopView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        if (isMusic)
            player.pause();
        if (gameView != null && !isPaused)
            gameView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMusic)
            player.start();
        if (gameView != null && !isPaused)
            gameView.onResume();
    }

    @Override
    protected void onDestroy() {
        player.release();
        super.onDestroy();
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
    public void onStop(int score) {
        if (isSound)
            soundPool.play(explosion2Id, 1, 1, 0, 0, 1);
        animateTitle(true);
        gameView.setOnClickListener(this);

        int highScore = prefs.getInt(PreferenceUtils.PREF_HIGH_SCORE, 0);
        if (score > highScore) {
            //TODO: awesome high score animation or something
            highScore = score;
            prefs.edit().putInt(PreferenceUtils.PREF_HIGH_SCORE, score).apply();
        }

        highScoreView.setText(String.format(getString(R.string.score_high), highScore));
    }

    @Override
    public void onAsteroidPassed() {

    }

    @Override
    public void onWeaponUpgraded(WeaponData weapon) {
        FontUtils.toast(this, "Weapon Equipped: " + weapon.getName(this));
        if (isSound)
            soundPool.play(upgradeId, 1, 1, 0, 0, 1);
    }

    @Override
    public void onAmmoReplenished() {
        if (isSound)
            soundPool.play(replenishId, 1, 1, 0, 0, 1);
    }

    @Override
    public void onProjectileFired(WeaponData weapon) {
        if (isSound)
            soundPool.play(weapon.soundId, 1, 1, 0, 0, 1);
    }

    @Override
    public void onOutOfAmmo() {
        FontUtils.toast(this, "Out of ammo :(");
        if (isSound) {
            soundPool.play(errorId, 1, 1, 0, 0, 1);
        }
    }

    @Override
    public void onScoreChanged(int score) {
        titleView.setText(String.valueOf(score));
        if (isSound)
            soundPool.play(explosionId, 1, 1, 0, 0, 1);
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
