package com.founder.xunwu.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @program: xunwu
 * @description:
 * @author: YangMing
 * @create: 2018-02-07 23:28
 **/
@Entity
@Table(name="subway")
public class Subway implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name="city_en_name")
    private String cityEnName;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCityEnName() {
        return cityEnName;
    }

    public void setCityEnName(String cityEnName) {
        this.cityEnName = cityEnName;
    }
}
