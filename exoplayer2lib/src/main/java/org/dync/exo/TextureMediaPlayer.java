//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.dync.exo;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;

import org.dync.exo.common.IMediaPlayer;
import org.dync.exo.common.ISurfaceTextureHolder;
import org.dync.exo.common.ISurfaceTextureHost;

@TargetApi(14)
public class TextureMediaPlayer extends MediaPlayerProxy implements IMediaPlayer, ISurfaceTextureHolder {
    private SurfaceTexture mSurfaceTexture;
    private ISurfaceTextureHost mSurfaceTextureHost;

    public TextureMediaPlayer(IMediaPlayer backEndMediaPlayer) {
        super(backEndMediaPlayer);
    }

    public void releaseSurfaceTexture() {
        if (this.mSurfaceTexture != null) {
            if (this.mSurfaceTextureHost != null) {
                this.mSurfaceTextureHost.releaseSurfaceTexture(this.mSurfaceTexture);
            } else {
                this.mSurfaceTexture.release();
            }

            this.mSurfaceTexture = null;
        }

    }

    public void reset() {
        super.reset();
        this.releaseSurfaceTexture();
    }

    public void release() {
        super.release();
        this.releaseSurfaceTexture();
    }

    public void setDisplay(SurfaceHolder sh) {
        if (this.mSurfaceTexture == null) {
            super.setDisplay(sh);
        }

    }

    public void setSurface(Surface surface) {
        if (this.mSurfaceTexture == null) {
            super.setSurface(surface);
        }

    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        if (this.mSurfaceTexture != surfaceTexture) {
            this.releaseSurfaceTexture();
            this.mSurfaceTexture = surfaceTexture;
            if (surfaceTexture == null) {
                super.setSurface((Surface)null);
            } else {
                super.setSurface(new Surface(surfaceTexture));
            }

        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mSurfaceTexture;
    }

    public void setSurfaceTextureHost(ISurfaceTextureHost surfaceTextureHost) {
        this.mSurfaceTextureHost = surfaceTextureHost;
    }
}
