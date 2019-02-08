package raj.kukadia.paintapp.paint.uicontroller.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.view.ColorPalette;


public class ColorPaletteFragment extends Fragment implements ColorPalette.OnColorSelectedListener {

    private View rootView;
    private ColorPalette colorPalette;
    private FragmentTransaction fragmentTransaction;
    public int color = Color.BLACK;

    public ColorPaletteFragment() {
    }


    public static ColorPaletteFragment newInstance() {
        ColorPaletteFragment fragment = new ColorPaletteFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_color_palette, container, false);
        colorPalette   =   rootView.findViewById(R.id.color_palette);
        colorPalette.setOnColorSelectedListener(this);
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onColorSelected(int color) {
       this.color = color;
    }
}
