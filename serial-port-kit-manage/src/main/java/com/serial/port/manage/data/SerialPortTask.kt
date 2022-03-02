package com.serial.port.manage.data

/**
 * 执行任务
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
interface SerialPortTask {

    fun onTaskStart()

    fun run()

    fun onTaskCompleted()

    fun mainThread(): Boolean = false
}