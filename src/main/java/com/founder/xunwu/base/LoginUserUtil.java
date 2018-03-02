package com.founder.xunwu.base;

import com.founder.xunwu.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @program: xunwu
 * @description: 在线用户工具类
 * @author: YangMing
 * @create: 2018-02-14 00:09
 **/
public class LoginUserUtil {


    public static User load(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal!=null&&principal instanceof  User){
            return (User) principal;
        }
        return  null;
    }

   public  static Long getLoginUserId(){
       User user = load();
       if(user==null){
           return -1L;
       }
       return user.getId();
   }



}
