package org.dync.ijkplayerlib.widget.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import org.dync.ijkplayerlib.widget.common.IMediaPlayer;
import org.dync.ijkplayerlib.widget.media.IRenderView;

import java.lang.reflect.Field;

public class WindowManagerUtil {

    private static final String TAG = "WindowManagerUtil";

    /**
     * 小悬浮窗View的实例
     */
    private static IjkWindowVideoView smallWindow;

    /**
     * 页面内View的实例
     */
    private static IjkWindowVideoView smallApp;

    /**
     * 小悬浮窗View的参数
     */
    private static LayoutParams smallWindowParams;

    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private static WindowManager mWindowManager;

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context
     * @param mediaPlayer
     */
    public static void createSmallWindow(final Context context, IMediaPlayer mediaPlayer) {
        mWindowManager = getWindowManager(context);
        int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        if (smallWindowParams == null) {
            smallWindowParams = new LayoutParams();
            smallWindowParams.type = LayoutParams.TYPE_PHONE;
            smallWindowParams.format = PixelFormat.RGBA_8888;
            smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            int width;
            int height;
            if(screenWidth > screenHeight) {
                width = screenHeight / 2;
                height = width * 8 / 16;
            }else {
                width = screenWidth / 2;
                height = width * 8 / 16;
            }
            //小窗口摆放的位置，手机屏幕中央
            smallWindowParams.x = screenWidth / 2 - width / 2;
            smallWindowParams.y = screenHeight / 2 - height / 2;
            smallWindowParams.width = width;
            smallWindowParams.height = height;
        }
        smallWindow = new IjkWindowVideoView(context);
        if(mediaPlayer != null) {
            smallWindow.setMediaPlayer(mediaPlayer);
            smallWindow.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        }
        smallWindow.setLayoutParams(smallWindowParams);
        mWindowManager.addView(smallWindow, smallWindowParams);
    }

    private static int statusBarHeight;
    private static int screenWidth;
    private static int screenHeight;
    private static int lastX;
    private static int lastY;
    private static int downX;
    private static int downY;
    private static long startTime;

    @SuppressLint("ClickableViewAccessibility")
    public static void createSmallWindow(ViewGroup view, IMediaPlayer mediaPlayer) {
        final Context context = view.getContext();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels - getStatusBarHeight(context);
        smallWindow = new IjkWindowVideoView(context);
        smallWindow.setFocusableInTouchMode(false);
        if(mediaPlayer != null) {
            smallWindow.setMediaPlayer(mediaPlayer);
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int width;
        int height;
        if(screenWidth > screenHeight) {
            width = screenHeight / 2;
            height = width * 8 / 16;
        }else {
            width = screenWidth / 2;
            height = width * 8 / 16;
        }
        layoutParams.width = width;
        layoutParams.height = height;
        smallWindow.addView(smallWindow);
        smallWindow.setClickable(true);
        smallWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action=event.getAction();
                Log.i("TAG", "Touch:"+action);
                switch(action){
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY() - getStatusBarHeight(context);
                        downX = (int) event.getRawX();
                        downY = (int) event.getRawY() - getStatusBarHeight(context);
                        break;
                    /**
                     * layout(l,t,r,b)
                     * l  Left position, relative to parent
                     t  Top position, relative to parent
                     r  Right position, relative to parent
                     b  Bottom position, relative to parent
                     * */
                    case MotionEvent.ACTION_MOVE:
                        int dx =(int)event.getRawX() - lastX;
                        int dy =(int)event.getRawY() - lastY;

                        int left = v.getLeft() + dx;
                        int top = v.getTop() + dy;
                        int right = v.getRight() + dx;
                        int bottom = v.getBottom() + dy;
                        if(left < 0){
                            left = 0;
                            right = left + v.getWidth();
                        }
                        if(right > screenWidth){
                            right = screenWidth;
                            left = right - v.getWidth();
                        }
                        if(top < 0){
                            top = 0;
                            bottom = top + v.getHeight();
                        }
                        if(bottom > screenHeight){
                            bottom = screenHeight;
                            top = bottom - v.getHeight();
                        }
                        v.layout(left, top, right, bottom);
                        Log.i(TAG, "position: " + left +", " + top + ", " + right + ", " + bottom);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY() - getStatusBarHeight(context);
                        break;
                    case MotionEvent.ACTION_UP:
                        // 如果手指离开屏幕时，downX和lastX相等，且downY和lastY相等，则视为触发了单击事件。
                        if (Math.abs(downX - lastX) < 5 && Math.abs(downY - lastY) < 5) {
                            long end = System.currentTimeMillis() - startTime;
                            // 双击的间隔在 300ms以下
                            if (end < 300) {
                                if(appCallBack != null) {
                                    appCallBack.removeSmallApp(smallApp.getMediaPlayer());
                                }
                            }
                            startTime = System.currentTimeMillis();
                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
        }
    }

    /**
     * 将视图从viewGroup上移除。
     *
     * @param viewGroup 必须为应用程序的Context.
     */
    public static void removeSmallApp(ViewGroup viewGroup) {
        if (smallApp != null) {
            viewGroup.removeView(smallApp);
            smallApp = null;
        }
    }

    public static void setWindowCallBack(IjkWindowVideoView.CallBack callBack) {
        smallWindow.setCallBack(callBack);
    }

    public static interface AppCallBack {
        void removeSmallApp(IMediaPlayer mediaPlayer);
    }

    private static AppCallBack appCallBack;

    public static void setAppCallBack(AppCallBack callBack) {
        appCallBack = callBack;
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context 必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private static int getStatusBarHeight(Context context) {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = context.getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

}
