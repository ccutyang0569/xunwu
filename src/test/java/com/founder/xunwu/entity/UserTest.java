package com.founder.xunwu.entity;

import com.founder.xunwu.XunwuApplicationTests;
import com.founder.xunwu.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: xunwu
 * @description: user 测试类
 * @author: YangMing
 * @create: 2018-01-28 15:55
 **/
public class UserTest extends XunwuApplicationTests {


      @Autowired
      private  UserRepository   userRepository;
      @Test
      public   void userTest01(){
          User user = userRepository.findOne(1L);
          System.out.println(user.getId()+"++++++++++++++++++++");


      }
}

