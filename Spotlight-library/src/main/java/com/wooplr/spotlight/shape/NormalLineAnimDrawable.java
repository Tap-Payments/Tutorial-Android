package com.wooplr.spotlight.shape;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.wooplr.spotlight.target.SpotAnimPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapted from github.com/dupengtao/LineAnimation
 */
public class NormalLineAnimDrawable extends Drawable implements ValueAnimator.AnimatorUpdateListener {

    private static final String FACTOR_X = "factorX";
    private static final String FACTOR_Y = "factorY";
    private final Path mPath2;
    private final Paint mPaint2;
    private float factorY, factorX;
    private SpotAnimPoint curSpotAnimPoint = null;
    private int moveTimes;
    private List<SpotAnimPoint> mSpotAnimPoints = new ArrayList<SpotAnimPoint>();
    private ObjectAnimator mLineAnim;
    private DisplayMode curDisplayMode = DisplayMode.Appear;
    private long lineAnimDuration = 400;
    private int lineColor = Color.parseColor("#eb273f");
    public static int lineStroke = 4;
    public static int arrowSize = 50;

    private Animator.AnimatorListener mListner;

    public NormalLineAnimDrawable() {
        this(null);
    }

    public NormalLineAnimDrawable(Paint paint) {
        mPath2 = new Path();
        mPaint2 = paint == null ? getDefaultPaint() : paint;
    }

    private Paint getDefaultPaint() {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(lineStroke);
        p.setColor(lineColor);
        return p;
    }

    private ObjectAnimator getLineAnim() {
        PropertyValuesHolder pvMoveY = PropertyValuesHolder.ofFloat(FACTOR_Y,
                0f, 1f);
        PropertyValuesHolder pvMoveX = PropertyValuesHolder.ofFloat(FACTOR_X,
                0f, 1f);
        ObjectAnimator lineAnim = ObjectAnimator.ofPropertyValuesHolder(
                this, pvMoveY, pvMoveX).setDuration(lineAnimDuration);
        lineAnim.setRepeatMode(ValueAnimator.RESTART);
        lineAnim.setRepeatCount(mSpotAnimPoints.size() - 1);
        lineAnim.addUpdateListener(this);
        if (android.os.Build.VERSION.SDK_INT > 17) {
            lineAnim.setAutoCancel(true);
        }
        lineAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                moveTimes = 0;
                curSpotAnimPoint = mSpotAnimPoints.get(moveTimes);
                if (mListner != null)
                    mListner.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mListner != null)
                    mListner.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (mListner != null)
                    mListner.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                moveTimes++;
                curSpotAnimPoint = mSpotAnimPoints.get(moveTimes);
                if (mListner != null)
                    mListner.onAnimationRepeat(animation);
            }
        });
        //lineAnim.addListener(mListner);
        return lineAnim;
    }

    @NonNull
    public void setmListner(Animator.AnimatorListener mListner) {
        this.mListner = mListner;
    }

    public void playAnim(List<SpotAnimPoint> spotAnimPoints) {
        if (spotAnimPoints != null) {
            mSpotAnimPoints = spotAnimPoints;
        }
        if (mLineAnim == null) {
            mLineAnim = getLineAnim();
        }
        if (mLineAnim.isRunning()) {
            mLineAnim.cancel();
        }

        mLineAnim.start();
    }

    public void playAnim() {
        playAnim(null);
    }


    public float getFactorY() {
        return factorY;
    }

    public void setFactorY(float factorY) {
        this.factorY = factorY;
    }

    public float getFactorX() {
        return factorX;
    }

    public void setFactorX(float factorX) {
        this.factorX = factorX;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidateSelf();
    }

    private void drawLine(List<SpotAnimPoint> spotAnimPoints, int num) {
        drawLine(spotAnimPoints, num, spotAnimPoints.size());
    }

    private void drawLine(List<SpotAnimPoint> spotAnimPoints, int num, int size) {
        for (int i = num, j = size; i < j; i++) {
            SpotAnimPoint p = spotAnimPoints.get(i);
            mPath2.moveTo(p.getCurX(), p.getCurY());
            mPath2.lineTo(p.getMoveX(), p.getMoveY());
        }
    }

    public DisplayMode getCurDisplayMode() {
        return curDisplayMode;
    }

    public void setCurDisplayMode(DisplayMode curDisplayMode) {
        this.curDisplayMode = curDisplayMode;
    }





    @Override
    public void draw(Canvas canvas) {
        if (curSpotAnimPoint != null) {
            mPath2.rewind();
            float curX = getPoints().get(0).getCurX();
            float curY = getPoints().get(0).getCurY();
            float moveX = getPoints().get(getPoints().size()-1).getMoveX();
            float moveY = getPoints().get(getPoints().size()-1).getMoveY();
            if (curDisplayMode == DisplayMode.Disappear) {
                mPath2.moveTo(curX == moveX ? moveX : curX + ((moveX - curX) * factorX), curY == moveY ? moveY : curY + ((moveY - curY) * factorY));
                mPath2.lineTo(moveX, moveY);
                drawLine(mSpotAnimPoints, moveTimes + 1);
                canvas.drawPath(mPath2, mPaint2);
            } else if (curDisplayMode == DisplayMode.Appear) {

                mPaint2.setStrokeWidth(lineStroke);

                float midleX  = getPoints().get(0).getMoveX();
                float midleY  = getPoints().get(0).getMoveY();




                mPath2.moveTo(moveX, moveY);
                mPath2.cubicTo(moveX, moveY,midleX,midleY,curX, curY);



                Quarter q = Quarter.rightTop;
                Direction d = Direction.side;


                if (curX > moveX){

                    if (curY < moveY){

                        q = Quarter.rightTop;
                        if (moveX < midleX){

                            d = Direction.bottom;
                        }else {

                            d = Direction.side;
                        }
                    }else if(curY > moveY) {

                        q = Quarter.rightBottom;
                        if (moveX < midleX){

                            d = Direction.top;
                        }else {

                            d = Direction.side;
                        }
                    }
                }else if (curX < moveX){

                    if (curY < moveY){

                        q = Quarter.leftTop;
                        if (moveX > midleX){

                            d = Direction.bottom;
                        }else {

                            d = Direction.side;
                        }
                    }else {

                        q = Quarter.leftBottom;
                        if (moveX > midleX){

                            d = Direction.top;
                        }else {

                            d = Direction.side;
                        }
                    }


                }else {

                    if (moveY > curY){

                        q = Quarter.leftTop;
                        d = Direction.bottom;
                    }else{

                        q = Quarter.leftBottom;
                        d = Direction.top;
                    }


                }


                drawArrowPath(q,d,curX,curY);
                drawAnimation(canvas,mPath2,mPaint2);

            }


        } else {
            canvas.drawPath(mPath2, mPaint2);
        }
    }

    private enum Quarter { leftTop,leftBottom,rightTop,rightBottom};

    private enum Direction { bottom,side,top};


    void drawArrowPath(Quarter quarter,Direction direction,float startX,float startY){

        float leftArrowPartX = 0;
        float leftArrowPartY = 0;
        float rightArrowPartX = 0;
        float rightArrowPartY = 0;

        switch (quarter){

            case rightTop:

                if(direction == Direction.bottom){

                    leftArrowPartX = startX - arrowSize;
                    leftArrowPartY = startY + arrowSize;

                    rightArrowPartX = startX + arrowSize;
                    rightArrowPartY = startY + arrowSize;
                }else if (direction == Direction.side){

                    leftArrowPartX = startX - arrowSize;
                    leftArrowPartY = startY - arrowSize;

                    rightArrowPartX = startX - arrowSize;
                    rightArrowPartY = startY + arrowSize;

                }

            break;

            case leftTop:

                if(direction == Direction.bottom){

                    leftArrowPartX = startX - arrowSize;
                    leftArrowPartY = startY + arrowSize;

                    rightArrowPartX = startX + arrowSize;
                    rightArrowPartY = startY + arrowSize;
                }else if (direction == Direction.side){

                    leftArrowPartX = startX + arrowSize;
                    leftArrowPartY = startY - arrowSize;

                    rightArrowPartX = startX + arrowSize;
                    rightArrowPartY = startY + arrowSize;

                }

                break;

            case leftBottom:

                if(direction == Direction.top){

                    leftArrowPartX = startX - arrowSize;
                    leftArrowPartY = startY - arrowSize;

                    rightArrowPartX = startX + arrowSize;
                    rightArrowPartY = startY - arrowSize;
                }else if (direction == Direction.side){

                    leftArrowPartX = startX - arrowSize;
                    leftArrowPartY = startY - arrowSize;

                    rightArrowPartX = startX - arrowSize;
                    rightArrowPartY = startY + arrowSize;
                }

                break;

            case rightBottom:

                if(direction == Direction.top){

                    leftArrowPartX = startX + arrowSize;
                    leftArrowPartY = startY - arrowSize;

                    rightArrowPartX = startX - arrowSize;
                    rightArrowPartY = startY - arrowSize;
                }else if (direction == Direction.side){

                    leftArrowPartX = startX - arrowSize;
                    leftArrowPartY = startY + arrowSize;

                    rightArrowPartX = startX - arrowSize;
                    rightArrowPartY = startY - arrowSize;
                }

                break;




        }




        mPath2.moveTo(startX,startY); // to start

        mPath2.lineTo(leftArrowPartX, leftArrowPartY);//draw the first arrowhead line to the left

        mPath2.moveTo(startX,startY); // to start

        mPath2.lineTo(rightArrowPartX, rightArrowPartY);//draw the next arrowhead line to the rights

        mPath2.moveTo(leftArrowPartX,leftArrowPartY); // to start
        mPath2.lineTo(rightArrowPartX, rightArrowPartY);

    }


    private static final float animSpeedInMs = .8f;
    private static final long animMsBetweenStrokes = 0;
    private long animLastUpdate;
    private boolean animRunning;
    private int animCurrentCountour;
    private float animCurrentPos;
    private Path animPath;
    private PathMeasure animPathMeasure;

    private void drawAnimation(Canvas canvas,Path path,Paint paint) {
        if (animPathMeasure == null) {
            // Start of animation. Set it up.
            animPathMeasure = new PathMeasure(path, false);
            animPathMeasure.nextContour();
            animPath = new Path();
            animLastUpdate = System.currentTimeMillis();
            animCurrentCountour = 0;
            animCurrentPos = 0.0f;
        } else {
            // Get time since last frame
            long now = System.currentTimeMillis();
            long timeSinceLast = now - animLastUpdate;

            if (animCurrentPos == 0.0f) {
                timeSinceLast -= animMsBetweenStrokes;
            }

            if (timeSinceLast > 0) {
                // Get next segment of path
                float newPos = (float)(timeSinceLast) / animSpeedInMs + animCurrentPos;
                boolean moveTo = (animCurrentPos == 0.0f);
                animPathMeasure.getSegment(animCurrentPos, newPos, animPath, moveTo);
                animCurrentPos = newPos;
                animLastUpdate = now;

                // If this stroke is done, move on to next
                if (newPos > animPathMeasure.getLength()) {
                    animCurrentPos = 0.0f;
                    animCurrentCountour++;
                    boolean more = animPathMeasure.nextContour();
                    // Check if finished
                    if (!more) { animRunning = false; }
                }
            }

            // Draw path
            canvas.drawPath(animPath, paint);
        }

        invalidateSelf();
    }





    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public List<SpotAnimPoint> getPoints() {
        return mSpotAnimPoints;
    }

    public void setPoints(List<SpotAnimPoint> spotAnimPoints) {
        mSpotAnimPoints = spotAnimPoints;
    }

    public void setLineAnimDuration(long lineAnimDuration) {
        this.lineAnimDuration = lineAnimDuration;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setLineStroke(int lineStroke) {
        this.lineStroke = lineStroke;
    }

    /**
     * How to display the LineAnim
     */
    public enum DisplayMode {

        Disappear,

        Appear,
    }

}
