package part.Client.rpcClient;

import part.common.Message.RpcRequest;
import part.common.Message.RpcResponse;

/**
 * @author Apric
 * @version 1.0
 * @description: TODO
 * @date 2026/8/5 16:16
 */
public interface RpcClient {
    //提取共性，定义底层通信的方法
    RpcResponse sendRequest(RpcRequest request);
}
