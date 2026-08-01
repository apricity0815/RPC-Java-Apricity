package part.Client;

import part.common.Message.RpcRequest;
import part.common.Message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Apric
 * @version 1.0
 * @description: 负责Socket层面的序列化和反序列化（使用Java默认序列化）
 * 底层通信做了哪些事情：建立连接、发送请求、接收响应、异常处理
 * @date 2026/7/31 17:44
 */
public class IOClient {
    //这里负责底层与服务端的通信，发送request，返回response
    public static RpcResponse sendRequest(String host, int port, RpcRequest request){ //服务端主机IP，端口号，请求对象
        try {
            //创建Socket，与服务器建立TCP连接
            Socket socket=new Socket(host, port);
            //创建对象输出流，用于将RpcRequest对象序列化并发送给服务端
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            //创建对象输入流，用于接收服务端返回的RpcResponse对象
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            //将RpcRequest对象序列化，并通过输出流发送给服务端
            oos.writeObject(request);
            //刷新输出流，确保数据发送出去
            oos.flush();

            //从输入流中读取服务端返回的序列化对象，并进行反序列化，还原为RpcResponse对象
            RpcResponse response=(RpcResponse) ois.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) { //捕获IO异常和类未找到异常（即与网络通信相关异常、反序列化对象找不到对应类的异常）
            e.printStackTrace();
            return null;
        }
    }
}
