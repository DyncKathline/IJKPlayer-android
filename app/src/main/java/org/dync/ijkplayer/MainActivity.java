package org.dync.ijkplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_ijkPlayer)
    Button btnIjkPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_setting, R.id.btn_ijkPlayer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_setting:
                break;
            case R.id.btn_ijkPlayer:
                String videoPath = "http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/playlist.m3u8";
                VideoActivity.intentTo(this, videoPath, "测试");
                break;
        }
    }
}
