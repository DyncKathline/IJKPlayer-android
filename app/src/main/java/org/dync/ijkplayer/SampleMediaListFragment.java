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

package org.dync.ijkplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SampleMediaListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SampleMediaAdapter mAdapter;

    public static SampleMediaListFragment newInstance() {
        SampleMediaListFragment f = new SampleMediaListFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_video_url, container, false);
        mRecyclerView = (RecyclerView) viewGroup.findViewById(R.id.recyclerView);
        return viewGroup;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();

        mAdapter = new SampleMediaAdapter(activity);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new SampleMediaAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, SampleMediaAdapter.SampleMediaItem item, int position) {
                String name = item.mName;
                String url = item.mUrl;
                if(onItemClickListener != null) {
                    onItemClickListener.OnItemClick(activity, url, name);
                }
            }
        });

        mAdapter.addItem("http://videos.jzvd.org/v/饺子主动.mp4", "mp4");
        mAdapter.addItem("http://videos.jzvd.org/v/饺子运动.mp4", "mp4");
        mAdapter.addItem("http://videos.jzvd.org/v/饺子有活.mp4", "mp4");
        mAdapter.addItem("http://down.fodizi.com/05/d4267-11.flv", "flv");
        mAdapter.addItem("http://vod-ali.qmai.cc/860287c2cab641d3815fdf271e70188a/ecae4c35afb541b4812b585b917f56c6-0e19caff3a68710e7adec8335f2dd85b-fd.mp4", "mp4");
        mAdapter.addItem("rtmp://live.hkstv.hk.lxdns.com/live/hks", "rtmp");
        mAdapter.addItem("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov", "rtsp");
        mAdapter.addItem("rtsp://218.204.223.237:554/live/1/0547424F573B085C/gsfp90ef4k0a6iap.sdp", "rtsp");
        mAdapter.addItem("http://baobab.wdjcdn.com/1457423930928CGI.mp4", "mp4");
        mAdapter.addItem("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4", "mp4");
        mAdapter.addItem("http://vod.leasewebcdn.com/bbb.flv?ri=1024&rs=150&start=0", "flv");
        mAdapter.addItem("https://videopull.10jqka.com.cn:8188/diwukejibenmianxuangufangfa_1505989287.flv", "flv");
        mAdapter.addItem("http://118.180.8.123/res-share!execute.flv?path=eyJwYXRoIjoiTVA0LzQwMjgzN2U2NTE4ZTk2MzIwMTUxOGVhNWY3ZmEwMGI5L-aWueWQkemXrumimC5tcDQubXA0IiwiYXBwSWQiOiIyMDE0MDEwNDE0MjIxNyIsImFwcE5hbWUiOiJZWFQtYW5kcm9pZCJ9", "flv");
        mAdapter.addItem("https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd", "mpd");
        mAdapter.addItem("https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_hd.mpd", "mpd");
        mAdapter.addItem("http://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism", "ism");
        mAdapter.addItem("http://223.110.243.139/PLTV/3/224/3221225548/index.m3u8", "m3u8");
        mAdapter.addItem("http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/playlist.m3u8", "m3u8");
        mAdapter.addItem("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8", "m3u8");
        mAdapter.addItem("http://daaiguangbo.qmai.cc/cj/dagb.m3u8", "m3u8");
        mAdapter.addItem("http://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism", "ism");
        mAdapter.addItem("https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd", "WV: Clear SD & HD (WebM,VP9)");
        mAdapter.addItem("http://demos.webmproject.org/dash/201410/vp9_glass/manifest_vp9_opus.mpd", "Google Glass DASH - VP9 and Opus");
        mAdapter.addItem("http://demos.webmproject.org/exoplayer/glass_vp9_vorbis.webm", "Google Glass (WebM Video with Vorbis Audio)");
        mAdapter.addItem("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/gear0/fileSequence0.aac", "Apple AAC 10s");
        mAdapter.addItem("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/gear1/fileSequence0.ts", "Apple TS 10s");
        mAdapter.addItem("https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv", "Android screens (Matroska)");


        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8", "bipbop basic master playlist");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8", "bipbop basic 400x300 @ 232 kbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8", "bipbop basic 640x480 @ 650 kbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8", "bipbop basic 640x480 @ 1 Mbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear4/prog_index.m3u8", "bipbop basic 960x720 @ 2 Mbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8", "bipbop basic 22.050Hz stereo @ 40 kbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8", "bipbop advanced master playlist");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear1/prog_index.m3u8", "bipbop advanced 416x234 @ 265 kbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear2/prog_index.m3u8", "bipbop advanced 640x360 @ 580 kbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear3/prog_index.m3u8", "bipbop advanced 960x540 @ 910 kbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear4/prog_index.m3u8", "bipbop advanced 1289x720 @ 1 Mbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear5/prog_index.m3u8", "bipbop advanced 1920x1080 @ 2 Mbps");
        mAdapter.addItem("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear0/prog_index.m3u8", "bipbop advanced 22.050Hz stereo @ 40 kbps");
    }

    interface OnItemClickListener {
        void OnItemClick(Context context, String videoPath, String videoTitle);
    }

    OnItemClickListener onItemClickListener;

    void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }
}
