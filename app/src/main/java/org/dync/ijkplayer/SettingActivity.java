package org.dync.ijkplayer;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_play_type);
        final Settings settings = new Settings(this);
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
    }

}
