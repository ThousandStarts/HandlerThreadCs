package com.example.myhandlerthread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * 顺序1，2，6，3，4，5
 * 具体流程是这样的，我先创建了一个线程thread，一个在子线程进行的异步zxcHandler，一个用来修改UI的异步uiHandler
 * 启动了线程thread，然后创建异步zxcHandler，并把thread的looper给了异步zxcHandler，然后在创建一个模拟更新方法updata，
 * 在updata方法中调用uiHandler异步来修改TextView的值，
 * 然后重写了onResume方法，去调用了异步zxcHandler，异步zxcHandler的handleMessage方法中有个flag判断，用于是否循环去调用异步zxcHandler
 */
public class MainActivity extends AppCompatActivity {

    private TextView textView;

    //创建HandlerThread线程
    private HandlerThread thread;

    //创建在子线程中运行的异步消息
    private Handler zxcHandler;

    //创建在UI中运行的异步消息
    private Handler uiHandler=new Handler();

    //用于防止退出界面后线程还在运行
    private boolean flag;

    //自定义消息类型，在handler中handlerMessage中case对应
    private static final int MSG_UPDATE_INFO = 0x110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("aaaaaa","1");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.text_view);
        init();
    }

    //初始化调用线程
    public void init(){
        Log.e("aaaaaa","2");
        //aaaaa为线程名称
        thread=new HandlerThread("aaaaa");
        //启动线程
        thread.start();

        //创建子线程的异步消息
        //把HandlerThread线程的looper传递给了异步消息
        zxcHandler=new Handler(thread.getLooper()){
            public void handleMessage(Message message){
                Log.e("aaaaaa","3");
                updata();
                if(flag){
                    zxcHandler.sendEmptyMessage(MSG_UPDATE_INFO);
                }
            }
        };
    }

    //模拟更新
    public void updata(){
        Log.e("aaaaaa","4");
        try {
            Thread.sleep(2000);

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.e("aaaaaa","5");
                    String text="每隔2秒跟新数据：";
                    text+=Math.random();
                    textView.setText(text);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        Log.e("aaaaaa","6");
        super.onResume();
        //开始查询
        flag=true;
        //发送一个通知，通知后台轮询线程，0x110是一个随意的值
        zxcHandler.sendEmptyMessage(MSG_UPDATE_INFO);
    }

    @Override
    protected void onPause() {
        Log.e("aaaaaa","7");
        super.onPause();
        //停止查询
        flag = false;
        zxcHandler.sendEmptyMessage(MSG_UPDATE_INFO);
    }

    @Override
    protected void onDestroy() {
        Log.e("aaaaaa","8");
        super.onDestroy();
        //退出HandlerThread的looper循环，释放线程资源
        thread.quit();
    }
}
