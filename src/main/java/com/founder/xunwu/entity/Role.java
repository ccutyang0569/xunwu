package com.founder.xunwu.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @program: xunwu
 * @description: 角色
 * @author: YangMing
 * @create: 2018-01-30 20:47
 **/
@Entity
@Table(name="role")
public class Role implements Serializable {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     @Column(name="user_id")
     private Long userId;
     private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
