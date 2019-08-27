package com.hc.myapplication;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.VlcListener;
import org.videolan.libvlc.VlcVideoLibrary;

public class MainActivity extends AppCompatActivity {
    private VlcVideoLibrary mVlcVideoLibrary;
    SurfaceView mVLCVideoView;
    ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVLCVideoView = findViewById(R.id.videoVlc);
        mProgressBar = findViewById(R.id.videoPb);
        mVlcVideoLibrary = new VlcVideoLibrary(this, mVLCVideoView);
        mVlcVideoLibrary.setVlcListener(new VlcListener() {
            @Override
            public void onComplete() {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                mVlcVideoLibrary.stop();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onBuffering(MediaPlayer.Event event) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
        mVlcVideoLibrary.play("http://223.110.243.173/ott.js.chinamobile.com/PLTV/3/224/3221227215/index.m3u8");
    }
}
