package com.soulkun.binderbestpractice.app1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class InterprocessService extends Service {

    private static final String TAG = "服务端";

    private final List<Person> mPersonList = new ArrayList<>();

    private IAidlInterface.Stub mAidlInterfaceImpl = new IAidlInterface.Stub() {
        @Override
        public void addPerson(Person person) throws RemoteException {
            Log.e(TAG, "成功添加：" + person);
            mPersonList.add(person);
            Log.e(TAG, "当前列表：" + mPersonList);
        }

        @Override
        public List<Person> getPersonList() throws RemoteException {
            Log.e(TAG, "当前列表：" + mPersonList);
            return mPersonList;
        }

        @Override
        public void setInPerson(Person person) throws RemoteException {
            if (person != null) {
                Log.e(TAG, "客户端发来：" + person);
                person.setAge(person.getAge() + 1);
                Log.e(TAG, "服务端修改：" + person);
            } else {
                Log.e(TAG, "客户端发来Null值");
            }
        }

        @Override
        public void setOutPerson(Person person) throws RemoteException {
            if (person != null) {
                Log.e(TAG, "客户端发来：" + person);
                person.setAge(person.getAge() + 1);
                Log.e(TAG, "服务端修改：" + person);
            } else {
                Log.e(TAG, "客户端发来Null值");
            }
        }

        @Override
        public void setInOutPerson(Person person) throws RemoteException {
            if (person != null) {
                Log.e(TAG, "客户端发来：" + person);
                person.setAge(person.getAge() + 1);
                Log.e(TAG, "服务端修改：" + person);
            } else {
                Log.e(TAG, "客户端发来Null值");
            }
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return mAidlInterfaceImpl.asBinder();
    }

}