package com.founder.xunwu.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: xunwu
 * @description: 公共路径
 * @author: YangMing
 * @create: 2018-01-28 18:48
 **/
@Controller
public class HomeController {

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/404")
    public String notFoundPage() {
        return "404";
    }

    @GetMapping("/403")
    public String accessError() {
        return "403";
    }

    @GetMapping("/500")
    public String internalError() {
        return "500";
    }

    @GetMapping("/logout/page")
    public String logoutPage() {
        return "logout";
    }

}
