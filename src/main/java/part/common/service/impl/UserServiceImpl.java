package part.common.service.impl;

import part.common.pojo.User;
import part.common.service.UserService;

import java.util.Random;
import java.util.UUID;

/**
 * @author Apric
 * @version 1.0
 * @description: UserServiceImpl实现类
 * @date 2026/7/31 17:28
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUserByUserId(Integer id) {
        System.out.println("客户端查询了" + id + "的用户");
        // 模拟从数据库中取用户的行为
        Random random = new Random();
        //UUID.randomUUID()生成一个全局唯一的字符串，作为随机用户名
        //random.nextBoolean()生成一个随机的布尔值，作为用户的性别
        User user = User.builder().userName(UUID.randomUUID().toString())
                .id(id)
                .sex(random.nextBoolean()).build();
        return user;
    }

    @Override
    public Integer insertUserId(User user) {
        System.out.println("插入数据成功"+user.getUserName());
        return user.getId();
    }
}
