package com.serial.port.manage.config

import android.app.Application
import android.graphics.Point

/**
 *
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
object SerialPortEnv {
    @Volatile
    var app: Application? = null

    val windowSize: Point = Point()

    @JvmStatic
    fun requireApp(): Application {
        return app ?: throw IllegalStateException("SerialPort app no set")
    }

}