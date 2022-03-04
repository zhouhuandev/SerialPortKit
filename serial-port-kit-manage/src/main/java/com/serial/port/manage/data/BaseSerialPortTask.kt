package com.serial.port.manage.data

import com.serial.port.manage.listener.OnDataReceiverListener
import java.io.IOException
import java.io.OutputStream

/**
 * 基础串口任务
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
abstract class BaseSerialPortTask : SerialPortTask {

    var sendTime = 0L
    var waitTime = 0L
    var receiveCount = 0
    var isSendSuccess = true
    private var outputStream: OutputStream? = null

    override fun run() {
        try {
            outputStream?.write(sendWrapData().sendData)
            outputStream?.flush()
        } catch (e: IOException) {
            // 捕获到发送指令失败，代表串口连接有问题，打开重试机制（重新打开串口）
            isSendSuccess = false
        }
    }

    abstract fun sendWrapData(): WrapSendData

    abstract fun onDataReceiverListener(): OnDataReceiverListener

    fun stream(outputStream: OutputStream) {
        this.outputStream = outputStream
    }

}