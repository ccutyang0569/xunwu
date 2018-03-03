package com.founder.xunwu.service.search;


import com.founder.xunwu.repository.HouseDetailRespository;
import com.founder.xunwu.repository.HouseRespository;
import com.founder.xunwu.repository.HouseTagRepository;
import com.founder.xunwu.repository.SupportAddressRepository;
import com.founder.xunwu.service.house.IAddressService;
import org.elasticsearch.client.transport.TransportClient;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: xunwu
 * @description: 搜索服务类
 * @author: YangMing
 * @create: 2018-03-03 16:08
 **/
@Service
public class SerachServiceImpl implements ISerachService {


    private static final Logger logger = LoggerFactory.getLogger(SerachServiceImpl.class);

    private static final String INDEX_NAME = "xunwu";

    private static final String INDEX_TYPE = "house";

    private static final String INDEX_TOPIC = "house_build";

    @Autowired
    private HouseRespository  hosueRespository;

    @Autowired
    private HouseDetailRespository houseDetailRespository;

    @Autowired
    private HouseTagRepository  houseTagRepository;
    @Autowired
    private SupportAddressRepository supportAddressRepository;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private TransportClient    esClient;




    @Override
    public void index(Long houseId) {

    }

    @Override
    public void remove(Long houseId) {

    }
}
