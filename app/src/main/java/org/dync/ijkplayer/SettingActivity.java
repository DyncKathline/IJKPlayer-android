package org.dync.ijkplayer;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import org.dync.ijkplayerlib.widget.util.Settings;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Setting");

        final Settings settings = new Settings(this);

        CheckBox cb_background_play = (CheckBox) findViewById(R.id.cb_background_play);
        boolean backgroundPlay = settings.getEnableBackgroundPlay();
        cb_background_play.setChecked(backgroundPlay);
        cb_background_play.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setEnableBackgroundPlay(isChecked);
            }
        });
        CheckBox cb_media_codec = (CheckBox) findViewById(R.id.cb_media_codec);
        boolean usingMediaCodec = settings.getUsingMediaCodec();
        cb_media_codec.setChecked(usingMediaCodec);
        cb_media_codec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setUsingMediaCodec(isChecked);
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_play_type);
        int video_mode = settings.getPlayer();
        switch (video_mode) {
            case Settings.PV_PLAYER__IjkExoMediaPlayer:
                radioGroup.check(R.id.IjkExoMediaPlayer);
                break;
            case Settings.PV_PLAYER__AndroidMediaPlayer:
                radioGroup.check(R.id.AndroidMediaPlayer);
                break;
            case Settings.PV_PLAYER__IjkMediaPlayer:
                radioGroup.check(R.id.IjkMediaPlayer);
                break;
            default:
                radioGroup.check(R.id.AutoSelect);
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.AutoSelect:
                        settings.setPlayer(Settings.PV_PLAYER__Auto);
                        break;
                    case R.id.AndroidMediaPlayer:
                        settings.setPlayer(Settings.PV_PLAYER__AndroidMediaPlayer);
                        break;
                    case R.id.IjkMediaPlayer:
                        settings.setPlayer(Settings.PV_PLAYER__IjkMediaPlayer);
                        break;
                    case R.id.IjkExoMediaPlayer:
                        settings.setPlayer(Settings.PV_PLAYER__IjkExoMediaPlayer);
                        break;
                }
            }
        });
        RadioGroup rg_fcc_type = (RadioGroup) findViewById(R.id.rg_fcc_type);
        String pixelFormat = settings.getPixelFormat();
        switch (pixelFormat) {
            case Settings.YV12:
                rg_fcc_type.check(R.id.fcc_rv12);
                break;
            case Settings.RGB_565:
                rg_fcc_type.check(R.id.fcc_rv16);
                break;
            case Settings.RGB_888:
                rg_fcc_type.check(R.id.fcc_rv24);
                break;
            case Settings.RGBX_8888:
                rg_fcc_type.check(R.id.fcc_rv32);
                break;
            case Settings.OpenGL_ES2:
                rg_fcc_type.check(R.id.fcc_es2);
                break;
            default:
                rg_fcc_type.check(R.id.fcc);
                break;
        }
        rg_fcc_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.fcc:
                        settings.setPixelFormat(Settings.Auto_Select);
                        break;
                    case R.id.fcc_rv12:
                        settings.setPixelFormat(Settings.YV12);
                        break;
                    case R.id.fcc_rv16:
                        settings.setPixelFormat(Settings.RGB_565);
                        break;
                    case R.id.fcc_rv24:
                        settings.setPixelFormat(Settings.RGB_888);
                        break;
                    case R.id.fcc_rv32:
                        settings.setPixelFormat(Settings.RGBX_8888);
                        break;
                    case R.id.fcc_es2:
                        settings.setPixelFormat(Settings.OpenGL_ES2);
                        break;
                }
            }
        });
    }

}
