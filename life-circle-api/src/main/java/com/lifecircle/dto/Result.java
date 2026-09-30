package com.lifecircle.dto;

public class Result<T> { //这是一个泛型类，用来通一封装所有接口返回的响应数据，让前端可以按照固定格式解析
    private int code;
    private String msg;
    private T data; //成功时放返回的业务数据，失败时一般为null

    public Result() {}
    /*无参构造方法，允许创建空对象再通过setter设置字段
    * Java有一个规则：有些方法需要无参构造才能创建对象*/

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    /*静态方法，创建一个成功的响应对象并携带数据。调用Result.success(postDTO)就得到一个 code=200、msg=“success”、data为传入对象的响应*/

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }
    //静态方法的重载，用于成功但不携带数据的情况，data为null。如HelloController里的Result.success()

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
    //静态方法，创建一个默认状态吗为500的错误相应，只传错误信息。data为null。

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
    //静态方法的重载，允许自定义错误状态码和错误信息。比如Result.error（400，“经度和纬度不能为空”）

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    /*这个类的作用，所有接口都返回Result类型，前端只需要解析一种结构
    *{
    "code": 200,
    "msg": "success",
    "data": { ... 或 null }
} */
}
