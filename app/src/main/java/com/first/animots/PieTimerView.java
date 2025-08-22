package com.first.animots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public class PieTimerView extends View {
    private Paint paint;
    private Paint backgroundPaint;
    private Paint borderPaint;
    private RectF rect;
    private float sweepAngle = 360f;
    private int currentScoreCenti = 500;

    public PieTimerView(Context context) {
        super(context);
        init();
    }

    public PieTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#FA8A67"));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        rect = new RectF();

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#30FA8A67"));
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#FF8A67"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = 20;
        rect.set(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawOval(rect, backgroundPaint);
        canvas.drawArc(rect, -90, -sweepAngle, true, paint);
        canvas.drawOval(rect, borderPaint);
    }

    public void setProgressFromScore(int scoreCenti) {
        this.currentScoreCenti = scoreCenti;
        this.sweepAngle = 360f * (scoreCenti / 500f);
        invalidate();
    }

    public int getScoreCenti() {
        return currentScoreCenti;
    }

    public String getScoreAsString() {
        return String.format(Locale.getDefault(), "%.2f", currentScoreCenti / 100.0);
    }

}
