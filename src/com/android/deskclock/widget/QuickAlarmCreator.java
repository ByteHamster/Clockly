/*
 * Copyright (c) 2024 ByteHamster.
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

package com.android.deskclock.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.android.deskclock.R;
import com.google.android.material.color.MaterialColors;

import java.util.function.BiConsumer;

public class QuickAlarmCreator extends View {
    private final Paint fabPaint = new Paint();
    private final Paint plusPaint = new Paint();
    private final Paint tintPaint = new Paint();
    private final Paint barPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final float fontSize = 60;
    private final float fontSizePlus = 100;
    private final Point touchDown = new Point();
    private boolean isTouching = false;

    private boolean setupRunning = false;
    private int hours = 0;
    private int minutes = 0;
    private BiConsumer<Integer, Integer> alarmSelectedListener;

    public QuickAlarmCreator(Context context) {
        this(context, null /* attrs */);
    }

    public QuickAlarmCreator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusableInTouchMode(true);

        tintPaint.setColor(0x77000000);
        tintPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        barPaint.setColor(0x44dddddd);
        barPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint.setAntiAlias(true);
        textPaint.setColor(0xffffffff);
        textPaint.setTextSize(fontSize);
        textPaint.setTextAlign(Paint.Align.LEFT);

        fabPaint.setColor(MaterialColors.getColor(context, R.attr.colorAccent, Color.BLACK));
        fabPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        plusPaint.setAntiAlias(true);
        plusPaint.setColor(0xff000000);
        plusPaint.setTextSize(fontSizePlus);
        plusPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (setupRunning) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), tintPaint);
            int y = (int) timeToPos(hours, minutes);

            canvas.drawRect(0, y - 2 * fontSize, getWidth(), y + fontSize, barPaint);
            canvas.drawText(String.format("%02d:%02d", hours, minutes), 80, y, textPaint);
        }

        RectF fabRect = getFabRect();
        canvas.drawOval(fabRect, fabPaint);
        canvas.drawText(setupRunning ? "✓" : "+", fabRect.centerX(), fabRect.centerY() + fontSizePlus / 3, plusPaint);
    }

    private RectF getFabRect() {
        float radius = getWidth() / 10;
        return new RectF(getWidth() / 2 - radius,
                getHeight() - 2.5f * radius,
                getWidth() / 2 + radius,
                getHeight() - 0.5f * radius);
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
            if (getFabRect().contains(event.getX(), event.getY())) {
                if (setupRunning) {
                    alarmSelectedListener.accept(hours, minutes);
                } else {
                    hours = 23;
                    minutes = 55;
                    setupRunning = true;
                    isTouching = false;
                    requestFocus();
                }
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
        } else if (!isTouching) {
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

    public void startSetup(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
        setupRunning = true;
        invalidate();
    }

    public void stopSetup() {
        setupRunning = false;
        clearFocus();
        invalidate();
    }

    public void setAlarmSelectedListener(BiConsumer<Integer, Integer> alarmSelectedListener) {
        this.alarmSelectedListener = alarmSelectedListener;
    }
}