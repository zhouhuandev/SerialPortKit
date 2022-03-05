# SerialPortKit
## Android串口通讯

### 介绍

🔥🔥🔥**SerialPortKit**是基于Android开发板进行与下位机进行通讯的工具套件SDK。串口通讯部分使用`C++`实现。**SerialPortKit**旨在帮助做Android开发板硬件开发的小伙伴们快速迭代开发，只关注业务。通常涉及到`RK3288`、`RK3399`等设备，**SerialPortKit**都能帮助到你。

如果我的付出可以换来对您的帮助的话，还请您点个start，将会是我不懈更新的动力，万分感谢。如果在使用中有任何问题，请留言

- 电子邮件：zhouhuandev@gmail.com

### 特点

- 支持自定义通讯协议
- 支持通讯地址校验
- 支持发送失败重试机制
- 支持一发一收，一发多收
- 支持多次接收指令
- 支持切换串口
- 支持切换波特率
- 支持指定接收最大数据长度
- 支持发送/接收超时检测
- 支持主线程/子线程
- 支持多线程并发通讯
- 支持自定义发送任务Task
- 支持指令池组装
- 支持指令工具

## 使用方法

### 接入方式

在Project `build.gradle`中添加

```kotlin
repositories {
    google()
    mavenCentral()
    maven {
        name 'maven-snapshot'
        url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
    }
}
```

在 app  `build.gradle`中添加

```kotlin
implementation 'io.github.zhouhuandev:serial-port-kit-core:1.0.1-SNAPSHOT' // require

implementation 'io.github.zhouhuandev:serial-port-kit-manage:1.0.1-SNAPSHOT'
```

### 初始化

老规矩，仍然是在Application中初始化。

```kotlin
class MyApp : Application() {

    companion object {
        private const val TAG = "MyApp"

        @JvmStatic
        var portManager: SerialPortManager? = null
    }

    override fun onCreate() {
        super.onCreate()
        initSerialPort()
    }

    private fun initSerialPort() {
        try {
            val serialPortFinder = SerialPortFinder()
            serialPortFinder.allDevices.forEach {
                Log.d(TAG, "搜索到的串口信息为: $it")
            }
        } catch (e: Exception) {
            Log.d(TAG, "initSerialPort: ", e)
        }

        portManager = SerialPortKit.newBuilder(this)
            // 设备地址
            .path("/dev/ttyS0")
            // 波特率
            .baudRate(115200)
            // Byte数组最大接收内存
            .maxSize(1024)
            // 发送失败重试次数
            .retryCount(2)
            // 发送一次指令，最多接收几次设备发送的数据
            .receiveMaxCount(1)
            // 是否按照 maxSize 内存进行接收
            .isReceiveMaxSize(false)
            // 是否显示吐司
            .isShowToast(true)
            // 是否Debug模式，Debug模式会输出Log
            .debug(BuildConfig.DEBUG)
            // 是否自定义校验下位机发送的数据正确性，把校验好的Byte数组装入WrapReceiverData
            .isCustom(true, object : OnDataCheckCall {
                override fun customCheck(
                    buffer: ByteArray,
                    size: Int,
                    onDataPickCall: (WrapReceiverData) -> Unit
                ): Boolean {
                    onDataPickCall.invoke(WrapReceiverData(buffer, size))
                    return true
                }
            })
            // 校验发送指令与接收指令的地址位，相同则为一次正常的通讯
            .addressCheckCall(object : OnAddressCheckCall {
                override fun checkAddress(
                    wrapSendData: WrapSendData,
                    wrapReceiverData: WrapReceiverData
                ): Boolean {
                    return wrapSendData.sendData[1] == wrapReceiverData.data[1]
                }
            })
            .build()
            .get()
    }
}
```

### maxSize & isReceiveMaxSize

maxSize是接收最大数据长度，默认为64，也可以设置isReceiveMaxSize，默认为false。若是isReceiveMaxSize设置为true，则接收数据长度按照maxSize最大长度进行返回。

**eg**. 通讯协议定义格式长度指定为16字节，然后设置maxSize为16，然后设置isReceiveMaxSize为true，则收到16字节以后才会返回命令。

温馨提示：isReceiveMaxSize主要是为了处理通讯过程中接收到的数据不完全的情况直到收到完整的命令而存在的，前提是通讯协议已规划好命令长度，一般不建议使用。

### 重试次数

通过retryCount设置发送失败以后重试次数，最大重试次数为3，支持0~3次进行重试发送。0次的时候不激活重试机制。

### 一对一&一对多收发指令

根据指定通讯协议来设置当前选项，根据往常项目中，有可能涉及到客户端发送一次指令，下位机会立即回复一次客户端发送的指令以代表建立了通讯。然后下位机进行处理任务，处理完任务以后会再次向客户端进行发送数据，这就涉及到发送一次指令，下位机回复两次数据的情况。对此，设计了一对多的收发指令。

**eg**. A给B打电话讲：“你好。我一会要去你家”（A给B发送指令），B接到电话会讲：“你好。稍等一会给你回电话。”（B收到了A发来的指令，我知道你要来了，我去收拾一下家务），B（处理好指令（家务））给A回电话：“你来吧！”（B告诉A处理结果）

这个过程A一共发送一次指令，B收到指令以后，立即告诉A让他稍等一会，自己处理完了再告诉A结果。B是给A发送了两次指令。

### 自定义校验指令

Android与下位机通讯，没有固定的通讯协议，都是根据各自项目进行指定符合要求的协议指定。只有符合要求的数据才可以进行返回处理，否则进行抛弃。

**eg**. 举例命令格式

```
命令格式
帧头 命令 数据长度 数据 校验
0xAA(1byte)    Cmd (1byte)    Len (1byte)    Data (Len byte)    ADD8校验和(1 byte)
帧头：数据帧开始字节，固定采用0xAA，长度为1字节。
命令：数据帧的命令字，详情见命令表，长度为1字节。
数据长度：数据区的字节数，代表有几个字节的数据。
数据：传输数据内容，长度为Len个字节。
校验：有很多种校验方式，有CRC，ADD8等。
```

根据命令格式进行解析，校验好数据以后通过 onDataPickCall.invoke(WrapReceiverData(buffer, size)) 把处理好的 Byte数组装载进入。注：return true或者return false，都可以，建议是返回 true。

### 校验地址位

制定串口通讯协议以后，不论是含有地址也好，还是命令（cmd）也好，为了组装好一次正常的配对通讯，一定要实现 addressCheckCall 当前接口，发送数据与收到数据都已暴露，执行取出地址位进行比较即可，返回 true 则是代表下位机返回的数据与发送数据是一对。否则继续等待下位机回来相匹配的数据。若是不实现该接口的话，则无论是否为当前发送指令的回复数据，都会回调至每个处于有效发送指令的接口。

### 开启串口

```kotlin
if (MyApp.portManager?.isOpenDevice == false) {
    val open = MyApp.portManager?.open() ?: false
    Log.d(TAG, "串口打开${if (open) "成功" else "失败"}")
}
```

### 关闭串口

```kotlin
val close = MyApp.portManager?.close() ?: false
Log.d(TAG, "串口关闭${if (close) "成功" else "失败"}")
```

### WrapSendData 发送数据

```kotlin
MyApp.portManager?.send(WrapSendData(byteArrayOf(0xAA.toByte(),0xA1.toByte(),0x00.toByte(), 0xB5.toByte())),
    object : OnDataReceiverListener {
        override fun onSuccess(data: WrapReceiverData) {
            Log.d(TAG, "响应数据：${TypeConversion.bytes2HexString(data.data)}")
        }

        override fun onFailed(wrapSendData: WrapSendData, msg: String) {
            Log.e(TAG,"发送数据: ${TypeConversion.bytes2HexString(wrapSendData.sendData)}, $msg")
        }

        override fun onTimeOut() {
            Log.e(TAG, "发送或者接收超时")
        }
    })
```

### 自定义Task 发送数据

每条指令的发送，在底层是以每个单独的Task执行发送，互不干扰。自定义Task继承父类 BaseSerialPortTask,同时可监控发送任务开始前做相应的操作，也可以监控发送任务完成后作相应的任务操作，于此同时，可以切换当前发送任务以及最终的 OnDataReceiverListener 监听回调是否执行在主线程。默认是在子线程中执行及回调。

自定义Task
```kotlin
class SimpleSerialPortTask(
    private val wrapSendData: WrapSendData,
    private val onDataReceiverListener: OnDataReceiverListener
) : BaseSerialPortTask() {
    override fun sendWrapData(): WrapSendData = wrapSendData

    override fun onDataReceiverListener(): OnDataReceiverListener = onDataReceiverListener

    override fun onTaskStart() {

    }

    override fun onTaskCompleted() {

    }

    override fun mainThread(): Boolean {
        return super.mainThread()
    }
}


```

发送Task

```kotlin
MyApp.portManager?.send(SimpleSerialPortTask(WrapSendData(SenderManager.getSender().sendStartDetect()), object : OnDataReceiverListener {
    override fun onSuccess(data: WrapReceiverData) {
        Log.d(TAG, "响应数据：${TypeConversion.bytes2HexString(data.data)}")
    }

    override fun onFailed(wrapSendData: WrapSendData, msg: String) {
        Log.e(
            TAG,
            "发送数据: ${TypeConversion.bytes2HexString(wrapSendData.sendData)}, $msg"
        )
    }

    override fun onTimeOut() {
        Log.e(TAG, "发送或者接收超时")
    }
}))
```

### 切换串口

```
val switchDevice = MyApp.portManager?.switchDevice(path = "/dev/ttyS1") ?: false
Log.d(TAG, "串口切换${if (switchDevice) "成功" else "失败"}")
```

### 切换波特率

```
val switchDevice = MyApp.portManager?.switchDevice(baudRate = 9600) ?: false
Log.d(TAG, "波特率切换${if (switchDevice) "成功" else "失败"}")
```

注：支持串口与波特率可以同时进行切换

### 命令管理池

```
object SerialCommandProtocol : BaseProtocol() {

}
```

### 发送指令实现

```
class AdapterSender : Sender {

}
```

### 命令池版本管理发送器

```
object SenderManager {
    private var senderMap = hashMapOf<Int, Sender>()

    /**
     * 获取发送者
     */
    @JvmOverloads
    fun getSender(type: Int = 0): Sender {
        if (senderMap[type] == null) {
            senderMap[type] = createSender(type)
        }
        return senderMap[type]!!
    }

    /**
     * 创建发送者
     *
     * @param type
     * @return
     */
    private fun createSender(type: Int): Sender {
        return when (type) {
            0, 1 -> AdapterSender()
            else -> AdapterSender()
        }
    }
}
```

## 问题反馈

欢迎 Start ，打call https://github.com/zhouhuandev/SerialPortKit 在使用中有任何问题，请留言

邮箱：zhouhuandev@gmail.com

## 关于作者

```
Name : "zhouhuandev",
Blog : "https://blog.csdn.net/youxun1312"
```

## 日志

- 2022.03.01 开源发布

## License

```
Copyright (C)  zhouhuan, SerialPortKit Framework Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```