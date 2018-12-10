package james.asteroid.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
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
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import james.asteroid.R;
import james.asteroid.data.WeaponData;
import james.asteroid.utils.AchievementUtils;
import james.asteroid.utils.FontUtils;
import james.asteroid.utils.ImageUtils;
import james.asteroid.utils.PreferenceUtils;
import james.asteroid.views.GameView;

public class MainActivity extends AppCompatActivity
        implements GameView.GameListener, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView titleView;
    private TextView highScoreView;
    private TextView hintView;
    private ImageView musicView;
    private ImageView soundView;
    private ImageView achievementsView;
    private ImageView rankView;
    private ImageView gamesView;
    private ImageView aboutView;
    private LinearLayout buttonLayout;
    private ImageView pauseView;
    private ImageView stopView;
    private GameView gameView;

    private ValueAnimator animator;
    private String appName, hintStart;

    private SoundPool soundPool;
    private int explosionId;
    private int explosion2Id;
    private int buttonId;
    private int hissId;
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

    private GoogleApiClient apiClient;
    private AchievementUtils achievementUtils;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        titleView = findViewById(R.id.title);
        highScoreView = findViewById(R.id.highScore);
        hintView = findViewById(R.id.hint);
        buttonLayout = findViewById(R.id.buttonLayout);
        musicView = findViewById(R.id.music);
        soundView = findViewById(R.id.sound);
        achievementsView = findViewById(R.id.achievements);
        rankView = findViewById(R.id.rank);
        gamesView = findViewById(R.id.games);
        aboutView = findViewById(R.id.about);
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

        isMusic = prefs.getBoolean(PreferenceUtils.PREF_MUSIC, true);
        musicEnabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_music_enabled), colorAccent, colorPrimary);
        musicDisabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_music_disabled), colorAccent, colorPrimary);
        musicView.setImageBitmap(isMusic ? musicEnabled : musicDisabled);
        musicView.setOnClickListener(view -> {
            isMusic = !isMusic;
            prefs.edit().putBoolean(PreferenceUtils.PREF_MUSIC, isMusic).apply();
            musicView.setImageBitmap(isMusic ? musicEnabled : musicDisabled);

            if (isMusic) player.start();
            else player.pause();

            if (isSound)
                soundPool.play(buttonId, 1, 1, 0, 0, 1);
        });

        isSound = prefs.getBoolean(PreferenceUtils.PREF_SOUND, true);
        soundEnabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_sound_enabled), colorAccent, colorPrimary);
        soundDisabled = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_sound_disabled), colorAccent, colorPrimary);
        soundView.setImageBitmap(isSound ? soundEnabled : soundDisabled);
        soundView.setOnClickListener(view -> {
            isSound = !isSound;
            prefs.edit().putBoolean(PreferenceUtils.PREF_SOUND, isSound).apply();
            soundView.setImageBitmap(isSound ? soundEnabled : soundDisabled);
            if (isSound)
                soundPool.play(buttonId, 1, 1, 0, 0, 1);
        });

        achievementsView.setImageBitmap(ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_achievements), colorAccent, colorPrimary));
        achievementsView.setOnClickListener(view -> {
            startActivityForResult(Games.Achievements.getAchievementsIntent(apiClient), 0);
            if (isSound)
                soundPool.play(buttonId, 1, 1, 0, 0, 1);
        });

        rankView.setImageBitmap(ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_rank), colorAccent, colorPrimary));
        rankView.setOnClickListener(view -> {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(apiClient, getString(R.string.leaderboard_high_score)), 0);
            if (isSound)
                soundPool.play(buttonId, 1, 1, 0, 0, 1);
        });

        gamesView.setImageBitmap(ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_game), colorAccent, colorPrimary));
        gamesView.setOnClickListener(view -> {
            if (apiClient != null) {
                if (apiClient.isConnected()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.title_sign_out)
                            .setMessage(R.string.msg_sign_out)
                            .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                                Games.signOut(apiClient);
                                if (apiClient.isConnected())
                                    apiClient.disconnect();

                                achievementsView.setVisibility(View.GONE);
                                rankView.setVisibility(View.GONE);
                                dialogInterface.dismiss();
                            })
                            .setNegativeButton(android.R.string.no, (dialogInterface, i) -> dialogInterface.dismiss())
                            .create()
                            .show();
                } else apiClient.connect();
            }
        });

        aboutView.setImageBitmap(ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_info), colorAccent, colorPrimary));
        aboutView.setOnClickListener(view -> {
            if (!gameView.isPlaying() && (animator == null || !animator.isStarted())) {
                gameView.setOnClickListener(null);
                //TODO: tutorial screen
                animateTitle(false);
                if (isSound)
                    soundPool.play(hissId, 1, 1, 0, 0, 1);
            }
        });

        play = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_play), colorAccent, colorPrimary);
        pause = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_pause), colorAccent, colorPrimary);
        Bitmap stop = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(this, R.drawable.ic_stop), colorAccent, colorPrimary);

        pauseView.setImageBitmap(pause);
        pauseView.setOnClickListener(view -> {
            isPaused = !isPaused;
            if (isPaused) {
                pauseView.setImageBitmap(play);
                if (!gameView.isTutorial())
                    stopView.setVisibility(View.VISIBLE);
                gameView.onPause();
            } else {
                pauseView.setImageBitmap(pause);
                pauseView.setAlpha(1f);
                stopView.setVisibility(View.GONE);
                gameView.onResume();
            }
        });

        stopView.setImageBitmap(stop);
        stopView.setOnClickListener(view -> {
            if (isPaused) {
                pauseView.setImageBitmap(pause);
                pauseView.setAlpha(1f);
                gameView.onResume();
                isPaused = false;
            }

            onStop(gameView.score);
            gameView.stop();
        });

        int highScore = prefs.getInt(PreferenceUtils.PREF_HIGH_SCORE, 0);
        if (highScore > 0)
            highScoreView.setText(String.format(getString(R.string.score_high), highScore));

        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true);
        if (isMusic)
            player.start();

        handler.postDelayed(hintRunnable, 1000);

        gameView.setListener(this);
        gameView.setOnClickListener(this);
        animateTitle(true);

        apiClient.connect();
    }

    private void animateTitle(final boolean isVisible) {
        highScoreView.setVisibility(View.GONE);

        animator = ValueAnimator.ofFloat(isVisible ? 0 : 1, isVisible ? 1 : 0);
        animator.setDuration(750);
        animator.setStartDelay(500);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            titleView.setText(appName.substring(0, (int) ((float) valueAnimator.getAnimatedValue() * appName.length())));
            hintView.setText(hintStart.substring(0, (int) ((float) valueAnimator.getAnimatedValue() * hintStart.length())));
        });
        animator.start();

        if (isVisible) {
            buttonLayout.setVisibility(View.VISIBLE);
            pauseView.setVisibility(View.GONE);
            stopView.setVisibility(View.GONE);

            if (prefs.getBoolean(PreferenceUtils.PREF_TUTORIAL, true))
                aboutView.setVisibility(View.GONE);
            else aboutView.setVisibility(View.VISIBLE);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Settings.Global.getFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1) != 1) {
                try {
                    ValueAnimator.class.getMethod("setDurationScale", float.class).invoke(null, 1f);
                } catch (Throwable t) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.title_animation_speed)
                            .setMessage(R.string.desc_animation_speed)
                            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                try {
                                    startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                                } catch (Exception ignored) {
                                }
                                dialogInterface.dismiss();
                            })
                            .setNegativeButton(android.R.string.cancel, (dialogInterface, i) ->
                                    dialogInterface.dismiss())
                            .create()
                            .show();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (apiClient != null)
            apiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isConnected())
            apiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        player.release();
        super.onDestroy();
    }

    @Override
    public void onStart(boolean isTutorial) {
        if (achievementUtils != null)
            achievementUtils.onStart(isTutorial);
    }

    @Override
    public void onTutorialFinish() {
        if (achievementUtils != null)
            achievementUtils.onTutorialFinish();

        prefs.edit().putBoolean(PreferenceUtils.PREF_TUTORIAL, false).apply();
    }

    @Override
    public void onStop(int score) {
        animateTitle(true);
        gameView.setOnClickListener(this);

        int highScore = prefs.getInt(PreferenceUtils.PREF_HIGH_SCORE, 0);
        if (score > highScore) {
            //TODO: awesome high score animation or something
            highScore = score;
            prefs.edit().putInt(PreferenceUtils.PREF_HIGH_SCORE, score).apply();

            if (isConnected())
                Games.Leaderboards.submitScore(apiClient, getString(R.string.leaderboard_high_score), highScore);
        }

        highScoreView.setText(String.format(getString(R.string.score_high), highScore));

        if (achievementUtils != null)
            achievementUtils.onStop(score);
    }

    @Override
    public void onAsteroidPassed() {
        if (achievementUtils != null)
            achievementUtils.onAsteroidPassed();
    }

    @Override
    public void onAsteroidCrashed() {
        if (isSound)
            soundPool.play(explosion2Id, 1, 1, 0, 0, 1);
        if (achievementUtils != null)
            achievementUtils.onAsteroidCrashed();
    }

    @Override
    public void onWeaponUpgraded(WeaponData weapon) {
        FontUtils.toast(this, String.format(getString(R.string.msg_weapon_equipped), weapon.getName(this)));
        if (isSound)
            soundPool.play(upgradeId, 1, 1, 0, 0, 1);
        if (achievementUtils != null)
            achievementUtils.onWeaponUpgraded(weapon);
    }

    @Override
    public void onAmmoReplenished() {
        if (isSound)
            soundPool.play(replenishId, 1, 1, 0, 0, 1);
        if (achievementUtils != null)
            achievementUtils.onAmmoReplenished();
    }

    @Override
    public void onProjectileFired(WeaponData weapon) {
        if (isSound)
            soundPool.play(weapon.soundId, 1, 1, 0, 0, 1);
        if (achievementUtils != null)
            achievementUtils.onProjectileFired(weapon);
    }

    @Override
    public void onOutOfAmmo() {
        FontUtils.toast(this, getString(R.string.msg_out_of_ammo));
        if (isSound)
            soundPool.play(errorId, 1, 1, 0, 0, 1);
        if (achievementUtils != null)
            achievementUtils.onOutOfAmmo();
    }

    @Override
    public void onAsteroidHit(int score) {
        titleView.setText(String.valueOf(score));
        if (isSound)
            soundPool.play(explosionId, 1, 1, 0, 0, 1);
        if (achievementUtils != null)
            achievementUtils.onAsteroidHit(score);
    }

    @Override
    public void onClick(View view) {
        if (!gameView.isPlaying() && (animator == null || !animator.isStarted())) {
            gameView.setOnClickListener(null);
            //TODO: tutorial screen
            //if (prefs.getBoolean(PreferenceUtils.PREF_TUTORIAL, true))
            gameView.play();
            animateTitle(false);
            if (isSound)
                soundPool.play(hissId, 1, 1, 0, 0, 1);
        }
    }

    private boolean isConnected() {
        return apiClient != null && apiClient.isConnected();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        achievementsView.setVisibility(View.VISIBLE);
        rankView.setVisibility(View.VISIBLE);
        gamesView.setVisibility(View.VISIBLE);
        achievementUtils = new AchievementUtils(this, apiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.connect();
        gamesView.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        achievementsView.setVisibility(View.GONE);
        rankView.setVisibility(View.GONE);
        gamesView.setVisibility(View.GONE);
        BaseGameUtils.resolveConnectionFailure(this, apiClient, connectionResult, 1801, R.string.msg_sign_in_error);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1801) {
            if (resultCode == RESULT_OK)
                apiClient.connect();
            else {
                Toast.makeText(this, getString(R.string.msg_sign_in_error), Toast.LENGTH_SHORT).show();
                gamesView.setVisibility(View.VISIBLE);
            }
        }
    }
}
