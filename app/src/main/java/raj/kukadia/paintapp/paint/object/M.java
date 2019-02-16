package raj.kukadia.paintapp.paint.object;

import android.content.Context;
import android.widget.Toast;

public class M{

    public static void displayLong(String message, Context context){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void displayShort(String message, Context context){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
