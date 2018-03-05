package com.founder.xunwu.service.search;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.founder.xunwu.repository.HouseDetailRespository;
import com.founder.xunwu.repository.HouseRespository;
import com.founder.xunwu.repository.HouseTagRepository;
import com.founder.xunwu.repository.SupportAddressRepository;
import com.founder.xunwu.service.house.IAddressService;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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

    @Autowired
    private ObjectMapper  objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;




    @Override
    public void index(Long houseId) {

    }

    @Override
    public void remove(Long houseId) {

    }

    /**
     * 创建索引
     * @param houseIndexTemplate
     * @return
     */
    public boolean create(HouseIndexTemplate houseIndexTemplate) {
        try {
            IndexResponse indexResponse = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE).setSource(objectMapper.writeValueAsBytes(houseIndexTemplate), XContentType.JSON).get();
            logger.debug("create index with house:"+houseIndexTemplate.getHouseId());

            if (indexResponse.status() == RestStatus.CREATED) {
                return true;
            }else{
                return false;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error to index house:" + houseIndexTemplate.getHouseId());
            return false;
        }
    }

    /**
     * update index
     * @param esId
     * @param houseIndexTemplate
     * @return
     */
    public  boolean update(String esId,HouseIndexTemplate houseIndexTemplate) {

        try {
            UpdateResponse updateResponse = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId).setDoc(objectMapper.writeValueAsBytes(houseIndexTemplate), XContentType.JSON).get();
            logger.debug("create index with house:"+houseIndexTemplate.getHouseId());

            if (updateResponse.status() == RestStatus.OK) {
                return true;
            }else{
                return false;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error to index house:" + houseIndexTemplate.getHouseId());
            return false;
        }
    }

    /**
     *
     * @param totalHit
     * @param houseIndexTemplate
     * @return
     */
    public boolean deleteAndCreate(int totalHit, HouseIndexTemplate houseIndexTemplate) {

        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseIndexTemplate.getHouseId()))
                .source(INDEX_NAME);
        logger.debug(" lDelete by query for house: "+ builder);

        BulkByScrollResponse reponse = builder.get();
        if (totalHit != reponse.getDeleted()) {
             logger.warn("Need delete {}, but {} was deleted !",totalHit,reponse.getDeleted());
             return false;
        }else{
            return create(houseIndexTemplate);
        }


    }
}
