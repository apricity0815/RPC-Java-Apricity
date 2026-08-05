package part.Server.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import part.Server.netty.nettyInitializer.NettyServerInitializer;
import part.Server.provider.ServiceProvider;
import part.Server.server.RpcServer;

/**
 * @author Apric
 * @version 1.0
 * @description: NettyRPC服务端实现类
 * @date 2026/8/5 20:58
 */
@AllArgsConstructor
public class NettyRPCRPCServer implements RpcServer {
    //服务提供者
    private ServiceProvider serviceProvider;

    @Override
    public void start(int port) {
        //将NioEventLoopGroup分为两个线程组：bossGroup和workGroup
        //bossGroup负责接收客户端连接，workGroup负责处理客户端的请求
        //通过将bossGroup和workGroup分开，可以更好地分离连接接收和数据处理的职责，提升性能和可拓展性。可以实现更好的负载均衡和并行处理。可以实现更优的线程分配
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        System.out.println("netty服务端启动了");

        try {
            //ServerBootstrap是Netty中用于启动服务器的核心类，用于配置和启动Netty服务端
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //初始化
            serverBootstrap.group(bossGroup,workGroup)  //将两个线程组配置到ServerBootstrap中，分别用于处理连接请求和处理I/O操作
                    .channel(NioServerSocketChannel.class) //指定服务端使用NIO服务器套接字通道，NioServerSocketChannel是Netty中专门用于接收TCP连接的通道类，用于监听端口、接受连接请求等操作
                    .childHandler(new NettyServerInitializer(serviceProvider));  //NettyClientInitializer这里 配置netty对消息的处理机制

            //创建一个ChannelFuture对象，表示一个异步操作的结果
            //调用bind()会创建一个ServerSocketChannel，然后开始监听指定端口，等待客户端发起连接
            ChannelFuture channelFuture = serverBootstrap.bind(port)  //将服务端绑定到指定的端口（客户端连接的目标端口）上，开始监听客户端的连接请求
                                                         .sync();     //sync()方法会阻塞当前线程，直到绑定操作完成，确保服务端成功启动并监听指定端口

            //死循环监听
            channelFuture.channel()     //获取与此次异步操作关联的 Channel 对象
                         .closeFuture() //返回一个 ChannelFuture，它会在 Channel 关闭时 被标记为完成（success）
                         .sync();       //阻塞当前线程，直到 CloseFuture 完成（即 Channel 真正关闭）
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            //
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {

    }
}
