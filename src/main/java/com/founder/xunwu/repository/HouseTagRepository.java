package com.founder.xunwu.repository;

import com.founder.xunwu.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @program: xunwu
 * @description:
 * @author: yangming
 * @create: 2018-02-14 15:20
 **/
public interface HouseTagRepository extends CrudRepository<HouseTag,Long>{
    HouseTag findByNameAndHouseId(String name, Long houseId);

    List<HouseTag> findAllByHouseId(Long id);

    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);

}
