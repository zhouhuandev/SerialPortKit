package com.serial.port.manage.thread

import android.util.Log
import com.serial.port.kit.core.SerialPort
import com.serial.port.manage.data.DataProcess
import java.io.IOException

/**
 * 读取数据线程
 *
 * @author zhouhuan
 * @time 2021/10/26
 */
internal class SerialReadThread(
    private val mSerialPort: SerialPort,
    private val dataProcess: DataProcess
) : Thread() {

    companion object {
        private const val TAG = "ReadDataThreads"
    }

    private var isStopRead = false

    init {
        initThread()
    }

    private fun initThread() {
        dataProcess.manager.dispatcher.submit(this)
    }

    fun stopReadDataThread(): Boolean {
        return true.also { isStopRead = it }
    }

    override fun run() {
        while (!isStopRead) {
            // 读取数据
            val inputStream = mSerialPort.inputStream
            try {
                val buffer = ByteArray(dataProcess.maxSize)
                val size = inputStream.read(buffer)
                if (size > 0) {
                    if (dataProcess.isCustom) {
                        // 自定义协议解析
                        dataProcess.manager.config.dataCheckCall?.customCheck(buffer, size) {
                            dataProcess.processingRecData(it.data, it.size)
                        }
                    } else {
                        dataProcess.processingRecData(buffer, size)
                    }
                }
                // 暂停一点时间，免得一直循环造成CPU占用率过高
                sleep(1)
                // 检查清理超时无效任务
                dataProcess.checkTimeOutTask()
            } catch (e: IOException) {
                if (dataProcess.manager.config.debug) {
                    Log.e(TAG, "Serial communication data read error", e)
                }
            } catch (e: InterruptedException) {
                if (dataProcess.manager.config.debug) {
                    Log.e(TAG, "The read thread stopped abnormally and is about to restart...")
                }
            }
        }
    }

}