package com.android.deskclock.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TimerDrawer extends View {
    private final Paint arcPaint = new Paint();
    private final Paint textPaint = new Paint();

    private RectF arcBounds = new RectF();

    protected int hours = 0;
    protected int minutes = 0;
    protected int seconds = 0;

    public TimerDrawer(Context context) {
        this(context, null /* attrs */);
    }

    public TimerDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);

        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setAntiAlias(true);
        textPaint.setColor(0xffffffff);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(80);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = Math.min(getWidth(), getHeight()) - 300;
        int radius = size / 2;
        arcBounds.set((getWidth() - size) / 2, (getHeight() - size) / 2,
                getWidth() - (getWidth() - size) / 2, getHeight() - (getHeight() - size) / 2);

        arcPaint.setStrokeWidth(3);
        arcPaint.setColor(0xffaaaaaa);

        for (int i = 0; i < 60; i++) {
            float innerMultiplier = (i % 15 == 0) ? 0.88f : (i % 5 == 0) ? 0.92f : 0.98f;
            Point p1 = radToPoint(i * 360.f/60.f, radius * innerMultiplier);
            float outerMultiplier = (i == seconds) ? 1.05f : 1.0f;
            Point p2 = radToPoint(i * 360.f/60.f, radius * outerMultiplier);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, arcPaint);
        }

        int differenceHourRadius =  20;
        float hourRadiusDelta = 0;

        if (minutes > 55) {
            hourRadiusDelta = (1.0f - 0.2f * (60 - minutes)) * differenceHourRadius;
            arcBounds.left += hourRadiusDelta;
            arcBounds.right -= hourRadiusDelta;
            arcBounds.top += hourRadiusDelta;
            arcBounds.bottom -= hourRadiusDelta;
        }

        arcPaint.setStrokeWidth(8);
        arcPaint.setColor(0xffffffff);
        float angle = ((float) minutes / 60.0f) * 360.0f;
        canvas.drawArc(arcBounds, -90, angle, false, arcPaint);

        int hourColor = 0xffffffff;
        float hoursSize = radius - hourRadiusDelta;
        for (int i = 0; i < hours; i++) {
            hoursSize -= differenceHourRadius;
            arcPaint.setColor(hourColor);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, hoursSize, arcPaint);
            hourColor -= 0x20000000;
            if ((hourColor & 0xf0000000) == 0x10000000) {
                break;
            }
        }

        canvas.drawText(String.format("%02d:%02d:%02d", hours, minutes, seconds), getWidth() / 2,  getHeight() / 2, textPaint);
    }

    protected Point radToPoint(float angle, float radius) {
        return new Point((int) (getWidth()/2 + radius * Math.sin(-angle * Math.PI / 180 + Math.PI)),
                (int) (getHeight()/2 + radius * Math.cos(-angle* Math.PI / 180 + Math.PI)));
    }

    public void setTime(long remainingTime) {
        hours = (int) ((remainingTime / 1000) / 60 / 60);
        minutes = (int) ((remainingTime / 1000 / 60) % 60);
        seconds = (int) ((remainingTime / 1000) % 60);
        invalidate();
    }
}
