package part.common.service;

import part.common.pojo.User;

/**
 * @author Apric
 * @version 1.0
 * @description: 定义调用所需要的服务接口
 * @date 2026/7/31 17:26
 */
public interface UserService {
    // 客户端通过这个接口调用服务端的实现类
    User getUserByUserId(Integer id);
    //新增一个功能
    Integer insertUserId(User user);
}
