package com.android.deskclock.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.deskclock.R;
import com.google.android.material.color.MaterialColors;

import java.io.Serializable;

import static com.android.deskclock.FabContainer.FAB_SHRINK_AND_EXPAND;

public class CircularTimerSetupView extends FrameLayout {
    private final Button topButton;
    private final Button bottomButton;
    private final Paint arcPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint knobPaint = new Paint();
    private final RectF arcBounds = new RectF();
    private final float textSize = 80;

    private boolean isNegative = false;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private TimerFragment mFabContainer;
    private boolean isEditable = true;

    public CircularTimerSetupView(Context context) {
        this(context, null /* attrs */);
    }

    public CircularTimerSetupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setAntiAlias(true);
        textPaint.setColor(0xffffffff);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textSize);

        knobPaint.setAntiAlias(true);
        knobPaint.setColor(MaterialColors.getColor(getContext(), R.attr.colorAccent, Color.WHITE));

        View.inflate(getContext(), R.layout.circular_timer_setup_view_buttons, this);
        bottomButton = findViewById(R.id.timer_drawer_bottom_button);
        topButton = findViewById(R.id.timer_drawer_top_button);
        setWillNotDraw(false);

        setupBottomButton("+ 15s", v -> setTime(getTimeInMillis() + 15000));
    }

    public void setupBottomButton(String text, View.OnClickListener onClickListener) {
        bottomButton.setText(text);
        bottomButton.setOnClickListener(onClickListener);
        bottomButton.setVisibility(View.VISIBLE);
    }

    public void setupTopButton(String text, View.OnClickListener onClickListener) {
        topButton.setText(text);
        topButton.setOnClickListener(onClickListener);
        topButton.setVisibility(View.VISIBLE);
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

        for (int i = 0; i < 60; i++) {
            float innerMultiplier = (i % 15 == 0) ? 0.86f : (i % 5 == 0) ? 0.90f : 0.96f;
            Point p1 = radToPoint(i * 360.f/60.f, radius * innerMultiplier);
            float outerMultiplier = (i == seconds && !isNegative) ? 1.03f : 0.98f;
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

        canvas.drawText(String.format("%s%02d:%02d:%02d", isNegative ? "-" : "", hours, minutes, seconds),
                getWidth() / 2,  getHeight() / 2 + textSize / 3, textPaint);

        if (isEditable) {
            Point p = radToPoint(angle, radius);
            canvas.drawCircle(p.x, p.y, 40, knobPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEditable) {
            return super.onTouchEvent(event);
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        double angleRad = Math.atan2(center.y - event.getY(), center.x - event.getX());
        float angle = (float) (angleRad * (180 / Math.PI));
        angle += 360 + 360 - 90;
        angle %= 360;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float knobAngle = ((float) minutes / 60.0f) * 360.0f;
            //return Math.abs(angle - knobAngle) < 10;
            float difference = Math.abs(angle - knobAngle);
            return difference < 15 || difference > (360 - 15);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int newMinutes = (int) (60 * (angle / 360.0));
            boolean hasValidInputBefore = hasValidInput();
            if (minutes > 45 && newMinutes < 15) {
                hours++;
                minutes = newMinutes;
            } else if (minutes < 15 && newMinutes > 45) {
                if (hours > 0) {
                    hours--;
                    minutes = newMinutes;
                }
            } else {
                minutes = newMinutes;
            }
            if (hours == 0 && minutes == 0) {
                seconds = 0;
            }
            if (hasValidInputBefore != hasValidInput() && mFabContainer != null) {
                mFabContainer.updateFab(FAB_SHRINK_AND_EXPAND);
            }
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    protected Point radToPoint(float angle, float radius) {
        return new Point((int) (getWidth()/2 + radius * Math.sin(-angle * Math.PI / 180 + Math.PI)),
                (int) (getHeight()/2 + radius * Math.cos(-angle* Math.PI / 180 + Math.PI)));
    }

    public void setTime(long remainingTime) {
        boolean hasValidInputBefore = hasValidInput();
        isNegative = remainingTime < 0;
        remainingTime = Math.abs(remainingTime);
        hours = (int) ((remainingTime / 1000) / 60 / 60);
        minutes = (int) ((remainingTime / 1000 / 60) % 60);
        seconds = (int) ((remainingTime / 1000) % 60);
        invalidate();
        if (hasValidInputBefore != hasValidInput() && mFabContainer != null) {
            mFabContainer.updateFab(FAB_SHRINK_AND_EXPAND);
        }
    }

    public void setFabContainer(TimerFragment timerFragment) {
        this.mFabContainer = timerFragment;
    }

    public void setState(Serializable ignored) {

    }

    public Serializable getState() {
        return null;
    }

    public boolean hasValidInput() {
        return hours + minutes + seconds != 0;
    }

    public long getTimeInMillis() {
        return seconds * 1000L + minutes * 1000L * 60L + hours * 1000L * 60L * 60L;
    }

    public void reset() {
        hours = minutes = seconds = 0;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        if (isEditable) {

        }
    }
}