package com.lifecircle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("posts") //这是MyBatis-Plus的注解，标记当前类和数据库里那张表对应，这里指定表名叫posts(如果表名和类名一样，该注解可以省略)
public class Post {

    @TableId(type = IdType.ASSIGN_UUID) //表明该字段是主键
    private String id;

    @TableField("user_id") //指定这个字段对应数据库表中的user_id列
    private String userId;

    private String text; //帖子的文字内容，没有注解，默认映射到同名的text列

    @TableField("images") //显式映射到images列
    private String images;

    private Double lng;

    private Double lat;

    private String address;

    @TableField("created_at")
    private LocalDateTime createdAt; //帖子的创建时间

    public Post() {} //无参构造方法。MyBatis-Plus在查询出数据后，会通过无参构造创建对象，再调用setter赋值，所以必须有

    public Post(String id, String userId, String text, String images, Double lng, Double lat, 
                String address, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.images = images;
        this.lng = lng;
        this.lat = lat;
        this.address = address;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
/*post实体类是和数据库表完全对应的，字段名、类型尽量和数据库保持一致，例如image在数据库里是一个字段存JSON串，这里也是String，
*但是PostDTO是返回给前端的，里面的image是List<String>(已解析成列表)*/