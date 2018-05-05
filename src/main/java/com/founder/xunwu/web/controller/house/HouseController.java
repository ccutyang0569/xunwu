package com.founder.xunwu.web.controller.house;

import com.founder.xunwu.base.ApiResponse;
import com.founder.xunwu.base.RentValueBlock;
import com.founder.xunwu.entity.SupportAddress;
import com.founder.xunwu.service.IUserService;
import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.service.house.IAddressService;
import com.founder.xunwu.service.house.IHouseService;
import com.founder.xunwu.service.search.HouseBucketDTO;
import com.founder.xunwu.service.search.ISearchService;
import com.founder.xunwu.web.dto.*;
import com.founder.xunwu.web.form.MapSearch;
import com.founder.xunwu.web.form.RentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * @program: xunwu
 * @description: houseController
 * @author: YangMing
 * @create: 2018-02-06 22:50
 **/
@Controller
public class HouseController {

    @Autowired
    private IAddressService addressService;
    @Autowired
    private IHouseService houseService;

    @Autowired
    private IUserService userService;
    @Autowired
    private ISearchService searchService;


    /**
     * method_name: rentMapHouses
     * param: [mapSearch]
     * return: com.founder.xunwu.base.ApiResponse
     * describe: TODO()
     * create_user: YangMing
     * create_date: 2018/5/5 15:30
     **/
    @GetMapping("rent/house/map/houses")
    @ResponseBody
    public ApiResponse rentMapHouses(@ModelAttribute MapSearch mapSearch) {
        if (mapSearch.getCityEnName()==null){
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须选择城市");
        }
        ServiceMultiResult<HouseDTO> serviceMultiResult=null;
        //百度地图缩放级别12
        if (mapSearch.getLevel()<13){
            serviceMultiResult=houseService.wholeMapQuery(mapSearch);

        }else{
            //小地图查询必须传递地图的参数
            serviceMultiResult=houseService.boundMapQuery(mapSearch);
        }

        ApiResponse response = ApiResponse.ofSuccess(serviceMultiResult.getResult());

        response.setMore(serviceMultiResult.getTotal()>(mapSearch.getStart() + mapSearch.getSize()));

        return response;

    }



    @GetMapping("rent/house/map")
    public String rentMapPage(@RequestParam(value="cityEnName") String cityEnName,
                              Model model,HttpSession session,
                              RedirectAttributes redirectAttributes) throws Exception {

        ServiceResult<SupportAddressDTO> city = addressService.findCity(cityEnName);
        //校验城市是否合法
        if (!city.isSuccess()){
            redirectAttributes.addAttribute("msg","must_chose_city");
            return "redirect:/index";
        }else {
            session.setAttribute("cityEnName",cityEnName);
            model.addAttribute("city", city.getResult());
        }
        //全部区域
        ServiceMultiResult<SupportAddressDTO> regions= addressService.findAllRegionsByCityName(cityEnName);
        //
        ServiceMultiResult<HouseBucketDTO> serviceResult=searchService.mapAggregate(cityEnName);
        model.addAttribute("aggData", serviceResult.getResult());
        model.addAttribute("total", serviceResult.getTotal());
        model.addAttribute("regions", regions.getResult());
        return "rent-map";
    }

   @GetMapping("rent/house/autocomplete")
   @ResponseBody
   public ApiResponse autocomplete(@RequestParam(value="prefix") String prefix){
        if(prefix.isEmpty()){
           return  ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
       }
       ServiceResult<List<String>> result= this.searchService.suggest(prefix);

       return ApiResponse.ofSuccess(result.getResult());

   }


    @ResponseBody
    @GetMapping("address/support/cities")
    public ApiResponse getSupportCities() {
        ServiceMultiResult<SupportAddressDTO> result;
        try {
            result = addressService.findAllCities();
            if (result.getResultSize() == 0) {
                return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
            }
            return ApiResponse.ofSuccess(result.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
    }

    @ResponseBody
    @GetMapping("address/support/regions")
    public ApiResponse getSupportRegions(@RequestParam("city_name") String cityName) throws Exception {

        ServiceMultiResult<SupportAddressDTO> regions = addressService.findAllRegionsByCityName(cityName);
        if (regions.getResult() == null || regions.getTotal() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);

        }

        return ApiResponse.ofSuccess(regions.getResult());

    }


    /**
     * method_name: getSubwayByCityName
     * param: [cityName]
     * return: com.founder.xunwu.base.ApiResponse
     * describe: TODO(获取城市支持的地铁线路)
     * create_user: YangMing
     * create_date: 2018/2/7 23:54
     **/

    @ResponseBody
    @GetMapping("address/support/subway/line")
    public ApiResponse getSubwayByCityName(@RequestParam("city_name") String cityName) throws Exception {

        ServiceMultiResult<SubwayDTO> subways = addressService.findSubwaysByCity(cityName);
        if (subways.getTotal() < 1 || subways.getResult() == null) {
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }

        return ApiResponse.ofSuccess(subways.getResult());



    }
    @ResponseBody
    @GetMapping("address/support/subway/station")
    public ApiResponse getSupportSubwaStation(@RequestParam(name="subway_id") Long subwayId) throws Exception {
        ServiceMultiResult<SubwayStationDTO> subwayStations = addressService.findAllBySubWayId(subwayId);
        if(subwayStations.getResult()==null||subwayStations.getTotal()<1){
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);

        }

        return ApiResponse.ofSuccess(subwayStations.getResult());
    }

    /**
     * 房源搜索页面
     * @param rentSearch
     * @param model
     * @param session
     * @param redirectAttributes
     * @return
     */
    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) throws Exception {

        if (rentSearch.getCityEnName() == null) {
            String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }

        ServiceResult<SupportAddressDTO> city = addressService.findCity(rentSearch.getCityEnName());
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        model.addAttribute("currentCity", city.getResult());

        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(rentSearch.getCityEnName());
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }

        ServiceMultiResult<HouseDTO> serviceMultiResult = houseService.query(rentSearch);

        model.addAttribute("total", serviceMultiResult.getTotal());
        model.addAttribute("houses", serviceMultiResult.getResult());

        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }

        model.addAttribute("searchBody", rentSearch);
        model.addAttribute("regions", addressResult.getResult());

        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }

    /**
     * 房源信息详情页
     * @return
     */
    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId,Model model){
        if (houseId < 0) {
            return "404";

        }
        ServiceResult<HouseDTO> houseDTOServiceResult = houseService.findCompleteOne(houseId);
        if (!houseDTOServiceResult.isSuccess()) {
            return "404";
        }
        HouseDTO houseDTO = houseDTOServiceResult.getResult();
        Map<SupportAddress.Level, SupportAddressDTO> cityAndRegion = addressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());

        SupportAddressDTO  region = cityAndRegion.get(SupportAddress.Level.REGION);
        SupportAddressDTO  city = cityAndRegion.get(SupportAddress.Level.CITY);
        model.addAttribute("city", city);
        model.addAttribute("region", region);
        String district = houseDTO.getDistrict();
        model.addAttribute("house", houseDTO);
        //查询房源的经纪人
        ServiceResult<UserDTO>userDTO  =userService.findByid(houseDTO.getAdminId());
        model.addAttribute("agent",userDTO.getResult());
        ServiceResult<Long> longServiceResult = searchService.aggregateDistrictHouse(city.getEnName(), region.getEnName(), district);
        model.addAttribute("houseCountInDistrict", longServiceResult.getResult());
        return "house-detail";
    }
}
