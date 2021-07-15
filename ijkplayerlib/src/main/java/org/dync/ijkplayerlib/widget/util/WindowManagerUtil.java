package org.dync.ijkplayerlib.widget.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import org.dync.ijkplayerlib.widget.media.IRenderView;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;

import java.lang.reflect.Field;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class WindowManagerUtil {

    private static final String TAG = "WindowManagerUtil";

    /**
     * 小悬浮窗View的实例
     */
    private static IjkVideoView smallWindow;

    /**
     * 页面内View的实例
     */
    private static IjkVideoView smallApp;

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
    public static void createSmallWindow(final Context context, final IMediaPlayer mediaPlayer) {
        if (smallWindow != null) {
            return;
        }
        mWindowManager = getWindowManager(context);
        int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        if (smallWindowParams == null) {
            smallWindowParams = new LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }else {
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            smallWindowParams.format = PixelFormat.RGBA_8888;
            smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            int width;
            int height;
            if(screenWidth > screenHeight) {
                width = screenHeight / 2;
                height = width * 9 / 16;
            }else {
                width = screenWidth / 2;
                height = width * 9 / 16;
            }
            //小窗口摆放的位置，手机屏幕中央
            smallWindowParams.x = screenWidth / 2 - width / 2;
            smallWindowParams.y = screenHeight / 2 - height / 2;
            smallWindowParams.width = width;
            smallWindowParams.height = height;
        }
        smallWindow = new IjkVideoView(context);
        if(mediaPlayer != null) {
            smallWindow.setMediaPlayer(mediaPlayer);
            smallWindow.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
            smallWindow.resetRenders();
        }
        final RelativeLayout relativeLayout = new RelativeLayout(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        relativeLayout.addView(smallWindow, params);
        relativeLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
        mWindowManager.addView(relativeLayout, smallWindowParams);
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {

            /**
             * 记录当前手指位置在屏幕上的横坐标值
             */
            private float xInScreen;

            /**
             * 记录当前手指位置在屏幕上的纵坐标值
             */
            private float yInScreen;

            /**
             * 记录手指按下时在屏幕上的横坐标的值
             */
            private float xDownInScreen;

            /**
             * 记录手指按下时在屏幕上的纵坐标的值
             */
            private float yDownInScreen;

            /**
             * 记录手指按下时在小悬浮窗的View上的横坐标的值
             */
            private float xInView;

            /**
             * 记录手指按下时在小悬浮窗的View上的纵坐标的值
             */
            private float yInView;

            /**
             * 按下的开始时间
             */
            private long startTime;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                        xInView = event.getX();
                        yInView = event.getY();
                        xDownInScreen = event.getRawX();
                        yDownInScreen = event.getRawY() - getStatusBarHeight(context);
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY() - getStatusBarHeight(context);
                        // 手指移动的时候更新小悬浮窗的位置
                        if(smallWindowParams != null) {
                            smallWindowParams.x = (int) (xInScreen - xInView);
                            smallWindowParams.y = (int) (yInScreen - yInView);
                            mWindowManager.updateViewLayout(relativeLayout, smallWindowParams);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                        if (Math.abs(xDownInScreen - xInScreen) < 5 && Math.abs(yDownInScreen - yInScreen) < 5) {
                            long end = System.currentTimeMillis() - startTime;
                            // 双击的间隔在 300ms以下
                            if (end < 300) {
                                if(mWindowCallBack != null) {
                                    mWindowCallBack.removeSmallWindow(mediaPlayer);
                                }
                            }
                            startTime = System.currentTimeMillis();
                        }
                        break;
                    default:
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
            windowManager.removeView((View) smallWindow.getParent());
            smallWindow = null;
        }
    }

    private static int statusBarHeight;
    private static int screenWidth;
    private static int screenHeight;

    /**
     * 在当前页面创建一个视图
     * @param activity
     * @param mediaPlayer
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void createSmallApp(final Activity activity, IMediaPlayer mediaPlayer) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        Context context = activity.getBaseContext();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        smallApp = new IjkVideoView(context);
        smallApp.setFocusableInTouchMode(false);
        if(mediaPlayer != null) {
            smallApp.setMediaPlayer(mediaPlayer);
            smallApp.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
            smallApp.resetRenders();
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int width;
        int height;
        if(screenWidth > screenHeight) {
            width = screenHeight / 2;
            height = width * 9 / 16;
        }else {
            width = screenWidth / 2;
            height = width * 9 / 16;
        }
        layoutParams.width = width;
        layoutParams.height = height;

        RelativeLayout relativeLayout = new RelativeLayout(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        relativeLayout.addView(smallApp, params);
        relativeLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
        relativeLayout.bringToFront();
        decorView.addView(relativeLayout, layoutParams);
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {

            private int lastX;
            private int lastY;
            private int downX;
            private int downY;
            private long startTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action=event.getAction();
                Log.i("TAG", "Touch:"+action);
                switch(action){
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        downX = (int) event.getRawX();
                        downY = (int) event.getRawY();
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
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        // 如果手指离开屏幕时，downX和lastX相等，且downY和lastY相等，则视为触发了单击事件。
                        if (Math.abs(downX - lastX) < 5 && Math.abs(downY - lastY) < 5) {
                            long end = System.currentTimeMillis() - startTime;
                            // 双击的间隔在 300ms以下
                            if (end < 300) {
                                removeSmallApp(activity);
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
     * 将视图从viewGroup上移除。
     *
     * @param activity 必须为应用程序的Context.
     */
    public static void removeSmallApp(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        if (smallApp != null) {
            decorView.removeView((View) smallApp.getParent());
        }
    }

    public static interface WindowCallBack {
        void removeSmallWindow(IMediaPlayer mediaPlayer);
    }

    private static WindowCallBack mWindowCallBack;

    public static void setWindowCallBack(WindowCallBack callBack) {
        mWindowCallBack = callBack;
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
