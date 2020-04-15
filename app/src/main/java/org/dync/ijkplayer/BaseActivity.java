package org.dync.ijkplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by KathLine on 2017/10/18.
 */

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    protected Context mContext;
    protected Activity mActivity;
    private View clickView;
    private int oldId = -1;
    private long lastClickTime = 0;
    private final int MIN_CLICK_DELAY_TIME = 500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mContext = this;
    }

    @Override
    public void onClick(View v) {
        avoidDouleClick(v);
    }

    /**
     * 禁止重复点击
     *
     * @param
     */
    public void avoidDouleClick(View v) {
        clickView = v;
        if (oldId == -1) {//第一次点击
            lastClickTime = SystemClock.elapsedRealtime();
            oldId = v.getId();
        } else if (v.getId() == oldId) {//第二次
            long time = SystemClock.elapsedRealtime();
            if (time - lastClickTime < MIN_CLICK_DELAY_TIME) {
                v.setId(0);
            }
            lastClickTime = time;
        } else if (v.getId() == 0) {//第三次
            lastClickTime = SystemClock.elapsedRealtime();
            v.setId(oldId);
        }
    }
}
