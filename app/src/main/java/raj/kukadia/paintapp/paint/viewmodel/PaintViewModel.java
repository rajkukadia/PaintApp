package raj.kukadia.paintapp.paint.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import raj.kukadia.paintapp.paint.object.TouchPath;

/**
 * Created by RAJ on 2/2/2019.
 *
 * ViewModel class for overcoming configuration changes
 */

public class PaintViewModel extends AndroidViewModel{

    private MutableLiveData<Map<Integer, List<Object>>> canvasMutableLiveData;
    private MutableLiveData<Integer> currId = new MutableLiveData<>();
    private Canvas canvas;
    private Bitmap bitmap;
    private DisplayMetrics displayMetrics;
    private List<TouchPath> paths;
    private Map<Integer, List<Object>> map;
    public PaintViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Map<Integer,List<Object>>> getCanvas(DisplayMetrics metrics, int id){
        if (this.canvasMutableLiveData== null || id>=map.size()) {
            this.canvasMutableLiveData = new MutableLiveData<Map<Integer, List<Object>>>();
            displayMetrics = metrics;
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            List<Object> list = new ArrayList<>();
            list.add(canvas);
            list.add(bitmap);
            paths = new ArrayList<>();
            list.add(paths);
            list.add(Color.RED);
            if (map == null) map = new HashMap<>();
            map.put(map.size(), list);
            this.canvasMutableLiveData.postValue(map);
        }

        return canvasMutableLiveData;
    }

    public LiveData<Integer> getCurrentId(){
        if(currId == null)currId.postValue(0);
        return this.currId;
    }
}
