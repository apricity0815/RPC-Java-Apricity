package part.Client.rpcClient.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import part.Client.netty.nettyInitializer.NettyClientInitializer;
import part.Client.rpcClient.RpcClient;
import part.common.Message.RpcRequest;
import part.common.Message.RpcResponse;

/**
 * @author Apric
 * @version 1.0
 * @description: 一个由Netty实现的客户端
 * @date 2026/8/5 16:17
 */
public class NettyRpcClient implements RpcClient {
    // 主机地址
    private String host;
    // 端口号
    private int port;
    // Netty客户端的引导类，是Netty用于启动客户端的对象，负责设置与服务器的连接配置
    private static final Bootstrap bootstrap;
    // Netty客户端的事件循环组，是Netty的线程池，用于处理I/O操作
    // NioEventLoopGroup是EventLoopGroup的一个实现，专门用于处理NIO（非阻塞I/O）操作，适用于客户端和服务端的网络通信
    private static final EventLoopGroup eventLoopGroup;

    //构造函数，初始化主机地址和端口号
    public NettyRpcClient(String host,int port){
        this.host=host;
        this.port=port;
    }

    //netty客户端初始化，在静态代码块中进行初始化，确保在类加载时就完成配置
    //配置和准备Netty网络通信所需的各种资源，使得客户端能够正确地与服务器建立连接、发送请求、处理响应等
    static {
        //NioEventLoopGroup是Netty的事件循环组，负责管理客户端所有的I/O线程，每个线程负责处理一个或多个Channel的I/O操作
        eventLoopGroup = new NioEventLoopGroup();
        //Bootstrap是Netty客户端的引导类，用来设置客户端连接的相关配置，包括I/O线程池、连接类型、消息处理器等
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)                  //将eventLoopGroup设置为客户端的I/O线程池，负责处理所有的I/O操作
                .channel(NioSocketChannel.class)         //指定使用NIO传输通道（NioSocketChannel）来建立TCP连接
                .handler(new NettyClientInitializer());  //设置客户端的Channel初始化器（NettyClientInitializer），用于配置客户端的消息处理逻辑（例如编码器、解码器、消息处理器等）
    }
    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            //创建一个ChannelFuture对象，表示一个异步操作的结果
            ChannelFuture channelFuture  = bootstrap.connect(host, port)    //客户端发起连接到指定的服务器
                                                    .sync();                //调用sync()方法，阻塞当前线程，直到连接操作完成（成功或失败），确保在继续执行后续代码之前，连接已经建立

            //获取与远程服务器建立的Channel
            //Channel表示一个网络连接，客户端与服务器之间的所有数据都通过这个Channel进行传输，类似于传统的Socket连接
            Channel channel = channelFuture.channel();

            // 将请求对象request写入Channel并立即发送到远程服务器
            channel.writeAndFlush(request);

            //阻塞操作，直到连接被关闭
            //在这里，它等待服务端返回结果后，客户端的连接才会关闭
            channel.closeFuture().sync();
            // 阻塞的获得结果，通过给channel设计别名，获取特定名字下的channel中的内容（这个在hanlder中设置）
            // AttributeKey是线程隔离的，不会由线程安全问题。
            // 当前场景下选择堵塞获取结果
            // 其它场景也可以选择添加监听器的方式来异步获取结果 channelFuture.addListener...

            //通过AttributeKey获取存储在Channel中的RpcResponse对象
            //AttributeKey用于在Channel中存储和检索特定的数据，在这里，它用于获取之前在NettyClientHandler中设置的RpcResponse对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");

            //从Channel中获取与key对应的RpcResponse对象，这个对象是在处理响应时，由NettyClientHandler中接收到服务端响应后设置的
            RpcResponse response = channel.attr(key).get();

            System.out.println(response);
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
