package part.Client.serviceCenter;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Apric
 * @version 1.0
 * @description: Zookeeper服务注册中心，负责服务的注册与发现
 * @date 2026/8/6 16:05
 */
public class ZKServiceCenter implements ServiceCenter{
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";

    //负责zookeeper客户端的初始化，并与zookeeper服务端进行连接
    public ZKServiceCenter(){
        // 指定一个 指数回退重试策略，用于在连接失败时，进行自动重试。参数分别为：初始等待时间（毫秒）和最大重试次数
        //初始重试间隔为1000毫秒（1秒），最大重试次数为3次。第一次重试等待1秒、第二次重试等待2秒、第三次重试等待4秒。总共最多尝试3次连接。
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);

        // zookeeper的地址固定，不管是服务提供者还是，消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        // CuratorFrameworkFactory是Curator框架提供的一个工厂类，用于创建CuratorFramework客户端实例。Curator是一个用于简化Zookeeper客户端开发的Java库，提供了更高层次的API和功能。
        this.client = CuratorFrameworkFactory.builder()  // 创建一个CuratorFramework客户端实例
                        .connectString("127.0.0.1:2181") // 指定连接到Zookeeper服务器的地址（在本地的2181端口）。无论是服务提供者还是消费者都要与Zookeeper建立连接
                        .sessionTimeoutMs(40000)         // 设置客户端的会话超时时间为40000毫秒（40秒）。如果客户端在这个时间内没有与Zookeeper服务器进行通信，Zookeeper会认为客户端已经失效
                        .retryPolicy(policy)             // 设置客户端的重试策略为前面定义的指数回退重试策略。用于在连接失败时进行自动重试
                        .namespace(ROOT_PATH)            // 设置客户端的命名空间为ROOT_PATH（"MyRPC"）。所有的Zookeeper操作都会在这个命名空间下进行，避免与其他应用的Zookeeper节点冲突
                        .build();                        // 构建客户端实例
        // 启动客户端，建立与Zookeeper服务器的连接
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }
    //根据服务名（接口名）返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            //获取指定serviceName（服务名称）路径下所有的子节点。每个子节点通常表示一个服务实例的地址（IP和端口），存储格式一般是ip:port
            //例如。如果服务名是UserService，Zookeeper上的路径可能是/MyRPC/UserService，其中包含多个子节点，每个子节点代表一个服务实例，存储的值是该实例的IP地址和端口号。
            List<String> strings = client.getChildren().forPath("/" + serviceName);
            // 这里默认用的第一个服务实例。如果有多个实例，可以通过负载均衡策略选择一个实例。负载均衡策略可以是轮询、随机、权重等。
            String string = strings.get(0);
            //将ip:port格式的字符串解析为InetSocketAddress对象，方便后续网络通信使用
            return parseAddress(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
