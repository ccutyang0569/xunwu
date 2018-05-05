package com.founder.xunwu.service.house;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.founder.xunwu.entity.Subway;
import com.founder.xunwu.entity.SubwayStation;
import com.founder.xunwu.entity.SupportAddress;
import com.founder.xunwu.repository.SubwayRepository;
import com.founder.xunwu.repository.SubwayStationRespository;
import com.founder.xunwu.repository.SupportAddressRepository;
import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.service.search.BaiduMapLocation;
import com.founder.xunwu.web.dto.SubwayDTO;
import com.founder.xunwu.web.dto.SubwayStationDTO;
import com.founder.xunwu.web.dto.SupportAddressDTO;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    @Autowired
    private ObjectMapper objectMapper;
    @Value("${baidu.map.geotableId}")
    private String geoTableId="1000003773";
    @Value("${baidu.map.javaServer.ak}")
    private  String BAIDU_MAP_KEY="VH4n7HtgbHBmDP46Qmdnn75nNRms5w91";
    @Value("${baidu.map.javaServer.sn}")
    private  String BAIDU_MAP_KEY_SN="Q9XfPbgcfZlatatUN50CPxVw9jeUckDj";

    private static final String BAIDU_MAP_GEOCONV_API="http://api.map.baidu.com/geocoder/v2/?";

    /**
     * POI数据管理接口
     */
    private static final String LBS_CREATE_API = "http://api.map.baidu.com/geodata/v3/poi/create";

    private static final String LBS_QUERY_API = "http://api.map.baidu.com/geodata/v3/poi/list?";

    private static final String LBS_UPDATE_API = "http://api.map.baidu.com/geodata/v3/poi/update";

    private static final String LBS_DELETE_API = "http://api.map.baidu.com/geodata/v3/poi/delete";


    private static final Logger logger=LoggerFactory.getLogger(IAddressService.class);
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

    @Override
    public ServiceResult<BaiduMapLocation> getBaiduMapLocation(String city, String address) {
         String encodeAddress;
         String encodeCity;
        try {
            encodeAddress = URLEncoder.encode(address,"UTF-8");
            encodeCity = URLEncoder.encode(city, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error to encode house address",e);
           return new ServiceResult<BaiduMapLocation>(false,"Error to encode house address");
        }
        HttpClient httpClient=HttpClients.createDefault();
        StringBuilder sb = new StringBuilder(BAIDU_MAP_GEOCONV_API);
        sb.append("address=").append(encodeAddress).append("&")
                .append("city=").append(encodeCity).append("&")
                .append("output=json&")
                .append("ak=").append(BAIDU_MAP_KEY);
        HttpGet httpGet = new HttpGet(sb.toString());


        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return new ServiceResult<BaiduMapLocation>(false, "Can not get baidu map location");
            }
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if(status!=0){
                return new ServiceResult<BaiduMapLocation>(false, "Error to get baidu map for status " + status);
            }else{
                BaiduMapLocation location=new BaiduMapLocation();
                JsonNode jsonLocation = jsonNode.get("result").get("location");
                location.setLongitude(jsonLocation.get("lng").asDouble());
                location.setLatitude(jsonLocation.get("lat").asDouble());
                return ServiceResult.of(location);
            }

        } catch (IOException e) {
            logger.error("Error to fetch baidu map api" + e);
            return  new ServiceResult<BaiduMapLocation>(false,"Error to fetch baidu map api" + e);
        }



    }

    @Override
    public ServiceResult lbsUpload(BaiduMapLocation location, String title, String address, long houseId, int price, int area) {
        HttpClient httpClient = HttpClients.createDefault();
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("latitude", String.valueOf(location.getLatitude())));
        nvps.add(new BasicNameValuePair("longitude", String.valueOf(location.getLongitude())));
        //百度坐标系
        nvps.add(new BasicNameValuePair("coord_type", "3"));

        nvps.add(new BasicNameValuePair("geotable_id", geoTableId));
        nvps.add(new BasicNameValuePair("ak", BAIDU_MAP_KEY));
        nvps.add(new BasicNameValuePair("houseId", String.valueOf(houseId)));
        nvps.add(new BasicNameValuePair("price", String.valueOf(price)));
        nvps.add(new BasicNameValuePair("area", String.valueOf(area)));
        nvps.add(new BasicNameValuePair("title", title));
        nvps.add(new BasicNameValuePair("address", address));

        HttpPost post;
        if(isLbsDataExists(houseId)){
            post = new HttpPost(LBS_UPDATE_API);
        }else{
            post=new HttpPost(LBS_CREATE_API);
        }

        try {
            post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
            HttpResponse response = httpClient.execute(post);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Can not upload lbs data for response:" + result);
                return new ServiceResult(false, "Can not upload baidu lbs data");
            }else{
                JsonNode jsonNode = objectMapper.readTree(result);
                int status = jsonNode.get("status").asInt();
                if(status!=0){
                    String message = jsonNode.get("message").asText();
                    logger.error("Error to upload lbs data for status:{},and message:{}", status, message);

                    return new ServiceResult(false,"Error to upload lbs data");


                }else{
                    return ServiceResult.success();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return new ServiceResult(false);
    }

    /**
     * method_name: isLbsDataExists
     * param: [houseId]
     * return: boolean
     * describe: TODO(百度LBS数据是否已经存在)
     * create_user: YangMing
     * create_date: 2018/5/5 20:18
     **/
    private boolean isLbsDataExists(long houseId) {
        HttpClient httpClient = HttpClients.createDefault();
        StringBuilder sb = new StringBuilder(LBS_QUERY_API);
        sb.append("geotable_id=").append(geoTableId).append("&")
                .append("ak=").append(BAIDU_MAP_KEY).append("&")
                .append("houseId=").append(houseId).append(",").append(houseId);
        HttpGet httpGet = new HttpGet(sb.toString());
        try {
            HttpResponse response = httpClient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Can not get lbs data for response: " + result);
                return false;
            }
            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if (status != 0) {
                logger.error("Error to get lbs data for status: " + status);
                return false;
            } else {
                long size = jsonNode.get("size").asLong();
                if (size > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return  false;
        }


    }


    @Override
    public ServiceResult removeLbs(Long houseId) {
        HttpClient httpClient = HttpClients.createDefault();
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("geotable_id", geoTableId));
        nvps.add(new BasicNameValuePair("ak", BAIDU_MAP_KEY));
        nvps.add(new BasicNameValuePair("houseId", String.valueOf(houseId)));

        HttpPost delete = new HttpPost(LBS_DELETE_API);
        try {
            delete.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            HttpResponse response = httpClient.execute(delete);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Error to delete lbs data for response: " + result);
                return new ServiceResult(false);
            }

            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if (status != 0) {
                String message = jsonNode.get("message").asText();
                logger.error("Error to delete lbs data for message: " + message);
                return new ServiceResult(false, "Error to delete lbs data for: " + message);
            }
            return ServiceResult.success();
        } catch (IOException e) {
            logger.error("Error to delete lbs data.", e);
            return new ServiceResult(false);
        }
    }
}



