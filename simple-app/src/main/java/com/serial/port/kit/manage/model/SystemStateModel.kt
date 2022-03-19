package com.serial.port.kit.manage.model

/**
 * 系统状态
 *
 * @author <a href="mailto: zhouhuandev@gmail.com" rel="nofollow">zhouhuan</a>
 * @since 2022/3/19 16:57
 */
data class SystemStateModel(
    /**
     * 输入电压
     */
    val inputVoltage: Double = 0.0,
    /**
     * 电机电压
     */
    val motorVoltage: Double = 0.0,
    /**
     * VCC电压
     */
    val vccVoltage: Double = 0.0,
    /**
     * MCU电压
     */

    val mcuVoltage: Double = 0.0,
    /**
     * 温度值
     */

    val temperature: Int = 0,
    /**
     * 照度值
     */
    val illumination: Int = 0,
)
