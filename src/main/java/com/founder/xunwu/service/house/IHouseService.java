package com.founder.xunwu.service.house;

import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.web.dto.HouseDTO;
import com.founder.xunwu.web.form.DatatableSearch;
import com.founder.xunwu.web.form.HouseForm;
import com.founder.xunwu.web.form.MapSearch;
import com.founder.xunwu.web.form.RentSearch;

/**
 * @program: xunwu
 * @description: 房屋DAO interface
 * @author: yangming
 * @create: 2018-02-13 23:54
 **/
public interface IHouseService  {
    /**
     * 保存房屋信息
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);

    ServiceResult<HouseDTO>  findCompleteOne(Long id);

    ServiceResult update(HouseForm houseForm);

    ServiceResult removePhoto(Long id);

    ServiceResult removeTag(Long houseId, String tag);

    ServiceResult addTag(Long houseId, String tag);

    ServiceResult updateStatus(Long id, int value);

    ServiceResult updateCover(Long coverId, Long targetId);

    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);

    /**
     * 全地图查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch);

    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<HouseDTO>boundMapQuery(MapSearch mapSearch);

}
