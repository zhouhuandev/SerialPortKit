package com.serial.port.manage.listener

import com.serial.port.manage.data.BaseSerialPortTask

/**
 *
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
interface OnRetryCall {
    fun retry(): Boolean

    fun call(task: BaseSerialPortTask)
}