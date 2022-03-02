package com.serial.port.manage.thread

import android.util.Log
import com.serial.port.manage.config.SerialPortConfig
import com.serial.port.manage.utils.ToastUtil
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 线程工厂
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
class SerialPortThreadFactory(
    private val config: SerialPortConfig,
    private val name: String,
    private val daemon: Boolean
) : ThreadFactory {

    companion object {
        private const val TAG = "SerialPortThreadFactory"
    }

    private val atomicInteger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AtomicInteger() }

    override fun newThread(r: Runnable?): Thread {
        val thread = Thread({
            try {
                r?.run()
            } catch (e: Exception) {
                val msg = "线程池中的某个线程发生了问题，请查看控制台或者日志文件！。"
                if (config.debug) {
                    Log.e(TAG, msg, e)
                }
                if (config.isShowToast) {
                    ToastUtil.showToastCenter(msg)
                }
            }
        }, "${name}-${atomicInteger.getAndIncrement()}")
        thread.isDaemon = daemon
        return thread
    }

}