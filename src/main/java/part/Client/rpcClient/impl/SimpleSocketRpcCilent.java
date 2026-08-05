package part.Client.rpcClient.impl;

import part.Client.rpcClient.RpcClient;
import part.common.Message.RpcRequest;
import part.common.Message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Apric
 * @version 1.0
 * @description: 一个简单实现的客户端，sendRequest方法与IOClient的sendRequest方法相同，都是通过socket发送请求和接收响应
 * @date 2026/8/5 16:17
 */
public class SimpleSocketRpcCilent implements RpcClient {
    private String host; //主机接口
    private  int port;   //端口号

    //构造函数
    public SimpleSocketRpcCilent(String host, int port){
        this.host=host;
        this.port=port;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            //创建socket连接
            Socket socket = new Socket(host, port);
            //创建对象输出流和输入流
            ObjectOutputStream oos = new  ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            //将请求对象写入输出流
            oos.writeObject(request);
            //刷新输出流
            oos.flush();

            //再从输入流中读取响应对象
            RpcResponse response = (RpcResponse) ois.readObject();

            //返回响应对象
            return response;

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }
}
