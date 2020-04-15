package org.dync.ijkplayer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StyleRes;


/**
 * Created by KathLine on 2016/8/2.
 */
public class CustomDialog extends Dialog {

    private Builder builder;
    private @LayoutRes
    int layoutId;
    private int gravity;
    private @StyleRes
    int animId;
    private boolean backgroundDrawableable;
    private float dimAmount;
    private boolean cancelable;
    private boolean existDialogLined;
    private boolean isFullScreen;
    private int width;
    private int height;

    private CustomDialog(Context context, Builder builder) {
        super(context);
        this.builder = builder;
        layoutId = builder.layoutId;
        gravity = builder.gravity;
        animId = builder.animId;
        backgroundDrawableable = builder.backgroundDrawableable;
        dimAmount = builder.dimAmount;
        cancelable = builder.cancelable;
        existDialogLined = builder.existDialogLined;
        isFullScreen = builder.isFullScreen;
        width = builder.width;
        height = builder.height;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (existDialogLined) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        setContentView(layoutId);
        Window window = getWindow();
        window.setGravity(gravity);
        if(animId != 0) {
            window.setWindowAnimations(animId);
        }
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        if (width != 0 && height != 0) {
            lp.width = width;
            lp.height = height;
        }
        if (isFullScreen) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏，即没有系统状态栏
        }
        if (backgroundDrawableable) {
            window.setBackgroundDrawable(new ColorDrawable(0));
        }
        if (dimAmount < 0f || dimAmount > 1f) {
            throw new RuntimeException("透明度必须在0~1之间");
        }else {
            lp.dimAmount = dimAmount;
        }
        window.setAttributes(lp);
        setCancelable(cancelable);
        if (cancelable) {
            setCanceledOnTouchOutside(true);
        }else {
            setCanceledOnTouchOutside(false);
        }
    }

    public Builder getBuilder() {
        return builder;
    }

    public CustomDialog show(Builder.onInitListener listener) {
        this.show();
        if (listener != null) {
            listener.init(this);
        }
        return this;
    }

    public static class Builder {
        private Context context;
        private int layoutId;
        private int gravity;
        private int animId;
        private boolean backgroundDrawableable;
        private float dimAmount;
        private boolean cancelable;
        private boolean existDialogLined;
        private boolean isFullScreen;
        private int width;
        private int height;

        public Builder(Context context) {
            this.context = context;
            layoutId = R.layout.dialog_base;
            gravity = Gravity.CENTER;
            animId = 0;
            backgroundDrawableable = true;
            dimAmount = 0.5f;
            cancelable = true;
            existDialogLined = true;
            width = 0;
            height = 0;
        }

        public Builder setContentView(@LayoutRes int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        /**
         * 必须使用Gravity的静态常量，默认在中间弹出
         *
         * @param gravity 详见{@link android.view.Gravity}
         * @return
         * @see Gravity
         */
        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * 设置Dialog弹出和Dialog退出的动画
         *
         * @param animId
         * @return
         */
        public Builder setAnimId(int animId) {
            this.animId = animId;
            return this;
        }

        /**
         * Creates a new set of layout parameters with the specified width
         * and height.
         *
         * @param width  the width, either set WindowManager.LayoutParams.WRAP_CONTENT or
         *               WindowManager.LayoutParams.FILL_PARENT (replaced by WindowManager.LayoutParams.MATCH_PARENT in
         *               API Level 8), or a fixed size in pixels
         * @param height the height, either set WindowManager.LayoutParams.WRAP_CONTENT or
         *               WindowManager.LayoutParams.FILL_PARENT (replaced by WindowManager.LayoutParams.MATCH_PARENT in
         *               API Level 8), or a fixed size in pixels
         * @return
         */
        public Builder setLayoutParams(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * 是否给Dialog的背景设置透明，默认false
         *
         * @param backgroundDrawableable
         * @return
         */
        public Builder setBackgroundDrawable(boolean backgroundDrawableable) {
            this.backgroundDrawableable = backgroundDrawableable;
            return this;
        }

        /**
         * 设置Dialog之外的背景透明度，0~1之间，默认值 0.5f，半透明
         *
         * @param dimAmount
         * @return
         */
        public Builder setDimAmount(float dimAmount) {
            this.dimAmount = dimAmount;
            return this;
        }

        /**
         * 设置Dialog是否可以关闭在Dialog之外的区域，默认true
         *
         * @param cancelable
         * @return
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * 如果存在Holo主题下Dialog有蓝色线(含有标题栏)可以尝试调用该方法，默认不存在
         *
         * @param existDialogLined
         * @return
         */
        public Builder setExistDialogLined(boolean existDialogLined) {
            this.existDialogLined = existDialogLined;
            return this;
        }

        /**
         * 是否设置全屏模式，指的是去除系统状态栏，默认不去除
         *
         * @param isFullScreen
         * @return
         */
        public Builder setFullScreen(boolean isFullScreen) {
            this.isFullScreen = isFullScreen;
            return this;
        }

        public interface onInitListener {
            /**
             * 绑定控件
             *
             * @param customDialog
             */
            void init(CustomDialog customDialog);
        }

        public CustomDialog build() {
            return new CustomDialog(context, this);
        }

        /**
         * 如果对话框仅仅起提示作用，可以传入null
         *
         * @param listener
         * @return
         */
        public CustomDialog show(onInitListener listener) {
            CustomDialog dialog = build();
            dialog.show();
            if (listener != null) {
                listener.init(dialog);
            }
            return dialog;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(@IdRes int viewId) {
        View view = findViewById(viewId);
        return (T) view;
    }
}
