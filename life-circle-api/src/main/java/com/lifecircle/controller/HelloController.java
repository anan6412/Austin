package com.lifecircle.controller;

import com.lifecircle.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //意味着类中所有的方法的返回值都会自动转换成JSON或XML写入HTTP响应体，而不是跳转页面
@RequestMapping("/api")//声明这个类里的所有接口地址都会以/api开头
public class HelloController {  //该公共类通过前面的注解赋予了处理HTTP请求的能力

    @GetMapping("/hello") //将下面的方法映射为处理HTTP GET请求
    public Result<Void> hello() {
        return Result.success();
    } //这个方法会构建一个包含状态码、消息且data为null的JSON结构
}



//这个HelloController的作用就是提供一个最简单的测试接口，当你访问/api/hello时，返回一个统一的成功响应，表示服务正在正常运行，根service层没关系