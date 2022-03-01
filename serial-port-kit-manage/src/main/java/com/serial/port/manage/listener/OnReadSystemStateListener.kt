package com.serial.port.manage.listener

import com.serial.port.manage.model.SystemStateModel

/**
 * 读取版本
 *
 * @author zhouhuan
 * @time 2021/10/27
 */
interface OnReadSystemStateListener {
    /**
     * 结果
     *
     * @param systemStateModel 系统结果
     */
    fun onResult(systemStateModel: SystemStateModel)
}