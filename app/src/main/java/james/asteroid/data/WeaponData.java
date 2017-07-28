package james.asteroid.data;

import android.content.Context;
import android.media.SoundPool;
import android.preference.PreferenceManager;

import java.util.List;

import james.asteroid.R;
import james.asteroid.utils.PreferenceUtils;

public class WeaponData {

    public static final WeaponData[] WEAPONS = new WeaponData[]{
            new WeaponData(R.string.weapon_pellet, R.raw.laser, 1, 1),
            new WeaponData(R.string.weapon_watermelon, R.raw.laser, 2, 1),
            new WeaponData(R.string.weapon_grape, R.raw.laser, 1, 3),
            new WeaponData(R.string.weapon_watermelons, R.raw.laser, 2, 3),
            new WeaponData(R.string.weapon_pellets, R.raw.laser, 1, 5),
            new WeaponData(R.string.weapon_laser, R.raw.laser, 3, 5),
            new WeaponData(R.string.weapon_seed, R.raw.laser, 1, 10),
            new WeaponData(R.string.weapon_brick, R.raw.laser, 5, 3),
            new WeaponData(R.string.weapon_lasers, R.raw.laser, 3, 8),
            new WeaponData(R.string.weapon_particle, R.raw.laser, 10, 1),
            new WeaponData(R.string.weapon_particles, R.raw.laser, 10, 3),
            new WeaponData(R.string.weapon_particless, R.raw.laser, 10, 5)
    };

    private int nameRes;
    private int strength;
    private int spray;
    private int soundRes;
    public int soundId;

    public WeaponData(int nameRes, int soundRes, int strength, int spray) {
        this.nameRes = nameRes;
        this.soundRes = soundRes;
        this.strength = strength;
        this.spray = spray;
    }

    public String getName(Context context) {
        return context.getString(nameRes);
    }

    public boolean isEquipped(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceUtils.PREF_WEAPON, WEAPONS[0].getName(context)).equals(getName(context));
    }

    public void setEquipped(Context context) {
        if (isEnabled(context))
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PreferenceUtils.PREF_WEAPON, getName(context)).apply();
    }

    public boolean isEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_WEAPON_ENABLED + getName(context), equals(WEAPONS[0]));
    }

    public void setEnabled(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PreferenceUtils.PREF_WEAPON_ENABLED + getName(context), true).apply();
    }

    public void fire(List<ProjectileData> projectiles, float x, float y) {
        for (int i = 0; i < spray; i++) {
            float xDiff = (((float) (i + 1) / (spray + 1)) - 0.5f) / 2;
            for (int i2 = 0; i2 < strength; i2++) {
                projectiles.add(new ProjectileData(x, y + (3 * i2), xDiff * 0.008f, 4));
            }
        }
    }

    private void loadSoundRes(Context context, SoundPool soundPool) {
        soundId = soundPool.load(context, soundRes, 1);
    }

    public static WeaponData getEquippedWeapon(Context context) {
        for (WeaponData weapon : WEAPONS) {
            if (weapon.isEquipped(context))
                return weapon;
        }

        return WEAPONS[0];
    }

    public static void loadSounds(Context context, SoundPool soundPool) {
        for (WeaponData weapon : WEAPONS) {
            weapon.loadSoundRes(context, soundPool);
        }
    }

}
