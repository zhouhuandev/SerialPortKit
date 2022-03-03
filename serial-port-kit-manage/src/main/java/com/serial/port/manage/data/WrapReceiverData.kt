package com.serial.port.manage.data

/**
 * 接收数据包装类
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
data class WrapReceiverData(
    var data: ByteArray,
    var size: Int,
    var duration: Long = 0L,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WrapReceiverData

        if (!data.contentEquals(other.data)) return false
        if (size != other.size) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + size
        result = 31 * result + duration.hashCode()
        return result
    }

}