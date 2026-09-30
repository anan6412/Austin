package com.lifecircle.controller;

import com.lifecircle.dto.Result;
import com.lifecircle.dto.ShopDTO;
import com.lifecircle.exception.ApiException;
import com.lifecircle.service.AmapService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController //标记这是一个REST控制器，所有方法的返回值会自动序列化为JSON并写入HTTP响应体
@RequestMapping("/api/shops") //定义该控制器的URL前缀为/api/shops。
@Validated //开启方法级别的参数参数校验，让方法参数上的@NotNull、@Min、@Max等注解生效
public class ShopController {

    @Autowired
    private AmapService amapService; //自动注入AmapService对象

    /**
     * 附近店铺搜索
     */
    @GetMapping("/nearby") //将这个方法映射为处理HTTP GET 请求，完整路劲GET/api/shops/nearby
    public Result<List<ShopDTO>> nearbySearch(
            @NotNull(message = "经度不能为空") @Min(value = -180, message = "经度范围不正确") @Max(value = 180, message = "经度范围不正确") Double lng,
            @NotNull(message = "纬度不能为空") @Min(value = -90, message = "纬度范围不正确") @Max(value = 90, message = "纬度范围不正确") Double lat,
            @RequestParam(defaultValue = "3000") @Min(value = 1, message = "半径必须大于 0") @Max(value = 50000, message = "半径最大 50000 米") Integer radius,
            @RequestParam(required = false) String keyword) {

        if (lng == null || lat == null) {
            throw new ApiException(400, "经度和纬度不能为空");
        }

        List<ShopDTO> shops = amapService.nearbySearch(lng, lat, radius, keyword); //调用amapService的nearbySearch方法，传入经度、纬度、搜索半径、关键词，获取附近店铺的列表
        return Result.success(shops); //将获取到的店铺列表封装进Result的成功响应里返回给前端
    }
}
