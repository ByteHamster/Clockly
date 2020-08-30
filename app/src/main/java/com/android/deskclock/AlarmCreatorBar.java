/*
 * Copyright (c) 2021 ByteHamster.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.deskclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AlarmCreatorBar extends View {
    private final Paint linesPaint = new Paint();
    private final Paint tintPaint = new Paint();
    private final Paint textPaint = new Paint();
    private float fontSize = 60;
    private Point touchDown = new Point();
    private boolean isTouching = false;

    private boolean setupRunning = false;
    private int hours = 0;
    private int minutes = 0;

    public AlarmCreatorBar(Context context) {
        this(context, null /* attrs */);
    }

    public AlarmCreatorBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        linesPaint.setAntiAlias(true);
        linesPaint.setColor(0xccffffff);
        linesPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linesPaint.setStrokeCap(Paint.Cap.ROUND);

        tintPaint.setColor(0x55000000);
        linesPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint.setAntiAlias(true);
        textPaint.setColor(0xffffffff);
        textPaint.setTextSize(fontSize);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < 24; i++) {
            int y = (int) ((i + 0.5f) * getHeight() / 24.0);
            canvas.drawLine(0, y, 30, y, linesPaint);
        }

        if (setupRunning) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), tintPaint);
            int y = (int) timeToPos(hours, minutes);

            canvas.drawRect(0, y - 2 * fontSize, getWidth(), y + fontSize, tintPaint);
            canvas.drawText(String.format("%02d:%02d", hours, minutes), 80, y, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float delta = (float) Math.sqrt(Math.pow(touchDown.x - event.getX(), 2)
                + Math.pow(touchDown.y - event.getY(), 2));
        if (delta > 80) {
            isTouching = false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDown.x = (int) event.getX();
            touchDown.y = (int) event.getY();
            isTouching = true;
            if (event.getX() < 60) {
                setupRunning = true;
                isTouching = false;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (setupRunning) {
                if (isTouching) {
                    // Tapping modify
                    if (event.getY() > timeToPos(hours, minutes)) {
                        minutes += 5;
                    } else {
                        minutes -= 5;
                    }
                    if (minutes < 0) {
                        minutes += 60;
                        hours--;
                    } else if (minutes >= 60) {
                        minutes -= 60;
                        hours++;
                    }
                }
            }
        } else if (setupRunning && !isTouching) {
            getParent().requestDisallowInterceptTouchEvent(true);
            hours = posToTime(event.getY())[0];
            minutes = posToTime(event.getY())[1];
        }

        if (setupRunning) {
            invalidate();
            return true;
        }

        return super.onTouchEvent(event);
    }

    private int[] posToTime(float pos) {
        float touchHour = 24.0f * (pos / getHeight()) + 0.5f;
        return new int[]{(int) touchHour, (int) (30 * Math.floor((touchHour % 1) * 2.0f))};
    }

    private float timeToPos(int hours, int minutes) {
        return (float) ((hours + minutes / 60.f) * getHeight() / 24.0);
    }

    public void stopSetup() {
        setupRunning = false;
        invalidate();
    }

    public boolean isSetupRunning() {
        return setupRunning;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }
}
