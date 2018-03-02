package com.founder.xunwu.service.house;

import com.founder.xunwu.entity.Subway;
import com.founder.xunwu.entity.SubwayStation;
import com.founder.xunwu.entity.SupportAddress;
import com.founder.xunwu.repository.SubwayRepository;
import com.founder.xunwu.repository.SubwayStationRespository;
import com.founder.xunwu.repository.SupportAddressRepository;
import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.web.dto.SubwayDTO;
import com.founder.xunwu.web.dto.SubwayStationDTO;
import com.founder.xunwu.web.dto.SupportAddressDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: xunwu
 * @description: 地址接口实现类
 * @author: YangMing
 * @create: 2018-02-06 23:40
 **/
@Service
public class AddressServiceImpl implements IAddressService {


    @Autowired
    private SupportAddressRepository supportAddressRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SubwayRepository subwayRepository;


    @Autowired
    private SubwayStationRespository subwayStationRespository;

    /**
     * method_name: findALLCities
     * param: [level]
     * return: com.founder.xunwu.service.ServiceMultiResult<com.founder.xunwu.web.dto.SupportAddressDTO>
     * describe: TODO(查询对应行政级别的地址)
     * create_user: YangMing
     * create_date: 2018/2/6 23:41
     **/
    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllCities() throws Exception {
        List<SupportAddress> address = supportAddressRepository.findAllByLevel(SupportAddress.Level.CITY.getValue());
        List<SupportAddressDTO> addressDTOS = new ArrayList<>();
        for (SupportAddress supportAddress : address) {
            SupportAddressDTO supportAddressDTO = modelMapper.map(supportAddress, SupportAddressDTO.class);
            addressDTOS.add(supportAddressDTO);
        }
        return new ServiceMultiResult<>(addressDTOS.size(), addressDTOS);
    }

    /**
     * method_name: findAllRegionsByCityName
     * param: [cityName]
     * return: com.founder.xunwu.service.ServiceMultiResult<com.founder.xunwu.web.dto.SupportAddressDTO>
     * describe: TODO()
     * create_user: YangMing
     * create_date: 2018/2/7 23:40
     **/
    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName) throws Exception {
        if (cityName == null) {
            return new ServiceMultiResult<SupportAddressDTO>(0, null);

        }
        List<SupportAddressDTO> result = new ArrayList<>();
        List<SupportAddress> regions = supportAddressRepository.findAllByLevelAndBelongTo(SupportAddress.Level.REGION.getValue(), cityName);
        for (SupportAddress region : regions) {
            result.add(modelMapper.map(region, SupportAddressDTO.class));

        }
        return new ServiceMultiResult<SupportAddressDTO>(regions.size(), result);

    }

    /**
     * method_name: findSubwaysByCity
     * param: [cityName]
     * return: com.founder.xunwu.service.ServiceMultiResult<com.founder.xunwu.web.dto.SubwayDTO>
     * describe: TODO(获取城市的地铁线路)
     * create_user: YangMing
     * create_date: 2018/2/7 23:42
     **/
    @Override
    public ServiceMultiResult<SubwayDTO> findSubwaysByCity(String cityName) throws Exception {

        if (cityName == null) {
            return new ServiceMultiResult<>(0, null);

        }
        List<SubwayDTO> result = new ArrayList<>();
        List<Subway> subways = subwayRepository.findAllByCityEnName(cityName);
        for (Subway subway : subways) {
            result.add(modelMapper.map(subway, SubwayDTO.class));

        }

        return new ServiceMultiResult<>(subways.size(), result);
    }

    /**
     * method_name: findAllBySubWayId
     * param: [subwayId]
     * return: com.founder.xunwu.service.ServiceMultiResult<com.founder.xunwu.web.dto.SubwayStationDTO>
     * describe: TODO(获取地铁线路对应的站台)
     * create_user: YangMing
     * create_date: 2018/2/13 21:24
     **/

    @Override
    public ServiceMultiResult<SubwayStationDTO> findAllBySubWayId(Long subwayId) throws Exception {
        if (subwayId == null) {
            return new ServiceMultiResult<>(0, null);
        }
        List<SubwayStationDTO> result = new ArrayList<>();
        List<SubwayStation> subwayStations = subwayStationRespository.findAllBySubWayId(subwayId);
        for (SubwayStation subwayStation : subwayStations) {
            result.add(modelMapper.map(subwayStation, SubwayStationDTO.class));
        }

        return new ServiceMultiResult<>(result.size(), result);


    }

    @Override
    public Map<SupportAddress.Level, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName) {

        Map<SupportAddress.Level, SupportAddressDTO> result = new HashMap<>();
        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(cityEnName, SupportAddress.Level.CITY.getValue());
        SupportAddress region = supportAddressRepository.findByEnNameAndBelongTo(regionEnName, city.getEnName());
        result.put(SupportAddress.Level.CITY, modelMapper.map(city,SupportAddressDTO.class));
        result.put(SupportAddress.Level.REGION, modelMapper.map(region,SupportAddressDTO.class));

        return result;
    }

    @Override
    public ServiceResult<SubwayDTO> findSubway(Long subwayLineId) {

        if(subwayLineId==null){
            return ServiceResult.notFound();

        }

        Subway subway = subwayRepository.findOne(subwayLineId);
        if (subway == null) {
            return ServiceResult.notFound();
        }


        return ServiceResult.of(modelMapper.map(subway,SubwayDTO.class));
    }

    @Override
    public ServiceResult<SubwayStationDTO> findSubwayStation(Long subwayStationId) {
        if(subwayStationId==null){
            return ServiceResult.notFound();
        }
        SubwayStation subwayStation = subwayStationRespository.findOne(subwayStationId);
        if(subwayStation==null){
            return ServiceResult.notFound();
        }
        return ServiceResult.of(modelMapper.map(subwayStation,SubwayStationDTO.class));
    }

    @Override
    public ServiceResult<SupportAddressDTO> findCity(String cityEnName) {


        if (cityEnName == null) {
            return ServiceResult.notFound();
        }
        SupportAddress supportAddress = supportAddressRepository.findByEnNameAndLevel(cityEnName, SupportAddress.Level.CITY.getValue());
        if (supportAddress == null) {
            return ServiceResult.notFound();

        }
        SupportAddressDTO supportAddressDTO = modelMapper.map(supportAddress, SupportAddressDTO.class);
        return ServiceResult.of(supportAddressDTO);
    }


}
