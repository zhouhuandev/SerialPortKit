package com.serial.port.kit.manage.listener

import com.serial.port.kit.manage.model.DeviceVersionModel

/**
 * 读取版本
 *
 * @author <a href="mailto: zhouhuandev@gmail.com" rel="nofollow">zhouhuan</a>
 * @since 2022/3/19 16:27
 */
interface OnReadVersionListener {
    /**
     * 结果
     *
     * @param deviceVersionModel 设备信息
     */
    fun onResult(deviceVersionModel: DeviceVersionModel)
}