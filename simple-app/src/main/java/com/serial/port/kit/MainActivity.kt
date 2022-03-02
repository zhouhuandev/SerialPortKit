package com.serial.port.kit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.serial.port.kit.sender.SenderManager
import com.serial.port.manage.data.WrapReceiverData
import com.serial.port.manage.data.WrapSendData
import com.serial.port.manage.listener.OnDataReceiverListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyApp.manager?.apply {
            val open = open()
            if (open)
                send(WrapSendData(SenderManager.getSender().sendStartDetect()),
                    object : OnDataReceiverListener {
                        override fun onSuccess(data: WrapReceiverData) {
                            TODO("Not yet implemented")
                        }

                        override fun onFailed(wrapSendData: WrapSendData, msg: String) {
                            TODO("Not yet implemented")
                        }

                        override fun onTimeOut() {
                            TODO("Not yet implemented")
                        }
                    })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MyApp.manager?.close()
    }
}