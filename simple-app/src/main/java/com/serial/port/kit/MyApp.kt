package com.serial.port.kit

import android.app.Application
import android.util.Log
import com.serial.port.kit.core.SerialPortFinder
import com.serial.port.kit.manage.utils.DataConvertUtil
import com.serial.port.manage.SerialPortKit
import com.serial.port.manage.SerialPortManager

/**
 * App
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
class MyApp : Application() {

    companion object {
        private const val TAG = "MyApp"

        private lateinit var serialPortManager: SerialPortManager

        @JvmStatic
        val portManager: SerialPortManager
            get() {
                // 默认开启串口
                if (!serialPortManager.isOpenDevice) {
                    serialPortManager.open()
                }
                return serialPortManager
            }
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

        serialPortManager = SerialPortKit.newBuilder(this)
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
    }
}