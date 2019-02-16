package raj.kukadia.paintapp.paint.uicontroller.activities;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.util.ArrayList;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.object.M;
import raj.kukadia.paintapp.paint.uicontroller.fragments.ColorPaletteFragment;
import raj.kukadia.paintapp.paint.uicontroller.fragments.PaintFragment;
import raj.kukadia.paintapp.paint.uicontroller.fragments.WarningFragment;
import raj.kukadia.paintapp.paint.uicontroller.observers.PaintActivityObserver;

public class PaintActivity extends AppCompatActivity implements ColorPaletteFragment.ColorSelectionListener, SensorEventListener, WarningFragment.WarningDialogListener, RecognitionListener {

    private PaintFragment paintFragment;
    private ColorPaletteFragment colorPaletteFragment;
    private FragmentTransaction fragmentTransaction;
    public  Boolean colors_screen;
    private Integer selectedColor = null;
    private static final String PAINT = "PAINT_FRAGMENT";
    private static final String COLOR = "COLOR";
    private static final String ERASER_SET = "ERASER_SET";
    private static final String ERASER_NOT_SET = "ERASER_NOT_SET";
    private static final int REQ_CODE = 100;
    private static final int REQ_CODE2 = 101;
    private Button undo;
    private Button refresh;
    private Button redo;
    private Button save;
    private Button eraser;
    private Button color;
    private boolean alternateShake = true;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1500;
    private SpeechRecognizer speechRecognizer = null;

    //First call back called by the app
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //the super call helps to retrieve the FragmentManager state
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
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

    private void startListening(){
        if(permissionCheckForSpeech()) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(this);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            speechRecognizer.startListening(intent);
        }
    }


    public void initializeButtons(){
        undo =  findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintFragment.undo();
            }
        });
        save = findViewById(R.id.save);
        refresh = findViewById(R.id.refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
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

        save.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(isExternalStorageWritable()&&(permissionCheckForSave()))paintFragment.saveImage();
            }
        });
        eraser = findViewById(R.id.eraser);
        color = findViewById(R.id.colour);
        if(!colors_screen) setButtonVisibilityForEraser();
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

        if(colors_screen) {
            setButtonVisibilityForPalette(false);
        }else{
            setButtonVisibilityForPalette(true);
        }
    }

    private boolean permissionCheckForSpeech(){
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
          if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
              return true;
          }else{
              ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_CODE2);
              return false;
          }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean permissionCheckForSave(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                paintFragment.saveImage();
            }
        }else if(requestCode == REQ_CODE2){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            }
        }
    }

    private void setButtonVisibilityForEraser(){
        if (paintFragment.eraserState().equals(ERASER_SET)) {
            color.setVisibility(View.INVISIBLE);
            eraser.setBackgroundResource(R.drawable.brush);
        } else {
            color.setVisibility(View.VISIBLE);
            eraser.setBackgroundResource(R.drawable.eraser);
        }
    }
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void setButtonVisibilityForPalette(boolean isVisible){
        if(!isVisible){
            eraser.setVisibility(View.INVISIBLE);
            undo.setVisibility(View.INVISIBLE);
            redo.setVisibility(View.INVISIBLE);
            refresh.setVisibility(View.INVISIBLE);
            save.setVisibility(View.INVISIBLE);
            color.setBackgroundResource(R.drawable.select);
        }else{
            eraser.setVisibility(View.VISIBLE);
            undo.setVisibility(View.VISIBLE);
            redo.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
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
    protected void onStop() {
        super.onStop();
        if(speechRecognizer!=null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
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
                   if(speechRecognizer==null){
                       startListening();
                   }
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onBackPressed() {
       //super.onBackPressed();
        WarningFragment warningFragment = new WarningFragment();
        warningFragment.show(getSupportFragmentManager(), "warning");
    }

    @Override
    public void onPositiveListener(DialogFragment dialog) {
        dialog.dismiss();
        finish();
    }

    @Override
    public void onNegativeListener(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }


    @Override
    public void onError(int error) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResults(Bundle results) {
       ArrayList<String> list =  results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
       if(paintFragment == null) return;
       for(String s:list){
           Log.d("heared", s);
           if(s.equalsIgnoreCase("save")){
                if(isExternalStorageWritable()&&(permissionCheckForSave()))paintFragment.saveImage();
                break;
          }else if(s.equalsIgnoreCase("undo")){
               paintFragment.undo();
               break;
           }else if(s.equalsIgnoreCase("redo")||s.equalsIgnoreCase("are you do")){
               paintFragment.redo();
               break;
           }else if(s.equalsIgnoreCase("red")){
               paintFragment.changeColor(Color.RED);
               M.displayShort("Color changed to Red", this);
               break;
           }else if(s.equalsIgnoreCase("blue")){
               if(paintFragment.eraserState().equals(ERASER_NOT_SET)) {
                   paintFragment.changeColor(Color.BLUE);
                   M.displayShort("Color changed to Blue", this);
               }else{
                   M.displayShort("You are in eraser mode", this);
               }
               break;
           }else if(s.equalsIgnoreCase("black")){
            if(paintFragment.eraserState().equals(ERASER_NOT_SET)){
               paintFragment.changeColor(Color.BLACK);
                M.displayShort("Color changed to Black", this);
            }else{
               M.displayShort("You are in eraser mode", this);
           }
               break;
           }else if(s.equalsIgnoreCase("green")){
               if(paintFragment.eraserState().equals(ERASER_NOT_SET)){
               paintFragment.changeColor(Color.GREEN);
               M.displayShort("Color changed to Green", this);
           }else{
               M.displayShort("You are in eraser mode", this);
           }

               break;
           }else if(s.equalsIgnoreCase("eraser")){
               M.displayShort("Switched to Eraser", this);
               handleEraserState();
               break;
           }else if(s.contains("draw circle of")){
               try{
               String[] arr = s.split("of");
               String s1 = arr[1];
               s1 = s1.trim();
               if(s1.equals("hundred")) s1 = "100";
               paintFragment.drawCircle(Float.valueOf(s1));
               }catch (NumberFormatException e){
                   M.displayShort("Number incorrect", this);
               }catch (ArrayIndexOutOfBoundsException e){
                   M.displayShort("Oops, try again!", this);
               }
               break;
           }else if(s.contains("draw rectangle of")){
               String[] arr = s.split("of");
               String s1 = arr[1];
               s1 = s1.trim();
               arr = s1.split("\\+");
               float a = Float.parseFloat(arr[0].trim());
               float b = Float.parseFloat(arr[1].trim());
               //paintFragment.drawRect(a, b);
               break;
           }else if(s.contains("brush")){
             handleEraserState();
             M.displayShort("Switched to brush", this);
             break;
           }else if(s.contains("stroke")){
               try{
                   String[] arr = s.split("of");
                   String s1 = arr[1];
                   s1 = s1.trim();
                   paintFragment.setStrokeWidth(Float.valueOf(s1));
               }catch (NumberFormatException e){
                   M.displayShort("Number incorrect", this);
               }catch (ArrayIndexOutOfBoundsException e){
                   M.displayShort("Oops, try again!", this);
               }
               break;
           }
       }

       speechRecognizer.stopListening();
       speechRecognizer.destroy();
       speechRecognizer = null;
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    private void handleEraserState(){
        if(paintFragment.eraserState().equals(ERASER_SET)){
            paintFragment.setEraser(false);
            color.setVisibility(View.VISIBLE);
            eraser.setBackgroundResource(R.drawable.eraser);
        }else{
            paintFragment.setEraser(true);
            color.setVisibility(View.INVISIBLE);
            eraser.setBackgroundResource(R.drawable.brush);
        }
    }
}
