package com.wooplr.spotlight;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wooplr.spotlight.prefs.PreferencesManager;
import com.wooplr.spotlight.shape.Circle;
import com.wooplr.spotlight.shape.NormalLineAnimDrawable;
import com.wooplr.spotlight.target.SpotAnimPoint;
import com.wooplr.spotlight.target.Target;
import com.wooplr.spotlight.target.ViewTarget;
import com.wooplr.spotlight.utils.SpotlightListener;
import com.wooplr.spotlight.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jitender on 10/06/16.
 */

public class SpotlightView extends FrameLayout {



    public  CloseButtonConfig closeButtonSettings = new CloseButtonConfig();

    /**
     * isArabic
     */
    private boolean isArabic = false;
    /**
     * isStraigth
     */
    private boolean isStraigth = false;

    /**
     * isStraigth
     */
    private boolean straigthVertical = true;

    private int heightOfView = -1;

    /**
     * isStraigth
     */
    private boolean isBrick = false;

    private float yOfArrow = -1;

    private int eraserMaskX = 0,eraserMaskY = 0,eraserMaskRadius = 0;

    /**
     * OverLay color
     */
    private int maskColor = 0x70000000;

    /**
     * Intro Animation Duration
     */
    private long introAnimationDuration = 300;

    /**
     * Toggel between reveal and fadein animation
     */
    private boolean isRevealAnimationEnabled = true;

    /**
     * Final fadein text duration
     */
    private long fadingTextDuration = 400;

    /**
     * Start intro once view is ready to show
     */
    private boolean isReady;

    /**
     * Overlay circle above the view
     */
    private Circle circleShape;

    /**
     * Target View
     */
    private Target targetView;


    /**
     * Eraser to erase the circle area
     */
    private Paint eraser;

    /**
     * Delay the intro view
     */
    private Handler handler;
    private Bitmap bitmap;
    private Canvas canvas;

    /**
     * Padding for circle
     */
    private int padding = 20;

    /**
     * View Width
     */
    private int width;

    /**
     * View Height
     */
    private int height;

    /**
     * Dismiss layout on touch
     */
    private boolean dismissOnTouch;
    private boolean dismissOnBackPress;

    private PreferencesManager preferencesManager;
    private String usageId;

    /**
     * Listener for spotLight when user clicks on the view
     */
    private SpotlightListener listener;

    /**
     * Perform click when user clicks on the targetView
     */
    private boolean isPerformClick;

    /**
     * Margin from left, right, top and bottom till the line will stop
     */
    private int gutter = Utils.dpToPx(36);

    /**
     * Views Heading and sub-heading for spotlight
     */
    private TextView subHeadingTv, headingTv;

    /**
     * Whether to show the arc at the end of the line that points to the target.
     */
    private boolean showTargetArc = true;

    /**
     * Extra padding around the arc
     */
    private int extraPaddingForArc = 40;

    /**
     * Defaults for heading TextView
     */
    private int headingTvSize = 24;
    private int headingTvColor = Color.parseColor("#eb273f");
    private CharSequence headingTvText = "Hello";

    /**
     * Defaults for sub-heading TextView
     */
    private int subHeadingTvSize = 24;
    private int subHeadingTvColor = Color.parseColor("#ffffff");
    private CharSequence subHeadingTvText = "Hello";

    /**
     * Values for line animation
     */
    private long lineAnimationDuration = 300;
    private int lineStroke;
    private PathEffect lineEffect;
    private int lineAndArcColor = Color.parseColor("#eb273f");

    private ArrayList<View>viewsForTargets = null;
    private Rect rect = null;
    private ArrayList<Circle>circles = new ArrayList<>();

    private boolean isCircleTargetView = true;

    private Typeface mTypeface = null;

    private int arrowSize = 25;

    private int arrowMargin = 10;

    boolean isElementCLick = true;
    private boolean considerStatusBarHeightInCloseButton;
    private Bitmap backgroundBitmap;
    private boolean isBackgroundFullScreen;


    public SpotlightView(Context context) {
        super(context);
        init(context);
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setVisibility(INVISIBLE);

        lineStroke = Utils.dpToPx(4);
        isReady = false;
        isRevealAnimationEnabled = true;
        dismissOnTouch = false;
        isPerformClick = false;
        dismissOnBackPress = false;
        handler = new Handler();
        preferencesManager = new PreferencesManager(context);
        eraser = new Paint();
        eraser.setColor(0xFFFFFFFF);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    private Canvas canvastemp = null;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvastemp = canvas;
        if (!isReady) return;

        int topPosition = isBackgroundFullScreen ? 0 : Utils.getStatusBarHeight(getContext());

        if (bitmap == null || canvas == null) {
            if (bitmap != null) bitmap.recycle();
            if(heightOfView != -1){

                height = heightOfView;
            }

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            this.canvas = new Canvas(bitmap);
        }

        this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (backgroundBitmap != null) {
            //this.bitmap = backgroundBitmap;
            this.canvas.drawBitmap(backgroundBitmap, 0, topPosition, null);
        } else {
            this.canvas.drawColor(maskColor);
        }

        circleShape.draw(this.canvas, eraser, padding);
        for(int i=1;i<circles.size();i++){
            Circle c  = circles.get(i);
            c.draw(this.canvas,eraser,padding);
        }

        canvas.drawBitmap(bitmap, 0, 0, null);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xT = event.getX();
        float yT = event.getY();

        int xV = circleShape.getPoint().x;
        int yV = circleShape.getPoint().y;

        int radius = circleShape.getRadius();

        double dx = Math.pow(xT - xV, 2);
        double dy = Math.pow(yT - yV, 2);

        boolean isTouchOnFocus = (dx + dy) <= Math.pow(radius, 2);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (isTouchOnFocus && isPerformClick) {
                    targetView.getView().setPressed(true);
                    targetView.getView().invalidate();
                }

                return true;
            case MotionEvent.ACTION_UP:
                if (isTouchOnFocus || dismissOnTouch)
                    dismiss();

                if (isTouchOnFocus && isPerformClick) {
                    targetView.getView().performClick();
                    targetView.getView().setPressed(true);
                    targetView.getView().invalidate();
                    targetView.getView().setPressed(false);
                    targetView.getView().invalidate();
                }

                return true;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }


    /**
     * Show the view based on the configuration
     * Reveal is available only for Lollipop and above in other only fadein will work
     * To support reveal in older versions use github.com/ozodrukh/CircularReveal
     *
     * @param activity
     */
    public void show(final Activity activity) {

        if (preferencesManager.isDisplayed(usageId))
            return;

        final ViewGroup strongThis = (ViewGroup)this;
        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setReady(true);
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    startFadeInAnimation(activity);
                                    setupCloseButton(activity);

                                }
                            }
                , 100);

    }


    private void setupCloseButton(final Activity activity){
        final FrameLayout closeButtonContainer = new FrameLayout(activity) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return super.onTouchEvent(event);
            }

            @Override
            public boolean performClick() {
                return super.performClick();
            }
        };
        boolean isStart = (targetView.getPoint().y < getHeight() / 2)&&(targetView.getPoint().x > getWidth() / 2);

        //close button params
        final View exit = new View(activity);
        if (closeButtonSettings.image == null){
            exit.setBackgroundResource(closeButtonSettings.backgroundInt);
        }else {
            exit.setBackground(closeButtonSettings.image);
        }

        closeButtonContainer.addView(exit);
        FrameLayout.LayoutParams lp = new LayoutParams(Utils.dpToPx(closeButtonSettings.size), Utils.dpToPx(closeButtonSettings.size));
        lp.gravity = (Gravity.CENTER);
        exit.setLayoutParams(lp);

        //close button container params
        int increasedClickAreaInPx = Utils.dpToPx(25);
        FrameLayout.LayoutParams containerParams = new LayoutParams(Utils.dpToPx(closeButtonSettings.size) + increasedClickAreaInPx * 2,
                Utils.dpToPx(closeButtonSettings.size) + increasedClickAreaInPx * 2);
        containerParams.setMargins(Utils.dpToPx(closeButtonSettings.marginStart) - increasedClickAreaInPx,
                Utils.dpToPx(closeButtonSettings.marginTop) + (considerStatusBarHeightInCloseButton ? Utils.getStatusBarHeight(getContext()) : 0) - increasedClickAreaInPx,
                Utils.dpToPx(closeButtonSettings.marginEnd) - increasedClickAreaInPx, 0);
        containerParams.setMarginEnd(Utils.dpToPx(closeButtonSettings.marginEnd) - increasedClickAreaInPx);
        containerParams.setMarginStart(Utils.dpToPx(closeButtonSettings.marginStart) - increasedClickAreaInPx);
        int position = Gravity.START;
        if (isStart&&isArabic){

            position = Gravity.END;
        }else if (!isStart&&!isArabic){

            position = Gravity.END;
        }
        containerParams.gravity = (Gravity.TOP|position);

        this.addView(closeButtonContainer);
        closeButtonContainer.setLayoutParams(containerParams);

        closeButtonContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeButtonContainer.setEnabled(false);
                isElementCLick = false;
                dismiss();
            }
        });
    }


    static private float dencity = -1;

    int getDpFromPx(int value){

        if (dencity == -1){

            dencity  = getResources().getDisplayMetrics().density;
        }


        return  (int)(dencity*value);
    }



    /**
     * Dissmiss view with reverse animation
     */
    public void dismiss() {
        preferencesManager.setDisplayed(usageId);
        if(listener!=null){
            listener.onUserClicked(usageId,true);
        }
        startFadeOutAnimation();
    }


    /**
     * Revel animation from target center to screen width and height
     *
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startRevealAnimation(final Activity activity) {

        float finalRadius = (float) Math.hypot(getWidth(), getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(this, targetView.getPoint().x, targetView.getPoint().y, 0, finalRadius);
        anim.setInterpolator(AnimationUtils.loadInterpolator(activity,
                android.R.interpolator.fast_out_linear_in));
        anim.setDuration(introAnimationDuration);

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (showTargetArc) {
                    addArcAnimation(activity);
                } else {
                    addPathAnimation(activity);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        setVisibility(View.VISIBLE);
        anim.start();
    }

    /**
     * Reverse reveal animation
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exitRevealAnimation() {
        float finalRadius = (float) Math.hypot(getWidth(), getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(this, targetView.getPoint().x, targetView.getPoint().y, finalRadius, 0);
        anim.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.accelerate_decelerate));
        anim.setDuration(introAnimationDuration);

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setVisibility(GONE);
                removeSpotlightView(isElementCLick);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        anim.start();
    }

    private void startFadeInAnimation(final Activity activity) {

        setVisibility(VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        fadeIn.setDuration(300);
        fadeIn.setFillAfter(true);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (showTargetArc) {
                    addArcAnimation(activity);
                } else {
                    addPathAnimation(activity);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation(fadeIn);
    }

    private void startFadeOutAnimation() {
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        fadeIn.setDuration(300);
        fadeIn.setFillAfter(true);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(GONE);
                removeSpotlightView(isElementCLick);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation(fadeIn);

    }

    /**
     * Add arc above/below the circular target overlay.
     */
    private void addArcAnimation(final Activity activity) {
        AppCompatImageView mImageView = new AppCompatImageView(activity);
        mImageView.setImageResource(R.drawable.ic_spotlight_arc);
        LayoutParams params = new LayoutParams(2 * (circleShape.getRadius() + extraPaddingForArc),
                2 * (circleShape.getRadius() + extraPaddingForArc));


        if (targetView.getPoint().y > getHeight() / 2) {//bottom
            if (targetView.getPoint().x > getWidth() / 2) {//Right
                params.rightMargin = getWidth() - targetView.getPoint().x - circleShape.getRadius() - extraPaddingForArc;
                params.bottomMargin = getHeight() - targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc;
                params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            } else {
                params.leftMargin = targetView.getPoint().x - circleShape.getRadius() - extraPaddingForArc;
                params.bottomMargin = getHeight() - targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc;
                params.gravity = Gravity.LEFT | Gravity.BOTTOM;
            }
        } else {//up
            mImageView.setRotation(180); //Reverse the view
            if (targetView.getPoint().x > getWidth() / 2) {//Right
                params.rightMargin = getWidth() - targetView.getPoint().x - circleShape.getRadius() - extraPaddingForArc;
                params.bottomMargin = getHeight() - targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc;
                params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            } else {
                params.leftMargin = targetView.getPoint().x - circleShape.getRadius() - extraPaddingForArc;
                params.bottomMargin = getHeight() - targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc;
                params.gravity = Gravity.LEFT | Gravity.BOTTOM;
            }

        }
        mImageView.postInvalidate();
        mImageView.setLayoutParams(params);
        addView(mImageView);

        PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(lineAndArcColor,
                PorterDuff.Mode.SRC_ATOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable)
                    ContextCompat.getDrawable(activity, R.drawable.avd_spotlight_arc);
            avd.setColorFilter(porterDuffColorFilter);
            mImageView.setImageDrawable(avd);
            avd.start();
        } else {
            AnimatedVectorDrawableCompat avdc =
                    AnimatedVectorDrawableCompat.create(activity, R.drawable.avd_spotlight_arc);
            avdc.setColorFilter(porterDuffColorFilter);
            mImageView.setImageDrawable(avdc);
            avdc.start();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                addPathAnimation(activity);
            }
        }, 400);
    }

    private void addPathAnimation(Activity activity) {


        View mView = new View(activity);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        params.width = getWidth();
        params.height = getHeight();
        addView(mView, params);

        //Textviews
        headingTv = new TextView(activity);
        if (mTypeface != null)
            headingTv.setTypeface(mTypeface);
        headingTv.setTextSize(headingTvSize);
        headingTv.setVisibility(View.GONE);
        headingTv.setTextColor(headingTvColor);
        headingTv.setText(headingTvText);

        subHeadingTv = new TextView(activity);
        if (mTypeface != null)
            subHeadingTv.setTypeface(mTypeface);
        subHeadingTv.setTextSize(subHeadingTvSize);
        subHeadingTv.setTextColor(subHeadingTvColor);
        subHeadingTv.setVisibility(View.GONE);
        subHeadingTv.setText(subHeadingTvText);

        //Line animation
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(lineStroke);
        p.setColor(lineAndArcColor);
        p.setPathEffect(lineEffect);

        NormalLineAnimDrawable animDrawable1 = new NormalLineAnimDrawable(p);
        if (lineAnimationDuration > 0)
            animDrawable1.setLineAnimDuration(lineAnimationDuration);
        if (Build.VERSION.SDK_INT < 16) {
            mView.setBackgroundDrawable(animDrawable1);
        } else {
            mView.setBackground(animDrawable1);
        }
        animDrawable1.setPoints(checkLinePoint());
        animDrawable1.playAnim();

        animDrawable1.setmListner(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dismissOnTouch = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                fadeIn.setDuration(fadingTextDuration);
                fadeIn.setFillAfter(true);
                headingTv.startAnimation(fadeIn);
                subHeadingTv.startAnimation(fadeIn);
                headingTv.setVisibility(View.VISIBLE);
                subHeadingTv.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }


    private int shiftValue = Utils.dpToPx(40);


    private void enableDismissOnBackPress() {
        setFocusableInTouchMode(true);
        setFocusable(true);
        requestFocus();
    }

    private List<SpotAnimPoint> checkLinePoint() {

        //Screen Height
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        List<SpotAnimPoint> spotAnimPoints = new ArrayList<>();

        //For TextViews
        LayoutParams headingParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        LayoutParams subHeadingParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        //Spaces above and below the line
        int spaceAboveLine = Utils.dpToPx(8);
        int spaceBelowLine = Utils.dpToPx(12);

        int extramargin = Utils.dpToPx(8);

        float radius = circleShape.getRadius();
        if (!isCircleTargetView){

            radius = Utils.dpToPx(30);
        }



        float startPX = 0,startPY = 0,midlePX = 0,midlePY = 0,endPX = 0,endPY = 0;

        NormalLineAnimDrawable.lineStroke = lineStroke;
        NormalLineAnimDrawable.arrowSize = arrowSize;


        if(isStraigth){

            if(straigthVertical){

                if (targetView.getPoint().y > screenHeight / 2){ //bottom

                    startPX = getMiddleOfViewX();
                    startPY = targetView.getPoint().y - (extraPaddingForArc) - arrowMargin;

                    endPX = startPX;
                    endPY = startPY - Utils.dpToPx(30);

                    spotAnimPoints.add(new SpotAnimPoint(startPX,
                            startPY,
                            endPX,
                            endPY
                    ));

                    headingParams.leftMargin = 10;
                    headingParams.rightMargin = 10;
                    headingParams.bottomMargin = screenHeight - (int) endPY + spaceAboveLine;
                    headingParams.topMargin = extramargin;
                    headingParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    headingTv.setGravity(Gravity.CENTER);
                }else{//top

                        startPX = getMiddleOfViewX();
                        startPY = targetView.getPoint().y + Utils.dpToPx(20);

                        endPX = startPX;
                        endPY = startPY + (int) (1.5 * shiftValue);

                        spotAnimPoints.add(new SpotAnimPoint(startPX,
                                startPY,
                                endPX,
                                endPY
                        ));

                        headingParams.leftMargin = 0;
                        headingParams.rightMargin = 0;
                        headingParams.bottomMargin = screenHeight - ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceAboveLine;
                        headingParams.topMargin = (int) endPY + spaceBelowLine;
                        headingParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                        headingTv.setGravity(Gravity.CENTER);
                }

            }else{//horizontal

                float middleY = 0;
                for(View v : viewsForTargets){

                    middleY += v.getY();
                }
                middleY /= viewsForTargets.size();
                middleY += shiftValue;
                int deviceWidth;
                WindowManager wm =
                        (WindowManager) headingTv.getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                deviceWidth = size.x;

                if(yOfArrow != -1){

                    middleY = yOfArrow;
                }


                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);



                if (targetView.getPoint().x > screenWidth / 2) {//right

                    startPY = middleY;
                    startPX = targetView.getPoint().x - (extraPaddingForArc*2) - arrowMargin;

                    endPX = startPX - (int) (shiftValue * 1.5);
                    endPY = startPY;

                    spotAnimPoints.add(new SpotAnimPoint(startPX,
                            startPY,
                            endPX,
                            endPY
                    ));

                    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)(deviceWidth - endPX), View.MeasureSpec.AT_MOST);
                    headingTv.measure(widthMeasureSpec, heightMeasureSpec);

                    headingParams.leftMargin = 0;
                    headingParams.rightMargin = (int)(endPX + shiftValue/2);
                    headingParams.bottomMargin = 0;
                    headingParams.topMargin = (int)(startPY - headingTv.getMeasuredHeight()/2 -extramargin);
                    headingParams.gravity = Gravity.TOP | Gravity.RIGHT;
                    headingTv.setGravity(Gravity.CENTER|Gravity.RIGHT);
                    //headingTv.setBackgroundColor(Color.RED);
                } else {//left

                    startPY = middleY;
                    startPX = ( targetView.getViewWidth()) + (float)(arrowMargin) + Utils.dpToPx(15);

                    endPX = startPX + (int) (shiftValue );
                    endPY = startPY;

                    spotAnimPoints.add(new SpotAnimPoint(startPX,
                            startPY,
                            endPX,
                            endPY
                    ));

                    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)(deviceWidth - endPX), View.MeasureSpec.AT_MOST);
                    headingTv.measure(widthMeasureSpec, heightMeasureSpec);

                    headingParams.leftMargin = (int)(endPX + Utils.dpToPx(5));
                    headingParams.rightMargin = Utils.dpToPx(10);
                    headingParams.bottomMargin = 0;
                    headingParams.topMargin = (int)(startPY - headingTv.getMeasuredHeight()/2 -extramargin);
                    headingParams.gravity = Gravity.TOP | Gravity.LEFT;
                    headingTv.setGravity(Gravity.CENTER|Gravity.LEFT);
                }


            }

        }else {
            if (targetView.getPoint().y > screenHeight / 2) {//Down

                    if (targetView.getPoint().x > screenWidth / 2) {//Right

                        startPX = (targetView.getViewRight() - targetView.getViewWidth() / 2);
                        startPY = targetView.getPoint().y - (extraPaddingForArc) - arrowMargin;

                        midlePX = startPX;
                        midlePY = startPY - (int) (shiftValue * 4);

                        endPX = startPX - (int) (shiftValue * 1.5);
                        endPY = midlePY;

                        if (startPX > screenWidth || startPX < 0) {

                            startPX = targetView.getViewWidth() / 2 + targetView.getViewLeft();
                            startPY = targetView.getViewTop() - shiftValue;

                            midlePX = startPX;
                            midlePY = startPY - shiftValue * 2;

                            endPX = midlePX - shiftValue;
                            endPY = midlePY;


                        }


                        spotAnimPoints.add(new SpotAnimPoint(startPX,
                                startPY,
                                midlePX,
                                midlePY

                        ));
                        spotAnimPoints.add(new SpotAnimPoint(midlePX,
                                midlePY,
                                endPX,
                                endPY));
                        //TextViews
                        headingParams.leftMargin = gutter;
                        headingParams.rightMargin = screenWidth - (targetView.getViewRight() - targetView.getViewWidth() / 2) + extramargin;
                        headingParams.bottomMargin = screenHeight - (int) endPY + spaceAboveLine;
                        headingParams.topMargin = extramargin;
                        headingParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                        headingTv.setGravity(Gravity.LEFT);


                        subHeadingParams.rightMargin = screenWidth - (targetView.getViewRight() - targetView.getViewWidth() / 2) + extramargin;
                        subHeadingParams.leftMargin = gutter;
                        subHeadingParams.bottomMargin = extramargin;
                        subHeadingParams.topMargin = targetView.getViewTop() / 2 + spaceBelowLine;
                        subHeadingParams.gravity = Gravity.LEFT;
                        subHeadingTv.setGravity(Gravity.LEFT);

                    } else {//left

                        startPX = (targetView.getViewRight() - targetView.getViewWidth() / 2);
                        startPY = targetView.getPoint().y - (extraPaddingForArc) - arrowMargin;

                        midlePX = startPX;
                        midlePY = startPY - shiftValue * 4;

                        endPX = startPX + (int) (shiftValue * 1.5);
                        endPY = midlePY;


                        if (startPX > screenWidth || startPX < 0) {

                            startPX = targetView.getViewWidth() / 2 + targetView.getViewLeft();
                            startPY = targetView.getViewTop() - shiftValue;

                            midlePX = startPX;
                            midlePY = startPY - shiftValue * 2;

                            endPX = midlePX - shiftValue;
                            endPY = midlePY;


                        }

                        spotAnimPoints.add(new SpotAnimPoint(startPX,
                                startPY,
                                midlePX,
                                midlePY

                        ));
                        spotAnimPoints.add(new SpotAnimPoint(midlePX,
                                midlePY,
                                endPX,
                                endPY));


                        //TextViews
                        headingParams.rightMargin = gutter;
                        headingParams.leftMargin = targetView.getPoint().x;
                        headingParams.bottomMargin = screenHeight - (int) endPY + spaceAboveLine;
                        headingParams.topMargin = extramargin;
                        headingParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                        headingTv.setGravity(Gravity.RIGHT);


                        subHeadingParams.rightMargin = gutter;
                        subHeadingParams.leftMargin = (targetView.getViewRight() - targetView.getViewWidth() / 2) + extramargin;
                        subHeadingParams.topMargin = targetView.getViewTop() / 2 + spaceBelowLine;
                        subHeadingParams.bottomMargin = extramargin;
                        subHeadingParams.gravity = Gravity.RIGHT;
                        subHeadingTv.setGravity(Gravity.LEFT);

                    }

            } else {//top

                    if (targetView.getPoint().x > screenWidth / 2) {//Right

                        startPX = (targetView.getViewLeft()) - arrowMargin ;
                        startPY = getMiddleOfViewY();


                        midlePX = startPX - shiftValue * 2;
                        midlePY = startPY;

                        endPX = midlePX;
                        endPY = startPY + (int) (1.5 * shiftValue);

                        if (startPX > screenWidth || startPX < 0) {

                            startPX = targetView.getViewWidth() / 2 + targetView.getViewLeft();
                            startPY = targetView.getViewBottom() + shiftValue;

                            midlePX = startPX;
                            midlePY = startPY + shiftValue * 2;

                            endPX = midlePX + shiftValue;
                            endPY = midlePY;


                        }

                        spotAnimPoints.add(new SpotAnimPoint(startPX,
                                startPY,
                                midlePX,
                                midlePY

                        ));
                        spotAnimPoints.add(new SpotAnimPoint(midlePX,
                                midlePY,
                                endPX,
                                endPY));


//                //TextViews
                        //headingTv.setBackgroundColor(Color.RED);
                        headingParams.leftMargin = gutter;
                        headingParams.rightMargin = screenWidth - targetView.getViewRight() + targetView.getViewWidth() / 2;
                        //headingParams.bottomMargin = screenHeight - ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceAboveLine;
                        headingParams.topMargin = (int) endPY + spaceBelowLine;
                        headingParams.gravity = Gravity.TOP | Gravity.RIGHT;
                        headingTv.setGravity(Gravity.RIGHT);

                        subHeadingParams.leftMargin = gutter;
                        subHeadingParams.rightMargin = screenWidth - targetView.getViewRight() + targetView.getViewWidth() / 2 + extramargin;
                        subHeadingParams.bottomMargin = extramargin;
                        subHeadingParams.topMargin = ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceBelowLine;
                        subHeadingParams.gravity = Gravity.LEFT;
                        subHeadingTv.setGravity(Gravity.LEFT);


                    } else {//left


                        startPX = (targetView.getViewRight()) + extraPaddingForArc + arrowMargin + radius;
                        startPY = getMiddleOfViewY();


                        midlePX = startPX + shiftValue * 2;
                        midlePY = startPY;

                        endPX = midlePX;
                        endPY = startPY + (int) (1.5 * shiftValue);

                        if (startPX > screenWidth || startPX < 0) {

                            startPX = targetView.getViewWidth() / 2 + targetView.getViewLeft();
                            startPY = targetView.getViewBottom() + shiftValue;

                            midlePX = startPX;
                            midlePY = startPY + shiftValue * 2;

                            endPX = midlePX + shiftValue;
                            endPY = midlePY;


                        }


                        spotAnimPoints.add(new SpotAnimPoint(startPX,
                                startPY,
                                midlePX,
                                midlePY

                        ));
                        spotAnimPoints.add(new SpotAnimPoint(midlePX,
                                midlePY,
                                endPX,
                                endPY));


//                //TextViews
                        headingParams.leftMargin = targetView.getViewRight() - targetView.getViewWidth() / 2 + extramargin;
                        headingParams.rightMargin = gutter;
                        //headingParams.bottomMargin = screenHeight - ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceAboveLine;
                        headingParams.topMargin = (int) endPY + spaceBelowLine;
                        headingParams.gravity = Gravity.TOP | Gravity.RIGHT;
                        headingTv.setGravity(Gravity.RIGHT);

                        subHeadingParams.leftMargin = targetView.getViewRight() - targetView.getViewWidth() / 2 + extramargin;
                        subHeadingParams.rightMargin = gutter;
                        subHeadingParams.bottomMargin = extramargin;
                        subHeadingParams.topMargin = ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceBelowLine;
                        subHeadingParams.gravity = Gravity.RIGHT;
                        subHeadingTv.setGravity(Gravity.LEFT);
                    }

            }
        }

        addView(headingTv, headingParams);
        addView(subHeadingTv, subHeadingParams);
        if(viewsForTargets.size()>1&&circles.get(0).isCircle){
            spotAnimPoints.remove(0);
            if (targetView.getPoint().x > screenWidth / 2){
                spotAnimPoints.get(0).curX -=100;
            }else{
                spotAnimPoints.get(0).curX +=100;
            }
        }
        Locale l =  getResources().getConfiguration().locale;

        headingTv.setTextDirection(l.toString().contains("ar")?TEXT_DIRECTION_RTL:TEXT_DIRECTION_LTR);

        return spotAnimPoints;
    }


    private float getMiddleOfViewY(){

        float res = (targetView.getViewBottom() - targetView.getViewTop());

        res /= 2;



        return res + targetView.getViewTop();
    }

    private float getMiddleOfViewX(){

        return getWidth()/2;
    }

    /**
     * Remove the spotlight view
     */
    private void removeSpotlightView(boolean fromCloseButton) {
        if (listener != null)
            listener.onUserClicked(usageId,fromCloseButton);

        if (getParent() != null)
            ((ViewGroup) getParent()).removeView(this);
    }


    /**
     * Setters
     */

    public void setListener(SpotlightListener listener) {
        this.listener = listener;
    }

    private void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }


    private void setBackgroundBitmap(Bitmap bitmap) {
        this.backgroundBitmap = bitmap;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void setDismissOnTouch(boolean dismissOnTouch) {
        this.dismissOnTouch = dismissOnTouch;
    }

    public void setDismissOnBackPress(boolean dismissOnBackPress) {
        this.dismissOnBackPress = dismissOnBackPress;
    }

    public void setPerformClick(boolean performClick) {
        isPerformClick = performClick;
    }

    public void setExtraPaddingForArc(int extraPaddingForArc) {
        this.extraPaddingForArc = extraPaddingForArc;
    }

    /**
     * Whether to show the arc under/above the circular target overlay.
     *
     * @param show Set to true to show the arc line, false otherwise.
     */
    public void setShowTargetArc(boolean show) {
        this.showTargetArc = show;
    }

    public void setIntroAnimationDuration(long introAnimationDuration) {
        this.introAnimationDuration = introAnimationDuration;
    }


    public void setRevealAnimationEnabled(boolean revealAnimationEnabled) {
        isRevealAnimationEnabled = revealAnimationEnabled;
    }

    public void setFadingTextDuration(long fadingTextDuration) {
        this.fadingTextDuration = fadingTextDuration;
    }

    public void setCircleShape(Circle circleShape) {
        this.circleShape = circleShape;
    }

    public void setTargetView(Target targetView) {
        this.targetView = targetView;
    }

    public void setUsageId(String usageId) {
        this.usageId = usageId;
    }

    public void setHeadingTvSize(int headingTvSize) {
        this.headingTvSize = headingTvSize;
    }

    public void setHeadingTvColor(int headingTvColor) {
        this.headingTvColor = headingTvColor;
    }

    public void setHeadingTvText(CharSequence headingTvText) {
        this.headingTvText = headingTvText;
    }

    public void setSubHeadingTvSize(int subHeadingTvSize) {
        this.subHeadingTvSize = subHeadingTvSize;
    }

    public void setSubHeadingTvColor(int subHeadingTvColor) {
        this.subHeadingTvColor = subHeadingTvColor;
    }

    public void setSubHeadingTvText(CharSequence subHeadingTvText) {
        this.subHeadingTvText = subHeadingTvText;
    }

    public void setLineAnimationDuration(long lineAnimationDuration) {
        this.lineAnimationDuration = lineAnimationDuration;
    }

    public void setLineAndArcColor(int lineAndArcColor) {
        this.lineAndArcColor = lineAndArcColor;
    }

    public void setLineStroke(int lineStroke) {
        this.lineStroke = lineStroke;
    }

    public void setLineEffect(PathEffect pathEffect) {
        this.lineEffect = pathEffect;
    }

    public void setTypeface(Typeface typeface) {
        this.mTypeface = typeface;
    }

    public void setConfiguration(SpotlightConfig configuration) {

        if (configuration != null) {
            this.maskColor = configuration.getMaskColor();
            this.introAnimationDuration = configuration.getIntroAnimationDuration();
            this.isRevealAnimationEnabled = configuration.isRevealAnimationEnabled();
            this.fadingTextDuration = configuration.getFadingTextDuration();
            this.padding = configuration.getPadding();
            this.dismissOnTouch = configuration.isDismissOnTouch();
            this.dismissOnBackPress = configuration.isDismissOnBackpress();
            this.isPerformClick = configuration.isPerformClick();
            this.headingTvSize = configuration.getHeadingTvSize();
            this.headingTvColor = configuration.getHeadingTvColor();
            this.headingTvText = configuration.getHeadingTvText();
            this.subHeadingTvSize = configuration.getSubHeadingTvSize();
            this.subHeadingTvColor = configuration.getSubHeadingTvColor();
            this.subHeadingTvText = configuration.getSubHeadingTvText();
            this.lineAnimationDuration = configuration.getLineAnimationDuration();
            this.lineStroke = configuration.getLineStroke();
            this.lineAndArcColor = configuration.getLineAndArcColor();
        }
    }



    public void setViews(ArrayList<View> views) {
        this.viewsForTargets = views;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    /**
     * Builder Class
     */
    public static class Builder {

        private SpotlightView spotlightView;

        private Activity activity;
        private boolean isCircle = true;
        private boolean isBrick = true;


        public Builder(Activity activity) {
            this.activity = activity;
            spotlightView = new SpotlightView(activity);
        }

        public Builder setCircleView(boolean isCircle) {
            this.isCircle = isCircle;
            return this;
        }

        public Builder setBrickView(boolean isBrick) {
            this.isBrick = isBrick;
            return this;
        }

        public Builder maskColor(int maskColor) {
            spotlightView.setMaskColor(maskColor);
            return this;
        }

        public Builder introAnimationDuration(long delayMillis) {
            spotlightView.setIntroAnimationDuration(delayMillis);
            return this;
        }

        public Builder enableRevealAnimation(boolean isFadeAnimationEnabled) {
            spotlightView.setRevealAnimationEnabled(isFadeAnimationEnabled);
            return this;
        }


        public Builder target(View view) {
            spotlightView.setTargetView(new ViewTarget(view));
            return this;
        }

        public Builder targetPadding(int padding) {
            spotlightView.setPadding(padding);
            return this;
        }


        public Builder dismissOnTouch(boolean dismissOnTouch) {
            spotlightView.setDismissOnTouch(dismissOnTouch);
            return this;
        }

        public Builder dismissOnBackPress(boolean dismissOnBackPress) {
            spotlightView.setDismissOnBackPress(dismissOnBackPress);
            return this;
        }

        public Builder usageId(String usageId) {
            spotlightView.setUsageId(usageId);
            return this;
        }

        public Builder setTypeface(Typeface typeface) {
            spotlightView.setTypeface(typeface);
            return this;
        }

        public Builder setListener(SpotlightListener spotlightListener) {
            spotlightView.setListener(spotlightListener);
            return this;
        }

        public Builder performClick(boolean isPerformClick) {
            spotlightView.setPerformClick(isPerformClick);
            return this;
        }


        public Builder fadeinTextDuration(long fadinTextDuration) {
            spotlightView.setFadingTextDuration(fadinTextDuration);
            return this;
        }

        public Builder headingTvSize(int headingTvSize) {
            spotlightView.setHeadingTvSize(headingTvSize);
            return this;
        }

        public Builder headingTvColor(int color) {
            spotlightView.setHeadingTvColor(color);
            return this;
        }

        public Builder setViews(ArrayList<View> views) {
            spotlightView.viewsForTargets = views;
            return this;
        }

        public Builder headingTvText(CharSequence text) {
            spotlightView.setHeadingTvText(text);
            return this;
        }

        public Builder subHeadingTvSize(int headingTvSize) {
            spotlightView.setSubHeadingTvSize(headingTvSize);
            return this;
        }

        public Builder subHeadingTvColor(int color) {
            spotlightView.setSubHeadingTvColor(color);
            return this;
        }

        public Builder subHeadingTvText(CharSequence text) {
            spotlightView.setSubHeadingTvText(text);
            return this;
        }

        public Builder lineAndArcColor(int color) {
            spotlightView.setLineAndArcColor(color);
            return this;
        }

        public Builder lineAnimDuration(long duration) {
            spotlightView.setLineAnimationDuration(duration);
            return this;
        }

        public Builder showTargetArc(boolean show) {
            spotlightView.setShowTargetArc(show);
            return this;
        }

        public Builder enableDismissAfterShown(boolean enable) {
            if (enable) {
                spotlightView.setDismissOnTouch(false);
            }
            return this;
        }

        public Builder setBackgroundColor(String hexColor){

            spotlightView.setMaskColor(Color.parseColor(hexColor));
            return this;
        }

        public Builder setBackgroundBitmap(Bitmap bitmap){
            spotlightView.setBackgroundBitmap(bitmap);
            return this;
        }

        public Builder setIsTargetCircleView(Boolean isTargetCircleView){

            spotlightView.isCircleTargetView = isTargetCircleView;
            return this;
        }

        public Builder setHeightOfView(int heightOfView){

            spotlightView.heightOfView = heightOfView;
            return this;
        }

        public Builder setAdditionalMask(int eraserMaskX,int eraserMaskY,int eraserMaskRadius){

            spotlightView.eraserMaskX = eraserMaskX;
            spotlightView.eraserMaskY = eraserMaskY;
            spotlightView.eraserMaskRadius = eraserMaskRadius;
            return this;
        }


        public Builder setArrowMarginStart(int start){

            spotlightView.arrowMargin = Utils.dpToPx(start);
            return this;
        }

        public Builder setArrowYpx(float y){

            spotlightView.yOfArrow = y;
            return this;
        }

        public Builder setStraigth(boolean straigth){

            spotlightView.isStraigth = straigth;
            return this;
        }

        public Builder lineStroke(double stroke) {
            spotlightView.setLineStroke(Utils.dpToPx(stroke));
            return this;
        }

        public Builder lineEffect(@Nullable PathEffect pathEffect) {
            spotlightView.setLineEffect(pathEffect);
            return this;
        }

        public Builder setConfiguration(SpotlightConfig configuration) {
            spotlightView.setConfiguration(configuration);
            return this;
        }

        public Builder setCloseButtonSettings(int size,int margin_top,int margin_end,int margin_start,Drawable d){

            spotlightView.closeButtonSettings = new CloseButtonConfig(size,margin_top,margin_end,margin_start,d);
            return this;
        }

        public Builder considerStatusBarHeightInCloseButton(boolean consider){
            spotlightView.considerStatusBarHeightInCloseButton = consider;
            return this;
        }

        public Builder setArrowSize(int arrowSizeDp){

            spotlightView.arrowSize = Utils.dpToPx(arrowSizeDp);
            return this;
        }

        public Builder setLineSize(int linesize){

            spotlightView.shiftValue = linesize;
            return this;
        }

        public Builder setStraightVertical(boolean straightVertical){

            spotlightView.straigthVertical = straightVertical;
            return this;
        }

        public Builder setArabic(boolean isArabic){

            spotlightView.isArabic = isArabic;
            return this;
        }

        public Builder isBackgroundFullScreen(boolean isBackgroundFullScreen) {
            spotlightView.isBackgroundFullScreen = isBackgroundFullScreen;
            return this;
        }

        public Builder setExtraPaddingForArc(int paddingForArc) {
            spotlightView.extraPaddingForArc = paddingForArc;
            return this;
        }


        public SpotlightView build() {

            if(spotlightView.viewsForTargets!=null){
                boolean setMain = false;
                spotlightView.circles.clear();
                for(View v : spotlightView.viewsForTargets){
                    Circle circle = new Circle(
                            new ViewTarget(v),
                            spotlightView.padding);
                    if(!setMain){
                        setMain = true;
                        spotlightView.setCircleShape(circle.setCircle(isCircle).setBrick(isBrick).setMask(spotlightView.eraserMaskX,spotlightView.eraserMaskY,spotlightView.eraserMaskRadius));
                    }
                    spotlightView.circles.add(circle.setCircle(isCircle).setBrick(isBrick).setMask(spotlightView.eraserMaskX,spotlightView.eraserMaskY,spotlightView.eraserMaskRadius));
                }
            }else if (spotlightView.rect != null){

            }else {
                Circle circle = new Circle(
                        spotlightView.targetView,
                        spotlightView.padding);
                spotlightView.setCircleShape(circle.setCircle(isCircle).setBrick(isBrick).setMask(spotlightView.eraserMaskX,spotlightView.eraserMaskY,spotlightView.eraserMaskRadius));
            }
            if (spotlightView.dismissOnBackPress) {
                spotlightView.enableDismissOnBackPress();
            }
            return spotlightView;
        }

        public SpotlightView show() {
            build().show(activity);
            return spotlightView;
        }





    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (dismissOnBackPress) {
            if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                dismiss();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void logger(String s) {
        Log.d("Spotlight", s);
    }
}
