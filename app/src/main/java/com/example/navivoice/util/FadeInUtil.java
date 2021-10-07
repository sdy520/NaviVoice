package com.example.navivoice.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.view.animation.LinearInterpolator;

/*
只是改变了音量，并没有关闭播放器
 */
public class FadeInUtil {
    public static void volumeGradient(final MediaPlayer mediaPlayer,
                                      final float from, final float to,
                                      MusicDoneCallBack doneCallBack) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(6000); // 淡入时间
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator it) {
                float volume = (float) it.getAnimatedValue();
                try {
                    // 此时可能 mediaPlayer 状态发生了改变,所以用try catch包裹,一旦发生错误,立马取消
                    mediaPlayer.setVolume(volume, volume);
                } catch (Exception e) {
                    it.cancel();
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                //mediaPlayer.setVolume(from, from);
                if (doneCallBack != null) {
                    doneCallBack.onComplete();
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
//                try {
//                    mediaPlayer.setVolume(from, from);
//                } catch (Exception e) {
//                    //忽略
//                }
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }
}
