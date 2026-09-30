package com.lifecircle.controller;

import com.lifecircle.dto.CreatePostRequest;
import com.lifecircle.dto.PostDTO;
import com.lifecircle.dto.Result;
import com.lifecircle.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts") //这个控制器里所有接口的路径前缀都是/api/posts
@Validated //使@NotNull、@Min、@Max这些注解才会生效，校验不通过时会直接抛出异常
public class PostController {

    @Autowired
    private PostService postService; //自动注入PostController的实例

    /**
     * 创建帖子
     */
    @PostMapping //映射HTTP POST请求
    public Result<PostDTO> createPost(@Valid @RequestBody CreatePostRequest request) {
        //@Valid对这个request对象开启嵌套校验；@RequestBody把HTTP请求体里的JSON数据自动转换成CreatePostRequest对象
        PostDTO post = postService.createPost(request);//调用Service层创建帖子，把请求对象传进去，返回一个组装好的PostDTO对象
        return Result.success(post);
    }

    /**
     * 查询附近的帖子
     */
    @GetMapping("/nearby") //映射HTTP GET 请求
    public Result<List<PostDTO>> getNearbyPosts(
            @NotNull(message = "经度不能为空") @Min(value = -180, message = "经度范围不正确") @Max(value = 180, message = "经度范围不正确") Double lng,
            @NotNull(message = "纬度不能为空") @Min(value = -90, message = "纬度范围不正确") @Max(value = 90, message = "纬度范围不正确") Double lat,
            @RequestParam//从URL查询参数里取值，比如？radius=3000
                    (defaultValue = "5000") @Min(value = 1, message = "半径必须大于 0") @Max(value = 50000, message = "半径最大 50000 米") Integer radius) {

        if (lng == null || lat == null) {
            return Result.error(400, "经度和纬度不能为空");
        }//兜底校验，多一层手动检查更保险

        List<PostDTO> posts = postService.getNearbyPosts(lng, lat, radius);//调用Service层，传入经度、纬度、半径，拿到附近帖子列表
        return Result.success(posts);//把帖子列表包进统一响应响应里返回
    }
}
