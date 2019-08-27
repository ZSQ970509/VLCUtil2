package org.videolan.libvlc;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedro on 25/06/17.
 * Play and stop need be in other thread or app can freeze
 */
public class VlcVideoLibrary implements MediaPlayer.EventListener {

    private int width = 0, height = 0;
    private LibVLC mLibvlc;
    private MediaPlayer mMediaPlayer;
    private VlcListener mVlcListener;
    //The library will select one of this class for rendering depend of constructor called
    private SurfaceView surfaceView;
    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private SurfaceHolder surfaceHolder;
    private List<String> options = new ArrayList<>();
    private Context mContext;

    public VlcVideoLibrary(Context context, SurfaceView surfaceView) {
        this.surfaceView = surfaceView;

        mContext = context;
        mLibvlc = new LibVLC(context, new VlcOptions().getDefaultOptions());
        options.add(":fullscreen");
//        options.add("--aout=opensles");
//        options.add("--audio-time-stretch"); // time stretching
//        options.add("--extraintf=");
//        options.add("--rtsp-tcp");
    }

    /**
     * This method should be called after constructor and before play methods.
     *
     * @param options seeted to VLC mMediaPlayer.
     */
    public void setOptions(List<String> options) {
        this.options = options;
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void play(String endPoint) {
        if (TextUtils.isEmpty(endPoint)) {
            Toast.makeText(mContext, "播放地址为空", Toast.LENGTH_LONG).show();
            return;
        }
        if (mMediaPlayer == null || mMediaPlayer.isReleased()) {
            setMedia(new Media(mLibvlc, Uri.parse(endPoint)));
        } else if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.play();
        }
    }

    public void stop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    private void setMedia(Media media) {
        //delay = network buffer + file buffer
        //media.addOption(":network-caching=" + Constants.BUFFER);
        //media.addOption(":file-caching=" + Constants.BUFFER);
        if (options != null) {
            for (String s : options) {
                media.addOption(s);
            }
        }
        media.setHWDecoderEnabled(true, false);
        mMediaPlayer = new MediaPlayer(mLibvlc);
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setEventListener(this);

        IVLCVout vlcOut = mMediaPlayer.getVLCVout();
        //set correct class for render depend of constructor called
        if (surfaceView != null) {
            vlcOut.setVideoView(surfaceView);
            width = surfaceView.getWidth();
            height = surfaceView.getHeight();
        } else if (textureView != null) {
            vlcOut.setVideoView(textureView);
            width = textureView.getWidth();
            height = textureView.getHeight();
        } else if (surfaceTexture != null) {
            vlcOut.setVideoSurface(surfaceTexture);
        } else if (surface != null) {
            vlcOut.setVideoSurface(surface, surfaceHolder);
        } else {
            throw new RuntimeException("You cant set a null render object");
        }
        if (width != 0 && height != 0) vlcOut.setWindowSize(width, height);
        vlcOut.attachViews();
        mMediaPlayer.setVideoTrackEnabled(true);
        mMediaPlayer.play();
    }

    public void releasePlayer() {
        if (textureView != null)
            textureView.setKeepScreenOn(false);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            final IVLCVout vout = mMediaPlayer.getVLCVout();
//      vout.removeCallback(mCallback);
            vout.detachViews();
        }
        if (mLibvlc != null) {
            mLibvlc.release();
            mLibvlc = null;
        }
    }

    public void setVlcListener(VlcListener vlcListener) {
        this.mVlcListener = vlcListener;
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        if (mVlcListener == null)
            return;
        switch (event.type) {
            case MediaPlayer.Event.Playing:
                mVlcListener.onBuffering(event);
                break;
            case MediaPlayer.Event.EncounteredError:
                mVlcListener.onError();
                break;
            case MediaPlayer.Event.Buffering:
                mVlcListener.onBuffering(event);
                break;
            case MediaPlayer.Event.TimeChanged: // 播放中
                mVlcListener.onComplete();
                break;
            default:
                break;
        }
    }
}