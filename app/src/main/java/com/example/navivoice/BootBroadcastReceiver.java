package com.example.navivoice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.navivoice.activity.MainActivity;

/**
 * @Description 功能描述： 该类用来实现该app开机自动运行
 */
/*
public class BootBroadcastReceiver extends BroadcastReceiver {

    *//**
     * demo2: 可以实现开机自动打开软件并运行。
     *//*
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("XRGPS", "BootReceiver.onReceive: " + intent.getAction());
        //System.out.println("自启动程序即将执行");
        //MainActivity就是开机显示的界面
        Intent mBootIntent = new Intent(context, MainActivity.class);
        //下面这句话必须加上才能开机自动运行app的界面
        mBootIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mBootIntent);

    }
}*/
