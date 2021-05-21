/*
 * Copyright (C) 2016 The Android Open Source Project
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
package org.dync.ijkplayer;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.dync.ijkplayerlib.widget.controller.VideoView;
import org.dync.ijkplayerlib.widget.util.PlayerController;
import org.dync.ijkplayerlib.widget.util.Utils;

public class SimpleActivity extends AppCompatActivity {

    private FrameLayout root;
    private VideoView videoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exo_activity);
        initView();

        videoView.setVideoCoverUrl("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=2973320425,1464020144&fm=27&gp=0.jpg");
        videoView.setVideoPath(this, "http://videos.jzvd.org/v/饺子主动.mp4");
        if(!Utils.isWifiConnected(this) && !PlayerController.WIFI_TIP_DIALOG_SHOWED) {
//            videoView.showWifiDialog();
            return;
        }else {
            videoView.start();
        }
    }

    private void initView() {
        root = (FrameLayout) findViewById(R.id.root);
        videoView = (VideoView) findViewById(R.id.video_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.onDestroyVideo();
        }
    }
}