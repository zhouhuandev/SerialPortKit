package com.serial.port.manage.thread

import android.util.Log
import com.serial.port.manage.config.SerialPortConfig
import com.serial.port.manage.data.BaseSerialPortTask
import java.util.concurrent.Future
import java.util.concurrent.ScheduledThreadPoolExecutor
import kotlin.math.max

/**
 *
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
internal class SerialPortDispatcher(
    private val config: SerialPortConfig,
) {

    companion object {
        private const val TAG = "SerialPortDispatcher"
    }

    private val mExecutor = config.executor ?: ScheduledThreadPoolExecutor(
        max(
            8,
            Runtime.getRuntime().availableProcessors() * 2
        ), SerialPortThreadFactory(config, "SerialPortThreadFactory", false)
    )

    fun dispatch(
        task: BaseSerialPortTask,
        onCompleted: ((task: BaseSerialPortTask) -> Unit)? = null
    ) {
        if (task.mainThread()) {
            execute(task)
            onCompleted?.invoke(task)
        } else {
            mExecutor.execute {
                execute(task)
                onCompleted?.invoke(task)
            }
        }
    }

    /**
     * 从线程池中创建子线程执行异步任务
     * 在任务数超过最大值，或者线程池Shutdown时将抛出异常
     *
     * @param runnable Runnable
     */
    fun submit(runnable: Runnable): Future<*> {
        return mExecutor.submit(runnable)
    }

    private fun execute(task: BaseSerialPortTask) {
        task.onTaskStart()
        task.run()
        task.onTaskCompleted()
    }

    fun dispatcherEnd() {
        if (config.executor != mExecutor) {
            if (config.debug) {
                Log.i(TAG, "auto shutdown default executor")
            }
            mExecutor.shutdown()
        }
    }

}


