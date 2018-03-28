package com.example.baidumap.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.example.baidumap.R;
import com.example.baidumap.widght.CustomVideoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppStartActivity extends Activity {

    @BindView(R.id.video_view)
    CustomVideoView videoView;
    @BindView(R.id.tv_time)
    TextView tv_time;

    private MyCountdownTimer countdowntimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appstart);
        ButterKnife.bind(this);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_video);
        videoView.playVideo(uri);

        countdowntimer = new MyCountdownTimer(10000, 1000);
        countdowntimer.start();
    }

    private void redirectTo() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.tv_time)
    public void onClick(View view) {
        finish();
        redirectTo();
    }


    /**
     * 继承 CountDownTimer 防范
     * 参数：  倒计时总数，单位为毫秒、每隔多久调用onTick一次
     * 重写 父类的方法 onTick() 、 onFinish()
     */
    protected class MyCountdownTimer extends CountDownTimer {

        public MyCountdownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tv_time.setText("倒计时(" + millisUntilFinished / 1000 + ")");
        }

        @Override
        public void onFinish() {
            finish();
            redirectTo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countdowntimer.cancel();
    }
}
