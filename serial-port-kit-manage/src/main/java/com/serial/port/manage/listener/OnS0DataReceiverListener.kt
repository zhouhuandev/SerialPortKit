package com.serial.port.manage.listener

/**
 * 串口数据回调
 *
 * @author zhouhuan
 * @time 2021/10/28
 */
interface OnS0DataReceiverListener {
    /**
     * 回调数据（主线程）
     *
     * @param buffer 数据
     * @param size   长度
     */
    fun onDataReceive(buffer: ByteArray, size: Int)
}