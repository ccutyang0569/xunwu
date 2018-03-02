package com.founder.xunwu.repository;

import com.founder.xunwu.entity.Subway;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @program: xunwu
 * @description: 地铁dao
 * @author: yangming
 * @create: 2018-02-07 23:27
 **/
public interface SubwayRepository extends CrudRepository<Subway,Long> {

    /**
     * 获取城市的地铁线路
     * @param cityName
     * @return
     */
    List<Subway> findAllByCityEnName(String cityName);
}
