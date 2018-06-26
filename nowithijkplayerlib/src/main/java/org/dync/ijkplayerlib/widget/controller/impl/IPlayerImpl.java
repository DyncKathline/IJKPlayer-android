package org.dync.ijkplayerlib.widget.controller.impl;

/**
 * Created by zxz on 2016/5/3.
 */
public abstract class IPlayerImpl {


    /**
     * 网络异常时处理
     */
    public void onNetWorkError() {

    }

    /**
     * 标题栏返回按钮功能
     */
    public void onBack() {

    }

    /**
     * 播放结束后
     */
    public boolean onComplete() {
        return false;
    }

    /**
     * 播放发生异常
     */
    public boolean onError() {
        return false;
    }


}
