package com.founder.xunwu.repository;

import com.founder.xunwu.entity.HousePicture;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @program: xunwu
 * @description: 房屋图片dao
 * @author: yangming
 * @create: 2018-02-14 15:04
 **/
public interface HousePictureRepository extends CrudRepository<HousePicture,Long> {


    List<HousePicture> findAllByHouseId(Long id);


}
