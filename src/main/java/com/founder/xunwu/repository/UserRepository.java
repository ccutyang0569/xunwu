package com.founder.xunwu.repository;

import com.founder.xunwu.entity.User;
import org.springframework.data.repository.CrudRepository;

/**
 * @program: xunwu
 * @description: JPA USER CRUD
 * @author: yangming
 * @create: 2018-01-28 15:50
 **/

public interface UserRepository extends CrudRepository<User,Long> {

       User findByName(String userName);


    User findUserByPhoneNumber(String telephone);
}
