package com.founder.xunwu.repository;

import com.founder.xunwu.entity.HouseDetail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @program: xunwu
 * @description: 房屋详情dao
 * @author: yangming
 * @create: 2018-02-13 23:56
 **/
public interface HouseDetailRespository extends CrudRepository<HouseDetail,Long> {
    HouseDetail findByHouseId(Long houseId);

    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);

}
