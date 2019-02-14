package raj.kukadia.paintapp.paint.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

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

public class ColorPalette extends LinearLayout {

    private static final int DEFAULT_COLUMN_COUNT = 4;

    private int colorItemDimension;
    private int colorItemMargin;
    private @ColorInt int[] colors;
    private @ColorInt int selectedColor;

    private OnColorSelectedListener listener;
    private boolean autoPadding = false;
    private boolean hasFixedColumnCount = false;
    private int fixedColumnCount = -1;
    private int outlineWidth = 0;
    private int computedVerticalPadding = 0;
    private int originalPaddingTop = 0;
    private int originalPaddingBottom = 0;
    private boolean setPaddingCalledInternally = false;

    private int numColumns = 2;
    private int oldNumColumns = -1;
    private boolean viewInitialized = false;

    private EventBus eventBus;

    private List<ColorItem> colorItemList = new ArrayList<>();

    public ColorPalette(Context context) {
        super(context);
        init();
    }


    public ColorPalette(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.ColorPalette, 0, 0);
        int id = typedArray.getResourceId(R.styleable.ColorPalette_colors, 0);
        if(id!=0) colors = getContext().getResources().getIntArray(id);
        autoPadding = typedArray.getBoolean(R.styleable.ColorPalette_color_autoPadding, false);
        outlineWidth = typedArray.getDimensionPixelSize(R.styleable.ColorPalette_color_outlineWidth, 0);
        fixedColumnCount = typedArray.getInt(R.styleable.ColorPalette_color_columnCount, -1);
        if (fixedColumnCount != -1) hasFixedColumnCount = true;

        typedArray.recycle();

        originalPaddingTop = getPaddingTop();
        originalPaddingBottom = getPaddingBottom();

        init();
    }

    public void init(){
        eventBus = new EventBus();
        eventBus.register(this);

        colorItemDimension = getResources().getDimensionPixelSize(R.dimen.color_item_small);
        colorItemMargin = getResources().getDimensionPixelSize(R.dimen.color_item_margins_small);
        setOrientation(LinearLayout.VERTICAL);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void createPaletteView(){
        if (viewInitialized && numColumns == oldNumColumns) {
            return;
        }
        viewInitialized = true;
        oldNumColumns = numColumns;

        removeAllViews();

        if(colors == null) return;
        int numItemsInRow = 0;

        LinearLayout row = createRow();
        for (int i = 0; i < colors.length; i++) {
            View colorItem = createColorItem(colors[i], selectedColor);
            row.addView(colorItem);
            numItemsInRow++;

            if (numItemsInRow == numColumns) {
                addView(row);
                row = createRow();
                numItemsInRow = 0;
            }
        }

        if (numItemsInRow > 0) {
            while (numItemsInRow < numColumns) {
                row.addView(createSpacer());
                numItemsInRow++;
            }
            addView(row);
        }
    }

    private LinearLayout createRow() {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(params);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        return row;
    }


    public void setOnColorSelectedListener(OnColorSelectedListener listener){
        this.listener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;

        if (!hasFixedColumnCount) {
            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
                numColumns = computeColumnCount(widthSize - (getPaddingLeft() + getPaddingRight()));
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = widthSize;
                numColumns = computeColumnCount(widthSize - (getPaddingLeft() + getPaddingRight()));
            } else {
                width = computeWidthForNumColumns(DEFAULT_COLUMN_COUNT) + getPaddingLeft() + getPaddingRight();
                numColumns = DEFAULT_COLUMN_COUNT;
            }
        } else {
            width = computeWidthForNumColumns(fixedColumnCount) + getPaddingLeft() + getPaddingRight();
            numColumns = fixedColumnCount;
        }

        computedVerticalPadding = (width - (computeWidthForNumColumns(numColumns) + getPaddingLeft() + getPaddingRight())) / 2;

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            int desiredHeight = computeHeight(numColumns) + originalPaddingTop + originalPaddingBottom;
            if (autoPadding) {
                desiredHeight += (2 * computedVerticalPadding);
            }
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = computeHeight(numColumns) + originalPaddingTop + originalPaddingBottom;
            if (autoPadding) {
                height += (2 * computedVerticalPadding);
            }
        }

        if (autoPadding) {
            setPaddingInternal(getPaddingLeft(), originalPaddingTop + computedVerticalPadding, getPaddingRight(), originalPaddingBottom + computedVerticalPadding);
        }
        createPaletteView();

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    private int computeColumnCount(int maxWidth) {
        int numColums = 0;
        while (((numColumns + 1) * colorItemDimension) + ((numColumns + 1) * 2 * colorItemMargin) <= maxWidth) {
            numColumns++;
        }
        return numColumns;
    }


    private int computeWidthForNumColumns(int columnCount) {
        return columnCount * (colorItemDimension + 2 * colorItemMargin);
    }

    private int computeHeight(int columnCount) {
        if (colors == null) {
            return 0;
        }
        int rowCount = colors.length / columnCount;
        if (colors.length % columnCount != 0) {
            rowCount++;
        }
        return rowCount * (colorItemDimension + 2 * colorItemMargin);
    }

    private void setPaddingInternal(int left, int top, int right, int bottom) {
        setPaddingCalledInternally = true;
        setPadding(left, top, right, bottom);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        if (!setPaddingCalledInternally) {
            originalPaddingTop = top;
            originalPaddingBottom = bottom;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ColorItem createColorItem(@ColorInt int color, @ColorInt int selectedColor) {
        ColorItem view = new ColorItem(getContext(), color, color == selectedColor, eventBus);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(colorItemDimension, colorItemDimension);
        params.setMargins(colorItemMargin, colorItemMargin, colorItemMargin, colorItemMargin);
        view.setLayoutParams(params);
        if (outlineWidth != 0) {
            view.setOutLineWidth(outlineWidth);
        }
        colorItemList.add(view);
        return view;
    }

    private ImageView createSpacer() {
        ImageView view = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(colorItemDimension, colorItemDimension);
        params.setMargins(colorItemMargin, colorItemMargin, colorItemMargin, colorItemMargin);
        view.setLayoutParams(params);
        return view;
    }

    public interface OnColorSelectedListener {
        void onColorSelected(@ColorInt int color);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Subscribe
    public void onSelectedColorChanged(SelectedColorChangedEvent event) {
        selectedColor = event.getSelectedColor();

        if (listener != null) {
            listener.onColorSelected(selectedColor);
        }
    }

}
