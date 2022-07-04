package com.serial.port.manage

import android.os.SystemClock
import android.util.Log
import com.serial.port.kit.core.SerialPort
import com.serial.port.manage.data.BaseSerialPortTask
import com.serial.port.manage.data.DataProcess
import com.serial.port.manage.data.SerialPortTask
import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.listener.OnDataPickListener
import com.serial.port.manage.listener.OnRetryCall
import com.serial.port.manage.thread.SerialReadThread
import com.serial.port.manage.utils.contains
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
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
    private val writeLock: Lock = readWriteLock.writeLock()
    private val tasks: MutableList<BaseSerialPortTask> = ArrayList()
    private val invalidTasks: MutableList<SerialPortTask> = ArrayList()
    private val onDataPickListeners: MutableList<WeakReference<OnDataPickListener>> = arrayListOf()

    /**
     * 打开串口
     *
     * @param manager SerialPortManager
     * @return true 打开成功 false 打开失败
     */
    private fun openDevice(manager: SerialPortManager): Boolean {
        requireNotNull(manager.config.path) { "You not have setting the device path!" }
        try {
            mSerialPort = SerialPort(
                device = File(manager.config.path),
                baudRate = manager.config.baudRate,
                cmdSuShell = manager.config.cmdSuShell
            )
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
    fun closeDevice(): Boolean {
        var isCloseSuccess = false
        if (isOpenDevice) {
            isOpenDevice = false
            mSerialPort?.close()
            val isSuccess = serialReadThread?.stopReadDataThread() ?: false
            if (isSuccess) {
                serialReadThread = null
                try {
                    mSerialPort?.inputStream?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to close serial data inputStream.", e)
                }
                try {
                    mSerialPort?.outputStream?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to close serial data outputStream.", e)
                }
            }
            mSerialPort = null
            isCloseSuccess = true
        } else {
            if (manager.config.debug) {
                Log.d(TAG, "The serial port has not been opened, no need to close it.")
            }
        }
        return isCloseSuccess
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
            val msg = "Failed to send. You did not open the drive device!"
            switchThread(task) {
                it.onDataReceiverListener().onFailed(it.sendWrapData(), msg)
            }
            if (manager.config.debug) {
                Log.d(TAG, msg)
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
                it.sendTime = SystemClock.elapsedRealtime()
            }
        }
        if (!isSendSuccess) {
            // 捕获到发送指令失败，代表串口连接有问题，打开重试机制（重新打开串口且重新发送命令）
            onRetryCall?.let {
                if (it.retry()) {
                    it.call(task)
                } else {
                    switchThread(task) {
                        task.onDataReceiverListener()
                            .onFailed(
                                task.sendWrapData(),
                                "Failed to send, retried ${manager.retryCount} time."
                            )
                    }
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
            // 检测 WrapReceiverData 是否为 Task 匹配指令数据，否则进行全局统一回调
            var isTask = false
            if (invalidTasks.isNotEmpty()) invalidTasks.clear()
            tasks.forEach { task ->
                manager.config.addressCheckCall?.apply {
                    if (checkAddress(task.sendWrapData(), data)) {
                        onSuccess(task, data) {
                            isTask = it
                        }
                    }
                } ?: onSuccess(task, data) {
                    isTask = it
                }
            }
            // 移除无效任务
            invalidTasks.forEach { task ->
                tasks.remove(task)
            }
            // 收到串口端数据是否需要与 Task 成对数据进行隔离，先决条件为：检测是否 Task 是否已经进行回调，否则进行统一回调
            if (!isTask) {
                sendMessageAllDataPickListener(data)
            }
        } finally {
            readLock.unlock()
        }
    }

    /**
     * 发送所有的监听
     */
    private fun sendMessageAllDataPickListener(data: WrapReceiverData) {
        val iterator = onDataPickListeners.iterator()
        while (iterator.hasNext()) {
            val reference = iterator.next()
            if (reference.get() == null) {
                iterator.remove()
                continue
            }
            reference.get()!!.onSuccess(data)
        }
    }

    private fun onSuccess(
        task: BaseSerialPortTask,
        data: WrapReceiverData,
        block: ((Boolean) -> Unit)
    ) {
        // Task 自定义接收次数若是不为0，则按照其 Task 自定义次数接收，否则按照全局最大接收次数接收
        val receiveMaxCount =
            if (task.sendWrapData().receiveMaxCount != 0) task.sendWrapData().receiveMaxCount else manager.config.receiveMaxCount
        // 接收次数小于最大次数，则代表一定进行回调
        block.invoke(task.receiveCount < receiveMaxCount)
        if (task.receiveCount < receiveMaxCount) {
            task.receiveCount++
            task.waitTime = SystemClock.elapsedRealtime()
            switchThread(task) {
                task.onDataReceiverListener().onSuccess(data.apply {
                    duration = abs(task.waitTime - task.sendTime)
                })
            }
            if (task.receiveCount == receiveMaxCount) {
                invalidTasks.add(task)
            }
        } else {
            invalidTasks.add(task)
        }
    }

    /**
     * 检查超时无效任务
     */
    fun checkTimeOutTask() {
        readLock.lock()
        try {
            if (invalidTasks.isNotEmpty()) invalidTasks.clear()
            tasks.forEach { task ->
                if (isTimeOut(task)) {
                    switchThread(task) {
                        task.onDataReceiverListener().onTimeOut()
                    }
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
        val currentTimeMillis = SystemClock.elapsedRealtime()
        return if (task.waitTime == 0L) {
            task.waitTime = SystemClock.elapsedRealtime()
            // 发送数据超时
            val sendOffset = abs(currentTimeMillis - task.sendTime)
            sendOffset > task.sendWrapData().sendOutTime
        } else {
            // 有接收到过数据，但是距离上一个数据已经超时或者第一次接收数据超时
            val waitOffset = abs(currentTimeMillis - task.waitTime)
            waitOffset > task.sendWrapData().waitOutTime
        }
    }

    /**
     * 切换线程
     */
    private fun switchThread(task: BaseSerialPortTask, block: (task: BaseSerialPortTask) -> Unit) {
        if (task.mainThread()) {
            manager.dispatcher.runOnUiThread {
                block.invoke(task)
            }
        } else {
            block.invoke(task)
        }
    }

    /**
     * 增加统一监听回调
     */
    fun addDataPickListener(listener: OnDataPickListener) {
        writeLock.lock()
        try {
            if (!onDataPickListeners.contains(listener)) {
                onDataPickListeners.add(WeakReference(listener))
                if (manager.config.debug) {
                    Log.d(TAG, "$listener add monitor successfully")
                }
            }
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * 移除统一监听回调
     */
    fun removeDataPickListener(listener: OnDataPickListener) {
        writeLock.lock()
        try {
            onDataPickListeners.contains(listener, true) {
                if (it && manager.config.debug) {
                    Log.d(TAG, "$listener remove monitor successfully")
                }
            }
        } finally {
            writeLock.unlock()
        }
    }

}