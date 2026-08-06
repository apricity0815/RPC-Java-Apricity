package part.Server.serviceRegister;

import java.net.InetSocketAddress;

/**
 * @author Apric
 * @version 1.0
 * @description: 服务注册接口
 * @date 2026/8/6 17:49
 */
public interface ServiceRegister {
    //注册服务：保存服务与地址
    void register(String serviceName, InetSocketAddress serviceAddress);
}
