package com.founder.xunwu.security;

import com.founder.xunwu.entity.User;
import com.founder.xunwu.service.IuserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @program: xunwu
 * @description: 自定义认证类
 * @author: YangMing
 * @create: 2018-01-30 20:18
 **/
public class AuthProvider implements AuthenticationProvider {
    @Autowired
    IuserService  userService;

    private Md5PasswordEncoder passwordEncoder=new Md5PasswordEncoder();
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String name = authentication.getName();
        String password =(String) authentication.getCredentials();
        User user = userService.findUserByName(name);
        if(user==null){
            throw new AuthenticationCredentialsNotFoundException("authError");
        }
        if(this.passwordEncoder.isPasswordValid(user.getPassword(),password,user.getId())){
                return new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
        }

        throw new BadCredentialsException("authError");
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
