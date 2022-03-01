package com.serial.port.manage.data

import android.util.Log
import com.serial.port.kit.core.SerialPort
import com.serial.port.kit.core.common.TypeConversion.byte2Int
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor

/**
 * 读取数据线程
 *
 * @author zhouhuan
 * @time 2021/10/26
 */
class SerialReadThread(private val mSerialPort: SerialPort, private val dataProcess: DataProcess) :
    Thread() {
    private val scheduledFuture: ScheduledFuture<*>? = null
    private var isStopRead = false
    private fun initThread() {
        val scheduledThreadPoolExecutor =
            Executors.newScheduledThreadPool(1) as ScheduledThreadPoolExecutor
        // 延迟 10ms 后，每隔 10ms 执行一次 ReadThread
        // scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(this, 10, 10, TimeUnit.MILLISECONDS);
        scheduledThreadPoolExecutor.submit(this)
    }

    fun stopReadDataThread(): Boolean {
        // boolean isSuccess = false;
        // if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
        //     isSuccess = scheduledFuture.cancel(true);
        //     scheduledFuture = null;
        // }
        // Log.d(TAG, "stopReadDataThread: 读取任务取消成功" + isSuccess);
        return true.also { isStopRead = it }
    }

    override fun run() {
        while (!isStopRead) {
            // 读取数据
            val inputStream = mSerialPort.inputStream ?: return
            try {
                if (dataProcess.isCustom) {
                    // 自定义协议解析

                } else {
                    val buffer = ByteArray(dataProcess.maxSize)
                    val size = inputStream.read(buffer)
                    if (size > 0) {
                        dataProcess.processingRecData(buffer, size)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "串口通讯数据读取有误", e)
            }
        }
    }

    companion object {
        private const val TAG = "ReadDataThreads"
    }

    init {
        initThread()
    }
}