package com.example.navivoice.util;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.navivoice.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
/**
 * 讯飞语音唤醒识别
 */
public class WakeUpAsr {
    String TAG = "wakeup";
    private final Context mContext;
    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    // 语音唤醒识别对象
    private final SpeechRecognizer mAsr;
    // 本地语法构建路径
    private final String grmPath;
    // 唤醒结果内容
    private String resultString;
    // 本地语法id
    private String mLocalGrammarID=null;
    // 本地语法文件
    private String mLocalGrammar = null;
    AudioManager mAudioManager;
    public WakeUpAsr(Context context,AudioManager mAudioManager,String grmPath) {
        this.mContext = context;
        this.mAudioManager = mAudioManager;
        this.grmPath = grmPath;
        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(mContext, null);
        // 初始化识别对象---唤醒+识别,用来构建语法
        mAsr = SpeechRecognizer.createRecognizer(mContext, null);

    }

    /**
     * 开启唤醒功能
     */
    public void startWakeUpAsr() {
        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            //唤醒的阈值，就相当于门限值，当用户输入的语音的置信度大于这一个值的时候，才被认定为成功唤醒。
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + 1450);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );

            // 启动唤醒
            mIvw.startListening(new MyWakeuperListener());
        }else
            Log.e("TAG", "startWakeuper:not ");


        mAsr.setParameter(SpeechConstant.PARAMS, null);
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // 设置引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        mAsr.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
        // 设置资源路径
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr1.wav");
        // 获取识别资源路径
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
    }

    /**
     * 获取唤醒词功能
     *
     * @return 返回文件位置
     */
    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + mContext.getString(R.string.app_id) + ".jet");
        return resPath;
    }

    /**
     * 销毁唤醒识别功能
     */
    public void destroyWakeUpAsr() {
        // 销毁合成对象
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
        }
        if( null != mAsr ){
            // 退出时释放连接
            mAsr.cancel();
            mAsr.destroy();
        }
    }

    /**
     * 唤醒词监听类
     */
    GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                mLocalGrammarID = grammarId;
                Log.e("gram",mLocalGrammarID);
            } else {
                Log.e("语法构建失败,错误码：" ,error.getErrorCode()+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e("初始化失败,错误码：",code+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };
    /**
     * 识别监听器。
     */
    private final RecognizerListener recognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.e("当前正在说话，音量大小：","1"+volume);
            Log.e(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result && !TextUtils.isEmpty(result.getResultString())) {
                Log.d(TAG, "recognizer result：" + result.getResultString());
                //recoString = JsonParser.parseGrammarResult(result.getResultString());
                /*int contact = JsonParser.parseGrammarResultcontact(result.getResultString());
                int callCmd = JsonParser.parseGrammarResultcallCmd(result.getResultString());
                int contactmin = JsonParser.parseGrammarResultcontactmin(result.getResultString());
                int callCmdmax = JsonParser.parseGrammarResultcallCmdmax(result.getResultString());
                Log.d(TAG, " "+contact);
                Log.d(TAG, " "+callCmd);
                if(contact>30)
                    //减少音量
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FX_FOCUS_NAVIGATION_UP);
                if(callCmd>30)
                    //增加电量
                    mAudioManager.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,AudioManager.FX_FOCUS_NAVIGATION_UP);
                if(contactmin>30)
                    //减少音量
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,AudioManager.FX_FOCUS_NAVIGATION_UP);
                if(callCmdmax>30)
                    //增加电量
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FX_FOCUS_NAVIGATION_UP);
                if(callCmd<=30&&contact<=30&&callCmdmax<=30&&contactmin<=30)
                    textView.setText("不好意思,没有听懂你的意思");*/
                Map<String, Integer> map = JsonParserUtil.parseGrammarResultScore(result.getResultString());
                Set<Map.Entry<String, Integer>> entries = map.entrySet();
                Log.e(TAG, entries.iterator().next().getKey());
                Log.e(TAG, String.valueOf(entries.iterator().next().getValue()));
                if(entries.iterator().next().getKey().equals("<callCmd>")&&entries.iterator().next().getValue()>30)
                    mAudioManager.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,AudioManager.FX_FOCUS_NAVIGATION_UP);
                else if(entries.iterator().next().getKey().equals("<contact>")&&entries.iterator().next().getValue()>30)
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FX_FOCUS_NAVIGATION_UP);
                else if(entries.iterator().next().getKey().equals("<callCmdmax>")&&entries.iterator().next().getValue()>30)
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FX_FOCUS_NAVIGATION_UP);
                else if(entries.iterator().next().getKey().equals("<contactmin>")&&entries.iterator().next().getValue()>30)
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,AudioManager.FX_FOCUS_NAVIGATION_UP);
                /*else if (entries.iterator().next().getValue()>30)
                    Log.e(TAG,entries.iterator().next().getKey());
                else
                    Log.e(TAG,"不好意思,没有听懂你的意思");*/
                //musicrestart();
                //为了m101c添加
                mIvw.startListening(new MyWakeuperListener());
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.e(TAG,"结束说话");
            //FucUtil.deleteDirWihtFile(file);
            //mediaPlayer.start(); // 开始播放

        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.e(TAG,"开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            //textView.setText("不好意思,没有听懂你的意思");
            //musicrestart();
            Log.e("onError Code：",""+ error.getErrorCode());
            //为了m101c添加
            mIvw.startListening(new MyWakeuperListener());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
        }

    };
    private class MyWakeuperListener implements WakeuperListener {
        //开始说话
        @Override
        public void onBeginOfSpeech() {

        }

        //错误码返回
        @Override
        public void onError(SpeechError arg0) {

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {

        }

        @Override
        public void onVolumeChanged(int i) {
            Log.d("dd", String.valueOf(i));
        }

        @Override
        public void onResult(WakeuperResult result) {
            //为了m101c添加
            mIvw.stopListening();

            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 ").append(text);
                buffer.append("\n");
                buffer.append("【操作类型】").append(object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】").append(object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】").append(object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】").append(object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】").append(object.optString("eos"));
                resultString =buffer.toString();
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
            //为了解决多次唤醒后，一次识别出现多次同一唤醒词
            if(mAsr!=null)
            {
                mAsr.stopListening();
            }
            Log.e(TAG, resultString);

            mLocalGrammar = readFile(mContext, "voiceadjust.bnf", "utf-8");
            mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "voiceadjust");
            mAsr.buildGrammar("bnf", mLocalGrammar, grammarListener);
            mAsr.startListening(recognizerListener);

        }
    }
    public void startIvmListening(){
        mIvw.startListening(new MyWakeuperListener());
    }
    public void stopIvmListening(){
        mIvw.stopListening();
    }
    /**
     * 读取asset目录下文件。
     * @return content
     */
    public static String readFile(Context mContext, String file, String code)
    {
        int len;
        byte []buf;
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
}
