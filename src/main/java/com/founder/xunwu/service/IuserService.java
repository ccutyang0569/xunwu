package com.founder.xunwu.service;

import com.founder.xunwu.entity.User;
import com.founder.xunwu.web.dto.UserDTO;

/**
 * @program: xunwu
 * @description: 用户服务接口
 * @author: yangming
 * @create: 2018-01-30 20:51
 **/
public interface IuserService  {



   User findUserByName(String userName);

    ServiceResult<UserDTO> findByid(Long adminId);
}
