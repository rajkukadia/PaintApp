package raj.kukadia.paintapp.paint.uicontroller.activities;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.uicontroller.fragments.ColorPaletteFragment;
import raj.kukadia.paintapp.paint.uicontroller.fragments.PaintFragment;
import raj.kukadia.paintapp.paint.uicontroller.observers.PaintActivityObserver;

public class PaintActivity extends AppCompatActivity implements ColorPaletteFragment.ColorSelectionListener, SensorEventListener {

    private PaintFragment paintFragment;
    private ColorPaletteFragment colorPaletteFragment;
    private FragmentTransaction fragmentTransaction;
    public  Boolean colors_screen;
    private Integer selectedColor = null;
    private static final String PAINT = "PAINT_FRAGMENT";
    private static final String COLOR = "COLOR";
    private static final String ERASER_SET = "ERASER_SET";
    private static final String ERASER_NOT_SET = "ERASER_NOT_SET";
    private Button undo;
    private Button refresh;
    private Button redo;
    private Button eraser;
    private Button color;
    private boolean alternateShake = true;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1500;

    //First call back called by the app
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //the super call helps to retrieve the FragmentManager state
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint_activity);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        colorPaletteFragment = (ColorPaletteFragment) fragmentManager.findFragmentByTag(COLOR);
        if(colorPaletteFragment!=null){
            colors_screen = true;
            fragmentTransaction.attach(colorPaletteFragment);
            fragmentTransaction.commitNow();
        }else {
            colors_screen = false;
            paintFragment = (PaintFragment) fragmentManager.findFragmentByTag(PAINT);
            if (paintFragment == null) {
                //this happens only once as across all configuration changes paintFragment survives and no new instance has been created due to the retain instance method in the fragment
                paintFragment = PaintFragment.newInstance();
                fragmentTransaction.add(R.id.fragment_container, paintFragment, PAINT);
            }else{
                fragmentTransaction.attach(paintFragment);
            }
            fragmentTransaction.commitNow();
        }

        //adding lifecycle observer
        getLifecycle().addObserver(new PaintActivityObserver(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeButtons();
    }

    public void initializeButtons(){
        undo =  findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintFragment.undo();
            }
        });

        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintFragment.clear();
            }
        });

        redo = findViewById(R.id.redo);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintFragment.redo();
            }
        });


        eraser = findViewById(R.id.eraser);
        color = findViewById(R.id.colour);
        if(paintFragment.eraserState().equals(ERASER_SET)){
            color.setVisibility(View.INVISIBLE);
            eraser.setBackgroundResource(R.drawable.brush);
        }else{
            color.setVisibility(View.VISIBLE);
            eraser.setBackgroundResource(R.drawable.eraser);
        }
        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(paintFragment.eraserState().equals(ERASER_SET)){
                    paintFragment.setEraser(false);
                    color.setVisibility(View.VISIBLE);
                    v.setBackgroundResource(R.drawable.eraser);
                }else{
                    paintFragment.setEraser(true);
                    color.setVisibility(View.INVISIBLE);
                    v.setBackgroundResource(R.drawable.brush);
                }
            }
        });

        color.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if(!colors_screen) {
                    setButtonVisibilityForPalette(false);

                }else{
                    setButtonVisibilityForPalette(true);
                }
                switchFragment(colors_screen);
            }
        });

    }


    private void setButtonVisibilityForPalette(boolean isVisible){
        if(!isVisible){
            eraser.setVisibility(View.INVISIBLE);
            undo.setVisibility(View.INVISIBLE);
            redo.setVisibility(View.INVISIBLE);
            refresh.setVisibility(View.INVISIBLE);
            color.setBackgroundResource(R.drawable.select);
        }else{
            eraser.setVisibility(View.VISIBLE);
            undo.setVisibility(View.VISIBLE);
            redo.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.VISIBLE);
            color.setBackgroundResource(R.drawable.custom_button);
        }
    }

    //Method to switch between paint view and color palette view
    public void switchFragment(boolean colors_screen){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        paintFragment = (PaintFragment) fragmentManager.findFragmentByTag(PAINT);
        colorPaletteFragment = (ColorPaletteFragment) fragmentManager.findFragmentByTag(COLOR);
        if(!colors_screen){
            if(colorPaletteFragment == null){
                colorPaletteFragment = ColorPaletteFragment.newInstance();
                colorPaletteFragment.registerListener(this);
                fragmentTransaction.detach(paintFragment);
                fragmentTransaction.add(R.id.fragment_container, colorPaletteFragment, COLOR);
            }else{
                fragmentTransaction.detach(paintFragment);
                fragmentTransaction.attach(colorPaletteFragment);
            }
        }else{
            fragmentTransaction.remove(colorPaletteFragment);
            if(this.selectedColor!=null) {
                Bundle bundle = new Bundle();
                bundle.putInt("color", selectedColor);
                paintFragment.setArguments(bundle);
            }
           fragmentTransaction.attach(paintFragment);
        }
        fragmentTransaction.commitNow();
        this.colors_screen = !colors_screen;
    }

    @Override
    public void onColorSelection(int color) {
        this.selectedColor = color;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long currTIme = System.currentTimeMillis();
            if(currTIme- lastUpdate >100){
                long diffTime = currTIme  - lastUpdate;
                lastUpdate = currTIme;
                float speed = Math.abs(x+y+z - last_x - last_y - last_z)/diffTime *10000;
                if (speed>SHAKE_THRESHOLD){
                    if(!colors_screen&& alternateShake)paintFragment.undo();
                }
                alternateShake = !alternateShake;
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
