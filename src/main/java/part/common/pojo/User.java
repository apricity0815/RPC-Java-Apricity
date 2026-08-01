package part.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Apricity
 * @version 1.0
 * @create 2026/7/31 15:50
 */
@Builder
@Data
@NoArgsConstructor //自动生成无参构造函数，方便使用反射、序列化框架或场景对象时不参入参数的情况
@AllArgsConstructor //自动生成全参构造函数，方便创建对象时直接传入所有属性的值
public class User implements Serializable {  //实现Serializable接口，保证对象可以被序列化和反序列化
    // 客户端和服务端共有的
    private Integer id;
    private String userName;
    private Boolean sex;
}
