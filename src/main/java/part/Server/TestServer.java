package part.Server;

import part.Server.provider.ServiceProvider;
import part.Server.server.RpcServer;
import part.Server.server.impl.SimpleRPCRPCServer;
import part.Server.server.impl.ThreadPoolRPCRPCServer;
import part.common.service.UserService;
import part.common.service.impl.UserServiceImpl;

/**
 * @author Apric
 * @version 1.0
 * @description: 服务端入口测试
 * @date 2026/8/1 17:04
 */
public class TestServer {
    public static void main(String[] args) {
        //创建一个UserService服务实现类对象，用于处理客户端请求
        UserService userService=new UserServiceImpl();

        //实例化服务注册中心，服务端通过ServiceProvider将服务注册到中心，供客户端查找并调用
        //这个注册中心管理着服务接口与对应实现类之间的映射关系，管理所有可供客户端调用的服务
        ServiceProvider serviceProvider=new ServiceProvider();

        //注册服务到服务注册中心，使得客户端能够根据接口名称或标识查找到对应的服务
        serviceProvider.provideServiceInterface(userService);

        //实例化服务端RPC服务器，传入服务注册中心对象
        //RpcServer rpcServer=new SimpleRPCRPCServer(serviceProvider);
        //这里可以选择使用简单服务端或线程池服务端
        RpcServer rpcServer = new ThreadPoolRPCRPCServer(serviceProvider);

        //启动服务端
        rpcServer.start(9999);
    }
}
