package com.founder.xunwu.service.user;

import com.founder.xunwu.entity.Role;
import com.founder.xunwu.entity.User;
import com.founder.xunwu.repository.RoleRespository;
import com.founder.xunwu.repository.UserRepository;
import com.founder.xunwu.service.IUserService;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.web.dto.UserDTO;
import com.google.common.collect.Lists;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @program: xunwu
 * @description: 用户服务实现类
 * @author: YangMing
 * @create: 2018-01-30 20:53
 **/
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private RoleRespository  roleRepository;

    @Autowired
    private ModelMapper  modelMapper;

    @Override
    public User findUserByName(String userName){
        User user = userRepository.findByName(userName);
        if(user==null){
             return null;
        }
        List<Role> userRoles = roleRepository.findRolesByUserId(user.getId());
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
        User user = userRepository.findOne(adminId);
        if (user == null) {
            return ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }

    @Override
    public User findUserByTelephone(String telephone) {


        User user =userRepository.findUserByPhoneNumber(telephone);
        if (user == null) {
            return null;
        }
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles==null||roles.isEmpty()){

            throw new DisabledException("非法权限");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_"+role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User addUserByPhone(String telephone) {
        User user = new User();
        user.setPhoneNumber(telephone);
        user.setName(telephone.substring(0, 3) + "****" + telephone.substring(7, telephone.length()));
        Date now = new Date();
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        user.setLastUpdateTime(now);
        user = userRepository.save(user);

        Role role = new Role();
        role.setName("USER");
        role.setUserId(user.getId());
        roleRepository.save(role);
        user.setAuthorityList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
        return user;
    }
}
