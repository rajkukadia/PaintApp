package raj.kukadia.paintapp.paint.view;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import raj.kukadia.paintapp.R;
import raj.kukadia.paintapp.paint.object.M;
import raj.kukadia.paintapp.paint.object.TouchPath;

/**
 * Created by RAJ on 2/2/2019.
 *
 * Paint Canvas View
 */


//Custom View Class
public class PaintView extends View{

    //Old x and y coordinates
    public float X, Y;
    public float rx, ry;
    public int colour;
    public int strokewidth;
    private Canvas canvas_;
    public static final int DEFAULT_COLOUR = Color.RED;
    public static final int DEFAULT_BACK_COLOUR = Color.WHITE;
    public static final float TOUCH_TOLERANCE =4;
    public static final String ERASER_SET = "ERASER_SET";
    public static final String ERASER_NOT_SET = "ERASER_NOT_SET";

    public static final int BRUSH_SIZE = 20;
    private Path path;
    private Paint paint;
    private Bitmap bitmap;
    private boolean isEraserSet;
    private boolean drawCircle = false;
    private boolean drawRect = false;
    private float radius = -1;
    private int prevColor;
    private int backcolour = DEFAULT_BACK_COLOUR;
    public ArrayList<TouchPath> paths = null;
    public ArrayList<Object> list = null;
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


    public void init(List<Object> list, DisplayMetrics displayMetrics){
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        this.list = (ArrayList<Object>) list;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas_ = new Canvas(bitmap);
        paths = (ArrayList<TouchPath>) list.get(0);
        colour = (Integer)list.get(1);
        prevColor = (int) list.get(3);
        isEraserSet = (boolean) list.get(2);
        strokewidth = (int) list.get(4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas_.drawColor(DEFAULT_BACK_COLOUR);
            for (TouchPath touchPath : paths) {
                paint.setColor(touchPath.colour);
                paint.setStrokeWidth(touchPath.strokewidth);
                canvas_.drawPath(touchPath.path, paint);
            }
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);//?????
        canvas.restore();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void undo(){
        if(paths.size()==0) return;
        stack.add(paths.remove(this.paths.size()-1));
        invalidate();
    }

    public void setEraser(boolean set){
        if(set){
            isEraserSet = true;
            prevColor = colour;
            this.colour = Color.WHITE;

        }else{
            isEraserSet = false;
            this.colour = prevColor;
        }
        list.remove(1);
        list.add(1, this.colour);
        list.remove(2);
        list.add(2,isEraserSet);
        list.remove(3);
        list.add(3, prevColor);
    }

    public String eraserState(){
        if(isEraserSet){
            return ERASER_SET;
        }else{
            return ERASER_NOT_SET;
        }
    }

    public void redo(){
        if(stack.isEmpty()) return;
        paths.add(stack.pop());
        invalidate();
    }

    public void changeColor(int color){
        this.colour = color;
        list.remove(1);
        list.add(1, this.colour);
    }

    public void clear(){
        backcolour = DEFAULT_BACK_COLOUR;
        paths.clear();
        invalidate();
    }

    //Create a new Path and Touch Path object
    private void touchDown(float x, float y){
        path = new Path();
        if(drawCircle) path.addCircle(x, y,radius, Path.Direction.CW);
        if(drawRect) path.addRect(x, y, x, y, Path.Direction.CW);
        TouchPath touchPath = new TouchPath(colour, strokewidth, path);
        //add the first touch path
        paths.add(touchPath);

        //clear the path
        if(drawCircle|| drawRect){return;}

        path.reset();

        //Set the beginning of next contour at x and y
        path.moveTo(x, y);

        X = x;
        Y = y;
    }

    //Ends the path
    private void touchUp(){
        //adds a line from the last point to the specified point
        path.lineTo(X, Y);
    }

    //onTouchEvent returns true on the view so continues to draw on this view
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

            //When the user starts to touch
            case MotionEvent.ACTION_DOWN:
                if(drawRect) {
                    touchDown(rx, ry);
                }else {
                    touchDown(x, y);
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                if(drawCircle || drawRect){ break;}
                touchMove(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                if(drawCircle || drawRect){drawRect = false; drawCircle = false;break;}
                touchUp();
                invalidate();
                break;
        }

        return true;
    }

    public Canvas getCanvas(){
        return canvas_;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }


    public void wantToDrawRect(float x, float y){
        drawRect = true;
        rx = x;
        ry = y;
    }


    public void wantToDrawCircle(float radius){
        drawCircle = true;
        this.radius = radius;
    }

    public void setStrokewidth(float width){
        strokewidth = (int) width;
        list.remove(4);
        list.add(4, strokewidth);
    }

}
