package com.serial.port.kit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.serial.port.kit.core.common.TypeConversion
import com.serial.port.kit.sender.SenderManager
import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.data.WrapSendData
import com.serial.port.manage.listener.OnDataReceiverListener

class MainActivity : AppCompatActivity() {

    private val button: Button by lazy { findViewById(R.id.button) }
    private val button2: Button by lazy { findViewById(R.id.button2) }
    private val button3: Button by lazy { findViewById(R.id.button3) }
    private val button4: Button by lazy { findViewById(R.id.button4) }
    private val button5: Button by lazy { findViewById(R.id.button5) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        // 打开串口
        button.setOnClickListener {
            if (MyApp.portManager?.isOpenDevice == false) {
                MyApp.portManager?.open()
            }
        }
        // 关闭串口
        button2.setOnClickListener {
            MyApp.portManager?.close()
        }
        // 发送数据
        button3.setOnClickListener {
            MyApp.portManager?.send(WrapSendData(SenderManager.getSender().sendStartDetect()),
                object : OnDataReceiverListener {
                    override fun onSuccess(data: WrapReceiverData) {
                        Log.d(TAG, "响应数据：${TypeConversion.bytes2HexString(data.data)}")
                    }

                    override fun onFailed(wrapSendData: WrapSendData, msg: String) {
                        Log.e(
                            TAG,
                            "发送数据: ${TypeConversion.bytes2HexString(wrapSendData.sendData)}, $msg"
                        )
                    }

                    override fun onTimeOut() {
                        Log.e(TAG, "发送或者接收超时")
                    }
                })
        }
        // 切换串口
        button4.setOnClickListener {
            MyApp.portManager?.switchDevice(path = "/dev/ttyS1")
        }
        // 切换波特率
        button5.setOnClickListener {
            MyApp.portManager?.switchDevice(baudRate = 9600)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}