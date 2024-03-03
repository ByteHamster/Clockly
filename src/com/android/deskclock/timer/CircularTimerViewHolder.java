package com.android.deskclock.timer;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.android.deskclock.R;
import com.android.deskclock.data.DataModel;
import com.android.deskclock.data.Timer;
import com.android.deskclock.events.Events;

public class CircularTimerViewHolder extends RecyclerView.ViewHolder {

    private int mTimerId;
    private final CircularTimerSetupView mTimerSetupView;
    private final TimerClickHandler mTimerClickHandler;

    public CircularTimerViewHolder(View view, TimerClickHandler timerClickHandler) {
        super(view);
        mTimerSetupView = (CircularTimerSetupView) view;
        mTimerSetupView.setEditable(false);
        mTimerSetupView.setupBottomButton("+ 1:00", v -> {
            DataModel.getDataModel().addTimerMinute(getTimer());
            Events.sendTimerEvent(R.string.action_add_minute, R.string.label_deskclock);
        });
        mTimerSetupView.setupTopButton(view.getContext().getString(R.string.timer_delete), v -> {
            DataModel.getDataModel().removeTimer(getTimer());
        });
        mTimerClickHandler = timerClickHandler;
    }

    /**
     * @return {@code true} if the timer is in a state that requires continuous updates
     */
    boolean updateTime() {
        mTimerSetupView.setTime(getTimer().getRemainingTime());
        return !getTimer().isReset();
    }

    public void onBind(int timerId) {
        mTimerId = timerId;
        updateTime();
    }

    private Timer getTimer() {
        return DataModel.getDataModel().getTimer(mTimerId);
    }
}
