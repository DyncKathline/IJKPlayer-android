package org.dync.ijkplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.dync.ijkplayerlib.widget.receiver.NetWorkControl;
import org.dync.ijkplayerlib.widget.receiver.NetworkChangedReceiver;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private final String TAG = "MainActivity";

    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_ijkPlayer)
    Button btnIjkPlayer;
    @BindView(R.id.btn_exoPlayer)
    Button btnExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
        NetworkChangedReceiver register = NetWorkControl.register(TAG, this);
//        register.setNetWorkChangeListener(new NetWorkControl.NetWorkChangeListener() {
//            @Override
//            public boolean isConnected(boolean wifiConnected, boolean wifiAvailable, boolean mobileConnected, boolean mobileAvailable) {
//
//                return false;
//            }
//        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetWorkControl.unRegister(TAG, this);
    }

    @OnClick({R.id.btn_setting, R.id.btn_ijkPlayer, R.id.btn_exoPlayer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_setting:
                startActivity(new Intent(mContext, SettingActivity.class));
                break;
            case R.id.btn_ijkPlayer:
                String videoPath = "http://videos.jzvd.org/v/ldj/01-ldj.mp4";
//                String videoPath = "http://videos.jzvd.org/v/饺子主动.mp4";
                VideoActivity.intentTo(mContext, videoPath, "测试");
                break;
            case R.id.btn_exoPlayer:
                startActivity(new Intent(mContext, SimpleActivity.class));
                break;
        }
    }
}
