package com.lifecircle.dto;

import java.util.List;

public class CreatePostRequest { //公共类，创建帖子请求
    private String userId; //存用户id
    private String text; //存帖子的文字内容
    private List<String> images; // 存放多张图片的地址（URL）
    private Double lng; //存发帖时的经度
    private Double lat; //存发帖时的纬度

    public CreatePostRequest() {}

    public CreatePostRequest(String userId, String text, List<String> images, Double lng, Double lat) {
        this.userId = userId;
        this.text = text;
        this.images = images;
        this.lng = lng;
        this.lat = lat;
    }

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
}

//这个类的作用就是约定好前端发帖时必须研究传什么字段，后端收到JSON后自动映射成CreatPostRequest对象，Service层直接取它的属性值来处理业务
