package com.serial.port.manage.model

/**
 * 硬件版本信息
 *
 * @author zhouhuan
 * @time 2021/10/27
 */
class DeviceVersionModel(
    /**
     * 序列号
     */
    var serialNumber: String,
    /**
     * 硬件版本
     */
    var hardwareAmount: String,
    /**
     * 软件版本
     */
    var hardwareAppAmount: String
)