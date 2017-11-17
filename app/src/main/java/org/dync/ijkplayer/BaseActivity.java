package org.dync.ijkplayer;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by KathLine on 2017/10/18.
 */

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private View clickView;
    private int oldId = -1;
    private long lastClickTime = 0;
    private final int MIN_CLICK_DELAY_TIME = 500;

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
