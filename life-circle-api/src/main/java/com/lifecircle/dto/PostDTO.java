package com.lifecircle.dto;

import java.util.List;

public class PostDTO { //帖子数据传输对象
    private String id;
    private String userId;
    private String text;
    private List<String> images; //帖子的图片列表，每个元素是一个图片URL
    private Double lng;
    private Double lat;
    private String address;
    private String createdAt;
    private Double distance; //帖子距离查询者当前位置的位置，这个值不是存数据可的，而是实时计算出来的，每次查询附近帖子时动态填充

    public PostDTO() {}

    public PostDTO(String id, String userId, String text, List<String> images, Double lng, Double lat, 
                   String address, String createdAt, Double distance) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.images = images;
        this.lng = lng;
        this.lat = lat;
        this.address = address;
        this.createdAt = createdAt;
        this.distance = distance;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
}
//CreatePostRequest是前端发请求时传进来的，只包含客户端能填的信息，PostDTO是后端返回给前端的，多了一些后端加工过的字段，比如地址描述、实时算的距离、格式化后的时间，这些字段前端不用传，由后端在Service层填充好再返回