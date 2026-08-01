package part.Server.server.work;

import lombok.AllArgsConstructor;
import part.Server.provider.ServiceProvider;
import part.common.Message.RpcRequest;
import part.common.Message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @author Apric
 * @version 1.0
 * @description: 单个连接的业务处理逻辑，核心是**反射+Java序列化
 *               实现Runnable接口，作为线程执行体，处理客户端请求并返回响应。
 *               线程池的使用：如果没有实现Runnable接口，无法将WorkThread交给线程池来管理。
 *                          通过实现Runnable接口，可以将WorkThread的实例作为任务提交给线程池，线程池会为每个请求分配一个线程来执行。
 *                          这种方法高效且易于拓展
 *               核心功能是在多线程环境中接收来自客户端的请求，调用本地服务，并将服务的结果返回给客户端
 * @date 2026/8/1 17:02
 */
@AllArgsConstructor
public class WorkThread implements Runnable{ //实现Runnable接口是为了使WorkThread可以被线程池或线程执行器执行，从而实现多线程处理客户端请求
    //用于与客户端通信的Socket对象，负责接收来自客户端的请求，并通过该socket将响应发送回客户端
    // 当客户端通过网络发送请求时，socket.getInputStream()用于接收客户端发送过来的数据（例如 RpcRequest 对象）
    // socket.getOutputStream()用于将处理结果通过网络发送给客户端（例如 RpcResponse 对象）
    //在WorkThread中，socket对象是与客户端通信的核心，它代表了服务器与某个特定客户端之间的连接，通过它可以实现数据的接收和发送，从而完成RPC调用的请求和响应过程
    private Socket socket;

    //serviceProvide是一个本地的服务注册中心，负责管理本地服务的注册和查找。它提供了一个getService方法，通过服务名（通常是接口名）来获取对应的服务实现类实例。
    //在WorkThread中，serviceProvide的作用是通过接口名称从本地获取相应的服务实例，然后调用该服务的方法处理请求
    //查找服务：当客户端请求某个服务时（通过RpcRequest中的接口名称），WorkThread会使用serviceProvide.getService(interfaceName)方法，根据请求的接口名称，从注册中心获取对应的服务实例。
    //服务调用：一旦获取到服务实例，WorkThread会使用Java反射机制调用该服务实例的方法（通过RpcRequest中的方法名称和参数类型），从而执行实际的业务逻辑，处理客户端请求。
    //serviceProvide是WorkThread类与本地服务之间的桥梁，它使得WorkThread能够根据请求动态地查找并调用相应的服务
    private ServiceProvider serviceProvide;

    //Runnable接口提供一个标准的方式来定义线程的任务，它的run()方法是线程执行的入口点，包含了线程执行的具体代码。当线程启动时，JVM会调用该方法
    @Override
    public void run() {
        try {
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            //读取客户端传过来的request
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            //反射调用服务方法获取返回值
            RpcResponse rpcResponse = getResponse(rpcRequest);
            //向客户端写入response
            oos.writeObject(rpcResponse);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //getResponse方法的作用是根据客户端发送的RpcRequest对象，查找对应的服务实例，并通过反射调用相应的方法，最终返回一个RpcResponse对象
    private RpcResponse getResponse(RpcRequest rpcRequest){
        //得到服务名：从RpcRequest对象中获取客户端请求的接口名称，这个名称用于在服务注册中心查找对应的服务实现类
        String interfaceName=rpcRequest.getInterfaceName();

        //得到服务端相应的服务实现类：通过ServiceProvider获取对应的服务实例，这里假设服务已经在ServiceProvider中注册过，serviceProvide.getService(interfaceName)会根据接口名称查询本地已注册的服务实例并返回
        Object service = serviceProvide.getService(interfaceName);

        //反射调用方法
        Method method = null;
        try {
            //使用反射，根据客户端请求的RpcRequest对象中的方法名称（rpcRequest.getMethodName()）和参数类型（rpcRequest.getParamsType()），获取服务实例中对应的方法对象
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());

            //通过Method对象调用服务实例的方法，并传入客户端请求的参数（rpcRequest.getParams()），获取方法执行的返回值
            //这是一个动态方法调用，能够在运行时确定调用哪个方法
            Object invoke = method.invoke(service,rpcRequest.getParams());

            //如果方法调用成功，则封装方法返回值（invoke）为成功的响应对象（RpcResponse.sussess）并返回给客户端
            return RpcResponse.sussess(invoke);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            //捕获反射过程中可能出现的异常，如：
            //NoSuchMethodException：如果服务实例中没有找到客户端请求的方法，会抛出此异常
            //IllegalAccessException：如果方法无法访问（例如方法是私有的），会抛出此异常
            //InvocationTargetException：如果方法在执行过程中抛出了异常，会被封装在此异常中

            //如果捕获到异常，打印异常堆栈信息，并返回一个失败的响应对象（RpcResponse.fail()）给客户端，表示请求处理失败
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RpcResponse.fail();
        }
    }
}
