package com.lifecircle.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifecircle.dto.PostDTO;
import com.lifecircle.dto.CreatePostRequest;
import com.lifecircle.entity.Post;
import com.lifecircle.exception.ApiException;
import com.lifecircle.mapper.PostMapper;
import com.lifecircle.util.DistanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service //标记为Spring 的 Service Bean，会被容器管理
public class PostService extends ServiceImpl<PostMapper, Post> implements CommandLineRunner { /*继承MyBatis-Plus的ServiceImpl<M,T>,泛型指定Mapper和实体，自动获得save、list、listByIds等方法
*实现CommandLineRunner，应用启动后会调用run方法*/


    @Autowired
    private AmapService amapService; //注入高德地图服务，用于逆地理编码获取地址

    @Autowired
    private GeoService geoService; //注入Redis GEO服务，用于管理帖子位置

    private final ObjectMapper objectMapper = new ObjectMapper(); //Jackson的JSON工具，用于将图片列表和JSON字符串互相转换

    /**
     * 应用启动时同步已有帖子到 Redis GEO
     */
    @Override
    public void run(String... args) {
        try {
            List<Post> posts = list();
            if (!posts.isEmpty()) {
                Map<String, Point> locations = new HashMap<>();
                for (Post post : posts) {
                    if (post.getLng() != null && post.getLat() != null) {
                        locations.put(post.getId(), new Point(post.getLng(), post.getLat()));
                    }
                }
                geoService.addPostLocations(locations);
                System.out.println("已同步 " + locations.size() + " 个帖子位置到 Redis GEO");
            }
        } catch (Exception e) {
            System.out.println("启动时同步帖子位置失败（可能数据库未启动）: " + e.getMessage());
        }
    }

    /**
     * 创建帖子
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "post:nearby", allEntries = true)
    public PostDTO createPost(CreatePostRequest request) {
        // 校验参数
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new ApiException(400, "userId 不能为空");
        }
        if (request.getText() == null || request.getText().isBlank()) {
            throw new ApiException(400, "text 不能为空");
        }
        if (request.getLng() == null || request.getLat() == null) {
            throw new ApiException(400, "位置信息不能为空");
        }

        // 逆地理编码获取地址
        String address = amapService.reverseGeocode(request.getLng(), request.getLat());

        // 构建 Post 实体
        Post post = new Post();
        post.setId(UUID.randomUUID().toString());
        post.setUserId(request.getUserId());
        post.setText(request.getText());
        post.setLng(request.getLng());
        post.setLat(request.getLat());
        post.setAddress(address);
        post.setCreatedAt(LocalDateTime.now());

        // 处理 images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                post.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                throw new ApiException("图片数据转换失败");
            }
        } else {
            post.setImages("[]");
        }

        // 保存到数据库
        save(post);

        // 添加位置到 Redis GEO
        geoService.addPostLocation(post.getId(), post.getLng(), post.getLat());

        // 转换为 DTO
        return convertToDTO(post, null);
    }

    /**
     * 查询附近的帖子（使用 Redis GEO）
     */
    public List<PostDTO> getNearbyPosts(double lng, double lat, int radius) {
        // 使用 Redis GEO 查询附近帖子 ID
        List<Map<String, Object>> nearbyPosts = geoService.getNearbyPostIds(lng, lat, radius);

        if (nearbyPosts.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询帖子详情
        List<String> postIds = new ArrayList<>();
        Map<String, Double> distanceMap = new HashMap<>();
        for (Map<String, Object> postInfo : nearbyPosts) {
            String postId = (String) postInfo.get("postId");
            postIds.add(postId);
            distanceMap.put(postId, (Double) postInfo.get("distance"));
        }

        // 从数据库查询帖子
        List<Post> posts = listByIds(postIds);

        // 转换为 DTO 并按时间倒序排序
        return posts.stream()
                .map(post -> convertToDTO(post, distanceMap.get(post.getId())))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private PostDTO convertToDTO(Post post, Double distance) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setText(post.getText());
        dto.setLng(post.getLng());
        dto.setLat(post.getLat());
        dto.setAddress(post.getAddress());
        dto.setCreatedAt(post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 解析 images
        try {
            List<String> images = objectMapper.readValue(post.getImages(), List.class);
            dto.setImages(images);
        } catch (Exception e) {
            dto.setImages(new ArrayList<>());
        }

        dto.setDistance(distance);

        return dto;
    }
}
