package raj.kukadia.paintapp.paint.uicontroller.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.object.M;
import raj.kukadia.paintapp.paint.view.PaintView;
import raj.kukadia.paintapp.paint.viewmodel.PaintViewModel;


public class PaintFragment extends Fragment {

    private PaintView paintView;
    View rootView;
    int currId;
    private static final String ERASER_NOT_SET = "ERASER_NOT_SET";
    public PaintFragment() {
    }

    public static PaintFragment newInstance() {
        return new PaintFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_paint, container, false);
        paintView = (PaintView) rootView.findViewById(R.id.paintview);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        PaintViewModel paintViewModel = ViewModelProviders.of(this).get(PaintViewModel.class);
        paintViewModel.getCurrentId().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                currId = integer;
            }
        });

        paintViewModel.getCanvas(currId).observe(this, new Observer<Map<Integer, List<Object>>>() {
            @Override
            public void onChanged(@Nullable Map<Integer, List<Object>> integerListMap) {
                List<Object> list = integerListMap.get(currId);
                paintView.init(list, displayMetrics);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getArguments()!=null && eraserState().equals(ERASER_NOT_SET)){
            paintView.changeColor(getArguments().getInt("color"));
        }
    }

    public void clear(){
        paintView.clear();
    }

    public void undo(){
        paintView.undo();
    }

    public void changeColor(int color)
    {
        paintView.changeColor(color);
    }

    public void setEraser(boolean set){
        paintView.setEraser(set);
    }

    public String eraserState(){
        return paintView.eraserState();
    }

    public void redo(){paintView.redo();}

    public void saveImage(){
        Bitmap bitmap = paintView.getBitmap();
        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "Image1" , "Saved Image1");
        M.displayLong("saved successfully", getActivity());
    }

    public void drawCircle(float radius){
        paintView.wantToDrawCircle(radius);
    }

    public void drawRect(float x, float y){
        paintView.wantToDrawRect(x, y);
    }

    public void setStrokeWidth(float w){
        paintView.setStrokewidth(w);

    }
}
