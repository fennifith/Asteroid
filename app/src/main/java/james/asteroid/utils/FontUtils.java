package james.asteroid.utils;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils {

    public static Typeface getTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "BlackOpsOne-Regular.ttf");
    }

}
