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

import org.dync.ijkplayerlib.widget.media.AndroidMediaController;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;
import org.dync.ijkplayerlib.widget.media.MeasureHelper;
import org.dync.ijkplayerlib.widget.util.PlayerController;
import org.dync.ijkplayerlib.widget.util.Settings;

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

    private SeekBar sbVdieo;
    private ImageView iv_paly;
    private LinearLayout app_video_fastForward_box;

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

        mPlayerController = new PlayerController(this, mVideoView)
                .operatorPanl()
                .setVideoRootLayout(findViewById(R.id.app_video_box))
                .setVideoController((SeekBar) findViewById(R.id.seekbar))
                .setVolumeController()
                .setBrightnessController()
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

        initVideoListener();

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
    }

    private void initView() {
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        tv_speed = (TextView) findViewById(R.id.app_video_speed);

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
        iv_paly = (ImageView) findViewById(R.id.play_icon);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        tv_total_time = (TextView) findViewById(R.id.tv_total_time);
        sbVdieo = (SeekBar) findViewById(R.id.seekbar);
    }

    public void initVideoListener() {
        iv_paly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mVideoView.isPlaying()){
                    mVideoView.pause();
                    updatePlayBtnBg(true);
                }else {
                    mVideoView.start();
                    updatePlayBtnBg(false);
                }
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
                resid = R.drawable.simple_player_center_pause;
            } else {
                // 播放
                resid = R.drawable.simple_player_center_play;
            }
            iv_paly.setImageResource(resid);
        }catch (Exception e){

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
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mPlayerController != null) {
            mPlayerController.onConfigurationChanged(newConfig);
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
