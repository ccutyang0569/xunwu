package com.founder.xunwu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.founder.xunwu.XunwuApplicationTests;
import com.founder.xunwu.service.search.ISearchService;
import com.founder.xunwu.web.form.RentSearch;
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
import org.junit.Assert;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: xunwu
 * @description:
 * @author: YangMing
 * @create: 2018-03-17 18:10
 **/
public class SearchServiceTest extends XunwuApplicationTests {
    @Autowired
    private ISearchService  searchService;

    private static final  String COLUMN_URI="http://api.map.baidu.com/geodata/v3/column/list?";

    private static final String UPDATE_COLUMN_URI="http://api.map.baidu.com/geodata/v3/column/update";
    @Value("${baidu.map.javaServer.ak}")
    private  String BAIDU_MAP_KEY="VH4n7HtgbHBmDP46Qmdnn75nNRms5w91";

    @Value("${baidu.map.geotableId}")
    private String geoTableId="188303";
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ModelMapper modelMapper;


    private static final Logger logger=LoggerFactory.getLogger(SearchServiceTest.class);
    @Test
    public void test01(){
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setStart(0);
        rentSearch.setSize(10);
        ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);
        System.out.println(serviceResult.toString()+":"+serviceResult.getTotal());

        Assert.assertTrue(serviceResult.getTotal() > 0);

    }
    @Test
   public  void  test02(){
        //Long houseId=28L;
       HttpClient httpClient = HttpClients.createDefault();
       StringBuilder sb = new StringBuilder(COLUMN_URI);
       sb.append("geotable_id=").append(geoTableId).append("&")
               .append("ak=").append(BAIDU_MAP_KEY).append("&");
               //.append("houseId=").append(houseId).append(",").append(houseId);
       HttpGet httpGet = new HttpGet(sb.toString());
       try {
           HttpResponse response = httpClient.execute(httpGet);
           String result = EntityUtils.toString(response.getEntity());
           if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
               logger.error("Can not get lbs data for response: " + result);
               //return false;
           }
           JsonNode jsonNode = objectMapper.readTree(result);
           int status = jsonNode.get("status").asInt();
           if (status != 0) {
               logger.error("Error to get lbs data for status: " + status);
              // return false;
           } else {
               long size = jsonNode.get("size").asLong();
               if (size > 0) {
                   //return true;
               } else {
                   // false;
               }
           }
       } catch (IOException e) {
           e.printStackTrace();
          // return  false;
       }
   }
   @Test
   public  void  test03(){
       String id="330693";
       HttpClient httpClient = HttpClients.createDefault();
       List<NameValuePair> nvps = new ArrayList<>();
       nvps.add(new BasicNameValuePair("id", id));


       //nvps.add(new BasicNameValuePair("is_unique_field", "1"));
       nvps.add(new BasicNameValuePair("is_index_field", "1"));
       nvps.add(new BasicNameValuePair("geotable_id", geoTableId));
       nvps.add(new BasicNameValuePair("ak", BAIDU_MAP_KEY));

       HttpPost  post=new HttpPost(UPDATE_COLUMN_URI);
       try {
           post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
           HttpResponse response = httpClient.execute(post);
           String result = EntityUtils.toString(response.getEntity(), "UTF-8");
           if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
               logger.error("Can not upload lbs data for response:" + result);
               //return new ServiceResult(false, "Can not upload baidu lbs data");
           }else{
               JsonNode jsonNode = objectMapper.readTree(result);
               int status = jsonNode.get("status").asInt();
               if(status!=0){
                   String message = jsonNode.get("message").asText();
                   logger.error("Error to upload lbs data for status:{},and message:{}", status, message);

                 //  return new ServiceResult(false,"Error to upload lbs data");


               }else{
                 //  return ServiceResult.success();
               }

           }

       } catch (Exception e) {
           e.printStackTrace();
       }

   }
}
