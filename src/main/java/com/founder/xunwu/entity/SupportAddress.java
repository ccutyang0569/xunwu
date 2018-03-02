package com.founder.xunwu.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @program: xunwu
 * @description: 支持的房源类
 * @author: YangMing
 * @create: 2018-02-06 22:55
 **/
@Entity
@Table(name="support_address")
public class SupportAddress implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="belong_to")
    private String belongTo;

    @Column(name="cn_name")
    private String cnName;

    @Column(name="en_name")
    private String enName;

    private String level;
    @Column(name="baidu_map_lng")
    private double baiduMapLongitude;

    @Column(name="baidu_map_lat")
    private double baiduMaplatitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public double getBaiduMapLongitude() {
        return baiduMapLongitude;
    }

    public void setBaiduMapLongitude(double baiduMapLongitude) {
        this.baiduMapLongitude = baiduMapLongitude;
    }

    public double getBaiduMaplatitude() {
        return baiduMaplatitude;
    }

    public void setBaiduMaplatitude(double baiduMaplatitude) {
        this.baiduMaplatitude = baiduMaplatitude;
    }


    public enum Level{
        CITY("city"),
        REGION("region");
          private String value;

        Level(String value) {
            this.value = value;

        }
        public String getValue(){
            return this.value;
        }

        public static Level of(String value) {
            for (Level level : Level.values()) {
                if (level.getValue().equals(value)) {
                    return level;
                }

            }
            throw new IllegalArgumentException();
        }
    }
}
