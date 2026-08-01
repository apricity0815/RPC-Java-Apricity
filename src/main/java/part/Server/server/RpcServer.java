package part.Server.server;

/**
 * @author Apric
 * @version 1.0
 * @description: 定义服务器行为契约
 * @date 2026/8/1 16:45
 */
public interface RpcServer {
    //开启服务端监听
    void start(int port);
    //停止服务端服务
    void stop();
}
