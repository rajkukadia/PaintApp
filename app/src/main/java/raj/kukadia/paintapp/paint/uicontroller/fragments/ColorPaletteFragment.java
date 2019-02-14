package raj.kukadia.paintapp.paint.uicontroller.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.view.ColorPalette;


public class ColorPaletteFragment extends Fragment{

    private View rootView;
    private ColorPalette colorPalette;
    private FragmentTransaction fragmentTransaction;
    public Integer color = null;
    private ColorSelectionListener listener;

    public ColorPaletteFragment() {
    }


    public static ColorPaletteFragment newInstance() {
        ColorPaletteFragment fragment = new ColorPaletteFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_color_palette, container, false);
        colorPalette   =   rootView.findViewById(R.id.color_palette);
        colorPalette.setOnColorSelectedListener(new ColorPalette.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selected_color) {
                color = selected_color;
                if(listener!=null)listener.onColorSelection(color);
            }
        });
        return rootView;
    }


    public void registerListener(ColorSelectionListener listener){
        this.listener = listener;
    }

    public interface ColorSelectionListener{
        public void onColorSelection(int color);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
}
