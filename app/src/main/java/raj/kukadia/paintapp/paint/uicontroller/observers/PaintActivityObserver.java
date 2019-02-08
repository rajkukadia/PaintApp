package raj.kukadia.paintapp.paint.uicontroller.observers;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.uicontroller.fragments.PaintFragment;

/**
 * Created by RAJ on 2/5/2019.
 */

public class PaintActivityObserver implements LifecycleObserver{

    private static final String LOG_TAG = PaintActivityObserver.class.getName();
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(){
        Log.d(LOG_TAG, "resumed to observe");
    }

}
