package com.founder.xunwu.service.search;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.founder.xunwu.base.HouseSort;
import com.founder.xunwu.base.RentValueBlock;
import com.founder.xunwu.entity.House;
import com.founder.xunwu.entity.HouseDetail;
import com.founder.xunwu.entity.HouseTag;
import com.founder.xunwu.entity.SupportAddress;
import com.founder.xunwu.repository.HouseDetailRespository;
import com.founder.xunwu.repository.HouseRespository;
import com.founder.xunwu.repository.HouseTagRepository;
import com.founder.xunwu.repository.SupportAddressRepository;
import com.founder.xunwu.service.ServiceMultiResult;
import com.founder.xunwu.service.ServiceResult;
import com.founder.xunwu.service.house.IAddressService;
import com.founder.xunwu.web.form.RentSearch;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: xunwu
 * @description: 搜索服务类
 * @author: YangMing
 * @create: 2018-03-03 16:08
 **/
@Service
public class SerachServiceImpl implements ISearchService {


    private static final Logger logger = LoggerFactory.getLogger(SerachServiceImpl.class);

    private static final String INDEX_NAME = "xunwu";

    private static final String INDEX_TYPE = "house";

    private static final String INDEX_TOPIC = "house_build";

    @Autowired
    private HouseRespository houseRespository;

    @Autowired
    private HouseDetailRespository houseDetailRespository;

    @Autowired
    private HouseTagRepository houseTagRepository;
    @Autowired
    private SupportAddressRepository supportAddressRepository;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private TransportClient esClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = INDEX_TOPIC)
    public void handleMassage(String content) {

        try {
            HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);
            switch (message.getOperation()) {

                case HouseIndexMessage.INDEX:
                    this.createOrUpdate(message);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(message);
                    break;
                default:
                    logger.warn("Not support message content" + content);
                    break;
            }

        } catch (IOException e) {
            logger.error("Cannot parse json for" + content, e);
        }

    }

    private void removeIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction
                .INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        logger.debug("Detail total" + deleted);

    }


    private void createOrUpdate(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        House house = houseRespository.findOne(houseId);
        if (house == null) {
            logger.error("Index house {} dose not Exist", houseId);
            this.index(houseId, message.getRetry() + 1);
            return;
        }
        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, indexTemplate);

        HouseDetail detail = houseDetailRespository.findByHouseId(houseId);
        if (detail == null) {
            // TODO 异常情况
        }
        modelMapper.map(detail, indexTemplate);
        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(house.getCityEnName(), SupportAddress.Level.CITY.getValue());

        SupportAddress region = supportAddressRepository.findByEnNameAndLevel(house.getCityEnName(), SupportAddress.Level.CITY.getValue());
        // String address = city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict() + detail.getDetailAddress();

        List<HouseTag> tags = houseTagRepository.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagsString = new ArrayList<>();
            tags.forEach(houseTag -> tagsString.add(houseTag.getName()));
            indexTemplate.setTags(tagsString);

        }

        //房源索引

        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE).setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));

        logger.debug(requestBuilder.toString());

        SearchResponse searchResponse = requestBuilder.get();
        boolean success;
        long totalHit = searchResponse.getHits().getTotalHits();
        if (totalHit == 0) {
            success = create(indexTemplate);
        } else if (totalHit == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(esId, indexTemplate);

        } else {
            success = deleteAndCreate(totalHit, indexTemplate);
        }


    }


    @Override
    public void index(Long houseId) {

        this.index(houseId, 0);

    }

    private void index(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry index times over 3 for house" + houseId + "Please check it");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error(" Json encode error for " + message);
        }
    }

    @Override
    public void remove(Long houseId) {

        this.remove(houseId, 0);

    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(
                QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName())
        );
        //区域
        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName())

            );

        }

        //房屋面价
        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            //最大面积
            if (area.getMax() > 0) {
                rangeQueryBuilder.lte(area.getMax());
            }
            //最小面积
            if (area.getMin() > 0) {
                rangeQueryBuilder.gte(area.getMin());


            }
            boolQuery.filter(rangeQueryBuilder);
        }
        //房屋价格
        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            //最大价格
            if (price.getMax() > 0) {
                rangeQueryBuilder.lte(price.getMax());

            }
            //最小价格
            if (price.getMin() > 0) {
                rangeQueryBuilder.gte(price.getMin());
            }
            boolQuery.filter(rangeQueryBuilder);
        }
        //房屋朝向
        if (rentSearch.getDirection() > 0) {
            boolQuery.filter(

                    QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection())
            );

        }
        //房屋出租方式
        if (rentSearch.getRentWay()>-1){

            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay())
            );
        }


        boolQuery.should(QueryBuilders.matchQuery
                (HouseIndexKey.TITLE,rentSearch.getKeywords())
                .boost(2.0f)
        );
        boolQuery.should(
            QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),

                    HouseIndexKey.TRAFFIC,
                    HouseIndexKey.DISTRICT,
                    HouseIndexKey.ROUND_SERVICE,
                    HouseIndexKey.SUBWAY_LINE_NAME,
                    HouseIndexKey.SUBWAY_STATION_NAME)
        );


        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolQuery)
                .addSort(
                        HouseSort.getSortKey(rentSearch.getOrderBy()),
                        SortOrder.fromString(rentSearch.getOrderDirection())
                ).setFrom(rentSearch.getStart())
                .setSize(rentSearch.getSize())
                //调优
                .setFetchSource(HouseIndexKey.HOUSE_ID, null);
         logger.debug(searchRequestBuilder.toString());

         List<Long> houseIds=new ArrayList<>();
        SearchResponse response = searchRequestBuilder.get();
        if (response.status()!=RestStatus.OK){
            logger.warn("Search status is not ok "+ searchRequestBuilder);
            return new ServiceMultiResult<>(0, houseIds);
        }

        for (SearchHit hit:response.getHits()){
            System.out.println(hit.getScore());
            houseIds.add(Longs.tryParse(String.valueOf(hit.getSource().get(HouseIndexKey.HOUSE_ID))));
        }
        return new ServiceMultiResult<>(response.getHits().totalHits, houseIds);
    }

    @Override
    public ServiceResult<String> suggest(String prefix) {
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autocomplete", suggestion);

        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME).
                setTypes(INDEX_TYPE)
                .suggest(suggestBuilder);

        logger.debug(requestBuilder.toString());
        SearchResponse response = requestBuilder.get();
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return ServiceResult.of(new ArrayList<>());
        }
        Suggest.Suggestion  result = suggest.getSuggestion("autocomplete");
        int maxSuggest=0;

        Set<String> suggestSet = new HashSet<>();
        for (Object term :result.getEntries()){
            if (term instanceof CompletionSuggestion.Entry) {
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;



                if(item.getOptions().isEmpty()){
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                    String tip = option.getText().string();
                    if (suggestSet.contains(tip)) {
                        continue;
                    }
                    suggestSet.add(tip);
                    maxSuggest++;
                }
            }
            if (maxSuggest > 5) {
                break;
            }

        }

        List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
        return ServiceResult.of(suggests);
    }

    @Override
    public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, regionEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT, district));

        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolQuery)
                .addAggregation(AggregationBuilders.terms(HouseIndexKey.AGG_DISTRICT)
                        .field(HouseIndexKey.DISTRICT))

                .setSize(0);
        logger.debug(requestBuilder.toString());
        SearchResponse response = requestBuilder.get();
        if (response.status()== RestStatus.OK){
            Terms terms = response.getAggregations().get(HouseIndexKey.AGG_DISTRICT);
            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
                return ServiceResult.of(terms.getBucketByKey(district).getDocCount());
            }else{

                logger.warn(" Faild to Aggregate for "+HouseIndexKey.AGG_DISTRICT);

            }
            return ServiceResult.of(0L);
        }
        return null;
    }

    private boolean updateSuggest(HouseIndexTemplate indexTemplate){
        AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(
                this.esClient, AnalyzeAction.INSTANCE, INDEX_NAME, indexTemplate.getTitle(),
                indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
                indexTemplate.getDescription(), indexTemplate.getSubwayLineName(),
                indexTemplate.getSubwayLineName()
        );
        requestBuilder.setAnalyzer("ik_smart");

        AnalyzeResponse response = requestBuilder.get();

        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
        if(tokens==null){
          logger.warn("Can not analyze token for house:"+indexTemplate.getHouseId());
          return false;
        }
        List<HouseSuggest> suggests = new ArrayList<>();
        for(AnalyzeResponse.AnalyzeToken token:tokens){
            //排序数字类型& 小于2个字符的分词结果
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                continue;

            }
            HouseSuggest suggest=new HouseSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);
        }
        //定制小区自动化
        HouseSuggest suggest=new HouseSuggest();

        suggest.setInput(indexTemplate.getDistrict());
        suggests.add(suggest);
        indexTemplate.setSuggest(suggests);
        return  true;

    }
    private void remove(Long houseId,int retry){

        if(retry>HouseIndexMessage.MAX_RETRY){
            logger.error("Retry remove times overs 3 for house:"+houseId+"Please check id !");
            return ;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            this.kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Cannot encode json for " + message, e);
        }
    }

    /**
     * 创建索引
     * @param houseIndexTemplate
     * @return
     */
    public boolean create(HouseIndexTemplate houseIndexTemplate) {
        if (!updateSuggest(houseIndexTemplate)) {
            return false;
        }
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
        if (!updateSuggest(houseIndexTemplate)) {
            return false;
        }

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
    public boolean deleteAndCreate(Long totalHit, HouseIndexTemplate houseIndexTemplate) {

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
