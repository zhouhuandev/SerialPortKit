package com.serial.port.manage.config

import com.serial.port.manage.listener.OnDataCheckCall
import com.serial.port.manage.listener.OnDataReceiverListener
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * 串口配置信息
 *
 * @author zhouhuan
 * @time 2021/10/26
 */
data class SerialPortConfig(
    /**
     * 串口地址
     */
    val path: String,

    /**
     * 波特率
     */
    val baudRate: Int = 115200,

    /**
     * 最大读取长度
     */
    val maxSize: Int = 64,

    /**
     * 重试次数
     */
    val retryCount: Int,

    /**
     * 是否按最大接收长度进行返回（注：当前需求不需要按照数据的最大长度进行返回）
     */
    val isReceiveMaxSize: Boolean = false,

    /**
     * 是否按照自定义协议读取数据信息。
     * 如果 isCustom = true 自定义协议的话，需要在 [SerialReadThread.run] 自行实现读取串口数据信息
     * 注：如果自定义协议，[SerialPortConfig.isReceiveMaxSize] 则必须为 false
     */
    val isCustom: Boolean = false,
    /**
     * 自定义数据校验
     */
    val dataCheckCall: OnDataCheckCall?,

    val debug: Boolean = false,

    val isShowToast: Boolean = false,
)