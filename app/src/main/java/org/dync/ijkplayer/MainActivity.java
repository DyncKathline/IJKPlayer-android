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

    @OnClick({R.id.btn_setting, R.id.btn_ijkPlayer, R.id.btn_exo2Player, R.id.btn_exoPlayer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_setting:
                startActivity(new Intent(mContext, SettingActivity.class));
                break;
            case R.id.btn_ijkPlayer:
//                String videoPath = "http://baobab.wdjcdn.com/1457423930928CGI.mp4";
//                String videoPath = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8";
//                String videoPath = "http://hot.vrs.sohu.com/ipad3969651_4718009227337_6170972.m3u8?plat=3&uid=e8192000-5281-4dac-9d6d-f4db0f8c7efa&pt=3&prod=mdk&pg=1&qd=130015&cv=1.5";
//                String videoPath = "http://daai.waaarp.wscdns.com/live-transcode/_definst_/smil:daai/tv01.smil/playlist.m3u8";
//                String videoPath = "http://baobab.wdjcdn.com/1457423930928CGI.mp4";
//                String videoPath = "http://www.jingsi.org/icloud/project/50-years/050.mp3";
                String videoPath = "http://jvc.flashapp.cn/baiducdnct.inter.iqiyi.com/tslive/c20_lb_mingzhentankenan_720p_t10/c20_lb_mingzhentankenan_720p_t10.m3u8";
                VideoActivity.intentTo(mContext, videoPath, "测试");
                break;
            case R.id.btn_exo2Player:
                videoPath = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";
                videoPath = "http://jvc.flashapp.cn/baiducdnct.inter.iqiyi.com/tslive/c20_lb_mingzhentankenan_720p_t10/c20_lb_mingzhentankenan_720p_t10.m3u8";
                Video2Activity.intentTo(mContext, videoPath, "测试");
                break;
            case R.id.btn_exoPlayer:
                startActivity(new Intent(mContext, ExoActivity.class));
                break;
        }
    }
}
