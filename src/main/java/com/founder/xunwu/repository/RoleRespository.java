package com.founder.xunwu.repository;

import com.founder.xunwu.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @program: xunwu
 * @description: 角色dao
 * @author: yangming
 * @create: 2018-01-30 20:55
 **/
public interface RoleRespository extends CrudRepository<Role,Long> {


          List<Role> findRolesByUserId(Long userId);
}
