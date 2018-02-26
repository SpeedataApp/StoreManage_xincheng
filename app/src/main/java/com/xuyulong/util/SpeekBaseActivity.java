package com.xuyulong.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.SynthesizerListener;

public class SpeekBaseActivity extends BaseActivity implements SynthesizerListener {

    SoundUtils mPlayer;

    //合成对象
    public SpeechSynthesizer speechSynthesizer;
    /**
     * 通用回调接口
     */
    public SpeechListener listener = new SpeechListener() {

        //消息回调
        @Override
        public void onEvent(int arg0, Bundle arg1) {
            // TODO Auto-generated method stub

        }

        //数据回调
        @Override
        public void onData(byte[] arg0) {
            // TODO Auto-generated method stub

        }

        //结束回调（没有错误）
        @Override
        public void onCompleted(SpeechError arg0) {
            // TODO Auto-generated method stub

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayer = new SoundUtils(this);
        //appid换成自己申请的
        SpeechUser.getUser().login(this, null, null, "appid=56f0eac7", listener);
        setParam();

    }

    public void Speek(int id) {
        mPlayer.play(id);
    }


    public void setParam() {
        speechSynthesizer = SpeechSynthesizer.createSynthesizer(this);
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        speechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        speechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        speechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
    }

    @Override
    public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCompleted(SpeechError arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSpeakBegin() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSpeakPaused() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSpeakProgress(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSpeakResumed() {
        // TODO Auto-generated method stub

    }

    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    int yourChoice = 7;


}
