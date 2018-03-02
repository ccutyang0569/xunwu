package com.founder.xunwu.service.user;

import com.founder.xunwu.entity.Role;
import com.founder.xunwu.entity.User;
import com.founder.xunwu.repository.RoleRespository;
import com.founder.xunwu.repository.UserRepository;
import com.founder.xunwu.service.IuserService;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.web.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: xunwu
 * @description: 用户服务实现类
 * @author: YangMing
 * @create: 2018-01-30 20:53
 **/
@Service
public class UserServiceImpl implements IuserService {
    @Autowired
    private  UserRepository userResPository;
    @Autowired
    private RoleRespository  roleRespository;

    @Autowired
    private ModelMapper  modelMapper;

    @Override
    public User findUserByName(String userName){
        User user = userResPository.findByName(userName);
        if(user==null){
             return null;
        }
        List<Role> userRoles = roleRespository.findRolesByUserId(user.getId());
        if(userRoles==null&&userRoles.isEmpty()){

            throw new DisabledException("非法权限");
        }
        List<GrantedAuthority> authorityList=new ArrayList<>(12);
        //java8 Lambda
        userRoles.forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_"+role.getName())));
        user.setAuthorityList(authorityList);
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findByid(Long adminId) {
        User user = userResPository.findOne(adminId);
        if (user == null) {
            return ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }
}
