package com.founder.xunwu.service.house;

import com.founder.xunwu.entity.SupportAddress;
import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.service.search.BaiduMapLocation;
import com.founder.xunwu.web.dto.SubwayDTO;
import com.founder.xunwu.web.dto.SubwayStationDTO;
import com.founder.xunwu.web.dto.SupportAddressDTO;

import java.util.Map;

/**
 * @program: xunwu
 * @description: 地址服务接口
 * @author: yangming
 * @create: 2018-02-06 23:36
 **/
public interface IAddressService {

    ServiceMultiResult<SupportAddressDTO> findAllCities() throws Exception;

    ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName) throws Exception;

    ServiceMultiResult<SubwayDTO> findSubwaysByCity(String cityName) throws Exception;

    ServiceMultiResult<SubwayStationDTO> findAllBySubWayId(Long subwayId) throws Exception;

    /**
     * 根据英文简写获取具体区域的信息
     *
     * @param cityEnName
     * @param regionEnName
     * @return
     */
    Map<SupportAddress.Level, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName);


    ServiceResult<SubwayDTO> findSubway(Long subwayLineId);

    ServiceResult<SubwayStationDTO> findSubwayStation(Long subwayStationId);

    ServiceResult<SupportAddressDTO> findCity(String cityEnName);

    /**
     * 根据城市具体地理位置获取百度地图的经纬度
     * @param city
     * @param address
     * @return
     */
    ServiceResult<BaiduMapLocation> getBaiduMapLocation(String city,String address);

    /**
     *上传百度LBS数据
     * @param location
     * @param title
     * @param address
     * @param houseId
     * @param price
     * @param area
     * @return
     */
    ServiceResult lbsUpload(BaiduMapLocation location, String title, String address,
                            long houseId, int price, int area);

    /**
     * 移除百度LBS数据
     * @param houseId
     * @return
     */
    ServiceResult removeLbs(Long houseId);

}
