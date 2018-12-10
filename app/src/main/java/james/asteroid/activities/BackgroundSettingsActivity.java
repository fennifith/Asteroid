package james.asteroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import james.asteroid.R;
import james.asteroid.services.BackgroundService;

public class BackgroundSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_settings);

        AppCompatSpinner speed = findViewById(R.id.speed);
        ArrayAdapter<CharSequence> speedsAdapter = ArrayAdapter.createFromResource(this, R.array.pref_speeds, R.layout.support_simple_spinner_dropdown_item);
        speedsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        speed.setAdapter(speedsAdapter);
        speed.setSelection(BackgroundService.getSpeed(this) - 1);
        speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                BackgroundService.setSpeed(BackgroundSettingsActivity.this, i + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        SwitchCompat asteroids = findViewById(R.id.asteroids);
        asteroids.setChecked(BackgroundService.isAsteroids(this));
        asteroids.setOnCheckedChangeListener((compoundButton, b) ->
                BackgroundService.setAsteroids(BackgroundSettingsActivity.this, b));

        AppCompatSpinner asteroidSpeed = findViewById(R.id.asteroidSpeed);
        asteroidSpeed.setAdapter(speedsAdapter);
        asteroidSpeed.setSelection(BackgroundService.getAsteroidSpeed(this) - 1);
        asteroidSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                BackgroundService.setAsteroidSpeed(BackgroundSettingsActivity.this, i + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        AppCompatSpinner asteroidInterval = findViewById(R.id.asteroidInterval);
        ArrayAdapter<CharSequence> intervalsAdapter = ArrayAdapter.createFromResource(this, R.array.pref_intervals, R.layout.support_simple_spinner_dropdown_item);
        intervalsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        asteroidInterval.setAdapter(intervalsAdapter);
        asteroidInterval.setSelection((BackgroundService.getAsteroidInterval(this) / 1000) - 1);
        asteroidInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                BackgroundService.setAsteroidInterval(BackgroundSettingsActivity.this, (i + 1) * 1000);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
}
