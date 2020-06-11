package com.mt.nettool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mt.netstatus.INetworkListener
import com.mt.netstatus.NetMonitor
import com.mt.netstatus.NetworkQuality
import kotlinx.android.synthetic.main.activity_net_status.*

class NetStatusActivity : AppCompatActivity() {
    var netMonitor: NetMonitor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_net_status)
        text.setText("waiting")

        netMonitor = NetMonitor(host = "www.baidu.com", application = application, listener = object : INetworkListener {
            override fun result(quality: NetworkQuality?, trafficStatus: String?, netStatus: String?) {
                runOnUiThread {
                    text.setText(netStatus)
                    text.append("\n网络质量: " + quality?.name)
                }
            }

            override fun speed(speed: String?) {
                runOnUiThread{
                    text_speed.setText(speed)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        netMonitor?.start()
    }

    override fun onPause() {
        super.onPause()
        netMonitor?.end()
    }
}
