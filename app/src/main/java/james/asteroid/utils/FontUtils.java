package james.asteroid.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Toast;

public class FontUtils {

    public static Typeface getTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "BlackOpsOne-Regular.ttf");
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
