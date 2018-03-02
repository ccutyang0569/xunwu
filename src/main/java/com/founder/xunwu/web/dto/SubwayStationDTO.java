package com.founder.xunwu.web.dto;

/**
 * @program: xunwu
 * @description: 地铁线路站台DTO类
 * @author: YangMing
 * @create: 2018-02-13 21:17
 **/
public class SubwayStationDTO {


    private Long id;
    private Long subwayId;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubwayId() {
        return subwayId;
    }

    public void setSubwayId(Long subwayId) {
        this.subwayId = subwayId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
