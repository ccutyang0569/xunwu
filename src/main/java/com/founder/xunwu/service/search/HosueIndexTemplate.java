package com.founder.xunwu.service.search;

import java.util.Date;
import java.util.List;

/**
 * @program: xunwu
 * @description: 房源索引模板结构体
 * @author: YangMing
 * @create: 2018-03-02 22:56
 **/
public class HosueIndexTemplate {
    private Long houseId;


    private String title;


    private int price;

    private int area;

    private Date createTime;


    private Date lastUpdateTime;

    private String cityEnName;


    private String regionEnName;

    private int direction;

    private int distanceToSubway;

    private String subwayLineName;

    private String  subwayStationName;

    private String  street;

    private String district;

    private String description;

    private String  layoutDesc;


    private String traffic;


    private String  roundService;

    private int rentWay;

    private List<String> tags;
    private List<HouseSuggest> sugget;

    private BaiduMapLocation location;





}
