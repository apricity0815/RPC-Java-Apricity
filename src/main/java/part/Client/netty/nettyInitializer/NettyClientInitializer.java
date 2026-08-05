package part.Client.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import part.Client.netty.handler.NettyClientHandler;

/**
 * @author Apric
 * @version 1.0
 * @description: NettyClientInitializer是一个Netty客户端的初始化器类，继承自ChannelInitializer<SocketChannel>
 *               用于在客户端连接建立时初始化SocketChannel的ChannelPipeline。
 *               Channel是Netty中用于处理网络通信的基本抽象，表示一个网络连接，可以是TCP连接、UDP连接等。ChannelInitializer是Netty提供的一个抽象类，用于在Channel注册到EventLoop时进行初始化操作。
 *               SocketChannel是Netty中用于处理TCP连接的通道类型，表示一个客户端与服务器之间的TCP连接。
 *               ChannelPipeline是Netty中用于处理数据流的管道，是一个用于处理消息的责任链，它包含了一系列的ChannelHandler，用于处理入站和出站的数据。
 * @date 2026/8/5 16:20
 */
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    //ChannelInitializer是Netty提供的一个抽象类，用于在Channel注册到EventLoop时进行初始化操作。它的主要作用是在Channel被创建后，配置其ChannelPipeline，以便在数据传输过程中处理入站和出站的数据。

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // ChannelPipeline是Netty中用于处理数据流的管道，它包含了一系列的ChannelHandler，用于处理入站和出站的数据。
        // 初始化管道，每个SocketChannel都有一个独立的管道ChannelPipeline，用于定义该连接上所有数据的处理流程。
        // 通过调用ch.pipeline()方法，我们可以获取到当前SocketChannel的ChannelPipeline对象，从而对其进行配置，以便在其中添加编解码器和处理器。
        ChannelPipeline pipeline = ch.pipeline();

        // 消息格式 【长度】【消息体】，解决沾包问题
        /*
        * LengthFieldBasedFrameDecoder是Netty提供的一个解码器，用于处理基于长度字段的消息帧。它的主要作用是从字节流中提取出完整的消息帧，以解决TCP协议中的粘包和拆包问题。
        *
        * 构造函数参数解释：
        * maxFrameLength：指定单个消息帧的最大长度。如果接收到的消息帧超过这个长度，解码器将抛出异常。
        * lengthFieldOffset：长度字段的偏移量，表示长度字段在消息帧中的起始位置。
        * lengthFieldLength：长度字段的长度，表示长度字段占用的字节数。
        * lengthAdjustment：长度调整值，用于在计算消息帧的总长度时进行调整。通常情况下，这个值为0。
        * initialBytesToStrip：在解码时需要跳过的字节数，通常用于去掉长度字段本身。
        *
        * Integer.MAX_VALUE：表示消息帧的最大长度为Integer类型的最大值，
        * 0, 4：表示消息中长度字段的位置。0表示长度字段从消息帧的起始位置开始读取，4表示长度字段占用4个字节，
        * 0, 4：指明在解码后，从第4个字节开始计算实际的数据部分。0表示不进行长度调整，4表示在解码时需要跳过前4个字节（即长度字段本身）。
        * */
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));

        // LengthFieldPrepender是Netty提供的一个编码器，用于在发送消息时为消息体添加长度字段。
        // 它的主要作用是计算当前待发送消息的长度，作为一个4字节的字段写入消息头，以便接收方能够正确地解析消息。
        pipeline.addLast(new LengthFieldPrepender(4));

        // 编码器
        // 使用Java的默认序列化方式，netty的自带的解码编码支持传输这种结构
        // ObjectEncoder是Netty提供的一个编码器，用于将Java对象序列化为字节流，以便在网络上传输。
        pipeline.addLast(new ObjectEncoder());

        // 解码器
        // 使用了Netty中的ObjectDecoder，它用于将字节流解码为 Java对象
        // 在ObjectDecoder的构造函数中传入了一个ClassResolver 对象，用于解析类名并加载相应的类。
        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            //ClassResolver是Netty提供的一个接口，用于根据类名解析并加载相应的类。它在ObjectDecoder中被用来将接收到的字节流解码为Java对象时，根据类名找到对应的类定义。

            // resolve方法用于根据类名解析并加载相应的类。在这里，我们使用了Class.forName(className)来实现类的加载。
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                return Class.forName(className);
            }
        }));

        // 添加自定义的客户端处理器，用于处理入站的RpcResponse消息
        pipeline.addLast(new NettyClientHandler());
    }
}
