package com.founder.xunwu.security;

import com.founder.xunwu.base.LoginUserUtil;
import com.founder.xunwu.entity.User;
import com.founder.xunwu.service.ISmsService;
import com.founder.xunwu.service.IUserService;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @program: xunwu
 * @description:
 * @author: YangMing
 * @create: 2018-03-27 22:48
 **/
public class AuthFilter extends UsernamePasswordAuthenticationFilter {


    @Autowired
    private IUserService userService;
    @Autowired
    private ISmsService  smsService;



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = obtainUsername(request);
        if(!Strings.isNullOrEmpty(username)){
            return super.attemptAuthentication(request, response);
        }

        String telephone = request.getParameter("telephone");

        if(Strings.isNullOrEmpty(telephone)&&!LoginUserUtil.checkTelephone(telephone)){
            throw new BadCredentialsException("Wrong telephone number");

        }
          User user   =userService.findUserByTelephone(telephone);

        String inputSmsCode = request.getParameter("smsCode");
        String sessionCode = smsService.getSmsCode(telephone);
        if(Objects.equals(inputSmsCode,sessionCode)){
            if(user==null){
                //用户第一次登陆,自动注册
                user=userService.addUserByPhone(telephone);
            }
            return new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());

        }else{
            throw new BadCredentialsException("smsCodeError");
        }



    }
}
