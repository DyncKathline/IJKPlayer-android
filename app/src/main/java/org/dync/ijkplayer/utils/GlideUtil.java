package org.dync.ijkplayer.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.dync.ijkplayer.R;

/**
 * Created by KathLine on 2017/12/6.
 */

public class GlideUtil {

    public static void showImg(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.background)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }
}
