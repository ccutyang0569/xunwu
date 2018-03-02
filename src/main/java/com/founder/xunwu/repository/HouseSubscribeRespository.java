package com.founder.xunwu.repository;

import com.founder.xunwu.entity.HouseSubscribe;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @program: xunwu
 * @description:
 * @author: yangming
 * @create: 2018-02-21 16:43
 **/
public interface HouseSubscribeRespository extends PagingAndSortingRepository<HouseSubscribe, Long> {


    HouseSubscribe findByHouseIdAndUserId(Long id, Long loginUserId);
}
