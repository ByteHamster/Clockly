/*
 * Copyright (C) 2015 The Android Open Source Project
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
import android.os.SystemClock;
import android.view.View;
import androidx.core.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.deskclock.R;
import com.android.deskclock.ThemeUtils;
import com.android.deskclock.TimerTextController;
import com.android.deskclock.Utils.ClickAccessibilityDelegate;
import com.android.deskclock.data.Timer;

import static android.R.attr.state_activated;
import static android.R.attr.state_pressed;

/**
 * This view is a visual representation of a {@link Timer}.
 */
public class TimerItem extends LinearLayout {

    /**
     * The last state of the timer that was rendered; used to avoid expensive operations.
     */
    private Timer.State mLastState;
    private TimerDrawer timerDrawer;

    public TimerItem(Context context) {
        this(context, null);
    }

    public TimerItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        timerDrawer = (TimerDrawer) findViewById(R.id.timer_drawer);
    }

    /**
     * Updates this view to display the latest state of the {@code timer}.
     */
    void update(Timer timer) {
        // Update the time.
        timerDrawer.setTime(timer.getRemainingTime());

        // Update visibility of things that may blink.
        final boolean blinkOff = SystemClock.elapsedRealtime() % 1000 < 500;
        if (timerDrawer != null) {
            final boolean hideCircle = (timer.isExpired() || timer.isMissed()) && blinkOff;
            timerDrawer.setBlinkVisibility(hideCircle);
        }

        // Update some potentially expensive areas of the user interface only on state changes.
        if (timer.getState() != mLastState) {
            mLastState = timer.getState();
            switch (mLastState) {
                case RESET:
                case PAUSED:
                    timerDrawer.setButtonText(getContext().getString(R.string.timer_reset));
                    break;
                case EXPIRED:
                case MISSED:
                case RUNNING:
                    timerDrawer.setButtonText(getContext().getString(R.string.timer_add_minute));
                    break;
            }
        }
    }

    public void setButtonAction(View.OnClickListener onClickListener) {
        timerDrawer.setButtonAction(onClickListener);
    }

    public void setButtonText(String text) {
        timerDrawer.setButtonText(text);
    }
}
