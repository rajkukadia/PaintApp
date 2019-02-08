package raj.kukadia.paintapp.paint.view;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import raj.kukadia.paintapp.paint.object.TouchPath;

/**
 * Created by RAJ on 2/2/2019.
 *
 * Paint Canvas View
 */

public class PaintView extends View {

    public float X, Y;
    public int colour;
    public int strokewidth;
    private Canvas canvas_;
    public static final int DEFAULT_COLOUR = Color.RED;
    public static final int DEFAULT_BACK_COLOUR = Color.WHITE;
    public static final float TOUCH_TOLERANCE = 4;
    public static final int BRUSH_SIZE = 20;
    private Path path;
    private Paint paint;
    private Bitmap bitmap;
    private int backcolour = DEFAULT_BACK_COLOUR;
    public ArrayList<TouchPath> paths = null;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Stack<TouchPath> stack = new Stack<>();

    public PaintView(Context context){
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(DEFAULT_COLOUR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(null);
        paint.setAlpha(0xff);
    }

    public void init(List<Object> list){

        canvas_ = (Canvas) list.get(0);
        bitmap = (Bitmap) list.get(1);
        paths = (ArrayList<TouchPath>) list.get(2);
        colour = (Integer)list.get(3);
        strokewidth = BRUSH_SIZE;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas_.drawColor(DEFAULT_BACK_COLOUR);
        for(TouchPath touchPath : paths){
            paint.setColor(touchPath.colour);
            paint.setStrokeWidth(touchPath.strokewidth);
            canvas_.drawPath(touchPath.path, paint);
        }
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore();
    }

    public void undo(){
        if(paths.size()==0) return;
        stack.add(paths.remove(this.paths.size()-1));
        invalidate();
    }

    public void redo(){
        if(stack.isEmpty()) return;
        paths.add(stack.pop());
        invalidate();
    }

    public void changeColor(int color){
        this.colour = color;
    }

    public void clear(){
        backcolour = DEFAULT_BACK_COLOUR;
        paths.clear();
        invalidate();
    }

    private void touchDown(float x, float y){
        path = new Path();
        TouchPath touchPath = new TouchPath(colour, strokewidth, path);
        paths.add(touchPath);

        path.reset();
        path.moveTo(x, y);

        X = x;
        Y = y;
    }

    private void touchUp(){
        path.lineTo(X, Y);
    }

    private void touchMove(float x, float y){
        float dx = Math.abs(x-X);
        float dy = Math.abs(y-Y);

        if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE){
            path.quadTo(X, Y, (x+X)/2, (y+Y)/2);
            X = x;
            Y = y;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }

    public Canvas getCanvas(){
        return canvas_;
    }

}
