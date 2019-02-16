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
    private List<TouchPath> paths;
    private Bitmap bitmap;
    private Map<Integer, List<Object>> map;
    public PaintViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Map<Integer,List<Object>>> getCanvas(int id){
        if (this.canvasMutableLiveData== null || id>=map.size()) {
            this.canvasMutableLiveData = new MutableLiveData<Map<Integer, List<Object>>>();
            List<Object> list = new ArrayList<>();
            paths = new ArrayList<>();
            list.add(paths);
            list.add(Color.RED);
            list.add(Boolean.FALSE);
            list.add(Color.RED);
            list.add(20);
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
