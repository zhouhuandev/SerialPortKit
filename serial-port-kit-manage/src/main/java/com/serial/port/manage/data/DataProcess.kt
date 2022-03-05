package com.serial.port.manage.data

import com.serial.port.manage.SerialPortManager

/**
 * 数据处理中心
 *
 * @author zhouhuan
 * @time 2021/10/26
 */
internal class DataProcess(val manager: SerialPortManager) {
    /**
     * 是否按照自定义协议读取数据信息
     */
    val isCustom: Boolean = manager.config.isCustom

    /**
     * 最大读取长度
     */
    val maxSize: Int = manager.config.maxSize

    /**
     * 是否按最大接收长度进行返回
     */
    private val isReceiveMaxSize: Boolean = manager.config.isReceiveMaxSize

    /**
     * 记录读取数据的大小
     */
    private var mSerialBufferSize = 0

    /**
     * 串口接收数据保存数组
     */
    private val mSerialBuffer: ByteArray = ByteArray(maxSize)

    /**
     * 根据配置对串口数据进行处理
     *
     * @param buffer 当前读取的数据字节数组（原始数据，根据其 Max 大小定义大小，没有读到的数据为 00 补位）
     * @param size   当前读取的数据长度
     */
    fun processingRecData(buffer: ByteArray, size: Int) {
        if (isReceiveMaxSize) {
            reCreateData(buffer, size)
            return
        }
        // 根据其读取到的大小进行二次转运，舍去补位的数据
        val mBufferTemp = ByteArray(size)
        System.arraycopy(buffer, 0, mBufferTemp, 0, size)
        resultCallback(mBufferTemp, size)
    }

    /**
     * 处理数据读取反馈，对读取的数据按maxSize进行处理
     * 如果数据一次没有读取完整，通过数组拷贝将数据补全完整
     *
     * @param buffer 当前读取的数据字节数组
     * @param size   当前读取的数据长度
     */
    private fun reCreateData(buffer: ByteArray, size: Int) {
        if (hasReadDone(size) || mSerialBufferSize + size > maxSize) {
            // 截取剩余需要读取的长度
            val copyLength = maxSize - mSerialBufferSize
            arrayCopy(buffer, 0, copyLength)
            mSerialBufferSize += copyLength
            checkReCreate(mSerialBuffer)

            // 对反馈数据剩余的数据进行重新拷贝
            val lastLength = size - copyLength
            arrayCopy(buffer, copyLength, lastLength)
            mSerialBufferSize = lastLength
        } else {
            // 没有读取完整的情况，继续进行读取
            arrayCopy(buffer, 0, size)
            mSerialBufferSize += size
        }
        checkReCreate(mSerialBuffer)
    }

    /**
     * 判断当前数据是否读取完整
     *
     * @param size 读取数据的长度
     * @return true 已经读完
     */
    private fun hasReadDone(size: Int): Boolean {
        return size >= maxSize && mSerialBufferSize != maxSize
    }

    /**
     * 判断是否完成重组
     *
     * @param buffer 数据
     */
    private fun checkReCreate(buffer: ByteArray) {
        if (mSerialBufferSize == maxSize) {
            resultCallback(buffer, maxSize)
        }
    }

    /**
     * 判断数据是否读取完成，通过回调输出读取数据
     */
    private fun resultCallback(buffer: ByteArray, size: Int) {
        sendMessage(buffer, size)
        reInit()
    }

    /**
     * 发送回调
     *
     * @param buffer 回调数据
     * @param size   大小
     */
    private fun sendMessage(buffer: ByteArray, size: Int) {
        manager.helper.sendMessage(WrapReceiverData(buffer, size))
    }

    /**
     * 检查超时无效任务
     */
    fun checkTimeOutTask() {
        manager.helper.checkTimeOutTask()
    }

    /**
     * 重置数据
     */
    private fun reInit() {
        mSerialBufferSize = 0
    }

    /**
     * 通过数组拷贝，对数据进行重组
     *
     * @param bytes  当前读取的数据字节数组
     * @param srcPos 需要拷贝的源数据位置
     * @param length 拷贝的数据长度
     */
    private fun arrayCopy(bytes: ByteArray, srcPos: Int, length: Int) {
        System.arraycopy(bytes, srcPos, mSerialBuffer, mSerialBufferSize, length)
    }

}