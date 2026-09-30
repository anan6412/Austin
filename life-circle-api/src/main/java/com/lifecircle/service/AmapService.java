package com.lifecircle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifecircle.dto.ShopDTO;
import com.lifecircle.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service //声明这个类是一个Service，Spring会自动扫描并创建它的实例，并注入到其它需要的地方
public class AmapService { //定义公共类AmapService，专门封装高德地图相关的业务调用

    private final RestTemplate restTemplate; //用于发送HTTP请求调用高德API。final确保只能在构造方法里赋值一次，防止被意外修改
    private final String amapKey; //用来存放高德API的key
    private final ObjectMapper objectMapper = new ObjectMapper(); //用于解析高德API返回的JSON字符串，它是Jackson库的核心类，这里直接初始化，不需要注入

    @Value("${amap.base-url}") //从配置文件(如application.yml)中取名为为amap.base-url的配置项，并赋值给baseUrl字段
    private String baseUrl;

    public AmapService(RestTemplate restTemplate, @Value("${amap.key}") String amapKey)
    /*构造方法，通过构造函数注入依赖；参数restTemplate由Spring自动传入RestTemplatelateConfig配置的Bean
    * 参数amapKey通过@Value("${amap.key}")直接从配置文件读取高德key，注入进来*/
    {
        this.restTemplate = restTemplate; //把传入的restTemplate赋值给类字段
        this.amapKey = amapKey; //把读取到的amapKey赋值给类字段
    }

    /**
     * 周边搜索
     * @param lng 经度
     * @param lat 纬度
     * @param radius 半径（米）
     * @param keywords 关键词
     * @return 店铺列表
     */
    @Cacheable(value = "shop:nearby", key = "#lng + ':' + #lat + ':' + #radius + ':' + (#keywords ?: '')")
    /*@Cacheable是Spring Cache的注解，声明该方法的返回值会被缓存
    * value = "shop:nearby"：缓存的空间名字，类似于一个类
    * key是缓存的键，这里用SpEL表达式拼接了经度、纬度、半径和关键词
    * 当再次用同样的参数调用时，方法体不会执行，直接返回缓存中的结果，提高性能*/
    public List<ShopDTO> nearbySearch(double lng, double lat, int radius, String keywords) {
        //方法签名：公开方法，返回List<shopDTO>.参数：经度、纬度、搜索半径（米）、关键词（可选）
        try {
            String url = String.format("%s/v3/place/around?key=%s&location=%f,%f&radius=%d&keywords=%s&offset=20&output=JSON",
                    baseUrl, amapKey, lng, lat, radius, keywords != null ? keywords : "");
            /*用String.format拼接高德周边搜索API的完整URL
            *%s第一个是baseUrl，第二个是amapKey，第三个keywords（如果为null则用空字符串）
            *%f用于经纬度的浮点数，%d用于半径整数，offset=20表示最多返回20条记录，output=JSON表示返回JSON格式*/

            String response = restTemplate.getForObject(url, String.class);//通过restTemplate发送GET请求到拼接好的URL，并将响应体以字符串形式返回
            JsonNode rootNode = objectMapper.readTree(response); //使用ObjectMapper把JSON字符串解析成一个树状结构的JsonNode对象，方便逐层取值

            if (rootNode.has("status") && "1".equals(rootNode.get("status").asText())) { //检查返回的JSON里是否有status字段，并且值是否为1（高德api约定1表示请求成功）
                JsonNode poisNode = rootNode.path("pois"); //从根节点取出 pois 字段，它是一个数组，包含搜索到的 POI（兴趣点）信息。
                List<ShopDTO> shops = new ArrayList<>(); //创建一个空的ArrayList，用来存放转换后的店铺DTO对象

                for (JsonNode poi : poisNode) { //遍历pois数组里的每一个POI对象
                    ShopDTO shop = new ShopDTO(); //每次循环创建一个新的shopDTO实例
                    shop.setName(poi.path("name").asText("")); //从当前POI节点取name字段，转换为字符串，如果不存在则返回字符串。设置为店铺名称
                    shop.setAddress(poi.path("address").asText("")); //取address字段，设置为地址
                    shop.setLocation(poi.path("location").asText("")); //取location字段，高德返回的格式是”经度，纬度“，设置为店铺坐标
                    shop.setDistance(poi.path("distance").asText("")); //取distance字段，高德返回的是距离查询点的位置（单位米），设置为距离
                    shop.setType(poi.path("type").asText("")); //取type字段，设置为店铺类型（如"餐饮服务；中餐厅"）
                    shops.add(shop); //将构建好的ShopDTO加入列表
                }

                return shops;
            } else { //如果高德返回了错误
                String info = rootNode.path("info").asText("高德 API 请求失败"); //尝试取出info字段，获取错误描述，若没有则用默认提示
                throw new ApiException("高德 API 错误：" + info); //抛出自定义业务异常ApiException，包含错误信息，会被全局异常处理器捕获
            }
        } catch (Exception e) { //捕获上面所有可能发生的异常，包括网络错误、JSON解析错误
            throw new ApiException("周边搜索失败：" + e.getMessage()); //包装成ApiException再次抛出，统一交由全局异常处理器返回给前端
        }
    }

    /**
     * 逆地理编码
     * @param lng 经度
     * @param lat 纬度
     * @return 结构化地址字符串
     */
    @Cacheable(value = "geocode:reverse", key = "#lng + ':' + #lat") //同样使用Spring Cache缓存，空间名为geocode:reverse,缓存键为”经度；纬度“

    public String reverseGeocode(double lng, double lat) { //根据经纬度获取格式化的地址字符串，返回一个String
        try {
            String url = String.format("%s/v3/geocode/regeo?key=%s&location=%f,%f&output=JSON",
                    baseUrl, amapKey, lng, lat); //拼接高德逆地理编码API的URL，参数：baseUrl、amapKey、经纬度

            String response = restTemplate.getForObject(url, String.class); //发送GET请求，获取JSON字符串响应
            JsonNode rootNode = objectMapper.readTree(response); //解析JSON为树状结构

            if (rootNode.has("status") && "1".equals(rootNode.get("status").asText())) { //判断status是否为1
                JsonNode regeocodeNode = rootNode.path("regeocode").path("formatted_address"); //从根节点进入regeocode对象，这是格式化后的详细地址
                return regeocodeNode.asText("未知地址"); //转为字符串返回，若该字段不存在则返回“未知地址”。
            } else { //请求失败情况
                String info = rootNode.path("info").asText("高德 API 请求失败"); //获取错误信息
                throw new ApiException("高德 API 错误：" + info); //抛出业务异常
            }
        } catch (Exception e) { //捕获所有异常
            throw new ApiException("逆地理编码失败：" + e.getMessage()); //包装后再抛出
        }
    }
}
