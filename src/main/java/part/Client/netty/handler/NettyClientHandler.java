package part.Client.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import part.common.Message.RpcResponse;

/**
 * @author Apric
 * @version 1.0
 * @description: Netty客户端处理器，用于处理入站的RpcResponse消息。它继承自SimpleChannelInboundHandler<RpcResponse>，并重写了channelRead0方法来处理接收到的RpcResponse消息。
 *              SimpleChannelInboundHandler是Netty中用于处理服务器端响应的一个处理器类，主要功能是接收来自服务器的RpcResponse对象，并在处理过程中管理连接的生命周期。
 * @date 2026/8/5 16:21
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    //SimpleChannelInboundHandler是Netty提供的一个处理器类，用于处理入站消息。它是一个泛型类，泛型参数指定了该处理器可以处理的消息类型。在这里，泛型参数是RpcResponse，表示该处理器可以处理RpcResponse类型的消息。
    //channelRead0方法是SimpleChannelInboundHandler类中的一个核心方法，用于读取服务端返回的数据。
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception { //ChannelHandlerContext是Netty提供的一个类，它封装了与Channel相关的操作和状态信息，代表了当前I/O操作的环境。它提供了许多方法，用于在处理器中与Channel进行交互，例如写入数据、关闭连接等。
        // 接收到response, 给channel设计别名，让sendRequest里读取response
        // AttributeKey是Netty提供的一个类，用于在Channel中存储和检索属性。它类似于一个键值对，其中键是一个唯一的标识符，值是与该键关联的数据。
        // 在这里，我们创建一个AttributeKey来存储RpcResponse对象，将服务端返回的RpcResponse对象绑定到当前Channel的属性中，以便在其他地方可以通过该属性获取到响应对象
        AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
        //ctx.channel()获取当前的Channel对象，然后调用attr(key)方法获取与指定AttributeKey关联的属性。最后，调用set(response)方法将接收到的RpcResponse对象设置为该属性的值。
        ctx.channel().attr(key).set(response);
        //关闭当前的Channel连接，释放资源(短连接模式)
        ctx.channel().close();
    }

    //用于捕获运行过程中发生的异常，并进行处理。在这里，我们简单地打印异常堆栈信息，并关闭当前的Channel连接，释放资源。
    @Override
    public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常处理
        cause.printStackTrace();
        ctx.close();
    }
}
