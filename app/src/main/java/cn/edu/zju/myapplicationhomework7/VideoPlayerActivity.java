package cn.edu.zju.myapplicationhomework7;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoView videoView;
    //手势
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.videoView);
        videoView.setVideoPath("android.resource://" + this.getPackageName() + "/" + R.raw.video);

        MediaController mMediaController = new MediaController(this);
        videoView.setMediaController(mMediaController);
        videoView.start();
        videoView.requestFocus();

        //循环播放
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                mPlayer.start();
                mPlayer.setLooping(true);
            }
        });

        //设置手势
        setGestureDetector();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.suspend();
        }
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            //重新得到进度，继续播放
            int sec = savedInstanceState.getInt("time");
            videoView.seekTo(sec);
            //重设亮度
            float lightness = savedInstanceState.getFloat("lightness");
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = lightness;
            getWindow().setAttributes(layoutParams);
            //重设音量
            int volume = savedInstanceState.getInt("volume");
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 记录当前播放进度
        int sec = videoView.getCurrentPosition();
        outState.putInt("time", sec);
        // 记录亮度
        float lightness = getWindow().getAttributes().screenBrightness;
        outState.putFloat("lightness", lightness);
        // 记录音量
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        outState.putInt("volume", volume);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    //手势方法
    public void setGestureDetector() {
        gestureDetector = new GestureDetector(this,
                new GestureDetector.OnGestureListener() {
                    //默认重载
                    @Override
                    public boolean onDown(MotionEvent motionEvent) {
                        return false;
                    }

                    @Override
                    public void onShowPress(MotionEvent motionEvent) {

                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent motionEvent) {
                        return false;
                    }

                    @Override
                    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent motionEvent) {

                    }

                    // 自定义滚动事件
                    // MotionEvent e1 手势起点事件
                    // MotionEvent e2 手势终点
                    // float distanceX X轴方向上移动的速度/每秒
                    // float distanceY Y轴方向上移动的速度/每秒
                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                        //上划
                        if (e1.getY() - e2.getY() > 0.5 && Math.abs(distanceY) > 0.5) {
                            if (e1.getX() > 500) {
                                addLightness(20);
                            } else {
                                addVolume(1);
                            }
                        }
                        //下划
                        if (e1.getY() - e2.getY() < 0.5 && Math.abs(distanceY) > 0.5) {
                            if (e1.getX() > 500) {
                                addLightness(-20);
                            } else {
                                addVolume(-1);
                            }
                        }
                        return true;
                    }

                }
        );
    }


    // 改变屏幕亮度
    public void addLightness(float lightness) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        //修改屏幕的亮度（最大是255）
        layoutParams.screenBrightness += lightness / 255f;
        if (layoutParams.screenBrightness > 1) {
            layoutParams.screenBrightness = 1;
        } else if (layoutParams.screenBrightness < 0.2) {
            layoutParams.screenBrightness = 0.2f;
        }
        getWindow().setAttributes(layoutParams);
    }

    // 改变音量
    public void addVolume(int volume) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //最大音量
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 修改音量
        currentVolume += volume;
        if (currentVolume < 0) {
            currentVolume = 0;
        }
        if (currentVolume > max) {
            currentVolume = max;
        }

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND);
    }
}
