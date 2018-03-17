package com.founder.xunwu.service.search;

import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.web.form.RentSearch;

/**
 * @program: xunwu
 * @description:
 * @author: yangming
 * @create: 2018-03-03 13:09
 **/
public interface ISearchService {

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

    /**
     * 查询房源接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);



}
