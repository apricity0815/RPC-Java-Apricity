package part.Client;

import part.Client.proxy.ClientProxy;
import part.common.pojo.User;
import part.common.service.UserService;

/**
 * @author Apric
 * @version 1.0
 * @description: 客户端入口测试
 * @date 2026/7/31 17:44
 */
public class TestClient {
    public static void main(String[] args) {
//      //创建ClientProxy对象作为代理类，传入服务端的ip和端口号。处理远程方法调用的封装、发送请求和接收响应的工作
//      ClientProxy clientProxy=new ClientProxy("127.0.0.1",9999);
        //创建代理对象参数修改一下，可选择不同的客户端（simple、netty）
        ClientProxy clientProxy=new ClientProxy("localhost",9999,0);
        //客户端通过clientProxy动态获取UserService接口的代理对象，调用接口方法时会被拦截并发送RPC请求到服务端
        UserService proxy=clientProxy.getProxy(UserService.class);

        //使用代理对象调用getUserByUserId方法和insertUserId方法时，实际上会触发ClientProxy中invoke方法的执行，后者会封装成远程调用请求并通过网络传递给服务端，服务端执行UserServiceImpl的实现逻辑并返回数据，客户端获取并输出这些数据
        User user = proxy.getUserByUserId(1);
        System.out.println("从服务端得到的user="+user.toString());

        User u=User.builder().id(100).userName("Apric").sex(true).build();
        Integer id = proxy.insertUserId(u);
        System.out.println("向服务端插入user的id"+id);
    }
}
