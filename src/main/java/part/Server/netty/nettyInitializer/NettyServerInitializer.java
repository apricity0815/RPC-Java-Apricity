package part.Server.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.AllArgsConstructor;
import part.Server.netty.handler.NettyRPCServerHandler;
import part.Server.provider.ServiceProvider;

/**
 * @author Apric
 * @version 1.0
 * @description: Netty服务端初始化器，配置消息的处理机制
 * @date 2026/8/5 20:55
 */
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    // 服务提供者，用于获取服务实例
    private ServiceProvider serviceProvider;

    //initChannel方法用于初始化通道，配置消息的处理机制
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 获取通道的管道，用于添加处理器
        ChannelPipeline pipeline = ch.pipeline();

        //消息格式 【长度】【消息体】，解决沾包问题
        pipeline.addLast(
                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));

        //计算当前待发送消息的长度，写入到前4个字节中
        pipeline.addLast(new LengthFieldPrepender(4));

        //使用Java序列化方式，netty的自带的解码编码支持传输这种结构
        pipeline.addLast(new ObjectEncoder());

        //使用了Netty中的ObjectDecoder，它用于将字节流解码为 Java 对象。
        //在ObjectDecoder的构造函数中传入了一个ClassResolver 对象，用于解析类名并加载相应的类。
        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                return Class.forName(className);
            }
        }));
        // 将自定义的NettyRPCServerHandler添加到管道ChannelPipeline中，用于处理RPC请求，使其成为数据处理链中的一个环节
        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}
