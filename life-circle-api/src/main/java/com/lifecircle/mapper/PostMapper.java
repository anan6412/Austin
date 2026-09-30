package com.lifecircle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifecircle.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper //MyBatis-plus提供的注解，标记这个接口是一个Mapper，Spring启动时会自动扫描并生成它的代理实现类，注入需要的地方（如Service)
public interface PostMapper extends BaseMapper<Post> {
}/*定义一个接口PostMapper，它继承MyBatis-Plus的BaseMapper<post>
  *BaseMapper<post>是一个泛型接口，类型参数是Post实体，表示这个Mapper操作的是posts表，对应的实体是Post*/
