package com.founder.xunwu.web.dto;

/**
 * @program: xunwu
 * @description: SubwayDTO
 * @author: YangMing
 * @create: 2018-02-07 23:36
 **/
public class SubwayDTO {

    private Long id ;
    private String name;
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
