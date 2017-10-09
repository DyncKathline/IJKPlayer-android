package org.dync.ijkplayer.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by xiongxuesong-pc on 2016/5/15.
 */
public class StatusBarUtil {

    private static SystemBarTintManager tintManager;

    /**
     * 修改状态栏为全透明
     *
     * @param activity
     */
    public static void transparencyBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本,保持沉浸式状态
     *
     * @param activity
     * @param colorId
     */
    public static void setStatusBarColor(Activity activity, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTint库使4.4版本状态栏变色，需要先将状态栏设置为透明
            transparencyBar(activity);
            ViewGroup contentFrameLayout = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            View parentView = contentFrameLayout.getChildAt(0);
            if (parentView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                parentView.setFitsSystemWindows(true);
            }
            tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(colorId);
            /*// set a custom tint color for all system bars
            tintManager.setTintColor(Color.parseColor("#99000FF"));
            // set a custom navigation bar resource
            tintManager.setNavigationBarTintResource(R.drawable.my_tint);
            // set a custom status bar drawable
            tintManager.setStatusBarTintDrawable(MyDrawable);*/
        }
    }

    /**
     * 设置WindowManager.LayoutParams.FLAG_FULLSCREEN时，由于使用了fitSystemWindows()方法,导致的问题
     * 支持4.4以上版本
     *
     * @param activity
     * @param flag_fullscreen true：添加全屏   false：清除全屏
     */
    public static void setFitsSystemWindows(Activity activity, boolean flag_fullscreen) {
        ViewGroup contentFrameLayout = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        View parentView = contentFrameLayout.getChildAt(0);
        if (flag_fullscreen) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//清除全屏
        }
        if (parentView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (tintManager != null) {
                if (flag_fullscreen) {
                    tintManager.setStatusBarTintEnabled(false);
                } else {
                    tintManager.setStatusBarTintEnabled(true);
                }
            }
        }
    }

    /**
     *
     * @param activity
     * @param navigationBarState    navigationBar是否可见，true：可见   false：不可见
     */
    public static void setSystemUI(Activity activity, boolean navigationBarState){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            if(!navigationBarState){
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                //添加Flag把状态栏设为可绘制模式
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                int uiFlags = 0;
//                uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;//隐藏NavigationBar时会留下空白
                uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                uiFlags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                window.getDecorView().setSystemUiVisibility(uiFlags);
                window.setNavigationBarColor(Color.TRANSPARENT);
                window.setStatusBarColor(Color.TRANSPARENT);
            }else {
                window.getDecorView().setSystemUiVisibility(0);
            }
        }
    }

    /**
     * 设置状态栏黑色字体图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @param dark     是否把状态栏字体及图标颜色设置为深色
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    public static int StatusBarLightMode(Activity activity, boolean dark) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (MIUISetStatusBarLightMode(activity.getWindow(), dark)) {
                result = 1;
            } else if (FlymeSetStatusBarLightMode(activity.getWindow(), dark)) {
                result = 2;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = activity.getWindow().getDecorView();
                int oldVis = decorView.getSystemUiVisibility();
                int newVis = oldVis;
                if (dark) {
                    newVis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    newVis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                if (newVis != oldVis) {
                    decorView.setSystemUiVisibility(newVis);
                }
                result = 3;
            }
        }
        return result;
    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 已知系统类型时，设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @param type     1:MIUUI 2:Flyme 3:android6.0
     */
    public static void StatusBarLightMode(Activity activity, int type) {
        if (type == 1) {
            MIUISetStatusBarLightMode(activity.getWindow(), true);
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(activity.getWindow(), true);
        } else if (type == 3) {
            View decorView = activity.getWindow().getDecorView();
            int oldVis = decorView.getSystemUiVisibility();
            int newVis = oldVis;
            newVis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (newVis != oldVis) {
                decorView.setSystemUiVisibility(newVis);
            }
        }

    }

    /**
     * 清除MIUI或flyme或6.0以上版本状态栏黑色字体，即白色字体
     */
    public static void StatusBarDarkMode(Activity activity, int type) {
        if (type == 1) {
            MIUISetStatusBarLightMode(activity.getWindow(), false);
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(activity.getWindow(), false);
        } else if (type == 3) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }

    }

}
