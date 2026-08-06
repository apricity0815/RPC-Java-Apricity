package part.Server.serviceRegister.impl;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import part.Server.serviceRegister.ServiceRegister;

import java.net.InetSocketAddress;

/**
 * @author Apric
 * @version 1.0
 * @description: Zookeeper服务注册实现类
 * @date 2026/8/6 17:49
 */
public class ZKServiceRegister implements ServiceRegister {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;

    //zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";

    //负责zookeeper服务端的初始化，并与zookeeper服务端进行连接
    public ZKServiceRegister(){
        // 指数时间重试策略
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是，消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }

    //注册服务到注册中心
    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress) {
        try {
            // serviceName创建成永久节点，服务提供者下线时，不删服务名，只删地址
            // 首先检测Zookeeper上是否已经有该服务的根节点。 /serviceName 代表一个服务类型的根节点
            // 如果该路径存在，则说明有这个服务的注册记录。如果路径不存在，则表示该服务名还没有注册过。
            // 这个检查的作用是避免重复创建相同的服务名节点
            if(client.checkExists().forPath("/" + serviceName) == null){
                // 如果服务名节点不存在，则创建一个持久化的服务名节点。持久化节点意味着该节点在Zookeeper中会一直存在，除非显式删除。即使服务提供者下线，服务名节点仍然保留
                client.create()  //curator提供的创建节点的方法
                        .creatingParentsIfNeeded()  //确保父路径存在，如果父节点不存在，则会一并创建父节点。例如要创建/UserService/127.0.0.1:8080节点，如果/UserService节点不存在，则会先创建/UserService节点
                        .withMode(CreateMode.PERSISTENT)  //使用持久化模式创建服务名节点。持久化节点意味着该节点在Zookeeper中会一直存在，除非显式删除。即使服务提供者下线，服务名节点仍然保留
                        .forPath("/" + serviceName);    //指定要创建的节点路径，这里是服务名的根节点路径，如/UserService
            }

            // 创建服务实例路径地址，一个/代表一个节点
            //这里构造了一个完整的服务实例路径，路径格式为：/serviceName/ip:port。serviceName是服务的名称，serviceAddress是服务实例的地址（例如127.0.0.1:8080）
            // 这个路径表示一个具体的服务实例的地址节点，存储了服务提供者的地址信息。例如/UserService/127.0.0.1:8080代表UserService服务在127.0.0.1:8080这个地址上的实例
            String path = "/" + serviceName +"/"+ getServiceAddress(serviceAddress);

            // 创建服务实例的临时节点，服务器下线就删除节点
            client.create() //curator提供的创建节点的方法
                    .creatingParentsIfNeeded()  //确保父路径存在，如果父节点不存在，则会一并创建父节点
                    .withMode(CreateMode.EPHEMERAL) //使用临时节点模式创建服务实例路径。临时节点意味着该节点在Zookeeper中会随着客户端的断开而自动删除。当服务提供者下线或断开连接时，临时节点会被删除，从而反映出服务实例的不可用状态
                    .forPath(path); //指定节点的路径，也就是/serviceName/ip:port，对应着服务实例的地址信息
        } catch (Exception e) {
            System.out.println("此服务已存在");
        }
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
