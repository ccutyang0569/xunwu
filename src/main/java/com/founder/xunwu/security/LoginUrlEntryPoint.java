package com.founder.xunwu.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * @program: xunwu
 * @description: 基于角色的登录入口控制器
 * @author: YangMing
 * @create: 2018-02-03 17:12
 **/
public class LoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {


    private PathMatcher pathMatcher=new AntPathMatcher();
    private final Map <String,String> authEntityPointMap;

    public LoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
        authEntityPointMap=new HashMap<>(12);
        authEntityPointMap.put("/user/**","/user/login");
        authEntityPointMap.put("/admin/**","/admin/login");
    }
    /**
     * method_name: determineUrlToUseForThisRequest
     * param: [request, response, exception]
     * return: java.lang.String
     * describe: TODO(根据请求跳转到指定的页面，父类默认使用LoginFormUrl)
     * create_user: YangMing
     * create_date: 2018/2/3 17:49
     **/
    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        String uri = request.getRequestURI().replace(request.getContextPath(), "");

        for(Map.Entry<String,String> authEntry :this.authEntityPointMap.entrySet()){

          if(this.pathMatcher.match(authEntry.getKey(),uri)){
                     return authEntry.getValue();
          }

        }
        return super.determineUrlToUseForThisRequest(request, response, exception);

        
        
    }
}
