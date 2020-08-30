/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock.timer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import androidx.annotation.IdRes;
import androidx.core.view.ViewCompat;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.deskclock.FabContainer;
import com.android.deskclock.FormattedTextUtils;
import com.android.deskclock.R;
import com.android.deskclock.ThemeUtils;
import com.android.deskclock.uidata.UiDataModel;

import java.io.Serializable;
import java.util.Arrays;

import static com.android.deskclock.FabContainer.FAB_REQUEST_FOCUS;
import static com.android.deskclock.FabContainer.FAB_SHRINK_AND_EXPAND;

public class TimerSetupView extends View {
    private int mInputPointer = -1;
    private final Paint arcPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint knobPaint = new Paint();

    private RectF arcBounds = new RectF();
    private boolean validValue = false;

    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;

    /** Updates to the fab are requested via this container. */
    private FabContainer mFabContainer;

    public TimerSetupView(Context context) {
        this(context, null /* attrs */);
    }

    public TimerSetupView(Context context, AttributeSet attrs) {
        super(context, attrs);

        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setAntiAlias(true);
        textPaint.setColor(0xffffffff);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(80);

        knobPaint.setAntiAlias(true);
        knobPaint.setColor(0xffffffff);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateTime();
    }

    public void setFabContainer(FabContainer fabContainer) {
        mFabContainer = fabContainer;
    }

    private void updateTime() {
        seconds = Math.max(0, seconds);
        while (seconds > 60) {
            seconds -= 60;
            minutes++;
        }
        minutes = Math.max(0, minutes);
        while (minutes > 60) {
            minutes -= 60;
            hours++;
        }
        hours = Math.max(0, hours);

        if (hasValidInput() != validValue && mFabContainer != null) {
            validValue = hasValidInput();
            mFabContainer.updateFab(FAB_SHRINK_AND_EXPAND);
        }
        invalidate();
    }

    public void reset() {
        if (mInputPointer != -1) {
            hours = 0;
            minutes = 0;
            seconds = 0;
            mInputPointer = -1;
            updateTime();
        }
    }

    public boolean hasValidInput() {
        return getTimeInMillis() != 0;
    }

    public long getTimeInMillis() {
        return seconds * DateUtils.SECOND_IN_MILLIS
                + minutes * DateUtils.MINUTE_IN_MILLIS
                + hours * DateUtils.HOUR_IN_MILLIS;
    }

    /**
     * @return an opaque representation of the state of timer setup
     */
    public Serializable getState() {
        return new int[] {hours, minutes, seconds};
    }

    /**
     * @param state an opaque state of this view previously produced by {@link #getState()}
     */
    public void setState(Serializable state) {
        final int[] input = (int[]) state;
        if (input != null && input.length == 3) {
            hours = input[0];
            minutes = input[1];
            seconds = input[2];
            updateTime();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = Math.min(getWidth(), getHeight()) - 300;
        arcBounds.set((getWidth() - size) / 2, (getHeight() - size) / 2,
                getWidth() - (getWidth() - size) / 2, getHeight() - (getHeight() - size) / 2);

        arcPaint.setStrokeWidth(3);
        arcPaint.setColor(0xffcccccc);
        canvas.drawArc(arcBounds, 0, 360, false, arcPaint);

        int differenceHourRadius =  30;
        float hourRadiusDelta = 0;

        if (minutes > 50) {
            hourRadiusDelta = (1.0f - 0.1f * (60 - minutes)) * differenceHourRadius;
            arcBounds.left += hourRadiusDelta;
            arcBounds.right -= hourRadiusDelta;
            arcBounds.top += hourRadiusDelta;
            arcBounds.bottom -= hourRadiusDelta;
        }

        arcPaint.setStrokeWidth(15);
        arcPaint.setColor(0xffffffff);
        float angle = ((float) minutes / 60.0f) * 360.0f;
        canvas.drawArc(arcBounds, -90, angle, false, arcPaint);

        float hoursSize = size / 2 - hourRadiusDelta;
        for (int i = 0; i < hours; i++) {
            hoursSize -= differenceHourRadius;
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, hoursSize, arcPaint);
        }

        float knobX = (float) (getWidth()/2 + size/2 * Math.sin(-angle * Math.PI / 180 + Math.PI));
        float knobY = (float) (getHeight()/2 + size/2 * Math.cos(-angle* Math.PI / 180 + Math.PI));
        canvas.drawCircle(knobX, knobY, 30, knobPaint);

        canvas.drawText(String.format("%02d:%02d:%02d", hours, minutes, seconds), getWidth() / 2,  getHeight() / 2, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        double angleRad = Math.atan2(center.y - event.getY(), center.x - event.getX());
        float angle = (float) (angleRad * (180 / Math.PI));
        angle += 360 + 360 - 90;
        angle %= 360;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float knobAngle = ((float) minutes / 60.0f) * 360.0f;
            return Math.abs(angle - knobAngle) < 10;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int newMinutes = (int) (60 * (angle / 360.0));
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
            updateTime();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }
}
