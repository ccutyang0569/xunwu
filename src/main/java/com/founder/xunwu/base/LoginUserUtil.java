package com.founder.xunwu.base;

import com.founder.xunwu.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.regex.Pattern;

/**
 * @program: xunwu
 * @description: 在线用户工具类
 * @author: YangMing
 * @create: 2018-02-14 00:09
 **/
public class LoginUserUtil {
    //手机号正则
    private static final String PHONE_REGEX="^((13[0-9])|(14[5|7])|(15([0-9]))|(18[0,5-9]))\\d{8}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    //常用邮箱正则
    private static final String EMAIL_REGEX="^[a_zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

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

    /**
     * 验证手机号
     * @param target
     * @return
     */
   public static boolean checkTelephone(String target){
       return PHONE_PATTERN.matcher(target).matches();

   }

    /**
     * 验证常用的邮箱
     * @param target
     * @return
     */
   public static   boolean checkEmail(String target){
       return EMAIL_PATTERN.matcher(target).matches();
   }
}
