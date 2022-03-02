package com.serial.port.manage

import android.app.Application
import com.serial.port.manage.config.SerialPortConfig
import com.serial.port.manage.config.SerialPortEnv
import com.serial.port.manage.listener.OnDataCheckCall
import com.serial.port.manage.utils.ToastUtil
import java.util.concurrent.ExecutorService

/**
 * SerialPortKit
 *
 * @author zhouhuan
 * @time 2022/3/2
 */
class SerialPortKit private constructor(builder: Builder) {

    companion object {
        @JvmStatic
        fun newBuilder(app: Application): Builder {
            return Builder(app)
        }
    }

    class Builder(app: Application) {

        init {
            SerialPortEnv.app = app
        }

        internal var mExecutor: ExecutorService? = null

        internal var path = ""
        internal var baudRate: Int = 115200
        internal var maxSize: Int = 64
        internal var retryCount = 0
        internal var isReceiveMaxSize: Boolean = false
        internal var isCustom: Boolean = false
        internal var dataCheckCall: OnDataCheckCall? = null
        internal var debug = false
        internal var isShowToast = false

        fun path(path: String): Builder = apply { this.path = path }

        fun baudRate(baudRate: Int): Builder = apply { this.baudRate = baudRate }

        fun maxSize(maxSize: Int): Builder = apply { this.maxSize = maxSize }

        fun retryCount(retryCount: Int): Builder = apply { this.retryCount = retryCount }

        fun isReceiveMaxSize(isReceiveMaxSize: Boolean): Builder =
            apply { this.isReceiveMaxSize = isReceiveMaxSize }

        fun isCustom(isCustom: Boolean, dataCheckCall: OnDataCheckCall? = null): Builder = apply {
            this.isCustom = isCustom
            this.dataCheckCall = dataCheckCall
        }

        fun setExecutor(executor: ExecutorService): Builder = apply { this.mExecutor = executor }

        fun debug(debug: Boolean): Builder = apply { this.debug = debug }

        fun isShowToast(showToast: Boolean): Builder = apply { this.isShowToast = showToast }

        fun build(): SerialPortKit {
            checkParams()
            return SerialPortKit(this)
        }

        private fun checkParams() {
            check(path != "") { "Path is must important parameters，and it's not null !" }
            check(retryCount in 1..SerialPortManager.MAX_RETRY_COUNT) { "The retryCount is $retryCount, The number of retries should be between 0 and 3 !" }
            if (isCustom) {
                checkNotNull(dataCheckCall) { "isCustom is ture, dataCheckCall is not null !" }
            }
        }
    }

    private val manager = SerialPortManager(
        SerialPortConfig(
            path = builder.path,
            baudRate = builder.baudRate,
            maxSize = builder.maxSize,
            retryCount = builder.retryCount,
            isReceiveMaxSize = builder.isReceiveMaxSize,
            isCustom = builder.isCustom,
            dataCheckCall = builder.dataCheckCall,
            debug = builder.debug,
            isShowToast = builder.isShowToast
        ), builder.mExecutor
    )

    init {
        ToastUtil.init(SerialPortEnv.requireApp())
    }

    fun get() = manager
}