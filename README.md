# NetTool
Android 设备网络监测

根据 ping、dns 解析、socket 连接、trace route、TrafficStats 判断设备网络情况.

每隔10秒执行一次 ping、dns 解析

最终结果:
1. good：dns 查询成功并且 ping 也成功
2. bad：ping 失败一次
3. unknown：初始状态或者识别不出来状态
4. offline：dns server 错误（没有获取到要发送的 DNS server 地址），网关错误（读取 /proc/net/route 文件内容失败），发送 dns 错误（发送 dns 数据出错），ping 读写错误（ping 的过程中读写错误），接收 dns 错误（接收 dns 数据出错），ping 地址错误（ping 地址是空），dns 未知域名错误（dns 没有查询到域名错误），dns udp 错误（创建 UDP socket 失败）

参考：
[Genius-Android](https://github.com/qiujuer/Genius-Android)
[network-connection-class](https://github.com/facebookarchive/network-connection-class)
