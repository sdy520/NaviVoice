package com.example.navivoice.util;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;


public class BackgroundMusicUtil {
    private static MediaPlayer bgmediaplayer;
    private static int bgnumber=0;
    private static final String bgfilename = "/storage/emulated/0/bg/";
    private static String fullbgfilename;
    public static void bgstart(){
        bgmediaplayer = new MediaPlayer();
        play();
    }
    public static void changetomusic(int musicnumber){
        FadeInUtil.volumeGradient(bgmediaplayer, 0.5f, 0f, new MusicDoneCallBack() {
            @Override
            public void onComplete() {
                bgmediaplayer.reset();
                bgnumber = musicnumber;
                play();
            }
        });
    }
    public static void changevolume(float leftvolume,float rightvolum){
        bgmediaplayer.setVolume(leftvolume, rightvolum);
    }
    public static void play(){
        fullbgfilename = bgfilename+bgnumber+".mp3";
        Log.e("TAG", fullbgfilename);
        try {
            bgmediaplayer.setDataSource(fullbgfilename);
            bgmediaplayer.prepare();
            bgmediaplayer.start();
            bgmediaplayer.setVolume(0.5f, 0.5f);
            bgmediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    bgmediaplayer.reset();
                    if(bgnumber<13)
                        bgnumber++;
                    else
                        bgnumber=0;
                    fullbgfilename = bgfilename+bgnumber+".mp3";
                    Log.e("TAG", fullbgfilename);
                    try {
                        bgmediaplayer.setDataSource(fullbgfilename);
                        bgmediaplayer.prepare();
                        bgmediaplayer.start();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
