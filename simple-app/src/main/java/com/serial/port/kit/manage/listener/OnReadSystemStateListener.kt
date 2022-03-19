package com.serial.port.kit.manage.listener

import com.serial.port.kit.manage.model.SystemStateModel

/**
 * 读取系统信息
 *
 * @author <a href="mailto: zhouhuandev@gmail.com" rel="nofollow">zhouhuan</a>
 * @since 2022/3/19 16:57
 */
interface OnReadSystemStateListener {
    /**
     * 结果
     *
     * @param systemStateModel 系统结果
     */
    fun onResult(systemStateModel: SystemStateModel)
}