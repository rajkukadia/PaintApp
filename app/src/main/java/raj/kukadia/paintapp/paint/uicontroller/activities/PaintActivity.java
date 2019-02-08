package raj.kukadia.paintapp.paint.uicontroller.activities;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.uicontroller.fragments.ColorPaletteFragment;
import raj.kukadia.paintapp.paint.uicontroller.fragments.PaintFragment;
import raj.kukadia.paintapp.paint.uicontroller.observers.PaintActivityObserver;

public class PaintActivity extends AppCompatActivity{

    private PaintFragment paintFragment;
    private ColorPaletteFragment colorPaletteFragment;
    private FragmentTransaction fragmentTransaction;
    public  Boolean colors_screen;
    private static final String PAINT = "PAINT_FRAGMENT";
    private static final String COLOR = "COLOR";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint_activity);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        colorPaletteFragment = (ColorPaletteFragment) fragmentManager.findFragmentByTag(COLOR);
        if(colorPaletteFragment!=null){
            colors_screen = true;
            fragmentTransaction.attach(colorPaletteFragment);
            fragmentTransaction.commit();
        }else {
            colors_screen = false;
            paintFragment = (PaintFragment) fragmentManager.findFragmentByTag(PAINT);
            if (paintFragment == null) {
                paintFragment = PaintFragment.newInstance();
                fragmentTransaction.add(R.id.fragment_container, paintFragment, PAINT);
            }else{
                fragmentTransaction.attach(paintFragment);
            }
            fragmentTransaction.commit();
        }
        getLifecycle().addObserver(new PaintActivityObserver());
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeButtons();
    }

    public void initializeButtons(){
        final Button undo =  findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintFragment.undo();
            }
        });

        final Button refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintFragment.clear();
            }
        });

        final Button redo = findViewById(R.id.redo);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintFragment.redo();
            }
        });


        final Button color = findViewById(R.id.colour);
        color.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if(!colors_screen) {
                    undo.setVisibility(View.INVISIBLE);
                    redo.setVisibility(View.INVISIBLE);
                    refresh.setVisibility(View.INVISIBLE);
                    color.setBackgroundResource(R.drawable.select);
                }else{
                    undo.setVisibility(View.VISIBLE);
                    redo.setVisibility(View.VISIBLE);
                    refresh.setVisibility(View.VISIBLE);
                    color.setBackgroundResource(R.drawable.custom_button);
                }
                int color  = Color.RED;
                if(colorPaletteFragment!=null) color = colorPaletteFragment.color;
                switchFragment(colors_screen, color);
            }
        });


        if(colors_screen){
            undo.setVisibility(View.INVISIBLE);
            redo.setVisibility(View.INVISIBLE);
            refresh.setVisibility(View.INVISIBLE);
            color.setBackgroundResource(R.drawable.select);
        }else{
            undo.setVisibility(View.VISIBLE);
            redo.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.VISIBLE);
            color.setBackgroundResource(R.drawable.custom_button);
        }

    }

    //Method to switch between paint view and color palette view
    public void switchFragment(boolean colors_screen, int color){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        paintFragment = (PaintFragment) fragmentManager.findFragmentByTag(PAINT);
        colorPaletteFragment = (ColorPaletteFragment) fragmentManager.findFragmentByTag(COLOR);
        if(!colors_screen){
            if(colorPaletteFragment == null){
                colorPaletteFragment = ColorPaletteFragment.newInstance();
                fragmentTransaction.detach(paintFragment);
                fragmentTransaction.add(R.id.fragment_container, colorPaletteFragment, COLOR);
            }else{
                fragmentTransaction.detach(paintFragment);
                fragmentTransaction.attach(colorPaletteFragment);
            }
        }else{
           fragmentTransaction.remove(colorPaletteFragment);
           Bundle bundle = new Bundle();
           bundle.putInt("color", color);
           paintFragment.setArguments(bundle);
           fragmentTransaction.attach(paintFragment);
        }
        fragmentTransaction.commit();
        this.colors_screen = !colors_screen;

    }

}
