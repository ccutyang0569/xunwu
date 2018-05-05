package com.founder.xunwu.service.search;

import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.web.form.MapSearch;
import com.founder.xunwu.web.form.RentSearch;

import java.util.List;

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

    /**
     * 获取补全关键词
     * @param prefix
     * @return
     */
    ServiceResult<List<String>> suggest(String prefix);

    ServiceResult<Long> aggregateDistrictHouse(String cityEnName,String regionEnName,String district);


    /**
     * 聚合城市数据
     * @param cityEnName
     * @return
     */
    ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName);

    /**
     * 城市级别查询
     * @param cityEnName
     * @param orderBy
     * @param orderDirection
     * @param start
     * @param size
     * @return
     */
    ServiceMultiResult<Long> mapQuery(String cityEnName, String orderBy, String orderDirection, int start, int size);

    /**
     *精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<Long> mapQuery(MapSearch mapSearch);
}
