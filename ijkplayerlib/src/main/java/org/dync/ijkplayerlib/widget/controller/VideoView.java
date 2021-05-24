package org.dync.ijkplayerlib.widget.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import org.dync.ijkplayerlib.R;
import org.dync.ijkplayerlib.widget.controller.impl.AnimationImpl;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerBottomImpl;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerImpl;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerTitleBarImpl;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerVolumeBrightImpl;
import org.dync.ijkplayerlib.widget.controller.util.NetworkUtil;
import org.dync.ijkplayerlib.widget.media.IRenderView;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;
import org.dync.ijkplayerlib.widget.util.PlayerController;
import org.dync.ijkplayerlib.widget.util.Settings;
import org.dync.ijkplayerlib.widget.util.Utils;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by KathLine on 2017/12/13.
 */

public class VideoView extends RelativeLayout {

    private static final String TAG = "VideoView";
    private Context mContext;

    private RelativeLayout rlVideoParent;
    private IjkVideoView videoView;
    private ImageView ivCover;
    private PlayerTitleBar playerTitleBar;
    private PlayerBottom playerBottom;
    private ProgressBar bottomProgress;
    private LinearLayout appVideoReplay;
    private TextView appVideoReplayText;
    private ImageView appVideoReplayIcon;
    private LinearLayout appVideoRetry;
    private TextView appVideoStatusText;
    private ImageView appVideoRetryIcon;
    private LinearLayout appVideoNetTie;
    private TextView appVideoNetTieIcon;
    private PlayerVolumeBright playerVolumeBright;
    private LinearLayout llLoading;
    private ProgressBar progressBar;
    private TextView tvSpeed;

    private Animation mEnterFromTop;
    private Animation mEnterFromBottom;
    private Animation mExitFromTop;
    private Animation mExitFromBottom;

    private PlayerController mPlayerController;
    private Activity mActivity;
    private Uri mVideoUri;//mVideoUri.getPath();得到String地址
    private String mVideoCoverUrl;

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(Activity activity, String path) {
        setVideoURI(activity, Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Activity activity, Uri uri) {
        mActivity = activity;
        mVideoUri = uri;
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            Log.e(TAG, "Null unknown scheme\n");
            return;
        }

        initPlayer();
    }

    /**
     * 播放器控制功能对外开放接口,包括返回按钮,播放等...
     */
    public void setPlayerController(IPlayerImpl IPlayerImpl) {
        mIPlayerImpl = IPlayerImpl;
    }

    private IPlayerImpl mIPlayerImpl = null;

    public VideoView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        inflate(context, R.layout.video_view, this);

        rlVideoParent = (RelativeLayout) findViewById(R.id.rl_video_parent);
        videoView = (IjkVideoView) findViewById(R.id.ijk_video_view);
        ivCover = (ImageView) findViewById(R.id.iv_cover);
        playerTitleBar = (PlayerTitleBar) findViewById(R.id.player_title_bar);
        playerBottom = (PlayerBottom) findViewById(R.id.player_bottom);
        bottomProgress = (ProgressBar) findViewById(R.id.bottom_progress);
        appVideoReplay = (LinearLayout) findViewById(R.id.app_video_replay);
        appVideoReplayText = (TextView) findViewById(R.id.app_video_replay_text);
        appVideoReplayIcon = (ImageView) findViewById(R.id.app_video_replay_icon);
        appVideoRetry = (LinearLayout) findViewById(R.id.app_video_retry);
        appVideoStatusText = (TextView) findViewById(R.id.app_video_status_text);
        appVideoRetryIcon = (ImageView) findViewById(R.id.app_video_retry_icon);
        appVideoNetTie = (LinearLayout) findViewById(R.id.app_video_netTie);
        appVideoNetTieIcon = (TextView) findViewById(R.id.app_video_netTie_icon);
        playerVolumeBright = (PlayerVolumeBright) findViewById(R.id.player_volume_bright);
        llLoading = (LinearLayout) findViewById(R.id.ll_loading);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);

        playerBottom.toggleExpandable(false);
        initAnimation();
        initListener();
        initVideoListener();
    }

    private void initListener() {
        playerTitleBar.setTitleBarImpl(new IPlayerTitleBarImpl() {
            @Override
            public void onBackClick() {
                if (mIPlayerImpl != null) {
                    mIPlayerImpl.onBack();
                } else {
                    if (mActivity != null) {
                        mActivity.finish();
                    }
                }
            }
        });
        playerVolumeBright.setPlayerVolumeBrightImpl(new IPlayerVolumeBrightImpl() {

            @Override
            public void onComplete() {

            }
        });
        playerBottom.setPlayerBottomImpl(new IPlayerBottomImpl() {
            @Override
            public void onPlayTurn() {
                if (videoView.isPlaying()) {
                    updatePlayState(true);
                } else {
                    updatePlayState(false);
                }
            }

            @Override
            public void onProgressChange(int state, int progress) {

            }

            @Override
            public void onOrientationChange() {

            }
        });
        appVideoReplayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayer();
            }
        });
        appVideoRetryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayer();
            }
        });
    }

    private void initVideoListener() {
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                appVideoReplay.setVisibility(View.GONE);
                appVideoRetry.setVisibility(View.GONE);
                hideVideoLoading();
                if(videoView.getDuration() > 1) {//exoplayer如果是直播流返回1
                    playerBottom.getSeekBar().setEnabled(true);
                }else {
                    playerBottom.getIvPlayPause().setEnabled(false);
                }
                if(!Utils.isWifiConnected(mActivity) && !mPlayerController.isLocalDataSource(mVideoUri) && !PlayerController.WIFI_TIP_DIALOG_SHOWED) {
//                    mPlayerController.showWifiDialog();
                }else {
                    updatePlayState(false);
                }

                videoView.startVideoInfo();
                if (!videoView.hasVideoTrackInfo()) {
                    if (!TextUtils.isEmpty(mVideoCoverUrl)) {
                        Glide.with(mContext).load(mVideoCoverUrl).into(ivCover);
                    }
                }else {
                    ivCover.setImageDrawable(new ColorDrawable(0));
                }

                mPlayerController
                        .setGestureEnabled(true)
                        .setAutoControlPanel(true);//视频加载后才自动隐藏操作面板
                mPlayerController.setSpeed(1.0f);
            }
        });
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                videoView.release(false);
                videoView.stopVideoInfo();
                appVideoReplay.setVisibility(View.VISIBLE);
                appVideoRetry.setVisibility(View.GONE);
                playerBottom.getIvPlayPause().setEnabled(false);
                playerBottom.initVideoControl();
                playerBottom.updatePlayState(false);
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                hideVideoLoading();

                if (mPlayerController != null) {
                    mPlayerController
                            .setGestureEnabled(false)
                            .setAutoControlPanel(false);
                }
                videoView.stopVideoInfo();
                appVideoReplay.setVisibility(View.GONE);
                appVideoRetry.setVisibility(View.VISIBLE);
                playerBottom.getIvPlayPause().setEnabled(false);
                playerBottom.initVideoControl();
                playerBottom.updatePlayState(false);
                return true;
            }
        });
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                Log.d(TAG, "onInfo: what= " + what + ", extra= " + extra);
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_STARTED_AS_NEXT://播放下一条
                        Log.d(TAG, "MEDIA_INFO_STARTED_AS_NEXT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://视频开始整备中
                        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
//                        hideVideoLoading();
//                        playerBottom.getSeekBar().setEnabled(true);
//                        playerBottom.getIvPlayPause().setEnabled(true);
//                        updatePlayState(false);
//                        ivCover.setImageDrawable(new ColorDrawable(0));
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START://音频开始整备中
                        Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
//                        hideVideoLoading();
//                        playerBottom.getSeekBar().setEnabled(true);
//                        playerBottom.getIvPlayPause().setEnabled(true);
//                        updatePlayState(false);
                        break;
                    case IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN:
//                        hideVideoLoading();
//                        playerBottom.getSeekBar().setEnabled(true);
//                        playerBottom.getIvPlayPause().setEnabled(true);
//                        updatePlayState(false);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START://视频缓冲开始
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                        showVideoLoading();
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END://视频缓冲结束
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                        hideVideoLoading();
//                        playerBottom.getSeekBar().setEnabled(true);
//                        playerBottom.getIvPlayPause().setEnabled(true);
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
    }

    public void initPlayer() {
        appVideoReplay.setVisibility(View.GONE);
        appVideoRetry.setVisibility(View.GONE);
        mPlayerController = null;
        mPlayerController = new PlayerController(mActivity, videoView)
                .setVideoParentLayout(rlVideoParent)
                .setVideoController(playerBottom.getSeekBar())
                .setVolumeController()
                .setBrightnessController()
                .setVideoParentRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setVideoRatio(IRenderView.AR_16_9_FIT_PARENT)
//                .setPortrait(true)
//                .setOnlyFullScreen(true)
                .setKeepScreenOn(true)
                .setNetWorkTypeTie(true)
                .setNetWorkListener(new PlayerController.OnNetWorkListener() {
                    @Override
                    public void onChanged() {
                        if(videoView.getCurrentState() == IjkVideoView.STATE_IDLE) {
                            appVideoReplay.setVisibility(View.VISIBLE);
                            appVideoRetry.setVisibility(View.GONE);
                            playerBottom.getIvPlayPause().setEnabled(false);
                            playerBottom.updatePlayState(false);
                        }else if(videoView.getCurrentState() == IjkVideoView.STATE_PAUSED) {
                            updatePlayState(false);
                        }else {
                            updatePlayState(false);
                        }
                    }
                })
                .setAutoControlListener(playerTitleBar, playerBottom)
                .setOnConfigurationChangedListener(new PlayerController.OnConfigurationChangedListener() {
                    @Override
                    public void onChanged(int requestedOrientation) {
                        if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            Utils.showSystemUI(getContext());
                            Utils.showStatusBar(getContext());
                        }else if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                            Utils.hideSystemUI(getContext());
                            Utils.hideStatusBar(getContext());
                        }
                    }
                })
                .setPanelControl(new PlayerController.PanelControlListener() {
                    @Override
                    public void operatorPanel(boolean isShowControlPanel) {
                        if (isShowControlPanel) {
                            showOrHideBars(true, true);
                        } else {
                            showOrHideBars(false, true);
                        }
                    }
                })
                .setPlayStateListener(new PlayerController.PlayStateListener() {
                    @Override
                    public void playState(int state) {
                        switch (state) {
                            case IjkVideoView.STATE_PLAYING:
                                updatePlayState(false);
                                break;
                            case IjkVideoView.STATE_PAUSED:
                                updatePlayState(true);
                                break;
                        }
                    }
                })
                .setSyncProgressListener(new PlayerController.SyncProgressListener() {
                    @Override
                    public void syncTime(long position, long duration) {
                        playerBottom.getTvCurrentTime().setText(mPlayerController.generateTime(position));
                        playerBottom.getTvTotalTime().setText(mPlayerController.generateTime(duration));
                    }

                    @Override
                    public void syncProgress(int progress, int secondaryProgress) {
                        bottomProgress.setProgress(progress);
                        bottomProgress.setSecondaryProgress(secondaryProgress);
                    }
                })
                .setGestureListener(new PlayerController.GestureListener() {
                    @Override
                    public void onProgressSlide(long newPosition, long duration, int showDelta) {
                        if (showDelta != 0) {
                            playerVolumeBright.getLlVideoFastForward().setVisibility(View.VISIBLE);
                            playerVolumeBright.getLlVideoBrightness().setVisibility(View.GONE);
                            playerVolumeBright.getLlVideoVolume().setVisibility(View.GONE);
                            playerVolumeBright.getTvVideoFastForwardTarget().setVisibility(View.VISIBLE);
                            playerVolumeBright.getTvVideoFastForwardAll().setVisibility(View.VISIBLE);
                            playerVolumeBright.getTvVideoFastForwardTarget().setText(mPlayerController.generateTime(newPosition) + "/");
                            playerVolumeBright.getTvVideoFastForwardAll().setText(mPlayerController.generateTime(duration));

                            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
                            playerVolumeBright.getTvVideoFastForward().setVisibility(View.VISIBLE);
                            playerVolumeBright.getTvVideoFastForward().setText(String.format("%ss", text));
                        }
                    }

                    @Override
                    public void onVolumeSlide(int volume) {
                        playerVolumeBright.getLlVideoFastForward().setVisibility(View.GONE);
                        playerVolumeBright.getLlVideoBrightness().setVisibility(View.GONE);
                        playerVolumeBright.getLlVideoVolume().setVisibility(View.VISIBLE);
                        playerVolumeBright.getTvVideoVolume().setVisibility(View.VISIBLE);
                        playerVolumeBright.getTvVideoVolume().setText(volume + "%");
                    }

                    @Override
                    public void onBrightnessSlide(float brightness) {
                        playerVolumeBright.getLlVideoFastForward().setVisibility(View.GONE);
                        playerVolumeBright.getLlVideoBrightness().setVisibility(View.VISIBLE);
                        playerVolumeBright.getLlVideoVolume().setVisibility(View.GONE);
                        playerVolumeBright.getTvVideoBrightness().setVisibility(View.VISIBLE);
                        playerVolumeBright.getTvVideoBrightness().setText((int) (brightness * 100) + "%");
                    }

                    @Override
                    public void endGesture() {
                        playerVolumeBright.getLlVideoFastForward().setVisibility(View.GONE);
                        playerVolumeBright.getLlVideoBrightness().setVisibility(View.GONE);
                        playerVolumeBright.getLlVideoVolume().setVisibility(View.GONE);
                    }
                });

        onDestroyVideo();
        if (mVideoUri != null) {
            videoView.setVideoURI(mVideoUri);
            if(!Utils.isWifiConnected(mActivity) && !mPlayerController.isLocalDataSource(mVideoUri) && !PlayerController.WIFI_TIP_DIALOG_SHOWED) {
                mPlayerController.showWifiDialog();
            }else {
                updatePlayState(false);
            }
        }
    }

    /**
     * 初始化标题栏/控制栏显隐动画效果
     */
    private void initAnimation() {
        mEnterFromTop = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_top);
        mEnterFromBottom = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_bottom);
        mExitFromTop = AnimationUtils.loadAnimation(mContext, R.anim.exit_from_top);
        mExitFromBottom = AnimationUtils.loadAnimation(mContext, R.anim.exit_from_bottom);

        mEnterFromTop.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                playerTitleBar.setVisibility(VISIBLE);
            }
        });
        mEnterFromBottom.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                playerBottom.setVisibility(VISIBLE);
            }
        });
        mExitFromTop.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                playerTitleBar.setVisibility(GONE);
            }
        });
        mExitFromBottom.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                playerBottom.setVisibility(GONE);
            }
        });
    }

    /**
     * 显隐标题栏和控制条
     *
     * @param show          是否显示
     * @param animateEffect 是否需要动画效果
     */
    public void showOrHideBars(boolean show, boolean animateEffect) {
        if (animateEffect) {
            animateShowOrHideBars(show);
        } else {
            forceShowOrHideBars(show);
        }
    }

    /**
     * 直接显隐标题栏和控制栏
     */
    public void forceShowOrHideBars(boolean show) {
        playerTitleBar.clearAnimation();
        playerBottom.clearAnimation();

        if (show) {
            playerBottom.setVisibility(VISIBLE);
            playerTitleBar.setVisibility(VISIBLE);
            bottomProgress.setVisibility(View.GONE);
        } else {
            playerBottom.setVisibility(GONE);
            playerTitleBar.setVisibility(GONE);
            bottomProgress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 带动画效果的显隐标题栏和控制栏
     */
    private void animateShowOrHideBars(boolean show) {
        playerTitleBar.clearAnimation();
        playerBottom.clearAnimation();

        if (show) {
            if (playerTitleBar.getVisibility() != VISIBLE) {
                playerTitleBar.startAnimation(mEnterFromTop);
                playerBottom.startAnimation(mEnterFromBottom);
                bottomProgress.setVisibility(View.GONE);
            }
        } else {
            if (playerTitleBar.getVisibility() != GONE) {
                playerTitleBar.startAnimation(mExitFromTop);
                playerBottom.startAnimation(mExitFromBottom);
                bottomProgress.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * @return 缓冲百分比 0-100
     */
    public int getBufferProgress() {
        if (videoView != null && videoView.getDuration() > 0) {
            return videoView.getBufferPercentage();
        } else {
            return 0;
        }
    }

    public IjkVideoView getVideoView() {
        return videoView;
    }

    public PlayerTitleBar getPlayerTitleBar() {
        return playerTitleBar;
    }

    public PlayerVolumeBright getPlayerVolumeBright() {
        return playerVolumeBright;
    }

    public PlayerBottom getPlayerBottom() {
        return playerBottom;
    }

    public void showVideoLoading() {
        if (llLoading != null) {
            llLoading.setVisibility(View.VISIBLE);
            tvSpeed.setVisibility(View.VISIBLE);
            tvSpeed.setText("");
        }
    }

    public void hideVideoLoading() {
        if (llLoading != null) {
            llLoading.setVisibility(View.GONE);
            tvSpeed.setVisibility(View.GONE);
            tvSpeed.setText("");
        }
    }

    public boolean isPlaying() {
        if (videoView != null) {
            return videoView.isPlaying();
        }
        return false;
    }

    /**
     * 更新放按钮状态
     *
     * @param isPlay true：播放中；false：暂停或者未播放
     */
    public void updatePlayState(boolean isPlay) {
        if (isPlay) {
            // 播放==>暂停
            playerBottom.updatePlayState(false);
            videoView.pause();
        } else {
            // 暂停==>播放
            playerBottom.updatePlayState(true);
            videoView.start();
        }
    }

    /**
     * 更新SeekBar的进度
     *
     * @param position
     */
    public void updateProgress(int position) {
        mPlayerController.updateProgress(position, videoView.getDuration());
    }

    /**
     * 发送message给handler,自动隐藏标题栏
     */
    public void sendAutoHideBarsMsg() {
        if (mPlayerController != null) {
            mPlayerController.sendAutoHideBarsMsg(5000);
        }
    }
    //////////////////////////////////////////开放方法///////////////////////////////////////////////
    public void start() {
        if (videoView != null) {
            updatePlayState(false);
        }
    }

    public void pause() {
        if (videoView.isPlaying()) {
            updatePlayState(true);
        }
    }

    public void stopPlayback() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    public void onDestroyVideo() {
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

    public void onConfigurationChanged() {
        if (mPlayerController != null) {
            mPlayerController.onConfigurationChanged();
        }
    }

    //////////////////////////////////////////set方法///////////////////////////////////////////////

    /**
     * 设置视频标题
     */
    public void setTitle(String title) {
        playerTitleBar.setTitle(title);
    }

    /**
     * 设置封面
     *
     * @param coverUrl
     */
    public void setVideoCoverUrl(String coverUrl) {
        mVideoCoverUrl = coverUrl;
    }

    /**
     * 设置进度条样式
     *
     * @param resId 进度条progressDrawable分层资源
     *              数组表示的进度资源分别为 background - secondaryProgress - progress
     *              若对应的数组元素值小于等于0,表示该层素材保持不变;
     *              注意:progress和secondaryProgress的shape资源需要做成clip的,否则会直接完全显示
     */
    public void setProgressLayerDrawables(@DrawableRes int resId) {
        playerBottom.setProgressLayerDrawables(resId);
    }

    /**
     * 设置进度条按钮图片
     *
     * @param thumbId
     */
    public void setProgressThumbDrawable(@DrawableRes int thumbId) {
        playerBottom.setProgressThumbDrawable(thumbId);
    }

    /**
     * 设置暂停按钮图标
     *
     * @param iconPause
     */
    public void setIconPause(@DrawableRes int iconPause) {
        playerBottom.setIconPause(iconPause);
    }

    /**
     * 设置播放按钮图标
     *
     * @param iconPlay
     */
    public void setIconPlay(@DrawableRes int iconPlay) {
        playerBottom.setIconPlay(iconPlay);
    }

    /**
     * 设置退出全屏按钮
     *
     * @param iconShrink
     */
    public void setIconShrink(@DrawableRes int iconShrink) {
        playerBottom.setIconShrink(iconShrink);
    }

    /**
     * 设置退出全屏按钮
     *
     * @param iconExpand
     */
    public void setIconExpand(@DrawableRes int iconExpand) {
        playerBottom.setIconExpand(iconExpand);
    }

    /**
     * 设置加载提示框图标资源
     */
    public void setIconLoading(@DrawableRes int iconLoading) {
        if (Build.VERSION.SDK_INT >= 21) {
            progressBar.setIndeterminateDrawable(getResources().getDrawable(iconLoading, null));
        } else {
            progressBar.setIndeterminateDrawable(getResources().getDrawable(iconLoading));
        }

    }
}
