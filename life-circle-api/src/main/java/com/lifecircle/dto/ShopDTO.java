package com.lifecircle.dto;

public class ShopDTO { //公共类ShopDTO，即“店铺数据传输对象‘
    private String name;
    private String address;
    private String location;
    private String distance; //距离查询者的距离
    private String type;

    public ShopDTO() {}

    public ShopDTO(String name, String address, String location, String distance, String type) {
        this.name = name;
        this.address = address;
        this.location = location;
        this.distance = distance;
        this.type = type;
    }//Service层组装数据时可以用这个构造方法创建对象

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

//这个类的一个特点是字段都时String类型。这杨做的好处是，数据在Service层就已经处理成了最终要展示的样子，前端拿到不用再做格式转换，直接拿来渲染就行
