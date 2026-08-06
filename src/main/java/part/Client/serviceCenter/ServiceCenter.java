package part.Client.serviceCenter;

import java.net.InetSocketAddress;

/**
 * @author Apric
 * @version 1.0
 * @description: 服务中心接口
 * @date 2026/8/6 16:04
 */
public interface ServiceCenter {
    //查询服务地址：根据服务名查找地址
    //InetSocketAddress是java.net包下的类，表示一个网络地址（包含IP地址和端口号）
    InetSocketAddress serviceDiscovery(String serviceName);
}
