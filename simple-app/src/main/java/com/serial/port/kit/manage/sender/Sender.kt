package com.serial.port.kit.manage.sender

/**
 * 发送指令接口
 *
 * @author zhouhuan
 * @time 2019/12/18 18:35
 */
interface Sender {
    /**
     * 主板：检测
     * @return true 发送成功
     */
    fun sendStartDetect(): ByteArray

    /**
     * 主板：检测版本号
     * @return true 发送成功
     */
    fun sendReadVersion(): ByteArray
}