package part.common.Message;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Apric
 * @version 1.0
 * @description: 封装RPC响应信息，从服务端返回给客户端
 * @date 2026/7/31 17:35
 */
@Data
@Builder
public class RpcResponse implements Serializable {
    //响应码，200表示成功，500表示失败
    private int code;
    //状态信息
    private String message;
    //具体数据
    private Object data;
    //构造成功信息
    public static RpcResponse sussess(Object data){
        return RpcResponse.builder().code(200).data(data).build();
    }
    //构造失败信息
    public static RpcResponse fail(){
        return RpcResponse.builder().code(500).message("服务器发生错误").build();
    }
}
