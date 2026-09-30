package com.lifecircle.dto;

public class ApiResponse { //公共类，代表api响应
    private int code; //用来存放状态码
    private String msg; //用来存放提示信息，比如“success”或者“经度和纬度不能为空”

    public ApiResponse() {}

    public ApiResponse(int code, String msg) { //无参构造方法，可以new ApiResponse先创造一个空对象，再通过setCode和setMsg设值
        this.code = code;
        this.msg = msg;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
}

/*这个类就是定义一个简单的数据传输格式，让返回的JSON长这样
{
   "code": 200,
   "msg":"success"
}
 */

