package com.serial.port.kit.core.annotation

import androidx.annotation.IntDef
import com.serial.port.kit.core.SerialPort

/**
 *
 *
 * @author [zhouhuan](mailto:zhouhuandev@gmail.com)
 * @since 2022/6/18 01:56
 */
@IntDef(value = [SerialPort.CMD_BIN_SU_SHELL, SerialPort.CMD_X_BIN_SU_SHELL])
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class CmdSuShell
