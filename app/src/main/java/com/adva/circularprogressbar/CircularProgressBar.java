package com.adva.circularprogressbar;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

/**
 * Created by T530 on 02/10/2014.
 */
public class CircularProgressBar extends ProgressBar {
    private static final String TAG = "CircularProgressBar";

    private static final int STROKE_WIDTH = 5;

    private String mTitle = "";

    private int mStrokeWidth = STROKE_WIDTH;

    private final RectF mCircleBounds = new RectF();

    private final Paint mProgressColorPaint = new Paint();
    private final Paint mBackgroundColorPaint = new Paint();
    private final Paint mTitlePaint = new Paint();


    public interface ProgressAnimationListener{
        public void onAnimationStart();
        public void onAnimationFinish();
        public void onAnimationProgress(int progress);
    }

    public CircularProgressBar(Context context) {
        super(context);
        init(null, 0);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void init(AttributeSet attrs, int style){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        Resources res = getResources();

        mProgressColorPaint.setColor(res.getColor(R.color.circular_progress_default_progress));

        mBackgroundColorPaint.setColor(res.getColor(R.color.circular_progress_default_background));

        mTitlePaint.setColor(res.getColor(R.color.circular_progress_default_title));

        mStrokeWidth = STROKE_WIDTH;


        mProgressColorPaint.setAntiAlias(true);
        mProgressColorPaint.setStyle(Paint.Style.STROKE);
        mProgressColorPaint.setStrokeWidth(mStrokeWidth);

        mBackgroundColorPaint.setAntiAlias(true);
        mBackgroundColorPaint.setStyle(Paint.Style.STROKE);
        mBackgroundColorPaint.setStrokeWidth(mStrokeWidth);

        mTitlePaint.setTextSize(24);
        mTitlePaint.setStyle(Paint.Style.FILL);
        mTitlePaint.setAntiAlias(true);
        mTitlePaint.setShadowLayer(0.1f, 0, 1, Color.GRAY);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.drawArc(mCircleBounds, 0, 360 , false, mBackgroundColorPaint);

        int prog = getProgress();
        float scale = getMax() > 0 ? (float)prog/getMax() *360: 0;

        canvas.drawArc(mCircleBounds, 270, scale , false, mProgressColorPaint);


        if(!TextUtils.isEmpty(mTitle)){
            Rect bounds = new Rect();
            mTitlePaint.getTextBounds(mTitle, 0, mTitle.length(), bounds);
            int height = bounds.height();

            int xPos =  (int)(getMeasuredWidth()/2 - mTitlePaint.measureText(mTitle) / 2);
            int yPos = (int) (getMeasuredHeight()/2 + height/2);

            canvas.drawText(mTitle, xPos, yPos, mTitlePaint);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int min = Math.min(width, height);
        setMeasuredDimension(min+2*STROKE_WIDTH, min+2*STROKE_WIDTH);

        mCircleBounds.set(STROKE_WIDTH, STROKE_WIDTH, min+STROKE_WIDTH, min+STROKE_WIDTH);
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);

        // the setProgress super will not change the details of the progress bar
        // anymore so we need to force an update to redraw the progress bar
        invalidate();
    }

    public void animateProgressTo(final long duration, final int start, final int end, final ProgressAnimationListener listener){
        if(start!=0)
            setProgress(start);

        final ObjectAnimator progressBarAnimator = ObjectAnimator.ofFloat(this, "animateProgress", start, end);
        progressBarAnimator.setDuration(duration);
        //		progressBarAnimator.setInterpolator(new AnticipateOvershootInterpolator(2f, 1.5f));
        progressBarAnimator.setInterpolator(new LinearInterpolator());

        progressBarAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                CircularProgressBar.this.setProgress(end);
                if(listener!=null)
                    listener.onAnimationFinish();
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
                if(listener!=null)
                    listener.onAnimationStart();
            }
        });

        progressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                int progress = ((Float) animation.getAnimatedValue()).intValue();
                if(progress!=CircularProgressBar.this.getProgress()){
                    Log.d(TAG, progress + "");
                    CircularProgressBar.this.setProgress(progress);
                    if(listener!=null)
                        listener.onAnimationProgress(progress);
                }
            }
        });
        progressBarAnimator.start();
    }

    public synchronized void setTitle(String title){
        this.mTitle = title;
        invalidate();
    }

    public synchronized void setTitleColor(int color){
        mTitlePaint.setColor(color);
        invalidate();
    }

    public String getTitle(){
        return mTitle;
    }
}
