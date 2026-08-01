package part.Server.server.impl;

import part.Server.provider.ServiceProvider;
import part.Server.server.RpcServer;
import part.Server.server.work.WorkThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Apric
 * @version 1.0
 * @description: ThreadPoolRPCRPCServer是一个基于线程池的RPC服务器实现类，它实现了RpcServer接口，提供了启动和停止服务器的功能。
 *               它通过线程池来管理和执行请求处理任务，从而提高了服务器的并发处理能力。
 *               相比于SimpleRPCRPCServer，ThreadPoolRPCRPCServer使用线程池来管理工作线程，更有效地处理大量并发请求，避免了每个请求都创建一个新的线程导致性能问题
 * @date 2026/8/1 17:03
 */
public class ThreadPoolRPCRPCServer implements RpcServer {
    //服务器使用的线程池，通过线程池管理和执行客户端请求的处理任务，避免频繁创建销毁线程的问题
    private final ThreadPoolExecutor threadPool;

    //服务提供者，用于获取本地注册的服务实例，处理客户端请求时需要调用相应的服务方法
    private ServiceProvider serviceProvider;

    //默认构造方法
    public ThreadPoolRPCRPCServer(ServiceProvider serviceProvider){
        // 核心线程数：设为CPU核心数（Runtime.getRuntime().availableProcessors()）
        // 最大线程数：设为1000，表示线程池中最多可以同时存在1000个工作线程
        // 非核心线程空闲存活时间：设为60秒，超过此时间后，非核心线程会被回收
        // 队列大小：使用一个大小为100的ArrayBlockingQueue作为任务队列，用于存放待处理的任务（客户端请求）
        threadPool=new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                1000,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100));

        this.serviceProvider= serviceProvider;
    }

    //带参数的构造方法，允许用户自定义线程池的配置
    public ThreadPoolRPCRPCServer(ServiceProvider serviceProvider,
                                  int corePoolSize,         //核心线程数
                                  int maximumPoolSize,      //最大线程数
                                  long keepAliveTime,       //非核心线程空闲存活时间
                                  TimeUnit unit,            //时间单位，默认构造方法中使用的是秒
                                  BlockingQueue<Runnable> workQueue  //任务队列，用于存放待处理的任务
                                    ){

        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start(int port) {
        System.out.println("服务端启动了");
        try {
            //创建一个ServerSocket对象，绑定到指定的端口号port上，开始监听客户端的连接请求
            ServerSocket serverSocket=new ServerSocket(port);

            //无限循环，持续监听客户端的连接请求
            while (true){
                //如果没有连接，会堵塞在这里等待连接
                Socket socket= serverSocket.accept();
                //有连接，将客户端请求交给线程池处理，接管线程的创建、调度和复用，调用者不需要也不应该再去调用 .start()
                threadPool.execute(new WorkThread(socket,serviceProvider));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }
}
