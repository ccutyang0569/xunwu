package com.founder.xunwu.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @program: xunwu
 * @description: 登录失败验证处理器
 * @author: YangMing
 * @create: 2018-02-04 08:57
 **/
public class LoginAuthFailHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginUrlEntryPoint loginUrlEntityPoint;

    public LoginAuthFailHandler( LoginUrlEntryPoint loginUrlEntryPoint) {

        this.loginUrlEntityPoint = loginUrlEntryPoint;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String targetUrl = loginUrlEntityPoint.determineUrlToUseForThisRequest(request, response, exception);
        targetUrl+="?"+exception.getMessage();
        super.setDefaultFailureUrl(targetUrl);
        super.onAuthenticationFailure(request, response, exception);
    }
}
