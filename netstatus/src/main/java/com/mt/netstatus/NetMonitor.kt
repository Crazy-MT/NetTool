package com.mt.netstatus

import android.app.Application
import android.util.Log
import com.facebook.network.connectionclass.ConnectionClassManager
import com.facebook.network.connectionclass.ConnectionClassManager.ConnectionClassStateChangeListener
import com.facebook.network.connectionclass.ConnectionQuality
import com.facebook.network.connectionclass.DeviceBandwidthSampler
import kotlinx.coroutines.*
import net.qiujuer.genius.kit.cmd.Cmd
import net.qiujuer.genius.kit.cmd.DnsResolve
import net.qiujuer.genius.kit.cmd.Ping
import net.qiujuer.genius.kit.cmd.Telnet
import java.net.InetAddress
import java.net.UnknownHostException

/**
 *  @author : MaoTong
 *  @date : 2020/6/10 20:04
 *  description :
 */
class NetMonitor(var listener: INetworkListener?, var application: Application?, private var host: String?) {
    var job: Job? = null

    private var mConnectionClassManager: ConnectionClassManager? = null
    private var mDeviceBandwidthSampler: DeviceBandwidthSampler? = null
    private var mListener: ConnectionChangedListener? = null
    private var mConnectionClass = ConnectionQuality.UNKNOWN

    init {
        Cmd.init(application)

        mConnectionClassManager = ConnectionClassManager.getInstance()
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance()
        mListener = ConnectionChangedListener()
    }

    fun start() {
        job = GlobalScope.launch(Dispatchers.Main) {
            mConnectionClassManager!!.register(mListener)
            mDeviceBandwidthSampler!!.startSampling()

            while (true) {
                val netStatus = netTool()
                listener?.result(netStatus?.quality, mConnectionClass.name, netStatus?.netResult)
                delay(10000)
            }
        }
    }

    suspend fun netTool(): NetTool? = withContext(Dispatchers.IO){
        var result: String = ""
        // 包数，包大小，目标，是否解析IP
        val ping = Ping(3, 32, host, true)
        ping.start()
        result += "Ping: $ping"
        // target
        // 目标，可指定解析服务器
        var dns: DnsResolve? = null
        try {
            // Add DNS service
            // 添加DNS服务器
            dns = DnsResolve(host, InetAddress.getByName(host))
            dns.start()
            result += "\nDnsResolve: $dns"
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }
        // target port
        // 目标，端口
        val telnet = Telnet(host, 80)
        telnet.start()
        result += "\nSocket: $telnet"

        // trace route
        /*val traceRoute = TraceRoute(host)
        traceRoute.start()
        result += "\n\nTraceRoute: $traceRoute"
        Log.e(TAG, "TraceRoute: $traceRoute")*/
        val netTool: NetTool = NetTool(NetworkQuality.UNKNOWN, result)
        if (ping.error == Cmd.SUCCEED && dns?.error == Cmd.SUCCEED) {
            netTool.quality = NetworkQuality.GOOD
        }

        if (ping.error != Cmd.SUCCEED) {
            netTool.quality = NetworkQuality.BAD
        }

        if (ping.error == Cmd.HOST_UNREACHABLE_ERROR || dns?.error != Cmd.SUCCEED) {
            netTool.quality = NetworkQuality.OFFLINE
        }
        return@withContext netTool
    }

    fun end() {
        job?.cancel()

        mConnectionClassManager!!.remove(mListener)
        mDeviceBandwidthSampler!!.stopSampling()
    }

    /**
     * Listener to update the UI upon connectionclass change.
     */
    inner class ConnectionChangedListener : ConnectionClassStateChangeListener {
        override fun onBandwidthStateChange(bandwidthState: ConnectionQuality, average: Double) {
            mConnectionClass = bandwidthState
            listener?.speed(mConnectionClass.name)
        }
    }
}