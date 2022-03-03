package com.serial.port.manage

import android.util.Log
import com.serial.port.manage.data.BaseSerialPortTask
import com.serial.port.manage.config.SerialPortConfig
import com.serial.port.manage.data.WrapSendData
import com.serial.port.manage.listener.OnDataReceiverListener
import com.serial.port.manage.listener.OnRetryCall
import com.serial.port.manage.model.SimpleSerialPortTask
import com.serial.port.manage.thread.SerialPortDispatcher
import com.serial.port.manage.utils.ToastUtil
import java.util.concurrent.ExecutorService

/**
 * 串口包装管理
 *
 * @author zhouhuan
 * @time 2021/10/27
 */
class SerialPortManager(
    val config: SerialPortConfig,
    private val executor: ExecutorService?
) {

    companion object {
        private const val TAG = "SerialPortManager"
        const val MAX_RETRY_COUNT = 3
    }

    internal var retryCount = config.retryCount
    internal val helper = SerialPortHelper(this)
    internal val dispatcher = SerialPortDispatcher(config, executor)

    init {
        helper.onRetryCall = object : OnRetryCall {
            override fun retry(): Boolean = retryCount in 1..MAX_RETRY_COUNT

            override fun call(task: BaseSerialPortTask) {
                if (config.debug) {
                    Log.d(TAG, "Retry opening the serial port for ${retryCount++}ed !")
                }
                if (open()) {
                    send(task)
                }
            }
        }
    }

    fun open(): Boolean {
        val isSuccess = helper.reOpenDevice()
        if (isSuccess) {
            // 开启成功，重置重试次数
            retryCount = config.retryCount
            if (config.debug) {
                Log.i(
                    TAG,
                    "Open serial port successfully!，path=${config.path},baudRate=${config.baudRate}"
                )
            }
        } else {
            val msg = "串口打开失败，请尝试重新启动App"
            if (config.debug) {
                Log.e(TAG, msg)
            }
            if (config.isShowToast) {
                ToastUtil.showToastCenter(msg)
            }
        }
        return isSuccess
    }

    fun close() {
        helper.closeDevice()
    }

    fun send(wrapSendData: WrapSendData, onDataReceiverListener: OnDataReceiverListener) {
        send(SimpleSerialPortTask(wrapSendData, onDataReceiverListener))
    }

    fun send(task: BaseSerialPortTask) {
        helper.sendBuffer(task)
    }
}