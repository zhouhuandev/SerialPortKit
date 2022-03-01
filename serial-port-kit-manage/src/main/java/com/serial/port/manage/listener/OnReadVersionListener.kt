package com.serial.port.manage.listener

import com.serial.port.manage.model.DeviceVersionModel

/**
 * 读取版本
 *
 * @author zhouhuan
 * @time 2021/10/27
 */
interface OnReadVersionListener {
    /**
     * 结果
     *
     * @param deviceVersionModel 设备信息
     */
    fun onResult(deviceVersionModel: DeviceVersionModel)
}