package com.founder.xunwu.service;

import com.founder.xunwu.XunwuApplicationTests;
import com.founder.xunwu.service.search.ISearchService;
import com.founder.xunwu.web.form.RentSearch;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: xunwu
 * @description:
 * @author: YangMing
 * @create: 2018-03-17 18:10
 **/
public class SearchServiceTest extends XunwuApplicationTests {
    @Autowired
    private ISearchService  searchService;


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



}
