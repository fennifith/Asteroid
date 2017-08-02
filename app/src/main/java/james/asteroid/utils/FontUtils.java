package james.asteroid.utils;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import james.asteroid.R;

public class FontUtils {

    public static Typeface getTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "BlackOpsOne-Regular.ttf");
    }

    public static void toast(Context context, String message) {
        TextView textView = new TextView(context);
        textView.setText(message);
        textView.setTypeface(getTypeface(context));
        textView.getPaint().setShader(new LinearGradient(
                0, 0, 0,
                textView.getLineHeight(),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimary),
                Shader.TileMode.REPEAT
        ));

        Toast toast = new Toast(context);
        toast.setView(textView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

}
