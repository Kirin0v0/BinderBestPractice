package com.soulkun.binderbestpractice.app1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.soulkun.binderbestpractice.app1.databinding.ActivityInterprocessBinding;

import java.util.Random;

public class InterprocessActivity extends AppCompatActivity {

    private static final String TAG = "客户端";

    private IAidlInterface mAidlInterfaceImpl;

    private ActivityInterprocessBinding mInterprocessBinding;

    private final InterprocessServiceConnection mServiceConnection = new InterprocessServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInterprocessBinding = ActivityInterprocessBinding.inflate(getLayoutInflater());
        setContentView(mInterprocessBinding.getRoot());
        mInterprocessBinding.btn1.setOnClickListener(v -> {
            if (mAidlInterfaceImpl != null) {
                final Person person = new Person("用户" + new Random().nextInt(10000), new Random().nextInt(100));
                Log.e(TAG, "开始添加：" + person);
                try {
                    mAidlInterfaceImpl.addPerson(person);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mInterprocessBinding.btn2.setOnClickListener(v -> {
            if (mAidlInterfaceImpl != null) {
                try {
                    Log.e(TAG, "获取列表：" + mAidlInterfaceImpl.getPersonList());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mInterprocessBinding.btn3.setOnClickListener(v -> {
            if (mAidlInterfaceImpl != null) {
                final Person person = new Person("用户" + new Random().nextInt(10000), new Random().nextInt(100));
                Log.e(TAG, "客户端发送：" + person);
                try {
                    mAidlInterfaceImpl.setInPerson(person);
                    Log.e(TAG, "客户端查看：" + person);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mInterprocessBinding.btn4.setOnClickListener(v -> {
            if (mAidlInterfaceImpl != null) {
                final Person person = new Person("用户" + new Random().nextInt(10000), new Random().nextInt(100));
                Log.e(TAG, "客户端发送：" + person);
                try {
                    mAidlInterfaceImpl.setOutPerson(person);
                    Log.e(TAG, "客户端查看：" + person);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mInterprocessBinding.btn5.setOnClickListener(v -> {
            if (mAidlInterfaceImpl != null) {
                final Person person = new Person("用户" + new Random().nextInt(10000), new Random().nextInt(100));
                Log.e(TAG, "客户端发送：" + person);
                try {
                    mAidlInterfaceImpl.setInOutPerson(person);
                    Log.e(TAG, "客户端查看：" + person);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, InterprocessService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(mServiceConnection);
        super.onStop();
    }

    private class InterprocessServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "成功建立连接");
            mAidlInterfaceImpl = IAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAidlInterfaceImpl = null;
        }
    }

}