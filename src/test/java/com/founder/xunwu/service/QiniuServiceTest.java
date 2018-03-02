package com.founder.xunwu.service;

import com.founder.xunwu.XunwuApplicationTests;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: xunwu
 * @description: 七牛云Test
 * @author: YangMing
 * @create: 2018-02-06 13:14
 **/
public class QiniuServiceTest extends XunwuApplicationTests {
    @Autowired
    private IQiniuService  qiniuService;


    @Test
    public void testRemovePhoto() throws QiniuException {
        String key="FlzH2UTWuyxPNj16dUzuGzLApfwb";
        Response response = qiniuService.deleteFile(key);
        System.out.println(response.isOK());

    }
}
