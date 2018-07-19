package com.wooplr.spotlight.shape;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;


import com.wooplr.spotlight.target.Target;
import com.wooplr.spotlight.utils.Utils;

/**
 * Created by jitender on 10/06/16.
 */

public class Circle {

    private Target target;
    private int radius;
    private Point circlePoint;
    private int padding = 20;
    public boolean isCircle = true;
    public boolean isBrick = false;

    public int shiftCoordX = 0,shiftCoordY = 0,shiftRadius = 0;



    public Circle(Target target, int padding) {
        this.target = target;
        this.padding = padding;
        circlePoint = getFocusPoint();
        calculateRadius(padding);
    }

    public  Circle setCircle(boolean circle){
        isCircle = circle;
        return this;
    }
    public  Circle setBrick(boolean brick){
        isBrick = brick;
        return this;
    }
    public  Circle setMask(int shiftCoordX,int shiftCoordY,int shiftRadius){
        this.shiftCoordX = shiftCoordX;
        this.shiftCoordY = shiftCoordY;
        this.shiftRadius = shiftRadius;
        return this;
    }

    public void draw(Canvas canvas, Paint eraser, int padding) {
        calculateRadius(padding);
        circlePoint = getFocusPoint();
        eraser.setColor(Color.RED);
        if(isCircle){
            canvas.drawCircle(circlePoint.x + shiftCoordX, circlePoint.y + shiftCoordY, radius + shiftRadius, eraser);
        }else if(isBrick){
            RectF rectF = new RectF(this.target.getRect().left, this.target.getRect().top, this.target.getRect().right, this.target.getRect().bottom);
            canvas.drawRect(rectF, eraser);
        }else{
            RectF rectF = new RectF(this.target.getRect().left, this.target.getRect().top, this.target.getRect().right, this.target.getRect().bottom);
            canvas.drawRoundRect(rectF, Utils.dpToPx(40), Utils.dpToPx(40), eraser);
        }

    }

    private Point getFocusPoint() {

        return target.getPoint();
    }

    public void reCalculateAll() {
        calculateRadius(padding);
        circlePoint = getFocusPoint();
    }

    private void calculateRadius(int padding) {
        int side;
        int minSide = Math.min(target.getRect().width() / 2, target.getRect().height() / 2);
        int maxSide = Math.max(target.getRect().width() / 2, target.getRect().height() / 2);
        side = (minSide + maxSide) / 2;
        radius = side + padding;
    }

    public int getRadius() {
        return radius;
    }

    public Point getPoint() {
        return circlePoint;
    }

}
