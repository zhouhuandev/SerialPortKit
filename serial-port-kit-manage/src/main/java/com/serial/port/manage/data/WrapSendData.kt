package com.serial.port.manage.data

import androidx.annotation.IntRange

/**
 * 发送数据包装类
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
data class WrapSendData
@JvmOverloads constructor(
    var sendData: ByteArray,
    var sendOutTime: Int = 3000,
    var waitOutTime: Int = 300,
    @IntRange(from = 0, to = 3)
    var receiveMaxCount: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WrapSendData

        if (!sendData.contentEquals(other.sendData)) return false
        if (sendOutTime != other.sendOutTime) return false
        if (waitOutTime != other.waitOutTime) return false
        if (receiveMaxCount != other.receiveMaxCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sendData.contentHashCode()
        result = 31 * result + sendOutTime
        result = 31 * result + waitOutTime
        result = 31 * result + receiveMaxCount
        return result
    }
}