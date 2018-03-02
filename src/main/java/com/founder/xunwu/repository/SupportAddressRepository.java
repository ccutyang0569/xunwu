package com.founder.xunwu.repository;

import com.founder.xunwu.entity.SupportAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @program: xunwu
 * @description: 支持房源dao
 * @author: yangming
 * @create: 2018-02-06 23:31
 **/

public interface SupportAddressRepository extends CrudRepository<SupportAddress,Long> {

    /**
     * 获取对应级别的房源信息
     * @param level
     * @return
     */
    List<SupportAddress>   findAllByLevel(String level);

    /**
     * 获取城市对应的区域
     * @param level
     * @param belongTo
     * @return
     */
    List<SupportAddress> findAllByLevelAndBelongTo(String level, String belongTo);

    /**
     * 获取城市英文名字与级别对应城市的具体信息
     * @param cityEnName
     * @param value
     * @return
     */
    SupportAddress findByEnNameAndLevel(String cityEnName, String value);

    /**
     * 获取城市的归属信息
     * @param regionEnName
     * @param enName
     * @return
     */
    SupportAddress findByEnNameAndBelongTo(String regionEnName, String enName);
}
