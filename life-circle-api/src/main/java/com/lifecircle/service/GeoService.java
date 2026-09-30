package com.lifecircle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service //声明该类为Spring的Service组件，会被自动扫描和注入
public class GeoService { //定义GeoService类，负责基于Redis GEO 的地理位置相关操作

    private static final String POST_GEO_KEY = "post:geo"; //定义一个常量POST_GEO_KEY，值为“post:geo”,作为Redis中存储帖子位置信息的key（相当于一个地理集合的名字），所有帖子的经纬度都存进这个key里

    @Autowired
    private RedisTemplate<String, Object> redisTemplate; //自动注入RedisTemplate<String, Object> 实例，用于操作Redis。泛型表示key是字符串，value是对象。通过它能获取opsForGeo()进行GEO操作

    /**
     * 添加帖子位置到 GEO
     */
    public void addPostLocation(String postId, double lng, double lat) { //添加一个帖子的经纬度到Redis GEO集合。参数：帖子ID、经度、纬度
        redisTemplate.opsForGeo().add(POST_GEO_KEY, new Point(lng, lat), postId);
    }/*opsForGeo()获取Redis的地理操作对象
      *add(KEY,Point,member)方法向名为POST_GEO_KEY的地理集合添加一个成员，member为帖子ID，位置由Point对象指定（经度，纬度）
      *执行后，Redis内部会把这个点与帖子ID关联起来，用于后续查半径查询*/

    /**
     * 批量添加帖子位置
     */
    public void addPostLocations(Map<String, Point> locations) { //批量添加帖子位置。参数是一个Map，键为帖子ID，值为对应的经纬度点Point（SPring Data Redis提供的Point类）
        locations.forEach((postId, point) -> { //遍历传入的Map，postId为帖子ID，point为坐标原点
            redisTemplate.opsForGeo().add(POST_GEO_KEY, point, postId); //对每个帖子执行与单条添加相同的操作，将其坐标加入post:geo集合
        });
    }

    /**
     * 删除帖子位置
     */
    public void removePostLocation(String postId) { //从GEO集合中删除指定帖子的位置，例如帖子被删除或下架时调用
        redisTemplate.opsForGeo().remove(POST_GEO_KEY, postId);} //remove(KEY,member)移除key下名为postId的成员及位置

    /**
     * 查询附近帖子 ID
     * @param lng 经度
     * @param lat 纬度
     * @param radius 半径（米）
     * @return 帖子 ID 和距离列表
     */
    public List<Map<String, Object>> getNearbyPostIds(double lng, double lat, double radius) { //查询指定坐标半径内的所有帖子ID和距离。参数：中心经度、纬度、半径（单位为米）。返回一个列表，每个元素包含postId和distance
        Point center = new Point(lng, lat); //创建一个Point对象，表示圆心（查询中心点）
        // 将米转换为千米
        Distance distance = new Distance(radius / 1000.0, Metrics.KILOMETERS); //Redis GEO的radius方法要求使用千米
        Circle circle = new Circle(center, distance); //创建一个圆，包含圆心和半径，用于GEO查询

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = 
            redisTemplate.opsForGeo().radius(POST_GEO_KEY, circle);//执行radius查询：在POST_GEO_KEY这个地理合集中，搜索落入指定圆内的所有成员。返回一个GeoResults对象，包含每个匹配成员的信息（包括坐标、距离等）

        List<Map<String, Object>> nearbyPosts = new ArrayList<>(); //准备一个列表用于存放结果
        
        if (results != null) { //如果结果不为空，进行处理
            results.forEach(result -> { //遍历GeoResults中的每一项result（GeoResult类型）
                Map<String, Object> postInfo = new HashMap<>(); //为每个结果创建一个Map，用于存放帖子ID和距离
                postInfo.put("postId", result.getContent().getName()); //getContent()获取GeoLocation对象，其getName()返回成员名称（即帖子ID），存入map
                // 将距离转换回米
                postInfo.put("distance", result.getDistance().getValue() * 1000); //result.getDistance()获取距离对象，其getValue()返回距离数值（单位是千米，因为我们查询时用了千米）。乘以1000转换回米，存入map
                nearbyPosts.add(postInfo); //将该map添加到结果列表
            });
        }

        return nearbyPosts; //返回包含附近帖子信息的列表
    }

    /**
     * 计算两点之间的距离
     */
    public double getDistance(String postId, double lng, double lat) { //计算指定帖子（通过ID获取其存储的位置）到某个坐标点的距离，单位返回米。如果不成功则返回-1
        Point point = new Point(lng, lat); //创建目标坐标点
        Distance distance = redisTemplate.opsForGeo().distance(POST_GEO_KEY, postId, point, Metrics.KILOMETERS);
        // 调用distance方法，计算post:geo集合中名为postId的成员位置与给定point之间的距离，指定单位为千米
        return distance != null ? distance.getValue() * 1000 : -1; //如果距离对象存在，将千米值乘以1000转换为米后返回；否则返回-1表示计算失败或成员不存在
    }

    /**
     * 获取帖子的位置
     */
    public Point getPostLocation(String postId) { //根据帖子ID获取其在Redis GEO中存储的经纬度坐标
        List<Point> positions = redisTemplate.opsForGeo().position(POST_GEO_KEY, postId); //postition(KEY,member)返回该成员的坐标列表（通常只有一个点），是一个List<Point>。
        return (positions != null && !positions.isEmpty()) ? positions.get(0) : null; //如果列表不为空，返回第一个坐标点；否则返回null，表示不存在或查询失败
    }
}
