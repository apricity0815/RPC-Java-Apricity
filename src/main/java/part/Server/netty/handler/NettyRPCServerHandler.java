package part.Server.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import part.Server.provider.ServiceProvider;
import part.common.Message.RpcRequest;
import part.common.Message.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Apric
 * @version 1.0
 * @description: NettyRPCServerHandler是一个用于处理入站RpcRequest消息的Netty服务器处理器。
 * 它继承自SimpleChannelInboundHandler<RpcRequest>，并重写了channelRead0方法来处理接收到的RpcRequest消息。
 * 该处理器的主要功能是接收客户端发送的RpcRequest，调用相应的服务方法，并将结果封装为RpcResponse返回给客户端。
 * @date 2026/8/5 20:55
 */
@AllArgsConstructor
public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    // ServiceProvider是一个服务提供者接口，用于获取服务实现类的实例。它负责管理和提供服务实例，以便在接收到RpcRequest时能够调用相应的服务方法。
    private ServiceProvider serviceProvider;

    // channelRead0方法是SimpleChannelInboundHandler类中的一个核心方法，是服务器端处理请求的地方。
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        //接收request请求，读取并调用服务，生成响应RpcResponse
        RpcResponse response = getResponse(request);
        //将响应写入通道并刷新，发送给客户端
        ctx.writeAndFlush(response);
        //关闭当前的Channel连接，释放资源(短连接模式)
        ctx.close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    private RpcResponse getResponse(RpcRequest rpcRequest){
        //得到服务名
        String interfaceName=rpcRequest.getInterfaceName();
        //得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        //反射调用方法
        Method method=null;
        try {
            method= service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke=method.invoke(service,rpcRequest.getParams());
            return RpcResponse.sussess(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RpcResponse.fail();
        }
    }
}
