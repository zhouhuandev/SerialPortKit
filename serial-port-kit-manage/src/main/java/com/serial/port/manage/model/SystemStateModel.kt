package com.serial.port.manage.model

import java.io.Serializable

/**
 * 系统状态
 *
 * @author zhouhuan
 * @time 2021/10/27
 */
class SystemStateModel(
    /**
     * 输入电压
     */
    var inputVoltage: Double,
    /**
     * 电机电压
     */
    var motorVoltage: Double,
    /**
     * VCC电压
     */
    var vccVoltage: Double,
    /**
     * MCU电压
     */
    var mcuVoltage: Double,
    /**
     * 温度值
     */
    var temperature: Int,
    /**
     * 照度值
     */
    var illumination: Int
) : Serializable