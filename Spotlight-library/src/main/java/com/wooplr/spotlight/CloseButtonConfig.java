package com.wooplr.spotlight;

import android.graphics.drawable.Drawable;

/**
 * Created by Morgot on 24.04.17.
 */

public class CloseButtonConfig{

    public int size = 50;
    public int marginTop = 20;
    public int marginEnd = 10;
    public int marginStart = 10;
    public Drawable image = null;
    public int backgroundInt = R.drawable.btn_close_selector;

    public CloseButtonConfig(int size,int marginTop,int marginEnd){

        this.size = size;
        this.marginEnd = marginEnd;
        this.marginTop = marginTop;
    }

    public CloseButtonConfig(int size,int marginTop,int marginEnd,int marginStart,Drawable image){

        this.size = size;
        this.marginEnd = marginEnd;
        this.marginTop = marginTop;
        this.image = image;
        this.marginStart = marginStart;
    }

    public CloseButtonConfig(){}



}