package raj.kukadia.paintapp.paint.uicontroller.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.view.PaintView;
import raj.kukadia.paintapp.paint.viewmodel.PaintViewModel;


public class PaintFragment extends Fragment {

    private PaintView paintView;
    View rootView;
    int currId;
    public PaintFragment() {
    }

    public static PaintFragment newInstance() {
        return new PaintFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

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

        paintViewModel.getCanvas(displayMetrics, currId).observe(this, new Observer<Map<Integer, List<Object>>>() {
            @Override
            public void onChanged(@Nullable Map<Integer, List<Object>> integerListMap) {
                List<Object> list = integerListMap.get(currId);
                paintView.init(list);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getArguments()!=null){
            paintView.changeColor(getArguments().getInt("color"));
        }
    }

    public void clear(){
        paintView.clear();
    }

    public void undo(){
        paintView.undo();
    }

    public void changeColor(int color){
        paintView.changeColor(color);
    }

    public void redo(){paintView.redo();}


}
