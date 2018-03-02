package com.founder.xunwu.entity;

import javax.persistence.*;

/**
 * @program: xunwu
 * @description: 地铁线路站台类
 * @author: YangMing
 * @create: 2018-02-13 21:07
 **/
@Entity
@Table(name="subway_station")
public class SubwayStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="subway_id")
    private Long subWayId;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubWayId() {
        return subWayId;
    }

    public void setSubWayId(Long subWayId) {
        this.subWayId = subWayId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
