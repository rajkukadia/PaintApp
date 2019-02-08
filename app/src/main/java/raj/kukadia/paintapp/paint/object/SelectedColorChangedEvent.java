package raj.kukadia.paintapp.paint.object;

/**
 * Created by RAJ on 2/7/2019.
 */

import android.support.annotation.ColorInt;

public class SelectedColorChangedEvent {

    private @ColorInt
    int selectedColor;

    public SelectedColorChangedEvent(@ColorInt int color) {
        selectedColor = color;
    }

    public @ColorInt int getSelectedColor() {
        return selectedColor;
    }
}