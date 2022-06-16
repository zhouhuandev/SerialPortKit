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
- 支持自定义收发超时时长 
- 支持主线程/子线程
- 支持多线程并发通讯
- 支持自定义发送任务Task
- 支持指令池组装
- 支持指令工具
- 支持统一数据结果回调
- 支持自定义发送Task接收次数
- 支持统一配置发送Task接收次数

## 使用方法

### 接入方式

在 Project `build.gradle`中添加

```groovy
repositories {
    maven {
        name 'maven-snapshot'
        url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
    }
}
```

在 Android Studio Chipmunk 时 改为 settings.gradle 中添加：

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name 'maven-snapshot'
            url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
        }
    }
}

在 app `build.gradle`中添加

```groovy
def serialPortVersion = "1.0.4-SNAPSHOT"

implementation "io.github.zhouhuandev:serial-port-kit-manage:$serialPortVersion" // require kotlin 1.7.0

// 需要使用数据转换工具或串口搜索或完全自定义数据输入输出的开发者可使用 serial-port-kit-core
implementation "io.github.zhouhuandev:serial-port-kit-core:$serialPortVersion" // 可选
```

如果在 build 过程中爆错 `resource android:attr/lStar not found.`

```shell
.gradle/caches/transforms-2/files-2.1/3c80c501edca1d8bdce41f94be0c4104/core-1.7.0/res/values/values.xml:105:5-114:25: AAPT: error: resource android:attr/lStar not found.
```

是因为您当前项目的Kotlin版本低于1.7.0导致，需要强制替换统一版本(替换成你的版本即可)

```groovy
configurations.all {
    resolutionStrategy {
        force 'androidx.core:core-ktx:1.6.0'
    }
}
```

### 快速初始化

为了隔离第三方SDK，下面提供代理方式的初始化方案，只有使用的时候才会进行初始化

#### SerialPortProxy代理类

```kotlin
class SerialPortProxy {

    companion object {
        private const val TAG = "SerialPortProxy"
    }

    private lateinit var serialPortManager: SerialPortManager

    private var isInitSuccess = false

    private val isInit
        get() = isInitSuccess

    private fun initSdk() {
        searchAllDevices()

        serialPortManager = SerialPortKit.newBuilder(ContextProvider.appContext)
            // 设备地址
            .path("/dev/ttyS0")
            // 波特率
            .baudRate(115200)
            // Byte数组最大接收内存
            .maxSize(1024)
            // 发送失败重试次数
            .retryCount(2)
            // 发送一次指令，最多接收几次设备发送的数据，局部接收次数优先级高
            .receiveMaxCount(1)
            // 是否按照 maxSize 内存进行接收
            .isReceiveMaxSize(false)
            // 是否显示吐司
            .isShowToast(true)
            // 是否Debug模式，Debug模式会输出Log
            .debug(BuildConfig.DEBUG)
            // 是否自定义校验下位机发送的数据正确性，把校验好的Byte数组装入WrapReceiverData
            .isCustom(true, DataConvertUtil.customProtocol())
            // 校验发送指令与接收指令的地址位，相同则为一次正常的通讯
            .addressCheckCall(DataConvertUtil.addressCheckCall())
            .build()
            .get()

        isInitSuccess = true
    }

    private fun reInitSdk() {
        if (!isInit) {
            initSdk()
        }
    }

    fun searchAllDevices() {
        try {
            val serialPortFinder = SerialPortFinder()
            serialPortFinder.allDevices.forEach {
                Log.d(TAG, "搜索到的串口信息为: $it")
            }
        } catch (e: Exception) {
            Log.d(TAG, "initSerialPort: ", e)
        }
    }

    val portManager: SerialPortManager
        get() {
            reInitSdk()
            return serialPortManager
        }
}
```

#### SerialPortHelper

当然，光代理指定是不可以的，肯定还需要一个助手呀，助手这就来了，核心就是为了解决部分不需要的一些回调，进行充分的解耦。让发送指令单单就是发送指令，而让业务只专注它需要的数据回调。

```kotlin
object SerialPortHelper {
    private val mProxy = SerialPortProxy()

    /**
     * 暴露SDK
     */
    val portManager: SerialPortManager
        get() = mProxy.portManager

    /**
     * 内部使用，默认开启串口
     */
    private val serialPortManager: SerialPortManager
        get() {
            // 默认开启串口
            if (!mProxy.portManager.isOpenDevice) {
                mProxy.portManager.open()
            }
            return portManager
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
if (!SerialPortHelper.portManager.isOpenDevice) {
    val open = SerialPortHelper.portManager.open()
    Log.d(TAG, "串口打开${if (open) "成功" else "失败"}")
}
```

### 关闭串口

```kotlin
val close = SerialPortHelper.portManager.close()
Log.d(TAG, "串口关闭${if (close) "成功" else "失败"}")
```

### WrapSendData 自定义收发超时时长

默认发送超时时长为 3000ms，等待超时时长为 300ms，自定义最大接收次数 0

注：自定义最大接收次数比全局配置最大接收次数优先级高。当自定义最大接收次数为 0 代表默认使用全局最大接收次数

```kotlin
data class WrapSendData
@JvmOverloads constructor(
    var sendData: ByteArray,
    var sendOutTime: Int = 3000,
    var waitOutTime: Int = 300,
    @IntRange(from = 0, to = 3)
    var receiveMaxCount: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WrapSendData

        if (!sendData.contentEquals(other.sendData)) return false
        if (sendOutTime != other.sendOutTime) return false
        if (waitOutTime != other.waitOutTime) return false
        if (receiveMaxCount != other.receiveMaxCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sendData.contentHashCode()
        result = 31 * result + sendOutTime
        result = 31 * result + waitOutTime
        result = 31 * result + receiveMaxCount
        return result
    }
}
```

### WrapSendData 发送数据

```kotlin
SerialPortHelper.portManager.send(WrapSendData(
    SenderManager.getSender().sendStartDetect()
),
    object : OnDataReceiverListener {
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
        return false
    }
}


```

发送Task

```kotlin
SerialPortHelper.portManager.send(SimpleSerialPortTask(WrapSendData(SenderManager.getSender().sendStartDetect()), object : OnDataReceiverListener {
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

```kotlin
val switchDevice = SerialPortHelper.portManager.switchDevice(path = "/dev/ttyS1")
Log.d(TAG, "串口切换${if (switchDevice) "成功" else "失败"}")
```

### 切换波特率

```kotlin
val switchDevice = SerialPortHelper.portManager.switchDevice(baudRate = 9600)
Log.d(TAG, "波特率切换${if (switchDevice) "成功" else "失败"}")
```

注：支持串口与波特率可以同时进行切换

### 统一监听数据接口

支持除任务以外的数据回调，增加监听。与发送 Task 任务收到的回调数据是互斥关系（剔除了 Task 回调的数据），优先级低于 Task 任务回调。

此处回调不参与校验地址位，但是仍然可选参与自定义指令规则。

```kotlin

    override fun onResume() {
        super.onResume()
        // 增加统一监听回调
        SerialPortHelper.portManager.addDataPickListener(onDataPickListener)
    }

    override fun onPause() {
        super.onPause()
        // 移除统一监听回调
        SerialPortHelper.portManager.removeDataPickListener(onDataPickListener)
    }

    private val onDataPickListener: OnDataPickListener = object : OnDataPickListener {
        override fun onSuccess(data: WrapReceiverData) {
            Log.d(TAG, "统一响应数据：${TypeConversion.bytes2HexString(data.data)}")
        }
    }

```

### 命令管理池

```kotlin
object SerialCommandProtocol : BaseProtocol() {
    // TODO: Something
}
```

### 发送指令实现

```kotlin
class AdapterSender : Sender {
    // TODO: Something
}
```

### 命令池版本管理发送器

```kotlin
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

```shell
Name : "zhouhuandev",
Blog : "https://blog.csdn.net/youxun1312"
```

## 日志

- 2022.03.01 开源发布
- 2022.03.10 增加统一数据监听回调
- 2022.03.20 修改示例Demo

## License

```shell
Copyright (C)  zhouhuan, SerialPortKit Framework Open Source Project

Licensed under the Apache License, Version 2.0 (the "License")
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
