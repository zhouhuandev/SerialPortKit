package com.serial.port.kit

import android.app.Application
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
        @JvmStatic
        var manager: SerialPortManager? = null
    }


    override fun onCreate() {
        super.onCreate()

        manager = SerialPortKit.newBuilder(this)
            .path("/dev/ttyS0")
            .baudRate(115200)
            .retryCount(2)
            .isShowToast(true)
            .debug(BuildConfig.DEBUG)
            .build()
            .get()
    }
}