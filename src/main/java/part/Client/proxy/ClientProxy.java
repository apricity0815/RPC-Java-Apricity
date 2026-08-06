package part.Client.proxy;

import lombok.AllArgsConstructor;
import part.Client.IOClient;
import part.Client.rpcClient.RpcClient;
import part.Client.rpcClient.impl.NettyRpcClient;
import part.Client.rpcClient.impl.SimpleSocketRpcCilent;
import part.common.Message.RpcRequest;
import part.common.Message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Apric
 * @version 1.0
 * @description: JDK动态代理核心！让客户端像调用普通本地方法一样调用远程方法
 * @date 2026/7/31 17:45
 */
@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
//    //传入参数service接口的class对象，反射封装成一个request
//    //初始化代理类时传入host和port，方便后续远程调用
//    private String host;
//    private int port;

    //加入RpcClient类变量，传入不同的client实现类（SimpleSocketRpcCilent、NettyRpcClient），即可调用公共的接口sendRequest方法发送请求
    private RpcClient rpcClient;

    //选择Netty客户端，不用传参
    public ClientProxy(){
        rpcClient = new NettyRpcClient();
    }

//    //构造函数，根据传入的参数选择不同的RpcClient实现类
//    public ClientProxy(String host, int port, int chooose) {
//        switch (chooose){
//            case 0:
//                rpcClient = new NettyRpcClient(host, port);
//                break;
//            case 1:
//                rpcClient = new SimpleSocketRpcCilent(host, port);
//        }
//    }

//    //构造函数，默认使用NettyRpcClient作为RpcClient的实现类
//    public ClientProxy(String host, int port) {
//        rpcClient = new NettyRpcClient(host, port);
//    }


    //jdk动态代理，每一次代理对象调用方法，都会经过此方法增强（反射获取request对象，socket发送到服务端）
    //核心逻辑，用于封装请求并处理服务端响应
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { //proxy是代理对象本身 ；method是被调用的方法，通过反射获取方法的详细信息； args是方法参数
        //构建RpcRequest对象，封装接口名、方法名、参数和参数类型
        RpcRequest request= RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramsType(method.getParameterTypes())
                .build();

//        //IOClient.sendRequest 和服务端进行数据传输,发送请求并接收响应
//        RpcResponse response= IOClient.sendRequest(host,port,request);
        //数据传输
        RpcResponse response = rpcClient.sendRequest(request);
        //获取服务端响应中的数据部分，返回给调用者
        return response.getData();
    }

    // 利用JDK的动态代理机制，动态生成代理对象的方法，返回一个实现指定接口的代理实例
    public <T>T getProxy(Class<T> clazz){
        //使用 Proxy.newProxyInstance 方法动态创建一个代理对象，传入类加载器、需要代理的接口数组和当前InvocationHandler（调用处理程序）实例
        //class.getClassLoader() ：传入接口的类加载器
        //new Class[]{clazz}：指定代理接口，clazz就是需要被代理的接口，
        // this ：InvocationHandler的实现，即当前的 ClientProxy 实例，作为调用处理程序，处理所有方法调用
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        //返回一个代理对象，强制转换为指定的接口类型 T，这样调用者就可以像调用本地方法一样调用远程服务
        //调用该对象的方法会被转发到ClientProxy的invoke方法中，从而实现远程调用的透明化
        return (T)o;
    }
}
