package com.lifecircle.exception;

public class ApiException extends RuntimeException {
    /*继承Runtime的好处：RuntimeException是非受检异常，抛出时不需要在方法签名上声明throws，也不用强制用方try-catch，
    *它会沿着调用栈往上抛，直到被全局异常处理器捕获，中途不需要每个方法都处理一遍*/

    private int code; //私有字段code，表示业务错误码。比如400表示参数错误，404表示资源不存在，500表示服务端错误

    public ApiException(String message) {
        super(message);
        this.code = 500;
    }/*第一个构造方法，只传错误信息。它把message传给父类RuntimeException，这样调用getMessage()就能拿到错误信息。
      *code默认为500，表示通用服务端错误*/

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }/*传错误码和错误信息。message同样传给父类，code用传入的值*/

    public int getCode() {
        return code;
    }// 获取错误码。全局异常处理器捕获到这个异常后，会调用getCode()取出状态码，设置到HTTP响应的状态码里

    public void setCode(int code) {
        this.code = code;
    }//设置错误码
}

/*简单梳理一下异常处理流程：
* 在Service或Controller里，遇到业务错误时 throw new ApiException（400，”参数不对“）
* 方法立即中断，异常往上抛
* GlobalExceptionHandler用@ExceptionHandler（ApiException.class）捕获到
* 取出e.getCode()和e.getMessage()
* 包装成Result.error（code，message）返回给前端*/