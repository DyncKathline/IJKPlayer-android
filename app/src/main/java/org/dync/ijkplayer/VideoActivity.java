package org.dync.ijkplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.dync.ijkplayer.utils.NetworkUtils;
import org.dync.ijkplayer.utils.StatusBarUtil;
import org.dync.ijkplayerlib.widget.media.AndroidMediaController;
import org.dync.ijkplayerlib.widget.media.IRenderView;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;
import org.dync.ijkplayerlib.widget.util.PlayerController;
import org.dync.ijkplayerlib.widget.util.Settings;

import java.util.Locale;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedDurationMilli;
import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedSize;
import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedSpeed;

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
    private LinearLayout app_video_replay;
    private ImageView app_video_replay_icon;

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

        if (app_video_loading != null) {
            app_video_loading.setVisibility(View.VISIBLE);
        }
        mPlayerController = null;

        mPlayerController = new PlayerController(this, mVideoView)
                .setVideoParentLayout(findViewById(R.id.rl_video_view_layout))//建议第一个调用
                .setVideoController((SeekBar) findViewById(R.id.seekbar))
                .setVolumeController()
                .setBrightnessController()
                .setVideoParentRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setVideoRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setPortrait(true)
                .setKeepScreenOn(true)
                .setAutoControlListener(iv_paly)
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
    }

    private void initView() {
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        tv_speed = (TextView) findViewById(R.id.app_video_speed);

        //重新播放
        app_video_replay = (LinearLayout) findViewById(R.id.app_video_replay);
        app_video_replay_icon = (ImageView) findViewById(R.id.app_video_replay_icon);

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
        sbVdieo.setEnabled(false);
        iv_paly.setEnabled(false);
    }

    public void initVideoListener() {
        iv_paly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()) {
                    updatePlayBtnBg(true);
                } else {
                    updatePlayBtnBg(false);
                }
            }
        });
        app_video_replay_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayer();
            }
        });
        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                sbVdieo.setEnabled(true);
                iv_paly.setEnabled(true);
                updatePlayBtnBg(false);
                app_video_replay.setVisibility(View.GONE);
                app_video_replay_icon.setVisibility(View.GONE);

                mVideoView.startVideoInfo();

                mPlayerController
                        .setGestureEnabled(true)
                        .setAutoControlPanel(true);//视频加载后才自动隐藏操作面板
                mPlayerController.setPlayRate(1.0f);
            }
        });
//        mVideoView.setTcpSeepListener(new IjkVideoView.TcpSeepListener() {
//            @Override
//            public void updateSpeed(IMediaPlayer mMediaPlayer, long speed) {
//                if (speed == -1) {
//                    return;
//                }
//                if (app_video_speed != null) {
//                    String formatSize = formatedSpeed(speed, 1000);
//                    Log.d(TAG, "updateSpeed: " + formatSize);
//                    app_video_speed.setText(formatSize);
//                }
//            }
//        });
        mVideoView.setVideoInfoListener(new IjkVideoView.VideoInfoListener() {
            @Override
            public void updateVideoInfo(IMediaPlayer mMediaPlayer) {
                showVideoInfo(mMediaPlayer);
            }
        });
        mVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_STARTED_AS_NEXT://播放下一条
                        Log.d(TAG, "MEDIA_INFO_STARTED_AS_NEXT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://视频开始整备中
                        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                        if (app_video_loading != null) {
                            app_video_loading.setVisibility(View.GONE);
                            app_video_speed.setVisibility(View.GONE);
                            app_video_speed.setText("");
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING://视频日志跟踪
                        Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START://视频缓冲开始
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                        if (!NetworkUtils.isNetworkConnected(mContext)) {
                            updatePlayBtnBg(true);
                        }
                        if (app_video_loading != null) {
                            app_video_loading.setVisibility(View.VISIBLE);
                            app_video_speed.setVisibility(View.VISIBLE);
                            app_video_speed.setText("");
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END://视频缓冲结束
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                        if (app_video_loading != null) {
                            app_video_loading.setVisibility(View.GONE);
                            app_video_speed.setVisibility(View.GONE);
                            app_video_speed.setText("");
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH://网络带宽
                        Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING://
                        Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE://不可设置播放位置，直播方面
                        Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE://视频数据更新
                        Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE: " + extra);
                        break;
                    case IMediaPlayer.MEDIA_INFO_TIMED_TEXT_ERROR://
                        Log.d(TAG, "MEDIA_INFO_TIMED_TEXT_ERROR:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE://不支持字幕
                        Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT://字幕超时
                        Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED://
                        Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START://音频开始整备中
                        Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE://
                        Log.d(TAG, "MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE:");
                        break;
//                    case IMediaPlayer.MEDIA_ERROR_UNKNOWN://
//                        Log.d(TAG, "MEDIA_ERROR_UNKNOWN:");
//                        break;
                    case IMediaPlayer.MEDIA_INFO_UNKNOWN://未知信息
                        Log.d(TAG, "MEDIA_INFO_UNKNOWN or MEDIA_ERROR_UNKNOWN:");
                        break;
                    case IMediaPlayer.MEDIA_ERROR_SERVER_DIED://服务挂掉
                        Log.d(TAG, "MEDIA_ERROR_SERVER_DIED:");
                        break;
                    case IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK://数据错误没有有效的回收
                        Log.d(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:");
                        break;
                    case IMediaPlayer.MEDIA_ERROR_IO://IO 错误
                        Log.d(TAG, "MEDIA_ERROR_IO :");
                        break;
                    case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED://数据不支持
                        Log.d(TAG, "MEDIA_ERROR_UNSUPPORTED :");
                        break;
                    case IMediaPlayer.MEDIA_ERROR_TIMED_OUT://数据超时
                        Log.d(TAG, "MEDIA_ERROR_TIMED_OUT :");
                        break;
                }
                return true;
            }
        });
        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
                if (app_video_loading != null) {
                    app_video_loading.setVisibility(View.GONE);
                    app_video_speed.setVisibility(View.GONE);
                    app_video_speed.setText("");
                }

                app_video_replay.setVisibility(View.VISIBLE);
                app_video_replay_icon.setVisibility(View.VISIBLE);

                if (mPlayerController != null) {
                    mPlayerController
                            .setGestureEnabled(false)
                            .setAutoControlPanel(false);
                }
                final int messageId;

                if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                    messageId = R.string.invalid_video;
                } else {
                    messageId = R.string.small_problem;
                }
                CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
                builder.setCancelable(false)
                        .show(new CustomDialog.Builder.onInitListener() {
                            @Override
                            public void init(final CustomDialog customDialog) {
                                TextView tvTitle = customDialog.getView(R.id.tv_message);
                                Button btnOk = customDialog.getView(R.id.btn_ok);
                                if (tvTitle != null) {
                                    tvTitle.setText(customDialog.getContext().getString(messageId));
                                }
                                if (btnOk != null) {
                                    btnOk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            customDialog.dismiss();
                                        }
                                    });
                                }
                            }
                        });
                return true;
            }
        });
        mVideoView.setOnNativeInvokeListener(new IjkVideoView.OnNativeInvokeListener() {
            @Override
            public boolean onNativeInvoke(IMediaPlayer mediaPlayer, int what, Bundle bundle) {
                Log.d(TAG, "onNativeInvoke: what= " + what + ", bundle= " + bundle);
                switch (what) {
                    case IjkMediaPlayer.OnNativeInvokeListener.EVENT_WILL_HTTP_OPEN:
                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, error=0, http_code=0}]
                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, error=0, http_code=0}]
                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, error=0, http_code=0}]
                        break;
                    case IjkMediaPlayer.OnNativeInvokeListener.EVENT_DID_HTTP_OPEN:
                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, error=0, http_code=200}]
                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, error=-101, http_code=0}]
                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, error=-5, http_code=0}]
                        int error = bundle.getInt("error", 404);
                        int http_code = bundle.getInt("http_code", 404);
                        if (error == -101) {//断网了
                            int bufferPercentage = mVideoView.getBufferPercentage();
                            Log.d(TAG, "onNativeInvoke: bufferPercentage=" + bufferPercentage + "%");
//                            updatePlayBtnBg(true);
                        }
                        break;
                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_WILL_TCP_OPEN:
                        //what= 131073, bundle= Bundle[{family=0, fd=0, ip=, port=0, error=0}]
                        //what= 131073, bundle= Bundle[{family=0, fd=0, ip=, port=0, error=0}]
                        break;
                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_DID_TCP_OPEN:
                        //what= 131074, bundle= Bundle[{family=2, fd=64, ip=118.178.143.146, port=20480, error=0}]
                        break;
                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_WILL_HTTP_OPEN:
                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, retry_counter=0}]
                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, retry_counter=1}]
                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=0}]
                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=1}]
                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=0}]
                        break;
                }
                return false;
            }
        });
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
        TextView fps = (TextView) findViewById(R.id.fps);
        TextView v_cache = (TextView) findViewById(R.id.v_cache);
        TextView a_cache = (TextView) findViewById(R.id.a_cache);
        TextView seek_load_cost = (TextView) findViewById(R.id.seek_load_cost);
        TextView tcp_speed = (TextView) findViewById(R.id.tcp_speed);
        TextView bit_rate = (TextView) findViewById(R.id.bit_rate);

        if (mMediaPlayer instanceof IjkMediaPlayer) {
            IjkMediaPlayer mp = (IjkMediaPlayer) mMediaPlayer;

            float fpsOutput = mp.getVideoOutputFramesPerSecond();
            float fpsDecode = mp.getVideoDecodeFramesPerSecond();
            long videoCachedDuration = mp.getVideoCachedDuration();
            long audioCachedDuration = mp.getAudioCachedDuration();
            long videoCachedBytes = mp.getVideoCachedBytes();
            long audioCachedBytes = mp.getAudioCachedBytes();
            long tcpSpeed = mp.getTcpSpeed();
            long bitRate = mp.getBitRate();
            long seekLoadDuration = mp.getSeekLoadDuration();

            mPlayerController.setVideoInfo(fps, String.format(Locale.US, "%.2f / %.2f", fpsDecode, fpsOutput));
            mPlayerController.setVideoInfo(v_cache, String.format(Locale.US, "%s, %s", formatedDurationMilli(videoCachedDuration), formatedSize(videoCachedBytes)));
            mPlayerController.setVideoInfo(a_cache, String.format(Locale.US, "%s, %s", formatedDurationMilli(audioCachedDuration), formatedSize(audioCachedBytes)));
            mPlayerController.setVideoInfo(seek_load_cost, String.format(Locale.US, "%d ms", seekLoadDuration));
            mPlayerController.setVideoInfo(tcp_speed, String.format(Locale.US, "%s", formatedSpeed(tcpSpeed, 1000)));
            mPlayerController.setVideoInfo(bit_rate, String.format(Locale.US, "%.2f kbs", bitRate / 1000f));

            if (tcpSpeed == -1) {
                return;
            }
            if (app_video_speed != null) {
                String formatSize = formatedSpeed(tcpSpeed, 1000);
                app_video_speed.setText(formatSize);
            }
            if (videoCachedDuration == 0) {//没有缓存了，如果断网
                if(NetworkUtils.isNetworkConnected(mContext)){
                    int currentPosition = mVideoView.getCurrentPosition();
                    mPlayerController.seekTo(currentPosition);
                    updatePlayBtnBg(false);
                    iv_paly.setEnabled(true);
                }else {
                    updatePlayBtnBg(true);
                    iv_paly.setEnabled(false);
                }
            }
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
                resid = R.drawable.simple_player_center_play;
                mVideoView.pause();
            } else {
                // 播放
                resid = R.drawable.simple_player_center_pause;
                mVideoView.start();
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
            updatePlayBtnBg(true);
        } else {
            mVideoView.enterBackground();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerController != null) {
            mPlayerController.onDestroy();
        }
        if (mVideoView != null) {
            mVideoView.stopVideoInfo();
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
