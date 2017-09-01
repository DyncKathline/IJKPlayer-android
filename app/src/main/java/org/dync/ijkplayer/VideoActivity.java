package org.dync.ijkplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.dync.ijkplayerlib.widget.media.AndroidMediaController;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;
import org.dync.ijkplayerlib.widget.media.MeasureHelper;
import org.dync.ijkplayerlib.widget.util.PlayerController;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

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
    private LinearLayout app_video_brightness_box;
    private LinearLayout app_video_volume_box;
    private TextView tv_volume;
    private TextView tv_brightness;


    private boolean hasBuffer;//是否视频有缓存
    protected boolean mVideoProgressTrackingTouch = false;
    protected static final int UPDATE_CURRENT_POSITION = 3;//更新当前播放的时间
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_CURRENT_POSITION:
                    updateVideo();
                    break;
            }
        }
    };
    private SeekBar sbVdieo;
    private ImageView iv_paly;

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

        // init player
        // 这里可以不写，如果要写，最好配合IjkMediaPlayer.native_profileEnd();使用
//        IjkMediaPlayer.loadLibrariesOnce(null);
//        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        initView();
//        ActionBar actionBar = getSupportActionBar();
//        mMediaController = new AndroidMediaController(this, false);
//        mMediaController.setSupportActionBar(actionBar);
//        mVideoView.setMediaController(mMediaController);

//        mPlayerController = new PlayerController(this, mVideoView)
//                .operatorPanl()
//                .setVideoRootLayout(findViewById(R.id.app_video_box))
//                .setVideoController((SeekBar) findViewById(R.id.simple_player_volume_controller))
//                .setVolumeController((SeekBar) findViewById(R.id.simple_player_volume_controller))
//                .setBrightnessController((SeekBar) findViewById(R.id.simple_player_brightness_controller))
//                .setSyncProgressListener(new PlayerController.SyncProgressListener() {
//                    @Override
//                    public void syncTime(long position, long duration) {
//                        tv_current_time.setText(mPlayerController.generateTime(position));
//                        tv_total_time.setText(mPlayerController.generateTime(duration));
//                    }
//                })
//                .setGestureListener(new PlayerController.GestureListener() {
//                    @Override
//                    public void onProgressSlide(long newPosition, long duration, int showDelta) {
//                        if (showDelta != 0) {
//                            tv_fastForwardTag.setVisibility(View.VISIBLE);
//                            tv_fastForwardAll.setVisibility(View.VISIBLE);
//                            tv_fastForwardTag.setText(mPlayerController.generateTime(newPosition));
//                            tv_fastForwardAll.setText(mPlayerController.generateTime(duration));
//
//                            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
//                            tv_fastForward.setVisibility(View.VISIBLE);
//                            tv_fastForward.setText(String.format("%ss", text));
//                        }
//                    }
//
//                    @Override
//                    public void onVolumeSlide(int volume) {
//                        app_video_brightness_box.setVisibility(View.GONE);
//                        app_video_volume_box.setVisibility(View.VISIBLE);
//                        tv_volume.setVisibility(View.VISIBLE);
//                        tv_volume.setText(volume + "%");
//                    }
//
//                    @Override
//                    public void onBrightnessSlide(float brightness) {
//                        tv_brightness.setVisibility(View.VISIBLE);
//                        tv_brightness.setText((int) (brightness * 100) + "%");
//                    }
//
//                    @Override
//                    public void endGesture() {
//                        app_video_brightness_box.setVisibility(View.GONE);
//                        app_video_volume_box.setVisibility(View.GONE);
//                    }
//                });

        initVideoListener();
        // prefer mVideoPath
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
    }

    private void initView() {
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        tv_speed = (TextView) findViewById(R.id.app_video_speed);

        //快进快退
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
        iv_paly = (ImageView) findViewById(R.id.play_icon);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        tv_total_time = (TextView) findViewById(R.id.tv_total_time);
        sbVdieo = (SeekBar) findViewById(R.id.seekbar);
    }

    public void initVideoListener() {
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        sbVdieo.setOnSeekBarChangeListener(mVideoOnSeekBarChangeListener);
    }

    private void updateVideo() {
        if (mVideoView != null) {
            long curTime = mVideoView.getCurrentPosition();
            long durTime = mVideoView.getDuration();
            if (curTime >= 0 && curTime <= durTime) {
//            Log.i(TAG, "updateVideo:sbVideo.getMax() =  " + sbVideo.getMax() + ",curTime = " + curTime);
                if (!mVideoProgressTrackingTouch) {
                    sbVdieo.setProgress((int) curTime);
                }
                CharSequence playTime = PlayerController.generateTime(durTime);// 视频总长度
                String currentPlayTime = PlayerController.generateTime(curTime).toString();// 视频当前长度
                tv_current_time.setText(currentPlayTime);
                tv_total_time.setText(playTime);
                mHandler.sendEmptyMessageDelayed(UPDATE_CURRENT_POSITION, 1000);
            }
        }
    }

    /**
     * 准备播放视频监听
     */
    IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer mp) {
            if (mVideoView == null) {
                return;
            }

            int duration = mVideoView.getDuration();

            tv_current_time.setText("00:00");
            tv_total_time.setText(PlayerController.generateTime(duration));
            sbVdieo.setMax(duration);
            sbVdieo.setEnabled(true);
            mVideoView.seekTo(0);

            if (NetworkUtils.getNetworkType(mContext) == 4) {//手机网络
                mVideoView.pause();
                updatePlayBtnBg(false); // 更新播放按钮背景
            } else {
                mVideoView.start(); // 开始播放视频
                updatePlayBtnBg(true); // 更新播放按钮背景
            }

            updateVideo();
            Log.i(TAG, "-----------OnPreparedListener------------------");
        }

    };

    /**
     * 视频播放完成的回调监听器
     */
    IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer mp) {
            if (mVideoView != null) {
                mVideoView.pause();

            }
//            mHandler.removeMessages(UPDATE_CURRENT_POSITION);
            PlayEnd();
            updatePlayBtnBg(false);
        }
    };

    private void PlayEnd() {
        if (mVideoView != null) {
            mVideoView.seekTo(0);
        }
    }

    /**
     * 更新播放按钮的背景图片
     */
    private void updatePlayBtnBg(boolean isPlay) {
        try {
            int resid;
            if (isPlay) {
                // 暂停
                resid = R.drawable.simple_player_center_pause;
            } else {
                // 播放
                resid = R.drawable.simple_player_center_play;
            }
            iv_paly.setImageResource(resid);
        }catch (Exception e){

        }

    }

    /**
     * 视频缓冲卡顿监听器
     */
    IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START: // 视频缓冲卡顿的时候会执行
                    hasBuffer = false;
                    if (mVideoView != null) {
                        Log.i(TAG, "视频缓冲卡顿的时候会执行: " + PlayerController.generateTime(mVideoView.getCurrentPosition()));
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END: // 视频缓冲到可以播放的时候会执行
                    hasBuffer = true;
                    if (mVideoView != null) {
                        Log.i(TAG, "视频缓冲到可以播放的时候会执行: " + PlayerController.generateTime(mVideoView.getCurrentPosition()));
                    }
//                    hideLoading();
                    break;
            }
            if (tv_speed != null) {
                tv_speed.setText(mPlayerController.getFormatSize(extra));
            }
            return true;
        }
    };

    /**
     * 视频播放失败回调
     */
    IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            Log.i(TAG, "mOnErrorListener");
            if (mVideoView != null) {
                mVideoView.stopPlayback();
            }
            mHandler.removeMessages(UPDATE_CURRENT_POSITION);
            return true;
        }

    };

    IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

        }
    };
    /**
     * 视频在SeekBar滑动改变监听
     */
    SeekBar.OnSeekBarChangeListener mVideoOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {

            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mVideoProgressTrackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mVideoProgressTrackingTouch = false;
            int curProgress = seekBar.getProgress();
            int maxProgress = seekBar.getMax();
            if (curProgress > 0 && curProgress <= maxProgress) {
                float percentage = ((float) curProgress) / maxProgress;
                final int position = (int) (mVideoView.getDuration() * percentage);
                Log.i(TAG, "onStopTrackingTouch: " + position);
                mVideoView.seekTo(position);
            }
            Log.i(TAG, "onStopTrackingTouch: " + PlayerController.generateTime(mVideoView.getCurrentPosition()));
        }
    };

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
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
