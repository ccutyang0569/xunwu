package com.founder.xunwu.repository;

import com.founder.xunwu.entity.House;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * @program: xunwu
 * @description:
 * @author: yangming
 * @create: 2018-02-13 23:55
 **/
public interface HouseRespository  extends PagingAndSortingRepository<House, Long>, JpaSpecificationExecutor<House> {

     @Modifying
     @Query("update House as house set house.cover=:cover where house.id=:id")
     void updateCover(@Param(value="id") Long id,@Param(value="cover") Long cover);

     @Modifying
     @Query("update House as house set  house.status=:status where house.id=:id")
     void updateStatus(@Param(value="id") Long id,@Param(value="status") int status);

     @Modifying
    @Query("update House as house set house.watchTimes = house.watchTimes + 1 where house.id = :id")
     void updateWatchTimes(@Param(value="id") Long houseId);




}
