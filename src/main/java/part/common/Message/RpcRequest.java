package part.common.Message;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Apric
 * @version 1.0
 * @description: 定义发送的消息格式，封装RPC请求信息，通过Socket传输到服务端
 * @date 2026/7/31 17:35
 */
@Data
@Builder
public class RpcRequest implements Serializable {
    //服务类名，定为接口名，我们使用的是动态代理，外部给定信息是接口信息，客户端只知道接口，服务端根据接口名找到对应的实现类
    private String interfaceName;
    //调用的方法名
    private String methodName;
    //参数列表
    private Object[] params;
    //参数类型
    private Class<?>[] paramsType;
}
