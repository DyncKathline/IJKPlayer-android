/*
 * Copyright (C) 2014 The Android Open Source Project
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
package tv.danmaku.ijk.media.exo.demo.player;

import android.content.Context;
import android.view.Surface;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.util.concurrent.CopyOnWriteArrayList;

import tv.danmaku.ijk.media.exo.demo.EventLogger;

/**
 * A wrapper around {link ExoPlayer} that provides a higher level interface. It can be prepared
 * with one of a number of {link RendererBuilder} classes to suit different use cases (e.g. DASH,
 * SmoothStreaming and so on).
 */
public class DemoPlayer implements Player.EventListener, DefaultBandwidthMeter.EventListener{

    /**
     * A listener for core events.
     */
    public interface Listener {
        void onStateChanged(boolean playWhenReady, int playbackState);
        void onError(Exception e);
        void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                float pixelWidthHeightRatio);
    }

    // Constants pulled into this class for convenience.
    public static final int STATE_IDLE = Player.STATE_IDLE;
    public static final int STATE_BUFFERING = Player.STATE_BUFFERING;
    public static final int STATE_READY = Player.STATE_READY;
    public static final int STATE_ENDED = Player.STATE_ENDED;

    public static final int RENDERER_COUNT = 4;
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_TEXT = 2;
    public static final int TYPE_METADATA = 3;

    private static final int RENDERER_BUILDING_STATE_IDLE = 1;
    private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
    private static final int RENDERER_BUILDING_STATE_BUILT = 3;

    private final MediaSource rendererBuilder;
    private final ExoPlayer player;
    private final CopyOnWriteArrayList<DemoPlayer.Listener> listeners;

    private int rendererBuildingState;
    private int lastReportedPlaybackState;
    private boolean lastReportedPlayWhenReady;

    private Surface surface;
    private DefaultTrackSelector trackSelector;
    private Format videoFormat;
    private int videoTrackToRestore;

    private BandwidthMeter bandwidthMeter;
    private boolean backgrounded;
    protected final EventLogger eventLogger;
    private final DefaultRenderersFactory renderersFactory;

    public DemoPlayer(Context context, MediaSource rendererBuilder) {
        this.rendererBuilder = rendererBuilder;
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        eventLogger = new EventLogger(trackSelector);
        renderersFactory = new DefaultRenderersFactory(context);
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
        player.addListener(this);
        player.addListener(eventLogger);
        listeners = new CopyOnWriteArrayList<>();
        lastReportedPlaybackState = STATE_IDLE;
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void setVideoSurface(Surface surface) {
        this.surface = surface;
        pushSurface(false);
    }

    public Surface getSurface() {
        return surface;
    }

    public void blockingClearSurface() {
        surface = null;
        pushSurface(true);
    }

//    public int getTrackCount(int type) {
//        return player.getTrackCount(type);
//    }
//
//    public MediaFormat getTrackFormat(int type, int index) {
//        return player.getTrackFormat(type, index);
//    }
//
//    public int getSelectedTrack(int type) {
//        return player.getSelectedTrack(type);
//    }
//
//    public void setSelectedTrack(int type, boolean isDisabled) {
//        trackSelector.setRendererDisabled(type, isDisabled);
//        if (type == TYPE_TEXT && captionListener != null) {
//            captionListener.onCues(Collections.<Cue>emptyList());
//        }
//    }
//
//    public boolean getBackgrounded() {
//        return backgrounded;
//    }
//
//    public void setBackgrounded(boolean backgrounded) {
//        if (this.backgrounded == backgrounded) {
//            return;
//        }
//        this.backgrounded = backgrounded;
//        if (backgrounded) {
//            videoTrackToRestore = getSelectedTrack(TYPE_VIDEO);
//            boolean isDisabled = trackSelector.getRendererDisabled(videoTrackToRestore);
//            setSelectedTrack(TYPE_VIDEO, isDisabled);
//            blockingClearSurface();
//        } else {
//            setSelectedTrack(TYPE_VIDEO, videoTrackToRestore);
//        }
//    }

    public void prepare() {
        if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILT) {
            player.stop();
        }
        videoFormat = null;
        trackSelector = null;
        rendererBuildingState = RENDERER_BUILDING_STATE_BUILDING;
        maybeReportPlayerState();
    }

    /**
     * Invoked if a {link RendererBuilder} encounters an error.
     *
     * @param e Describes the error.
     */
  /* package */ void onRenderersError(Exception e) {
        for (Listener listener : listeners) {
            listener.onError(e);
        }
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
        maybeReportPlayerState();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);
    }

    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    public void release() {
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
        surface = null;
        player.release();
    }

    public int getPlaybackState() {
        return player.getPlaybackState();
    }

    public Format getFormat() {
        return videoFormat;
    }

    public BandwidthMeter getBandwidthMeter() {
        return bandwidthMeter;
    }

    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public long getDuration() {
        return player.getDuration();
    }

    public int getBufferedPercentage() {
        return player.getBufferedPercentage();
    }

    public boolean getPlayWhenReady() {
        return player.getPlayWhenReady();
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int state) {
        maybeReportPlayerState();
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException exception) {
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
        for (Listener listener : listeners) {
            listener.onError(exception);
        }
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {

    }

    private void maybeReportPlayerState() {
        boolean playWhenReady = player.getPlayWhenReady();
        int playbackState = getPlaybackState();
        if (lastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
            for (Listener listener : listeners) {
                listener.onStateChanged(playWhenReady, playbackState);
            }
            lastReportedPlayWhenReady = playWhenReady;
            lastReportedPlaybackState = playbackState;
        }
    }

    private void pushSurface(boolean blockForSurfacePush) {
        if (trackSelector == null) {
            return;
        }
//        if (blockForSurfacePush) {

//            Looper eventLooper = Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper();
//            Handler eventHandler = new Handler(eventLooper);
//            Renderer[] renderers = renderersFactory.createRenderers(eventHandler, componentListener, componentListener,
//                    componentListener, componentListener);
//            for (Renderer renderer : renderers) {
//
//            }
//            new ExoPlayer.ExoPlayerMessage()
//            player.blockingSendMessages(new ExoPlayer.ExoPlayerMessage(trackSelector, C.MSG_SET_SURFACE, surface));
//        } else {
//            player.sendMessages(new ExoPlayer.ExoPlayerMessage(trackSelector, C.MSG_SET_SURFACE, surface));
//        }
    }

}
