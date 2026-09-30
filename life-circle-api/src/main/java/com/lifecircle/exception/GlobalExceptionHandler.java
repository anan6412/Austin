package com.lifecircle.exception;

import com.lifecircle.dto.Result;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice //标记这个类是全局异常处理器，同时它的返回值会自动转成JSON写入响应体。它相当于@Controlleradvice+@ResponseBody，专门用于REST接口的统一异常处理
public class GlobalExceptionHandler { //定义全局异常处理类，里面集中处理整个应用抛出的各种异常

    @ExceptionHandler(ApiException.class) //声明该方法专门捕获ApiException类型的异常，只要任何Controller或Service抛出了ApiException，就会进入这个方法处理
    public Result<?> handleApiException(ApiException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    //因为错误是data为null，不需要指定具体类型  从异常对象里取出code和massage，调用Result.error创建一个错误响应

    @ExceptionHandler(MethodArgumentTypeMismatchException.class) //声明这个方法专门捕获ethodArgumentTypeMismatchException，这个异常是Spring在参数类型转换失败时抛出的
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(400, "参数类型错误：" + e.getName());
    }//返回400状态码的错误响应，并拼接上出错的参数名（e.getName()）

    @ExceptionHandler(HttpMessageNotReadableException.class) //声明这个方法专门捕获HttpMessageNotReadableException，当请求体无法解析（比如JSON格式错误、缺少大括号)时，Spring会抛出这个异常
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error(400, "请求体格式错误");
    }

    @ExceptionHandler(Exception.class) //声明这个方法用来捕获所有未被前面指定异常处理器捕获的异常，Exception.class是所有异常的父类，相当于兜底处理
    public Result<?> handleException(Exception e) {
        return Result.error(500, "服务器内部错误：" + e.getMessage());
    }
} //返回500错误，并附上异常的具体信息（e.getMessage()）

//这个全局异常处理器只做一件事：拦截应用抛出的各种异常，转换成统一的Result格式返回给前端，保证接口格式始终一致，不要暴露内部栈细节