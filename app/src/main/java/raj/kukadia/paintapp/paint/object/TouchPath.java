package raj.kukadia.paintapp.paint.object;

import android.graphics.Path;

/**
 * Created by RAJ on 2/2/2019.
 */

public class TouchPath {

    public int colour;
    public int strokewidth;
    public Path path;

    public TouchPath(int colour, int strokewidth, Path path){
        this.strokewidth = strokewidth;
        this.path = path;
        this.colour = colour;
    }
}
