package com.example.navivoice.activity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.navivoice.R;
import com.example.navivoice.entity.PosSite;
import com.example.navivoice.room.Pos;
import com.example.navivoice.room.PosDao;
import com.example.navivoice.room.PosDatabase;
import com.example.navivoice.util.APPCONST;
import com.example.navivoice.util.BackgroundMusicUtil;
import com.example.navivoice.util.FadeInUtil;
import com.example.navivoice.util.JsonParserUtil;
import com.example.navivoice.util.MusicDoneCallBack;
import com.example.navivoice.util.WakeUpAsr;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.temolin.hardware.GPIO_Pin;


import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends CheckPermissionsActivity implements INaviInfoCallback {
    private final static String TAG = "MainActivity";
    String posPath = "/storage/emulated/0/voice/pos/";
    String nonPath = "/storage/emulated/0/voice/nonpos/";
    AMapNavi aMapNavi;
    private static MediaPlayer mediaPlayer=new MediaPlayer();
    MediaPlayer smallPosMediaPlayer=new MediaPlayer();
    double car_longitude;
    double car_latitude;
    float car_speed;
    PosDao posDao;
    // ??????????????????????????????
    private SpeechRecognizer mAsrQuestion;
    // ????????????????????????
    private String grmPath;
    private String grmPathQuestion;
    // ??????????????????
    private String resultString;
    // ????????????id
    private String mLocalGrammarID=null;
    // ??????????????????
    private String mLocalGrammar = null;
    //???????????????????????????????????????
    //ArrayList<PosSite> posSiteArrayList;
    ArrayList<Pos> posSiteArrayList;
    ArrayList<Pos> posSmallArrayList;
    //???????????????????????????????????????
    ArrayList<PosSite> nearPosSiteArrayList;
    static String[] nonPosArrayList;
    String[] nonPosQuesArrayList = new String[]{"nla0004","nla0012","nla0013","nla0014","nla0015","nlg01","nlg02","nlg03","nlg05","nlg08","nlg09","nlg10"};
    //????????????????????????
    private AudioManager mAudioManager;
    //???????????????
    private WakeUpAsr wakeUpAsr;
    ExecutorService exec;
    //?????????????????????
    Queue<String> queue;
    Boolean allMediaPlayerFlag = true;
    int value21;
    int value3;
    int value17;
    int value18;
    GPIO_Pin pin21;
    GPIO_Pin pin17;
    GPIO_Pin pin18;
    GPIO_Pin pin3;
    //??????????????????????????????
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    GeocodeSearch geocodeSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //??????1???????????????SharedPreferences??????
        sharedPreferences = getSharedPreferences("publicNoPosData",Context.MODE_PRIVATE);
        //??????2??? ?????????SharedPreferences.Editor??????
        editor = sharedPreferences.edit();
        //??????3????????????????????????????????????
        editor.putInt("publicNoPosNumber", 0);
        //??????4?????????
        editor.apply();
        //?????????????????????
        geocodeSearch = new GeocodeSearch(this);
        //??????????????????????????????
        GetPosSite();
        //??????????????????
        BackgroundMusicUtil.bgstart();
        //????????????????????????
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // ????????????AMapNavi???????????????
        aMapNavi = AMapNavi.getInstance(getApplicationContext());
        //mediaPlayer = new MediaPlayer();

        //??????103.40433,36.444996???????????????
        //Poi start = new Poi("??????", new LatLng(36.444996,103.40433),null);
        Poi start = new Poi("??????", new LatLng(36.489411,103.61449),null);
        //Poi start = new Poi("??????", new LatLng(36.874387,103.187258),null);
        //Poi start = new Poi("??????", new LatLng(36.911395,103.15939),null);
        //????????????103.166964,36.890526
        Poi end = new Poi("??????", new LatLng(37.941925, 102.638239),null);
        //Poi end = new Poi("??????", new LatLng(36.890526,103.166964),null);
        // ??????????????????
        AmapNaviParams params = new AmapNaviParams(start, null, end, AmapNaviType.DRIVER, AmapPageType.ROUTE);
        //?????????????????? 3-????????????
        params.setBroadcastMode(this,3);
        //????????????????????????????????????????????????
        params.setShowCrossImage(false);
        params.setUseInnerVoice(false);
        params.setDayAndNightMode(this,1);
        // ????????????
        AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, MainActivity.this);
        //?????????
        exec = Executors.newSingleThreadExecutor();
        queue = new LinkedList<>();
        grmPath = getExternalFilesDir("msc").getAbsolutePath() + "/test";
        //grmPathQuestion = getExternalFilesDir("msc1").getAbsolutePath() + "/question";
        wakeUpAsr = new WakeUpAsr(this,mAudioManager,grmPath);
        wakeUpAsr.startWakeUpAsr();
        // ?????????????????????---??????
        mAsrQuestion = SpeechRecognizer.createRecognizer(this, mInitListener);
        //initGrammar();
        //??????
        //initGpio();

    }

    private void initGpio() {
        pin21 = new GPIO_Pin(2010,true);
        pin21.setToGpioMode();
        pin21.setModeINPUT();
        pin3 = new GPIO_Pin(2005,true);
        pin3.setToGpioMode();
        pin3.setModeINPUT();
        pin17 = new GPIO_Pin(2002,true);
        pin17.setToGpioMode();
        pin17.setModeINPUT();
        pin18 = new GPIO_Pin(2007,true);
        pin18.setToGpioMode();
        pin18.setModeINPUT();
    }

    private void initGrammar() {
        mAsrQuestion.setParameter(SpeechConstant.PARAMS, null);
        mAsrQuestion.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // ??????????????????
        mAsrQuestion.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // ????????????????????????
        mAsrQuestion.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPathQuestion);
        mAsrQuestion.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
        // ??????????????????
        // ???????????????????????????????????????????????????pcm???wav??????????????????sd????????????WRITE_EXTERNAL_STORAGE??????
        mAsrQuestion.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mAsrQuestion.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr2.wav");
        // ????????????????????????
        mAsrQuestion.setParameter(ResourceUtil.ASR_RES_PATH, ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
    }

    /*?????????????????????????????????????????????????????????????????????????????????
     * GetPosSite()?????????????????????
     * GetSmallPosSite()??????????????????,????????????mediaplay??????
     * */
    private void GetPosSite() {
        posDao = PosDatabase.getDatabase(this).getPosDao();
        new Thread(() -> {
            //posSiteArrayList = (ArrayList<PosSite>) posDao.getAllPosSite();
            posSiteArrayList = (ArrayList<Pos>) posDao.getAllPos();
            posSmallArrayList = (ArrayList<Pos>) posDao.getAllSmallPos();
            Log.e(TAG, String.valueOf(posSiteArrayList));
            Log.e(TAG, String.valueOf(posSmallArrayList));
            //Log.e(TAG, String.valueOf(pos));
        }).start();
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onGetNavigationText(String s) {

    }
    //?????????????????????
    static int nonPosVoiceNumber;
    //?????????????????????
    static int nonPublicPosVoiceNumber;
    //??????????????????????????????????????????
    boolean[] areas = {true,true,true,true};
    Boolean initArrayList =true;
    static int nonPosArrayList_index;
    //???????????????????????????????????????
    static int allpublic;
    static String nonPosPath;
    String adCode;
    String cityCode;
    RegeocodeQuery query;
    //?????????????????????????????????
    boolean geocodeSearch_flag =true;
    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        car_longitude=aMapNaviLocation.getCoord().getLongitude();
        car_latitude=aMapNaviLocation.getCoord().getLatitude();
        car_speed=aMapNaviLocation.getSpeed();

        //??????????????????????????????????????????????????????????????????????????????????????????????????????
        LatLonPoint latLonPoint = new LatLonPoint(aMapNaviLocation.getCoord().getLatitude(), aMapNaviLocation.getCoord().getLongitude());
        // AMAP ????????????????????????????????????????????????500m
        query = new RegeocodeQuery(latLonPoint, 1000f, GeocodeSearch.AMAP);
        //????????????



        if(geocodeSearch_flag) {
            geocodeSearch_flag=false;
            geocodeSearch.getFromLocationAsyn(query);

            geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                @Override
                public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                    geocodeSearch_flag=true;
                    RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
                    adCode = regeocodeAddress.getAdCode();
                    cityCode = regeocodeAddress.getCityCode();
                    Log.i(TAG, adCode + "onRegeocodeSearched: " + cityCode);
                    //String formatAddress = regeocodeAddress.getFormatAddress();
                    //Log.e("TAG", "onRegeocodeSearched: "+formatAddress.substring(9)+adCode+cityCode);
                    //????????????
                    determineAreas();

                    //??????????????????
                    SpotTrigger();
                }

                @Override
                public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

                }
            });
        }

        //gpio???????????????
        //GpioControl();
    }

    private void determineAreas() {
        //???????????????
        if (areas[0] && cityCode.equals("0931")){
            Log.e(TAG, "???????????? ");
            nonPosArrayList = APPCONST.lanzhounonPosArrayList;
            nonPosPath = APPCONST.lanzhouNonPosPath;
            nonPosVoiceNumber = 10;//??????11???
            nonPublicPosVoiceNumber = 3;//??????3???
            nonPosArrayList_index = 0;
            allpublic =0;
            areas[0] = false;
        }else if(areas[1] && adCode.equals("620623")){
            Log.e(TAG, "???????????? ");
            nonPosArrayList = APPCONST.tianzhunonPosArrayList;
            nonPosPath = APPCONST.tianzhuNonPosPath;
            nonPosVoiceNumber = 1;
            nonPublicPosVoiceNumber = 2;
            nonPosArrayList_index = 0;
            allpublic =0;
            areas[1] = false;
        }else if(areas[2] && adCode.equals("620622")){
            Log.e(TAG, "???????????? ");
            nonPosArrayList = APPCONST.gulangnonPosArrayList;
            nonPosPath = APPCONST.gulangNonPosPath;
            nonPosVoiceNumber = 0;
            nonPublicPosVoiceNumber = 3;
            nonPosArrayList_index = 0;
            allpublic =0;
            areas[2] = false;
            //???????????????????????????????????????
        }else if(areas[3] && (adCode.equals("620600")||adCode.equals("620601")||adCode.equals("620602")||adCode.equals("620621"))){
            Log.e(TAG, "????????????????????????");
            nonPosArrayList = APPCONST.wuweinonPosArrayList;
            nonPosPath = APPCONST.wuweiNonPosPath;
            nonPosVoiceNumber = 9;
            nonPublicPosVoiceNumber = 2;
            nonPosArrayList_index = 0;
            allpublic =0;
            areas[3] = false;
        }
    }

    private void GpioControl(){
        value21 = pin21.getInputVal();
        value3 = pin3.getInputVal();
        value17 = pin17.getInputVal();
        value18 = pin18.getInputVal();
        if(value21==0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(value21==0) {
                Log.e(TAG, "onLocationChange:" + value21);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        }
        if(value3==0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(value3==0) {
                Log.e(TAG, "onLocationChange:" + value3);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        }
        if(value17==0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(value17==0) {
                Log.e(TAG, "onLocationChange:" + value17);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        }
        if(value18==0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(value18==0) {
                Log.e(TAG, "onLocationChange:" + value18);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        }
    }
    Boolean posflag = true;
    int musicDuration;
    double toNextPosTime;
    int musicPublicDuration;
    double toNextPublicPosTime;
    //??????????????????????????????
    String noPosName;
    //??????????????????????????????
    String noPosPath;
    //??????????????????????????????
    String noPublicPosName;
    //??????????????????????????????
    String noPublicPosPath;
    //????????????????????????????????????
    int nonPosPublicArrayList_index;
    Boolean onlyOnceQuestionFlag = false;
    Boolean smallPosMediaplayerFlag = true;
    private void SpotTrigger() {
        ArrayList<Pos> posSiteNear = GetNearPos(car_latitude);
        //Log.e(TAG, posSiteNear.toString());
        double distance = getDistance(car_longitude,car_latitude,posSiteNear.get(0).getLongitude(),posSiteNear.get(0).getLatitude());
        if(distance*1000 <= posSiteNear.get(0).getRadius()){
            Log.d(TAG, "??????"+ distance);
            //???????????????????????????????????????????????????????????????
            //????????????????????????????????????????????????????????????

            if(mediaPlayer.isPlaying() && posflag && allMediaPlayerFlag){
                //?????????????????????????????????????????????????????????????????????????????????
                //???????????????????????????
                /*if(!queue.isEmpty())
                    onlyOnceQuestionFlag = true;*/
                //if(posflag && allMediaPlayerFlag){
                posflag = false;
                FadeInUtil.volumeGradient(mediaPlayer, 1f, 0f, new MusicDoneCallBack() {
                    @Override
                    public void onComplete() {
                        String PosPath = posPath + posSiteNear.get(0).getVoice_name() + ".mp3";
                        Log.e(TAG, 1+PosPath);
                        playMediaPlayer(PosPath);
                        //??????????????????????????????????????????????????????????????????
                        //posSiteNear.get(0).setAttraction_flag(1);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //posDao.updatePos(posSiteNear.get(0));
                                posSiteArrayList.remove(posSiteNear.get(0));
                            }
                        }).start();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                posflag = true;
                                Log.e(TAG, 1+String.valueOf(posflag));
                            }
                        });
                    }
                });
            }
            //??????????????????????????????????????????
            else if(posflag && allMediaPlayerFlag){
                //?????????????????????????????????????????????????????????????????????????????????
                //???????????????????????????
                /*if(!queue.isEmpty())
                    onlyOnceQuestionFlag = true;*/
                posflag = false;
                String PosPath = posPath + posSiteNear.get(0).getVoice_name() + ".mp3";
                Log.e(TAG, 2+PosPath);
                playMediaPlayer(PosPath);
                //??????????????????????????????????????????????????????????????????
                //posSiteNear.get(0).setAttraction_flag(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //posDao.updatePos(posSiteNear.get(0));
                        posSiteArrayList.remove(posSiteNear.get(0));
                    }
                }).start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        //?????????????????????posflag???true,????????????????????????
                        posflag = true;
                        Log.e(TAG, 2+String.valueOf(posflag));
                    }
                });
            }
        } else {
            //isPlaying????????????????????????internal/external state mismatch corrected,mAudioTearDown = 0

            /*if(nonPosArrayList_index==0 || nonPosArrayList_index==nonPosArrayList.length-1){
                musicDuration = getMusicDuration(noPosPath);
                toNextPosTime = (distance-(posSiteNear.get(0).getRadius()/1000.0))/car_speed*3600;
                Log.e(TAG, "SpotTrigger1: "+musicDuration+"comp"+toNextPosTime);
            } else if(nonPosArrayList_index<nonPosArrayList.length-1){
                String nextNoPosName = nonPosArrayList[nonPosArrayList_index+1];
                String nextNoPosPath = nonPosPath + nextNoPosName + ".mp3";
                musicDuration = getMusicDuration(nextNoPosPath);
                toNextPosTime = (distance-(posSiteNear.get(0).getRadius()/1000.0))/car_speed*3600;
                Log.e(TAG, "SpotTrigger: "+musicDuration+"comp"+toNextPosTime);
            }*/
            if(nonPosArrayList_index<=nonPosArrayList.length-1){

                noPosName = nonPosArrayList[nonPosArrayList_index];
                noPosPath = nonPosPath + noPosName + ".mp3";
                musicDuration = getMusicDuration(noPosPath);
                toNextPosTime = (distance-(posSiteNear.get(0).getRadius()/1000.0))/car_speed*3600;
                //Log.e(TAG, "SpotTrigger1: "+musicDuration+"comp"+toNextPosTime);
            }

            nonPosPublicArrayList_index = sharedPreferences.getInt("publicNoPosNumber",0);

            if(nonPosPublicArrayList_index <= APPCONST.nonPublicPosArrayList.length-1)
            {

                noPublicPosName = APPCONST.nonPublicPosArrayList[nonPosPublicArrayList_index];
                noPublicPosPath = nonPath + noPublicPosName + ".mp3";
                musicPublicDuration = getMusicDuration(noPublicPosPath);
                toNextPublicPosTime = (distance-(posSiteNear.get(0).getRadius()/1000.0))/car_speed*3600;
            }
            /*if(nonPosPublicArrayList_index == 0)
            {
                musicPublicDuration = getMusicDuration(noPublicPosPath);
                toNextPublicPosTime = (distance-(posSiteNear.get(0).getRadius()/1000.0))/car_speed*3600;
            }
            if(nonPosPublicArrayList_index<APPCONST.nonPublicPosArrayList.length-1){
                String nextNoPublicPosName = APPCONST.nonPublicPosArrayList[nonPosPublicArrayList_index+1];
                String nextNoPublicPosPath = nonPath + nextNoPublicPosName + ".mp3";
                musicPublicDuration = getMusicDuration(nextNoPublicPosPath);
                toNextPublicPosTime = (distance-(posSiteNear.get(0).getRadius()/1000.0))/car_speed*3600;
            }*/
            if(smallPosMediaplayerFlag && !mediaPlayer.isPlaying() && allMediaPlayerFlag && (!queue.isEmpty())){
                Log.e(TAG, "SpotTrig   30 ");
                exec.execute(new ThreadShow(0));
            }
            else if(smallPosMediaplayerFlag &&!mediaPlayer.isPlaying() && allMediaPlayerFlag && nonPosArrayList_index >nonPosVoiceNumber && allpublic < nonPublicPosVoiceNumber && toNextPublicPosTime > musicPublicDuration && nonPosPublicArrayList_index < APPCONST.nonPublicPosArrayList.length)
            {
                allpublic++;
                Log.e(TAG, noPublicPosPath);
                playMediaPlayer(noPublicPosPath);
                if(useLoop(nonPosQuesArrayList,noPublicPosName)) {
                    queue.offer(noPublicPosName);
                    Log.e(TAG, String.valueOf(queue));
                    //exec.execute(new ThreadShow(30000));
                }
                nonPosPublicArrayList_index++;
                editor.putInt("publicNoPosNumber",nonPosPublicArrayList_index);
                editor.apply();
                //if(nonPosPublicArrayList_index < APPCONST.nonPublicPosArrayList.length-1) {
                   /* nonPosPublicArrayList_index++;
                    editor.putInt("publicNoPosNumber",nonPosPublicArrayList_index);
                    editor.apply();*/
               // }
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        //???????????????setOnCompletionListener???????????????????????????
                        posflag = true;
                       /* Log.e(TAG, noPublicPosName);
                        if(useLoop(nonPosQuesArrayList,noPublicPosName)) {
                            Log.e(TAG, noPublicPosName);
                            queue.offer(noPublicPosName);
                            Log.e(TAG, String.valueOf(queue));
                            //exec.execute(new ThreadShow(30000));
                        }*/
                        /*int temp_nonPosPublicArrayList_index = nonPosPublicArrayList_index -1;
                        String temp_noPublicPosName= APPCONST.nonPublicPosArrayList[temp_nonPosPublicArrayList_index];
                        if(useLoop(nonPosQuesArrayList,temp_noPublicPosName)) {
                            Log.e(TAG, temp_noPublicPosName);
                            exec.execute(new ThreadShow(30000));
                        }*/
                    }
                });

            }
            else if(smallPosMediaplayerFlag && !mediaPlayer.isPlaying() && allMediaPlayerFlag && toNextPosTime > musicDuration && nonPosArrayList_index<nonPosArrayList.length){
                Log.e(TAG, noPosPath);
                playMediaPlayer(noPosPath);
                if(useLoop(nonPosQuesArrayList,noPosName)) {
                    Log.e(TAG, noPosName);
                    queue.offer(noPosName);
                    Log.e(TAG, String.valueOf(queue));
                    //exec.execute(new ThreadShow(30000));
                }
                //?????????????????????setOnCompletionListener????????????
                nonPosArrayList_index++;
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        //???????????????setOnCompletionListener???????????????????????????
                        posflag = true;
                        /*Log.e(TAG, noPosName);
                        if(useLoop(nonPosQuesArrayList,noPosName)) {
                            Log.e(TAG, noPosName);
                            queue.offer(noPosName);
                            Log.e(TAG, String.valueOf(queue));
                            //exec.execute(new ThreadShow(30000));
                        }*/
                        //?????????????????????????????????????????????????????????
                       /* if(nonPosArrayList_index!=0) {
                            int temp_nonPosArrayList_index = nonPosArrayList_index - 1;
                            String temp_noPosName = nonPosArrayList[temp_nonPosArrayList_index];
                            if (useLoop(nonPosQuesArrayList, temp_noPosName)) {
                                Log.e(TAG, temp_noPosName);
                                exec.execute(new ThreadShow(30000));
                            }
                        }*/
                    }
                });

                //if(nonPosArrayList_index < nonPosArrayList.length){
                //nonPosArrayList_index++;
                //}
                //????????????????????????
                /*if(nonPosArrayList_index==nonPosArrayList.length-1)
                    BackgroundMusicUtil.changevolume(1f,1f);*/
            }
            //else if(!mediaPlayer.isPlaying() && toNextPosTime >30 && onlyOnceQuestionFlag){
         /*   else if(!mediaPlayer.isPlaying()){
                Log.e(TAG, "SpotTrig   30 ");
                if(!queue.isEmpty()){
                    exec.execute(new ThreadShow(0));
                    //onlyOnceQuestionFlag = false;
                }
            }*/
            /*else if(!mediaPlayer.isPlaying()){
                Log.e(TAG, "SpotTrigger: "+ (int) toNextPosTime +"public"+(int)toNextPublicPosTime );
            }*/
            /*else if(!queue.isEmpty() && !mediaPlayer.isPlaying() && allMediaPlayerFlag && toNextPosTime > 25){
                //????????????????????????????????????????????????
                exec.execute(new ThreadShow(2000));
            }*/
            //?????????????????????
            // else if(!mediaPlayer.isPlaying() && allMediaPlayerFlag)

            //?????????????????????
            smallPosTrigger();
        }

    }
    //????????????????????? ???????????????????????????????????????
/*    private void smallPosTrigger(){
        Pos nearSmallPos = GetNearSmallPos(car_latitude);
        if(nearSmallPos!=null){
            double distanceSmallPos = getDistance(car_longitude,car_latitude,nearSmallPos.getLongitude(),nearSmallPos.getLatitude());
            if(distanceSmallPos*1000 <= nearSmallPos.getRadius()){
                String smallPosPath = posPath + nearSmallPos.getVoice_name() + ".mp3";
                Log.e(TAG, "small"+smallPosPath);
                //BackgroundMusicUtil.changevolume(0f,0f);

                mediaPlayer.setVolume(0.2f,0.2f);
                smallPosMediaPlayer.reset();
                try {
                    smallPosMediaPlayer.setDataSource(smallPosPath);
                    smallPosMediaPlayer.prepare();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                smallPosMediaPlayer.start();
                smallPosMediaPlayer.setVolume(1f,1f);
                smallPosMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        //BackgroundMusicUtil.changevolume(0.5f,0.5f);
                        mediaPlayer.setVolume(1f,1f);
                    }
                });
                //??????????????????????????????????????????????????????????????????
                // posSiteNear.get(0).setAttraction_flag(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //posDao.updatePos(posSiteNear.get(0));
                        posSmallArrayList.remove(nearSmallPos);
                    }
                }).start();
            }
        }
    }*/

    //?????????????????????
    private void smallPosTrigger(){
        Pos nearSmallPos = GetNearSmallPos(car_latitude);
        if(nearSmallPos!=null){
            double distanceSmallPos = getDistance(car_longitude,car_latitude,nearSmallPos.getLongitude(),nearSmallPos.getLatitude());
            if(distanceSmallPos*1000 <= nearSmallPos.getRadius()){
                //??????????????????mediaPlayer???????????????mediaPlayer??????????????????????????????????????????????????????mediaPlayer??????????????????
                smallPosMediaplayerFlag = false;
                String smallPosPath = posPath + nearSmallPos.getVoice_name() + ".mp3";
                Log.e(TAG, "small"+smallPosPath);
                //BackgroundMusicUtil.changevolume(0f,0f);
                //??????????????????????????????????????????????????????mediaPlayer.start()????????????????????????????????????mediaPlayer.isplaying()?????????
                FadeInUtil.volumeGradient(mediaPlayer, 1f, 0f, new MusicDoneCallBack() {
                    @Override
                    public void onComplete() {
                        mediaPlayer.pause();
                        smallPosMediaPlayer.reset();
                        try {
                            smallPosMediaPlayer.setDataSource(smallPosPath);
                            smallPosMediaPlayer.prepare();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        smallPosMediaPlayer.start();
                        smallPosMediaPlayer.setVolume(1f,1f);
                        smallPosMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mediaPlayer.start();
                                smallPosMediaplayerFlag = true;
                                FadeInUtil.volumeGradient(mediaPlayer, 0f, 1f, new MusicDoneCallBack() {
                                    @Override
                                    public void onComplete() {
                                    }
                                });
                            }
                        });
                    }
                });
                //??????????????????????????????????????????????????????????????????
                // posSiteNear.get(0).setAttraction_flag(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //posDao.updatePos(posSiteNear.get(0));
                        posSmallArrayList.remove(nearSmallPos);
                    }
                }).start();
            }
        }
    }
   /* private void SpotTrigger() {
        nearPosSiteArrayList = GetNearPosSite(car_latitude);
        for(int i = 0;i < nearPosSiteArrayList.size();i++){
            double distance = getDistance(car_longitude,car_latitude,nearPosSiteArrayList.get(i).getLongitude(),nearPosSiteArrayList.get(i).getLatitude());
            Log.e(TAG, "??????"+ distance);
            if(distance*1000 <= nearPosSiteArrayList.get(i).getRadius()){
                Log.d(TAG, "??????"+ distance);
                //???????????????????????????????????????????????????????????????
                //????????????????????????????????????????????????????????????

                int finalI = i;
                if(mediaPlayer.isPlaying() && posflag && allMediaPlayerFlag){
                //if(posflag && allMediaPlayerFlag){
                    posflag = false;
                    Log.e(TAG, 1+String.valueOf(posflag));
                    FadeInUtil.volumeGradient(mediaPlayer, 1f, 0f, new MusicDoneCallBack() {
                        @Override
                        public void onComplete() {
                            String PosPath = posPath + nearPosSiteArrayList.get(finalI).getVoice_name() + ".mp3";
                            Log.e(TAG, 1+PosPath);
                            playMediaPlayer(PosPath);
                            //??????????????????????????????????????????????????????????????????
                            Log.e(TAG,nearPosSiteArrayList.get(finalI).toString());
                            posSiteArrayList.remove(nearPosSiteArrayList.get(finalI));
                            Log.e(TAG, String.valueOf(posSiteArrayList));
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    posflag = true;
                                    Log.e(TAG, 1+String.valueOf(posflag));
                                }
                            });
                        }
                    });
                }
                //??????????????????????????????????????????
                else if(posflag && allMediaPlayerFlag){
                    posflag = false;
                    Log.e(TAG, 2+String.valueOf(posflag));
                    String PosPath = posPath + nearPosSiteArrayList.get(finalI).getVoice_name() + ".mp3";
                    Log.e(TAG, 2+PosPath);
                    playMediaPlayer(PosPath);
                    //??????????????????????????????????????????????????????????????????
                    Log.e(TAG,nearPosSiteArrayList.get(finalI).toString());
                    posSiteArrayList.remove(nearPosSiteArrayList.get(finalI));
                    Log.e(TAG, String.valueOf(posSiteArrayList));
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            posflag = true;
                            Log.e(TAG, 2+String.valueOf(posflag));
                        }
                    });
                }

            }else{
                PosSite posSiteNear = new PosSite();
                posSiteNear = GetNearPos(car_latitude);
                //Log.e(TAG, posSiteNear.toString());
                double distanceNearCar = getDistance(car_longitude,car_latitude,posSiteNear.getLongitude(),posSiteNear.getLatitude());
                //isPlaying????????????????????????internal/external state mismatch corrected,mAudioTearDown = 0
                String noPosName = lanzhounonPosArrayList[lanzhounonPosArrayList_index];
                String noPosPath = lanzhouNonPosPath + noPosName + ".mp3";
                String nextNoPosName = lanzhounonPosArrayList[lanzhounonPosArrayList_index+1];
                String nextNoPosPath = lanzhouNonPosPath + nextNoPosName + ".mp3";
                int musicDuration = getMusicDuration(nextNoPosPath);
                //double toNextPosTime = (distance-(nearPosSiteArrayList.get(i).getRadius()/1000.0))/car_speed*3600;
                double toNextPosTime = (distanceNearCar-(posSiteNear.getRadius()/1000.0))/car_speed*3600;
                Log.e(TAG, "SpotTrigger: "+(toNextPosTime-musicDuration));
                Log.e(TAG, posSiteNear.toString());
                if(!mediaPlayer.isPlaying() && allMediaPlayerFlag && toNextPosTime > musicDuration){

                    Log.e(TAG, noPosPath);
                    playMediaPlayer(noPosPath);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.e(TAG, noPosName);
                            if(useLoop(nonPosQuesArrayList,noPosName)) {
                                Log.e(TAG, noPosName);
                                queue.offer(noPosName);
                                Log.e(TAG, String.valueOf(queue));
                                exec.execute(new ThreadShow(30000));
                            }
                        }
                    });
                    if(lanzhounonPosArrayList_index < lanzhounonPosArrayList.length-1)
                        lanzhounonPosArrayList_index++;
                    else
                        lanzhounonPosArrayList_index = 0;

                }
            }
        }
    }*/
    //???????????????????????????
    public static boolean useLoop(String[] arr, String targetValue) {
        for(String s: arr){
            if(s.equals(targetValue))
                return true;
        }
        return false;
    }
    private int getMusicDuration(String filename) {
        MP3File file_mp3 = null;
        File file_read = new File(filename);
        int length = 0;
        try {
            file_mp3 = (MP3File) AudioFileIO.read(file_read);
            MP3AudioHeader audioHeader = (MP3AudioHeader) file_mp3.getAudioHeader();
            length = audioHeader.getTrackLength();
            //Log.e("MP3 ?????????????????????(s)???", String.valueOf(length));
            return length;
        } catch (Exception e) {
            Log.e("MP3 ????????????????????????", "???????????????????????????");
            e.printStackTrace();
        }
        return 0;
    }
    int asrAnswerNumber;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.e(TAG, String.valueOf(posflag));
                if(posflag){
                    allMediaPlayerFlag = false;//??????????????????????????????????????????
                    BackgroundMusicUtil.changetomusic(13);
                    FadeInUtil.volumeGradient(mediaPlayer, 1f, 0f, new MusicDoneCallBack() {
                        @Override
                        public void onComplete() {
                            String quesName = queue.poll();
                            String filename = "/storage/emulated/0/voice/question/";
                            filename=filename+quesName+".mp3";
                            Log.e(TAG,filename);
                            playMediaPlayer(filename);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    //allMediaPlayerFlag = false;//??????????????????????????????????????????,???????????????????????????????????????
                                    //??????????????????????????????
                                    asrAnswerNumber = 1;
                                    String dynasty=quesName;
                                    grmPathQuestion = getExternalFilesDir("msc").getAbsolutePath() + "/question"+quesName;
                                    //grmPathQuestion = Environment.getExternalStorageDirectory() + "/msc1/question"+quesName;
                                    //String gram="grmPath"+ques_number;
                                    mLocalGrammar = readFile(MainActivity.this, dynasty+".bnf", "utf-8");
                                    String local = new String(mLocalGrammar);
                                    Log.e(TAG,dynasty);
                                    mAsrQuestion.setParameter(SpeechConstant.PARAMS, null);
                                    mAsrQuestion.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
                                    // ??????????????????
                                    mAsrQuestion.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
                                    // ????????????????????????
                                    mAsrQuestion.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPathQuestion);
                                    mAsrQuestion.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
                                    // ??????????????????
                                    // ???????????????????????????????????????????????????pcm???wav??????????????????sd????????????WRITE_EXTERNAL_STORAGE??????
                                    mAsrQuestion.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
                                    mAsrQuestion.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr2"+quesName+".wav");
                                    // ????????????????????????
                                    mAsrQuestion.setParameter(ResourceUtil.ASR_RES_PATH, ResourceUtil.generateResourcePath(getApplicationContext(), ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
                                    mAsrQuestion.setParameter(SpeechConstant.LOCAL_GRAMMAR, dynasty);
                                    //mAsrQuestion.setParameter(ResourceUtil.GRM_BUILD_PATH, gram);
                                    mAsrQuestion.buildGrammar("bnf",local, grammarListener);
                                    //??????????????????
                                    wakeUpAsr.stopIvmListening();

                                   // mAsrQuestion.startListening(mRecognizerListener);
                                }
                            });
                        }
                    });
                }else {
                    Log.e(TAG, "???????????????: ");
                    exec.execute(new ThreadShow(12000));

                }
                //?????????????????????????????????
                /*if(mMediaPlayer.isPlaying()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/
            }

        };
    };

    // ?????????
    class ThreadShow implements Runnable {
        private long time;
        public ThreadShow(long time){
            this.time = time;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(time);
                //1000,1???
                //Thread.sleep(30000);
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                mLocalGrammarID = grammarId;
                Log.e("gram",mLocalGrammarID);
                mAsrQuestion.startListening(mRecognizerListener);
            } else {
                Log.e("??????????????????,????????????" ,error.getErrorCode()+",???????????????https://www.xfyun.cn/document/error-code??????????????????");
            }
        }
    };

    /**
     * ?????????????????????
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e("???????????????,????????????",code+",???????????????https://www.xfyun.cn/document/error-code??????????????????");
            }
        }
    };
    public int errocode;
    /**
     * ??????????????????
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d("????????????????????????????????????", String.valueOf(volume));
            Log.d("TAG", "?????????????????????"+data.length);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result && !TextUtils.isEmpty(result.getResultString())) {
                Log.e("TAG", "recognizer result???" + result.getResultString());
                //recoString = JsonParser.parseGrammarResult(result.getResultString());
                int recoint = JsonParserUtil.parseGrammarResultcontact(result.getResultString());
                Log.e("TAG", " "+recoint);
                if(recoint>30)
                {
                    String correct = "/storage/emulated/0/voice/reply/correct.mp3";
                    playMediaPlayer(correct);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                           allMediaPlayerFlag = true;
                        }
                    });
                    Log.e(TAG, "?????????: ");
                }
                else {
                    String mistake = "/storage/emulated/0/voice/reply/mistake.mp3";
                    playMediaPlayer(mistake);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            allMediaPlayerFlag = true;
                        }
                    });
                    Log.e("TAG", "????????????????????????????????????????????????recognizer result : null");
                }
                wakeUpAsr.startWakeUpAsr();
            }
        }

        @Override
        public void onEndOfSpeech() {
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????
            Log.e(TAG, "end");

        }

        @Override
        public void onBeginOfSpeech() {
            // ??????????????????sdk??????????????????????????????????????????????????????????????????
            Log.e(TAG,"????????????");
        }

        @Override
        public void onError(SpeechError error) {
            if(error.getErrorCode()==20005&&asrAnswerNumber<5){
                asrAnswerNumber++;
                Log.e(TAG, "??????"+ asrAnswerNumber);
                mAsrQuestion.startListening(mRecognizerListener);
            }
            else {

                String fault= "/storage/emulated/0/voice/reply/fault.mp3";
                playMediaPlayer(fault);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        allMediaPlayerFlag = true;
                    }
                });
                Log.e(TAG, "????????????????????????????????????????????????: ");
                wakeUpAsr.startWakeUpAsr();
            }
            errocode=error.getErrorCode();
            Log.e("TAG1", "onError Code???"	+ error.getErrorCode());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            Log.e("TAG1", "event");
        }

    };
    @Override
    public void onArriveDestination(boolean b) {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onStopSpeaking() {

    }

    @Override
    public void onReCalculateRoute(int i) {

    }

    @Override
    public void onExitPage(int i) {

    }

    @Override
    public void onStrategyChanged(int i) {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviDirectionChanged(int i) {

    }

    @Override
    public void onDayAndNightModeChanged(int i) {

    }

    @Override
    public void onBroadcastModeChanged(int i) {

    }

    @Override
    public void onScaleAutoChanged(boolean b) {

    }

    @Override
    public View getCustomMiddleView() {
        return null;
    }

    @Override
    public View getCustomNaviView() {
        return null;
    }

    @Override
    public View getCustomNaviBottomView() {
        return null;
    }

    private void playMediaPlayer(String musicName){
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(musicName);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        // //FadeIn???????????????0???????????????????????????(??????FadeIn???)
        mediaPlayer.setVolume(1f, 1f);
    }

    /*public ArrayList<PosSite> GetNearPosSite(Double latitude){
        ArrayList<PosSite> posSites = new ArrayList<>();
        for(int i = 0; i < posSiteArrayList.size(); i++) {
            if(latitude - 0.05 < posSiteArrayList.get(i).getLatitude() && posSiteArrayList.get(i).getLatitude() < latitude + 0.05)
                posSites.add(posSiteArrayList.get(i));
        }
        return posSites;
    }*/
//????????????????????????????????????????????????
    public ArrayList<Pos> GetNearPos(Double latitude){
        ArrayList<Pos> posSites = new ArrayList<>();
        int tempNumber = 0;
        double temp=10000;
        for(int i = 0; i < posSiteArrayList.size(); i++) {
            //if(latitude - 0.05 < posSiteArrayList.get(i).getLatitude() && posSiteArrayList.get(i).getLatitude() < latitude + 0.05)
            if(latitude - 0.2 < posSiteArrayList.get(i).getLatitude() && posSiteArrayList.get(i).getLatitude() < latitude + 0.2)
            {
                if(temp>getDistance(car_longitude,car_latitude,posSiteArrayList.get(i).getLongitude(),posSiteArrayList.get(i).getLatitude())){
                    temp =getDistance(car_longitude,car_latitude,posSiteArrayList.get(i).getLongitude(),posSiteArrayList.get(i).getLatitude());
                    tempNumber = i;
                }
            }
        }
        posSites.add(posSiteArrayList.get(tempNumber));
        return posSites;
    }
    //???????????????????????????,10000???????????????null
    public Pos GetNearSmallPos(Double latitude){
        Pos posSmallSites;
        int tempNumber = 0;
        double temp=10000;
        for(int i = 0; i < posSmallArrayList.size(); i++) {
            if(latitude - 0.2 < posSmallArrayList.get(i).getLatitude() && posSmallArrayList.get(i).getLatitude() < latitude + 0.2) {
                if (temp > getDistance(car_longitude, car_latitude, posSmallArrayList.get(i).getLongitude(), posSmallArrayList.get(i).getLatitude())) {
                    temp = getDistance(car_longitude, car_latitude, posSmallArrayList.get(i).getLongitude(), posSmallArrayList.get(i).getLatitude());
                    tempNumber = i;
                }
            }
        }
        if(tempNumber == 0)
            posSmallSites = null;
        else
            posSmallSites = posSmallArrayList.get(tempNumber);
        return posSmallSites;
    }
    /*public PosSite GetNearPos(Double latitude){
        PosSite posSites = new PosSite();
        int tempNumber = 0;
        double temp=10000;
        for(int i = 0; i < posSiteArrayList.size(); i++) {
            if(latitude - 0.05 < posSiteArrayList.get(i).getLatitude() && posSiteArrayList.get(i).getLatitude() < latitude + 0.05)
            {
                if(temp>getDistance(car_longitude,car_latitude,posSiteArrayList.get(i).getLongitude(),posSiteArrayList.get(i).getLatitude())){
                    temp =getDistance(car_longitude,car_latitude,posSiteArrayList.get(i).getLongitude(),posSiteArrayList.get(i).getLatitude());
                    tempNumber = i;
                }
            }
        }
        posSites.setLatitude(posSiteArrayList.get(tempNumber).getLatitude());
        posSites.setLongitude(posSiteArrayList.get(tempNumber).getLongitude());
        posSites.setRadius(posSiteArrayList.get(tempNumber).getRadius());
        posSites.setVoice_name(posSiteArrayList.get(tempNumber).getVoice_name());
        return posSites;
    }
*/
    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        final double EARTH_RADIUS = 6378.137;
        // ??????
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        // ??????
        double lng1 = Math.toRadians(longitude1);
        double lng2 = Math.toRadians(longitude2);
        // ????????????
        double a = lat1 - lat2;
        // ????????????
        double b = lng1 - lng2;
        // ???????????????????????????
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        // ?????????????????????, ????????????: ??????
        s =  s * EARTH_RADIUS;
        return s;
    }
    /**
     * ??????asset??????????????????
     * @return content
     */
    public static String readFile(Context mContext, String file, String code)
    {
        int len = 0;
        byte []buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len  = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf,code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        wakeUpAsr.destroyWakeUpAsr();
        if( null != mAsrQuestion ){
            // ?????????????????????
            mAsrQuestion.cancel();
            mAsrQuestion.destroy();
        }
       /* pin21.close();
        pin3.close();
        pin17.close();
        pin18.close();*/
    }
}