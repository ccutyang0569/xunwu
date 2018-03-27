package com.founder.xunwu.web.controller;

import com.founder.xunwu.base.ApiResponse;
import com.founder.xunwu.base.LoginUserUtil;
import com.founder.xunwu.service.ISmsService;
import com.founder.xunwu.service.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: xunwu
 * @description: 公共路径
 * @author: YangMing
 * @create: 2018-01-28 18:48
 **/
@Controller
public class HomeController {
    @Autowired
    private ISmsService smsServive;

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

    @GetMapping("sms/code")
    @ResponseBody
    public ApiResponse smsCode(@RequestParam(value="telephone") String telephone){
        if(!LoginUserUtil.checkTelephone(telephone)){
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "请输入正确的手机号码！");
        }

        ServiceResult<String> result = smsServive.sendSms(telephone);
          if(result.isSuccess()){
              return ApiResponse.ofSuccess("");

          }else{
              return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
          }


    }

}
