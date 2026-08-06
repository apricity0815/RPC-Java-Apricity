package part.Server.provider;

import part.Server.serviceRegister.ServiceRegister;
import part.Server.serviceRegister.impl.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Apric
 * @version 1.0
 * @description: 本地服务存放器，维护服务注册表，存储服务提供者实例。
 *               ServiceProvider类提供了注册服务和获取服务实例的功能。将服务对象和接口名称映射存储在一个HashMap中，实现了服务的本地注册和动态获取。
 * @date 2026/8/1 17:01
 */
public class ServiceProvider {
    //集合中存放服务的实例，key为服务接口的全限定名（String类型），value为服务接口对应的实现类实例（Object类型）
    private Map<String,Object> interfaceProvider;

    //使用Zookeeper注册服务，需要在本地注册这里加入服务端的端口和地址
    private int port;
    private String host;
    //使用Zookeeper注册服务，需要在本地注册这里加入服务注册表对象来注册服务类
    private ServiceRegister serviceRegister;

//    //构造函数，初始化服务注册表，为interfaceProvider字段分配一个新的HashMap实例
//    public ServiceProvider(){
//        this.interfaceProvider=new HashMap<>();
//    }
    public ServiceProvider(String host, int port){
        //传入的host和port参数用于指定服务端的地址和端口号，便于在Zookeeper中注册服务时使用
        this.host = host;
        this.port = port;
        //初始化服务注册表，为interfaceProvider字段分配一个新的HashMap实例
        this.interfaceProvider = new HashMap<>();
        //初始化Zookeeper服务注册器，用于将服务注册到Zookeeper中
        this.serviceRegister = new ZKServiceRegister();
    }

    //本地注册服务
    //将一个服务实例service注册到服务注册表interfaceProvider中，将服务对象与其实现的接口关联起来
    public void provideServiceInterface(Object service){ //接收一个服务实例service作为参数
        //获取服务实例的类名（全限定名），用于在服务注册表中存储服务实例
        String serviceName = service.getClass().getName();
        //获取服务实例实现的所有接口
        //service.getClass().getInterfaces() 通过反射获取服务对象实现的所有接口（Class<?>[]类型）。每个服务对象可能实现多个接口，因此返回一个接口数组。
        Class<?>[] interfaceName = service.getClass().getInterfaces();

        //遍历服务实例service实现的接口数组，将每个接口的全限定名作为key，对应的服务实例作为value，存入服务注册表interfaceProvider中
        for (Class<?> clazz:interfaceName){
            //本机的映射表
            interfaceProvider.put(clazz.getName(),service);

            //注册到Zookeeper上，在注册中心注册服务
            serviceRegister.register(clazz.getName(),new InetSocketAddress(host, port));
        }

    }
    //根据接口的全限定类名（interfaceName），从服务注册表interfaceProvider中获取对应的服务实例
    //提供根据接口名称获取服务实例的功能，可以动态获取服务对象，具体调用哪个服务接口完全由接口名称决定
    public Object getService(String interfaceName){
        //从interfaceProvider中获取接口名称对应的服务实例，如果存在则返回该服务实例，否则返回null
        return interfaceProvider.get(interfaceName);
    }
}
