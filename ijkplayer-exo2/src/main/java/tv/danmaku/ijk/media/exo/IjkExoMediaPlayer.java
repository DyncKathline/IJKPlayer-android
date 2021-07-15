/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.exo;

import android.app.Activity;
import android.content.Context;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.video.VideoSize;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;

public class IjkExoMediaPlayer extends AbstractMediaPlayer implements Player.Listener {
    private static final String TAG = "IjkExoMediaPlayer";

    private Context mAppContext;
    private SimpleExoPlayer mInternalPlayer;
    private MyEventLogger mEventLogger;
    private MediaSource mMediaSource;
    private DefaultTrackSelector mTrackSelector;
    private String mDataSource;
    private int mVideoWidth;
    private int mVideoHeight;
    private Surface mSurface;
    private Map<String, String> mHeaders = new HashMap<>();
    private int lastReportedPlaybackState;
    private boolean lastReportedPlayWhenReady;
    private boolean isPrepareing = true;
    private boolean isBuffering = false;
    private boolean isLooping = false;
    /**
     * 是否带上header
     */
    private boolean isPreview = false;
    /**
     * 是否开启缓存
     */
    private boolean isCache = false;
    /**
     * dataSource等的帮组类
     */
    private ExoSourceManager mExoSourceManager;
    /**
     * 缓存目录，可以为空
     */
    private File mCacheDir;

    private int audioSessionId = C.AUDIO_SESSION_ID_UNSET;

    public Handler handler;
    private Runnable callback;

    public IjkExoMediaPlayer(Context context) {
        mAppContext = context.getApplicationContext();
        lastReportedPlaybackState = Player.STATE_IDLE;
        mExoSourceManager = ExoSourceManager.newInstance(context, mHeaders);
        handler = new Handler();
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        if (sh == null)
            setSurface(null);
        else
            setSurface(sh.getSurface());
    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mInternalPlayer != null)
            mInternalPlayer.setVideoSurface(surface);
    }

    @Override
    public void setDataSource(Context context, Uri uri) {
        mDataSource = uri.toString();
        mMediaSource = mExoSourceManager.getMediaSource(mDataSource, isPreview, isCache, isLooping, mCacheDir, "");
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) {
        // TODO: handle headers
        setDataSource(context, uri);
    }

    @Override
    public void setDataSource(String path) {
        setDataSource(mAppContext, Uri.parse(path));
    }

    @Override
    public void setDataSource(FileDescriptor fd) {
        // TODO: no support
        throw new UnsupportedOperationException("no support");
    }

    @Override
    public String getDataSource() {
        return mDataSource;
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (mInternalPlayer != null)
            throw new IllegalStateException("can't prepare a prepared player");

        DataSource.Factory dataSourceFactory = DemoUtil.getDataSourceFactory(mAppContext);
        DefaultTrackSelector.ParametersBuilder builder =
                new DefaultTrackSelector.ParametersBuilder(mAppContext);
        DefaultTrackSelector.Parameters trackSelectorParameters = builder.build();
        boolean preferExtensionDecoders = true;
        RenderersFactory renderersFactory = DemoUtil.buildRenderersFactory(mAppContext, preferExtensionDecoders);
        MediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(dataSourceFactory);

        mTrackSelector = new DefaultTrackSelector(mAppContext);
        mTrackSelector.setParameters(trackSelectorParameters);

        mEventLogger = new MyEventLogger(mTrackSelector);

        DefaultLoadControl loadControl = new DefaultLoadControl();
        mInternalPlayer = new SimpleExoPlayer.Builder(mAppContext, renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLooper(Looper.myLooper())
                .setTrackSelector(mTrackSelector)
                .setLoadControl(loadControl).build();
        mInternalPlayer.addListener(this);
        mInternalPlayer.addAnalyticsListener(mEventLogger);
        mInternalPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true);

        if (mSurface != null)
            mInternalPlayer.setVideoSurface(mSurface);

        mInternalPlayer.setMediaSource(mMediaSource);
        mInternalPlayer.prepare();
        mInternalPlayer.setPlayWhenReady(false);

        if(callback == null) {
            callback = new onBufferingUpdate();
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.setPlayWhenReady(true);
    }

    @Override
    public void stop() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.release();
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.setPlayWhenReady(false);
    }

    @Override
    public void setWakeMode(Context context, int mode) {
        // FIXME: implement
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        // TODO: do nothing
    }

    @Override
    public IjkTrackInfo[] getTrackInfo() {
        // TODO: implement
        return null;
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public boolean isPlaying() {
        if (mInternalPlayer == null)
            return false;
        int state = mInternalPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mInternalPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
            default:
                return false;
        }
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.seekTo(msec);
    }

    @Override
    public long getCurrentPosition() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getDuration();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public void reset() {
        if (mInternalPlayer != null) {
            mInternalPlayer.release();
            mInternalPlayer.removeListener(this);
            mInternalPlayer.removeAnalyticsListener(mEventLogger);
            mInternalPlayer = null;
        }

        mSurface = null;
        mDataSource = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    @Override
    public boolean isLooping() {
        return isLooping;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mInternalPlayer.setVolume((leftVolume + rightVolume) / 2);
    }

    @Override
    public int getAudioSessionId() {
        return audioSessionId;
    }

    @Override
    public MediaInfo getMediaInfo() {
        // TODO: no support
        return null;
    }

    @Override
    public void setLogEnabled(boolean enable) {
        // do nothing
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        // do nothing
    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {
        // do nothing
    }

    @Override
    public void release() {
        if (mInternalPlayer != null) {
            reset();
            mEventLogger = null;
        }
    }

    public void stopPlayback() {
        mInternalPlayer.stop();
    }

    /**
     * 是否需要带上header
     * setDataSource之前生效
     *
     * @param preview
     */
    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    public boolean isPreview() {
        return isPreview;
    }

    public boolean isCache() {
        return isCache;
    }

    /**
     * 是否开启cache
     * setDataSource之前生效
     *
     * @param cache
     */
    public void setCache(boolean cache) {
        isCache = cache;
    }

    public File getCacheDir() {
        return mCacheDir;
    }

    /**
     * cache文件的目录
     * setDataSource之前生效
     *
     * @param cacheDir
     */
    public void setCacheDir(File cacheDir) {
        this.mCacheDir = cacheDir;
    }

    public MediaSource getMediaSource() {
        return mMediaSource;
    }

    public void setMediaSource(MediaSource mediaSource) {
        this.mMediaSource = mediaSource;
    }

    public ExoSourceManager getExoSourceManager() {
        return mExoSourceManager;
    }

    /**
     * 倍速播放
     *
     * @param speed 倍速播放，默认为1
     * @param pitch 音量缩放，默认为1，修改会导致声音变调
     */
    public void setSpeed(@Size(min = 0) float speed, @Size(min = 0) float pitch) {
        PlaybackParameters playbackParameters = new PlaybackParameters(speed, pitch);
        mInternalPlayer.setPlaybackParameters(playbackParameters);
    }

    public float getSpeed() {
        return mInternalPlayer.getPlaybackParameters().speed;
    }

    public int getBufferedPercentage() {
        if (mInternalPlayer == null)
            return 0;

        return mInternalPlayer.getBufferedPercentage();
    }

    /**
     * 获取视频包含视频轨道、音频轨道、文字轨道
     * @return
     */
    public ArrayList<Integer> getTrackGroup() {
        ArrayList<Integer> trackInfos = new ArrayList<Integer>();
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = mTrackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return trackInfos;
        }
        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                int trackType;
                switch (mInternalPlayer.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        trackType = C.TRACK_TYPE_AUDIO;
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        trackType = C.TRACK_TYPE_VIDEO;
                        break;
                    case C.TRACK_TYPE_TEXT:
                        trackType = C.TRACK_TYPE_TEXT;
                        break;
                    default:
                        continue;
                }
                trackInfos.add(trackType);
            }
        }
        return trackInfos;
    }

    /***
     * 获取地当前网速
     *
     * @param activity 活动对象
     * @return long total rx bytes
     */
    public long getTotalRxBytes(@NonNull Activity activity) {
        return TrafficStats.getUidRxBytes(activity.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    private class MyEventLogger extends EventLogger {

        public MyEventLogger(@Nullable MappingTrackSelector trackSelector) {
            super(trackSelector);
        }

        public MyEventLogger(@Nullable MappingTrackSelector trackSelector, String tag) {
            super(trackSelector, tag);
        }

        @Override
        public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {
            super.onBandwidthEstimate(eventTime, totalLoadTimeMs, totalBytesLoaded, bitrateEstimate);
            Log.i("kath----", totalLoadTimeMs + ", " + totalBytesLoaded + ", " + bitrateEstimate);
        }
    }

    /////////////////////////////////////EventListener/////////////////////////////////////////////

    @Override
    public void onTimelineChanged(Timeline timeline, int reason) {

    }

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onStaticMetadataChanged(List<Metadata> metadataList) {

    }

    @Override
    public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {

    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onAvailableCommandsChanged(Player.Commands availableCommands) {

    }

    @Override
    public void onPlaybackStateChanged(int state) {
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        //重新播放状态顺序为：STATE_IDLE -》STATE_BUFFERING -》STATE_READY
        //缓冲时顺序为：STATE_BUFFERING -》STATE_READY
        Log.e(TAG, "onPlayerStateChanged: playWhenReady = " + playWhenReady + ", playbackState = " + playbackState);
        if (lastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
            if (isBuffering) {
                switch (playbackState) {
                    case Player.STATE_ENDED:
                    case Player.STATE_READY:
                        notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_END, mInternalPlayer.getBufferedPercentage());
                        isBuffering = false;
                        break;
                }
            }

            if (isPrepareing) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        notifyOnPrepared();
                        isPrepareing = false;
                        break;
                }
            }

            switch (playbackState) {
//                case Player.STATE_IDLE:
//                    notifyOnCompletion();
//                    break;
                case Player.STATE_BUFFERING:
                    notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_START, mInternalPlayer.getBufferedPercentage());
                    isBuffering = true;
                    if(callback == null) {
                        callback = new onBufferingUpdate();
                    }
                    handler.post(callback);
                    break;
                case Player.STATE_READY:
                    break;
                case Player.STATE_ENDED:
                    notifyOnCompletion();
                    break;
                default:
                    break;
            }
        }
        lastReportedPlayWhenReady = playWhenReady;
        lastReportedPlaybackState = playbackState;
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        notifyOnError(IMediaPlayer.MEDIA_ERROR_UNKNOWN, IMediaPlayer.MEDIA_ERROR_UNKNOWN);
    }

    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    /////////////////////////////////////VideoListener/////////////////////////////////////////////

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        mVideoWidth = videoSize.width;
        mVideoHeight = videoSize.height;
        notifyOnVideoSizeChanged(videoSize.width, videoSize.height, 1, 1);
        if (videoSize.unappliedRotationDegrees > 0)
            notifyOnInfo(IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED, videoSize.unappliedRotationDegrees);
    }

    @Override
    public void onSurfaceSizeChanged(int width, int height) {

    }

    @Override
    public void onRenderedFirstFrame() {

    }

    /////////////////////////////////////AudioListener/////////////////////////////////////////////

    @Override
    public void onAudioSessionIdChanged(int audioSessionId) {
        this.audioSessionId = audioSessionId;
    }

    @Override
    public void onAudioAttributesChanged(AudioAttributes audioAttributes) {

    }

    @Override
    public void onVolumeChanged(float volume) {

    }

    @Override
    public void onSkipSilenceEnabledChanged(boolean skipSilenceEnabled) {

    }

    private class onBufferingUpdate implements Runnable {
        @Override
        public void run() {
            if (mInternalPlayer != null) {
                final int percent = mInternalPlayer.getBufferedPercentage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyOnBufferingUpdate(percent);
                    }
                });
                if (percent < 100) {
                    handler.postDelayed(callback, 300);
                } else {
                    handler.removeCallbacks(callback);
                }
            }
        }
    }

    /////////////////////////////////////TextOutput/////////////////////////////////////////////

    @Override
    public void onCues(List<Cue> cues) {

    }

    /////////////////////////////////////MetadataOutput/////////////////////////////////////////////


    @Override
    public void onMetadata(Metadata metadata) {

    }
}
