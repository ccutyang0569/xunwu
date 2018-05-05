package com.founder.xunwu.service.house;

import com.founder.xunwu.base.HouseSort;
import com.founder.xunwu.base.HouseStatus;
import com.founder.xunwu.base.LoginUserUtil;
import com.founder.xunwu.entity.*;
import com.founder.xunwu.repository.*;
import com.founder.xunwu.service.IQiniuService;
import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.service.search.ISearchService;
import com.founder.xunwu.web.dto.HouseDTO;
import com.founder.xunwu.web.dto.HouseDetailDTO;
import com.founder.xunwu.web.dto.HousePictureDTO;
import com.founder.xunwu.web.form.*;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;

/**
 * @program: xunwu
 * @description:
 * @author: YangMing
 * @create: 2018-02-14 00:43
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class HouseServiceImpl implements IHouseService {
    @Autowired
    private HouseRespository houseRespository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private SubwayRepository subwayRepository;
    @Autowired
    private SubwayStationRespository subwayStationRespository;
    @Autowired
    private HouseDetailRespository houseDetailRespository;
    @Value("${qiniu.cdn.prefix}")
    private String cdnPrefix;

    @Autowired
    private HousePictureRepository housePictureRepository;
    @Autowired
    private HouseTagRepository houseTagRepository;
    @Autowired
    private HouseSubscribeRespository houseSubscribeRespository;

    @Autowired
    private IQiniuService qiniuService;

    @Autowired
    private ISearchService  searchService;

    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        HouseDetail detail = new HouseDetail();
        ServiceResult<HouseDTO> subwayValidatonResult = wrapperDetailInfo(detail, houseForm);
        if (subwayValidatonResult != null) {
            return subwayValidatonResult;

        }
        House house = new House();
        modelMapper.map(houseForm, house);
        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        house = houseRespository.save(house);
        detail.setHouseId(house.getId());
        detail = houseDetailRespository.save(detail);
        List<HousePicture> housePictures = generatePictures(houseForm, house.getId());

        Iterable<HousePicture> saveHousePictures = housePictureRepository.save(housePictures);
        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        houseDTO.setHouseDetail(houseDetailDTO);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        housePictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class)));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(this.cdnPrefix + houseDTO.getCover());
        List<String> tags = houseForm.getTags();
        if (tags != null || !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();


            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagRepository.save(houseTags);

            houseDTO.setTags(tags);


        }

        return new ServiceResult<HouseDTO>(true, null, houseDTO);
    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody) {

        List<HouseDTO> houseDTOS = new ArrayList<>();
        //排序
        Sort sort = new Sort(Sort.Direction.fromString(searchBody
                .getDirection()), searchBody.getOrderBy());
        //页数
        int page = searchBody.getStart() / searchBody.getLength();

        Pageable pageable = new PageRequest(page, searchBody.getLength(), sort);
        Specification<House> specification = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("adminId"), LoginUserUtil.getLoginUserId());

            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatus.DELETED.getValue()));


            if (searchBody.getCity() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("cityEnName"), searchBody.getCity()));

            }
            if (searchBody.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), searchBody.getStatus()));

            }
            if (searchBody.getCreateTimeMin() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createTimeMin"), searchBody.getCreateTimeMin()));


            }
            if (searchBody.getCreateTimeMax() != null) {

                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createTimeMax"), searchBody.getCreateTimeMax()));

            }
            return predicate;
        };
        Page<House> houses = houseRespository.findAll(specification, pageable);

        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);

            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);
        });
        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    /**
     * method_name: findCompleteOne
     * param: [id]
     * return: com.founder.xunwu.service.ServiceResult<com.founder.xunwu.web.dto.HouseDTO>
     * describe: TODO()
     * create_user: YangMing
     * create_date: 2018/2/21 16:21
     **/

    @Override
    public ServiceResult<HouseDTO> findCompleteOne(Long id) {
        House house = houseRespository.findOne(id);
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseDetail houseDetail = houseDetailRespository.findByHouseId(id);
        List<HousePicture> housePictureList = housePictureRepository.findAllByHouseId(id);

        List<HousePictureDTO> housePictureDTOS = new ArrayList<>();

        //转换
        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
        //转换
        for (HousePicture housePicture : housePictureList) {
            HousePictureDTO housePictureDTO = modelMapper.map(housePicture, HousePictureDTO.class);
            housePictureDTOS.add(housePictureDTO);
        }
        List<HouseTag> houseTags = houseTagRepository.findAllByHouseId(id);

        List<String> tagList = new ArrayList<>();

        for (HouseTag houseTag : houseTags) {

            tagList.add(houseTag.getName());

        }
        HouseDTO houseDTO = modelMapper.map(
                house, HouseDTO.class
        );
        //houseDTO
        houseDTO.setHouseDetail(houseDetailDTO);
        houseDTO.setPictures(housePictureDTOS);
        houseDTO.setTags(tagList);
        //用户已经登录
        if (LoginUserUtil.getLoginUserId()>0){

            HouseSubscribe subscribe = houseSubscribeRespository.findByHouseIdAndUserId(house.getId(), LoginUserUtil.getLoginUserId());
            if(subscribe!=null){
             houseDTO.setSubscribeStatus(subscribe.getStatus());
            }
        }
        return ServiceResult.of(houseDTO);
    }

    @Override
    public ServiceResult update(HouseForm houseForm) {
        House house = houseRespository.findOne(houseForm.getId());
        if(house==null){
            return ServiceResult.notFound();
        }
        HouseDetail houseDetail = houseDetailRespository.findByHouseId(house.getId());
        if(houseDetail==null){
            return ServiceResult.notFound();
        }
        ServiceResult<HouseDTO> wrapperDetailInfo = wrapperDetailInfo(houseDetail, houseForm);
        if(wrapperDetailInfo!=null){
            return wrapperDetailInfo;
        }


        houseDetailRespository.save(houseDetail);
        List<HousePicture> housePictures = generatePictures(houseForm, houseForm.getId());
        housePictureRepository.save(housePictures);
        if(houseForm.getCover()==null){
            houseForm.setCover(house.getCover());
        }
        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseRespository.save(house);

        if (house.getStatus() == HouseStatus.PASSES.getValue()) {
            searchService.index(house.getId());
        }

        return ServiceResult.success();
    }

    @Override
    public ServiceResult removePhoto(Long id) {
        HousePicture picture = housePictureRepository.findOne(id);
        if (picture == null) {
            return ServiceResult.notFound();
        }

        try {
            Response response = qiniuService.deleteFile(picture.getPath());
            //七牛删除成功
            if (response.isOK()) {
                //数据库删除相应的图片
                housePictureRepository.delete(id);
                return ServiceResult.success();

            } else {
                return new ServiceResult(false, response.error);
            }

        } catch (QiniuException e) {
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }


    }

    @Override
    public ServiceResult removeTag(Long houseId, String tag) {
        House house = houseRespository.findOne(houseId);
        if (house == null) {
            return ServiceResult.notFound();
        }
        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag, houseId);
        if (houseTag == null) {
            return new ServiceResult(false, "标签不存在");
        }
        houseTagRepository.delete(houseTag.getId());

        return ServiceResult.success();
    }

    @Override
    public ServiceResult addTag(Long houseId, String tag) {

        House house = houseRespository.findOne(houseId);
        if (house == null) {
            return ServiceResult.notFound();
        }
        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag, houseId);
        if (houseTag != null) {
            return new ServiceResult(false, "标签已经存在");
        }

        houseTagRepository.save(new HouseTag(houseId, tag));

        return ServiceResult.success();
    }

    @Override
    public ServiceResult updateStatus(Long id, int value) {

        House house = houseRespository.findOne(id);
        if (house == null) {
            return ServiceResult.notFound();
        }
        if(house.getStatus()==value){

            return  new ServiceResult(false,"房屋的状态没有发生变化");
        }
        if (house.getStatus() == HouseStatus.RENTED.getValue()) {
            return new ServiceResult(false, "已经出租的房源不允许修改");
        }
        if (house.getStatus() == HouseStatus.DELETED.getValue()) {
            return new ServiceResult(false, "已经删除的房源不允许操作");
        }
        houseRespository.updateStatus(id,value);
        // 上架更新索引 其他情况都要删除索引
        if (value == HouseStatus.PASSES.getValue()) {
            searchService.index(id);
        } else {
            searchService.remove(id);
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult updateCover(Long coverId, Long targetId) {
        HousePicture housePicture = housePictureRepository.findOne(coverId);
        if (housePicture == null) {
            return ServiceResult.notFound();
        }
      houseRespository.updateCover(coverId,targetId);
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
            ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);
            if (serviceResult.getTotal() == 0) {
                return new ServiceMultiResult<>(0, new ArrayList<>());
            }
            return new ServiceMultiResult<>(serviceResult.getTotal(), wrapperHouseResult(serviceResult.getResult()));
        }

        return simpleQuery(rentSearch);
    }

    @Override
    public ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch) {
        ServiceMultiResult<Long> result=searchService.mapQuery(mapSearch.getCityEnName(),mapSearch.getOrderBy(),mapSearch.getOrderDirection(),
                mapSearch.getStart(),mapSearch.getSize());
        if (result.getTotal() ==0) {
            return new ServiceMultiResult<>(0,new ArrayList<>());
        }
        List<HouseDTO> houses = wrapperHouseResult(result.getResult());
        return new ServiceMultiResult<>(result.getTotal(), houses);

    }

    @Override
    public ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch) {
        ServiceMultiResult<Long> serviceMultiResult = searchService.mapQuery(mapSearch);
        if (serviceMultiResult.getTotal() == 0) {
            return new ServiceMultiResult<>(0, new ArrayList<>());
        }
        List<HouseDTO> houses = wrapperHouseResult(serviceMultiResult.getResult());
        return new ServiceMultiResult<>(serviceMultiResult.getTotal(), houses);

    }

    private List<HouseDTO> wrapperHouseResult(List<Long> houseIds) {
        List<HouseDTO> result = new ArrayList<>();
        Map<Long, HouseDTO> idToHosueMap = new HashMap<>();
        Iterable<House> houses = houseRespository.findAll(houseIds);
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            idToHosueMap.put(house.getId(), houseDTO);
        });
        wrapperHouseList(houseIds, idToHosueMap);
        //矫正顺序
        for (Long houseId : houseIds) {
            result.add(idToHosueMap.get(houseId));
        }
        return  result;

    }


    public ServiceMultiResult simpleQuery(RentSearch rentSearch) {
        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(),rentSearch.getOrderDirection());
        int page = rentSearch.getStart() / rentSearch.getSize();
        Pageable pageable = new PageRequest(page, rentSearch.getSize(), sort);
        Specification<House> specification = (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatus.PASSES.getValue());
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"), rentSearch.getCityEnName()));

            if (HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())) {
                predicate=criteriaBuilder.and(predicate,criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY),-1));
            }

            return predicate;
        };
        Page<House> houses = houseRespository.findAll(specification, pageable);
        List<HouseDTO> houseDTOS = new ArrayList<>();
        //房源ID集合
        List<Long> houseIds = new ArrayList<>();
        //房源ID与房源DTO
        Map<Long, HouseDTO> idToHOuseMap = new HashMap<>();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix+house.getCover());
            houseDTOS.add(houseDTO);
            houseIds.add(house.getId());
            idToHOuseMap.put(house.getId(), houseDTO);


        });
        wrapperHouseList(houseIds,idToHOuseMap);

        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    /**
     * 渲染房源详细信息以及标签
     * @param houseIds
     * @param idToHOuseMap
     */
    private void wrapperHouseList(List<Long> houseIds, Map<Long, HouseDTO> idToHOuseMap) {
        List<HouseDetail> houseDetails = houseDetailRespository.findAllByHouseIdIn(houseIds);
        houseDetails.forEach(houseDetail -> {

            HouseDTO houseDTO = idToHOuseMap.get(houseDetail.getHouseId());
            HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(houseDetailDTO);

        });
        List<HouseTag> houseTags = houseTagRepository.findAllByHouseIdIn(houseIds);
        houseTags.forEach(houseTag -> {
            HouseDTO houseDTO = idToHOuseMap.get(houseTag.getHouseId());
            List<String> tags = houseDTO.getTags();
            tags.add(houseTag.getName());
        });


    }


    private List<HousePicture> generatePictures(HouseForm houseForm, Long id) {
        List<HousePicture> pictures = new ArrayList<>();
        if (houseForm.getPhotos() == null || houseForm.getPhotos().isEmpty()) {
            return pictures;
        }
        for (PhotoForm photoForm : houseForm.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(id);
            picture.setCdnPrefix(cdnPrefix);
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setHeight(photoForm.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }

    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail detail, HouseForm houseForm) {
        Subway subway = subwayRepository.findOne(houseForm.getSubwayLineId());
        if (subway == null) {
            return new ServiceResult<>(false, "Not valid subway line !");
        }

        SubwayStation subwayStation = subwayStationRespository.findOne(houseForm.getSubwayStationId());
        if (subwayStation == null || subway.getId() != subwayStation.getSubWayId()) {
            return new ServiceResult<>(false, "Not valid subway station !");
        }

        detail.setSubwayLineId(subway.getId());
        detail.setSubwayLineName(subway.getName());
        detail.setSubwayStationId(subwayStation.getId());
        detail.setSubwayStationName(subwayStation.getName());
        detail.setDescription(houseForm.getDescription());
        detail.setDetailAddress(houseForm.getDetailAddress());
        detail.setLayoutDesc(houseForm.getLayoutDesc());
        detail.setRentWay(houseForm.getRentWay());
        detail.setRoundService(houseForm.getRoundService());
        detail.setTraffic(houseForm.getTraffic());
        return null;
    }
}
