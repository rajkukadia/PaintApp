package raj.kukadia.paintapp.paint.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.object.ColorUtil;
import raj.kukadia.paintapp.paint.object.SelectedColorChangedEvent;

/**
 * Created by RAJ on 2/7/2019.
 *
 * Inspired from various Color Picker libraries on https://android-arsenal.com/tag/18
 *
 * Referred and studied in detail from:
 * https://github.com/the-blue-alliance/spectrum
 * https://github.com/duanhong169/ColorPicker
 */

public class ColorItem extends FrameLayout implements View.OnClickListener {

    private boolean isSelected = false;
    private  int outLineWidth = 0;
    private  @ColorInt int color;
    private ImageView itemCheckMark;
    private EventBus eventBus;

    public ColorItem(@NonNull Context context) {
        super(context);
    }

    public ColorItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ColorItem(Context context, @ColorInt int color, boolean isSelected, EventBus eventBus){
        super(context);
        this.color = color;
        this.isSelected = isSelected;
        this.eventBus = eventBus;
        init();
        setChecked(isSelected);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setOutLineWidth(int width){
        outLineWidth = width;
        updateDrawables();
    }

    private Drawable createBackgroundDrawable(){
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        if(outLineWidth!=0) gradientDrawable.setStroke(outLineWidth, ColorUtil.isColorDark(color)?Color.WHITE:Color.BLACK);
        gradientDrawable.setColor(color);
        return gradientDrawable;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Drawable createForegroundDrawable(){
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(Color.BLACK);
        return new RippleDrawable(ColorStateList.valueOf(ColorUtil.getRippleColor(color)), null, gradientDrawable);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateDrawables(){
        setForeground(createForegroundDrawable());
        setBackground(createBackgroundDrawable());
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(){
        updateDrawables();
        eventBus.register(this);
        setOnClickListener(this);
        LayoutInflater.from(getContext()).inflate(R.layout.color_item, this, true);
        itemCheckMark = (ImageView)findViewById(R.id.selected_checkmark);
        itemCheckMark.setColorFilter(ColorUtil.isColorDark(color)?Color.WHITE:Color.BLACK);
    }

    public void setChecked(boolean checked){
        boolean prevChecked = isSelected;
        isSelected = checked;
        if(!prevChecked && isSelected){
            setItemCheckMarkAttr(0.0f);
            itemCheckMark.setVisibility(View.VISIBLE);
            itemCheckMark.animate()
                    .alpha(1.0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(250)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setItemCheckMarkAttr(1.0f);
                        }
                    }).start();
        }else if(prevChecked && !isSelected){
            setItemCheckMarkAttr(0.0f);
            itemCheckMark.setVisibility(View.VISIBLE);
            itemCheckMark.animate()
                    .alpha(0.0f)
                    .scaleX(.0f)
                    .scaleY(0.0f)
                    .setDuration(250)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            itemCheckMark.setVisibility(View.INVISIBLE);
                            setItemCheckMarkAttr(0.0f);
                        }
                    }).start();
        }else{
            updateCheckMarkVisibility();
        }

    }

    private void updateCheckMarkVisibility(){
        itemCheckMark.setVisibility(isSelected?View.VISIBLE:View.INVISIBLE);
        setItemCheckMarkAttr(1.0f);
    }

    private void setItemCheckMarkAttr(float val){
        itemCheckMark.setAlpha(val);
        itemCheckMark.setScaleX(val);
        itemCheckMark.setScaleY(val);
    }

    @Subscribe
    public void onSelectedColorChanged(SelectedColorChangedEvent event) {
        setChecked(event.getSelectedColor() == color);
    }

    @Override
    public void onClick(View v) {
        eventBus.post(new SelectedColorChangedEvent(color));
    }
}
