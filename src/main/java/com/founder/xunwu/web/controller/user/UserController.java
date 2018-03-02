package com.founder.xunwu.web.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: xunwu
 * @description: 用户Controller
 * @author: YangMing
 * @create: 2018-02-03 17:01
 **/
@Controller
public class UserController {

    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/user/center")
    public String centerPage() {
        return "user/center";
    }
}
