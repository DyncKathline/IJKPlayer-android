//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.dync.exo;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

import org.dync.exo.common.IMediaPlayer;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

public class MediaPlayerProxy implements IMediaPlayer {
    protected final IMediaPlayer mBackEndMediaPlayer;

    public MediaPlayerProxy(IMediaPlayer backEndMediaPlayer) {
        this.mBackEndMediaPlayer = backEndMediaPlayer;
    }

    public IMediaPlayer getInternalMediaPlayer() {
        return this.mBackEndMediaPlayer;
    }

    public void setDisplay(SurfaceHolder sh) {
        this.mBackEndMediaPlayer.setDisplay(sh);
    }

    @TargetApi(14)
    public void setSurface(Surface surface) {
        this.mBackEndMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDataSource(Uri uri) {
        throw new UnsupportedOperationException();
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mBackEndMediaPlayer.setDataSource(context, uri);
    }

    @TargetApi(14)
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mBackEndMediaPlayer.setDataSource(context, uri, headers);
    }

    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mBackEndMediaPlayer.setDataSource(fd);
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mBackEndMediaPlayer.setDataSource(path);
    }

    public void setDataSource(MediaDataSource mediaDataSource) {
        this.mBackEndMediaPlayer.setDataSource(mediaDataSource);
    }

    public String getDataSource() {
        return this.mBackEndMediaPlayer.getDataSource();
    }

    public void prepareAsync() throws IllegalStateException {
        this.mBackEndMediaPlayer.prepareAsync();
    }

    public void start() throws IllegalStateException {
        this.mBackEndMediaPlayer.start();
    }

    public void stop() throws IllegalStateException {
        this.mBackEndMediaPlayer.stop();
    }

    public void pause() throws IllegalStateException {
        this.mBackEndMediaPlayer.pause();
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        this.mBackEndMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    public int getVideoWidth() {
        return this.mBackEndMediaPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return this.mBackEndMediaPlayer.getVideoHeight();
    }

    public boolean isPlaying() {
        return this.mBackEndMediaPlayer.isPlaying();
    }

    public void seekTo(long msec) throws IllegalStateException {
        this.mBackEndMediaPlayer.seekTo(msec);
    }

    public long getCurrentPosition() {
        return this.mBackEndMediaPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return this.mBackEndMediaPlayer.getDuration();
    }

    public void release() {
        this.mBackEndMediaPlayer.release();
    }

    public void reset() {
        this.mBackEndMediaPlayer.reset();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        this.mBackEndMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public int getAudioSessionId() {
        return this.mBackEndMediaPlayer.getAudioSessionId();
    }

    public void setLogEnabled(boolean enable) {
    }

    public boolean isPlayable() {
        return false;
    }

    public void setOnPreparedListener(final OnPreparedListener listener) {
        if (listener != null) {
            this.mBackEndMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(IMediaPlayer mp) {
                    listener.onPrepared(MediaPlayerProxy.this);
                }
            });
        } else {
            this.mBackEndMediaPlayer.setOnPreparedListener((OnPreparedListener)null);
        }

    }

    public void setOnCompletionListener(final OnCompletionListener listener) {
        if (listener != null) {
            this.mBackEndMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    listener.onCompletion(MediaPlayerProxy.this);
                }
            });
        } else {
            this.mBackEndMediaPlayer.setOnCompletionListener((OnCompletionListener)null);
        }

    }

    public void setOnBufferingUpdateListener(final OnBufferingUpdateListener listener) {
        if (listener != null) {
            this.mBackEndMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    listener.onBufferingUpdate(MediaPlayerProxy.this, percent);
                }
            });
        } else {
            this.mBackEndMediaPlayer.setOnBufferingUpdateListener((OnBufferingUpdateListener)null);
        }

    }

    public void setOnSeekCompleteListener(final OnSeekCompleteListener listener) {
        if (listener != null) {
            this.mBackEndMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                public void onSeekComplete(IMediaPlayer mp) {
                    listener.onSeekComplete(MediaPlayerProxy.this);
                }
            });
        } else {
            this.mBackEndMediaPlayer.setOnSeekCompleteListener((OnSeekCompleteListener)null);
        }

    }

    public void setOnVideoSizeChangedListener(final OnVideoSizeChangedListener listener) {
        if (listener != null) {
            this.mBackEndMediaPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                    listener.onVideoSizeChanged(MediaPlayerProxy.this, width, height, sar_num, sar_den);
                }
            });
        } else {
            this.mBackEndMediaPlayer.setOnVideoSizeChangedListener((OnVideoSizeChangedListener)null);
        }

    }

    public void setOnErrorListener(final OnErrorListener listener) {
        if (listener != null) {
            this.mBackEndMediaPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    return listener.onError(MediaPlayerProxy.this, what, extra);
                }
            });
        } else {
            this.mBackEndMediaPlayer.setOnErrorListener((OnErrorListener)null);
        }

    }

    public void setOnInfoListener(final OnInfoListener listener) {
        if (listener != null) {
            this.mBackEndMediaPlayer.setOnInfoListener(new OnInfoListener() {
                public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                    return listener.onInfo(MediaPlayerProxy.this, what, extra);
                }
            });
        } else {
            this.mBackEndMediaPlayer.setOnInfoListener((OnInfoListener)null);
        }

    }

    public void setOnTimedTextListener(final OnTimedTextListener listener) {
        if (listener != null) {
            this.mBackEndMediaPlayer.setOnTimedTextListener(new OnTimedTextListener() {
                public void onTimedText(IMediaPlayer mp, TimedText text) {
                    listener.onTimedText(MediaPlayerProxy.this, text);
                }
            });
        } else {
            this.mBackEndMediaPlayer.setOnTimedTextListener((OnTimedTextListener)null);
        }

    }

    public void setAudioStreamType(int streamtype) {
        this.mBackEndMediaPlayer.setAudioStreamType(streamtype);
    }

    public void setKeepInBackground(boolean keepInBackground) {
        this.mBackEndMediaPlayer.setKeepInBackground(keepInBackground);
    }

    public int getVideoSarNum() {
        return this.mBackEndMediaPlayer.getVideoSarNum();
    }

    public int getVideoSarDen() {
        return this.mBackEndMediaPlayer.getVideoSarDen();
    }

    public void setWakeMode(Context context, int mode) {
        this.mBackEndMediaPlayer.setWakeMode(context, mode);
    }

    public MediaPlayer.TrackInfo[] getTrackInfo() {
        return this.mBackEndMediaPlayer.getTrackInfo();
    }

    public void setLooping(boolean looping) {
        this.mBackEndMediaPlayer.setLooping(looping);
    }

    public boolean isLooping() {
        return this.mBackEndMediaPlayer.isLooping();
    }
}
