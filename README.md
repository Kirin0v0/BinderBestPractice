# Binder学习指南
目前已完成AIDL部分



## 一、AIDL全解

> 参考自：
>
> * [Android：学习AIDL，这一篇文章就够了(上)](https://www.jianshu.com/p/a8e43ad5d7d2)
> * [Android：学习AIDL，这一篇文章就够了(下)](https://www.jianshu.com/p/0cca211df63c)
> * [你真的理解AIDL中的in，out，inout么？](https://www.jianshu.com/p/ddbb40c7a251)

### 1.AIDL的概念

​	AIDL全称是Android Interface Definition Language，也就是Android接口定义语言，设计这门语言的目的是为了实现**进程间通信**，其实现基础是**Binder机制**，通过Binder机制和这门语言，**我们可以通过AIDL文件快速生成复杂但重复性高的Java代码，在一个进程中访问其他进程的数据，并调用接口中定义的方法**。

### 2.AIDL的语法

1. AIDL文件类型：

   * 实现`Parcelable`接口的数据类：

     ```java
     // Person.aidl
     package com.soulkun.binderbestpractice.app1;
     
     parcelable Person;
     ```

     > Java中实现实现`Parcelable`接口的数据类：
     >
     > ```java
     > package com.soulkun.binderbestpractice.app1;
     > 
     > import android.os.Parcel;
     > import android.os.Parcelable;
     > 
     > import androidx.annotation.NonNull;
     > 
     > public class Person implements Parcelable {
     > 
     >     private String name;
     >     private Integer age;
     > 
     >     // 注意，使用到out限定符时必须保留空构造器，同时空构造器中必须实现默认赋值防止空异常！！！
     >     public Person() {
     >         name = "";
     >         age = 0;
     >     }
     > 
     >     public Person(String name, Integer age) {
     >         this.name = name;
     >         this.age = age;
     >     }
     > 
     >     protected Person(Parcel in) {
     >         name = in.readString();
     >         if (in.readByte() == 0) {
     >             age = null;
     >         } else {
     >             age = in.readInt();
     >         }
     >     }
     > 
     >     public static final Creator<Person> CREATOR = new Creator<Person>() {
     >         @Override
     >         public Person createFromParcel(Parcel in) {
     >             return new Person(in);
     >         }
     > 
     >         @Override
     >         public Person[] newArray(int size) {
     >             return new Person[size];
     >         }
     >     };
     > 
     >     public String getName() {
     >         return name;
     >     }
     > 
     >     public void setName(String name) {
     >         this.name = name;
     >     }
     > 
     >     public Integer getAge() {
     >         return age;
     >     }
     > 
     >     public void setAge(Integer age) {
     >         this.age = age;
     >     }
     > 
     >     @Override
     >     public int describeContents() {
     >         return 0;
     >     }
     > 
     >     @Override
     >     public void writeToParcel(@NonNull Parcel dest, int flags) {
     >         dest.writeString(name);
     >         if (age == null) {
     >             dest.writeByte((byte) 0);
     >         } else {
     >             dest.writeByte((byte) 1);
     >             dest.writeInt(age);
     >         }
     >     }
     > 
     >     // 注意，使用到out和inout限定符时必须手动添加该方法！！！该方法不在Parcelable接口中，但代码逻辑与Person(Parcel in)相同！！！
     >     public void readFromParcel(Parcel in) {
     >         name = in.readString();
     >         if (in.readByte() == 0) {
     >             age = null;
     >         } else {
     >             age = in.readInt();
     >         }
     >     }
     > 
     >     @Override
     >     public String toString() {
     >         return "Person{" +
     >                 "name='" + name + '\'' +
     >                 ", age=" + age +
     >                 '}';
     >     }
     > 
     > }
     > ```

   * 接口类：

     ```java
     // IAidlInterface.aidl
     package com.soulkun.binderbestpractice.app1;
     
     import com.soulkun.binderbestpractice.app1.Person;
     
     interface IAidlInterface {
         void addPerson(in Person person);
         List<Person> getPersonList();
         void setInPerson(in Person person);
         void setOutPerson(out Person person);
         void setInOutPerson(inout Person person);
     }
     ```

     ​	注意，根据后续源码分析可知方法名是用来判断Binder操作，因此**无论方法参数是否相同都不可以重名**！！！

2. AIDL定向tag：

   ​	**AIDL中的定向 tag 表示了在跨进程通信中数据的流向**。

   * **in**：

     ​	**表示数据只能由客户端流向服务端**，服务端将接收客户端调用接口方法传入的对象的完整数据，但客户端的对象不会因为服务端对其的修改而发生变化。

   * **out**：

     ​	**表示数据只能由服务端流向客户端**，服务端将接收到**空构造器**构造的传入参数，但在服务端对接收到的对象修改后客户端中的传入参数将同步变化为修改后的对象。

     ​	注意，根据后续源码分析可知**使用该tag必须实现空构造器**。

     ```java
     // 空构造器中必须实现默认赋值防止空异常！！！
     public Person() {
         name = "";
         age = 0;
     }
     ```

   * **inout**：

     ​	**表示数据可在服务端与客户端之间双向流通**，服务端将接收到客户端传入的参数对象，同时客户端传入的参数在服务端修改传参对象后同步发生变化。

​		注意，根据后续源码分析可知，**当使用out或inout限定符时，必须在自定义数据类中实现`void readFromParcel(Parcel parcel)`方法**。

```java
// 该方法不在Parcelable接口中，但代码逻辑与Person(Parcel in)相同！！！
public void readFromParcel(Parcel in) {
    name = in.readString();
    if (in.readByte() == 0) {
        age = null;
    } else {
        age = in.readInt();
    }
}
```

### 3.AIDL的使用步骤

1. 使数据类实现`Parcelable`接口，并**符合对应AIDL定向tag的要求**；

2. 先在一方AIDL文件夹中书写正确的AIDL文件，**AIDL数据类需要与Java数据类在同包名下**；

   > 实现数据类同包名的两种方式：
   >
   > * Java文件在包含该AIDL的AIDL文件夹内：
   >
   >   ​	**需要修改build.gradle文件，在 android{} 中间加上下面的内容**
   >
   >   ```java
   >   sourceSets {
   >       main {
   >           java.srcDirs = ['src/main/java', 'src/main/aidl']
   >       }
   >   }
   >   ```
   >
   > * Java文件在与该AIDL同包名的Java文件夹内：
   >
   >   ​	**在移植时需要同时复制Java文件和Java文件夹**

3. 移植AIDL及其相关文件，另一方的AIDL文件和包名保持不变，同时还要移植Java数据类，包名仍然不变；

4. 编写服务端Service代码，Service的exported需要为true，在实现`public IBinder onBind(Intent intent)`方法时注意**返回`AIDL接口.Stub#asBinder()`方法返回值**；

   ```java
   private IAidlInterface.Stub mAidlInterfaceImpl = new IAidlInterface.Stub() {
       @Override
       public void addPerson(Person person) throws RemoteException {
       }
   
       @Override
       public List<Person> getPersonList() throws RemoteException {
       }
   
       @Override
       public void setInPerson(Person person) throws RemoteException {
       }
   
       @Override
       public void setOutPerson(Person person) throws RemoteException {
       }
   
       @Override
       public void setInOutPerson(Person person) throws RemoteException {
       }
   
   };
   
   // 创建Stub对象后返回asBinder()方法的返回值
   @Override
   public IBinder onBind(Intent intent) {
       return mAidlInterfaceImpl.asBinder();
   }
   ```

5. 编写客户端代码，与服务端Service建立通信，建立通信成功时注意**将IBinder对象通过`AIDL接口.Stub.asInterface(IBinder iBinder)`方法转为AIDL接口**，最后方可使用AIDL接口对象定义好的方法。

   ```java
   private class InterprocessServiceConnection implements ServiceConnection {
       @Override
       public void onServiceConnected(ComponentName name, IBinder service) {
           // 转为AIDL接口对象
           mAidlInterfaceImpl = IAidlInterface.Stub.asInterface(service);
       }
   
       @Override
       public void onServiceDisconnected(ComponentName name) {
           mAidlInterfaceImpl = null;
       }
   }
   ```

### 4.AIDL的重点源码分析

> 以下分析皆以IAidlInterface.java文件为例。

​	AIDL重点分为AIDL接口文件的编译生成、AIDL转换接口对象、AIDL进程内/进程间通信和AIDL定向tag分析这四个部分。

* **AIDL接口文件的编译生成**：

  ​	AIDL接口文件在编译后会自动生成AIDL接口Java文件，忽略与AIDL无关信息后的UML类图如下所示。

  ![AIDL接口文件类图 ](https://raw.githubusercontent.com/soulkun926/typora-images/main/images/AIDL%E6%8E%A5%E5%8F%A3%E6%96%87%E4%BB%B6%E7%B1%BB%E5%9B%BE%20.png)

  ​	`IAidlInterface`接口方法为AIDL接口文件定义的方法，`IAidlInterface.Stub`类继承`Binder`类并实现`IAidlInterface`接口方法，其中接口方法由自身调用Binder类实现，而`IAidlInterface.Stub.Proxy`类仅实现`IAidlInterface`接口方法，但使用代理模式的思想使用`Binder`成员实现接口方法。

* **AIDL转换接口对象**：

  ​	在客户端使用AIDL接口的过程中最关键的一处是`AIDL接口类 接口对象 = AIDL接口类.Stub.asInterface(iBinder)`，该方法将IBinder对象转为接口对象。

  ```java
  public static com.soulkun.binderbestpractice.app1.IAidlInterface asInterface(android.os.IBinder obj)
  {
      if ((obj==null)) {
          return null;
      }
      // 判断AIDL接口是否为本地接口
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.soulkun.binderbestpractice.app1.IAidlInterface))) {  // 是则强制转换为接口对象
          return ((com.soulkun.binderbestpractice.app1.IAidlInterface)iin);
      }
      // 否则返回包含IBinder并实现接口类的代理类对象
      return new com.soulkun.binderbestpractice.app1.IAidlInterface.Stub.Proxy(obj);
  }
  ```

  ​	根据源码可知，在该方法中会判断**AIDL接口是否为本地接口，即是否处于同一进程内**。若是则强制转换为接口对象，接口方法逻辑则为服务端`public IBinder onBind(Intent intent)`方法返回的`IAidlInterface.Stub`类对象中定义好的逻辑；否则接口对象则返回`IAidlInterface.Stub.Proxy`代理类对象，其中接口方法逻辑大体为客户端进程通过代理实例远程传输接口方法及方法参数到服务端进程提供的Binder上，在判断方法操作后执行服务端Binder对应的接口方法，具体逻辑可查看后续分析。

  > 为什么客户端和服务端AIDL接口文件的包名要完全一致？
  >
  > 因为在查询本地接口时要通过文件描述符即完全类名（包名+类名）判断，不一致则永远查询不到接口对象！

* **AIDL进程内/进程间通信**：

  ​	由AIDL转换接口对象分析可知，AIDL通过不同的接口对象实现了进程内通信和进程间通信。

  > 以`mAidlInterfaceImpl.addPerson(person)`方法调用为例。

  * 进程内通信：

    ​	由于进程内实例可共享，客户端可直接获取到服务端提供的IBinder对象，所以在转换接口对象中方法传入的IBinder对象是服务端返回的`IAidlInterface.Stub`实例，可被直接强制转换为`IAidlInterface`对象。

    ​	客户端调用接口方法本质为调用在服务端中重写的`IAidlInterface.Stub#addPerson(Person person)`方法。

    ```java
    @Override
    public void addPerson(Person person) throws RemoteException {
        Log.e(TAG, "成功添加：" + person);
        mPersonList.add(person);
        Log.e(TAG, "当前列表：" + mPersonList);
    }
    ```

  * 进程间通信：

    ​	由于进程间实例不可共享，客户端无法直接获取到服务端提供的IBinder对象，因此在转换接口对象中方法传入的IBinder对象是实现`IBinder`类的`BinderProxy`代理对象，采取了通过`IAidlInterface.Stub.Proxy`代理类的方式代理客户端的接口方法。

    ```java
    @Override public void addPerson(com.soulkun.binderbestpractice.app1.Person person) throws android.os.RemoteException
    {
        // 定义写入和读出的Parcel
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
            // 写入Parcel中写入文件描述符
            _data.writeInterfaceToken(DESCRIPTOR);
            // 将数据写入到写入Parcel中
            if ((person!=null)) {
                _data.writeInt(1);
                person.writeToParcel(_data, 0);
            }
            else {
                _data.writeInt(0);
            }
            // 这里是跨进程通信的核心方法
            boolean _status = mRemote.transact(Stub.TRANSACTION_addPerson, _data, _reply, 0);
            if (!_status && getDefaultImpl() != null) {
                getDefaultImpl().addPerson(person);
                return;
            }
            _reply.readException();
        }
        finally {
            // Parcel回收
            _reply.recycle();
            _data.recycle();
        }
    }
    ```

    ​	在`IAidlInterface.Stub.Proxy`代理类实现的接口方法中可以看到两点，一点是跨进程通信的传入和传出（即方法参数和方法返回）是通过`Parcel`类实现的，另一点则是接口方法的最核心逻辑是`BinderProxy#transact(Stub.TRANSACTION_addPerson, _data, _reply, 0)`。

    ```java
    /**
     * 代理实现IBinder核心transact方法
     * code 执行的动作，即为执行接口的哪个方法
     * data 写入Parcel，即为方法参数
     * reply 读出Parcel，即为方法返回值
     * flag 额外标志，0表示普通RPC，需要等待服务端返回，会阻塞，FLAG_ONEWAY(1)表示单向RPC，不需要等待服务端返回，不会阻塞
     */
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Binder.checkParcel(this, code, data, "Unreasonably large binder buffer");
        ……
    	// 本地底层方法调用
        return transactNative(code, data, reply, flags);
    }
    ```

    ​	在调用`BinderProxy#transact`方法的过程中可知`IAidlInterface.Stub.Proxy`代理类调用方法基本是普通RPC即需要等待服务端返回，此外该方法核心方法为`BinderProxy#transactNative(code, data, reply, flags)`，由于涉及底层不方便展开，这里只描述过程，即为客户端进程调用`BinderProxy#transactNative`方法访问Binder驱动，驱动最终会获取服务端进程提供的`IBinder`对象并回调其`Binder#execTransact`方法，下一步就来到了`Binder#execTransact`方法。

    ```java
    @UnsupportedAppUsage
    private boolean execTransact(int code, long dataObj, long replyObj,
                                 int flags) {
        final int callingUid = Binder.getCallingUid();
        final long origWorkSource = ThreadLocalWorkSource.setUid(callingUid);
        try {
            // 核心方法
            return execTransactInternal(code, dataObj, replyObj, flags, callingUid);
        } finally {
            ThreadLocalWorkSource.restore(origWorkSource);
        }
    }
    ```

    ​	分析源码可知，该方法直接返回`Binder#execTransactInternal(code, dataObj, replyObj, flags, callingUid)`方法的返回值。

    ```java
    private boolean execTransactInternal(int code, long dataObj, long replyObj, int flags,
                                         int callingUid) {
        ……
    	boolean res;
    	……
        // 无论是哪个分支都会走onTransact(code, data, reply, flags)
        if ((flags & FLAG_COLLECT_NOTED_APP_OPS) != 0) {
            ……
    		res = onTransact(code, data, reply, flags);
            ……
        } else {
            res = onTransact(code, data, reply, flags);
        }
        ……
    	return res;
    }
    ```

    ​	接着我们再简化源码可以直接看出，该方法一定会返回`Binder#onTransact(code, data, reply, flags)`，又因为当前`Binder`类的实际类型是服务端提供的`IAidlInterface.Stub`类，那么在方法重写机制下实际的核心方法即为`IAidlInterface.Stub#onTransact(code, data, reply, flags)`。

    ```java
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
        java.lang.String descriptor = DESCRIPTOR;
        switch (code)
        {
                ……
                case TRANSACTION_addPerson:  // 通过code直接走对应方法逻辑
                {
                    data.enforceInterface(descriptor);
                    com.soulkun.binderbestpractice.app1.Person _arg0;
                    // 从写入Parcel中重建传入参数
                    if ((0!=data.readInt())) {
                        _arg0 = com.soulkun.binderbestpractice.app1.Person.CREATOR.createFromParcel(data);
                    }
                    else {
                        _arg0 = null;
                    }
                    // 这里就调用服务端重写的接口方法了
                    this.addPerson(_arg0);
                    reply.writeNoException();
                    return true;
                }
                ……
                default:
                {
                    return super.onTransact(code, data, reply, flags);
                }
        }
    }
    ```

    ​	接下来就不必多说了，直接跳到之前服务端重写的对应方法逻辑上。

    ```java
    @Override
    public void addPerson(Person person) throws RemoteException {
        Log.e(TAG, "成功添加：" + person);
        mPersonList.add(person);
        Log.e(TAG, "当前列表：" + mPersonList);
    }
    ```

    总结下进程间通信的整体流程，从客户端调用接口方法开始，由于接口对象实际上是`IAidlInterface.Stub.Proxy`代理类对象，接口方法中会将写入和读出数据通过`Parcel`传递，并借助代理`IBinder`对象调用到`BinderProxy#transact(Stub.TRANSACTION_addPerson, _data, _reply, 0)`，在该方法中会直接接触到本地底层方法`transactNative(code, data, reply, flags)`，本地底层方法会通过Binder驱动回调到服务端提供的`Binder`对象上执行`Binder#execTransact(int code, long dataObj, long replyObj, int flags)`方法，最终会走到`IAidlInterface.Stub#onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags)`方法上，在该方法中会通过`Parcel`重建写入数据和更新读出数据，最终执行到服务端重写的对应接口方法的逻辑上。

* **AIDL定向tag分析**：

  ​	定向tag分为in、out和inout三类，默认情况下为in限定符，将从`Parcel`的写入和读出这个方面对其源码分析。

  * **in**：表示数据只能由客户端流向服务端。

    > in的分析以调用`IAidlInterface#setInPerson(Person person)`为例。

    * 客户端`IAidlInterface.Stub.Proxy#setInPerson(Person person)`：

      ```java
      @Override public void setInPerson(com.soulkun.binderbestpractice.app1.Person person) throws android.os.RemoteException
      {
          android.os.Parcel _data = android.os.Parcel.obtain();
          android.os.Parcel _reply = android.os.Parcel.obtain();
          try {
              _data.writeInterfaceToken(DESCRIPTOR);
              // in限定符下会将传入参数写进写入Parcel数据中
              if ((person!=null)) {
                  _data.writeInt(1);
                  person.writeToParcel(_data, 0);
              }
              else {
                  _data.writeInt(0);
              }
              boolean _status = mRemote.transact(Stub.TRANSACTION_setInPerson, _data, _reply, 0);
              if (!_status && getDefaultImpl() != null) {
                  getDefaultImpl().setInPerson(person);
                  return;
              }
              // in限定符下不会将读出Parcel数据重新读到传入参数中
              _reply.readException();
          }
          finally {
              _reply.recycle();
              _data.recycle();
          }
      }
      ```

      ​	客户端接口方法中in限定符下只会将传入参数写进写入Parcel数据中。

    * 服务端`IAidlInterface.Stub#onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags)`：

      ```java
      @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
      {
          java.lang.String descriptor = DESCRIPTOR;
          switch (code)
          {
                  ……
                  case TRANSACTION_setInPerson:
                  {
                      data.enforceInterface(descriptor);
                      com.soulkun.binderbestpractice.app1.Person _arg0;
                      // in限定符下会将写入Parcel数据重建为传入参数
                      if ((0!=data.readInt())) {
                          _arg0 = com.soulkun.binderbestpractice.app1.Person.CREATOR.createFromParcel(data);
                      }
                      else {
                          _arg0 = null;
                      }
                      this.setInPerson(_arg0);
                      // in限定符下不会将新的传入参数重新写入读出Parcel数据中
                      reply.writeNoException();
                      return true;
                  }
                  ……
          }
      }
      ```

      ​	服务端接口方法中in限定符下只会将写入Parcel数据重建为传入参数。

  * **out**：表示数据只能由服务端流向客户端。

    > in的分析以调用`IAidlInterface#setOutPerson(Person person)`为例。

    * 客户端`IAidlInterface.Stub.Proxy#setOutPerson(Person person)`：

      ```java
      @Override public void setOutPerson(com.soulkun.binderbestpractice.app1.Person person) throws android.os.RemoteException
      {
          android.os.Parcel _data = android.os.Parcel.obtain();
          android.os.Parcel _reply = android.os.Parcel.obtain();
          try {
              // out限定符下不会将传入参数写进写入Parcel数据中
              _data.writeInterfaceToken(DESCRIPTOR);
              boolean _status = mRemote.transact(Stub.TRANSACTION_setOutPerson, _data, _reply, 0);
              if (!_status && getDefaultImpl() != null) {
                  getDefaultImpl().setOutPerson(person);
                  return;
              }
              _reply.readException();
              // out限定符下会将读出Parcel数据重新读到传入参数中
              if ((0!=_reply.readInt())) {
                  person.readFromParcel(_reply);
              }
          }
          finally {
              _reply.recycle();
              _data.recycle();
          }
      }
      ```

      ​	客户端接口方法中out限定符下会将读出Parcel数据重新读到传入参数中。

    * 服务端`IAidlInterface.Stub#onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags)`：

      ```java
      @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
      {
          java.lang.String descriptor = DESCRIPTOR;
          switch (code)
          {
                  ……
                  case TRANSACTION_setOutPerson:
                  {
                      data.enforceInterface(descriptor);
                      com.soulkun.binderbestpractice.app1.Person _arg0;
                      // out限定符下不会将写入Parcel数据重建为传入参数，而是用空构造器构造传入参数
                      _arg0 = new com.soulkun.binderbestpractice.app1.Person();
                      this.setOutPerson(_arg0);
                      reply.writeNoException();
                      // out限定符下会将新的传入参数重新写入读出Parcel数据中
                      if ((_arg0!=null)) {
                          reply.writeInt(1);
                          _arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                      }
                      else {
                          reply.writeInt(0);
                      }
                      return true;
                  }
                  ……
          }
      }
      ```

      ​	服务端接口方法中out限定符下会用空构造器构造传入参数，并将新的传入参数重新写入读出Parcel数据中。

  * **inout**：表示数据可在服务端与客户端之间双向流通。

    > in的分析以调用`IAidlInterface#setInOutPerson(Person person)`为例。

    * 客户端`IAidlInterface.Stub.Proxy#setInOutPerson(Person person)`：

      ```java
      @Override public void setInOutPerson(com.soulkun.binderbestpractice.app1.Person person) throws android.os.RemoteException
      {
          android.os.Parcel _data = android.os.Parcel.obtain();
          android.os.Parcel _reply = android.os.Parcel.obtain();
          try {
              _data.writeInterfaceToken(DESCRIPTOR);
              // inout限定符下会将传入参数写进写入Parcel数据中
              if ((person!=null)) {
                  _data.writeInt(1);
                  person.writeToParcel(_data, 0);
              }
              else {
                  _data.writeInt(0);
              }
              boolean _status = mRemote.transact(Stub.TRANSACTION_setInOutPerson, _data, _reply, 0);
              if (!_status && getDefaultImpl() != null) {
                  getDefaultImpl().setInOutPerson(person);
                  return;
              }
              _reply.readException();
              // inout限定符下会将读出Parcel数据重新读到传入参数中
              if ((0!=_reply.readInt())) {
                  person.readFromParcel(_reply);
              }
          }
          finally {
              _reply.recycle();
              _data.recycle();
          }
      }
      ```

      ​	客户端接口方法中inout限定符下会将传入参数写进写入Parcel数据中，而且还会将读出Parcel数据重新读到传入参数中。

    * 服务端`IAidlInterface.Stub#onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags)`：

      ```java
      @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
      {
          java.lang.String descriptor = DESCRIPTOR;
          switch (code)
          {
                  ……
      			case TRANSACTION_setInOutPerson:
                  {
                      data.enforceInterface(descriptor);
                      com.soulkun.binderbestpractice.app1.Person _arg0;
                      // inout限定符下会将写入Parcel数据重建为传入参数
                      if ((0!=data.readInt())) {
                          _arg0 = com.soulkun.binderbestpractice.app1.Person.CREATOR.createFromParcel(data);
                      }
                      else {
                          _arg0 = null;
                      }
                      this.setInOutPerson(_arg0);
                      reply.writeNoException();
                      // inout限定符下会将新的传入参数重新写入读出Parcel数据中
                      if ((_arg0!=null)) {
                          reply.writeInt(1);
                          _arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                      }
                      else {
                          reply.writeInt(0);
                      }
                      return true;
                  }
                  ……
          }
      }
      ```

      ​	服务端接口方法中inout限定符下会将写入Parcel数据重建为传入参数，而且还会将新的传入参数重新写入读出Parcel数据中。

### 5.AIDL的跨进程整体流程分析

![AIDL跨进程整体流程图](https://raw.githubusercontent.com/soulkun926/typora-images/main/images/AIDL%E8%B7%A8%E8%BF%9B%E7%A8%8B%E6%95%B4%E4%BD%93%E6%B5%81%E7%A8%8B%E5%9B%BE.png)



