package com.serial.port.manage

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.serial.port.kit.core.SerialPort
import com.serial.port.manage.config.SerialPortConfig
import com.serial.port.manage.data.DataProcess
import com.serial.port.manage.data.SerialReadThread
import com.serial.port.manage.listener.OnS0DataReceiverListener
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 串口帮助类
 *
 * @author zhouhuan
 * @time 2021/10/26
 */
object SerialPortHelper {
    private const val TAG = "SerialPortHelper"

    /**
     * 串口是否已经打开
     *
     * @return true' 打开
     */
    var isOpenDevice = false
        private set
    /**
     * 重试机制是否启动(重新打开串口)
     *
     * @return true 打开
     */
    /**
     * 设置重试机制
     *
     *  false 关闭重试机制
     */
    var isRetry = false

    private var mSerialPort: SerialPort? = null

    /**
     * 获取串口配置信息
     *
     * @return 配置
     */
    private var config: SerialPortConfig? = null
    private var serialReadThread: SerialReadThread? = null
    private val readWriteLock = ReentrantReadWriteLock()
    private val readLock: Lock = readWriteLock.readLock()
    private val writeLock: Lock = readWriteLock.writeLock()
    private val M_MAIN_LOOPER_HANDLER = Handler(Looper.getMainLooper())

    /**
     * 打开串口失败
     *
     * @param config 配置
     * @return true 打开成功 false 打开失败
     */
    fun openDevice(config: SerialPortConfig): Boolean {
        requireNotNull(config.path) { "You not have setting the device path !" }
        this.config = config
        try {
            mSerialPort = SerialPort(File(config.path), config.baudRate)
            isOpenDevice = true
        } catch (e: Exception) {
            mSerialPort = null
            isOpenDevice = false
            Log.e(TAG, "openDevice: open device is fail.", e)
        }
        if (isOpenDevice) {
            val processingData = DataProcess(config)
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
            mSerialPort!!.close()
            val isSuccess = serialReadThread!!.stopReadDataThread()
            if (isSuccess) {
                try {
                    serialReadThread = null
                    mSerialPort!!.inputStream.close()
                    mSerialPort!!.outputStream.close()
                } catch (e: IOException) {
                    Log.e(TAG, "closeDevice: 关闭串口数据流失败", e)
                }
            }
            mSerialPort = null
        } else {
            Log.d(TAG, "closeDevice: 串口尚未开启，无需关闭")
        }
    }

    /**
     * 重新打开串口
     *
     * @param config 配置信息，若不为 null，则使用其配置进行重新启动
     * @return true 打开成功 false 打开失败
     */
    fun reOpenDevice(config: SerialPortConfig?): Boolean {
        closeDevice()
        // 为了保障串口第一次打开的使用此方法，以防止配置 this.config 尚未初始化
        require(!(config == null && this.config == null)) { "You not have setting the device config !" }
        return openDevice((config ?: this.config)!!)
    }

    /**
     * 发送指令到串口
     *
     * @param cmd 指令
     * @return true 发送成功
     */
    fun sendCmds(cmd: String): Boolean {
        val mBuffer = """$cmd
""".toByteArray()
        return sendBuffer(mBuffer)
    }

    /**
     * 发送数据到串口
     *
     * @param mBuffer 数据
     * @return true 发送成功
     */
    fun sendBuffer(mBuffer: ByteArray?): Boolean {
        if (!isOpenDevice) {
            Log.d(TAG, "sendBuffer: You not open device !!!")
            return false
        }
        var result = true
        try {
            mSerialPort!!.outputStream.write(mBuffer)
            mSerialPort!!.outputStream.flush()
        } catch (e: IOException) {
            // 捕获到发送指令失败，代表串口连接有问题，打开重试机制（重新打开串口）
            result = false
            isRetry = true
        }
        return result
    }

    /**
     * 增加串口通讯监听
     *
     * @param onS0DataReceiverListener 监听器
     */
    fun addS0DataReceiverListener(onS0DataReceiverListener: OnS0DataReceiverListener) {
        writeLock.lock()
        try {
            if (config != null) {
                val onS0DataReceiverListeners = config!!.getOnS0DataReceiverListeners()
                val listenerWeakReference = WeakReference(onS0DataReceiverListener)
                onS0DataReceiverListeners.add(listenerWeakReference)
            }
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * 移除串口通讯监听
     *
     * @param onS0DataReceiverListener 监听器
     */
    fun removeS0DataReceiverListener(onS0DataReceiverListener: OnS0DataReceiverListener) {
        writeLock.lock()
        try {
            if (config != null) {
                val onS0DataReceiverListeners: MutableList<WeakReference<OnS0DataReceiverListener>> =
                    config!!.getOnS0DataReceiverListeners()
                val iterator = onS0DataReceiverListeners.iterator()
                while (iterator.hasNext()) {
                    val reference = iterator.next()
                    if (reference.get() == null) {
                        iterator.remove()
                        continue
                    }
                    if (reference.get() === onS0DataReceiverListener) {
                        iterator.remove()
                    }
                }
            }
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * 发送回调
     *
     * @param buffer 回调数据
     * @param size   大小
     */
    fun sendMessage(buffer: ByteArray, size: Int) {
        readLock.lock()
        try {
            if (config != null) {
                val onS0DataReceiverListeners = config!!.getOnS0DataReceiverListeners()
                for (i in onS0DataReceiverListeners.indices.reversed()) {
                    val listener = onS0DataReceiverListeners[i].get()
                    if (listener != null) {
                        runOnUiThread { listener.onDataReceive(buffer, size) }
                    }
                }
            }
        } finally {
            readLock.unlock()
        }
    }

    /**
     * 切换到主线程
     *
     * @param runnable Runnable
     */
    private fun runOnUiThread(runnable: Runnable) {
        M_MAIN_LOOPER_HANDLER.post(runnable)
    }

    /**
     * 清除配置信息
     */
    fun clear() {
        if (config != null) {
            config!!.getOnS0DataReceiverListeners().clear()
            config = null
        }
    }

}