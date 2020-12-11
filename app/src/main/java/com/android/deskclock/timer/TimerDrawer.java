package com.android.deskclock.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.deskclock.R;

public class TimerDrawer extends FrameLayout {
    Button button;
    private final Paint arcPaint = new Paint();
    private final Paint textPaint = new Paint();

    private RectF arcBounds = new RectF();

    protected boolean isNegative = false;
    protected int hours = 0;
    protected int minutes = 0;
    protected int seconds = 0;
    private boolean hideCircle = false;

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

        View.inflate(getContext(), R.layout.timer_drawer_button, this);
        button = findViewById(R.id.timer_drawer_button);
        setWillNotDraw(false);
    }

    public void setButtonText(String text) {
        button.setText(text);
        button.setVisibility(View.VISIBLE);
    }

    public void setButtonAction(View.OnClickListener onClickListener) {
        button.setOnClickListener(onClickListener);
        button.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
                && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        } else if (MeasureSpec.getSize(widthMeasureSpec) < MeasureSpec.getSize(heightMeasureSpec)) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        }
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

        if (!hideCircle) {
            for (int i = 0; i < 60; i++) {
                float innerMultiplier = (i % 15 == 0) ? 0.86f : (i % 5 == 0) ? 0.90f : 0.96f;
                Point p1 = radToPoint(i * 360.f/60.f, radius * innerMultiplier);
                float outerMultiplier = (i == seconds && !isNegative) ? 1.03f : 0.98f;
                Point p2 = radToPoint(i * 360.f/60.f, radius * outerMultiplier);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, arcPaint);
            }
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

        canvas.drawText(String.format("%s%02d:%02d:%02d", isNegative ? "-" : "", hours, minutes, seconds),
                getWidth() / 2,  getHeight() / 2, textPaint);
    }

    protected Point radToPoint(float angle, float radius) {
        return new Point((int) (getWidth()/2 + radius * Math.sin(-angle * Math.PI / 180 + Math.PI)),
                (int) (getHeight()/2 + radius * Math.cos(-angle* Math.PI / 180 + Math.PI)));
    }

    public void setTime(long remainingTime) {
        isNegative = remainingTime < 0;
        remainingTime = Math.abs(remainingTime);
        hours = (int) ((remainingTime / 1000) / 60 / 60);
        minutes = (int) ((remainingTime / 1000 / 60) % 60);
        seconds = (int) ((remainingTime / 1000) % 60);
        invalidate();
    }

    public void setBlinkVisibility(boolean hideCircle) {
        this.hideCircle = hideCircle;
    }
}
