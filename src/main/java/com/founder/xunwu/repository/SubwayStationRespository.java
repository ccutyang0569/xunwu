package com.founder.xunwu.repository;

import com.founder.xunwu.entity.SubwayStation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @program: xunwu
 * @description: 查询地铁线路站台dao Interface
 * @author: yangming
 * @create: 2018-02-13 21:12
 **/
public interface SubwayStationRespository  extends CrudRepository<SubwayStation,Long>{


    List<SubwayStation> findAllBySubWayId(Long subwayId);



}
