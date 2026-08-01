package part.Server.server.impl;

import lombok.AllArgsConstructor;
import part.Server.provider.ServiceProvider;
import part.Server.server.RpcServer;
import part.Server.server.work.WorkThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Apric
 * @version 1.0
 * @description: SimpleRPCRPCServer是一个简单的RPC服务器实现类，它实现了RpcServer接口，提供了启动和停止服务器的功能。
 *               用于启动一个简单的RPC服务器，并监听客户端的连接请求，处理客户端请求，并通过多线程处并发处理理每个连接请求。
 * @date 2026/8/1 17:03
 */
@AllArgsConstructor
public class SimpleRPCRPCServer implements RpcServer {
    //本地注册中心
    private ServiceProvider serviceProvide;

    @Override
    public void start(int port) {
        try {
            //创建一个ServerSocket对象，绑定到指定的端口号port上，开始监听客户端的连接请求
            //ServerSocket是一个TCP服务器端的类，它负责接收客户端的连接请求，并为每个连接创建一个新的Socket对象，用于与客户端进行通信
            ServerSocket serverSocket=new ServerSocket(port);

            System.out.println("服务器启动了");

            //循环监听客户端的连接请求
            while (true) {
                //如果没有连接，会堵塞在这里等待连接
                //accept()方法会阻塞当前线程，直到有客户端连接到服务器。当有客户端连接时，它会返回一个新的Socket对象，该对象用于与客户端进行通信
                Socket socket = serverSocket.accept();

                //有连接，创建一个新的线程来处理客户端的请求，将socket和serviceProvide传递给WorkThread
                // 每次请求都手动创建一个新的 Thread 对象，然后调用 .start() 让它运行。每来一个客户端连接，就 fork一个线程，请求处理完后线程销毁。
                new Thread(new WorkThread(socket,serviceProvide)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        //停止服务器
        //可再次优化服务端的关闭流程
    }
}

