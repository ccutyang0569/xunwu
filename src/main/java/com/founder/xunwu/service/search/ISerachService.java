package com.founder.xunwu.service.search;

/**
 * @program: xunwu
 * @description:
 * @author: yangming
 * @create: 2018-03-03 13:09
 **/
public interface ISerachService {

    /**
     * 索引房源目标
     * @param houseId
     */

    void index(Long houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void  remove(Long houseId);



}
