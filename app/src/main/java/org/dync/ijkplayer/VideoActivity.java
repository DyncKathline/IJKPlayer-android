package org.dync.ijkplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.dync.ijkplayer.utils.GlideUtil;
import org.dync.ijkplayer.utils.NetworkUtils;
import org.dync.ijkplayer.utils.StatusBarUtil;
import org.dync.ijkplayer.utils.ThreadUtil;
import org.dync.ijkplayerlib.widget.media.AndroidMediaController;
import org.dync.ijkplayerlib.widget.media.IRenderView;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;
import org.dync.ijkplayerlib.widget.util.PlayerController;
import org.dync.ijkplayerlib.widget.util.Settings;
import org.dync.ijkplayerlib.widget.util.WindowManagerUtil;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedDurationMilli;
import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedSize;
import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedSpeed;

public class VideoActivity extends BaseActivity {
    private static final String TAG = "VideoActivity";
    private String mVideoPath;
    private Uri mVideoUri;

    private AndroidMediaController mMediaController;
    private PlayerController mPlayerController;

    private boolean mBackPressed;
    private String mVideoCoverUrl;


    @BindView(R.id.video_view)
    IjkVideoView videoView;
    @BindView(R.id.video_cover)
    ImageView videoCover;
    @BindView(R.id.app_video_status_text)
    TextView appVideoStatusText;
    @BindView(R.id.app_video_replay_icon)
    ImageView appVideoReplayIcon;
    @BindView(R.id.app_video_replay)
    LinearLayout appVideoReplay;
    @BindView(R.id.app_video_netTie_icon)
    TextView appVideoNetTieIcon;
    @BindView(R.id.app_video_netTie)
    LinearLayout appVideoNetTie;
    @BindView(R.id.app_video_freeTie_icon)
    TextView appVideoFreeTieIcon;
    @BindView(R.id.app_video_freeTie)
    LinearLayout appVideoFreeTie;
    @BindView(R.id.app_video_speed)
    TextView appVideoSpeed;
    @BindView(R.id.app_video_loading)
    LinearLayout appVideoLoading;
    @BindView(R.id.app_video_volume_icon)
    ImageView appVideoVolumeIcon;
    @BindView(R.id.app_video_volume)
    TextView appVideoVolume;
    @BindView(R.id.app_video_volume_box)
    LinearLayout appVideoVolumeBox;
    @BindView(R.id.app_video_brightness_icon)
    ImageView appVideoBrightnessIcon;
    @BindView(R.id.app_video_brightness)
    TextView appVideoBrightness;
    @BindView(R.id.app_video_brightness_box)
    LinearLayout appVideoBrightnessBox;
    @BindView(R.id.app_video_fastForward)
    TextView appVideoFastForward;
    @BindView(R.id.app_video_fastForward_target)
    TextView appVideoFastForwardTarget;
    @BindView(R.id.app_video_fastForward_all)
    TextView appVideoFastForwardAll;
    @BindView(R.id.app_video_fastForward_box)
    LinearLayout appVideoFastForwardBox;
    @BindView(R.id.app_video_center_box)
    FrameLayout appVideoCenterBox;
    @BindView(R.id.play_icon)
    ImageView playIcon;
    @BindView(R.id.tv_current_time)
    TextView tvCurrentTime;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.tv_total_time)
    TextView tvTotalTime;
    @BindView(R.id.img_change_screen)
    ImageView imgChangeScreen;
    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.rl_video_view_layout)
    RelativeLayout rlVideoViewLayout;
    @BindView(R.id.btn_ratio)
    Button btnRatio;
    @BindView(R.id.btn_rotation)
    Button btnRotation;
    @BindView(R.id.btn_ijk_player)
    Button btnIjkPlayer;
    @BindView(R.id.btn_exo_player)
    Button btnExoPlayer;
    @BindView(R.id.sp_speed)
    Spinner spSpeed;
    @BindView(R.id.btn_window_player)
    Button btnWindowPlayer;
    @BindView(R.id.btn_app_player)
    Button btnAppPlayer;
    @BindView(R.id.horizontalScrollView)
    HorizontalScrollView horizontalScrollView;
    @BindView(R.id.fps)
    TextView fps;
    @BindView(R.id.v_cache)
    TextView vCache;
    @BindView(R.id.a_cache)
    TextView aCache;
    @BindView(R.id.seek_load_cost)
    TextView seekLoadCost;
    @BindView(R.id.tcp_speed)
    TextView tcpSpeed;
    @BindView(R.id.bit_rate)
    TextView bitRate;
    @BindView(R.id.iv_preview)
    ImageView ivPreview;
    @BindView(R.id.ll_video_info)
    LinearLayout llVideoInfo;
    @BindView(R.id.fl_video_url)
    FrameLayout flVideoUrl;
    @BindView(R.id.fl_app_window)
    FrameLayout flAppWindow;
    @BindView(R.id.app_video_box)
    RelativeLayout appVideoBox;

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
        ButterKnife.bind(this);

        mContext = this;

        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");
        mVideoCoverUrl = "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=491343424,3697954862&fm=27&gp=0.jpg";

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

        initVideoControl();
        initPlayer();
        initFragment();
        initListener();
        initVideoListener();

        StatusBarUtil.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary));
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ijk_player:
                mPlayerController.switchPlayer(Settings.PV_PLAYER__IjkMediaPlayer);
                break;
            case R.id.btn_exo_player:
                mPlayerController.switchPlayer(Settings.PV_PLAYER__IjkExoMediaPlayer);
                break;
            case R.id.btn_rotation:
                mPlayerController.toogleVideoRotation();
//                mPlayerController.setPlayerRotation(90);
                break;
            case R.id.btn_ratio:
                mPlayerController.toggleAspectRatio();
                break;
            case R.id.btn_window_player:
                WindowManagerUtil.createSmallWindow(mContext, videoView.getMediaPlayer());
                break;
            case R.id.btn_app_player:
//                WindowManagerUtil.createSmallWindow(flAppWindow, videoView.getMediaPlayer());
                break;
        }
    }

    private void initPlayer() {
        //        ActionBar actionBar = getSupportActionBar();
//        mMediaController = new AndroidMediaController(this, false);
//        mMediaController.setSupportActionBar(actionBar);
//        mVideoView.setMediaController(mMediaController);

        showVideoLoading();
        mPlayerController = null;

        mPlayerController = new PlayerController(this, videoView)
                .setVideoParentLayout(findViewById(R.id.rl_video_view_layout))//建议第一个调用
                .setVideoController((SeekBar) findViewById(R.id.seekbar))
                .setVolumeController()
                .setBrightnessController()
                .setVideoParentRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setVideoRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setPortrait(true)
                .setKeepScreenOn(true)
                .setAutoControlListener(llBottom)//触摸以下控件可以取消自动隐藏布局的线程
                .setPanelControl(new PlayerController.PanelControlListener() {
                    @Override
                    public void operatorPanel(boolean isShowControlPanel) {
                        if (isShowControlPanel) {
                            llBottom.setVisibility(View.VISIBLE);
                        } else {
                            llBottom.setVisibility(View.GONE);
                        }
                    }
                })
                .setSyncProgressListener(new PlayerController.SyncProgressListener() {
                    @Override
                    public void syncTime(long position, long duration) {
                        tvCurrentTime.setText(mPlayerController.generateTime(position));
                        tvTotalTime.setText(mPlayerController.generateTime(duration));
                    }
                })
                .setGestureListener(new PlayerController.GestureListener() {
                    @Override
                    public void onProgressSlide(long newPosition, long duration, int showDelta) {
                        if (showDelta != 0) {
                            appVideoFastForwardBox.setVisibility(View.VISIBLE);
                            appVideoBrightnessBox.setVisibility(View.GONE);
                            appVideoVolumeBox.setVisibility(View.GONE);
                            appVideoFastForwardTarget.setVisibility(View.VISIBLE);
                            appVideoFastForwardAll.setVisibility(View.VISIBLE);
                            appVideoFastForwardTarget.setText(mPlayerController.generateTime(newPosition) + "/");
                            appVideoFastForwardAll.setText(mPlayerController.generateTime(duration));

                            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
                            appVideoFastForward.setVisibility(View.VISIBLE);
                            appVideoFastForward.setText(String.format("%ss", text));
                        }
                    }

                    @Override
                    public void onVolumeSlide(int volume) {
                        appVideoFastForwardBox.setVisibility(View.GONE);
                        appVideoBrightnessBox.setVisibility(View.GONE);
                        appVideoVolumeBox.setVisibility(View.VISIBLE);
                        appVideoVolume.setVisibility(View.VISIBLE);
                        appVideoVolume.setText(volume + "%");
                    }

                    @Override
                    public void onBrightnessSlide(float brightness) {
                        appVideoFastForwardBox.setVisibility(View.GONE);
                        appVideoBrightnessBox.setVisibility(View.VISIBLE);
                        appVideoVolumeBox.setVisibility(View.GONE);
                        appVideoBrightness.setVisibility(View.VISIBLE);
                        appVideoBrightness.setText((int) (brightness * 100) + "%");
                    }

                    @Override
                    public void endGesture() {
                        appVideoFastForwardBox.setVisibility(View.GONE);
                        appVideoBrightnessBox.setVisibility(View.GONE);
                        appVideoVolumeBox.setVisibility(View.GONE);
                    }
                });

        // prefer mVideoPath
//        Settings settings = new Settings(this);
//        settings.setPlayer(Settings.PV_PLAYER__IjkMediaPlayer);
//        if (mVideoPath != null)
//            videoView.setVideoPath(mVideoPath);
//        else if (mVideoUri != null)
//            videoView.setVideoURI(mVideoUri);
//        else {
//            Log.e(TAG, "Null Data Source\n");
//            finish();
//            return;
//        }
//        videoView.start();
        onDestroyVideo();
        if (mVideoPath != null) {
            showVideoLoading();
            videoView.setVideoPath(mVideoPath);
            videoView.start();
        }
    }

    private void initFragment() {
        SampleMediaListFragment videoUrlFragment = SampleMediaListFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_video_url, videoUrlFragment);
        fragmentTransaction.commit();

        videoUrlFragment.setOnItemClickListener(new SampleMediaListFragment.OnItemClickListener() {
            @Override
            public void OnItemClick(Context context, String videoPath, String videoTitle) {
                onDestroyVideo();
                mVideoPath = videoPath;
                Log.d(TAG, "OnItemClick: mVideoPath: " + mVideoPath);
                if (mVideoPath != null) {
                    showVideoLoading();
                    videoView.setVideoPath(mVideoPath);
                    videoView.start();
                }
            }
        });
    }

    private void initListener() {
        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying()) {
                    updatePlayBtnBg(true);
                } else {
                    updatePlayBtnBg(false);
                }
            }
        });
        imgChangeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerController != null) {
                    if (mPlayerController.isPortrait()) {
                        updateFullScreenBg(true);
                    } else {
                        updateFullScreenBg(false);
                    }
                    mPlayerController.toggleScreenOrientation();
                }
            }
        });
        appVideoReplayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayer();
            }
        });
        Spinner sp_speed = (Spinner) findViewById(R.id.sp_speed);
        final String[] speeds = {"倍速播放", "0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speeds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sp_speed.setAdapter(adapter);
        sp_speed.setSelection(0, true);
        sp_speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                try {
                    float parseFloat = Float.parseFloat(speeds[pos]);
                    videoView.setSpeed(parseFloat);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    private void initVideoListener() {
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                appVideoReplay.setVisibility(View.GONE);
                appVideoReplayIcon.setVisibility(View.GONE);

                videoView.startVideoInfo();

                mPlayerController
                        .setGestureEnabled(true)
                        .setAutoControlPanel(true);//视频加载后才自动隐藏操作面板
                mPlayerController.setSpeed(1.0f);
            }
        });
        videoView.setVideoInfoListener(new IjkVideoView.VideoInfoListener() {
            @Override
            public void updateVideoInfo(IMediaPlayer mMediaPlayer) {
                showVideoInfo(mMediaPlayer);
            }
        });
        final ArrayList<Integer> audios = new ArrayList<>();
        //音频软解成功通知
        audios.add(IMediaPlayer.MEDIA_INFO_OPEN_INPUT);
        audios.add(IMediaPlayer.MEDIA_INFO_FIND_STREAM_INFO);
        audios.add(IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN);
        audios.add(IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED);
        audios.add(IMediaPlayer.MEDIA_INFO_AUDIO_DECODED_START);
        audios.add(IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START);
        final ArrayList<Integer> temp_audios = new ArrayList<>();
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                Log.d(TAG, "onInfo: what= " + what + ", extra= " + extra);
                if(what == IMediaPlayer.MEDIA_INFO_OPEN_INPUT) {
                    temp_audios.clear();
                    temp_audios.add(what);
                }else if(temp_audios.size() < 6) {
                    temp_audios.add(what);
                }
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_STARTED_AS_NEXT://播放下一条
                        Log.d(TAG, "MEDIA_INFO_STARTED_AS_NEXT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://视频开始整备中
                        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                        hideVideoLoading();
                        seekbar.setEnabled(true);
                        playIcon.setEnabled(true);
                        updatePlayBtnBg(false);
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START://音频开始整备中
                        Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                        hideVideoLoading();
                        seekbar.setEnabled(true);
                        playIcon.setEnabled(true);
                        updatePlayBtnBg(false);
                        if (!temp_audios.contains(IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)) {
                            if (!TextUtils.isEmpty(mVideoCoverUrl)) {
                                GlideUtil.showImg(mContext, mVideoCoverUrl, videoCover);
                            }
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN:
                        hideVideoLoading();
                        seekbar.setEnabled(true);
                        playIcon.setEnabled(true);
                        updatePlayBtnBg(false);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START://视频缓冲开始
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                        if (!NetworkUtils.isNetworkConnected(mContext)) {
                            updatePlayBtnBg(true);
                        }
                        showVideoLoading();
                        ThreadUtil.runInThread(new Runnable() {
                            @Override
                            public void run() {
                                if(temp_audios.get(0) == IMediaPlayer.MEDIA_INFO_OPEN_INPUT) {
                                    for (int i = 0; i < temp_audios.size(); i++) {
                                        if(!audios.get(i).equals(temp_audios.get(i))) {
                                            onDestroyVideo();
                                            if (mVideoPath != null) {
                                                videoView.setVideoPath(mVideoPath);
                                                videoView.start();
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        });
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END://视频缓冲结束
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                        hideVideoLoading();
                        seekbar.setEnabled(true);
                        playIcon.setEnabled(true);
                        updatePlayBtnBg(!videoView.isPlaying());
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING://视频日志跟踪
                        Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                        break;
//                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH://网络带宽
//                        Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING://
//                        Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE://不可设置播放位置，直播方面
//                        Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE://视频数据更新
//                        Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE: " + extra);
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_TIMED_TEXT_ERROR://
//                        Log.d(TAG, "MEDIA_INFO_TIMED_TEXT_ERROR:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE://不支持字幕
//                        Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT://字幕超时
//                        Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED://
//                        Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE://
//                        Log.d(TAG, "MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE:");
//                        break;
////                    case IMediaPlayer.MEDIA_ERROR_UNKNOWN://
////                        Log.d(TAG, "MEDIA_ERROR_UNKNOWN:");
////                        break;
//                    case IMediaPlayer.MEDIA_INFO_UNKNOWN://未知信息
//                        Log.d(TAG, "MEDIA_INFO_UNKNOWN or MEDIA_ERROR_UNKNOWN:");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_SERVER_DIED://服务挂掉
//                        Log.d(TAG, "MEDIA_ERROR_SERVER_DIED:");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK://数据错误没有有效的回收
//                        Log.d(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_IO://IO 错误
//                        Log.d(TAG, "MEDIA_ERROR_IO :");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED://数据不支持
//                        Log.d(TAG, "MEDIA_ERROR_UNSUPPORTED :");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_TIMED_OUT://数据超时
//                        Log.d(TAG, "MEDIA_ERROR_TIMED_OUT :");
//                        break;
                }
                return true;
            }
        });
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                updatePlayBtnBg(true);
                videoView.release(false);
                videoView.stopVideoInfo();
                initVideoControl();
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
                hideVideoLoading();

                appVideoReplay.setVisibility(View.VISIBLE);
                appVideoReplayIcon.setVisibility(View.VISIBLE);

                if (mPlayerController != null) {
                    mPlayerController
                            .setGestureEnabled(false)
                            .setAutoControlPanel(false);
                }
                videoView.stopVideoInfo();
                return true;
            }
        });
//        videoView.setOnNativeInvokeListener(new IjkVideoView.OnNativeInvokeListener() {
//            @Override
//            public boolean onNativeInvoke(IMediaPlayer mediaPlayer, int what, Bundle bundle) {
//                Log.w(TAG, "onNativeInvoke: what= " + what + ", bundle= " + bundle);
//                int error, http_code;
//                switch (what) {
//                    case IjkMediaPlayer.OnNativeInvokeListener.EVENT_WILL_HTTP_OPEN:
//                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, error=0, http_code=0}]
//                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, error=0, http_code=0}]
//                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, error=0, http_code=0}]
//                        break;
//                    case IjkMediaPlayer.OnNativeInvokeListener.EVENT_DID_HTTP_OPEN:
//                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, error=0, http_code=200}]
//                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, error=-101, http_code=0}]
//                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, error=-5, http_code=0}]
//                        error = bundle.getInt("error");
//                        http_code = bundle.getInt("http_code");
//                        if (error == -101) {//断网了
//
//                        }
//                        if(http_code == 200) {
//                            hideVideoLoading();
//                        }
//                        break;
//                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_WILL_TCP_OPEN:
//                        //what= 131073, bundle= Bundle[{family=0, fd=0, ip=, port=0, error=0}]
//                        //what= 131073, bundle= Bundle[{family=0, fd=0, ip=, port=0, error=0}]
//                        break;
//                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_DID_TCP_OPEN:
//                        //what= 131074, bundle= Bundle[{family=2, fd=64, ip=118.178.143.146, port=20480, error=0}]
//                        break;
//                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_WILL_HTTP_OPEN:
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, retry_counter=0}]
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, retry_counter=1}]
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=0}]
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=1}]
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=0}]
//                        break;
//                }
//                return true;
//            }
//        });
    }

    private void showVideoLoading() {
        if (appVideoLoading != null) {
            appVideoLoading.setVisibility(View.VISIBLE);
            appVideoSpeed.setVisibility(View.VISIBLE);
            appVideoSpeed.setText("");
        }
    }

    private void hideVideoLoading() {
        if (appVideoLoading != null) {
            appVideoLoading.setVisibility(View.GONE);
            appVideoSpeed.setVisibility(View.GONE);
            appVideoSpeed.setText("");
        }
    }

    private void initVideoControl() {
        playIcon.setEnabled(false);
        seekbar.setEnabled(false);
        seekbar.setProgress(0);
    }

    private void showVideoInfo(IMediaPlayer mMediaPlayer) {
//        LinearLayout ll_video_info = (LinearLayout) findViewById(R.id.ll_video_info);
//        if(mVideoView != null) {
//            ITrackInfo[] trackInfos = mVideoView.getTrackInfo();
//            for(ITrackInfo trackInfo: trackInfos) {
//                final CheckBox checkBox = new CheckBox(ll_video_info.getContext());
//
//                String infoInline = String.format(Locale.US, "%s", trackInfo.getInfoInline());
//                final int trackType = trackInfo.getTrackType()-1;//不知道为什么不跟ITrackInfo类中的参数一致，而是减1
//                checkBox.setText(infoInline);
//                checkBox.setChecked(true);
//                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                        if(mVideoView != null) {
//                            if(b) {
//                                mVideoView.selectTrack(trackType);
//                            }else {
//                                mVideoView.deselectTrack(trackType);
//                            }
//                        }
//                    }
//                });
//                ll_video_info.addView(checkBox);
//            }
//        }

        if (mMediaPlayer != null && mMediaPlayer instanceof IjkMediaPlayer) {
            IjkMediaPlayer mp = (IjkMediaPlayer) mMediaPlayer;

            float fpsOutput = mp.getVideoOutputFramesPerSecond();
            float fpsDecode = mp.getVideoDecodeFramesPerSecond();
            long videoCachedDuration = mp.getVideoCachedDuration();
            long audioCachedDuration = mp.getAudioCachedDuration();
            long videoCachedBytes = mp.getVideoCachedBytes();
            long audioCachedBytes = mp.getAudioCachedBytes();
            long tcpSpeeds = mp.getTcpSpeed();
            long bitRates = mp.getBitRate();
            long seekLoadDuration = mp.getSeekLoadDuration();

            mPlayerController.setVideoInfo(fps, String.format(Locale.US, "%.2f / %.2f", fpsDecode, fpsOutput));
            mPlayerController.setVideoInfo(vCache, String.format(Locale.US, "%s, %s", formatedDurationMilli(videoCachedDuration), formatedSize(videoCachedBytes)));
            mPlayerController.setVideoInfo(aCache, String.format(Locale.US, "%s, %s", formatedDurationMilli(audioCachedDuration), formatedSize(audioCachedBytes)));
            mPlayerController.setVideoInfo(seekLoadCost, String.format(Locale.US, "%d ms", seekLoadDuration));
            mPlayerController.setVideoInfo(tcpSpeed, String.format(Locale.US, "%s", formatedSpeed(tcpSpeeds)));
            mPlayerController.setVideoInfo(bitRate, String.format(Locale.US, "%.2f kbs", bitRates / 1000f));

            if (tcpSpeeds == -1) {
                return;
            }
            if (appVideoSpeed != null) {
                String formatSize = formatedSpeed(tcpSpeeds);
                appVideoSpeed.setText(formatSize);
            }
//            if (videoCachedDuration == 0) {//没有缓存了，如果断网
//                if (NetworkUtils.isNetworkConnected(mContext)) {
//                    int currentPosition = videoView.getCurrentPosition();
//                    mPlayerController.seekTo(currentPosition);
//                    updatePlayBtnBg(false);
//                    playIcon.setEnabled(true);
//                } else {
//                    updatePlayBtnBg(true);
//                    playIcon.setEnabled(false);
//                }
//            }
        }
    }

    /**
     * 更新播放按钮的背景图片，正在播放
     */
    private void updatePlayBtnBg(boolean isPlay) {
        try {
            int resid;
            if (isPlay) {
                // 暂停
                resid = R.drawable.simple_player_center_play;
                videoView.pause();
            } else {
                // 播放
                resid = R.drawable.simple_player_center_pause;
                videoView.start();
            }
            playIcon.setImageResource(resid);
        } catch (Exception e) {

        }

    }

    /**
     * 更新全屏按钮的背景图片
     */
    private void updateFullScreenBg(boolean isFullSrceen) {
        try {
            int resid;
            if (isFullSrceen) {
                // 全屏
                resid = R.drawable.simple_player_icon_fullscreen_shrink;
            } else {
                // 非全屏
                resid = R.drawable.simple_player_icon_fullscreen_stretch;
            }
            imgChangeScreen.setBackgroundResource(resid);
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!videoView.isBackgroundPlayEnabled()) {
            updatePlayBtnBg(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !videoView.isBackgroundPlayEnabled()) {
//            mVideoView.stopPlayback();
//            mVideoView.release(true);
//            mVideoView.stopBackgroundPlay();
            updatePlayBtnBg(true);
        } else {
            videoView.enterBackground();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyVideo();
        WindowManagerUtil.removeSmallWindow(mContext);
    }

    private void onDestroyVideo() {
        if (appVideoReplay != null) {
            appVideoReplay.setVisibility(View.GONE);
        }
        if (appVideoReplayIcon != null) {
            appVideoReplayIcon.setVisibility(View.GONE);
        }
        if (mPlayerController != null) {
            mPlayerController.onDestroy();
        }
        if (videoView != null) {
            videoView.stopPlayback();
            videoView.release(true);
            videoView.stopBackgroundPlay();
            videoView.stopVideoInfo();
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
}
