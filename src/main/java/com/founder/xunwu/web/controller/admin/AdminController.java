package com.founder.xunwu.web.controller.admin;

import com.founder.xunwu.base.ApiDataTableResponse;
import com.founder.xunwu.base.ApiResponse;
import com.founder.xunwu.base.HouseOperation;
import com.founder.xunwu.base.HouseStatus;
import com.founder.xunwu.entity.SupportAddress;
import com.founder.xunwu.service.IQiniuService;
import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.service.house.IAddressService;
import com.founder.xunwu.service.house.IHouseService;
import com.founder.xunwu.web.dto.*;
import com.founder.xunwu.web.form.DatatableSearch;
import com.founder.xunwu.web.form.HouseForm;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @program: xunwu
 * @description: 管理员controller
 * @author: YangMing
 * @create: 2018-02-03 16:17
 **/
@Controller
public class AdminController {


    @Autowired
    private IQiniuService qiniuService;
    @Autowired
    private Gson gson;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IHouseService houseService;


    /**
     * 后台管理中心
     *
     * @return
     */
    @GetMapping("/admin/center")
    public String adminCenterPage() {
        return "admin/center";
    }

    /**
     * 欢迎页
     *
     * @return
     */
    @GetMapping("/admin/welcome")
    public String welcomePage() {
        return "admin/welcome";
    }

    /**
     * 管理员登录页
     *
     * @return
     */
    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }


    /**
     * 房源列表页
     *
     * @return
     */
    @GetMapping("admin/house/list")
    public String houseListPage() {
        return "admin/house-list";
    }

    @PostMapping("admin/houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DatatableSearch searchBody){
        ServiceMultiResult<HouseDTO> result =houseService.adminQuery(searchBody);
        ApiDataTableResponse response = new ApiDataTableResponse(ApiResponse.Status.SUCCESS);

        response.setData(result.getResult());
        response.setRecordsTotal(result.getTotal());
        response.setRecordsFilterd(result.getTotal());
        response.setDraw(searchBody.getDraw());
        return response;

    }




    /**
     * 新增房源页面
     *
     * @return
     */
    @GetMapping("admin/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }

    /**
     * method_name: uploadPhoto
     * param: [file]
     * return: com.founder.xunwu.base.ApiResponse
     * describe: TODO(房源图片上传)
     * create_user: YangMing
     * create_date: 2018/2/6 9:55
     **/
    @PostMapping(value = "admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        String originalFilename = file.getOriginalFilename();

        try {
            InputStream inputStream = file.getInputStream();
            Response response = qiniuService.uploadFile(inputStream);

            if (response.isOK()) {
                QINIUPutRet qiniuPutRet = gson.fromJson(response.bodyString(), QINIUPutRet.class);
                System.out.println(qiniuPutRet.toString());
                return ApiResponse.ofSuccess(qiniuPutRet);
            } else {
                return ApiResponse.ofMessage(response.statusCode, response.getInfo());
            }
        } catch (QiniuException e) {
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode, response.bodyString());
            } catch (QiniuException e1) {
                return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
            }

        } catch (IOException e1) {
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("admin/add/house")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add") HouseForm houseForm, BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage(), null);

        }
        if (houseForm.getPhotos() == null || houseForm.getCover() == null) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须上传图片");
        }

        Map<SupportAddress.Level, SupportAddressDTO> cityAndRegion =
                addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (cityAndRegion.keySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);

        }
        ServiceResult<HouseDTO> result = houseService.save(houseForm);


        if(result.isSuccess()){
            return ApiResponse.ofSuccess(result.getResult());
        }

        return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
    }

    @GetMapping("admin/house/edit")
    public String houseEditPage(@RequestParam("id") Long id, Model model) {
        if(id==null||id<1){
          return "404";
        }

        ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(id);
        if(!serviceResult.isSuccess()){
            return "404";
        }
        HouseDTO houseDTO = serviceResult.getResult();


        model.addAttribute("house", houseDTO);

        Map<SupportAddress.Level, SupportAddressDTO> cityAndRegion = addressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());

        model.addAttribute("city", cityAndRegion.get(SupportAddress.Level.CITY));
        model.addAttribute("region", cityAndRegion.get(SupportAddress.Level.REGION));

        HouseDetailDTO houseDetail = houseDTO.getHouseDetail();
        ServiceResult<SubwayDTO> subwayServiceResult = addressService.findSubway(houseDetail.getSubwayLineId());

        if (subwayServiceResult.isSuccess()){
            model.addAttribute("subway", subwayServiceResult.getResult());

        }

        ServiceResult<SubwayStationDTO> subwayStationServiceResult = addressService.findSubwayStation(houseDetail.getSubwayStationId());
        if (subwayStationServiceResult.isSuccess()) {
            model.addAttribute("station", subwayStationServiceResult.getResult());
        }

        return "admin/house-edit";


    }
    @PostMapping("admin/house/edit")
    @ResponseBody
    public ApiResponse saveHouse(@Valid @ModelAttribute("from-house-edit") HouseForm houseForm,BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage(), null);
        }
        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (addressMap.keySet().size()!=2){
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
        }
       ServiceResult  result= houseService.update(houseForm);
        if(result.isSuccess()){
            return ApiResponse.ofSuccess(null);
        }

        ApiResponse response = ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        response.setMessage(result.getMessage());

        return response;
    }

    /**
     * 删除图片接口
     * @param id
     * @return
     */
    @DeleteMapping("admin/house/photo")
    @ResponseBody
    public ApiResponse removeHousePhoto(@RequestParam("id") Long id){
        ServiceResult result = houseService.removePhoto(id);
        if(result.isSuccess()){
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }else{
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }


    }

    /**
     * 增加标签接口
     * @param houseId
     * @param tag
     * @return
     */
   @PostMapping("admin/house/tag")
   @ResponseBody
   public ApiResponse addHouseTag( @RequestParam(value="house_id") Long houseId,@RequestParam(value="tag")String tag){
       if (houseId < 1 || Strings.isNullOrEmpty(tag)) {
           return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
       }

       ServiceResult result = houseService.addTag(houseId, tag);
       if (result.isSuccess()) {
           return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
       }else{
           return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
       }



   }
    @PostMapping("admin/house/cover")
    public ApiResponse updateHouseCover(@RequestParam(value = "cover_id") Long coverId,
                                        @RequestParam(value = "target_id") Long targetId) {
        ServiceResult result = houseService.updateCover(coverId, targetId);
        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }


    }

    /**
     * 移除标签接口
     * @param houseId
     * @param tag
     * @return
     */
    @DeleteMapping("admin/house/tag")
    @ResponseBody
    public ApiResponse removeHouseTag(@RequestParam(value="house_id")Long houseId,@RequestParam(value="tag")String tag){
        if (houseId < 1 || Strings.isNullOrEmpty(tag)) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
         ServiceResult result  =houseService.removeTag(houseId, tag);
        return null;
    }


    /**
     * 房屋审核接口
     * @param id
     * @param operation
     * @return
     */
    @PutMapping("admin/house/operate/{id}/{operation}")
    @ResponseBody
    public ApiResponse operateHouse(@PathVariable(value = "id") Long id, @PathVariable(value = "operation") int operation) {
        if (id < 0) {

            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        ServiceResult result;
        switch (operation) {
            case HouseOperation.PASS:
                result = houseService.updateStatus(id, HouseStatus.PASSES.getValue());
                break;
            case HouseOperation.PULL_OUT:
                result = houseService.updateStatus(id, HouseStatus.NOT_AUDITED.getValue());
                break;
            case HouseOperation.DELETE:
                result = houseService.updateStatus(id, HouseStatus.DELETED.getValue());
                break;
            case HouseOperation.RENT:
                result = houseService.updateStatus(id, HouseStatus.RENTED.getValue());
                break;

            default:
                return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        if (result.isSuccess()) {

            return ApiResponse.ofSuccess(null);
        }

        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST
                .value(), result.getMessage());

    }
    @GetMapping("admin/house/subscribe")
    public String houseSubscribe(){
        return "admin/subscribe";
    }


}
