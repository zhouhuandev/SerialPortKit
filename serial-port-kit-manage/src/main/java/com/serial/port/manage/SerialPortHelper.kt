package com.serial.port.manage

import android.util.Log
import com.serial.port.kit.core.SerialPort
import com.serial.port.manage.data.BaseSerialPortTask
import com.serial.port.manage.data.DataProcess
import com.serial.port.manage.data.SerialPortTask
import com.serial.port.manage.thread.SerialReadThread
import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.listener.OnRetryCall
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.abs

/**
 * 串口帮助类
 *
 * @author zhouhuan
 * @time 2021/10/26
 */
internal class SerialPortHelper(private val manager: SerialPortManager) {

    companion object {
        private const val TAG = "SerialPortHelper"
    }

    /**
     * 串口是否已经打开
     *
     * @return true 打开
     */
    var isOpenDevice = false
        private set

    /**
     * 重试
     */
    var onRetryCall: OnRetryCall? = null

    private var mSerialPort: SerialPort? = null
    private var serialReadThread: SerialReadThread? = null
    private val readWriteLock = ReentrantReadWriteLock()
    private val readLock: Lock = readWriteLock.readLock()
    private val tasks: MutableList<BaseSerialPortTask> = ArrayList()

    /**
     * 打开串口
     *
     * @param manager SerialPortManager
     * @return true 打开成功 false 打开失败
     */
    private fun openDevice(manager: SerialPortManager): Boolean {
        requireNotNull(manager.config.path) { "You not have setting the device path!" }
        try {
            mSerialPort = SerialPort(File(manager.config.path), manager.config.baudRate)
            isOpenDevice = true
        } catch (e: Exception) {
            mSerialPort = null
            isOpenDevice = false
            Log.e(TAG, "Failed to open device.", e)
        }
        if (isOpenDevice) {
            val processingData = DataProcess(manager)
            serialReadThread = SerialReadThread(mSerialPort!!, processingData)
        }
        return isOpenDevice
    }

    /**
     * 关闭串口
     */
    fun closeDevice() {
        if (isOpenDevice) {
            isOpenDevice = false
            mSerialPort?.close()
            val isSuccess = serialReadThread?.stopReadDataThread() ?: false
            if (isSuccess) {
                try {
                    serialReadThread = null
                    mSerialPort?.inputStream?.close()
                    mSerialPort?.outputStream?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to close serial data stream.", e)
                }
            }
            mSerialPort = null
        } else {
            if (manager.config.debug) {
                Log.d(TAG, "The serial port has not been opened, no need to close it")
            }
        }
    }

    /**
     * 重新打开串口
     *
     * @return true 打开成功 false 打开失败
     */
    fun reOpenDevice(): Boolean {
        closeDevice()
        return openDevice(manager)
    }

    /**
     * 发送数据到串口
     * @param task 发送数据任务
     */
    fun sendBuffer(task: BaseSerialPortTask): Boolean {
        if (!isOpenDevice) {
            if (manager.config.debug) {
                Log.d(TAG, "You not open device !!!")
            }
            return false
        }
        var isSendSuccess = true
        mSerialPort?.apply {
            task.stream(outputStream = outputStream)
        }
        manager.dispatcher.dispatch(task) {
            isSendSuccess = it.isSendSuccess
            if (isSendSuccess) {
                it.sendTime = System.currentTimeMillis()
            }
        }
        if (!isSendSuccess) {
            // 捕获到发送指令失败，代表串口连接有问题，打开重试机制（重新打开串口且重新发送命令）
            onRetryCall?.let {
                if (it.retry()) {
                    it.call(task)
                } else {
                    task.onDataReceiverListener()
                        .onFailed(
                            task.sendWrapData(),
                            "Failed to send, retried ${manager.retryCount} time"
                        )
                }
            }
        } else {
            if (!tasks.contains(task)) {
                tasks.add(task)
            }
        }
        return isSendSuccess
    }

    /**
     * 收到串口端数据
     *
     * @param data 回调数据
     */
    fun sendMessage(data: WrapReceiverData) {
        readLock.lock()
        try {
            tasks.forEach { task ->
                task.waitTime = System.currentTimeMillis()
                task.onDataReceiverListener().onSuccess(data)
            }
        } finally {
            readLock.unlock()
        }
    }

    /**
     * 检查超时无效任务
     */
    fun checkTimeOutTask() {
        readLock.lock()
        try {
            val invalidTasks: MutableList<SerialPortTask> = ArrayList()
            tasks.forEach { task ->
                if (isTimeOut(task)) {
                    task.onDataReceiverListener().onTimeOut()
                    invalidTasks.add(task)
                }
            }
            // 移除无效任务
            invalidTasks.forEach { task ->
                tasks.remove(task)
            }
        } finally {
            readLock.unlock()
        }
    }

    /**
     * 检测是否超时
     */
    private fun isTimeOut(task: BaseSerialPortTask): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        return if (task.waitTime == 0L) {
            // 表示一直没收到数据
            val sendOffset = abs(currentTimeMillis - task.sendTime)
            sendOffset > task.sendWrapData().sendOutTime
        } else {
            // 有接收到过数据，但是距离上一个数据已经超时
            val waitOffset = abs(currentTimeMillis - task.waitTime)
            waitOffset > task.sendWrapData().waitOutTime
        }
    }
}