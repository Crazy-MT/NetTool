package com.mt.netstatus;

/**
 * @author : MaoTong
 * @date : 2020/6/10 19:26
 * description :
 */
public interface INetworkListener {

    /**
     * 最终结果回调
     * @param quality 最终结果
     * @param trafficStatus 流量
     * @param netStatus ping dns
     */
    void result(NetworkQuality quality, String trafficStatus, String netStatus);

    void speed(String speed);
}
