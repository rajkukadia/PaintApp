package raj.kukadia.paintapp.paint.uicontroller.observers;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by RAJ on 2/5/2019.
 */

public class PaintActivityObserver implements LifecycleObserver{

    private static final String LOG_TAG = PaintActivityObserver.class.getName();
    private SensorManager sensorManager;
    private Sensor sensor;
    private Context context;

    public PaintActivityObserver(Context context){
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void registerSensor(){
        sensorManager.registerListener((SensorEventListener) context, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void unregisterSensor(){
        sensorManager.unregisterListener((SensorEventListener) context);
    }
}
