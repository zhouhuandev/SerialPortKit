package com.serial.port.kit

import android.app.Application
import android.util.Log
import com.serial.port.kit.core.SerialPortFinder
import com.serial.port.manage.SerialPortKit
import com.serial.port.manage.SerialPortManager

/**
 *
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
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
            .path("/dev/ttyS0")
            .baudRate(115200)
            .maxSize(1024)
            .retryCount(2)
            .isShowToast(true)
            .debug(BuildConfig.DEBUG)
            .build()
            .get()
    }
}