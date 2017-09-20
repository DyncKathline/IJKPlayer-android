package org.dync.ijkplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.dync.ijkplayer.utils.StatusBarUtil;
import org.dync.ijkplayerlib.widget.media.AndroidMediaController;
import org.dync.ijkplayerlib.widget.media.IRenderView;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;
import org.dync.ijkplayerlib.widget.media.MeasureHelper;
import org.dync.ijkplayerlib.widget.util.PlayerController;
import org.dync.ijkplayerlib.widget.util.Settings;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";
    private Context mContext;
    private String mVideoPath;
    private Uri mVideoUri;

    private AndroidMediaController mMediaController;
    private IjkVideoView mVideoView;

    private boolean mBackPressed;

    private PlayerController mPlayerController;
    private TextView tv_speed;
    private TextView tv_current_time;
    private TextView tv_total_time;
    private TextView tv_fastForward;
    private TextView tv_fastForwardTag;
    private TextView tv_fastForwardAll;
    private LinearLayout app_video_loading;
    private TextView app_video_speed;
    private LinearLayout app_video_brightness_box;
    private LinearLayout app_video_volume_box;
    private TextView tv_volume;
    private TextView tv_brightness;

    private SeekBar sbVdieo;
    private ImageView iv_paly;
    private LinearLayout app_video_fastForward_box;
    private LinearLayout ll_bottom;

    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle) {
        context.startActivity(newIntent(context, videoPath, videoTitle));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mContext = this;

        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        finish();
                        return;
                    }
                }
            }
        }

        initView();
        initPlayer();
        initVideoListener();

        StatusBarUtil.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary), false);
    }

    private void initPlayer() {
        //        ActionBar actionBar = getSupportActionBar();
//        mMediaController = new AndroidMediaController(this, false);
//        mMediaController.setSupportActionBar(actionBar);
//        mVideoView.setMediaController(mMediaController);

        mPlayerController = new PlayerController(this, mVideoView)
                .setVideoParentLayout(findViewById(R.id.rl_video_view_layout))
                .setVideoController((SeekBar) findViewById(R.id.seekbar))
                .setVolumeController()
                .setBrightnessController()
                .setVideoParentRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setVideoRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setPortrait(true)
                .setKeepScreenOn(true)
                .setAutoControlListener(iv_paly)
                .setAutoControlPanel(true)
                .setPanelControl(new PlayerController.PanelControlListener() {
                    @Override
                    public void operatorPanel(boolean isShowControlPanel) {
                        if (isShowControlPanel) {
                            ll_bottom.setVisibility(View.VISIBLE);
                        } else {
                            ll_bottom.setVisibility(View.GONE);
                        }
                    }
                })
                .setSyncProgressListener(new PlayerController.SyncProgressListener() {
                    @Override
                    public void syncTime(long position, long duration) {
                        tv_current_time.setText(mPlayerController.generateTime(position));
                        tv_total_time.setText(mPlayerController.generateTime(duration));
                    }
                })
                .setGestureListener(new PlayerController.GestureListener() {
                    @Override
                    public void onProgressSlide(long newPosition, long duration, int showDelta) {
                        if (showDelta != 0) {
                            app_video_fastForward_box.setVisibility(View.VISIBLE);
                            app_video_brightness_box.setVisibility(View.GONE);
                            app_video_volume_box.setVisibility(View.GONE);
                            tv_fastForwardTag.setVisibility(View.VISIBLE);
                            tv_fastForwardAll.setVisibility(View.VISIBLE);
                            tv_fastForwardTag.setText(mPlayerController.generateTime(newPosition) + "/");
                            tv_fastForwardAll.setText(mPlayerController.generateTime(duration));

                            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
                            tv_fastForward.setVisibility(View.VISIBLE);
                            tv_fastForward.setText(String.format("%ss", text));
                        }
                    }

                    @Override
                    public void onVolumeSlide(int volume) {
                        app_video_fastForward_box.setVisibility(View.GONE);
                        app_video_brightness_box.setVisibility(View.GONE);
                        app_video_volume_box.setVisibility(View.VISIBLE);
                        tv_volume.setVisibility(View.VISIBLE);
                        tv_volume.setText(volume + "%");
                    }

                    @Override
                    public void onBrightnessSlide(float brightness) {
                        app_video_fastForward_box.setVisibility(View.GONE);
                        app_video_brightness_box.setVisibility(View.VISIBLE);
                        app_video_volume_box.setVisibility(View.GONE);
                        tv_brightness.setVisibility(View.VISIBLE);
                        tv_brightness.setText((int) (brightness * 100) + "%");
                    }

                    @Override
                    public void endGesture() {
                        app_video_fastForward_box.setVisibility(View.GONE);
                        app_video_brightness_box.setVisibility(View.GONE);
                        app_video_volume_box.setVisibility(View.GONE);
                        app_video_brightness_box.setVisibility(View.GONE);
                        app_video_volume_box.setVisibility(View.GONE);
                    }
                });

        // prefer mVideoPath
        Settings settings = new Settings(this);
        settings.setPlayer(Settings.PV_PLAYER__Auto);
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else if (mVideoUri != null)
            mVideoView.setVideoURI(mVideoUri);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }
        mVideoView.start();
        if (app_video_loading != null) {
            app_video_loading.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        tv_speed = (TextView) findViewById(R.id.app_video_speed);

        //加载中布局
        app_video_loading = (LinearLayout) findViewById(R.id.app_video_loading);
        app_video_speed = (TextView) findViewById(R.id.app_video_speed);

        //快进快退
        app_video_fastForward_box = (LinearLayout) findViewById(R.id.app_video_fastForward_box);
        tv_fastForward = (TextView) findViewById(R.id.app_video_fastForward);
        tv_fastForwardTag = (TextView) findViewById(R.id.app_video_fastForward_target);
        tv_fastForwardAll = (TextView) findViewById(R.id.app_video_fastForward_all);

        //亮度
        app_video_brightness_box = (LinearLayout) findViewById(R.id.app_video_brightness_box);
        tv_brightness = (TextView) findViewById(R.id.app_video_brightness);
        //声音
        app_video_volume_box = (LinearLayout) findViewById(R.id.app_video_volume_box);
        tv_volume = (TextView) findViewById(R.id.app_video_volume);

        //
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
        iv_paly = (ImageView) findViewById(R.id.play_icon);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        tv_total_time = (TextView) findViewById(R.id.tv_total_time);
        sbVdieo = (SeekBar) findViewById(R.id.seekbar);
    }

    public void initVideoListener() {
        iv_paly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    updatePlayBtnBg(true);
                } else {
                    mVideoView.start();
                    updatePlayBtnBg(false);
                }
            }
        });
        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                updatePlayBtnBg(false);
                mPlayerController.setPlayRate(1.0f);
            }
        });
        mVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://视频开始渲染
                        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                        if (app_video_loading != null) {
                            app_video_loading.setVisibility(View.GONE);
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START://视频缓冲开始
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                        if (app_video_loading != null) {
                            app_video_loading.setVisibility(View.VISIBLE);
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END://视频缓冲结束
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                        if (app_video_loading != null) {
                            app_video_loading.setVisibility(View.GONE);
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH://网络带宽
                        Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
                        if (app_video_speed != null) {
                            app_video_loading.setVisibility(View.VISIBLE);
                            app_video_speed.setText(PlayerController.getFormatSize(extra));
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE://视频数据更新
                        Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE: " + extra);
                        if (app_video_speed != null) {
                            app_video_loading.setVisibility(View.VISIBLE);
                            app_video_speed.setText(PlayerController.getFormatSize(extra));
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                        Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                        Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                        Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 更新播放按钮的背景图片
     */
    private void updatePlayBtnBg(boolean isPlay) {
        try {
            int resid;
            if (isPlay) {
                // 暂停
                resid = R.drawable.simple_player_center_play;
            } else {
                // 播放
                resid = R.drawable.simple_player_center_pause;
            }
            iv_paly.setImageResource(resid);
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
//            mVideoView.stopPlayback();
//            mVideoView.release(true);
//            mVideoView.stopBackgroundPlay();
            mVideoView.pause();
            updatePlayBtnBg(true);
        } else {
            mVideoView.enterBackground();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mPlayerController != null) {
            mPlayerController.onConfigurationChanged();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
                StatusBarUtil.setFitsSystemWindows(this, true);
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
                StatusBarUtil.setFitsSystemWindows(this, false);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle_ratio) {
            int aspectRatio = mVideoView.toggleAspectRatio();
            String aspectRatioText = MeasureHelper.getAspectRatioText(this, aspectRatio);
            return true;
        } else if (id == R.id.action_toggle_player) {
            int player = mVideoView.togglePlayer();
            String playerText = IjkVideoView.getPlayerText(this, player);
            return true;
        } else if (id == R.id.action_toggle_render) {
            int render = mVideoView.toggleRender();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
